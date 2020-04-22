/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g3d;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FlushablePool;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

/** ModelCache tries to combine multiple render calls into a single render call by merging them where possible. Can be used for
 * multiple type of models (e.g. varying vertex attributes or materials), the ModelCache will combine where possible. Can be used
 * dynamically (e.g. every frame) or statically (e.g. to combine part of scenery). Be aware that any combined vertices are
 * directly transformed, therefore the resulting {@link Renderable#worldTransform} might not be suitable for sorting anymore (such
 * as the default sorter of ModelBatch does).
 * @author Xoppa */
public class ModelCache implements Disposable, RenderableProvider {
	/** Allows to reuse one or more meshes while avoiding creating new objects. Depending on the implementation it might add memory
	 * optimizations as well. Call the {@link #obtain(VertexAttributes, int, int)} method to obtain a mesh which can at minimum the
	 * specified amount of vertices and indices. Call the {@link #flush()} method to flush the pool ant release all previously
	 * obtained meshes. */
	public interface MeshPool extends Disposable {
		/** Will try to reuse or, when not possible to reuse, optionally create a {@link Mesh} that meets the specified criteria.
		 * @param vertexAttributes the vertex attributes of the mesh to obtain
		 * @param vertexCount the minimum amount vertices the mesh should be able to store
		 * @param indexCount the minimum amount of indices the mesh should be able to store
		 * @return the obtained Mesh, or null when no mesh could be obtained. */
		Mesh obtain (VertexAttributes vertexAttributes, int vertexCount, int indexCount);

		/** Releases all previously obtained {@link Mesh}es using the the {@link #obtain(VertexAttributes, int, int)} method. */
		void flush ();
	}

	/** A basic {@link MeshPool} implementation that avoids creating new meshes at the cost of memory usage. It does this by making
	 * the mesh always the maximum (64k) size. Use this when for dynamic caching where you need to obtain meshes very frequently
	 * (typically every frame).
	 * @author Xoppa */
	public static class SimpleMeshPool implements MeshPool {
		// FIXME Make a better (preferable JNI) MeshPool implementation
		private Array<Mesh> freeMeshes = new Array<Mesh>();
		private Array<Mesh> usedMeshes = new Array<Mesh>();

		@Override
		public void flush () {
			freeMeshes.addAll(usedMeshes);
			usedMeshes.clear();
		}

		@Override
		public Mesh obtain (VertexAttributes vertexAttributes, int vertexCount, int indexCount) {
			for (int i = 0, n = freeMeshes.size; i < n; ++i) {
				final Mesh mesh = freeMeshes.get(i);
				if (mesh.getVertexAttributes().equals(vertexAttributes) && mesh.getMaxVertices() >= vertexCount
					&& mesh.getMaxIndices() >= indexCount) {
					freeMeshes.removeIndex(i);
					usedMeshes.add(mesh);
					return mesh;
				}
			}
			vertexCount = MeshBuilder.MAX_VERTICES;
			indexCount = Math.max(vertexCount, 1 << (32 - Integer.numberOfLeadingZeros(indexCount - 1)));
			Mesh result = new Mesh(false, vertexCount, indexCount, vertexAttributes);
			usedMeshes.add(result);
			return result;
		}

		@Override
		public void dispose () {
			for (Mesh m : usedMeshes)
				m.dispose();
			usedMeshes.clear();
			for (Mesh m : freeMeshes)
				m.dispose();
			freeMeshes.clear();
		}
	}

	/** A tight {@link MeshPool} implementation, which is typically used for static meshes (create once, use many).
	 * @author Xoppa */
	public static class TightMeshPool implements MeshPool {
		private Array<Mesh> freeMeshes = new Array<Mesh>();
		private Array<Mesh> usedMeshes = new Array<Mesh>();

		@Override
		public void flush () {
			freeMeshes.addAll(usedMeshes);
			usedMeshes.clear();
		}

		@Override
		public Mesh obtain (VertexAttributes vertexAttributes, int vertexCount, int indexCount) {
			for (int i = 0, n = freeMeshes.size; i < n; ++i) {
				final Mesh mesh = freeMeshes.get(i);
				if (mesh.getVertexAttributes().equals(vertexAttributes) && mesh.getMaxVertices() == vertexCount
					&& mesh.getMaxIndices() == indexCount) {
					freeMeshes.removeIndex(i);
					usedMeshes.add(mesh);
					return mesh;
				}
			}
			Mesh result = new Mesh(true, vertexCount, indexCount, vertexAttributes);
			usedMeshes.add(result);
			return result;
		}

		@Override
		public void dispose () {
			for (Mesh m : usedMeshes)
				m.dispose();
			usedMeshes.clear();
			for (Mesh m : freeMeshes)
				m.dispose();
			freeMeshes.clear();
		}
	}

	/** A {@link RenderableSorter} that sorts by vertex attributes, material attributes and primitive types (in that order), so that
	 * meshes can be easily merged.
	 * @author Xoppa */
	public static class Sorter implements RenderableSorter, Comparator<Renderable> {
		@Override
		public void sort (Camera camera, Array<Renderable> renderables) {
			renderables.sort(this);
		}

		@Override
		public int compare (Renderable arg0, Renderable arg1) {
			final VertexAttributes va0 = arg0.meshPart.mesh.getVertexAttributes();
			final VertexAttributes va1 = arg1.meshPart.mesh.getVertexAttributes();
			final int vc = va0.compareTo(va1);
			if (vc == 0) {
				final int mc = arg0.material.compareTo(arg1.material);
				if (mc == 0) {
					return arg0.meshPart.primitiveType - arg1.meshPart.primitiveType;
				}
				return mc;
			}
			return vc;
		}
	}

	private Array<Renderable> renderables = new Array<Renderable>();
	private FlushablePool<Renderable> renderablesPool = new FlushablePool<Renderable>() {
		@Override
		protected Renderable newObject () {
			return new Renderable();
		}
	};
	private FlushablePool<MeshPart> meshPartPool = new FlushablePool<MeshPart>() {
		@Override
		protected MeshPart newObject () {
			return new MeshPart();
		}
	};

	private Array<Renderable> items = new Array<Renderable>();
	private Array<Renderable> tmp = new Array<Renderable>();

	private MeshBuilder meshBuilder;
	private boolean building;
	private RenderableSorter sorter;
	private MeshPool meshPool;
	private Camera camera;

	/** Create a ModelCache using the default {@link Sorter} and the {@link SimpleMeshPool} implementation. This might not be the
	 * most optimal implementation for you use-case, but should be good to start with. */
	public ModelCache () {
		this(new Sorter(), new SimpleMeshPool());
	}

	/** Create a ModelCache using the specified {@link RenderableSorter} and {@link MeshPool} implementation. The
	 * {@link RenderableSorter} implementation will be called with the camera specified in {@link #begin(Camera)}. By default this
	 * will be null. The sorter is important for optimizing the cache. For the best result, make sure that renderables that can be
	 * merged are next to each other. */
	public ModelCache (RenderableSorter sorter, MeshPool meshPool) {
		this.sorter = sorter;
		this.meshPool = meshPool;
		meshBuilder = new MeshBuilder();
	}

	/** Begin creating the cache, must be followed by a call to {@link #end()}, in between these calls one or more calls to one of
	 * the add(...) methods can be made. Calling this method will clear the cache and prepare it for creating a new cache. The
	 * cache is not valid until the call to {@link #end()} is made. Use one of the add methods (e.g. {@link #add(Renderable)} or
	 * {@link #add(RenderableProvider)}) to add renderables to the cache. */
	public void begin () {
		begin(null);
	}

	/** Begin creating the cache, must be followed by a call to {@link #end()}, in between these calls one or more calls to one of
	 * the add(...) methods can be made. Calling this method will clear the cache and prepare it for creating a new cache. The
	 * cache is not valid until the call to {@link #end()} is made. Use one of the add methods (e.g. {@link #add(Renderable)} or
	 * {@link #add(RenderableProvider)}) to add renderables to the cache.
	 * @param camera The {@link Camera} that will passed to the {@link RenderableSorter} */
	public void begin (Camera camera) {
		if (building) throw new GdxRuntimeException("Call end() after calling begin()");
		building = true;

		this.camera = camera;
		renderablesPool.flush();
		renderables.clear();
		items.clear();
		meshPartPool.flush();
		meshPool.flush();
	}

	private Renderable obtainRenderable (Material material, int primitiveType) {
		Renderable result = renderablesPool.obtain();
		result.bones = null;
		result.environment = null;
		result.material = material;
		result.meshPart.mesh = null;
		result.meshPart.offset = 0;
		result.meshPart.size = 0;
		result.meshPart.primitiveType = primitiveType;
		result.meshPart.center.set(0, 0, 0);
		result.meshPart.halfExtents.set(0, 0, 0);
		result.meshPart.radius = -1f;
		result.shader = null;
		result.userData = null;
		result.worldTransform.idt();
		return result;
	}

	/** Finishes creating the cache, must be called after a call to {@link #begin()}, only after this call the cache will be valid
	 * (until the next call to {@link #begin()}). Calling this method will process all renderables added using one of the add(...)
	 * methods and will combine them if possible. */
	public void end () {
		if (!building) throw new GdxRuntimeException("Call begin() prior to calling end()");
		building = false;

		if (items.size == 0) return;
		sorter.sort(camera, items);

		int itemCount = items.size;
		int initCount = renderables.size;

		final Renderable first = items.get(0);
		VertexAttributes vertexAttributes = first.meshPart.mesh.getVertexAttributes();
		Material material = first.material;
		int primitiveType = first.meshPart.primitiveType;
		int offset = renderables.size;

		meshBuilder.begin(vertexAttributes);
		MeshPart part = meshBuilder.part("", primitiveType, meshPartPool.obtain());
		renderables.add(obtainRenderable(material, primitiveType));

		for (int i = 0, n = items.size; i < n; ++i) {
			final Renderable renderable = items.get(i);
			final VertexAttributes va = renderable.meshPart.mesh.getVertexAttributes();
			final Material mat = renderable.material;
			final int pt = renderable.meshPart.primitiveType;

			final boolean sameAttributes = va.equals(vertexAttributes);
			final boolean indexedMesh = renderable.meshPart.mesh.getNumIndices() > 0;
			final int verticesToAdd = indexedMesh ? renderable.meshPart.mesh.getNumVertices() : renderable.meshPart.size;
			final boolean canHoldVertices = meshBuilder.getNumVertices() + verticesToAdd <= MeshBuilder.MAX_VERTICES;
			final boolean sameMesh = sameAttributes && canHoldVertices;
			final boolean samePart = sameMesh && pt == primitiveType && mat.same(material, true);

			if (!samePart) {
				if (!sameMesh) {
					final Mesh mesh = meshBuilder.end(meshPool.obtain(vertexAttributes, meshBuilder.getNumVertices(),
						meshBuilder.getNumIndices()));
					while (offset < renderables.size)
						renderables.get(offset++).meshPart.mesh = mesh;
					meshBuilder.begin(vertexAttributes = va);
				}

				final MeshPart newPart = meshBuilder.part("", pt, meshPartPool.obtain());
				final Renderable previous = renderables.get(renderables.size - 1);
				previous.meshPart.offset = part.offset;
				previous.meshPart.size = part.size;
				part = newPart;

				renderables.add(obtainRenderable(material = mat, primitiveType = pt));
			}

			meshBuilder.setVertexTransform(renderable.worldTransform);
			meshBuilder.addMesh(renderable.meshPart.mesh, renderable.meshPart.offset, renderable.meshPart.size);
		}

		final Mesh mesh = meshBuilder.end(meshPool.obtain(vertexAttributes, meshBuilder.getNumVertices(),
			meshBuilder.getNumIndices()));
		while (offset < renderables.size)
			renderables.get(offset++).meshPart.mesh = mesh;

		final Renderable previous = renderables.get(renderables.size - 1);
		previous.meshPart.offset = part.offset;
		previous.meshPart.size = part.size;
	}

	/** Adds the specified {@link Renderable} to the cache. Must be called in between a call to {@link #begin()} and {@link #end()}.
	 * All member objects might (depending on possibilities) be used by reference and should not change while the cache is used. If
	 * the {@link Renderable#bones} member is not null then skinning is assumed and the renderable will be added as-is, by
	 * reference. Otherwise the renderable will be merged with other renderables as much as possible, depending on the
	 * {@link Mesh#getVertexAttributes()}, {@link Renderable#material} and primitiveType (in that order). The
	 * {@link Renderable#environment}, {@link Renderable#shader} and {@link Renderable#userData} values (if any) are removed.
	 * @param renderable The {@link Renderable} to add, should not change while the cache is needed. */
	public void add (Renderable renderable) {
		if (!building) throw new GdxRuntimeException("Can only add items to the ModelCache in between .begin() and .end()");
		if (renderable.bones == null)
			items.add(renderable);
		else
			renderables.add(renderable);
	}

	/** Adds the specified {@link RenderableProvider} to the cache, see {@link #add(Renderable)}. */
	public void add (final RenderableProvider renderableProvider) {
		renderableProvider.getRenderables(tmp, renderablesPool);
		for (int i = 0, n = tmp.size; i < n; ++i)
			add(tmp.get(i));
		tmp.clear();
	}

	/** Adds the specified {@link RenderableProvider}s to the cache, see {@link #add(Renderable)}. */
	public <T extends RenderableProvider> void add (final Iterable<T> renderableProviders) {
		for (final RenderableProvider renderableProvider : renderableProviders)
			add(renderableProvider);
	}

	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		if (building) throw new GdxRuntimeException("Cannot render a ModelCache in between .begin() and .end()");
		for (Renderable r : this.renderables) {
			r.shader = null;
			r.environment = null;
		}
		renderables.addAll(this.renderables);
	}

	@Override
	public void dispose () {
		if (building) throw new GdxRuntimeException("Cannot dispose a ModelCache in between .begin() and .end()");
		meshPool.dispose();
	}
}

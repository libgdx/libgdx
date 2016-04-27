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

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

/** A relatively lightweight class which can be used to render basic shapes which don't need a node structure and alike. Can be
 * used for batching both static and dynamic shapes which share the same {@link Material} and transformation {@link Matrix4}
 * within the world. Use {@link ModelBatch} to render the `ShapeCache`. Must be disposed when no longer needed to release native
 * resources.
 * <p>
 * How to use it :
 * </p>
 * 
 * <pre>
 * // Create cache
 * ShapeCache cache = new ShapeCache();
 * // Build the cache, for dynamic shapes, this would be in the render method.
 * MeshPartBuilder builder = cache.begin();
 * FrustumShapeBuilder.build(builder, camera);
 * BoxShapeBuilder.build(builder, box);
 * cache.end()
 * // Render
 * modelBatch.render(cache);
 * // After using it
 * cache.dispose();
 * </pre>
 * 
 * @author realitix */
public class ShapeCache implements Disposable, RenderableProvider {

	/** Builder used to update the mesh */
	private final MeshBuilder builder;

	/** Mesh being rendered */
	private final Mesh mesh;

	private boolean building;
	private final String id = "id";
	private final Renderable renderable = new Renderable();

	/** Create a ShapeCache with default values */
	public ShapeCache () {
		this(5000, 5000, new VertexAttributes(new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(
			Usage.ColorPacked, 4, "a_color")), GL20.GL_LINES);
	}

	/** Create a ShapeCache with parameters
	 * @param maxVertices max vertices in mesh
	 * @param maxIndices max indices in mesh
	 * @param attributes vertex attributes
	 * @param primitiveType */
	public ShapeCache (int maxVertices, int maxIndices, VertexAttributes attributes, int primitiveType) {
		// Init mesh
		mesh = new Mesh(false, maxVertices, maxIndices, attributes);

		// Init builder
		builder = new MeshBuilder();

		// Init renderable
		renderable.meshPart.mesh = mesh;
		renderable.meshPart.primitiveType = primitiveType;
		renderable.material = new Material();
	}

	/** Initialize ShapeCache for mesh generation with GL_LINES primitive type */
	public MeshPartBuilder begin () {
		return begin(GL20.GL_LINES);
	}

	/** Initialize ShapeCache for mesh generation
	 * @param primitiveType OpenGL primitive type */
	public MeshPartBuilder begin (int primitiveType) {
		if (building) throw new GdxRuntimeException("Call end() after calling begin()");
		building = true;

		builder.begin(mesh.getVertexAttributes());
		builder.part(id, primitiveType, renderable.meshPart);
		return builder;
	}

	/** Generate mesh and renderable */
	public void end () {
		if (!building) throw new GdxRuntimeException("Call begin() prior to calling end()");
		building = false;

		builder.end(mesh);
	}

	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		renderables.add(renderable);
	}

	/** Allows to customize the material.
	 * @return material */
	public Material getMaterial () {
		return renderable.material;
	}

	/** Allows to customize the world transform matrix.
	 * @return world transform */
	public Matrix4 getWorldTransform () {
		return renderable.worldTransform;
	}

	@Override
	public void dispose () {
		mesh.dispose();
	}
}

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

package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.SortedIntList;

/** <p>
 * Renderer for {@link Decal} objects.
 * </p>
 * <p>
 * New objects are added using {@link DecalBatch#add(Decal)}, there is no limit on how many decals can be added.<br/>
 * Once all the decals have been submitted a call to {@link DecalBatch#flush()} will batch them together and send big chunks of
 * geometry to the GL.
 * </p>
 * <p>
 * The size of the batch specifies the maximum number of decals that can be batched together before they have to be submitted to
 * the graphics pipeline. The default size is {@link DecalBatch#DEFAULT_SIZE}. If it is known before hand that not as many will be
 * needed on average the batch can be downsized to save memory. If the game is basically 3d based and decals will only be needed
 * for an orthogonal HUD it makes sense to tune the size down.
 * </p>
 * <p>
 * The way the batch handles things depends on the {@link GroupStrategy}. Different strategies can be used to customize shaders,
 * states, culling etc. for more details see the {@link GroupStrategy} java doc.<br/>
 * While it shouldn't be necessary to change strategies, if you have to do so, do it before calling {@link #add(Decal)}, and if
 * you already did, call {@link #flush()} first.
 * </p> */
public class DecalBatch implements Disposable {
	private static final int DEFAULT_SIZE = 1000;
	private float[] vertices;
	private Mesh mesh;

	private final SortedIntList<Array<Decal>> groupList = new SortedIntList<Array<Decal>>();
	private GroupStrategy groupStrategy;
	private final Pool<Array<Decal>> groupPool = new Pool<Array<Decal>>(16) {
		@Override
		protected Array<Decal> newObject () {
			return new Array<Decal>(false, 100);
		}
	};
	private final Array<Array<Decal>> usedGroups = new Array<Array<Decal>>(16);

	/**
	 * Creates a new DecalBatch using the given {@link GroupStrategy}. The most
	 * commong strategy to use is a {@link CameraGroupStrategy}
	 * @param groupStrategy
	 */
	public DecalBatch (GroupStrategy groupStrategy) {
		this(DEFAULT_SIZE, groupStrategy);
	}

	public DecalBatch (int size, GroupStrategy groupStrategy) {
		initialize(size);
		setGroupStrategy(groupStrategy);
	}

	/** Sets the {@link GroupStrategy} used
	 * @param groupStrategy Group strategy to use */
	public void setGroupStrategy (GroupStrategy groupStrategy) {
		this.groupStrategy = groupStrategy;
	}

	/** Initializes the batch with the given amount of decal objects the buffer is able to hold when full.
	 * 
	 * @param size Maximum size of decal objects to hold in memory */
	public void initialize (int size) {
		vertices = new float[size * Decal.SIZE];

		Mesh.VertexDataType vertexDataType = Mesh.VertexDataType.VertexArray;
		if(Gdx.gl30 != null) {
			vertexDataType = Mesh.VertexDataType.VertexBufferObjectWithVAO;
		}
		mesh = new Mesh(vertexDataType, false, size * 4, size * 6, new VertexAttribute(
				VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(
				VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE), new VertexAttribute(
				VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

		short[] indices = new short[size * 6];
		int v = 0;
		for (int i = 0; i < indices.length; i += 6, v += 4) {
			indices[i] = (short)(v);
			indices[i + 1] = (short)(v + 2);
			indices[i + 2] = (short)(v + 1);
			indices[i + 3] = (short)(v + 1);
			indices[i + 4] = (short)(v + 2);
			indices[i + 5] = (short)(v + 3);
		}
		mesh.setIndices(indices);
	}

	/** @return maximum amount of decal objects this buffer can hold in memory */
	public int getSize () {
		return vertices.length / Decal.SIZE;
	}

	/** Add a decal to the batch, marking it for later rendering
	 * 
	 * @param decal Decal to add for rendering */
	public void add (Decal decal) {
		int groupIndex = groupStrategy.decideGroup(decal);
		Array<Decal> targetGroup = groupList.get(groupIndex);
		if (targetGroup == null) {
			targetGroup = groupPool.obtain();
			targetGroup.clear();
			usedGroups.add(targetGroup);
			groupList.insert(groupIndex, targetGroup);
		}
		targetGroup.add(decal);
	}

	/** Flush this batch sending all contained decals to GL. After flushing the batch is empty once again. */
	public void flush () {
		render();
		clear();
	}

	/** Renders all decals to the buffer and flushes the buffer to the GL when full/done */
	protected void render () {
		groupStrategy.beforeGroups();
		for (SortedIntList.Node<Array<Decal>> group : groupList) {
			groupStrategy.beforeGroup(group.index, group.value);
			ShaderProgram shader = groupStrategy.getGroupShader(group.index);
			render(shader, group.value);
			groupStrategy.afterGroup(group.index);
		}
		groupStrategy.afterGroups();
	}

	/** Renders a group of vertices to the buffer, flushing them to GL when done/full
	 * 
	 * @param decals Decals to render */
	private void render (ShaderProgram shader, Array<Decal> decals) {
		// batch vertices
		DecalMaterial lastMaterial = null;
		int idx = 0;
		for (Decal decal : decals) {
			if (lastMaterial == null || !lastMaterial.equals(decal.getMaterial())) {
				if (idx > 0) {
					flush(shader, idx);
					idx = 0;
				}
				decal.material.set();
				lastMaterial = decal.material;
			}
			decal.update();
			System.arraycopy(decal.vertices, 0, vertices, idx, decal.vertices.length);
			idx += decal.vertices.length;
			// if our batch is full we have to flush it
			if (idx == vertices.length) {
				flush(shader, idx);
				idx = 0;
			}
		}
		// at the end if there is stuff left in the batch we render that
		if (idx > 0) {
			flush(shader, idx);
		}
	}

	/** Flushes vertices[0,verticesPosition[ to GL verticesPosition % Decal.SIZE must equal 0
	 * 
	 * @param verticesPosition Amount of elements from the vertices array to flush */
	protected void flush (ShaderProgram shader, int verticesPosition) {
		mesh.setVertices(vertices, 0, verticesPosition);
		mesh.render(shader, GL20.GL_TRIANGLES, 0, verticesPosition / 4);
	}

	/** Remove all decals from batch */
	protected void clear () {
		groupList.clear();
		groupPool.freeAll(usedGroups);
		usedGroups.clear();
	}

	/** Frees up memory by dropping the buffer and underlying resources. If the batch is needed again after disposing it can be
	 * {@link #initialize(int) initialized} again. */
	public void dispose () {
		clear();
		vertices = null;
		mesh.dispose();
	}
}

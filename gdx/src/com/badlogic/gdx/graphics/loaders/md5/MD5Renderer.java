/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.graphics.loaders.md5;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;

public class MD5Renderer {
	private final MD5Model model;
	private final Mesh mesh;
	private final short[][] indices;
	private final float[][] vertices;
	private boolean useJni;

	public MD5Renderer (MD5Model model, boolean useJni) {
		int maxVertices = 0;
		int maxIndices = 0;

		this.model = model;
		this.useJni = useJni;
		this.indices = new short[model.meshes.length][];
		this.vertices = new float[model.meshes.length][];

		for (int i = 0; i < model.meshes.length; i++) {
			if (maxVertices < model.meshes[i].numVertices) maxVertices = model.meshes[i].numVertices;
			if (maxIndices < model.meshes[i].numTriangles * 3) maxIndices = model.meshes[i].numTriangles * 3;

			this.indices[i] = model.meshes[i].getIndices();
			this.vertices[i] = model.meshes[i].createVertexArray();
		}

		this.mesh = new Mesh(false, maxVertices, maxIndices, new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords"));
	}

	public void setSkeleton (MD5Joints skeleton) {
		for (int i = 0; i < model.meshes.length; i++) {
			MD5Mesh mesh = model.meshes[i];
			if (useJni)
				mesh.calculateVerticesJni(skeleton, vertices[i]);
			else
				mesh.calculateVertices(skeleton, vertices[i]);
		}
	}

	public void setUseJni (boolean useJni) {
		this.useJni = useJni;
	}

	public void render () {
		for (int i = 0; i < model.meshes.length; i++) {
			this.mesh.setIndices(indices[i]);
			this.mesh.setVertices(vertices[i]);
			this.mesh.render(GL10.GL_TRIANGLES, 0, indices[i].length);
		}
	}

	public void dispose () {
		mesh.dispose();
	}

	public boolean isJniUsed () {
		return useJni;
	}
}

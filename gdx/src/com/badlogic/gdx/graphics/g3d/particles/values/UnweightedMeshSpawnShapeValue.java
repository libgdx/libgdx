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

package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/** Encapsulate the formulas to spawn a particle on a mesh shape.
 * @author Inferno */
public final class UnweightedMeshSpawnShapeValue extends MeshSpawnShapeValue {
	private float[] vertices;
	private short[] indices;
	private int positionOffset, vertexSize, vertexCount, triangleCount;

	public UnweightedMeshSpawnShapeValue (UnweightedMeshSpawnShapeValue value) {
		super(value);
		load(value);
	}

	public UnweightedMeshSpawnShapeValue () {
	}

	@Override
	public void setMesh (Mesh mesh, Model model) {
		super.setMesh(mesh, model);
		vertexSize = mesh.getVertexSize() / 4;
		positionOffset = mesh.getVertexAttribute(Usage.Position).offset / 4;
		int indicesCount = mesh.getNumIndices();
		if (indicesCount > 0) {
			indices = new short[indicesCount];
			mesh.getIndices(indices);
			triangleCount = indices.length / 3;
		} else
			indices = null;
		vertexCount = mesh.getNumVertices();
		vertices = new float[vertexCount * vertexSize];
		mesh.getVertices(vertices);
	}

	@Override
	public void spawnAux (Vector3 vector, float percent) {
		if (indices == null) {
			// Triangles
			int triangleIndex = MathUtils.random(vertexCount - 3) * vertexSize;
			int p1Offset = triangleIndex + positionOffset, p2Offset = p1Offset + vertexSize, p3Offset = p2Offset + vertexSize;
			float x1 = vertices[p1Offset], y1 = vertices[p1Offset + 1], z1 = vertices[p1Offset + 2], x2 = vertices[p2Offset], y2 = vertices[p2Offset + 1], z2 = vertices[p2Offset + 2], x3 = vertices[p3Offset], y3 = vertices[p3Offset + 1], z3 = vertices[p3Offset + 2];
			Triangle.pick(x1, y1, z1, x2, y2, z2, x3, y3, z3, vector);
		} else {
			// Indices
			int triangleIndex = MathUtils.random(triangleCount - 1) * 3;
			int p1Offset = indices[triangleIndex] * vertexSize + positionOffset, p2Offset = indices[triangleIndex + 1] * vertexSize
				+ positionOffset, p3Offset = indices[triangleIndex + 2] * vertexSize + positionOffset;
			float x1 = vertices[p1Offset], y1 = vertices[p1Offset + 1], z1 = vertices[p1Offset + 2], x2 = vertices[p2Offset], y2 = vertices[p2Offset + 1], z2 = vertices[p2Offset + 2], x3 = vertices[p3Offset], y3 = vertices[p3Offset + 1], z3 = vertices[p3Offset + 2];
			Triangle.pick(x1, y1, z1, x2, y2, z2, x3, y3, z3, vector);
		}
	}

	@Override
	public SpawnShapeValue copy () {
		return new UnweightedMeshSpawnShapeValue(this);
	}

}

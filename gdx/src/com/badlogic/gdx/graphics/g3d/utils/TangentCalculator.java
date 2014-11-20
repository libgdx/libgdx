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

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.IndexData;
import com.badlogic.gdx.graphics.glutils.VertexData;

public class TangentCalculator {
	private final FloatBuffer vertices;
	private final ShortBuffer indices;
	private final int vertexSize, posOffset, uvOffset, tangentOffset, bitangentOffset;

	public TangentCalculator(FloatBuffer vertices, ShortBuffer indices, int vertexSize, int posOffset, int uvOffset, int tangentOffset, int bitangentOffset, int primitiveType) {
		if (primitiveType != GL20.GL_TRIANGLES)
			throw new IllegalArgumentException("Only GL_TRIANGLES are supported");
		this.vertices = vertices.slice();
		this.indices = indices.slice();
		this.vertexSize = vertexSize / 4;
		this.posOffset = posOffset;
		this.uvOffset = uvOffset;
		this.tangentOffset = tangentOffset;
		this.bitangentOffset = bitangentOffset;
	}

	public TangentCalculator(FloatBuffer vertices, ShortBuffer indices, VertexAttributes attributes, int primitiveType) {
		this(vertices, indices,
				attributes.vertexSize,
				attributes.getOffset(VertexAttributes.Usage.Position),
				attributes.getOffset(VertexAttributes.Usage.TextureCoordinates),
				attributes.getOffset(VertexAttributes.Usage.Tangent),
				attributes.getOffset(VertexAttributes.Usage.BiNormal),
				primitiveType);
		if (attributes.findByUsage(VertexAttributes.Usage.Position) == null ||
				attributes.findByUsage(VertexAttributes.Usage.TextureCoordinates) == null ||
				attributes.findByUsage(VertexAttributes.Usage.Tangent) == null ||
				attributes.findByUsage(VertexAttributes.Usage.BiNormal) == null) {
			throw new IllegalArgumentException("");
		}
	}

	public TangentCalculator(VertexData vertices, IndexData indices, int primitiveType) {
		this(vertices.getBuffer(), indices.getBuffer(), vertices.getAttributes(), primitiveType);
	}

	public TangentCalculator(Mesh mesh, int primitiveType) {
		this(mesh.getVerticesBuffer(), mesh.getIndicesBuffer(), mesh.getVertexAttributes(), primitiveType);
	}

	public void clear() {
		ShortBuffer is = indices.duplicate();
		while (is.hasRemaining()) {
			short ix = is.get();
			int vertexOffset = ix * vertexSize;

			vertices.put(vertexOffset + tangentOffset, 0)
					.put(vertexOffset + tangentOffset + 1, 0)
					.put(vertexOffset + tangentOffset + 2, 0)
					.put(vertexOffset + bitangentOffset, 0)
					.put(vertexOffset + bitangentOffset + 1, 0)
					.put(vertexOffset + bitangentOffset + 2, 0);
		}
	}

	public void calculate() {
		clear();

		ShortBuffer is = indices.duplicate();
		float pos1[] = new float[3];
		float pos2[] = new float[3];
		float pos3[] = new float[3];
		float uv1[] = new float[2];
		float uv2[] = new float[2];
		float uv3[] = new float[2];
		float tangent1[] = new float[3];
		float tangent2[] = new float[3];
		float tangent3[] = new float[3];
		float bitangent1[] = new float[3];
		float bitangent2[] = new float[3];
		float bitangent3[] = new float[3];
		float tangentUpdate[] = new float[3];
		float bitangentUpdate[] = new float[3];

		FloatBuffer vs = vertices.duplicate();

		while (is.remaining() >= 3) {
			short ix1 = is.get();
			short ix2 = is.get();
			short ix3 = is.get();

			readTriangle(vs, ix1, pos1, uv1, tangent1, bitangent1);
			readTriangle(vs, ix2, pos2, uv2, tangent2, bitangent2);
			readTriangle(vs, ix3, pos3, uv3, tangent3, bitangent3);

			calculateTangent(pos1, pos2, pos3, uv1, uv2, uv3, tangentUpdate, bitangentUpdate);

			for (int i = 0; i < 3; i++) {
				tangent1[i] += tangentUpdate[i];
				tangent2[i] += tangentUpdate[i];
				tangent3[i] += tangentUpdate[i];

				bitangent1[i] += bitangentUpdate[i];
				bitangent2[i] += bitangentUpdate[i];
				bitangent3[i] += bitangentUpdate[i];
			}

			writeTriangleTangent(vs, ix1, tangent1, bitangent1);
			writeTriangleTangent(vs, ix2, tangent2, bitangent2);
			writeTriangleTangent(vs, ix3, tangent3, bitangent3);
		}
	}

	private void readTriangle(FloatBuffer vs, short ix, float[] pos, float[] uv, float[] tangent, float[] bitangent) {
		int vertexOffset = ix * vertexSize;
		vs.position(vertexOffset + posOffset);
		vs.get(pos);
		vs.position(vertexOffset + uvOffset);
		vs.get(uv);
		vs.position(vertexOffset + tangentOffset);
		vs.get(tangent);
		vs.position(vertexOffset + bitangentOffset);
		vs.get(bitangent);
	}

	private void writeTriangleTangent(FloatBuffer vs, short ix, float[] tangent, float[] bitangent) {
		int vertexOffset = ix * vertexSize;
		vs.position(vertexOffset + tangentOffset);
		vs.put(tangent);
		vs.position(vertexOffset + bitangentOffset);
		vs.put(bitangent);
	}

	void calculateTangent(float[] pos1, float[] pos2, float[] pos3, float[] uv1, float[] uv2, float[] uv3, float[] tangent, float[] bitangent) {
		float x1 = pos2[0] - pos1[0];
		float x2 = pos3[0] - pos1[0];
		float y1 = pos2[1] - pos1[1];
		float y2 = pos3[1] - pos1[1];
		float z1 = pos2[2] - pos1[2];
		float z2 = pos3[2] - pos1[2];

		float u1 = uv2[0] - uv1[0];
		float u2 = uv3[0] - uv1[0];
		float v1 = uv2[1] - uv1[1];
		float v2 = uv3[1] - uv1[1];

		float r = 1.0f / (u1 * v2 - u2 * v1);

		tangent[0] = (v2 * x1 - v1 * x2) * r;
		tangent[1] = (v2 * y1 - v1 * y2) * r;
		tangent[2] = (v2 * z1 - v1 * z2) * r;

		bitangent[0] = (u1 * x2 - u2 * x1) * r;
		bitangent[1] = (u1 * y2 - u2 * y1) * r;
		bitangent[2] = (u1 * z2 - u2 * z1) * r;
	}
}

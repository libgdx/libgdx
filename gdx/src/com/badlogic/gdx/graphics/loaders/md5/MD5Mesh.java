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

public class MD5Mesh {
	public String shader;

	public int numVertices;
	public int numWeights;
	public int numTriangles;
	/** s, t, start, count **/
	public float[] vertices;
	/** joint, bias, pos.x, pos.y, pos.z **/
	public float[] weights;
	/** idx 1, idx 2, idx 3 **/
	public short[] indices;

	public float[] createVertexArray () {
		float[] verts = new float[numVertices * 5];
		for (int i = 0, j = 3; i < vertices.length; i += 4) {
			verts[j++] = vertices[i];
			verts[j++] = vertices[i + 1];
			j += 3;
		}

		return verts;
	}

	public short[] getIndices () {
		return indices;
	}

	public void calculateVertices (MD5Joints skeleton, float[] verts) {
		calculateVertices(skeleton, weights, vertices, verts);
	}

	public static void calculateVertices (MD5Joints skeleton, float[] weights, float vertices[], float[] verts) {
		for (int vertexOffset = 2, k = 0; vertexOffset < vertices.length; vertexOffset += 4) {
			float finalX = 0;
			float finalY = 0;
			float finalZ = 0;

			int weightOffset = (int)vertices[vertexOffset];
			int weightCount = (int)vertices[vertexOffset + 1];
			weightOffset = (weightOffset << 2) + weightOffset;

			for (int j = 0; j < weightCount; j++) {
				int jointOffset = (int)weights[weightOffset++] << 3;
				float bias = weights[weightOffset++];
				float vx = weights[weightOffset++];
				float vy = weights[weightOffset++];
				float vz = weights[weightOffset++];

				float qx = skeleton.joints[jointOffset + 4];
				float qy = skeleton.joints[jointOffset + 5];
				float qz = skeleton.joints[jointOffset + 6];
				float qw = skeleton.joints[jointOffset + 7];

				float ix = -qx, iy = -qy, iz = -qz, iw = qw;

				float tw = -qx * vx - qy * vy - qz * vz;
				float tx = qw * vx + qy * vz - qz * vy;
				float ty = qw * vy + qz * vx - qx * vz;
				float tz = qw * vz + qx * vy - qy * vx;

				vx = tx * iw + tw * ix + ty * iz - tz * iy;
				vy = ty * iw + tw * iy + tz * ix - tx * iz;
				vz = tz * iw + tw * iz + tx * iy - ty * ix;

				finalX += (skeleton.joints[jointOffset + 1] + vx) * bias;
				finalY += (skeleton.joints[jointOffset + 2] + vy) * bias;
				finalZ += (skeleton.joints[jointOffset + 3] + vz) * bias;
			}

			verts[k++] = finalX;
			verts[k++] = finalY;
			verts[k++] = finalZ;
			k += 2;
		}
	}

	public void calculateVerticesJni (MD5Joints skeleton, float[] verts) {
		MD5Jni.calculateVertices(skeleton.joints, weights, vertices, verts, numVertices);
	}
}

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

package com.badlogic.gdx.graphics.g3d.loaders.md5;

public class MD5Jni {
	public static native void calculateVertices (float[] joints, float[] weights, float[] verticesIn, float[] verticesOut,
		int numVertices, int vstride, int wstride); /*
		for (int vertexOffset = 2, k = 0; vertexOffset < numVertices; vertexOffset += vstride) {
			float finalX = 0;
			float finalY = 0;
			float finalZ = 0;

			int weightOffset = (int)verticesIn[vertexOffset];
			int weightCount = (int)verticesIn[vertexOffset + 1];
			weightOffset = (weightOffset << 2) + weightOffset;

			for (int j = 0; j < weightCount; j++) {
				int jointOffset = (int)weights[weightOffset++] << 3;
				float bias = weights[weightOffset++];
				float vx = weights[weightOffset++];
				float vy = weights[weightOffset++];
				float vz = weights[weightOffset++];

				float qx = joints[jointOffset + 4];
				float qy = joints[jointOffset + 5];
				float qz = joints[jointOffset + 6];
				float qw = joints[jointOffset + 7];

				float ix = -qx, iy = -qy, iz = -qz, iw = qw;

				float tw = -qx * vx - qy * vy - qz * vz;
				float tx = qw * vx + qy * vz - qz * vy;
				float ty = qw * vy + qz * vx - qx * vz;
				float tz = qw * vz + qx * vy - qy * vx;

				vx = tx * iw + tw * ix + ty * iz - tz * iy;
				vy = ty * iw + tw * iy + tz * ix - tx * iz;
				vz = tz * iw + tw * iz + tx * iy - ty * ix;

				finalX += (joints[jointOffset + 1] + vx) * bias;
				finalY += (joints[jointOffset + 2] + vy) * bias;
				finalZ += (joints[jointOffset + 3] + vz) * bias;
			}

			verticesOut[k++] = finalX;
			verticesOut[k++] = finalY;
			verticesOut[k++] = finalZ;
			k += 2;
		}
	*/
}

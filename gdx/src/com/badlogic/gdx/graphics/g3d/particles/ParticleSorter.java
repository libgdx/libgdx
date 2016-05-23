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

package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerRenderData;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/** This class is used by particle batches to sort the particles before rendering.
 * @author Inferno */
public abstract class ParticleSorter {
	static final Vector3 TMP_V1 = new Vector3();

	/** Using this class will not apply sorting */
	public static class None extends ParticleSorter {
		int currentCapacity = 0;
		int[] indices;

		@Override
		public void ensureCapacity (int capacity) {
			if (currentCapacity < capacity) {
				indices = new int[capacity];
				for (int i = 0; i < capacity; ++i)
					indices[i] = i;
				currentCapacity = capacity;
			}
		}

		@Override
		public <T extends ParticleControllerRenderData> int[] sort (Array<T> renderData) {
			return indices;
		}
	}

	/** This class will sort all the particles using the distance from camera. */
	public static class Distance extends ParticleSorter {
		private float[] distances;
		private int[] particleIndices, particleOffsets;
		private int currentSize = 0;

		@Override
		public void ensureCapacity (int capacity) {
			if (currentSize < capacity) {
				distances = new float[capacity];
				particleIndices = new int[capacity];
				particleOffsets = new int[capacity];
				currentSize = capacity;
			}
		}

		@Override
		public <T extends ParticleControllerRenderData> int[] sort (Array<T> renderData) {
			float[] val = camera.view.val;
			float cx = val[Matrix4.M20], cy = val[Matrix4.M21], cz = val[Matrix4.M22];
			int count = 0, i = 0;
			for (ParticleControllerRenderData data : renderData) {
				for (int k = 0, c = i + data.controller.particles.size; i < c; ++i, k += data.positionChannel.strideSize) {
					distances[i] = cx * data.positionChannel.data[k + ParticleChannels.XOffset] + cy
						* data.positionChannel.data[k + ParticleChannels.YOffset] + cz
						* data.positionChannel.data[k + ParticleChannels.ZOffset];
					particleIndices[i] = i;
				}
				count += data.controller.particles.size;
			}

			qsort(0, count - 1);

			for (i = 0; i < count; ++i) {
				particleOffsets[particleIndices[i]] = i;
			}
			return particleOffsets;
		}

		public void qsort (int si, int ei) {
			// base case
			if (si < ei) {
				float tmp;
				int tmpIndex, particlesPivotIndex;
				// insertion
				if (ei - si <= 8) {
					for (int i = si; i <= ei; i++)
						for (int j = i; j > si && distances[j - 1] > distances[j]; j--) {
							tmp = distances[j];
							distances[j] = distances[j - 1];
							distances[j - 1] = tmp;

							// Swap indices
							tmpIndex = particleIndices[j];
							particleIndices[j] = particleIndices[j - 1];
							particleIndices[j - 1] = tmpIndex;
						}
					return;
				}

				// Quick
				float pivot = distances[si];
				int i = si + 1;
				particlesPivotIndex = particleIndices[si];

				// partition array
				for (int j = si + 1; j <= ei; j++) {
					if (pivot > distances[j]) {
						if (j > i) {
							// Swap distances
							tmp = distances[j];
							distances[j] = distances[i];
							distances[i] = tmp;

							// Swap indices
							tmpIndex = particleIndices[j];
							particleIndices[j] = particleIndices[i];
							particleIndices[i] = tmpIndex;
						}
						i++;
					}
				}

				// put pivot in right position
				distances[si] = distances[i - 1];
				distances[i - 1] = pivot;
				particleIndices[si] = particleIndices[i - 1];
				particleIndices[i - 1] = particlesPivotIndex;

				// call qsort on right and left sides of pivot
				qsort(si, i - 2);
				qsort(i, ei);
			}
		}
	}

	protected Camera camera;

	/** @return an array of offsets where each particle should be put in the resulting mesh (also if more than one mesh will be
	 *         generated, this is an absolute offset considering a BIG output array). */
	public abstract <T extends ParticleControllerRenderData> int[] sort (Array<T> renderData);

	public void setCamera (Camera camera) {
		this.camera = camera;
	}

	/** This method is called when the batch has increased the underlying particle buffer. In this way the sorter can increase the
	 * data structures used to sort the particles (i.e increase backing array size) */
	public void ensureCapacity (int capacity) {
	}
}

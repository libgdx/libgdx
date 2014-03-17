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

package com.badlogic.gdx.tools.texturepacker;

import com.badlogic.gdx.tools.texturepacker.ColorBleedEffect.Mask.MaskIterator;

import java.awt.image.BufferedImage;
import java.util.NoSuchElementException;

/** @author Ruben Garat
 * @author Ariel Coppes
 * @author Nathan Sweet */
public class ColorBleedEffect {
	static int TO_PROCESS = 0;
	static int IN_PROCESS = 1;
	static int REALDATA = 2;
	static int[][] offsets = { {-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

	ARGBColor color = new ARGBColor();

	public BufferedImage processImage (BufferedImage image, int maxIterations) {
		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
		Mask mask = new Mask(rgb);

		int iterations = 0;
		int lastPending = -1;
		while (mask.pendingSize > 0 && mask.pendingSize != lastPending && iterations < maxIterations) {
			lastPending = mask.pendingSize;
			executeIteration(rgb, mask, width, height);
			iterations++;
		}

		processedImage.setRGB(0, 0, width, height, rgb, 0, width);
		return processedImage;
	}

	private void executeIteration (int[] rgb, Mask mask, int width, int height) {
		MaskIterator iterator = mask.new MaskIterator();
		while (iterator.hasNext()) {
			int pixelIndex = iterator.next();
			int x = pixelIndex % width;
			int y = pixelIndex / width;
			int r = 0, g = 0, b = 0;
			int count = 0;

			for (int i = 0, n = offsets.length; i < n; i++) {
				int[] offset = offsets[i];
				int column = x + offset[0];
				int row = y + offset[1];

				if (column < 0 || column >= width || row < 0 || row >= height) continue;

				int currentPixelIndex = getPixelIndex(width, column, row);
				if (mask.getMask(currentPixelIndex) == REALDATA) {
					color.argb = rgb[currentPixelIndex];
					r += color.red();
					g += color.green();
					b += color.blue();
					count++;
				}
			}

			if (count != 0) {
				color.setARGBA(0, r / count, g / count, b / count);
				rgb[pixelIndex] = color.argb;
				iterator.markAsInProgress();
			}
		}

		iterator.reset();
	}

	private int getPixelIndex (int width, int x, int y) {
		return y * width + x;
	}

	static class Mask {
		int[] data, pending, changing;
		int pendingSize, changingSize;

		Mask (int[] rgb) {
			data = new int[rgb.length];
			pending = new int[rgb.length];
			changing = new int[rgb.length];
			ARGBColor color = new ARGBColor();
			for (int i = 0; i < rgb.length; i++) {
				color.argb = rgb[i];
				if (color.alpha() == 0) {
					data[i] = TO_PROCESS;
					pending[pendingSize] = i;
					pendingSize++;
				} else
					data[i] = REALDATA;
			}
		}

		int getMask (int index) {
			return data[index];
		}

		int removeIndex (int index) {
			if (index >= pendingSize) throw new IndexOutOfBoundsException(String.valueOf(index));
			int value = pending[index];
			pendingSize--;
			pending[index] = pending[pendingSize];
			return value;
		}

		class MaskIterator {
			private int index;

			boolean hasNext () {
				return index < pendingSize;
			}

			int next () {
				if (index >= pendingSize) throw new NoSuchElementException(String.valueOf(index));
				return pending[index++];
			}

			void markAsInProgress () {
				index--;
				changing[changingSize] = removeIndex(index);
				changingSize++;
			}

			void reset () {
				index = 0;
				for (int i = 0; i < changingSize; i++) {
					int index = changing[i];
					data[index] = REALDATA;
				}
				changingSize = 0;
			}
		}
	}

	static class ARGBColor {
		int argb = 0xff000000;

		public int red () {
			return (argb >> 16) & 0xFF;
		}

		public int green () {
			return (argb >> 8) & 0xFF;
		}

		public int blue () {
			return (argb >> 0) & 0xFF;
		}

		public int alpha () {
			return (argb >> 24) & 0xff;
		}

		public void setARGBA (int a, int r, int g, int b) {
			if (a < 0 || a > 255 || r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255)
				throw new IllegalArgumentException("Invalid RGBA: " + r + ", " + g + "," + b + "," + a);
			argb = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
	}
}

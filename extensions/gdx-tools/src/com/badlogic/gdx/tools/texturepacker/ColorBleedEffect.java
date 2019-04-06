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

import java.util.NoSuchElementException;

import java.awt.image.BufferedImage;

/** @author Ruben Garat
 * @author Ariel Coppes
 * @author Nathan Sweet */
public class ColorBleedEffect {
	static private final int[] offsets = {-1, -1, 0, -1, 1, -1, -1, 0, 1, 0, -1, 1, 0, 1, 1, 1};

	public BufferedImage processImage (BufferedImage image, int maxIterations) {
		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage processedImage;
		if (image.getType() == BufferedImage.TYPE_INT_ARGB)
			processedImage = image;
		else
			processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
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
		Mask.MaskIterator iterator = mask.new MaskIterator();
		while (iterator.hasNext()) {
			int pixelIndex = iterator.next();
			int x = pixelIndex % width;
			int y = pixelIndex / width;
			int r = 0, g = 0, b = 0;
			int count = 0;

			for (int i = 0, n = offsets.length; i < n; i += 2) {
				int column = x + offsets[i];
				int row = y + offsets[i + 1];
				if (column < 0 || column >= width || row < 0 || row >= height) {
					column = x;
					row = y;
					continue;
				}

				int currentPixelIndex = getPixelIndex(width, column, row);
				if (!mask.isBlank(currentPixelIndex)) {
					int argb = rgb[currentPixelIndex];
					r += red(argb);
					g += green(argb);
					b += blue(argb);
					count++;
				}
			}

			if (count != 0) {
				rgb[pixelIndex] = argb(0, r / count, g / count, b / count);
				iterator.markAsInProgress();
			}
		}

		iterator.reset();
	}

	static private int getPixelIndex (int width, int x, int y) {
		return y * width + x;
	}

	static private int red (int argb) {
		return (argb >> 16) & 0xFF;
	}

	static private int green (int argb) {
		return (argb >> 8) & 0xFF;
	}

	static private int blue (int argb) {
		return (argb >> 0) & 0xFF;
	}

	static private int argb (int a, int r, int g, int b) {
		if (a < 0 || a > 255 || r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255)
			throw new IllegalArgumentException("Invalid RGBA: " + r + ", " + g + "," + b + "," + a);
		return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
	}

	static private class Mask {
		final boolean[] blank;
		final int[] pending, changing;
		int pendingSize, changingSize;

		Mask (int[] rgb) {
			int n = rgb.length;
			blank = new boolean[n];
			pending = new int[n];
			changing = new int[n];
			for (int i = 0; i < n; i++) {
				if (alpha(rgb[i]) == 0) {
					blank[i] = true;
					pending[pendingSize] = i;
					pendingSize++;
				}
			}
		}

		boolean isBlank (int index) {
			return blank[index];
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
				for (int i = 0, n = changingSize; i < n; i++)
					blank[changing[i]] = false;
				changingSize = 0;
			}
		}

		static private int alpha (int argb) {
			return (argb >> 24) & 0xff;
		}
	}
}

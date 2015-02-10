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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.StringBuilder;

/** @see com.badlogic.gdx.graphics.g2d.BitmapFontCache
 *
 * @author davebaol
 * @author Alexander Dorokhov */
class TextMarkup {
	private static final Color tempColor = new Color();
	private static final StringBuilder tempColorBuffer = new StringBuilder();

	/** Parses a color tag.
	 * @param str the input string
	 * @param nomarkupStart the index of the string, excluding characters used to markup the text, where color info will be added.
	 *           if it's negative color info is not set.
	 * @param start the begin index
	 * @param end the end index
	 * @return the number of characters in the tag; {@code -1} in case of unknown color. */
	public int parseColorTag (CharSequence str, int nomarkupStart, int start, int end) {
		if (start < end) {
			final Color hexColor = tempColor;
			if (str.charAt(start) == '#') {
				// Parse hex color RRGGBBAA where AA is optional and defaults to 0xFF if less than 6 chars are used
				int colorInt = 0;
				for (int i = start + 1; i < end; i++) {
					char ch = str.charAt(i);
					if (ch == ']') {
						if (i < start + 2 || i > start + 9) return -1; // Illegal number of hex digits
						if (i <= start + 7) { // RRGGBB
							Color.rgb888ToColor(hexColor, colorInt);
							hexColor.a = 1f;
						} else { // RRGGBBAA
							Color.rgba8888ToColor(hexColor, colorInt);
						}
						if (nomarkupStart >= 0) beginChunk(hexColor, nomarkupStart);
						return i - start;
					}
					if (ch >= '0' && ch <= '9')
						colorInt = colorInt * 16 + (ch - '0');
					else if (ch >= 'a' && ch <= 'f')
						colorInt = colorInt * 16 + (ch - ('a' - 10));
					else if (ch >= 'A' && ch <= 'F')
						colorInt = colorInt * 16 + (ch - ('A' - 10));
					else
						return -1; // Unexpected character in hex color
				}
			} else {
				// Parse named color
				tempColorBuffer.setLength(0);
				for (int i = start; i < end; i++) {
					char ch = str.charAt(i);
					if (ch == ']') {
						if (i == start) { // end tag []
							if (nomarkupStart >= 0) endChunk(nomarkupStart);
						} else {
							String colorString = tempColorBuffer.toString();
							Color newColor = Colors.get(colorString);
							if (newColor == null) return -1; // Unknown color
							if (nomarkupStart >= 0) beginChunk(newColor, nomarkupStart);
						}
						return i - start;
					} else {
						tempColorBuffer.append(ch);
					}
				}
			}
		}
		return -1; // Unclosed color tag
	}

	private static Pool<ColorChunk> colorChunkPool;
	private static Application app = null;

	private Array<ColorChunk> colorChunks = new Array<ColorChunk>();
	private Array<Color> currentColorStack = new Array<Color>();
	private Color lastColor = Color.WHITE;
	private Color defaultColor = Color.WHITE;

	public TextMarkup () {
		if (Gdx.app != app) {
			colorChunkPool = new Pool<ColorChunk>(32) {
				protected ColorChunk newObject () {
					return new ColorChunk();
				}
			};
			app = Gdx.app;
		}
	}

	public void beginChunk (Color color, int start) {
		ColorChunk newChunk = colorChunkPool.obtain();
		newChunk.color.set(color);
		newChunk.start = start;
		colorChunks.add(newChunk);
		currentColorStack.add(lastColor);
		lastColor = newChunk.color;
	}

	public void endChunk (int start) {
		if (currentColorStack.size > 0) {
			lastColor = currentColorStack.pop();
			ColorChunk newChunk = colorChunkPool.obtain();
			newChunk.color.set(lastColor);
			newChunk.start = start;
			colorChunks.add(newChunk);
		}
	}

	public void tint (BitmapFontCache cache, Color tint) {
		int current = 0;
		float floatColor = tempColor.set(defaultColor).mul(tint).toFloatBits();
		for (ColorChunk chunk : colorChunks) {
			int next = chunk.start;
			if (current < next) {
				cache.setColors(floatColor, current, next);
				current = next;
			}
			floatColor = tempColor.set(chunk.color).mul(tint).toFloatBits();
		}
		int charsCount = cache.getCharsCount();
		if (current < charsCount) {
			cache.setColors(floatColor, current, charsCount);
		}
	}

	/** Removes all the color chunks from the list and releases them to the internal pool */
	public void clear () {
		final int size = colorChunks.size;
		for (int i = 0; i < size; i++) {
			colorChunkPool.free(colorChunks.get(i));
			colorChunks.set(i, null);
		}
		colorChunks.size = 0;
		currentColorStack.clear();
		setDefaultChunk(defaultColor, 0);
	}

	public Color getLastColor () {
		return lastColor;
	}

	private void setDefaultColor (Color defaultColor) {
		if (currentColorStack.size == 0) {
			this.defaultColor = defaultColor;
			this.lastColor = defaultColor;
		}
	}

	public void setDefaultChunk (float color, int start) {
		int abgr = NumberUtils.floatToIntColor(color);
		setDefaultChunk(abgr, start);
	}

	public void setDefaultChunk (int abgr, int start) {
		ColorChunk newChunk = colorChunkPool.obtain();
		Color color = newChunk.color;
		color.r = (abgr & 0xff) / 255f;
		color.g = ((abgr >>> 8) & 0xff) / 255f;
		color.b = ((abgr >>> 16) & 0xff) / 255f;
		color.a = ((abgr >>> 24) & 0xff) / 255f;
		newChunk.start = start;
		colorChunks.add(newChunk);
		setDefaultColor(newChunk.color);
	}

	public void setDefaultChunk (Color color, int start) {
		ColorChunk newChunk = colorChunkPool.obtain();
		newChunk.color.set(color);
		newChunk.start = start;
		colorChunks.add(newChunk);
		setDefaultColor(newChunk.color);
	}

	public static class ColorChunk {
		public int start;
		public final Color color = new Color();
	}
}

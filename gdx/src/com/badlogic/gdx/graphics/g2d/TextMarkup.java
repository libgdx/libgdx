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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.StringBuilder;

/** @see com.badlogic.gdx.graphics.g2d.BitmapFontCache
 *
 * @author davebaol
 * @author Alexander Dorokhov */
class TextMarkup {
	private static final Pool<ColorChunk> colorChunkPool = new Pool<ColorChunk>(32) {
		protected ColorChunk newObject () {
			return new ColorChunk();
		}
	};

	private static final Color tempColor = new Color();
	private static final com.badlogic.gdx.utils.StringBuilder tempColorBuffer = new StringBuilder();

	public static int parseColorTag (TextMarkup markup, CharSequence str, int nomarkupStart, int start, int end) {
		if (start < end) {
			final Color hexColor = tempColor;
			if (str.charAt(start) == '#') {
				// Parse hex color RRGGBBAA where AA is optional and defaults to 0xFF if less than 6 chars are used
				int colorInt = 0;
				for (int i = start + 1; i < end; i++) {
					char ch = str.charAt(i);
					if (ch == ']') {
						if (i < start + 2 || i > start + 9)
							throw new GdxRuntimeException("Hex color cannot have " + (i - start - 1) + " digits.");
						if (i <= start + 7) { // RRGGBB
							Color.rgb888ToColor(hexColor, colorInt);
							hexColor.a = 1f;
						} else { // RRGGBBAA
							Color.rgba8888ToColor(hexColor, colorInt);
						}
						markup.beginChunk(hexColor, nomarkupStart);
						return i - start;
					}
					if (ch >= '0' && ch <= '9')
						colorInt = colorInt * 16 + (ch - '0');
					else if (ch >= 'a' && ch <= 'f')
						colorInt = colorInt * 16 + (ch - ('a' - 10));
					else if (ch >= 'A' && ch <= 'F')
						colorInt = colorInt * 16 + (ch - ('A' - 10));
					else
						throw new GdxRuntimeException("Unexpected character in hex color: " + ch);
				}
			} else {
				// Parse named color
				tempColorBuffer.setLength(0);
				for (int i = start; i < end; i++) {
					char ch = str.charAt(i);
					if (ch == ']') {
						if (tempColorBuffer.length() == 0) { // end tag []
							markup.endChunk(nomarkupStart);
						} else {
							String colorString = tempColorBuffer.toString();
							Color newColor = Colors.get(colorString);
							if (newColor == null) throw new GdxRuntimeException("Unknown color: " + colorString);
							markup.beginChunk(newColor, nomarkupStart);
						}
						return i - start;
					} else {
						tempColorBuffer.append(ch);
					}
				}
			}
		}
		throw new GdxRuntimeException("Unclosed color tag.");
	}

	private Array<ColorChunk> colorChunks = new Array<ColorChunk>();
	private Array<Color> currentColorStack = new Array<Color>();
	private Color lastColor = Color.WHITE;
	private Color defaultColor = Color.WHITE;

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

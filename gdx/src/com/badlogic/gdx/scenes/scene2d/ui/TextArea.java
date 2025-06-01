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

package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/** A text input field with multiple lines. */
public class TextArea extends TextField {
	/** Array storing lines breaks positions **/
	IntArray linesBreak;

	/** Last text processed. This attribute is used to avoid unnecessary computations while calculating offsets **/
	private String lastText;

	/** Current line for the cursor **/
	int cursorLine;

	/** Index of the first line showed by the text area **/
	int firstLineShowing;

	/** Number of lines showed by the text area **/
	private int linesShowing;

	/** Variable to maintain the x offset of the cursor when moving up and down. If it's set to -1, the offset is reset **/
	float moveOffset;

	private float prefRows;

	public TextArea (String text, Skin skin) {
		super(text, skin);
	}

	public TextArea (String text, Skin skin, String styleName) {
		super(text, skin, styleName);
	}

	public TextArea (String text, TextFieldStyle style) {
		super(text, style);
	}

	protected void initialize () {
		super.initialize();
		writeEnters = true;
		linesBreak = new IntArray();
		cursorLine = 0;
		firstLineShowing = 0;
		moveOffset = -1;
		linesShowing = 0;
	}

	protected int letterUnderCursor (float x) {
		if (linesBreak.size > 0) {
			if (cursorLine * 2 >= linesBreak.size) {
				return text.length();
			} else {
				float[] glyphPositions = this.glyphPositions.items;
				int start = linesBreak.items[cursorLine * 2];
				x += glyphPositions[start];
				int end = linesBreak.items[cursorLine * 2 + 1];
				int i = start;
				for (; i < end; i++)
					if (glyphPositions[i] > x) break;
				if (i > 0 && glyphPositions[i] - x <= x - glyphPositions[i - 1]) return i;
				return Math.max(0, i - 1);
			}
		} else {
			return 0;
		}
	}

	public void setStyle (TextFieldStyle style) {
		// same as super(), just different textHeight. no super() so we don't do same work twice
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;

		// no extra descent to fake line height
		textHeight = style.font.getCapHeight() - style.font.getDescent();
		if (text != null) updateDisplayText();
		invalidateHierarchy();
	}

	/** Sets the preferred number of rows (lines) for this text area. Used to calculate preferred height */
	public void setPrefRows (float prefRows) {
		this.prefRows = prefRows;
	}

	public float getPrefHeight () {
		if (prefRows <= 0) {
			return super.getPrefHeight();
		} else {
			// without ceil we might end up with one less row then expected
			// due to how linesShowing is calculated in #sizeChanged and #getHeight() returning rounded value
			float prefHeight = (float)Math.ceil(style.font.getLineHeight() * prefRows);
			if (style.background != null) {
				prefHeight = Math.max(prefHeight + style.background.getBottomHeight() + style.background.getTopHeight(),
					style.background.getMinHeight());
			}
			return prefHeight;
		}
	}

	/** Returns total number of lines that the text occupies **/
	public int getLines () {
		return linesBreak.size / 2 + (newLineAtEnd() ? 1 : 0);
	}

	/** Returns if there's a new line at then end of the text **/
	public boolean newLineAtEnd () {
		return text.length() != 0
			&& (text.charAt(text.length() - 1) == NEWLINE || text.charAt(text.length() - 1) == CARRIAGE_RETURN);
	}

	/** Moves the cursor to the given number line **/
	public void moveCursorLine (int line) {
		if (line < 0) {
			cursorLine = 0;
			cursor = 0;
			moveOffset = -1;
		} else if (line >= getLines()) {
			int newLine = getLines() - 1;
			cursor = text.length();
			if (line > getLines() || newLine == cursorLine) {
				moveOffset = -1;
			}
			cursorLine = newLine;
		} else if (line != cursorLine) {
			if (moveOffset < 0) {
				moveOffset = linesBreak.size <= cursorLine * 2 ? 0
					: glyphPositions.get(cursor) - glyphPositions.get(linesBreak.get(cursorLine * 2));
			}
			cursorLine = line;
			cursor = cursorLine * 2 >= linesBreak.size ? text.length() : linesBreak.get(cursorLine * 2);
			while (cursor < text.length() && cursor <= linesBreak.get(cursorLine * 2 + 1) - 1
				&& glyphPositions.get(cursor) - glyphPositions.get(linesBreak.get(cursorLine * 2)) < moveOffset) {
				cursor++;
			}
			showCursor();
		}
	}

	/** Updates the current line, checking the cursor position in the text **/
	void updateCurrentLine () {
		int index = calculateCurrentLineIndex(cursor);
		int line = index / 2;
		// Special case when cursor moves to the beginning of the line from the end of another and a word
		// wider than the box
		if (index % 2 == 0 || index + 1 >= linesBreak.size || cursor != linesBreak.items[index]
			|| linesBreak.items[index + 1] != linesBreak.items[index]) {
			if (line < linesBreak.size / 2 || text.length() == 0 || text.charAt(text.length() - 1) == NEWLINE
				|| text.charAt(text.length() - 1) == CARRIAGE_RETURN) {
				cursorLine = line;
			}
		}
		updateFirstLineShowing(); // fix for drag-selecting text out of the TextArea's bounds
	}

	/** Scroll the text area to show the line of the cursor **/
	void showCursor () {
		updateCurrentLine();
		updateFirstLineShowing();
	}

	void updateFirstLineShowing () {
		if (cursorLine != firstLineShowing) {
			int step = cursorLine >= firstLineShowing ? 1 : -1;
			while (firstLineShowing > cursorLine || firstLineShowing + linesShowing - 1 < cursorLine) {
				firstLineShowing += step;
			}
		}
	}

	/** Calculates the text area line for the given cursor position **/
	private int calculateCurrentLineIndex (int cursor) {
		int index = 0;
		while (index < linesBreak.size && cursor > linesBreak.items[index]) {
			index++;
		}
		return index;
	}

	// OVERRIDE from TextField

	protected void sizeChanged () {
		lastText = null; // Cause calculateOffsets to recalculate the line breaks.

		// The number of lines showed must be updated whenever the height is updated
		BitmapFont font = style.font;
		Drawable background = style.background;
		float availableHeight = getHeight() - (background == null ? 0 : background.getBottomHeight() + background.getTopHeight());
		linesShowing = (int)Math.floor(availableHeight / font.getLineHeight());
	}

	protected float getTextY (BitmapFont font, @Null Drawable background) {
		float textY = getHeight();
		if (background != null) {
			textY = textY - background.getTopHeight();
		}
		if (font.usesIntegerPositions()) textY = (int)textY;
		return textY;
	}

	protected void drawSelection (Drawable selection, Batch batch, BitmapFont font, float x, float y) {
		int i = firstLineShowing * 2;
		float offsetY = 0;
		int minIndex = Math.min(cursor, selectionStart);
		int maxIndex = Math.max(cursor, selectionStart);
		BitmapFont.BitmapFontData fontData = font.getData();
		float lineHeight = style.font.getLineHeight();
		while (i + 1 < linesBreak.size && i < (firstLineShowing + linesShowing) * 2) {

			int lineStart = linesBreak.get(i);
			int lineEnd = linesBreak.get(i + 1);

			if (!((minIndex < lineStart && minIndex < lineEnd && maxIndex < lineStart && maxIndex < lineEnd)
				|| (minIndex > lineStart && minIndex > lineEnd && maxIndex > lineStart && maxIndex > lineEnd))) {

				int start = Math.max(lineStart, minIndex);
				int end = Math.min(lineEnd, maxIndex);

				float fontLineOffsetX = 0;
				float fontLineOffsetWidth = 0;
				// We can't use fontOffset as it is valid only for first glyph/line in the text.
				// We will grab first character in this line and calculate proper offset for this line.
				BitmapFont.Glyph lineFirst = fontData.getGlyph(displayText.charAt(lineStart));
				if (lineFirst != null) {
					// See BitmapFontData.getGlyphs() for offset calculation.
					// If selection starts when line starts we want to offset width instead of moving the start as it looks better.
					if (start == lineStart) {
						fontLineOffsetWidth = lineFirst.fixedWidth ? 0 : -lineFirst.xoffset * fontData.scaleX - fontData.padLeft;
					} else {
						fontLineOffsetX = lineFirst.fixedWidth ? 0 : -lineFirst.xoffset * fontData.scaleX - fontData.padLeft;
					}
				}
				float selectionX = glyphPositions.get(start) - glyphPositions.get(lineStart);
				float selectionWidth = glyphPositions.get(end) - glyphPositions.get(start);
				selection.draw(batch, x + selectionX + fontLineOffsetX, y - lineHeight - offsetY,
					selectionWidth + fontLineOffsetWidth, font.getLineHeight());
			}

			offsetY += font.getLineHeight();
			i += 2;
		}
	}

	protected void drawText (Batch batch, BitmapFont font, float x, float y) {
		float offsetY = -(style.font.getLineHeight() - textHeight) / 2;
		for (int i = firstLineShowing * 2; i < (firstLineShowing + linesShowing) * 2 && i < linesBreak.size; i += 2) {
			font.draw(batch, displayText, x, y + offsetY, linesBreak.items[i], linesBreak.items[i + 1], 0, Align.left, false);
			offsetY -= font.getLineHeight();
		}
	}

	protected void drawCursor (Drawable cursorPatch, Batch batch, BitmapFont font, float x, float y) {
		cursorPatch.draw(batch, x + getCursorX(), y + getCursorY(), cursorPatch.getMinWidth(), font.getLineHeight());
	}

	protected void calculateOffsets () {
		super.calculateOffsets();
		if (!this.text.equals(lastText)) {
			this.lastText = text;
			BitmapFont font = style.font;
			float maxWidthLine = this.getWidth()
				- (style.background != null ? style.background.getLeftWidth() + style.background.getRightWidth() : 0);
			linesBreak.clear();
			int lineStart = 0;
			int lastSpace = 0;
			char lastCharacter;
			Pool<GlyphLayout> layoutPool = Pools.get(GlyphLayout.class);
			GlyphLayout layout = layoutPool.obtain();
			for (int i = 0; i < text.length(); i++) {
				lastCharacter = text.charAt(i);
				if (lastCharacter == CARRIAGE_RETURN || lastCharacter == NEWLINE) {
					linesBreak.add(lineStart);
					linesBreak.add(i);
					lineStart = i + 1;
				} else {
					lastSpace = (continueCursor(i, 0) ? lastSpace : i);
					layout.setText(font, text.subSequence(lineStart, i + 1));
					if (layout.width > maxWidthLine) {
						if (lineStart >= lastSpace) {
							lastSpace = i - 1;
						}
						linesBreak.add(lineStart);
						linesBreak.add(lastSpace + 1);
						lineStart = lastSpace + 1;
						lastSpace = lineStart;
					}
				}
			}
			layoutPool.free(layout);
			// Add last line
			if (lineStart < text.length()) {
				linesBreak.add(lineStart);
				linesBreak.add(text.length());
			}
			showCursor();
		}
	}

	protected InputListener createInputListener () {
		return new TextAreaListener();
	}

	public void setSelection (int selectionStart, int selectionEnd) {
		super.setSelection(selectionStart, selectionEnd);
		updateCurrentLine();
	}

	protected void moveCursor (boolean forward, boolean jump) {
		int count = forward ? 1 : -1;
		int index = (cursorLine * 2) + count;
		if (index >= 0 && index + 1 < linesBreak.size && linesBreak.items[index] == cursor
			&& linesBreak.items[index + 1] == cursor) {
			cursorLine += count;
			if (jump) {
				super.moveCursor(forward, jump);
			}
			showCursor();
		} else {
			super.moveCursor(forward, jump);
		}
		updateCurrentLine();

	}

	protected boolean continueCursor (int index, int offset) {
		int pos = calculateCurrentLineIndex(index + offset);
		return super.continueCursor(index, offset) && (pos < 0 || pos >= linesBreak.size - 2 || (linesBreak.items[pos + 1] != index)
			|| (linesBreak.items[pos + 1] == linesBreak.items[pos + 2]));
	}

	public int getCursorLine () {
		return cursorLine;
	}

	public int getFirstLineShowing () {
		return firstLineShowing;
	}

	public int getLinesShowing () {
		return linesShowing;
	}

	public float getCursorX () {
		float textOffset = 0;
		BitmapFont.BitmapFontData fontData = style.font.getData();
		if (!(cursor >= glyphPositions.size || cursorLine * 2 >= linesBreak.size)) {
			int lineStart = linesBreak.items[cursorLine * 2];
			float glyphOffset = 0;
			BitmapFont.Glyph lineFirst = fontData.getGlyph(displayText.charAt(lineStart));
			if (lineFirst != null) {
				// See BitmapFontData.getGlyphs() for offset calculation.
				glyphOffset = lineFirst.fixedWidth ? 0 : -lineFirst.xoffset * fontData.scaleX - fontData.padLeft;
			}
			textOffset = glyphPositions.get(cursor) - glyphPositions.get(lineStart) + glyphOffset;
		}
		return textOffset + fontData.cursorX;
	}

	public float getCursorY () {
		BitmapFont font = style.font;
		return -(cursorLine - firstLineShowing + 1) * font.getLineHeight();
	}

	/** Input listener for the text area **/
	public class TextAreaListener extends TextFieldClickListener {
		protected void setCursorPosition (float x, float y) {
			moveOffset = -1;

			Drawable background = style.background;
			BitmapFont font = style.font;

			float height = getHeight();

			if (background != null) {
				height -= background.getTopHeight();
				x -= background.getLeftWidth();
			}
			x = Math.max(0, x);
			if (background != null) {
				y -= background.getTopHeight();
			}

			cursorLine = (int)Math.floor((height - y) / font.getLineHeight()) + firstLineShowing;
			cursorLine = Math.max(0, Math.min(cursorLine, getLines() - 1));

			super.setCursorPosition(x, y);
			updateCurrentLine();
		}

		public boolean keyDown (InputEvent event, int keycode) {
			boolean result = super.keyDown(event, keycode);
			if (hasKeyboardFocus()) {
				boolean repeat = false;
				boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
				if (keycode == Input.Keys.DOWN) {
					if (shift) {
						if (!hasSelection) {
							selectionStart = cursor;
							hasSelection = true;
						}
					} else {
						clearSelection();
					}
					moveCursorLine(cursorLine + 1);
					repeat = true;

				} else if (keycode == Input.Keys.UP) {
					if (shift) {
						if (!hasSelection) {
							selectionStart = cursor;
							hasSelection = true;
						}
					} else {
						clearSelection();
					}
					moveCursorLine(cursorLine - 1);
					repeat = true;

				} else {
					moveOffset = -1;
				}
				if (repeat) {
					scheduleKeyRepeatTask(keycode);
				}
				showCursor();
				return true;
			}
			return result;
		}

		protected boolean checkFocusTraversal (char character) {
			return focusTraversal && character == TAB;
		}

		public boolean keyTyped (InputEvent event, char character) {
			boolean result = super.keyTyped(event, character);
			showCursor();
			return result;
		}

		protected void goHome (boolean jump) {
			if (jump) {
				cursor = 0;
			} else if (cursorLine * 2 < linesBreak.size) {
				cursor = linesBreak.get(cursorLine * 2);
			}
		}

		protected void goEnd (boolean jump) {
			if (jump || cursorLine >= getLines()) {
				cursor = text.length();
			} else if (cursorLine * 2 + 1 < linesBreak.size) {
				cursor = linesBreak.get(cursorLine * 2 + 1);
			}
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2011, Nathan Sweet <nathan.sweet@gmail.com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.badlogic.gdx.scenes.scene2d.ui.tablelayout;

import static com.badlogic.gdx.scenes.scene2d.ui.tablelayout.TableLayout.*;

/** @author Nathan Sweet */
public class Cell<C> {
	String minWidth, minHeight;
	String prefWidth, prefHeight;
	String maxWidth, maxHeight;
	String spaceTop, spaceLeft, spaceBottom, spaceRight;
	String padTop, padLeft, padBottom, padRight;
	Float fillX, fillY;
	Integer align;
	Integer expandX, expandY;
	Boolean ignore;
	Integer colspan;
	Boolean uniformX, uniformY;
	String name;

	C widget;
	int widgetX, widgetY;
	int widgetWidth, widgetHeight;

	private final BaseTableLayout layout;
	boolean endRow;
	int column, row;
	int cellAboveIndex = -1;
	int padTopTemp, padLeftTemp, padBottomTemp, padRightTemp;

	Cell (BaseTableLayout layout) {
		this.layout = layout;
	}

	void set (Cell defaults) {
		minWidth = defaults.minWidth;
		minHeight = defaults.minHeight;
		prefWidth = defaults.prefWidth;
		prefHeight = defaults.prefHeight;
		maxWidth = defaults.maxWidth;
		maxHeight = defaults.maxHeight;
		spaceTop = defaults.spaceTop;
		spaceLeft = defaults.spaceLeft;
		spaceBottom = defaults.spaceBottom;
		spaceRight = defaults.spaceRight;
		padTop = defaults.padTop;
		padLeft = defaults.padLeft;
		padBottom = defaults.padBottom;
		padRight = defaults.padRight;
		fillX = defaults.fillX;
		fillY = defaults.fillY;
		align = defaults.align;
		expandX = defaults.expandX;
		expandY = defaults.expandY;
		ignore = defaults.ignore;
		colspan = defaults.colspan;
		uniformX = defaults.uniformX;
		uniformY = defaults.uniformY;
	}

	void merge (Cell cell) {
		if (cell == null) return;
		if (cell.minWidth != null) minWidth = cell.minWidth;
		if (cell.minHeight != null) minHeight = cell.minHeight;
		if (cell.prefWidth != null) prefWidth = cell.prefWidth;
		if (cell.prefHeight != null) prefHeight = cell.prefHeight;
		if (cell.maxWidth != null) maxWidth = cell.maxWidth;
		if (cell.maxHeight != null) maxHeight = cell.maxHeight;
		if (cell.spaceTop != null) spaceTop = cell.spaceTop;
		if (cell.spaceLeft != null) spaceLeft = cell.spaceLeft;
		if (cell.spaceBottom != null) spaceBottom = cell.spaceBottom;
		if (cell.spaceRight != null) spaceRight = cell.spaceRight;
		if (cell.padTop != null) padTop = cell.padTop;
		if (cell.padLeft != null) padLeft = cell.padLeft;
		if (cell.padBottom != null) padBottom = cell.padBottom;
		if (cell.padRight != null) padRight = cell.padRight;
		if (cell.fillX != null) fillX = cell.fillX;
		if (cell.fillY != null) fillY = cell.fillY;
		if (cell.align != null) align = cell.align;
		if (cell.expandX != null) expandX = cell.expandX;
		if (cell.expandY != null) expandY = cell.expandY;
		if (cell.ignore != null) ignore = cell.ignore;
		if (cell.colspan != null) colspan = cell.colspan;
		if (cell.uniformX != null) uniformX = cell.uniformX;
		if (cell.uniformY != null) uniformY = cell.uniformY;
	}

	public Cell setWidget (C widget) {
		layout.toolkit.setWidget(layout, this, widget);
		return this;
	}

	public String getName () {
		return name;
	}

	public C getWidget () {
		return widget;
	}

	public Cell minSize (String size) {
		minWidth = size;
		minHeight = size;
		return this;
	}

	public Cell minWidth (String minWidth) {
		this.minWidth = minWidth;
		return this;
	}

	public Cell minHeight (String minHeight) {
		this.minHeight = minHeight;
		return this;
	}

	public Cell minSize (int size) {
		minWidth = String.valueOf(size);
		minHeight = String.valueOf(size);
		return this;
	}

	public Cell minWidth (int minWidth) {
		this.minWidth = String.valueOf(minWidth);
		return this;
	}

	public Cell minHeight (int minHeight) {
		this.minHeight = String.valueOf(minHeight);
		return this;
	}

	public Cell prefSize (String size) {
		prefWidth = size;
		prefHeight = size;
		return this;
	}

	public Cell prefWidth (String prefWidth) {
		this.prefWidth = prefWidth;
		return this;
	}

	public Cell prefHeight (String prefHeight) {
		this.prefHeight = prefHeight;
		return this;
	}

	public Cell prefSize (int size) {
		prefWidth = String.valueOf(size);
		prefHeight = String.valueOf(size);
		return this;
	}

	public Cell prefWidth (int prefWidth) {
		this.prefWidth = String.valueOf(prefWidth);
		return this;
	}

	public Cell prefHeight (int prefHeight) {
		this.prefHeight = String.valueOf(prefHeight);
		return this;
	}

	public Cell maxSize (String size) {
		maxWidth = size;
		maxHeight = size;
		return this;
	}

	public Cell maxWidth (String maxWidth) {
		this.maxWidth = maxWidth;
		return this;
	}

	public Cell maxHeight (String maxHeight) {
		this.maxHeight = maxHeight;
		return this;
	}

	public Cell maxSize (int size) {
		maxWidth = String.valueOf(size);
		maxHeight = String.valueOf(size);
		return this;
	}

	public Cell maxWidth (int maxWidth) {
		this.maxWidth = String.valueOf(maxWidth);
		return this;
	}

	public Cell maxHeight (int maxHeight) {
		this.maxHeight = String.valueOf(maxHeight);
		return this;
	}

	public Cell space (String space) {
		spaceTop = space;
		spaceLeft = space;
		spaceBottom = space;
		spaceRight = space;
		return this;
	}

	public Cell space (String top, String left, String bottom, String right) {
		spaceTop = top;
		spaceLeft = left;
		spaceBottom = bottom;
		spaceRight = right;
		return this;
	}

	public Cell spaceTop (String spaceTop) {
		this.spaceTop = spaceTop;
		return this;
	}

	public Cell spaceLeft (String spaceLeft) {
		this.spaceLeft = spaceLeft;
		return this;
	}

	public Cell spaceBottom (String spaceBottom) {
		this.spaceBottom = spaceBottom;
		return this;
	}

	public Cell spaceRight (String spaceRight) {
		this.spaceRight = spaceRight;
		return this;
	}

	public Cell space (int space) {
		String value = String.valueOf(space);
		spaceTop = value;
		spaceLeft = value;
		spaceBottom = value;
		spaceRight = value;
		return this;
	}

	public Cell space (int top, int left, int bottom, int right) {
		spaceTop = String.valueOf(top);
		spaceLeft = String.valueOf(left);
		spaceBottom = String.valueOf(bottom);
		spaceRight = String.valueOf(right);
		return this;
	}

	public Cell spaceTop (int spaceTop) {
		this.spaceTop = String.valueOf(spaceTop);
		return this;
	}

	public Cell spaceLeft (int spaceLeft) {
		this.spaceLeft = String.valueOf(spaceLeft);
		return this;
	}

	public Cell spaceBottom (int spaceBottom) {
		this.spaceBottom = String.valueOf(spaceBottom);
		return this;
	}

	public Cell spaceRight (int spaceRight) {
		this.spaceRight = String.valueOf(spaceRight);
		return this;
	}

	public Cell pad (String pad) {
		padTop = pad;
		padLeft = pad;
		padBottom = pad;
		padRight = pad;
		return this;
	}

	public Cell pad (String top, String left, String bottom, String right) {
		padTop = top;
		padLeft = left;
		padBottom = bottom;
		padRight = right;
		return this;
	}

	public Cell padTop (String padTop) {
		this.padTop = padTop;
		return this;
	}

	public Cell padLeft (String padLeft) {
		this.padLeft = padLeft;
		return this;
	}

	public Cell padBottom (String padBottom) {
		this.padBottom = padBottom;
		return this;
	}

	public Cell padRight (String padRight) {
		this.padRight = padRight;
		return this;
	}

	public Cell pad (int pad) {
		String value = String.valueOf(pad);
		padTop = value;
		padLeft = value;
		padBottom = value;
		padRight = value;
		return this;
	}

	public Cell pad (int top, int left, int bottom, int right) {
		padTop = String.valueOf(top);
		padLeft = String.valueOf(left);
		padBottom = String.valueOf(bottom);
		padRight = String.valueOf(right);
		return this;
	}

	public Cell padTop (int padTop) {
		this.padTop = String.valueOf(padTop);
		return this;
	}

	public Cell padLeft (int padLeft) {
		this.padLeft = String.valueOf(padLeft);
		return this;
	}

	public Cell padBottom (int padBottom) {
		this.padBottom = String.valueOf(padBottom);
		return this;
	}

	public Cell padRight (int padRight) {
		this.padRight = String.valueOf(padRight);
		return this;
	}

	public Cell fill () {
		fillX = 1f;
		fillY = 1f;
		return this;
	}

	public Cell fillX () {
		fillX = 1f;
		return this;
	}

	public Cell fillY () {
		fillY = 1f;
		return this;
	}

	public Cell fill (Float x, Float y) {
		fillX = x;
		fillY = y;
		return this;
	}

	public Cell fill (boolean x, boolean y) {
		fillX = x ? 1f : 0;
		fillY = y ? 1f : 0;
		return this;
	}

	/** Alignment of the widget within the cell. Set to {@link #CENTER}, {@link #TOP}, {@link #BOTTOM}, {@link #LEFT},
	 * {@link #RIGHT}, or any combination of those. */
	public Cell align (Integer align) {
		this.align = align;
		return this;
	}

	/** Alignment of the widget within the cell. Set to "center", "top", "bottom", "left", "right", or a string containing any
	 * combination of those. */
	public Cell align (String value) {
		align = 0;
		if (value.contains("center")) align |= CENTER;
		if (value.contains("left")) align |= LEFT;
		if (value.contains("right")) align |= RIGHT;
		if (value.contains("top")) align |= TOP;
		if (value.contains("bottom")) align |= BOTTOM;
		return this;
	}

	public Cell center () {
		if (align == null)
			align = CENTER;
		else
			align |= CENTER;
		return this;
	}

	public Cell top () {
		if (align == null)
			align = TOP;
		else {
			align |= TOP;
			align &= ~BOTTOM;
		}
		return this;
	}

	public Cell left () {
		if (align == null)
			align = LEFT;
		else {
			align |= LEFT;
			align &= ~RIGHT;
		}
		return this;
	}

	public Cell bottom () {
		if (align == null)
			align = BOTTOM;
		else {
			align |= BOTTOM;
			align &= ~TOP;
		}
		return this;
	}

	public Cell right () {
		if (align == null)
			align = RIGHT;
		else {
			align |= RIGHT;
			align &= ~LEFT;
		}
		return this;
	}

	public Cell expand () {
		expandX = 1;
		expandY = 1;
		return this;
	}

	public Cell expandX () {
		expandX = 1;
		return this;
	}

	public Cell expandY () {
		expandY = 1;
		return this;
	}

	public Cell expand (Integer x, Integer y) {
		expandX = x;
		expandY = y;
		return this;
	}

	public Cell expand (boolean x, boolean y) {
		expandX = x ? 1 : 0;
		expandY = y ? 1 : 0;
		return this;
	}

	public Cell ignore (Boolean ignore) {
		this.ignore = ignore;
		return this;
	}

	public Cell ignore () {
		this.ignore = true;
		return this;
	}

	public boolean getIgnore () {
		return ignore != null && ignore == true;
	}

	public Cell colspan (Integer colspan) {
		this.colspan = colspan;
		return this;
	}

	public Cell uniform () {
		uniformX = true;
		uniformY = true;
		return this;
	}

	public Cell uniformX () {
		uniformX = true;
		return this;
	}

	public Cell uniformY () {
		uniformY = true;
		return this;
	}

	public Cell uniform (Boolean x, Boolean y) {
		uniformX = x;
		uniformY = y;
		return this;
	}

	public int getWidgetX () {
		return widgetX;
	}

	public int getWidgetY () {
		return widgetY;
	}

	public int getWidgetWidth () {
		return widgetWidth;
	}

	public int getWidgetHeight () {
		return widgetHeight;
	}

	public int getColumn () {
		return column;
	}

	public int getRow () {
		return row;
	}

	public String getMinWidth () {
		return minWidth;
	}

	public String getMinHeight () {
		return minHeight;
	}

	public String getPrefWidth () {
		return prefWidth;
	}

	public String getPrefHeight () {
		return prefHeight;
	}

	public String getMaxWidth () {
		return maxWidth;
	}

	public String getMaxHeight () {
		return maxHeight;
	}

	public String getSpaceTop () {
		return spaceTop;
	}

	public String getSpaceLeft () {
		return spaceLeft;
	}

	public String getSpaceBottom () {
		return spaceBottom;
	}

	public String getSpaceRight () {
		return spaceRight;
	}

	public String getPadTop () {
		return padTop;
	}

	public String getPadLeft () {
		return padLeft;
	}

	public String getPadBottom () {
		return padBottom;
	}

	public String getPadRight () {
		return padRight;
	}

	public Float getFillX () {
		return fillX;
	}

	public Float getFillY () {
		return fillY;
	}

	public Integer getAlign () {
		return align;
	}

	public Integer getExpandX () {
		return expandX;
	}

	public Integer getExpandY () {
		return expandY;
	}

	public Integer getColspan () {
		return colspan;
	}

	public Boolean getUniformX () {
		return uniformX;
	}

	public Boolean getUniformY () {
		return uniformY;
	}

	public boolean isEndRow () {
		return endRow;
	}

	static Cell defaults (BaseTableLayout layout) {
		Cell defaults = new Cell(layout);
		defaults.minWidth = MIN;
		defaults.minHeight = MIN;
		defaults.prefWidth = PREF;
		defaults.prefHeight = PREF;
		defaults.maxWidth = MAX;
		defaults.maxHeight = MAX;
		defaults.spaceTop = null;
		defaults.spaceLeft = null;
		defaults.spaceBottom = null;
		defaults.spaceRight = null;
		defaults.padTop = null;
		defaults.padLeft = null;
		defaults.padBottom = null;
		defaults.padRight = null;
		defaults.fillX = 0f;
		defaults.fillY = 0f;
		defaults.align = CENTER;
		defaults.expandX = 0;
		defaults.expandY = 0;
		defaults.ignore = false;
		defaults.colspan = 1;
		return defaults;
	}
}

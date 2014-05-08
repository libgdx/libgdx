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

package com.esotericsoftware.tablelayout;

import com.esotericsoftware.tablelayout.Value.FixedValue;

import static com.esotericsoftware.tablelayout.BaseTableLayout.*;

/** A cell in a table.
 * @author Nathan Sweet */
public class Cell<C> {
	Value minWidth, minHeight;
	Value prefWidth, prefHeight;
	Value maxWidth, maxHeight;
	Value spaceTop, spaceLeft, spaceBottom, spaceRight;
	Value padTop, padLeft, padBottom, padRight;
	Float fillX, fillY;
	Integer align;
	Integer expandX, expandY;
	Boolean ignore;
	Integer colspan;
	Boolean uniformX, uniformY;

	C widget;
	float widgetX, widgetY;
	float widgetWidth, widgetHeight;

	private BaseTableLayout layout;
	boolean endRow;
	int column, row;
	int cellAboveIndex = -1;
	float computedPadTop, computedPadLeft, computedPadBottom, computedPadRight;

	public void setLayout (BaseTableLayout layout) {
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

	/** Sets the widget in this cell and adds the widget to the cell's table. If null, removes any current widget. */
	public Cell<C> setWidget (C widget) {
		layout.toolkit.setWidget(layout, this, widget);
		return this;
	}

	/** Returns the widget for this cell, or null. */
	public C getWidget () {
		return widget;
	}

	/** Returns true if the cell's widget is not null. */
	public boolean hasWidget () {
		return widget != null;
	}

	/** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified value. */
	public Cell<C> size (Value size) {
		minWidth = size;
		minHeight = size;
		prefWidth = size;
		prefHeight = size;
		maxWidth = size;
		maxHeight = size;
		return this;
	}

	/** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified values. */
	public Cell<C> size (Value width, Value height) {
		minWidth = width;
		minHeight = height;
		prefWidth = width;
		prefHeight = height;
		maxWidth = width;
		maxHeight = height;
		return this;
	}

	/** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified value. */
	public Cell<C> size (float size) {
		size(new FixedValue(size));
		return this;
	}

	/** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified values. */
	public Cell<C> size (float width, float height) {
		size(new FixedValue(width), new FixedValue(height));
		return this;
	}

	/** Sets the minWidth, prefWidth, and maxWidth to the specified value. */
	public Cell<C> width (Value width) {
		minWidth = width;
		prefWidth = width;
		maxWidth = width;
		return this;
	}

	/** Sets the minWidth, prefWidth, and maxWidth to the specified value. */
	public Cell<C> width (float width) {
		width(new FixedValue(width));
		return this;
	}

	/** Sets the minHeight, prefHeight, and maxHeight to the specified value. */
	public Cell<C> height (Value height) {
		minHeight = height;
		prefHeight = height;
		maxHeight = height;
		return this;
	}

	/** Sets the minHeight, prefHeight, and maxHeight to the specified value. */
	public Cell<C> height (float height) {
		height(new FixedValue(height));
		return this;
	}

	/** Sets the minWidth and minHeight to the specified value. */
	public Cell<C> minSize (Value size) {
		minWidth = size;
		minHeight = size;
		return this;
	}

	/** Sets the minWidth and minHeight to the specified values. */
	public Cell<C> minSize (Value width, Value height) {
		minWidth = width;
		minHeight = height;
		return this;
	}

	public Cell<C> minWidth (Value minWidth) {
		this.minWidth = minWidth;
		return this;
	}

	public Cell<C> minHeight (Value minHeight) {
		this.minHeight = minHeight;
		return this;
	}

	/** Sets the minWidth and minHeight to the specified value. */
	public Cell<C> minSize (float size) {
		minWidth = new FixedValue(size);
		minHeight = new FixedValue(size);
		return this;
	}

	/** Sets the minWidth and minHeight to the specified values. */
	public Cell<C> minSize (float width, float height) {
		minWidth = new FixedValue(width);
		minHeight = new FixedValue(height);
		return this;
	}

	public Cell<C> minWidth (float minWidth) {
		this.minWidth = new FixedValue(minWidth);
		return this;
	}

	public Cell<C> minHeight (float minHeight) {
		this.minHeight = new FixedValue(minHeight);
		return this;
	}

	/** Sets the prefWidth and prefHeight to the specified value. */
	public Cell<C> prefSize (Value size) {
		prefWidth = size;
		prefHeight = size;
		return this;
	}

	/** Sets the prefWidth and prefHeight to the specified values. */
	public Cell<C> prefSize (Value width, Value height) {
		prefWidth = width;
		prefHeight = height;
		return this;
	}

	public Cell<C> prefWidth (Value prefWidth) {
		this.prefWidth = prefWidth;
		return this;
	}

	public Cell<C> prefHeight (Value prefHeight) {
		this.prefHeight = prefHeight;
		return this;
	}

	/** Sets the prefWidth and prefHeight to the specified value. */
	public Cell<C> prefSize (float width, float height) {
		prefWidth = new FixedValue(width);
		prefHeight = new FixedValue(height);
		return this;
	}

	/** Sets the prefWidth and prefHeight to the specified values. */
	public Cell<C> prefSize (float size) {
		prefWidth = new FixedValue(size);
		prefHeight = new FixedValue(size);
		return this;
	}

	public Cell<C> prefWidth (float prefWidth) {
		this.prefWidth = new FixedValue(prefWidth);
		return this;
	}

	public Cell<C> prefHeight (float prefHeight) {
		this.prefHeight = new FixedValue(prefHeight);
		return this;
	}

	/** Sets the maxWidth and maxHeight to the specified value. */
	public Cell<C> maxSize (Value size) {
		maxWidth = size;
		maxHeight = size;
		return this;
	}

	/** Sets the maxWidth and maxHeight to the specified values. */
	public Cell<C> maxSize (Value width, Value height) {
		maxWidth = width;
		maxHeight = height;
		return this;
	}

	public Cell<C> maxWidth (Value maxWidth) {
		this.maxWidth = maxWidth;
		return this;
	}

	public Cell<C> maxHeight (Value maxHeight) {
		this.maxHeight = maxHeight;
		return this;
	}

	/** Sets the maxWidth and maxHeight to the specified value. */
	public Cell<C> maxSize (float size) {
		maxWidth = new FixedValue(size);
		maxHeight = new FixedValue(size);
		return this;
	}

	/** Sets the maxWidth and maxHeight to the specified values. */
	public Cell<C> maxSize (float width, float height) {
		maxWidth = new FixedValue(width);
		maxHeight = new FixedValue(height);
		return this;
	}

	public Cell<C> maxWidth (float maxWidth) {
		this.maxWidth = new FixedValue(maxWidth);
		return this;
	}

	public Cell<C> maxHeight (float maxHeight) {
		this.maxHeight = new FixedValue(maxHeight);
		return this;
	}

	/** Sets the spaceTop, spaceLeft, spaceBottom, and spaceRight to the specified value. */
	public Cell<C> space (Value space) {
		spaceTop = space;
		spaceLeft = space;
		spaceBottom = space;
		spaceRight = space;
		return this;
	}

	public Cell<C> space (Value top, Value left, Value bottom, Value right) {
		spaceTop = top;
		spaceLeft = left;
		spaceBottom = bottom;
		spaceRight = right;
		return this;
	}

	public Cell<C> spaceTop (Value spaceTop) {
		this.spaceTop = spaceTop;
		return this;
	}

	public Cell<C> spaceLeft (Value spaceLeft) {
		this.spaceLeft = spaceLeft;
		return this;
	}

	public Cell<C> spaceBottom (Value spaceBottom) {
		this.spaceBottom = spaceBottom;
		return this;
	}

	public Cell<C> spaceRight (Value spaceRight) {
		this.spaceRight = spaceRight;
		return this;
	}

	/** Sets the spaceTop, spaceLeft, spaceBottom, and spaceRight to the specified value. */
	public Cell<C> space (float space) {
		if (space < 0) throw new IllegalArgumentException("space cannot be < 0.");
		Value value = new FixedValue(space);
		spaceTop = value;
		spaceLeft = value;
		spaceBottom = value;
		spaceRight = value;
		return this;
	}

	public Cell<C> space (float top, float left, float bottom, float right) {
		if (top < 0) throw new IllegalArgumentException("top cannot be < 0.");
		if (left < 0) throw new IllegalArgumentException("left cannot be < 0.");
		if (bottom < 0) throw new IllegalArgumentException("bottom cannot be < 0.");
		if (right < 0) throw new IllegalArgumentException("right cannot be < 0.");
		spaceTop = new FixedValue(top);
		spaceLeft = new FixedValue(left);
		spaceBottom = new FixedValue(bottom);
		spaceRight = new FixedValue(right);
		return this;
	}

	public Cell<C> spaceTop (float spaceTop) {
		if (spaceTop < 0) throw new IllegalArgumentException("spaceTop cannot be < 0.");
		this.spaceTop = new FixedValue(spaceTop);
		return this;
	}

	public Cell<C> spaceLeft (float spaceLeft) {
		if (spaceLeft < 0) throw new IllegalArgumentException("spaceLeft cannot be < 0.");
		this.spaceLeft = new FixedValue(spaceLeft);
		return this;
	}

	public Cell<C> spaceBottom (float spaceBottom) {
		if (spaceBottom < 0) throw new IllegalArgumentException("spaceBottom cannot be < 0.");
		this.spaceBottom = new FixedValue(spaceBottom);
		return this;
	}

	public Cell<C> spaceRight (float spaceRight) {
		if (spaceRight < 0) throw new IllegalArgumentException("spaceRight cannot be < 0.");
		this.spaceRight = new FixedValue(spaceRight);
		return this;
	}

	/** Sets the padTop, padLeft, padBottom, and padRight to the specified value. */
	public Cell<C> pad (Value pad) {
		padTop = pad;
		padLeft = pad;
		padBottom = pad;
		padRight = pad;
		return this;
	}

	public Cell<C> pad (Value top, Value left, Value bottom, Value right) {
		padTop = top;
		padLeft = left;
		padBottom = bottom;
		padRight = right;
		return this;
	}

	public Cell<C> padTop (Value padTop) {
		this.padTop = padTop;
		return this;
	}

	public Cell<C> padLeft (Value padLeft) {
		this.padLeft = padLeft;
		return this;
	}

	public Cell<C> padBottom (Value padBottom) {
		this.padBottom = padBottom;
		return this;
	}

	public Cell<C> padRight (Value padRight) {
		this.padRight = padRight;
		return this;
	}

	/** Sets the padTop, padLeft, padBottom, and padRight to the specified value. */
	public Cell<C> pad (float pad) {
		Value value = new FixedValue(pad);
		padTop = value;
		padLeft = value;
		padBottom = value;
		padRight = value;
		return this;
	}

	public Cell<C> pad (float top, float left, float bottom, float right) {
		padTop = new FixedValue(top);
		padLeft = new FixedValue(left);
		padBottom = new FixedValue(bottom);
		padRight = new FixedValue(right);
		return this;
	}

	public Cell<C> padTop (float padTop) {
		this.padTop = new FixedValue(padTop);
		return this;
	}

	public Cell<C> padLeft (float padLeft) {
		this.padLeft = new FixedValue(padLeft);
		return this;
	}

	public Cell<C> padBottom (float padBottom) {
		this.padBottom = new FixedValue(padBottom);
		return this;
	}

	public Cell<C> padRight (float padRight) {
		this.padRight = new FixedValue(padRight);
		return this;
	}

	/** Sets fillX and fillY to 1. */
	public Cell<C> fill () {
		fillX = 1f;
		fillY = 1f;
		return this;
	}

	/** Sets fillX to 1. */
	public Cell<C> fillX () {
		fillX = 1f;
		return this;
	}

	/** Sets fillY to 1. */
	public Cell<C> fillY () {
		fillY = 1f;
		return this;
	}

	public Cell<C> fill (Float x, Float y) {
		fillX = x;
		fillY = y;
		return this;
	}

	/** Sets fillX and fillY to 1 if true, 0 if false. */
	public Cell<C> fill (boolean x, boolean y) {
		fillX = x ? 1f : 0;
		fillY = y ? 1f : 0;
		return this;
	}

	/** Sets fillX and fillY to 1 if true, 0 if false. */
	public Cell<C> fill (boolean fill) {
		fillX = fill ? 1f : 0;
		fillY = fill ? 1f : 0;
		return this;
	}

	/** Sets the alignment of the widget within the cell. Set to {@link #CENTER}, {@link #TOP}, {@link #BOTTOM}, {@link #LEFT},
	 * {@link #RIGHT}, or any combination of those. */
	public Cell<C> align (Integer align) {
		this.align = align;
		return this;
	}

	/** Sets the alignment of the widget within the cell to {@link #CENTER}. This clears any other alignment. */
	public Cell<C> center () {
		align = CENTER;
		return this;
	}

	/** Adds {@link #TOP} and clears {@link #BOTTOM} for the alignment of the widget within the cell. */
	public Cell<C> top () {
		if (align == null)
			align = TOP;
		else {
			align |= TOP;
			align &= ~BOTTOM;
		}
		return this;
	}

	/** Adds {@link #LEFT} and clears {@link #RIGHT} for the alignment of the widget within the cell. */
	public Cell<C> left () {
		if (align == null)
			align = LEFT;
		else {
			align |= LEFT;
			align &= ~RIGHT;
		}
		return this;
	}

	/** Adds {@link #BOTTOM} and clears {@link #TOP} for the alignment of the widget within the cell. */
	public Cell<C> bottom () {
		if (align == null)
			align = BOTTOM;
		else {
			align |= BOTTOM;
			align &= ~TOP;
		}
		return this;
	}

	/** Adds {@link #RIGHT} and clears {@link #LEFT} for the alignment of the widget within the cell. */
	public Cell<C> right () {
		if (align == null)
			align = RIGHT;
		else {
			align |= RIGHT;
			align &= ~LEFT;
		}
		return this;
	}

	/** Sets expandX and expandY to 1. */
	public Cell<C> expand () {
		expandX = 1;
		expandY = 1;
		return this;
	}

	/** Sets expandX to 1. */
	public Cell<C> expandX () {
		expandX = 1;
		return this;
	}

	/** Sets expandY to 1. */
	public Cell<C> expandY () {
		expandY = 1;
		return this;
	}

	public Cell<C> expand (Integer x, Integer y) {
		expandX = x;
		expandY = y;
		return this;
	}

	/** Sets expandX and expandY to 1 if true, 0 if false. */
	public Cell<C> expand (boolean x, boolean y) {
		expandX = x ? 1 : 0;
		expandY = y ? 1 : 0;
		return this;
	}

	public Cell<C> ignore (Boolean ignore) {
		this.ignore = ignore;
		return this;
	}

	/** Sets ignore to true. */
	public Cell<C> ignore () {
		this.ignore = true;
		return this;
	}

	public boolean getIgnore () {
		return ignore != null && ignore == true;
	}

	public Cell<C> colspan (Integer colspan) {
		this.colspan = colspan;
		return this;
	}

	/** Sets uniformX and uniformY to true. */
	public Cell<C> uniform () {
		uniformX = true;
		uniformY = true;
		return this;
	}

	/** Sets uniformX to true. */
	public Cell<C> uniformX () {
		uniformX = true;
		return this;
	}

	/** Sets uniformY to true. */
	public Cell<C> uniformY () {
		uniformY = true;
		return this;
	}

	public Cell<C> uniform (Boolean x, Boolean y) {
		uniformX = x;
		uniformY = y;
		return this;
	}

	public void setWidgetBounds (float x, float y, float width, float height) {
		widgetX = x;
		widgetY = y;
		widgetWidth = width;
		widgetHeight = height;
	}

	public float getWidgetX () {
		return widgetX;
	}

	public void setWidgetX (float widgetX) {
		this.widgetX = widgetX;
	}

	public float getWidgetY () {
		return widgetY;
	}

	public void setWidgetY (float widgetY) {
		this.widgetY = widgetY;
	}

	public float getWidgetWidth () {
		return widgetWidth;
	}

	public void setWidgetWidth (float widgetWidth) {
		this.widgetWidth = widgetWidth;
	}

	public float getWidgetHeight () {
		return widgetHeight;
	}

	public void setWidgetHeight (float widgetHeight) {
		this.widgetHeight = widgetHeight;
	}

	public int getColumn () {
		return column;
	}

	public int getRow () {
		return row;
	}

	/** @return May be null if this cell is row defaults. */
	public Value getMinWidthValue () {
		return minWidth;
	}

	public float getMinWidth () {
		return minWidth == null ? 0 : minWidth.width(this);
	}

	/** @return May be null if this cell is row defaults. */
	public Value getMinHeightValue () {
		return minHeight;
	}

	public float getMinHeight () {
		return minHeight == null ? 0 : minHeight.height(this);
	}

	/** @return May be null if this cell is row defaults. */
	public Value getPrefWidthValue () {
		return prefWidth;
	}

	public float getPrefWidth () {
		return prefWidth == null ? 0 : prefWidth.width(this);
	}

	/** @return May be null if this cell is row defaults. */
	public Value getPrefHeightValue () {
		return prefHeight;
	}

	public float getPrefHeight () {
		return prefHeight == null ? 0 : prefHeight.height(this);
	}

	/** @return May be null if this cell is row defaults. */
	public Value getMaxWidthValue () {
		return maxWidth;
	}

	public float getMaxWidth () {
		return maxWidth == null ? 0 : maxWidth.width(this);
	}

	/** @return May be null if this cell is row defaults. */
	public Value getMaxHeightValue () {
		return maxHeight;
	}

	public float getMaxHeight () {
		return maxHeight == null ? 0 : maxHeight.height(this);
	}

	/** @return May be null if this value is not set. */
	public Value getSpaceTopValue () {
		return spaceTop;
	}

	public float getSpaceTop () {
		return spaceTop == null ? 0 : spaceTop.height(this);
	}

	/** @return May be null if this value is not set. */
	public Value getSpaceLeftValue () {
		return spaceLeft;
	}

	public float getSpaceLeft () {
		return spaceLeft == null ? 0 : spaceLeft.width(this);
	}

	/** @return May be null if this value is not set. */
	public Value getSpaceBottomValue () {
		return spaceBottom;
	}

	public float getSpaceBottom () {
		return spaceBottom == null ? 0 : spaceBottom.height(this);
	}

	/** @return May be null if this value is not set. */
	public Value getSpaceRightValue () {
		return spaceRight;
	}

	public float getSpaceRight () {
		return spaceRight == null ? 0 : spaceRight.width(this);
	}

	/** @return May be null if this value is not set. */
	public Value getPadTopValue () {
		return padTop;
	}

	public float getPadTop () {
		return padTop == null ? 0 : padTop.height(this);
	}

	/** @return May be null if this value is not set. */
	public Value getPadLeftValue () {
		return padLeft;
	}

	public float getPadLeft () {
		return padLeft == null ? 0 : padLeft.width(this);
	}

	/** @return May be null if this value is not set. */
	public Value getPadBottomValue () {
		return padBottom;
	}

	public float getPadBottom () {
		return padBottom == null ? 0 : padBottom.height(this);
	}

	/** @return May be null if this value is not set. */
	public Value getPadRightValue () {
		return padRight;
	}

	public float getPadRight () {
		return padRight == null ? 0 : padRight.width(this);
	}

	/** @return May be null if this value is not set. */
	public Float getFillX () {
		return fillX;
	}

	/** @return May be null. */
	public Float getFillY () {
		return fillY;
	}

	/** @return May be null. */
	public Integer getAlign () {
		return align;
	}

	/** @return May be null. */
	public Integer getExpandX () {
		return expandX;
	}

	/** @return May be null. */
	public Integer getExpandY () {
		return expandY;
	}

	/** @return May be null. */
	public Integer getColspan () {
		return colspan;
	}

	/** @return May be null. */
	public Boolean getUniformX () {
		return uniformX;
	}

	/** @return May be null. */
	public Boolean getUniformY () {
		return uniformY;
	}

	/** Returns true if this cell is the last cell in the row. */
	public boolean isEndRow () {
		return endRow;
	}

	/** The actual amount of combined padding and spacing from the last layout. */
	public float getComputedPadTop () {
		return computedPadTop;
	}

	/** The actual amount of combined padding and spacing from the last layout. */
	public float getComputedPadLeft () {
		return computedPadLeft;
	}

	/** The actual amount of combined padding and spacing from the last layout. */
	public float getComputedPadBottom () {
		return computedPadBottom;
	}

	/** The actual amount of combined padding and spacing from the last layout. */
	public float getComputedPadRight () {
		return computedPadRight;
	}

	public Cell<C> row () {
		return layout.row();
	}

	public BaseTableLayout getLayout () {
		return layout;
	}

	/** Sets all constraint fields to null. */
	public void clear () {
		minWidth = null;
		minHeight = null;
		prefWidth = null;
		prefHeight = null;
		maxWidth = null;
		maxHeight = null;
		spaceTop = null;
		spaceLeft = null;
		spaceBottom = null;
		spaceRight = null;
		padTop = null;
		padLeft = null;
		padBottom = null;
		padRight = null;
		fillX = null;
		fillY = null;
		align = null;
		expandX = null;
		expandY = null;
		ignore = null;
		colspan = null;
		uniformX = null;
		uniformY = null;
	}

	/** Reset state so the cell can be reused. Doesn't reset the constraint fields. */
	public void free () {
		widget = null;
		layout = null;
		endRow = false;
		cellAboveIndex = -1;
	}

	/** Set all constraints to cell default values. */
	void defaults () {
		minWidth = Value.minWidth;
		minHeight = Value.minHeight;
		prefWidth = Value.prefWidth;
		prefHeight = Value.prefHeight;
		maxWidth = Value.maxWidth;
		maxHeight = Value.maxHeight;
		spaceTop = Value.zero;
		spaceLeft = Value.zero;
		spaceBottom = Value.zero;
		spaceRight = Value.zero;
		padTop = Value.zero;
		padLeft = Value.zero;
		padBottom = Value.zero;
		padRight = Value.zero;
		fillX = 0f;
		fillY = 0f;
		align = CENTER;
		expandX = 0;
		expandY = 0;
		ignore = false;
		colspan = 1;
		uniformX = null;
		uniformY = null;
	}
}

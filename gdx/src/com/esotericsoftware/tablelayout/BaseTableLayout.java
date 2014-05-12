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

import java.util.ArrayList;
import java.util.List;

/** Base layout functionality.
 * @author Nathan Sweet */
abstract public class BaseTableLayout<C, T extends C, L extends BaseTableLayout, K extends Toolkit<C, T, L>> {
	static public final int CENTER = 1 << 0;
	static public final int TOP = 1 << 1;
	static public final int BOTTOM = 1 << 2;
	static public final int LEFT = 1 << 3;
	static public final int RIGHT = 1 << 4;

	static public enum Debug {
		none, all, table, cell, widget
	}

	K toolkit;
	T table;
	private int columns, rows;

	private final ArrayList<Cell> cells = new ArrayList(4);
	private final Cell cellDefaults;
	private final ArrayList<Cell> columnDefaults = new ArrayList(2);
	private Cell rowDefaults;

	private boolean sizeInvalid = true;
	private float[] columnMinWidth, rowMinHeight;
	private float[] columnPrefWidth, rowPrefHeight;
	private float tableMinWidth, tableMinHeight;
	private float tablePrefWidth, tablePrefHeight;
	private float[] columnWidth, rowHeight;
	private float[] expandWidth, expandHeight;
	private float[] columnWeightedWidth, rowWeightedHeight;

	Value padTop, padLeft, padBottom, padRight;
	int align = CENTER;
	Debug debug = Debug.none;

	public BaseTableLayout (K toolkit) {
		this.toolkit = toolkit;
		cellDefaults = toolkit.obtainCell((L)this);
		cellDefaults.defaults();
	}

	/** Invalidates the layout. The cached min and pref sizes are recalculated the next time layout is done or the min or pref sizes
	 * are accessed. */
	public void invalidate () {
		sizeInvalid = true;
	}

	/** Invalidates the layout of this table and every parent widget. */
	abstract public void invalidateHierarchy ();

	/** Adds a new cell to the table with the specified widget. */
	public Cell<C> add (C widget) {
		Cell cell = toolkit.obtainCell((L)this);
		cell.widget = widget;

		if (cells.size() > 0) {
			// Set cell column and row.
			Cell lastCell = cells.get(cells.size() - 1);
			if (!lastCell.endRow) {
				cell.column = lastCell.column + lastCell.colspan;
				cell.row = lastCell.row;
			} else {
				cell.column = 0;
				cell.row = lastCell.row + 1;
			}
			// Set the index of the cell above.
			if (cell.row > 0) {
				outer:
				for (int i = cells.size() - 1; i >= 0; i--) {
					Cell other = cells.get(i);
					for (int column = other.column, nn = column + other.colspan; column < nn; column++) {
						if (column == cell.column) {
							cell.cellAboveIndex = i;
							break outer;
						}
					}
				}
			}
		} else {
			cell.column = 0;
			cell.row = 0;
		}
		cells.add(cell);

		cell.set(cellDefaults);
		if (cell.column < columnDefaults.size()) {
			Cell columnCell = columnDefaults.get(cell.column);
			if (columnCell != null) cell.merge(columnCell);
		}
		cell.merge(rowDefaults);

		if (widget != null) toolkit.addChild(table, widget);

		return cell;
	}

	/** Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row. */
	public Cell row () {
		if (cells.size() > 0) {
			endRow();
			invalidate();
		}
		if (rowDefaults != null) toolkit.freeCell(rowDefaults);
		rowDefaults = toolkit.obtainCell((L)this);
		rowDefaults.clear();
		return rowDefaults;
	}

	private void endRow () {
		int rowColumns = 0;
		for (int i = cells.size() - 1; i >= 0; i--) {
			Cell cell = cells.get(i);
			if (cell.endRow) break;
			rowColumns += cell.colspan;
		}
		columns = Math.max(columns, rowColumns);
		rows++;
		cells.get(cells.size() - 1).endRow = true;
	}

	/** Gets the cell values that will be used as the defaults for all cells in the specified column. Columns are indexed starting
	 * at 0. */
	public Cell columnDefaults (int column) {
		Cell cell = columnDefaults.size() > column ? columnDefaults.get(column) : null;
		if (cell == null) {
			cell = toolkit.obtainCell((L)this);
			cell.clear();
			if (column >= columnDefaults.size()) {
				for (int i = columnDefaults.size(); i < column; i++)
					columnDefaults.add(null);
				columnDefaults.add(cell);
			} else
				columnDefaults.set(column, cell);
		}
		return cell;
	}

	/** Removes all widgets and cells from the table (same as {@link #clear()}) and additionally resets all table properties and
	 * cell, column, and row defaults. */
	public void reset () {
		clear();
		padTop = null;
		padLeft = null;
		padBottom = null;
		padRight = null;
		align = CENTER;
		if (debug != Debug.none) toolkit.clearDebugRectangles((L)this);
		debug = Debug.none;
		cellDefaults.defaults();
		for (int i = 0, n = columnDefaults.size(); i < n; i++) {
			Cell columnCell = columnDefaults.get(i);
			if (columnCell != null) toolkit.freeCell(columnCell);
		}
		columnDefaults.clear();
	}

	/** Removes all widgets and cells from the table. */
	public void clear () {
		for (int i = cells.size() - 1; i >= 0; i--) {
			Cell cell = cells.get(i);
			Object widget = cell.widget;
			if (widget != null) toolkit.removeChild(table, (C)widget);
			toolkit.freeCell(cell);
		}
		cells.clear();
		rows = 0;
		columns = 0;
		if (rowDefaults != null) toolkit.freeCell(rowDefaults);
		rowDefaults = null;
		invalidate();
	}

	/** Returns the cell for the specified widget in this table, or null. */
	public Cell<C> getCell (C widget) {
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.widget == widget) return c;
		}
		return null;
	}

	/** Returns the cells for this table. */
	public List<Cell> getCells () {
		return cells;
	}

	public void setToolkit (K toolkit) {
		this.toolkit = toolkit;
	}

	/** Returns the table widget that will be laid out. */
	public T getTable () {
		return table;
	}

	/** Sets the table widget that will be laid out. */
	public void setTable (T table) {
		this.table = table;
	}

	/** The minimum width of the table. */
	public float getMinWidth () {
		if (sizeInvalid) computeSize();
		return tableMinWidth;
	}

	/** The minimum size of the table. */
	public float getMinHeight () {
		if (sizeInvalid) computeSize();
		return tableMinHeight;
	}

	/** The preferred width of the table. */
	public float getPrefWidth () {
		if (sizeInvalid) computeSize();
		return tablePrefWidth;
	}

	/** The preferred height of the table. */
	public float getPrefHeight () {
		if (sizeInvalid) computeSize();
		return tablePrefHeight;
	}

	/** The cell values that will be used as the defaults for all cells. */
	public Cell defaults () {
		return cellDefaults;
	}

	/** Sets the padTop, padLeft, padBottom, and padRight around the table to the specified value. */
	public L pad (Value pad) {
		padTop = pad;
		padLeft = pad;
		padBottom = pad;
		padRight = pad;
		sizeInvalid = true;
		return (L)this;
	}

	public L pad (Value top, Value left, Value bottom, Value right) {
		padTop = top;
		padLeft = left;
		padBottom = bottom;
		padRight = right;
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the top edge of the table. */
	public L padTop (Value padTop) {
		this.padTop = padTop;
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the left edge of the table. */
	public L padLeft (Value padLeft) {
		this.padLeft = padLeft;
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the bottom edge of the table. */
	public L padBottom (Value padBottom) {
		this.padBottom = padBottom;
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the right edge of the table. */
	public L padRight (Value padRight) {
		this.padRight = padRight;
		sizeInvalid = true;
		return (L)this;
	}

	/** Sets the padTop, padLeft, padBottom, and padRight around the table to the specified value. */
	public L pad (float pad) {
		padTop = new FixedValue(pad);
		padLeft = new FixedValue(pad);
		padBottom = new FixedValue(pad);
		padRight = new FixedValue(pad);
		sizeInvalid = true;
		return (L)this;
	}

	public L pad (float top, float left, float bottom, float right) {
		padTop = new FixedValue(top);
		padLeft = new FixedValue(left);
		padBottom = new FixedValue(bottom);
		padRight = new FixedValue(right);
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the top edge of the table. */
	public L padTop (float padTop) {
		this.padTop = new FixedValue(padTop);
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the left edge of the table. */
	public L padLeft (float padLeft) {
		this.padLeft = new FixedValue(padLeft);
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the bottom edge of the table. */
	public L padBottom (float padBottom) {
		this.padBottom = new FixedValue(padBottom);
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the right edge of the table. */
	public L padRight (float padRight) {
		this.padRight = new FixedValue(padRight);
		sizeInvalid = true;
		return (L)this;
	}

	/** Alignment of the logical table within the table widget. Set to {@link #CENTER}, {@link #TOP}, {@link #BOTTOM} ,
	 * {@link #LEFT}, {@link #RIGHT}, or any combination of those. */
	public L align (int align) {
		this.align = align;
		return (L)this;
	}

	/** Sets the alignment of the logical table within the table widget to {@link #CENTER}. This clears any other alignment. */
	public L center () {
		align = CENTER;
		return (L)this;
	}

	/** Adds {@link #TOP} and clears {@link #BOTTOM} for the alignment of the logical table within the table widget. */
	public L top () {
		align |= TOP;
		align &= ~BOTTOM;
		return (L)this;
	}

	/** Adds {@link #LEFT} and clears {@link #RIGHT} for the alignment of the logical table within the table widget. */
	public L left () {
		align |= LEFT;
		align &= ~RIGHT;
		return (L)this;
	}

	/** Adds {@link #BOTTOM} and clears {@link #TOP} for the alignment of the logical table within the table widget. */
	public L bottom () {
		align |= BOTTOM;
		align &= ~TOP;
		return (L)this;
	}

	/** Adds {@link #RIGHT} and clears {@link #LEFT} for the alignment of the logical table within the table widget. */
	public L right () {
		align |= RIGHT;
		align &= ~LEFT;
		return (L)this;
	}

	/** Turns on all debug lines. */
	public L debug () {
		this.debug = Debug.all;
		invalidate();
		return (L)this;
	}

	/** Turns on table debug lines. */
	public L debugTable () {
		this.debug = Debug.table;
		invalidate();
		return (L)this;
	}

	/** Turns on cell debug lines. */
	public L debugCell () {
		this.debug = Debug.cell;
		invalidate();
		return (L)this;
	}

	/** Turns on widget debug lines. */
	public L debugWidget () {
		this.debug = Debug.widget;
		invalidate();
		return (L)this;
	}

	/** Turns on debug lines. */
	public L debug (Debug debug) {
		this.debug = debug;
		if (debug == Debug.none)
			toolkit.clearDebugRectangles((L)this);
		else
			invalidate();
		return (L)this;
	}

	public Debug getDebug () {
		return debug;
	}

	public Value getPadTopValue () {
		return padTop;
	}

	public float getPadTop () {
		return padTop == null ? 0 : padTop.height(table);
	}

	public Value getPadLeftValue () {
		return padLeft;
	}

	public float getPadLeft () {
		return padLeft == null ? 0 : padLeft.width(table);
	}

	public Value getPadBottomValue () {
		return padBottom;
	}

	public float getPadBottom () {
		return padBottom == null ? 0 : padBottom.height(table);
	}

	public Value getPadRightValue () {
		return padRight;
	}

	public float getPadRight () {
		return padRight == null ? 0 : padRight.width(table);
	}

	public int getAlign () {
		return align;
	}

	/** Returns the row index for the y coordinate, or -1 if there are no cells. */
	public int getRow (float y) {
		int row = 0;
		y += h(padTop);
		int i = 0, n = cells.size();
		if (n == 0) return -1;
		if (n == 1) return 0;
		if (cells.get(0).widgetY < cells.get(1).widgetY) {
			// Using y-down coordinate system.
			while (i < n) {
				Cell c = cells.get(i++);
				if (c.getIgnore()) continue;
				if (c.widgetY + c.computedPadTop > y) break;
				if (c.endRow) row++;
			}
			return row - 1;
		}
		// Using y-up coordinate system.
		while (i < n) {
			Cell c = cells.get(i++);
			if (c.getIgnore()) continue;
			if (c.widgetY + c.computedPadTop < y) break;
			if (c.endRow) row++;
		}
		return row;
	}

	private float[] ensureSize (float[] array, int size) {
		if (array == null || array.length < size) return new float[size];
		for (int i = 0, n = array.length; i < n; i++)
			array[i] = 0;
		return array;
	}

	private float w (Value value) {
		return value == null ? 0 : value.width(table);
	}

	private float h (Value value) {
		return value == null ? 0 : value.height(table);
	}

	private float w (Value value, Cell cell) {
		return value == null ? 0 : value.width(cell);
	}

	private float h (Value value, Cell cell) {
		return value == null ? 0 : value.height(cell);
	}

	private void computeSize () {
		sizeInvalid = false;

		ArrayList<Cell> cells = this.cells;

		if (cells.size() > 0 && !cells.get(cells.size() - 1).endRow) endRow();

		columnMinWidth = ensureSize(columnMinWidth, columns);
		rowMinHeight = ensureSize(rowMinHeight, rows);
		columnPrefWidth = ensureSize(columnPrefWidth, columns);
		rowPrefHeight = ensureSize(rowPrefHeight, rows);
		columnWidth = ensureSize(columnWidth, columns);
		rowHeight = ensureSize(rowHeight, rows);
		expandWidth = ensureSize(expandWidth, columns);
		expandHeight = ensureSize(expandHeight, rows);

		float spaceRightLast = 0;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Collect columns/rows that expand.
			if (c.expandY != 0 && expandHeight[c.row] == 0) expandHeight[c.row] = c.expandY;
			if (c.colspan == 1 && c.expandX != 0 && expandWidth[c.column] == 0) expandWidth[c.column] = c.expandX;

			// Compute combined padding/spacing for cells.
			// Spacing between widgets isn't additive, the larger is used. Also, no spacing around edges.
			c.computedPadLeft = w(c.padLeft, c) + (c.column == 0 ? 0 : Math.max(0, w(c.spaceLeft, c) - spaceRightLast));
			c.computedPadTop = h(c.padTop, c);
			if (c.cellAboveIndex != -1) {
				Cell above = cells.get(c.cellAboveIndex);
				c.computedPadTop += Math.max(0, h(c.spaceTop, c) - h(above.spaceBottom, above));
			}
			float spaceRight = w(c.spaceRight, c);
			c.computedPadRight = w(c.padRight, c) + ((c.column + c.colspan) == columns ? 0 : spaceRight);
			c.computedPadBottom = h(c.padBottom, c) + (c.row == rows - 1 ? 0 : h(c.spaceBottom, c));
			spaceRightLast = spaceRight;

			// Determine minimum and preferred cell sizes.
			float prefWidth = c.prefWidth.get(c);
			float prefHeight = c.prefHeight.get(c);
			float minWidth = c.minWidth.get(c);
			float minHeight = c.minHeight.get(c);
			float maxWidth = c.maxWidth.get(c);
			float maxHeight = c.maxHeight.get(c);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (prefHeight < minHeight) prefHeight = minHeight;
			if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth;
			if (maxHeight > 0 && prefHeight > maxHeight) prefHeight = maxHeight;

			if (c.colspan == 1) { // Spanned column min and pref width is added later.
				float hpadding = c.computedPadLeft + c.computedPadRight;
				columnPrefWidth[c.column] = Math.max(columnPrefWidth[c.column], prefWidth + hpadding);
				columnMinWidth[c.column] = Math.max(columnMinWidth[c.column], minWidth + hpadding);
			}
			float vpadding = c.computedPadTop + c.computedPadBottom;
			rowPrefHeight[c.row] = Math.max(rowPrefHeight[c.row], prefHeight + vpadding);
			rowMinHeight[c.row] = Math.max(rowMinHeight[c.row], minHeight + vpadding);
		}

		// Colspan with expand will expand all spanned columns if none of the spanned columns have expand.
		outer:
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore || c.expandX == 0) continue;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				if (expandWidth[column] != 0) continue outer;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				expandWidth[column] = c.expandX;
		}

		// Distribute any additional min and pref width added by colspanned cells to the columns spanned.
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore || c.colspan == 1) continue;

			float minWidth = c.minWidth.get(c);
			float prefWidth = c.prefWidth.get(c);
			float maxWidth = c.maxWidth.get(c);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth;

			float spannedMinWidth = -(c.computedPadLeft + c.computedPadRight), spannedPrefWidth = spannedMinWidth;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++) {
				spannedMinWidth += columnMinWidth[column];
				spannedPrefWidth += columnPrefWidth[column];
			}

			// Distribute extra space using expand, if any columns have expand.
			float totalExpandWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				totalExpandWidth += expandWidth[column];

			float extraMinWidth = Math.max(0, minWidth - spannedMinWidth);
			float extraPrefWidth = Math.max(0, prefWidth - spannedPrefWidth);
			for (int column = c.column, nn = column + c.colspan; column < nn; column++) {
				float ratio = totalExpandWidth == 0 ? 1f / c.colspan : expandWidth[column] / totalExpandWidth;
				columnMinWidth[column] += extraMinWidth * ratio;
				columnPrefWidth[column] += extraPrefWidth * ratio;
			}
		}

		// Collect uniform size.
		float uniformMinWidth = 0, uniformMinHeight = 0;
		float uniformPrefWidth = 0, uniformPrefHeight = 0;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Collect uniform sizes.
			if (c.uniformX == Boolean.TRUE && c.colspan == 1) {
				float hpadding = c.computedPadLeft + c.computedPadRight;
				uniformMinWidth = Math.max(uniformMinWidth, columnMinWidth[c.column] - hpadding);
				uniformPrefWidth = Math.max(uniformPrefWidth, columnPrefWidth[c.column] - hpadding);
			}
			if (c.uniformY == Boolean.TRUE) {
				float vpadding = c.computedPadTop + c.computedPadBottom;
				uniformMinHeight = Math.max(uniformMinHeight, rowMinHeight[c.row] - vpadding);
				uniformPrefHeight = Math.max(uniformPrefHeight, rowPrefHeight[c.row] - vpadding);
			}
		}

		// Size uniform cells to the same width/height.
		if (uniformPrefWidth > 0 || uniformPrefHeight > 0) {
			for (int i = 0, n = cells.size(); i < n; i++) {
				Cell c = cells.get(i);
				if (c.ignore) continue;
				if (uniformPrefWidth > 0 && c.uniformX == Boolean.TRUE && c.colspan == 1) {
					float hpadding = c.computedPadLeft + c.computedPadRight;
					columnMinWidth[c.column] = uniformMinWidth + hpadding;
					columnPrefWidth[c.column] = uniformPrefWidth + hpadding;
				}
				if (uniformPrefHeight > 0 && c.uniformY == Boolean.TRUE) {
					float vpadding = c.computedPadTop + c.computedPadBottom;
					rowMinHeight[c.row] = uniformMinHeight + vpadding;
					rowPrefHeight[c.row] = uniformPrefHeight + vpadding;
				}
			}
		}

		// Determine table min and pref size.
		tableMinWidth = 0;
		tableMinHeight = 0;
		tablePrefWidth = 0;
		tablePrefHeight = 0;
		for (int i = 0; i < columns; i++) {
			tableMinWidth += columnMinWidth[i];
			tablePrefWidth += columnPrefWidth[i];
		}
		for (int i = 0; i < rows; i++) {
			tableMinHeight += rowMinHeight[i];
			tablePrefHeight += Math.max(rowMinHeight[i], rowPrefHeight[i]);
		}
		float hpadding = w(padLeft) + w(padRight);
		float vpadding = h(padTop) + h(padBottom);
		tableMinWidth = tableMinWidth + hpadding;
		tableMinHeight = tableMinHeight + vpadding;
		tablePrefWidth = Math.max(tablePrefWidth + hpadding, tableMinWidth);
		tablePrefHeight = Math.max(tablePrefHeight + vpadding, tableMinHeight);
	}

	/** Positions and sizes children of the table using the cell associated with each child. The values given are the position
	 * within the parent and size of the table. */
	public void layout (float layoutX, float layoutY, float layoutWidth, float layoutHeight) {
		Toolkit toolkit = this.toolkit;
		ArrayList<Cell> cells = this.cells;

		if (sizeInvalid) computeSize();

		float padLeft = w(this.padLeft);
		float hpadding = padLeft + w(padRight);
		float padTop = h(this.padTop);
		float vpadding = padTop + h(padBottom);

		float totalExpandWidth = 0, totalExpandHeight = 0;
		for (int i = 0; i < columns; i++)
			totalExpandWidth += expandWidth[i];
		for (int i = 0; i < rows; i++)
			totalExpandHeight += expandHeight[i];

		// Size columns and rows between min and pref size using (preferred - min) size to weight distribution of extra space.
		float[] columnWeightedWidth;
		float totalGrowWidth = tablePrefWidth - tableMinWidth;
		if (totalGrowWidth == 0)
			columnWeightedWidth = columnMinWidth;
		else {
			float extraWidth = Math.min(totalGrowWidth, Math.max(0, layoutWidth - tableMinWidth));
			columnWeightedWidth = this.columnWeightedWidth = ensureSize(this.columnWeightedWidth, columns);
			for (int i = 0; i < columns; i++) {
				float growWidth = columnPrefWidth[i] - columnMinWidth[i];
				float growRatio = growWidth / totalGrowWidth;
				columnWeightedWidth[i] = columnMinWidth[i] + extraWidth * growRatio;
			}
		}

		float[] rowWeightedHeight;
		float totalGrowHeight = tablePrefHeight - tableMinHeight;
		if (totalGrowHeight == 0)
			rowWeightedHeight = rowMinHeight;
		else {
			rowWeightedHeight = this.rowWeightedHeight = ensureSize(this.rowWeightedHeight, rows);
			float extraHeight = Math.min(totalGrowHeight, Math.max(0, layoutHeight - tableMinHeight));
			for (int i = 0; i < rows; i++) {
				float growHeight = rowPrefHeight[i] - rowMinHeight[i];
				float growRatio = growHeight / totalGrowHeight;
				rowWeightedHeight[i] = rowMinHeight[i] + extraHeight * growRatio;
			}
		}

		// Determine widget and cell sizes (before expand or fill).
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			float spannedWeightedWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedWeightedWidth += columnWeightedWidth[column];
			float weightedHeight = rowWeightedHeight[c.row];

			float prefWidth = c.prefWidth.get(c);
			float prefHeight = c.prefHeight.get(c);
			float minWidth = c.minWidth.get(c);
			float minHeight = c.minHeight.get(c);
			float maxWidth = c.maxWidth.get(c);
			float maxHeight = c.maxHeight.get(c);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (prefHeight < minHeight) prefHeight = minHeight;
			if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth;
			if (maxHeight > 0 && prefHeight > maxHeight) prefHeight = maxHeight;

			c.widgetWidth = Math.min(spannedWeightedWidth - c.computedPadLeft - c.computedPadRight, prefWidth);
			c.widgetHeight = Math.min(weightedHeight - c.computedPadTop - c.computedPadBottom, prefHeight);

			if (c.colspan == 1) columnWidth[c.column] = Math.max(columnWidth[c.column], spannedWeightedWidth);
			rowHeight[c.row] = Math.max(rowHeight[c.row], weightedHeight);
		}

		// Distribute remaining space to any expanding columns/rows.
		if (totalExpandWidth > 0) {
			float extra = layoutWidth - hpadding;
			for (int i = 0; i < columns; i++)
				extra -= columnWidth[i];
			float used = 0;
			int lastIndex = 0;
			for (int i = 0; i < columns; i++) {
				if (expandWidth[i] == 0) continue;
				float amount = extra * expandWidth[i] / totalExpandWidth;
				columnWidth[i] += amount;
				used += amount;
				lastIndex = i;
			}
			columnWidth[lastIndex] += extra - used;
		}
		if (totalExpandHeight > 0) {
			float extra = layoutHeight - vpadding;
			for (int i = 0; i < rows; i++)
				extra -= rowHeight[i];
			float used = 0;
			int lastIndex = 0;
			for (int i = 0; i < rows; i++) {
				if (expandHeight[i] == 0) continue;
				float amount = extra * expandHeight[i] / totalExpandHeight;
				rowHeight[i] += amount;
				used += amount;
				lastIndex = i;
			}
			rowHeight[lastIndex] += extra - used;
		}

		// Distribute any additional width added by colspanned cells to the columns spanned.
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			if (c.colspan == 1) continue;

			float extraWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				extraWidth += columnWeightedWidth[column] - columnWidth[column];
			extraWidth -= Math.max(0, c.computedPadLeft + c.computedPadRight);

			extraWidth /= c.colspan;
			if (extraWidth > 0) {
				for (int column = c.column, nn = column + c.colspan; column < nn; column++)
					columnWidth[column] += extraWidth;
			}
		}

		// Determine table size.
		float tableWidth = hpadding, tableHeight = vpadding;
		for (int i = 0; i < columns; i++)
			tableWidth += columnWidth[i];
		for (int i = 0; i < rows; i++)
			tableHeight += rowHeight[i];

		// Position table within the container.
		float x = layoutX + padLeft;
		if ((align & RIGHT) != 0)
			x += layoutWidth - tableWidth;
		else if ((align & LEFT) == 0) // Center
			x += (layoutWidth - tableWidth) / 2;

		float y = layoutY + padTop;
		if ((align & BOTTOM) != 0)
			y += layoutHeight - tableHeight;
		else if ((align & TOP) == 0) // Center
			y += (layoutHeight - tableHeight) / 2;

		// Position widgets within cells.
		float currentX = x, currentY = y;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			float spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.computedPadLeft + c.computedPadRight;

			currentX += c.computedPadLeft;

			if (c.fillX > 0) {
				c.widgetWidth = spannedCellWidth * c.fillX;
				float maxWidth = c.maxWidth.get(c);
				if (maxWidth > 0) c.widgetWidth = Math.min(c.widgetWidth, maxWidth);
			}
			if (c.fillY > 0) {
				c.widgetHeight = rowHeight[c.row] * c.fillY - c.computedPadTop - c.computedPadBottom;
				float maxHeight = c.maxHeight.get(c);
				if (maxHeight > 0) c.widgetHeight = Math.min(c.widgetHeight, maxHeight);
			}

			if ((c.align & LEFT) != 0)
				c.widgetX = currentX;
			else if ((c.align & RIGHT) != 0)
				c.widgetX = currentX + spannedCellWidth - c.widgetWidth;
			else
				c.widgetX = currentX + (spannedCellWidth - c.widgetWidth) / 2;

			if ((c.align & TOP) != 0)
				c.widgetY = currentY + c.computedPadTop;
			else if ((c.align & BOTTOM) != 0)
				c.widgetY = currentY + rowHeight[c.row] - c.widgetHeight - c.computedPadBottom;
			else
				c.widgetY = currentY + (rowHeight[c.row] - c.widgetHeight + c.computedPadTop - c.computedPadBottom) / 2;

			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.computedPadRight;
		}

		// Draw debug widgets and bounds.
		if (debug == Debug.none) return;
		toolkit.clearDebugRectangles(this);
		currentX = x;
		currentY = y;
		if (debug == Debug.table || debug == Debug.all) {
			toolkit.addDebugRectangle(this, Debug.table, layoutX, layoutY, layoutWidth, layoutHeight);
			toolkit.addDebugRectangle(this, Debug.table, x, y, tableWidth - hpadding, tableHeight - vpadding);
		}
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Widget bounds.
			if (debug == Debug.widget || debug == Debug.all)
				toolkit.addDebugRectangle(this, Debug.widget, c.widgetX, c.widgetY, c.widgetWidth, c.widgetHeight);

			// Cell bounds.
			float spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.computedPadLeft + c.computedPadRight;
			currentX += c.computedPadLeft;
			if (debug == Debug.cell || debug == Debug.all) {
				toolkit.addDebugRectangle(this, Debug.cell, currentX, currentY + c.computedPadTop, spannedCellWidth, rowHeight[c.row]
					- c.computedPadTop - c.computedPadBottom);
			}

			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.computedPadRight;
		}
	}
}

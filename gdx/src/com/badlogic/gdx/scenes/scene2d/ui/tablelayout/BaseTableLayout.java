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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/** Base layout functionality.
 * @author Nathan Sweet */
abstract public class BaseTableLayout<C, T extends C, L extends BaseTableLayout, K extends Toolkit> {
	static public final int CENTER = 1 << 0;
	static public final int TOP = 1 << 1;
	static public final int BOTTOM = 1 << 2;
	static public final int LEFT = 1 << 3;
	static public final int RIGHT = 1 << 4;

	static public final String MIN = "min";
	static public final String PREF = "pref";
	static public final String MAX = "max";

	static public final int DEBUG_NONE = 0;
	static public final int DEBUG_ALL = 1 << 0;
	static public final int DEBUG_TABLE = 1 << 1;
	static public final int DEBUG_CELL = 1 << 2;
	static public final int DEBUG_WIDGET = 1 << 3;

	K toolkit;
	T table;
	HashMap<String, C> nameToWidget = new HashMap();
	HashMap<C, Cell> widgetToCell = new HashMap();
	private int columns, rows;

	private final ArrayList<Cell> cells = new ArrayList();
	private final Cell cellDefaults = Cell.defaults(this);
	private final ArrayList<Cell> columnDefaults = new ArrayList(4);
	private Cell rowDefaults;

	private int layoutX, layoutY;
	private int layoutWidth, layoutHeight;

	private int tableMinWidth, tableMinHeight;
	private int tablePrefWidth, tablePrefHeight;

	String width, height;
	String padTop, padLeft, padBottom, padRight;
	int align = CENTER;
	int debug = DEBUG_NONE;

	public BaseTableLayout (K toolkit) {
		this.toolkit = toolkit;
	}

	abstract public void invalidate ();

	abstract public void invalidateHierarchy ();

	/** The position within it's parent and size of the widget that will be laid out. Must be set before layout. */
	public void setLayoutSize (int tableLayoutX, int tableLayoutY, int tableLayoutWidth, int tableLayoutHeight) {
		this.layoutX = tableLayoutX;
		this.layoutY = tableLayoutY;
		this.layoutWidth = tableLayoutWidth;
		this.layoutHeight = tableLayoutHeight;
	}

	/** Sets the name of a widget so it may be referenced in {@link #parse(String)}. */
	public C register (String name, C widget) {
		name = name.toLowerCase();
		if (nameToWidget.containsKey(name)) throw new IllegalArgumentException("Name is already used: " + name);
		nameToWidget.put(name, widget);
		return widget;
	}

	/** Parses a table description and adds the widgets and cells to the table. */
	public void parse (String tableDescription) {
		TableLayoutParser.parse(this, tableDescription);
	}

	/** Adds a new cell to the table with the specified widget.
	 * @param widget May be null to add a cell without a widget. */
	public Cell<C> add (C widget) { // BOZO - Add column description parsing.
		Cell cell = new Cell(this);
		cell.widget = widget;

		// BOZO - Fix widget being null!

		widgetToCell.put(widget, cell);

		for (Entry<String, C> entry : nameToWidget.entrySet()) {
			if (widget == entry.getValue()) {
				cell.name = entry.getKey();
				break;
			}
		}

		if (cells.size() > 0) {
			// Set cell x and y.
			Cell lastCell = cells.get(cells.size() - 1);
			if (!lastCell.endRow) {
				cell.column = lastCell.column + lastCell.colspan;
				cell.row = lastCell.row;
			} else
				cell.row = lastCell.row + 1;
			// Set the index of the cell above.
			if (cell.row > 0) {
				outer:
				for (int i = cells.size() - 1; i >= 0; i--) {
					Cell other = cells.get(i);
					for (int column = other.column, nn = column + other.colspan; column < nn; column++) {
						if (other.column == cell.column) {
							cell.cellAboveIndex = i;
							break outer;
						}
					}
				}
			}
		}
		cells.add(cell);

		if (cell.column < columnDefaults.size()) {
			Cell columnDefaults = this.columnDefaults.get(cell.column);
			cell.set(columnDefaults != null ? columnDefaults : cellDefaults);
		} else
			cell.set(cellDefaults);
		cell.merge(rowDefaults);

		toolkit.addChild(table, widget, null);

		return cell;
	}

	/** Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row. */
	public Cell row () {
		if (cells.size() > 0) endRow();
		rowDefaults = new Cell(this);
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

	/** Gets the cell values that will be used as the defaults for all cells in the specified column. */
	public Cell columnDefaults (int column) {
		Cell cell = columnDefaults.size() > column ? columnDefaults.get(column) : null;
		if (cell == null) {
			cell = new Cell(this);
			cell.set(cellDefaults);
			if (column <= columnDefaults.size()) {
				for (int i = columnDefaults.size(); i < column; i++)
					columnDefaults.add(null);
				columnDefaults.add(cell);
			} else
				columnDefaults.set(column, cell);
		}
		return cell;
	}

	/** Removes all widgets and cells from the table and resets all table properties and cell, column, and row defaults. */
	public void reset () {
		clear();
		padTop = null;
		padLeft = null;
		padBottom = null;
		padRight = null;
		align = CENTER;
		if (debug != DEBUG_NONE) toolkit.clearDebugRectangles(this);
		debug = DEBUG_NONE;
		cellDefaults.set(Cell.defaults(this));
		columnDefaults.clear();
		rowDefaults = null;
	}

	/** Removes all widgets and cells from the table. */
	public void clear () {
		for (int i = cells.size() - 1; i >= 0; i--)
			toolkit.removeChild(table, (C)cells.get(i).widget);
		cells.clear();
		nameToWidget.clear();
		widgetToCell.clear();
		rows = 0;
		columns = 0;
	}

	/** Returns the widget with the specified name, anywhere in the table hierarchy. */
	public C getWidget (String name) {
		return nameToWidget.get(name.toLowerCase());
	}

	/** Returns all widgets, anywhere in the table hierarchy. */
	public List<C> getWidgets () {
		return new ArrayList(nameToWidget.values());
	}

	/** Returns all widgets with the specified name prefix, anywhere in the table hierarchy. */
	public List<C> getWidgets (String namePrefix) {
		ArrayList<C> widgets = new ArrayList();
		for (Entry<String, C> entry : nameToWidget.entrySet())
			if (entry.getKey().startsWith(namePrefix)) widgets.add(entry.getValue());
		return widgets;
	}

	/** Returns the cell for the specified widget, anywhere in the table hierarchy. */
	public Cell getCell (C widget) {
		return widgetToCell.get(widget);
	}

	/** Returns the cell with the specified name, anywhere in the table hierarchy. */
	public Cell getCell (String name) {
		return getCell(getWidget(name));
	}

	/** Returns all cells, anywhere in the table hierarchy. */
	public List<Cell> getAllCells () {
		return new ArrayList(widgetToCell.values());
	}

	/** Returns all cells with the specified name prefix, anywhere in the table hierarchy. */
	public List<Cell> getAllCells (String namePrefix) {
		ArrayList<Cell> cells = new ArrayList();
		for (Cell cell : widgetToCell.values())
			if (cell.name.startsWith(namePrefix)) cells.add(cell);
		return cells;
	}

	/** Returns the cells for this table. */
	public List<Cell> getCells () {
		return cells;
	}

	/** Sets the widget in the cell with the specified name. */
	public void setWidget (String name, C widget) {
		getCell(name).setWidget(widget);
	}

	/** Sets that this table is nested under the specified parent. This allows the root table to look up widgets and cells in nested
	 * tables, for convenience. */
	public void setParent (BaseTableLayout parent) {
		// Shared per table hierarchy.
		nameToWidget = parent.nameToWidget;
		widgetToCell = parent.widgetToCell;
	}

	public void setToolkit (K toolkit) {
		this.toolkit = toolkit;
	}

	/** Returns the widget that will be laid out. */
	public T getTable () {
		return table;
	}

	/** Sets the widget that will be laid out. */
	public void setTable (T table) {
		this.table = table;
	}

	/** The x position within it's parent of the widget that will be laid out. Set by {@link #setLayoutSize(int, int, int, int)}
	 * before layout. */
	public int getLayoutX () {
		return layoutX;
	}

	/** The y position within it's parent of the widget that will be laid out. Set by {@link #setLayoutSize(int, int, int, int)}
	 * before layout. */
	public int getLayoutY () {
		return layoutY;
	}

	/** The width of the widget that will be laid out. Set by {@link #setLayoutSize(int, int, int, int)} before layout. */
	public int getLayoutWidth () {
		return layoutWidth;
	}

	/** The height of the widget that will be laid out. Set by {@link #setLayoutSize(int, int, int, int)} before layout. */
	public int getLayoutHeight () {
		return layoutHeight;
	}

	/** The minimum width of the table. Available after laying out. */
	public int getMinWidth () {
		return tableMinWidth;
	}

	/** The minimum size of the table. Available after laying out. */
	public int getMinHeight () {
		return tableMinHeight;
	}

	/** The preferred width of the table. May be dependent on {@link #layoutWidth}. Available after laying out. */
	public int getPrefWidth () {
		return tablePrefWidth;
	}

	/** The preferred height of the table. May be dependent on {@link #layoutHeight}. Available after laying out. */
	public int getPrefHeight () {
		return tablePrefHeight;
	}

	/** The cell values that will be used as the defaults for all cells. */
	public Cell defaults () {
		return cellDefaults;
	}

	public K getToolkit () {
		return toolkit;
	}

	/** The fixed size of the table. */
	public L size (String width, String height) {
		this.width = width;
		this.height = height;
		return (L)this;
	}

	/** The fixed width of the table, or null. */
	public L width (String width) {
		this.width = width;
		return (L)this;
	}

	/** The fixed height of the table, or null. */
	public L height (String height) {
		this.height = height;
		return (L)this;
	}

	/** The fixed size of the table. */
	public L size (int width, int height) {
		this.width = String.valueOf(width);
		this.height = String.valueOf(height);
		return (L)this;
	}

	/** The fixed width of the table. */
	public L width (int width) {
		this.width = String.valueOf(width);
		return (L)this;
	}

	/** The fixed height of the table. */
	public L height (int height) {
		this.height = String.valueOf(height);
		return (L)this;
	}

	/** Padding around the table. */
	public L pad (String pad) {
		padTop = pad;
		padLeft = pad;
		padBottom = pad;
		padRight = pad;
		return (L)this;
	}

	/** Padding around the table. */
	public L pad (String top, String left, String bottom, String right) {
		padTop = top;
		padLeft = left;
		padBottom = bottom;
		padRight = right;
		return (L)this;
	}

	/** Padding at the top of the table. */
	public L padTop (String padTop) {
		this.padTop = padTop;
		return (L)this;
	}

	/** Padding at the left of the table. */
	public L padLeft (String padLeft) {
		this.padLeft = padLeft;
		return (L)this;
	}

	/** Padding at the bottom of the table. */
	public L padBottom (String padBottom) {
		this.padBottom = padBottom;
		return (L)this;
	}

	/** Padding at the right of the table. */
	public L padRight (String padRight) {
		this.padRight = padRight;
		return (L)this;
	}

	/** Padding around the table. */
	public L pad (int pad) {
		padTop = String.valueOf(pad);
		padLeft = String.valueOf(pad);
		padBottom = String.valueOf(pad);
		padRight = String.valueOf(pad);
		return (L)this;
	}

	/** Padding around the table. */
	public L pad (int top, int left, int bottom, int right) {
		padTop = String.valueOf(top);
		padLeft = String.valueOf(left);
		padBottom = String.valueOf(bottom);
		padRight = String.valueOf(right);
		return (L)this;
	}

	/** Padding at the top of the table. */
	public L padTop (int padTop) {
		this.padTop = String.valueOf(padTop);
		return (L)this;
	}

	/** Padding at the left of the table. */
	public L padLeft (int padLeft) {
		this.padLeft = String.valueOf(padLeft);
		return (L)this;
	}

	/** Padding at the bottom of the table. */
	public L padBottom (int padBottom) {
		this.padBottom = String.valueOf(padBottom);
		return (L)this;
	}

	/** Padding at the right of the table. */
	public L padRight (int padRight) {
		this.padRight = String.valueOf(padRight);
		return (L)this;
	}

	/** Alignment of the table within the widget being laid out. Set to {@link #CENTER}, {@link #TOP}, {@link #BOTTOM},
	 * {@link #LEFT}, {@link #RIGHT}, or any combination of those. */
	public L align (int align) {
		this.align = align;
		return (L)this;
	}

	/** Alignment of the table within the widget being laid out. Set to "center", "top", "bottom", "left", "right", or a string
	 * containing any combination of those. */
	public L align (String value) {
		align = 0;
		if (value.contains("center")) align |= CENTER;
		if (value.contains("left")) align |= LEFT;
		if (value.contains("right")) align |= RIGHT;
		if (value.contains("top")) align |= TOP;
		if (value.contains("bottom")) align |= BOTTOM;
		return (L)this;
	}

	/** Sets the alignment of the table within the widget being laid out to {@link #CENTER}. */
	public L center () {
		align |= CENTER;
		return (L)this;
	}

	/** Sets the alignment of the table within the widget being laid out to {@link #TOP}. */
	public L top () {
		align |= TOP;
		align &= ~BOTTOM;
		return (L)this;
	}

	/** Sets the alignment of the table within the widget being laid out to {@link #LEFT}. */
	public L left () {
		align |= LEFT;
		align &= ~RIGHT;
		return (L)this;
	}

	/** Sets the alignment of the table within the widget being laid out to {@link #BOTTOM}. */
	public L bottom () {
		align |= BOTTOM;
		align &= ~TOP;
		return (L)this;
	}

	/** Sets the alignment of the table within the widget being laid out to {@link #RIGHT}. */
	public L right () {
		align |= RIGHT;
		align &= ~LEFT;
		return (L)this;
	}

	/** Turns on debug lines. Set to {@value #DEBUG_ALL}, {@value #DEBUG_TABLE}, {@value #DEBUG_CELL}, {@value #DEBUG_WIDGET}, or
	 * any combination of those. Set to {@value #DEBUG_NONE} to disable. */
	public L debug (int debug) {
		this.debug = debug;
		if (debug == DEBUG_NONE) toolkit.clearDebugRectangles(this);
		return (L)this;
	}

	/** Turns on debug lines. Set to "all", "table", "cell", "widget", or a string containing any combination of those. Set to null
	 * to disable. */
	public L debug (String value) {
		debug = 0;
		if (value == null) return (L)this;
		if (value.equalsIgnoreCase("true")) debug |= DEBUG_ALL;
		if (value.contains("all")) debug |= DEBUG_ALL;
		if (value.contains("cell")) debug |= DEBUG_CELL;
		if (value.contains("table")) debug |= DEBUG_TABLE;
		if (value.contains("widget")) debug |= DEBUG_WIDGET;
		if (debug == DEBUG_NONE) toolkit.clearDebugRectangles(this);
		return (L)this;
	}

	public int getDebug () {
		return debug;
	}

	public String getWidth () {
		return width;
	}

	public String getHeight () {
		return height;
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

	public int getAlign () {
		return align;
	}

	/** Positions and sizes children of the widget being laid out using the cell associated with each child. */
	public void layout () {
		Toolkit toolkit = this.toolkit;
		ArrayList<Cell> cells = this.cells;

		if (cells.size() > 0 && !cells.get(cells.size() - 1).endRow) endRow();

		// Determine minimum and preferred cell sizes. Also compute the combined padding/spacing for each cell.
		int[] columnMinWidth = new int[columns];
		int[] rowMinHeight = new int[rows];
		int[] columnPrefWidth = new int[columns];
		int[] rowPrefHeight = new int[rows];
		int spaceRightLast = 0;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Spacing between widgets isn't additive, the larger is used. Also, no spacing around edges.
			c.padLeftTemp = c.column == 0 ? toolkit.width(this, c.padLeft) : toolkit.width(this, c.padLeft)
				+ Math.max(0, toolkit.width(this, c.spaceLeft) - spaceRightLast);
			c.padTopTemp = c.cellAboveIndex == -1 ? toolkit.height(this, c.padTop) : toolkit.height(this, c.padTop)
				+ Math.max(0, toolkit.height(this, c.spaceTop) - toolkit.height(this, cells.get(c.cellAboveIndex).spaceBottom));
			int spaceRight = toolkit.width(this, c.spaceRight);
			c.padRightTemp = c.column + c.colspan == columns ? toolkit.width(this, c.padRight) : toolkit.width(this, c.padRight)
				+ spaceRight;
			c.padBottomTemp = c.row == rows - 1 ? toolkit.height(this, c.padBottom) : toolkit.height(this, c.padBottom)
				+ toolkit.height(this, c.spaceBottom);
			spaceRightLast = spaceRight;

			int prefWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.prefWidth);
			int prefHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.prefHeight);
			int minWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.minWidth);
			int minHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.minHeight);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (prefHeight < minHeight) prefHeight = minHeight;

			if (c.colspan == 1) {
				int hpadding = c.padLeftTemp + c.padRightTemp;
				columnPrefWidth[c.column] = Math.max(columnPrefWidth[c.column], prefWidth + hpadding);
				columnMinWidth[c.column] = Math.max(columnMinWidth[c.column], minWidth + hpadding);
			}
			int vpadding = c.padTopTemp + c.padBottomTemp;
			rowPrefHeight[c.row] = Math.max(rowPrefHeight[c.row], prefHeight + vpadding);
			rowMinHeight[c.row] = Math.max(rowMinHeight[c.row], minHeight + vpadding);
		}

		// Determine maximum cell sizes using (preferred - min) size to weight distribution of extra space.
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
		int hpadding = toolkit.width(this, padLeft) + toolkit.width(this, padRight);
		int vpadding = toolkit.height(this, padTop) + toolkit.height(this, padBottom);
		int width = toolkit.width(this, this.width) - hpadding;
		int height = toolkit.height(this, this.height) - vpadding;
		tableMinWidth = Math.max(tableMinWidth + hpadding, width);
		tableMinHeight = Math.max(tableMinHeight + vpadding, height);
		tablePrefWidth = Math.max(tablePrefWidth + hpadding, tableMinWidth);
		tablePrefHeight = Math.max(tablePrefHeight + vpadding, tableMinHeight);

		int[] columnMaxWidth;
		int tableLayoutWidth = this.layoutWidth;
		int totalGrowWidth = tablePrefWidth - tableMinWidth;
		if (totalGrowWidth == 0)
			columnMaxWidth = columnMinWidth;
		else {
			int extraWidth = Math.max(0, tableLayoutWidth - tableMinWidth);
			columnMaxWidth = new int[columns];
			for (int i = 0; i < columns; i++) {
				int growWidth = columnPrefWidth[i] - columnMinWidth[i];
				float growRatio = growWidth / (float)totalGrowWidth;
				columnMaxWidth[i] = columnMinWidth[i] + (int)(extraWidth * growRatio);
			}
		}

		int[] rowMaxHeight;
		int tableLayoutHeight = this.layoutHeight;
		int totalGrowHeight = tablePrefHeight - tableMinHeight;
		if (totalGrowHeight == 0)
			rowMaxHeight = rowMinHeight;
		else {
			int extraHeight = Math.max(0, tableLayoutHeight - tableMinHeight);
			rowMaxHeight = new int[rows];
			for (int i = 0; i < rows; i++) {
				int growHeight = rowPrefHeight[i] - rowMinHeight[i];
				float growRatio = growHeight / (float)totalGrowHeight;
				rowMaxHeight[i] = rowMinHeight[i] + (int)(extraHeight * growRatio);
			}
		}

		// Determine widget and cell sizes (before uniform/expand/fill). Also collect columns/rows that expand.
		int[] columnWidth = new int[columns];
		int[] rowHeight = new int[rows];
		float[] expandWidth = new float[columns];
		float[] expandHeight = new float[rows];
		float totalExpandWidth = 0, totalExpandHeight = 0;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			int spannedCellMaxWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++) {
				spannedCellMaxWidth += columnMaxWidth[column];

				if (c.colspan == 1 && c.expandX != 0 && expandWidth[column] == 0) {
					expandWidth[column] = c.expandX / (float)c.colspan;
					totalExpandWidth += c.expandX / (float)c.colspan;
				}
			}
			spannedCellMaxWidth -= c.padLeftTemp + c.padRightTemp;
			if (c.expandY != 0 && expandHeight[c.row] == 0) {
				expandHeight[c.row] = c.expandY;
				totalExpandHeight += c.expandY;
			}

			int prefWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.prefWidth);
			int prefHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.prefHeight);
			int minWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.minWidth);
			int minHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.minHeight);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (prefHeight < minHeight) prefHeight = minHeight;

			c.widgetWidth = Math.min(spannedCellMaxWidth, prefWidth);
			c.widgetHeight = Math.min(rowMaxHeight[c.row] - c.padTopTemp - c.padBottomTemp, prefHeight);

			if (c.colspan == 1)
				columnWidth[c.column] = Math.max(columnWidth[c.column], c.widgetWidth + c.padLeftTemp + c.padRightTemp);
			rowHeight[c.row] = Math.max(rowHeight[c.row], c.widgetHeight + c.padTopTemp + c.padBottomTemp);
		}

		// Uniform cells are all the same width/height.
		int uniformMaxWidth = 0, uniformMaxHeight = 0;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			if (c.uniformX != null) uniformMaxWidth = Math.max(uniformMaxWidth, columnWidth[c.column]);
			if (c.uniformY != null) uniformMaxHeight = Math.max(uniformMaxHeight, rowHeight[c.row]);
		}
		if (uniformMaxWidth > 0 || uniformMaxHeight > 0) {
			outer:
			for (int i = 0, n = cells.size(); i < n; i++) {
				Cell c = cells.get(i);
				if (c.ignore) continue;
				if (uniformMaxWidth > 0 && c.uniformX != null) {
					int diff = uniformMaxWidth - columnWidth[c.column];
					if (diff > 0) {
						columnWidth[c.column] = uniformMaxWidth;
						tableMinWidth += diff;
						tablePrefWidth += diff;
					}
				}
				if (uniformMaxHeight > 0 && c.uniformY != null) {
					int diff = uniformMaxHeight - rowHeight[c.row];
					if (diff > 0) {
						rowHeight[c.row] = uniformMaxHeight;
						tableMinHeight += diff;
						tablePrefHeight += diff;
					}
				}
				continue outer;
			}
		}

		// Distribute remaining space to any expanding columns/rows.
		if (totalExpandWidth > 0) {
			int extra = Math.max(0, tableLayoutWidth - tablePrefWidth);
			int used = 0, lastIndex = 0;
			for (int i = 0; i < columns; i++) {
				if (expandWidth[i] == 0) continue;
				int amount = (int)(extra * expandWidth[i] / totalExpandWidth);
				columnWidth[i] += amount;
				used += amount;
				lastIndex = i;
			}
			columnWidth[lastIndex] += extra - used;
		}
		if (totalExpandHeight > 0) {
			int extra = Math.max(0, tableLayoutHeight - tablePrefHeight);
			int used = 0, lastIndex = 0;
			for (int i = 0; i < rows; i++) {
				if (expandHeight[i] == 0) continue;
				int amount = (int)(extra * expandHeight[i] / totalExpandHeight);
				rowHeight[i] += amount;
				used += amount;
				lastIndex = i;
			}
			rowHeight[lastIndex] += extra - used;
		}

		// Distribute any additional width added by colspanned cells evenly to the columns spanned.
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			if (c.colspan == 1) continue;

			int minWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.minWidth);

			int spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];

			int extraWidth = Math.max(0, minWidth - spannedCellWidth) / c.colspan;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				columnWidth[column] += extraWidth;

			c.widgetWidth = Math.max(c.widgetWidth, minWidth - (c.padLeftTemp + c.padRightTemp));
		}

		// Determine table size.
		int tableWidth = 0, tableHeight = 0;
		for (int i = 0; i < columns; i++)
			tableWidth += columnWidth[i];
		tableWidth = Math.max(tableWidth + hpadding, width);
		for (int i = 0; i < rows; i++)
			tableHeight += rowHeight[i];
		tableHeight = Math.max(tableHeight + vpadding, height);

		// Position table within the container.
		int x = layoutX + toolkit.width(this, padLeft);
		if ((align & RIGHT) != 0)
			x += tableLayoutWidth - tableWidth;
		else if ((align & LEFT) == 0) // Center
			x += (tableLayoutWidth - tableWidth) / 2;

		int y = layoutY + toolkit.height(this, padTop);
		if ((align & BOTTOM) != 0)
			y += tableLayoutHeight - tableHeight;
		else if ((align & TOP) == 0) // Center
			y += (tableLayoutHeight - tableHeight) / 2;

		// Position widgets within cells.
		int currentX = x;
		int currentY = y;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			int spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.padLeftTemp + c.padRightTemp;

			currentX += c.padLeftTemp;

			if (c.fillX > 0) {
				c.widgetWidth = (int)(spannedCellWidth * c.fillX);
				int maxWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.maxWidth);
				if (maxWidth > 0) c.widgetWidth = Math.min(c.widgetWidth, maxWidth);
			}
			if (c.fillY > 0) {
				c.widgetHeight = (int)(rowHeight[c.row] * c.fillY) - c.padTopTemp - c.padBottomTemp;
				int maxHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.maxHeight);
				if (maxHeight > 0) c.widgetHeight = Math.min(c.widgetHeight, maxHeight);
			}

			if ((c.align & LEFT) != 0)
				c.widgetX = currentX;
			else if ((c.align & RIGHT) != 0)
				c.widgetX = currentX + spannedCellWidth - c.widgetWidth;
			else
				c.widgetX = currentX + (spannedCellWidth - c.widgetWidth) / 2;

			if ((c.align & TOP) != 0)
				c.widgetY = currentY + c.padTopTemp;
			else if ((c.align & BOTTOM) != 0)
				c.widgetY = currentY + rowHeight[c.row] - c.widgetHeight - c.padBottomTemp;
			else
				c.widgetY = currentY + (rowHeight[c.row] - c.widgetHeight + c.padTopTemp - c.padBottomTemp) / 2;

			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.padRightTemp;
		}

		// Draw widgets and bounds.
		if (debug == DEBUG_NONE) return;
		toolkit.clearDebugRectangles(this);
		currentX = x;
		currentY = y;
		if ((debug & DEBUG_TABLE) != 0 || (debug & DEBUG_ALL) != 0) {
			toolkit.addDebugRectangle(this, DEBUG_TABLE, layoutX, layoutY, tableLayoutWidth, tableLayoutHeight);
			toolkit.addDebugRectangle(this, DEBUG_TABLE, x, y, tableWidth - hpadding, tableHeight - vpadding);
		}
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Widget bounds.
			if ((debug & DEBUG_WIDGET) != 0 || (debug & DEBUG_ALL) != 0)
				toolkit.addDebugRectangle(this, DEBUG_WIDGET, c.widgetX, c.widgetY, c.widgetWidth, c.widgetHeight);

			// Cell bounds.
			int spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.padLeftTemp + c.padRightTemp;
			currentX += c.padLeftTemp;
			if ((debug & DEBUG_CELL) != 0 || (debug & DEBUG_ALL) != 0) {
				toolkit.addDebugRectangle(this, DEBUG_CELL, currentX, currentY + c.padTopTemp, spannedCellWidth, rowHeight[c.row]
					- c.padTopTemp - c.padBottomTemp);
			}

			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.padRightTemp;
		}
	}
}

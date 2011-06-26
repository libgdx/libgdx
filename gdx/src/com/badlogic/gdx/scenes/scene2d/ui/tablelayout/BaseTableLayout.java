
package com.badlogic.gdx.scenes.scene2d.ui.tablelayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Base layout functionality. Subclasses must implement {@link #newTableLayout()}.
 */
abstract public class BaseTableLayout<T> {
	static private final ArrayList<String> classPrefixes = new ArrayList();

	static public final int CENTER = 1 << 0;
	static public final int TOP = 1 << 1;
	static public final int BOTTOM = 1 << 2;
	static public final int LEFT = 1 << 3;
	static public final int RIGHT = 1 << 4;

	static public final int X = 1 << 5;
	static public final int Y = 1 << 6;

	static public final String MIN = "min";
	static public final String PREF = "pref";
	static public final String MAX = "max";

	static public final String DEBUG_ALL = "all";
	static public final String DEBUG_TABLE = "table";
	static public final String DEBUG_CELL = "cell";
	static public final String DEBUG_WIDGET = "widget";

	public String width, height;
	public String padTop, padLeft, padBottom, padRight;
	public int align = CENTER;
	public String debug;

	public int tableLayoutX, tableLayoutY;
	public int tableLayoutWidth, tableLayoutHeight;
	public int tableMinWidth, tableMinHeight;
	public int tablePrefWidth, tablePrefHeight;

	/** The cells for this table only. */
	public final ArrayList<Cell> cells = new ArrayList();

	/** The cell values that will be used as the defaults for all cells. */
	public final Cell cellDefaults = Cell.defaults();

	private final ArrayList<Cell> columnDefaults = new ArrayList(4);
	private Cell rowDefaults;
	private int columns, rows;
	private HashMap<String, T> nameToWidget = new HashMap();
	private HashMap<T, Cell> widgetToCell = new HashMap();

	/**
	 * Adds a child to the specified parent.
	 * @param layoutString May be null.
	 */
	abstract public void addChild (T parent, T child, String layoutString);

	abstract public void removeChild (T parent, T child);

	/**
	 * Returns a new TableLayout that will be nested under this table. The {@link #setParent(BaseTableLayout) parent} of the
	 * returned layout must be set.
	 */
	abstract public BaseTableLayout newTableLayout ();

	/**
	 * Returns a new widget that sizes all of its children to its size.
	 */
	abstract public T newStack ();

	abstract public int getMinWidth (T widget);

	abstract public int getMinHeight (T widget);

	abstract public int getPrefWidth (T widget);

	abstract public int getPrefHeight (T widget);

	abstract public int getMaxWidth (T widget);

	abstract public int getMaxHeight (T widget);

	/**
	 * Marks the TableLayout as needing to layout again.
	 */
	abstract public void invalidate ();

	/**
	 * Clears all debugging rectangles.
	 */
	abstract public void clearDebugRectangles ();

	/**
	 * Adds a rectangle that should be drawn for debugging.
	 */
	abstract public void addDebugRectangle (String type, int x, int y, int w, int h);

	/**
	 * Returns the widget that will be laid out.
	 */
	abstract public T getTable ();

	/**
	 * Sets that this table is nested under the specified parent. This allows the root table to look up widgets and cells in nested
	 * tables, for convenience.
	 */
	public void setParent (BaseTableLayout parent) {
		// Shared per table hierarchy.
		nameToWidget = parent.nameToWidget;
		widgetToCell = parent.widgetToCell;
	}

	/**
	 * Sets the name of a widget so it may be referenced in {@link #parse(String)}.
	 */
	public T register (String name, T widget) {
		name = name.toLowerCase();
		if (nameToWidget.containsKey(name)) throw new IllegalArgumentException("Name is already used: " + name);
		nameToWidget.put(name, widget);
		return widget;
	}

	/**
	 * Parses a table description and adds the widgets and cells to the table.
	 */
	public void parse (String tableDescription) {
		TableLayoutParser.parse(this, tableDescription);
	}

	/**
	 * Adds a new cell to the table with the specified widget.
	 * @param widget May be null to add a cell without a widget.
	 */
	public Cell addCell (T widget) { // BOZO - Add column description parsing.
		Cell cell = new Cell();
		cell.widget = widget;

		widgetToCell.put(widget, cell);

		for (Entry<String, T> entry : nameToWidget.entrySet()) {
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

		addChild(getTable(), widget, null);

		return cell;
	}

	/**
	 * Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row.
	 */
	public Cell startRow () {
		if (cells.size() > 0) endRow();
		rowDefaults = new Cell();
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

	/**
	 * Gets the cell values that will be used as the defaults for all cells in the specified column.
	 */
	public Cell getColumnDefaults (int column) {
		Cell cell = columnDefaults.size() > column ? columnDefaults.get(column) : null;
		if (cell == null) {
			cell = new Cell();
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

	/**
	 * Removes all widgets and cells from the table and resets the cell and column defaults.
	 */
	public void clear () {
		for (int i = cells.size() - 1; i >= 0; i--)
			removeChild(getTable(), (T)cells.get(i).widget);
		cells.clear();
		columnDefaults.clear();
		nameToWidget.clear();
		widgetToCell.clear();
		cellDefaults.set(Cell.defaults());
		debug = null;
		rows = 0;
		columns = 0;
		rowDefaults = null;
		padTop = null;
		padLeft = null;
		padBottom = null;
		padRight = null;
		align = CENTER;
		if (debug != null) clearDebugRectangles();
	}

	/**
	 * Returns the widget with the specified name, anywhere in the table hierarchy.
	 */
	public T getWidget (String name) {
		return nameToWidget.get(name.toLowerCase());
	}

	/**
	 * Returns all widgets, anywhere in the table hierarchy.
	 */
	public List<T> getWidgets () {
		return new ArrayList(nameToWidget.values());
	}

	/**
	 * Returns all widgets with the specified name prefix, anywhere in the table hierarchy.
	 */
	public List<T> getWidgets (String namePrefix) {
		ArrayList<T> widgets = new ArrayList();
		for (Entry<String, T> entry : nameToWidget.entrySet())
			if (entry.getKey().startsWith(namePrefix)) widgets.add(entry.getValue());
		return widgets;
	}

	/**
	 * Returns the cell for the specified widget, anywhere in the table hierarchy.
	 */
	public Cell getCell (T widget) {
		return widgetToCell.get(widget);
	}

	/**
	 * Returns the cell with the specified name, anywhere in the table hierarchy.
	 */
	public Cell getCell (String name) {
		return getCell(getWidget(name));
	}

	/**
	 * Returns all cells with the specified name prefix, anywhere in the table hierarchy.
	 */
	public List<Cell> getCells (String namePrefix) {
		ArrayList<Cell> cells = new ArrayList();
		for (Cell cell : widgetToCell.values())
			if (cell.name.startsWith(namePrefix)) cells.add(cell);
		return cells;
	}

	/**
	 * Returns all cells, anywhere in the table hierarchy.
	 */
	public List<Cell> getCells () {
		return cells;
	}

	public void setWidget (Cell cell, T widget) {
		if (cell.widget != null) {
			removeChild(getTable(), (T)cell.widget);
			widgetToCell.remove((T)cell.widget);
		}
		cell.widget = widget;
		nameToWidget.put(cell.name, widget);
		widgetToCell.put(widget, cell);
		addChild(getTable(), widget, null);
	}

	/**
	 * Sets the widget in the cell with the specified name.
	 */
	public void setWidget (String name, T widget) {
		setWidget(getCell(name), widget);
	}

	/**
	 * Performs the actual layout.
	 */
	public void layout () {
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
			c.padLeftTemp = c.column == 0 ? width(c.padLeft) : width(c.padLeft) + Math.max(0, width(c.spaceLeft) - spaceRightLast);
			c.padTopTemp = c.cellAboveIndex == -1 ? height(c.padTop) : height(c.padTop)
				+ Math.max(0, height(c.spaceTop) - height(cells.get(c.cellAboveIndex).spaceBottom));
			int spaceRight = width(c.spaceRight);
			c.padRightTemp = c.column + c.colspan == columns ? width(c.padRight) : width(c.padRight) + spaceRight;
			c.padBottomTemp = c.row == rows - 1 ? height(c.padBottom) : height(c.padBottom) + height(c.spaceBottom);
			spaceRightLast = spaceRight;

			int prefWidth = getWidth((T)c.widget, c.prefWidth);
			int prefHeight = getHeight((T)c.widget, c.prefHeight);
			int minWidth = getWidth((T)c.widget, c.minWidth);
			int minHeight = getHeight((T)c.widget, c.minHeight);
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
		int hpadding = width(padLeft) + width(padRight);
		int vpadding = height(padTop) + height(padBottom);
		int width = width(this.width) - hpadding;
		int height = height(this.height) - vpadding;
		tableMinWidth = Math.max(tableMinWidth + hpadding, width);
		tableMinHeight = Math.max(tableMinHeight + vpadding, height);
		tablePrefWidth = Math.max(tablePrefWidth + hpadding, tableMinWidth);
		tablePrefHeight = Math.max(tablePrefHeight + vpadding, tableMinHeight);

		int[] columnMaxWidth;
		int tableLayoutWidth = this.tableLayoutWidth;
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
		int tableLayoutHeight = this.tableLayoutHeight;
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

				if (c.colspan == 1 && c.expandWidth != 0 && expandWidth[column] == 0) {
					expandWidth[column] = c.expandWidth / (float)c.colspan;
					totalExpandWidth += c.expandWidth / (float)c.colspan;
				}
			}
			spannedCellMaxWidth -= c.padLeftTemp + c.padRightTemp;
			if (c.expandHeight != 0 && expandHeight[c.row] == 0) {
				expandHeight[c.row] = c.expandHeight;
				totalExpandHeight += c.expandHeight;
			}

			int prefWidth = getWidth((T)c.widget, c.prefWidth);
			int prefHeight = getHeight((T)c.widget, c.prefHeight);
			int minWidth = getWidth((T)c.widget, c.minWidth);
			int minHeight = getHeight((T)c.widget, c.minHeight);
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
			if (c.uniformWidth != null) uniformMaxWidth = Math.max(uniformMaxWidth, columnWidth[c.column]);
			if (c.uniformHeight != null) uniformMaxHeight = Math.max(uniformMaxHeight, rowHeight[c.row]);
		}
		if (uniformMaxWidth > 0 || uniformMaxHeight > 0) {
			outer:
			for (int i = 0, n = cells.size(); i < n; i++) {
				Cell c = cells.get(i);
				if (c.ignore) continue;
				if (uniformMaxWidth > 0 && c.uniformWidth != null) {
					int diff = uniformMaxWidth - columnWidth[c.column];
					if (diff > 0) {
						columnWidth[c.column] = uniformMaxWidth;
						tableMinWidth += diff;
						tablePrefWidth += diff;
					}
				}
				if (uniformMaxHeight > 0 && c.uniformHeight != null) {
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

			int minWidth = getWidth((T)c.widget, c.minWidth);

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
		int x = tableLayoutX + width(padLeft);
		if ((align & RIGHT) != 0)
			x += tableLayoutWidth - tableWidth;
		else if ((align & LEFT) == 0) // Center
			x += (tableLayoutWidth - tableWidth) / 2;

		int y = tableLayoutY + height(padTop);
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

			if (c.fillWidth > 0) {
				c.widgetWidth = (int)(spannedCellWidth * c.fillWidth);
				int maxWidth = getWidth((T)c.widget, c.maxWidth);
				if (maxWidth > 0) c.widgetWidth = Math.min(c.widgetWidth, maxWidth);
			}
			if (c.fillHeight > 0) {
				c.widgetHeight = (int)(rowHeight[c.row] * c.fillHeight) - c.padTopTemp - c.padBottomTemp;
				int maxHeight = getHeight((T)c.widget, c.maxHeight);
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
		if (debug == null) return;
		clearDebugRectangles();
		currentX = x;
		currentY = y;
		if (debug.contains(DEBUG_TABLE) || debug.contains(DEBUG_ALL)) {
			addDebugRectangle(DEBUG_TABLE, tableLayoutX, tableLayoutY, tableLayoutWidth, tableLayoutHeight);
			addDebugRectangle(DEBUG_TABLE, x, y, tableWidth - hpadding, tableHeight - vpadding);
		}
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Widget bounds.
			if (debug.contains(DEBUG_WIDGET) || debug.contains(DEBUG_ALL))
				addDebugRectangle(DEBUG_WIDGET, c.widgetX, c.widgetY, c.widgetWidth, c.widgetHeight);

			// Cell bounds.
			int spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.padLeftTemp + c.padRightTemp;
			currentX += c.padLeftTemp;
			if (debug.contains(DEBUG_CELL) || debug.contains(DEBUG_ALL))
				addDebugRectangle(DEBUG_CELL, currentX, currentY + c.padTopTemp, spannedCellWidth, rowHeight[c.row] - c.padTopTemp
					- c.padBottomTemp);

			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.padRightTemp;
		}
	}

	/**
	 * Returns the value unless it is one of the special integers representing min, pref, or max height.
	 */
	int getWidth (T widget, String value) {
		if (value.equals(MIN)) return getMinWidth(widget);
		if (value.equals(PREF)) return getPrefWidth(widget);
		if (value.equals(MAX)) return getMaxWidth(widget);
		return width(value);
	}

	/**
	 * Returns the value unless it is one of the special integers representing min, pref, or max height.
	 */
	int getHeight (T widget, String value) {
		if (value.equals(MIN)) return getMinHeight(widget);
		if (value.equals(PREF)) return getPrefHeight(widget);
		if (value.equals(MAX)) return getMaxHeight(widget);
		return height(value);
	}

	/**
	 * Validates the specified size is properly formatted. A size is valid if it can be parsed by {@link #width(String)} and
	 * {@link #height(String)}. All sizes that are stored as strings pass through this method, providing a hook to validate sizes
	 * at parse time rather than layout time. The default implementation just returns the specified string. If the specified String
	 * is invalid, an exception should be thrown.
	 */
	protected String validateSize (String size) {
		return size;
	}

	/**
	 * Creates a new widget from the specified class name. This method can be overriden to create widgets using shortcut names (eg,
	 * "button"). The default implementation creates an instance of the class and calls {@link #wrap(Object)}.
	 * @see #addClassPrefix(String)
	 * @throws RuntimeException if the class could be found or otherwise failed to be instantiated.
	 */
	public T newWidget (String className) {
		try {
			return wrap(Class.forName(className).newInstance());
		} catch (Exception ex) {
			for (int i = 0, n = classPrefixes.size(); i < n; i++) {
				String prefix = classPrefixes.get(i);
				try {
					return newInstance(prefix + className);
				} catch (Exception ignored) {
				}
			}
			throw new RuntimeException("Error creating instance of class: " + className, ex);
		}
	}

	/**
	 * Returns an instance of the specified class. The default uses Class.forName(className).newInstance();
	 * @throws Exception if the class cannot be found or instantiated.
	 */
	protected T newInstance (String className) throws Exception {
		return (T)Class.forName(className).newInstance();
	}

	/**
	 * Wraps the specified object in a widget. The default implementation handles the object being a TableLayout or widget. If the
	 * object is null, a placeholder widget to use for empty cells must be returned. If the object is a string, a label widget must
	 * be returned. Otherwise, the object can be any other class that was added to the table (eg, a Swing LayoutManager would be
	 * wrapped in a JPanel).
	 * @throws RuntimeException if the object could not be wrapped.
	 */
	public T wrap (Object object) {
		if (object instanceof BaseTableLayout) return (T)((BaseTableLayout)object).getTable();
		try {
			return (T)object;
		} catch (ClassCastException ex) {
			throw new RuntimeException("Unknown object type: " + object.getClass());
		}
	}

	/**
	 * Sets a property on the widget. This is called for widget properties specified in the table description. The default
	 * implementation attempts to find a method, bean setter method, or field that will accept the specified values.
	 * @throws RuntimeException if the property could not be set.
	 */
	public void setProperty (T object, String name, List<String> values) {
		try {
			invokeMethod(object, name, values);
		} catch (NoSuchMethodException ex1) {
			try {
				invokeMethod(object, "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1), values);
			} catch (NoSuchMethodException ex2) {
				try {
					Field field = object.getClass().getField(name);
					Object value = convertType(object, values.get(0), field.getType(), name);
					if (value != null) field.set(object, value);
				} catch (Exception ex3) {
					throw new RuntimeException("No method, bean property, or field found: " + name + "\nClass: " + object.getClass()
						+ "\nValues: " + values);
				}
			}
		}
	}

	/**
	 * Sets a property on the table. This is called for table properties specified in the table description.
	 * @throws RuntimeException if the property could not be set.
	 */
	public void setTableProperty (String name, List<String> values) {
		name = name.toLowerCase();
		for (int i = 0, n = values.size(); i < n; i++)
			values.set(i, values.get(i).toLowerCase());
		try {
			String value;
			if (name.equals("size")) {
				switch (values.size()) {
				case 1:
					width = height = validateSize(values.get(0));
					break;
				case 2:
					width = validateSize(values.get(0));
					height = validateSize(values.get(1));
					break;
				}

			} else if (name.equals("width") || name.equals("w")) {
				width = validateSize(values.get(0));

			} else if (name.equals("height") || name.equals("h")) {
				height = validateSize(values.get(0));

			} else if (name.equals("padding") || name.equals("pad")) {
				switch (values.size()) {
				case 4:
					value = values.get(3);
					if (value.length() > 0) padRight = validateSize(value);
				case 3:
					value = values.get(2);
					if (value.length() > 0) padBottom = validateSize(value);
				case 2:
					value = values.get(0);
					if (value.length() > 0) padTop = validateSize(value);
					value = values.get(1);
					if (value.length() > 0) padLeft = validateSize(value);
					break;
				case 1:
					padTop = padLeft = padBottom = padRight = validateSize(values.get(0));
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.startsWith("padding") || name.startsWith("pad")) {
				name = name.replace("padding", "").replace("pad", "");
				if (name.equals("top") || name.equals("t"))
					padTop = validateSize(values.get(0));
				else if (name.equals("left") || name.equals("l"))
					padLeft = validateSize(values.get(0));
				else if (name.equals("bottom") || name.equals("b"))
					padBottom = validateSize(values.get(0));
				else if (name.equals("right") || name.equals("r"))
					padRight = validateSize(values.get(0));
				else
					throw new IllegalArgumentException("Unknown property.");

			} else if (name.equals("align")) {
				align = 0;
				for (int i = 0, n = values.size(); i < n; i++) {
					value = values.get(i);
					if (value.equals("center"))
						align |= CENTER;
					else if (value.equals("left"))
						align |= LEFT;
					else if (value.equals("right"))
						align |= RIGHT;
					else if (value.equals("top"))
						align |= TOP;
					else if (value.equals("bottom"))
						align |= BOTTOM;
					else
						throw new IllegalArgumentException("Invalid value: " + value);
				}

			} else if (name.equals("debug")) {
				debug = "";
				if (values.size() == 0) debug = DEBUG_ALL;
				for (int i = 0, n = values.size(); i < n; i++)
					debug += values.get(i) + ",";
				if (debug.equals("true,")) debug = DEBUG_ALL;

			} else
				throw new IllegalArgumentException("Unknown table property: " + name);
		} catch (Exception ex) {
			throw new RuntimeException("Error setting table property: " + name, ex);
		}
	}

	/**
	 * Sets a property on the cell. This is called for cell properties specified in the table description.
	 * @throws RuntimeException if the property could not be set.
	 */
	public void setCellProperty (Cell c, String name, List<String> values) {
		name = name.toLowerCase();
		for (int i = 0, n = values.size(); i < n; i++)
			values.set(i, values.get(i).toLowerCase());
		try {
			String value;
			if (name.equals("expand")) {
				switch (values.size()) {
				case 0:
					c.expandWidth = c.expandHeight = 1;
					break;
				case 1:
					value = values.get(0);
					if (value.equals("x"))
						c.expandWidth = 1;
					else if (value.equals("y")) //
						c.expandHeight = 1;
					else
						c.expandWidth = c.expandHeight = Integer.parseInt(value);
					break;
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.expandWidth = Integer.parseInt(value);
					value = values.get(1);
					if (value.length() > 0) c.expandHeight = Integer.parseInt(value);
					break;
				}

			} else if (name.equals("fill")) {
				switch (values.size()) {
				case 0:
					c.fillWidth = c.fillHeight = 1f;
					break;
				case 1:
					value = values.get(0);
					if (value.equals("x"))
						c.fillWidth = 1f;
					else if (value.equals("y")) //
						c.fillHeight = 1f;
					else
						c.fillWidth = c.fillHeight = Integer.parseInt(value) / 100f;
					break;
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.fillWidth = Integer.parseInt(value) / 100f;
					value = values.get(1);
					if (value.length() > 0) c.fillHeight = Integer.parseInt(value) / 100f;
					break;
				}

			} else if (name.equals("size")) {
				switch (values.size()) {
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.minWidth = c.prefWidth = validateSize(value);
					value = values.get(1);
					if (value.length() > 0) c.minHeight = c.prefHeight = validateSize(value);
					break;
				case 1:
					value = values.get(0);
					if (value.length() > 0) c.minWidth = c.minHeight = c.prefWidth = c.prefHeight = validateSize(value);
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("width") || name.equals("w")) {
				switch (values.size()) {
				case 3:
					value = values.get(2);
					if (value.length() > 0) c.maxWidth = validateSize(value);
				case 2:
					value = values.get(1);
					if (value.length() > 0) c.prefWidth = validateSize(value);
				case 1:
					value = values.get(0);
					if (value.length() > 0) c.minWidth = validateSize(value);
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("height") || name.equals("h")) {
				switch (values.size()) {
				case 3:
					value = values.get(2);
					if (value.length() > 0) c.maxHeight = validateSize(value);
				case 2:
					value = values.get(1);
					if (value.length() > 0) c.prefHeight = validateSize(value);
				case 1:
					value = values.get(0);
					if (value.length() > 0) c.minHeight = validateSize(value);
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("spacing") || name.equals("space")) {
				switch (values.size()) {
				case 4:
					value = values.get(3);
					if (value.length() > 0) c.spaceRight = validateSize(value);
				case 3:
					value = values.get(2);
					if (value.length() > 0) c.spaceBottom = validateSize(value);
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.spaceTop = validateSize(value);
					value = values.get(1);
					if (value.length() > 0) c.spaceLeft = validateSize(value);
					break;
				case 1:
					c.spaceTop = c.spaceLeft = c.spaceBottom = c.spaceRight = validateSize(values.get(0));
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("padding") || name.equals("pad")) {
				switch (values.size()) {
				case 4:
					value = values.get(3);
					if (value.length() > 0) c.padRight = validateSize(value);
				case 3:
					value = values.get(2);
					if (value.length() > 0) c.padBottom = validateSize(value);
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.padTop = validateSize(value);
					value = values.get(1);
					if (value.length() > 0) c.padLeft = validateSize(value);
					break;
				case 1:
					c.padTop = c.padLeft = c.padBottom = c.padRight = validateSize(values.get(0));
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.startsWith("padding") || name.startsWith("pad")) {
				name = name.replace("padding", "").replace("pad", "");
				if (name.equals("top") || name.equals("t"))
					c.padTop = validateSize(values.get(0));
				else if (name.equals("left") || name.equals("l"))
					c.padLeft = validateSize(values.get(0));
				else if (name.equals("bottom") || name.equals("b"))
					c.padBottom = validateSize(values.get(0));
				else if (name.equals("right") || name.equals("r")) //
					c.padRight = validateSize(values.get(0));
				else
					throw new IllegalArgumentException("Unknown property.");

			} else if (name.startsWith("spacing") || name.startsWith("space")) {
				name = name.replace("spacing", "").replace("space", "");
				if (name.equals("top") || name.equals("t"))
					c.spaceTop = validateSize(values.get(0));
				else if (name.equals("left") || name.equals("l"))
					c.spaceLeft = validateSize(values.get(0));
				else if (name.equals("bottom") || name.equals("b"))
					c.spaceBottom = validateSize(values.get(0));
				else if (name.equals("right") || name.equals("r")) //
					c.spaceRight = validateSize(values.get(0));
				else
					throw new IllegalArgumentException("Unknown property.");

			} else if (name.equals("align")) {
				c.align = 0;
				for (int i = 0, n = values.size(); i < n; i++) {
					value = values.get(i);
					if (value.equals("center"))
						c.align |= CENTER;
					else if (value.equals("left"))
						c.align |= LEFT;
					else if (value.equals("right"))
						c.align |= RIGHT;
					else if (value.equals("top"))
						c.align |= TOP;
					else if (value.equals("bottom"))
						c.align |= BOTTOM;
					else
						throw new IllegalArgumentException("Invalid value: " + value);
				}

			} else if (name.equals("ignore")) {
				c.ignore = values.size() == 0 ? true : Boolean.valueOf(values.get(0));

			} else if (name.equals("colspan")) {
				c.colspan = Integer.parseInt(values.get(0));

			} else if (name.equals("uniform")) {
				if (values.size() == 0) c.uniformWidth = c.uniformHeight = true;
				for (int i = 0, n = values.size(); i < n; i++) {
					value = values.get(i);
					if (value.equals("x"))
						c.uniformWidth = true;
					else if (value.equals("y"))
						c.uniformHeight = true;
					else if (value.equals("false"))
						c.uniformHeight = c.uniformHeight = null;
					else
						throw new IllegalArgumentException("Invalid value: " + value);
				}

			} else
				throw new IllegalArgumentException("Unknown cell property.");
		} catch (Exception ex) {
			throw new RuntimeException("Error setting cell property: " + name, ex);
		}
	}

	/**
	 * Interprets the specified value as a width. This can be used to scale all sizes applied to a cell, implement size units (eg,
	 * 23px or 23em), etc. The default implementation converts to an int and calls {@link #width(float)}. Zero is used for null and
	 * empty string. If the suffix is "%", the value is converted to an int, divided by 100, and multiplied by
	 * {@link #tableLayoutWidth}. If the suffix is "px", the value is converted to int without the suffix and returned unscaled.
	 * @param value May be null.
	 */
	public int width (String value) {
		int length;
		if (value == null || (length = value.length()) == 0) return width(0);
		if (value.charAt(length - 1) == '%' && length > 1)
			return (int)(tableLayoutWidth * Integer.parseInt(value.substring(0, length - 1)) / 100f);
		if (value.endsWith("px")) return Integer.parseInt(value.substring(0, value.length() - 2));
		return width(value == null ? 0 : Integer.parseInt(value));
	}

	/**
	 * Interprets the specified value as a size. This can be used to scale all sizes applied to a cell. The default implementation
	 * just casts to int.
	 */
	public int width (float value) {
		return (int)value;
	}

	/**
	 * Interprets the specified value as a height. This can be used to scale all sizes applied to a cell, implement size units (eg,
	 * 23px or 23em), etc. The default implementation converts to an int and calls {@link #height(float)}. Zero is used for null
	 * and empty string. If the suffix is "%", the value is converted to an int, divided by 100, and multiplied by
	 * {@link #tableLayoutHeight}. If the suffix is "px", the value is converted to int without the suffix and returned unscaled.
	 * @param value May be null.
	 */
	public int height (String value) {
		int length;
		if (value == null || (length = value.length()) == 0) return height(0);
		if (value.charAt(length - 1) == '%' && length > 1)
			return (int)(tableLayoutHeight * Integer.parseInt(value.substring(0, length - 1)) / 100f);
		if (value.endsWith("px")) return Integer.parseInt(value.substring(0, value.length() - 2));
		return height(value == null ? 0 : Integer.parseInt(value));
	}

	/**
	 * Interprets the specified value as a size. This can be used to scale all sizes applied to a cell. The default implementation
	 * just casts to int.
	 */
	public int height (float value) {
		return (int)value;
	}

	private void invokeMethod (Object object, String name, List<String> values) throws NoSuchMethodException {
		Object[] params = values.toArray();
		// Prefer methods with string parameters.
		Class[] stringParamTypes = new Class[params.length];
		Method method = null;
		try {
			for (int i = 0, n = params.length; i < n; i++)
				stringParamTypes[i] = String.class;
			method = object.getClass().getMethod(name, stringParamTypes);
		} catch (NoSuchMethodException ignored) {
			try {
				for (int i = 0, n = params.length; i < n; i++)
					stringParamTypes[i] = CharSequence.class;
				method = object.getClass().getMethod(name, stringParamTypes);
			} catch (NoSuchMethodException ignored2) {
			}
		}
		if (method != null) {
			try {
				method.invoke(object, params);
			} catch (Exception ex) {
				throw new RuntimeException("Error invoking method: " + name, ex);
			}
			return;
		}
		// Try to convert the strings to match a method.
		Method[] methods = object.getClass().getMethods();
		outer:
		for (int i = 0, n = methods.length; i < n; i++) {
			method = methods[i];
			if (!method.getName().equalsIgnoreCase(name)) continue;
			Class[] paramTypes = method.getParameterTypes();
			if (paramTypes.length != values.size()) continue;
			params = values.toArray();
			for (int ii = 0, nn = paramTypes.length; ii < nn; ii++) {
				Object value = convertType(object, (String)params[ii], paramTypes[ii], name);
				if (value == null) continue outer;
				params[ii] = value;
			}
			try {
				method.invoke(object, params);
				return;
			} catch (Exception ex) {
				throw new RuntimeException("Error invoking method: " + name, ex);
			}
		}
		throw new NoSuchMethodException();
	}

	/**
	 * Attempts to convert a string value to a non-string type to match a field or method on the specified parentObject. The
	 * default implementation tries all primitive type wrappers and looks for a static field with the name on both the specified
	 * type and the parentObject's type.
	 * @return the converted type, or null if it could not be converted.
	 */
	protected Object convertType (Object parentObject, String value, Class type, String memberName) {
		if (type == String.class || type == CharSequence.class) return value;
		try {
			if (type == int.class || type == Integer.class) return Integer.valueOf(value);
			if (type == float.class || type == Float.class) return Float.valueOf(value);
			if (type == long.class || type == Long.class) return Long.valueOf(value);
			if (type == double.class || type == Double.class) return Double.valueOf(value);
		} catch (NumberFormatException ignored) {
		}
		if (type == boolean.class || type == Boolean.class) return Boolean.valueOf(value);
		if (type == char.class || type == Character.class) return value.charAt(0);
		if (type == short.class || type == Short.class) return Short.valueOf(value);
		if (type == byte.class || type == Byte.class) return Byte.valueOf(value);
		// Look for a static field.
		try {
			Field field = getField(type, value);
			if (field != null && type == field.getType()) return field.get(null);
		} catch (Exception ignored) {
		}
		try {
			Field field = getField(parentObject.getClass(), value);
			if (field != null && type == field.getType()) return field.get(null);
		} catch (Exception ignored) {
		}
		return null;
	}

	static private Field getField (Class type, String name) {
		try {
			Field field = type.getField(name);
			if (field != null) return field;
		} catch (Exception ignored) {
		}
		while (type != null && type != Object.class) {
			Field[] fields = type.getDeclaredFields();
			for (int i = 0, n = fields.length; i < n; i++)
				if (fields[i].getName().equalsIgnoreCase(name)) return fields[i];
			type = type.getSuperclass();
		}
		return null;
	}

	/**
	 * Used by {@link #newWidget(String)} to resolve class names.
	 */
	static public void addClassPrefix (String prefix) {
		classPrefixes.add(prefix);
	}
}

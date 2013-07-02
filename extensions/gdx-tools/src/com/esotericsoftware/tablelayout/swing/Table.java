
package com.esotericsoftware.tablelayout.swing;

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Toolkit;
import com.esotericsoftware.tablelayout.Value;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class Table extends JComponent {
	static {
		Toolkit.instance = new SwingToolkit();
	}

	private final TableLayout layout;

	public Table () {
		this(new TableLayout());
	}

	public Table (final TableLayout layout) {
		this.layout = layout;
		layout.setTable(this);

		setLayout(new LayoutManager() {
			private Dimension minSize = new Dimension(), prefSize = new Dimension();

			public Dimension preferredLayoutSize (Container parent) {
				layout.layout(); // BOZO - Cache layout?
				prefSize.width = (int)layout.getMinWidth();
				prefSize.height = (int)layout.getMinHeight();
				return prefSize;
			}

			public Dimension minimumLayoutSize (Container parent) {
				layout.layout(); // BOZO - Cache layout?
				minSize.width = (int)layout.getMinWidth();
				minSize.height = (int)layout.getMinHeight();
				return minSize;
			}

			public void layoutContainer (Container ignored) {
				layout.layout();
			}

			public void addLayoutComponent (String name, Component comp) {
			}

			public void removeLayoutComponent (Component comp) {
			}
		});
	}

	/** Removes all Components and cells from the table. */
	public void clear () {
		layout.clear();
		invalidate();
	}

	public Cell addCell (String text) {
		return addCell(new JLabel(text));
	}

	/** Adds a cell with a placeholder Component. */
	public Cell addCell () {
		return addCell((Component)null);
	}

	/** Adds a new cell to the table with the specified Component.
	 * @see TableLayout#add(Component)
	 * @param Component May be null to add a cell without an Component. */
	public Cell addCell (Component Component) {
		return layout.add(Component);
	}

	/** Adds a new cell to the table with the specified Components in a {@link Stack}.
	 * @param components May be null to add a cell without an Component. */
	public Cell stack (Component... components) {
		Stack stack = new Stack();
		for (int i = 0, n = components.length; i < n; i++)
			stack.add(components[i]);
		return addCell(stack);
	}

	/** Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row.
	 * @see TableLayout#row() */
	public Cell row () {
		return layout.row();
	}

	/** Gets the cell values that will be used as the defaults for all cells in the specified column.
	 * @see TableLayout#columnDefaults(int) */
	public Cell columnDefaults (int column) {
		return layout.columnDefaults(column);
	}

	/** The cell values that will be used as the defaults for all cells.
	 * @see TableLayout#defaults() */
	public Cell defaults () {
		return layout.defaults();
	}

	/** Positions and sizes children of the Component being laid out using the cell associated with each child.
	 * @see TableLayout#layout() */
	public void layout () {
		layout.layout();
	}

	/** Removes all Components and cells from the table (same as {@link #clear()}) and additionally resets all table properties and
	 * cell, column, and row defaults.
	 * @see TableLayout#reset() */
	public void reset () {
		layout.reset();
	}

	/** Returns the cell for the specified Component, anywhere in the table hierarchy.
	 * @see TableLayout#getCell(Component) */
	public Cell getCell (Component Component) {
		return layout.getCell(Component);
	}

	/** Returns the cells for this table.
	 * @see TableLayout#getCells() */
	public List<Cell> getCells () {
		return layout.getCells();
	}

	/** Padding around the table.
	 * @see TableLayout#pad(Value) */
	public Table pad (Value pad) {
		layout.pad(pad);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(Value, Value, Value, Value) */
	public Table pad (Value top, Value left, Value bottom, Value right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top of the table.
	 * @see TableLayout#padTop(Value) */
	public Table padTop (Value padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left of the table.
	 * @see TableLayout#padLeft(Value) */
	public Table padLeft (Value padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom of the table.
	 * @see TableLayout#padBottom(Value) */
	public Table padBottom (Value padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right of the table.
	 * @see TableLayout#padRight(Value) */
	public Table padRight (Value padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(float) */
	public Table pad (int pad) {
		layout.pad(pad);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(float, float, float, float) */
	public Table pad (int top, int left, int bottom, int right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top of the table.
	 * @see TableLayout#padTop(float) */
	public Table padTop (int padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left of the table.
	 * @see TableLayout#padLeft(float) */
	public Table padLeft (int padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom of the table.
	 * @see TableLayout#padBottom(float) */
	public Table padBottom (int padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right of the table.
	 * @see TableLayout#padRight(float) */
	public Table padRight (int padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Alignment of the table within the Component being laid out. Set to {@link BaseTableLayout#CENTER},
	 * {@link BaseTableLayout#TOP}, {@link BaseTableLayout#BOTTOM} , {@link BaseTableLayout#LEFT} , {@link BaseTableLayout#RIGHT},
	 * or any combination of those.
	 * @see TableLayout#align(int) */
	public Table align (int align) {
		layout.align(align);
		return this;
	}

	/** Sets the alignment of the table within the Component being laid out to {@link BaseTableLayout#CENTER}.
	 * @see TableLayout#center() */
	public Table center () {
		layout.center();
		return this;
	}

	/** Sets the alignment of the table within the Component being laid out to {@link BaseTableLayout#TOP}.
	 * @see TableLayout#top() */
	public Table top () {
		layout.top();
		return this;
	}

	/** Sets the alignment of the table within the Component being laid out to {@link BaseTableLayout#LEFT}.
	 * @see TableLayout#left() */
	public Table left () {
		layout.left();
		return this;
	}

	/** Sets the alignment of the table within the Component being laid out to {@link BaseTableLayout#BOTTOM}.
	 * @see TableLayout#bottom() */
	public Table bottom () {
		layout.bottom();
		return this;
	}

	/** Sets the alignment of the table within the Component being laid out to {@link BaseTableLayout#RIGHT}.
	 * @see TableLayout#right() */
	public Table right () {
		layout.right();
		return this;
	}

	/** Turns on all debug lines.
	 * @see TableLayout#debug() */
	public Table debug () {
		layout.debug();
		return this;
	}

	/** Turns on debug lines.
	 * @see TableLayout#debug() */
	public Table debug (Debug debug) {
		layout.debug(debug);
		return this;
	}

	public Debug getDebug () {
		return layout.getDebug();
	}

	public Value getPadTop () {
		return layout.getPadTopValue();
	}

	public Value getPadLeft () {
		return layout.getPadLeftValue();
	}

	public Value getPadBottom () {
		return layout.getPadBottomValue();
	}

	public Value getPadRight () {
		return layout.getPadRightValue();
	}

	public int getAlign () {
		return layout.getAlign();
	}

	public TableLayout getTableLayout () {
		return layout;
	}

	public void invalidate () {
		super.invalidate();
		layout.invalidate();
	}
}

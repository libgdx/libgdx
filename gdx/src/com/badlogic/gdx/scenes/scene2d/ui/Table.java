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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Value.Fixed;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/** A group that sizes and positions children using table constraints. By default, {@link #getTouchable()} is
 * {@link Touchable#childrenOnly}.
 * <p>
 * The preferred and minimum sizes are that of the children when laid out in columns and rows.
 * @author Nathan Sweet */
public class Table extends WidgetGroup {
	static Pool<Cell> cellPool = new Pool() {
		protected Cell newObject () {
			return new Cell();
		}
	};
	static private float[] columnWeightedWidth, rowWeightedHeight;

	private int columns, rows;

	private final Array<Cell> cells = new Array(4);
	private final Cell cellDefaults;
	private final Array<Cell> columnDefaults = new Array(2);
	private Cell rowDefaults;

	private boolean sizeInvalid = true;
	private float[] columnMinWidth, rowMinHeight;
	private float[] columnPrefWidth, rowPrefHeight;
	private float tableMinWidth, tableMinHeight;
	private float tablePrefWidth, tablePrefHeight;
	private float[] columnWidth, rowHeight;
	private float[] expandWidth, expandHeight;

	Value padTop = Value.zero, padLeft = Value.zero, padBottom = Value.zero, padRight = Value.zero;
	int align = Align.center;

	Debug debug = Debug.none;
	Array<DebugRect> debugRects;

	private Drawable background;
	private boolean clip;
	private Skin skin;
	boolean round = true;

	public Table () {
		this(null);
	}

	/** Creates a table with a skin, which enables the {@link #add(String)} and {@link #add(String, String)} methods to be used. */
	public Table (Skin skin) {
		this.skin = skin;

		cellDefaults = obtainCell();
		cellDefaults.defaults();

		setTransform(false);
		setTouchable(Touchable.childrenOnly);
	}

	private Cell obtainCell () {
		Cell cell = cellPool.obtain();
		cell.setLayout(this);
		return cell;
	}

	public void draw (Batch batch, float parentAlpha) {
		validate();
		if (isTransform()) {
			applyTransform(batch, computeTransform());
			drawBackground(batch, parentAlpha, 0, 0);
			if (clip) {
				batch.flush();
				float x = 0, y = 0, width = getWidth(), height = getHeight();
				if (background != null) {
					x = padLeft.get(this);
					y = padBottom.get(this);
					width -= x + padRight.get(this);
					height -= y + padTop.get(this);
				}
				boolean draw = clipBegin(x, y, width, height);
				if (draw) {
					drawChildren(batch, parentAlpha);
					clipEnd();
				}
			} else
				drawChildren(batch, parentAlpha);
			resetTransform(batch);
		} else {
			drawBackground(batch, parentAlpha, getX(), getY());
			super.draw(batch, parentAlpha);
		}
	}

	/** Called to draw the background, before clipping is applied (if enabled). Default implementation draws the background
	 * drawable. */
	protected void drawBackground (Batch batch, float parentAlpha, float x, float y) {
		if (background == null) return;
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		background.draw(batch, x, y, getWidth(), getHeight());
	}

	/** Sets the background drawable from the skin and adjusts the table's padding to match the background. This may only be called
	 * if {@link Table#Table(Skin)} or {@link #setSkin(Skin)} was used.
	 * @see #setBackground(Drawable, boolean) */
	public void setBackground (String drawableName) {
		setBackground(skin.getDrawable(drawableName), true);
	}

	/** Sets the background drawable and adjusts the table's padding to match the background.
	 * @see #setBackground(Drawable, boolean) */
	public void setBackground (Drawable background) {
		setBackground(background, true);
	}

	/** Sets the background drawable and, if adjustPadding is true, sets the table's padding to {@link Drawable#getBottomHeight()} ,
	 * {@link Drawable#getTopHeight()}, {@link Drawable#getLeftWidth()}, and {@link Drawable#getRightWidth()}.
	 * @param background If null, the background will be cleared and padding removed. */
	public void setBackground (Drawable background, boolean adjustPadding) {
		if (this.background == background) return;
		this.background = background;
		if (adjustPadding) {
			if (background == null)
				pad(null);
			else
				pad(background.getTopHeight(), background.getLeftWidth(), background.getBottomHeight(), background.getRightWidth());
			invalidate();
		}
	}

	/** @see #setBackground(Drawable) */
	public Table background (Drawable background) {
		setBackground(background);
		return this;
	}

	/** @see #setBackground(String) */
	public Table background (String drawableName) {
		setBackground(drawableName);
		return this;
	}

	public Drawable getBackground () {
		return background;
	}

	public Actor hit (float x, float y, boolean touchable) {
		if (clip) {
			if (touchable && getTouchable() == Touchable.disabled) return null;
			if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return null;
		}
		return super.hit(x, y, touchable);
	}

	/** Causes the contents to be clipped if they exceed the table actor's bounds. Enabling clipping will set
	 * {@link #setTransform(boolean)} to true. */
	public void setClip (boolean enabled) {
		clip = enabled;
		setTransform(enabled);
		invalidate();
	}

	public boolean getClip () {
		return clip;
	}

	public void invalidate () {
		sizeInvalid = true;
		super.invalidate();
	}

	/** Adds a new cell to the table with the specified actor. */
	public Cell add (Actor actor) {
		Cell cell = obtainCell();
		cell.actor = actor;

		Array<Cell> cells = this.cells;
		int cellCount = cells.size;
		if (cellCount > 0) {
			// Set cell column and row.
			Cell lastCell = cells.peek();
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
				for (int i = cellCount - 1; i >= 0; i--) {
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
		if (cell.column < columnDefaults.size) {
			Cell columnCell = columnDefaults.get(cell.column);
			if (columnCell != null) cell.merge(columnCell);
		}
		cell.merge(rowDefaults);

		if (actor != null) addActor(actor);

		return cell;
	}

	public void add (Actor... actors) {
		for (int i = 0, n = actors.length; i < n; i++)
			add(actors[i]);
	}

	/** Adds a new cell with a label. This may only be called if {@link Table#Table(Skin)} or {@link #setSkin(Skin)} was used. */
	public Cell<Label> add (String text) {
		if (skin == null) throw new IllegalStateException("Table must have a skin set to use this method.");
		return add(new Label(text, skin));
	}

	/** Adds a new cell with a label. This may only be called if {@link Table#Table(Skin)} or {@link #setSkin(Skin)} was used. */
	public Cell<Label> add (String text, String labelStyleName) {
		if (skin == null) throw new IllegalStateException("Table must have a skin set to use this method.");
		return add(new Label(text, skin.get(labelStyleName, LabelStyle.class)));
	}

	/** Adds a new cell with a label. This may only be called if {@link Table#Table(Skin)} or {@link #setSkin(Skin)} was used. */
	public Cell<Label> add (String text, String fontName, Color color) {
		if (skin == null) throw new IllegalStateException("Table must have a skin set to use this method.");
		return add(new Label(text, new LabelStyle(skin.getFont(fontName), color)));
	}

	/** Adds a new cell with a label. This may only be called if {@link Table#Table(Skin)} or {@link #setSkin(Skin)} was used. */
	public Cell<Label> add (String text, String fontName, String colorName) {
		if (skin == null) throw new IllegalStateException("Table must have a skin set to use this method.");
		return add(new Label(text, new LabelStyle(skin.getFont(fontName), skin.getColor(colorName))));
	}

	/** Adds a cell without an actor. */
	public Cell add () {
		return add((Actor)null);
	}

	/** Adds a new cell to the table with the specified actors in a {@link Stack}.
	 * @param actors May be null to add a stack without any actors. */
	public Cell<Stack> stack (Actor... actors) {
		Stack stack = new Stack();
		if (actors != null) {
			for (int i = 0, n = actors.length; i < n; i++)
				stack.addActor(actors[i]);
		}
		return add(stack);
	}

	public boolean removeActor (Actor actor) {
		if (!super.removeActor(actor)) return false;
		Cell cell = getCell(actor);
		if (cell != null) cell.actor = null;
		return true;
	}

	/** Removes all actors and cells from the table. */
	public void clearChildren () {
		Array<Cell> cells = this.cells;
		for (int i = cells.size - 1; i >= 0; i--) {
			Cell cell = cells.get(i);
			Actor actor = cell.actor;
			if (actor != null) actor.remove();
		}
		cellPool.freeAll(cells);
		cells.clear();
		rows = 0;
		columns = 0;
		if (rowDefaults != null) cellPool.free(rowDefaults);
		rowDefaults = null;

		super.clearChildren();
	}

	/** Removes all actors and cells from the table (same as {@link #clear()}) and additionally resets all table properties and
	 * cell, column, and row defaults. */
	public void reset () {
		clear();
		padTop = Value.zero;
		padLeft = Value.zero;
		padBottom = Value.zero;
		padRight = Value.zero;
		align = Align.center;
		if (debug != Debug.none) debugRects.clear();
		debug = Debug.none;
		cellDefaults.defaults();
		for (int i = 0, n = columnDefaults.size; i < n; i++) {
			Cell columnCell = columnDefaults.get(i);
			if (columnCell != null) cellPool.free(columnCell);
		}
		columnDefaults.clear();
	}

	/** Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row. */
	public Cell row () {
		if (cells.size > 0) {
			endRow();
			invalidate();
		}
		if (rowDefaults != null) cellPool.free(rowDefaults);
		rowDefaults = obtainCell();
		rowDefaults.clear();
		return rowDefaults;
	}

	private void endRow () {
		Array<Cell> cells = this.cells;
		int rowColumns = 0;
		for (int i = cells.size - 1; i >= 0; i--) {
			Cell cell = cells.get(i);
			if (cell.endRow) break;
			rowColumns += cell.colspan;
		}
		columns = Math.max(columns, rowColumns);
		rows++;
		cells.peek().endRow = true;
	}

	/** Gets the cell values that will be used as the defaults for all cells in the specified column. Columns are indexed starting
	 * at 0. */
	public Cell columnDefaults (int column) {
		Cell cell = columnDefaults.size > column ? columnDefaults.get(column) : null;
		if (cell == null) {
			cell = obtainCell();
			cell.clear();
			if (column >= columnDefaults.size) {
				for (int i = columnDefaults.size; i < column; i++)
					columnDefaults.add(null);
				columnDefaults.add(cell);
			} else
				columnDefaults.set(column, cell);
		}
		return cell;
	}

	/** Returns the cell for the specified actor in this table, or null. */
	public <T extends Actor> Cell<T> getCell (T actor) {
		Array<Cell> cells = this.cells;
		for (int i = 0, n = cells.size; i < n; i++) {
			Cell c = cells.get(i);
			if (c.actor == actor) return c;
		}
		return null;
	}

	/** Returns the cells for this table. */
	public Array<Cell> getCells () {
		return cells;
	}

	public float getPrefWidth () {
		if (sizeInvalid) computeSize();
		float width = tablePrefWidth;
		if (background != null) return Math.max(width, background.getMinWidth());
		return width;
	}

	public float getPrefHeight () {
		if (sizeInvalid) computeSize();
		float height = tablePrefHeight;
		if (background != null) return Math.max(height, background.getMinHeight());
		return height;
	}

	public float getMinWidth () {
		if (sizeInvalid) computeSize();
		return tableMinWidth;
	}

	public float getMinHeight () {
		if (sizeInvalid) computeSize();
		return tableMinHeight;
	}

	/** The cell values that will be used as the defaults for all cells. */
	public Cell defaults () {
		return cellDefaults;
	}

	/** Sets the padTop, padLeft, padBottom, and padRight around the table to the specified value. */
	public Table pad (Value pad) {
		padTop = pad;
		padLeft = pad;
		padBottom = pad;
		padRight = pad;
		sizeInvalid = true;
		return this;
	}

	public Table pad (Value top, Value left, Value bottom, Value right) {
		padTop = top;
		padLeft = left;
		padBottom = bottom;
		padRight = right;
		sizeInvalid = true;
		return this;
	}

	/** Padding at the top edge of the table. */
	public Table padTop (Value padTop) {
		this.padTop = padTop;
		sizeInvalid = true;
		return this;
	}

	/** Padding at the left edge of the table. */
	public Table padLeft (Value padLeft) {
		this.padLeft = padLeft;
		sizeInvalid = true;
		return this;
	}

	/** Padding at the bottom edge of the table. */
	public Table padBottom (Value padBottom) {
		this.padBottom = padBottom;
		sizeInvalid = true;
		return this;
	}

	/** Padding at the right edge of the table. */
	public Table padRight (Value padRight) {
		this.padRight = padRight;
		sizeInvalid = true;
		return this;
	}

	/** Sets the padTop, padLeft, padBottom, and padRight around the table to the specified value. */
	public Table pad (float pad) {
		pad(new Fixed(pad));
		return this;
	}

	public Table pad (float top, float left, float bottom, float right) {
		padTop = new Fixed(top);
		padLeft = new Fixed(left);
		padBottom = new Fixed(bottom);
		padRight = new Fixed(right);
		sizeInvalid = true;
		return this;
	}

	/** Padding at the top edge of the table. */
	public Table padTop (float padTop) {
		this.padTop = new Fixed(padTop);
		sizeInvalid = true;
		return this;
	}

	/** Padding at the left edge of the table. */
	public Table padLeft (float padLeft) {
		this.padLeft = new Fixed(padLeft);
		sizeInvalid = true;
		return this;
	}

	/** Padding at the bottom edge of the table. */
	public Table padBottom (float padBottom) {
		this.padBottom = new Fixed(padBottom);
		sizeInvalid = true;
		return this;
	}

	/** Padding at the right edge of the table. */
	public Table padRight (float padRight) {
		this.padRight = new Fixed(padRight);
		sizeInvalid = true;
		return this;
	}

	/** Alignment of the logical table within the table actor. Set to {@link Align#center}, {@link Align#top}, {@link Align#bottom}
	 * , {@link Align#left}, {@link Align#right}, or any combination of those. */
	public Table align (int align) {
		this.align = align;
		return this;
	}

	/** Sets the alignment of the logical table within the table actor to {@link Align#center}. This clears any other alignment. */
	public Table center () {
		align = Align.center;
		return this;
	}

	/** Adds {@link Align#top} and clears {@link Align#bottom} for the alignment of the logical table within the table actor. */
	public Table top () {
		align |= Align.top;
		align &= ~Align.bottom;
		return this;
	}

	/** Adds {@link Align#left} and clears {@link Align#right} for the alignment of the logical table within the table actor. */
	public Table left () {
		align |= Align.left;
		align &= ~Align.right;
		return this;
	}

	/** Adds {@link Align#bottom} and clears {@link Align#top} for the alignment of the logical table within the table actor. */
	public Table bottom () {
		align |= Align.bottom;
		align &= ~Align.top;
		return this;
	}

	/** Adds {@link Align#right} and clears {@link Align#left} for the alignment of the logical table within the table actor. */
	public Table right () {
		align |= Align.right;
		align &= ~Align.left;
		return this;
	}

	/** Turns on all debug lines. */
	public Table debug () {
		this.debug = Debug.all;
		invalidate();
		return this;
	}

	/** Turns on table debug lines. */
	public Table debugTable () {
		this.debug = Debug.table;
		invalidate();
		return this;
	}

	/** Turns on cell debug lines. */
	public Table debugCell () {
		this.debug = Debug.cell;
		invalidate();
		return this;
	}

	/** Turns on actor debug lines. */
	public Table debugActor () {
		this.debug = Debug.actor;
		invalidate();
		return this;
	}

	/** Turns on debug lines. */
	public Table debug (Debug debug) {
		this.debug = debug;
		if (debug == Debug.none) {
			if (debugRects != null) debugRects.clear();
		} else
			invalidate();
		return this;
	}

	public Debug getDebug () {
		return debug;
	}

	public Value getPadTopValue () {
		return padTop;
	}

	public float getPadTop () {
		return padTop.get(this);
	}

	public Value getPadLeftValue () {
		return padLeft;
	}

	public float getPadLeft () {
		return padLeft.get(this);
	}

	public Value getPadBottomValue () {
		return padBottom;
	}

	public float getPadBottom () {
		return padBottom.get(this);
	}

	public Value getPadRightValue () {
		return padRight;
	}

	public float getPadRight () {
		return padRight.get(this);
	}

	/** Returns {@link #getPadLeft()} plus {@link #getPadRight()}. */
	public float getPadX () {
		return padLeft.get(this) + padRight.get(this);
	}

	/** Returns {@link #getPadTop()} plus {@link #getPadBottom()}. */
	public float getPadY () {
		return padTop.get(this) + padBottom.get(this);
	}

	public int getAlign () {
		return align;
	}

	/** Returns the row index for the y coordinate, or -1 if there are no cells. */
	public int getRow (float y) {
		Array<Cell> cells = this.cells;
		int row = 0;
		y += getPadTop();
		int i = 0, n = cells.size;
		if (n == 0) return -1;
		if (n == 1) return 0;
		while (i < n) {
			Cell c = cells.get(i++);
			if (c.actorY + c.computedPadTop < y) break;
			if (c.endRow) row++;
		}
		return row;
	}

	public void setSkin (Skin skin) {
		this.skin = skin;
	}

	/** If true (the default), positions and sizes are rounded to integers. */
	public void setRound (boolean round) {
		this.round = round;
	}

	private float[] ensureSize (float[] array, int size) {
		if (array == null || array.length < size) return new float[size];
		for (int i = 0, n = array.length; i < n; i++)
			array[i] = 0;
		return array;
	}

	public void layout () {
		float width = getWidth();
		float height = getHeight();

		layout(0, 0, width, height);

		Array<Cell> cells = this.cells;
		if (round) {
			for (int i = 0, n = cells.size; i < n; i++) {
				Cell c = cells.get(i);
				float actorWidth = Math.round(c.actorWidth);
				float actorHeight = Math.round(c.actorHeight);
				float actorX = Math.round(c.actorX);
				float actorY = height - Math.round(c.actorY) - actorHeight;
				c.setActorBounds(actorX, actorY, actorWidth, actorHeight);
				Actor actor = c.actor;
				if (actor != null) actor.setBounds(actorX, actorY, actorWidth, actorHeight);
			}
		} else {
			for (int i = 0, n = cells.size; i < n; i++) {
				Cell c = cells.get(i);
				float actorHeight = c.actorHeight;
				float actorY = height - c.actorY - actorHeight;
				c.setActorY(actorY);
				Actor actor = c.actor;
				if (actor != null) actor.setBounds(c.actorX, actorY, c.actorWidth, actorHeight);
			}
		}
		// Validate children separately from sizing actors to ensure actors without a cell are validated.
		Array<Actor> children = getChildren();
		for (int i = 0, n = children.size; i < n; i++) {
			Actor child = children.get(i);
			if (child instanceof Layout) ((Layout)child).validate();
		}
	}

	private void computeSize () {
		sizeInvalid = false;

		Array<Cell> cells = this.cells;
		int cellCount = cells.size;

		if (cellCount > 0 && !cells.peek().endRow) endRow();

		int columns = this.columns, rows = this.rows;
		float[] columnMinWidth = this.columnMinWidth = ensureSize(this.columnMinWidth, columns);
		float[] rowMinHeight = this.rowMinHeight = ensureSize(this.rowMinHeight, rows);
		float[] columnPrefWidth = this.columnPrefWidth = ensureSize(this.columnPrefWidth, columns);
		float[] rowPrefHeight = this.rowPrefHeight = ensureSize(this.rowPrefHeight, rows);
		float[] columnWidth = this.columnWidth = ensureSize(this.columnWidth, columns);
		float[] rowHeight = this.rowHeight = ensureSize(this.rowHeight, rows);
		float[] expandWidth = this.expandWidth = ensureSize(this.expandWidth, columns);
		float[] expandHeight = this.expandHeight = ensureSize(this.expandHeight, rows);

		float spaceRightLast = 0;
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);
			int column = c.column, row = c.row, colspan = c.colspan;
			Actor a = c.actor;

			// Collect columns/rows that expand.
			if (c.expandY != 0 && expandHeight[row] == 0) expandHeight[row] = c.expandY;
			if (colspan == 1 && c.expandX != 0 && expandWidth[column] == 0) expandWidth[column] = c.expandX;

			// Compute combined padding/spacing for cells.
			// Spacing between actors isn't additive, the larger is used. Also, no spacing around edges.
			c.computedPadLeft = c.padLeft.get(a) + (column == 0 ? 0 : Math.max(0, c.spaceLeft.get(a) - spaceRightLast));
			c.computedPadTop = c.padTop.get(a);
			if (c.cellAboveIndex != -1) {
				Cell above = cells.get(c.cellAboveIndex);
				c.computedPadTop += Math.max(0, c.spaceTop.get(a) - above.spaceBottom.get(a));
			}
			float spaceRight = c.spaceRight.get(a);
			c.computedPadRight = c.padRight.get(a) + ((column + colspan) == columns ? 0 : spaceRight);
			c.computedPadBottom = c.padBottom.get(a) + (row == rows - 1 ? 0 : c.spaceBottom.get(a));
			spaceRightLast = spaceRight;

			// Determine minimum and preferred cell sizes.
			float prefWidth = c.prefWidth.get(a);
			float prefHeight = c.prefHeight.get(a);
			float minWidth = c.minWidth.get(a);
			float minHeight = c.minHeight.get(a);
			float maxWidth = c.maxWidth.get(a);
			float maxHeight = c.maxHeight.get(a);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (prefHeight < minHeight) prefHeight = minHeight;
			if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth;
			if (maxHeight > 0 && prefHeight > maxHeight) prefHeight = maxHeight;

			if (colspan == 1) { // Spanned column min and pref width is added later.
				float hpadding = c.computedPadLeft + c.computedPadRight;
				columnPrefWidth[column] = Math.max(columnPrefWidth[column], prefWidth + hpadding);
				columnMinWidth[column] = Math.max(columnMinWidth[column], minWidth + hpadding);
			}
			float vpadding = c.computedPadTop + c.computedPadBottom;
			rowPrefHeight[row] = Math.max(rowPrefHeight[row], prefHeight + vpadding);
			rowMinHeight[row] = Math.max(rowMinHeight[row], minHeight + vpadding);
		}

		// Colspan with expand will expand all spanned columns if none of the spanned columns have expand.
		outer:
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);
			if (c.expandX == 0) continue;
			int column = c.column;
			int nn = column + c.colspan;
			for (int ii = column; ii < nn; ii++)
				if (expandWidth[ii] != 0) continue outer;
			int expandX = c.expandX;
			for (int ii = column; ii < nn; ii++)
				expandWidth[ii] = expandX;
		}

		// Distribute any additional min and pref width added by colspanned cells to the columns spanned.
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);
			int colspan = c.colspan;
			if (colspan == 1) continue;
			int column = c.column;

			Actor a = c.actor;
			float minWidth = c.minWidth.get(a);
			float prefWidth = c.prefWidth.get(a);
			float maxWidth = c.maxWidth.get(a);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth;

			float spannedMinWidth = -(c.computedPadLeft + c.computedPadRight), spannedPrefWidth = spannedMinWidth;
			for (int ii = column, nn = ii + colspan; ii < nn; ii++) {
				spannedMinWidth += columnMinWidth[ii];
				spannedPrefWidth += columnPrefWidth[ii];
			}

			// Distribute extra space using expand, if any columns have expand.
			float totalExpandWidth = 0;
			for (int ii = column, nn = ii + colspan; ii < nn; ii++)
				totalExpandWidth += expandWidth[ii];

			float extraMinWidth = Math.max(0, minWidth - spannedMinWidth);
			float extraPrefWidth = Math.max(0, prefWidth - spannedPrefWidth);
			for (int ii = column, nn = ii + colspan; ii < nn; ii++) {
				float ratio = totalExpandWidth == 0 ? 1f / colspan : expandWidth[ii] / totalExpandWidth;
				columnMinWidth[ii] += extraMinWidth * ratio;
				columnPrefWidth[ii] += extraPrefWidth * ratio;
			}
		}

		// Collect uniform size.
		float uniformMinWidth = 0, uniformMinHeight = 0;
		float uniformPrefWidth = 0, uniformPrefHeight = 0;
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);

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
			for (int i = 0; i < cellCount; i++) {
				Cell c = cells.get(i);
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
		float hpadding = padLeft.get(this) + padRight.get(this);
		float vpadding = padTop.get(this) + padBottom.get(this);
		tableMinWidth = tableMinWidth + hpadding;
		tableMinHeight = tableMinHeight + vpadding;
		tablePrefWidth = Math.max(tablePrefWidth + hpadding, tableMinWidth);
		tablePrefHeight = Math.max(tablePrefHeight + vpadding, tableMinHeight);
	}

	/** Positions and sizes children of the table using the cell associated with each child. The values given are the position
	 * within the parent and size of the table. */
	private void layout (float layoutX, float layoutY, float layoutWidth, float layoutHeight) {
		Array<Cell> cells = this.cells;
		int cellCount = cells.size;

		if (sizeInvalid) computeSize();

		float padLeft = this.padLeft.get(this);
		float hpadding = padLeft + padRight.get(this);
		float padTop = this.padTop.get(this);
		float vpadding = padTop + padBottom.get(this);

		int columns = this.columns, rows = this.rows;
		float[] expandWidth = this.expandWidth, expandHeight = this.expandHeight;
		float[] columnWidth = this.columnWidth, rowHeight = this.rowHeight;

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
			columnWeightedWidth = Table.columnWeightedWidth = ensureSize(Table.columnWeightedWidth, columns);
			float[] columnMinWidth = this.columnMinWidth, columnPrefWidth = this.columnPrefWidth;
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
			rowWeightedHeight = Table.rowWeightedHeight = ensureSize(Table.rowWeightedHeight, rows);
			float extraHeight = Math.min(totalGrowHeight, Math.max(0, layoutHeight - tableMinHeight));
			float[] rowMinHeight = this.rowMinHeight, rowPrefHeight = this.rowPrefHeight;
			for (int i = 0; i < rows; i++) {
				float growHeight = rowPrefHeight[i] - rowMinHeight[i];
				float growRatio = growHeight / totalGrowHeight;
				rowWeightedHeight[i] = rowMinHeight[i] + extraHeight * growRatio;
			}
		}

		// Determine actor and cell sizes (before expand or fill).
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);
			int column = c.column, row = c.row;
			Actor a = c.actor;

			float spannedWeightedWidth = 0;
			for (int ii = column, nn = ii + c.colspan; ii < nn; ii++)
				spannedWeightedWidth += columnWeightedWidth[ii];
			float weightedHeight = rowWeightedHeight[row];

			float prefWidth = c.prefWidth.get(a);
			float prefHeight = c.prefHeight.get(a);
			float minWidth = c.minWidth.get(a);
			float minHeight = c.minHeight.get(a);
			float maxWidth = c.maxWidth.get(a);
			float maxHeight = c.maxHeight.get(a);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (prefHeight < minHeight) prefHeight = minHeight;
			if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth;
			if (maxHeight > 0 && prefHeight > maxHeight) prefHeight = maxHeight;

			c.actorWidth = Math.min(spannedWeightedWidth - c.computedPadLeft - c.computedPadRight, prefWidth);
			c.actorHeight = Math.min(weightedHeight - c.computedPadTop - c.computedPadBottom, prefHeight);

			if (c.colspan == 1) columnWidth[column] = Math.max(columnWidth[column], spannedWeightedWidth);
			rowHeight[row] = Math.max(rowHeight[row], weightedHeight);
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
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);
			int colspan = c.colspan;
			if (colspan == 1) continue;

			float extraWidth = 0;
			for (int column = c.column, nn = column + colspan; column < nn; column++)
				extraWidth += columnWeightedWidth[column] - columnWidth[column];
			extraWidth -= Math.max(0, c.computedPadLeft + c.computedPadRight);

			extraWidth /= colspan;
			if (extraWidth > 0) {
				for (int column = c.column, nn = column + colspan; column < nn; column++)
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
		int align = this.align;
		float x = layoutX + padLeft;
		if ((align & Align.right) != 0)
			x += layoutWidth - tableWidth;
		else if ((align & Align.left) == 0) // Center
			x += (layoutWidth - tableWidth) / 2;

		float y = layoutY + padTop;
		if ((align & Align.bottom) != 0)
			y += layoutHeight - tableHeight;
		else if ((align & Align.top) == 0) // Center
			y += (layoutHeight - tableHeight) / 2;

		// Position actors within cells.
		float currentX = x, currentY = y;
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);

			float spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.computedPadLeft + c.computedPadRight;

			currentX += c.computedPadLeft;

			if (c.fillX > 0) {
				c.actorWidth = spannedCellWidth * c.fillX;
				float maxWidth = c.maxWidth.get(c.actor);
				if (maxWidth > 0) c.actorWidth = Math.min(c.actorWidth, maxWidth);
			}
			if (c.fillY > 0) {
				c.actorHeight = rowHeight[c.row] * c.fillY - c.computedPadTop - c.computedPadBottom;
				float maxHeight = c.maxHeight.get(c.actor);
				if (maxHeight > 0) c.actorHeight = Math.min(c.actorHeight, maxHeight);
			}

			if ((c.align & Align.left) != 0)
				c.actorX = currentX;
			else if ((c.align & Align.right) != 0)
				c.actorX = currentX + spannedCellWidth - c.actorWidth;
			else
				c.actorX = currentX + (spannedCellWidth - c.actorWidth) / 2;

			if ((c.align & Align.top) != 0)
				c.actorY = currentY + c.computedPadTop;
			else if ((c.align & Align.bottom) != 0)
				c.actorY = currentY + rowHeight[c.row] - c.actorHeight - c.computedPadBottom;
			else
				c.actorY = currentY + (rowHeight[c.row] - c.actorHeight + c.computedPadTop - c.computedPadBottom) / 2;

			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.computedPadRight;
		}

		// Store debug rectangles.
		if (debug == Debug.none) return;
		if (debugRects != null) debugRects.clear();
		currentX = x;
		currentY = y;
		if (debug == Debug.table || debug == Debug.all) {
			TableDebug.addRectangle(this, Debug.table, layoutX, layoutY, layoutWidth, layoutHeight);
			TableDebug.addRectangle(this, Debug.table, x, y, tableWidth - hpadding, tableHeight - vpadding);
		}
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);

			// Actor bounds.
			if (debug == Debug.actor || debug == Debug.all)
				TableDebug.addRectangle(this, Debug.actor, c.actorX, c.actorY, c.actorWidth, c.actorHeight);

			// Cell bounds.
			float spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.computedPadLeft + c.computedPadRight;
			currentX += c.computedPadLeft;
			if (debug == Debug.cell || debug == Debug.all) {
				TableDebug.addRectangle(this, Debug.cell, currentX, currentY + c.computedPadTop, spannedCellWidth, rowHeight[c.row]
					- c.computedPadTop - c.computedPadBottom);
			}

			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.computedPadRight;
		}
	}

	/** Draws the debug lines for all tables in the stage. If this method is not called each frame, no debug lines will be drawn. If
	 * debug is never turned on for any table in the application, calling this method will have no effect. If a table has ever had
	 * debug set, calling this method causes an expensive traversal of all actors in the stage. */
	static public void drawDebug (Stage stage) {
		if (TableDebug.draw) TableDebug.draw(stage);
	}

	/** @author Nathan Sweet */
	static public enum Debug {
		none, all, table, cell, actor
	}

	/** @author Nathan Sweet */
	static class DebugRect extends Rectangle {
		final Debug type;

		public DebugRect (Debug type, float x, float y, float width, float height) {
			super(x, y, width, height);
			this.type = type;
		}
	}

	/** @author Nathan Sweet */
	static class TableDebug {
		static boolean draw;

		static private Application app;
		static private ImmediateModeRenderer debugRenderer;

		static public void addRectangle (Table table, Debug type, float x, float y, float w, float h) {
			draw = true;
			if (table.debugRects == null) table.debugRects = new Array();
			table.debugRects.add(new DebugRect(type, x, table.getHeight() - y, w, h));
		}

		static void draw (Stage stage) {
			if (app != Gdx.app) debugRenderer = new ImmediateModeRenderer20(128, false, true, 0);

			debugRenderer.begin(stage.getBatch().getProjectionMatrix(), GL20.GL_LINES);
			draw(stage.getActors());
			debugRenderer.end();
		}

		static void draw (Array<Actor> actors) {
			for (int i = 0, n = actors.size; i < n; i++) {
				Actor actor = actors.get(i);
				if (!actor.isVisible()) continue;
				if (actor instanceof Table) draw((Table)actor);
				if (actor instanceof Group) draw(((Group)actor).getChildren());
			}
		}

		static public void draw (Table table) {
			if (table.debug == Debug.none) return;
			Array<DebugRect> debugRects = table.debugRects;
			if (debugRects == null) return;

			float x = 0, y = 0;
			Actor parent = table;
			while (parent != null) {
				if (parent instanceof Group) {
					x += parent.getX();
					y += parent.getY();
				}
				parent = parent.getParent();
			}

			for (int i = 0, n = debugRects.size; i < n; i++) {
				DebugRect rect = debugRects.get(i);
				float x1 = x + rect.x;
				float y1 = y + rect.y - rect.height;
				float x2 = x1 + rect.width;
				float y2 = y1 + rect.height;
				float r = rect.type == Debug.cell ? 1 : 0;
				float g = rect.type == Debug.actor ? 1 : 0;
				float b = rect.type == Debug.table ? 1 : 0;

				debugRenderer.color(r, g, b, 1);
				debugRenderer.vertex(x1, y1, 0);
				debugRenderer.color(r, g, b, 1);
				debugRenderer.vertex(x1, y2, 0);

				debugRenderer.color(r, g, b, 1);
				debugRenderer.vertex(x1, y2, 0);
				debugRenderer.color(r, g, b, 1);
				debugRenderer.vertex(x2, y2, 0);

				debugRenderer.color(r, g, b, 1);
				debugRenderer.vertex(x2, y2, 0);
				debugRenderer.color(r, g, b, 1);
				debugRenderer.vertex(x2, y1, 0);

				debugRenderer.color(r, g, b, 1);
				debugRenderer.vertex(x2, y1, 0);
				debugRenderer.color(r, g, b, 1);
				debugRenderer.vertex(x1, y1, 0);

				if (debugRenderer.getNumVertices() == 128) debugRenderer.flush();
			}
		}
	}
}

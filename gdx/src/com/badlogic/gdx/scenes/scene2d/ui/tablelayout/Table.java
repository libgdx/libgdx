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

import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.esotericsoftware.tablelayout.Cell;

import static com.badlogic.gdx.scenes.scene2d.ui.tablelayout.TableLayout.*;

/** @author Nathan Sweet */
public class Table extends Group implements Layout {
	private final TableLayout layout;
	boolean prefSizeInvalid = true;
	private int prefWidth, prefHeight;

	public Table () {
		this(null, new TableLayout());
	}

	public Table (float width, float height) {
		this(null, new TableLayout());
		this.width = width;
		this.height = height;
	}

	public Table (TableLayout layout) {
		this(null, layout);
	}

	public Table (String name) {
		this(name, new TableLayout());
	}

	public Table (String name, float width, float height) {
		this(name, new TableLayout());
		this.width = width;
		this.height = height;
	}

	public Table (String name, TableLayout layout) {
		super(name);
		transform = false;
		this.layout = layout;
		layout.setTable(this);
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		if (!visible) return;
		if (layout.needsLayout) layout.layout();
		super.draw(batch, parentAlpha);
	}

	private void computePrefSize () {
		if (!prefSizeInvalid) return;
		prefSizeInvalid = false;
		layout.setLayoutSize(0, 0, 0, 0);
		layout.invalidate();
		layout.layout();
		prefWidth = layout.getMinWidth();
		prefHeight = layout.getMinHeight();
		layout.invalidate();
	}

	public float getPrefWidth () {
		if (prefSizeInvalid) computePrefSize();
		return prefWidth;
	}

	public float getPrefHeight () {
		if (prefSizeInvalid) computePrefSize();
		return prefHeight;
	}

	public TableLayout getTableLayout () {
		return layout;
	}

	/** Removes all actors and cells from the table. */
	public void clear () {
		super.clear();
		layout.clear();
	}

	/** Adds a new cell to the table with the specified actor.
	 * @see TableLayout#add(Actor)
	 * @param actor May be null to add a cell without an actor. */
	public Cell add (Actor actor) {
		return layout.add(actor);
	}

	/** Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row.
	 * @see TableLayout#row() */
	public Cell row () {
		return layout.row();
	}

	/** Parses a table description and adds the actors and cells to the table.
	 * @see TableLayout#parse(String) */
	public void parse (String tableDescription) {
		layout.parse(tableDescription);
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

	/** Positions and sizes children of the actor being laid out using the cell associated with each child.
	 * @see TableLayout#layout() */
	public void layout () {
		layout.layout();
	}

	public void invalidate () {
		layout.invalidate();
	}

	/** Invalides the layout of this actor and every parent actor to the root of the hierarchy.
	 * @see TableLayout#invalidateHierarchy() */
	public void invalidateHierarchy () {
		layout.invalidateHierarchy();
	}

	/** Removes all actors and cells from the table (same as {@link #clear()}) and additionally resets all table properties and
	 * cell, column, and row defaults.
	 * @see TableLayout#reset() */
	public void reset () {
		layout.reset();
	}

	/** Returns the cell for the specified actor, anywhere in the table hierarchy.
	 * @see TableLayout#getCell(Actor) */
	public Cell getCell (Actor actor) {
		return layout.getCell(actor);
	}

	/** Returns the cell with the specified name, anywhere in the table hierarchy.
	 * @see TableLayout#getCell(String) */
	public Cell getCell (String name) {
		return layout.getCell(name);
	}

	/** Returns all cells, anywhere in the table hierarchy.
	 * @see TableLayout#getAllCells() */
	public List<Cell> getAllCells () {
		return layout.getAllCells();
	}

	/** Returns all cells with the specified name prefix, anywhere in the table hierarchy.
	 * @see TableLayout#getAllCells(String) */
	public List<Cell> getAllCells (String namePrefix) {
		return layout.getAllCells(namePrefix);
	}

	/** Returns the cells for this table.
	 * @see TableLayout#getCells() */
	public List<Cell> getCells () {
		return layout.getCells();
	}

	/** Sets the actor in the cell with the specified name.
	 * @see TableLayout#setWidget(String, Actor) */
	public void setActor (String name, Actor actor) {
		layout.setWidget(name, actor);
	}

	/** The minimum width of the table. Available after laying out.
	 * @see TableLayout#getMinWidth() */
	public int getMinWidth () {
		return layout.getMinWidth();
	}

	/** The minimum size of the table. Available after laying out.
	 * @see TableLayout#getMinHeight() */
	public int getMinHeight () {
		return layout.getMinHeight();
	}

	/** The fixed size of the table.
	 * @see TableLayout#size(String, String) */
	public Table size (String width, String height) {
		layout.size(width, height);
		return this;
	}

	/** The fixed width of the table, or null.
	 * @see TableLayout#width(String) */
	public Table width (String width) {
		layout.width(width);
		return this;
	}

	/** The fixed height of the table, or null.
	 * @see TableLayout#height(String) */
	public Table height (String height) {
		layout.height(height);
		return this;
	}

	/** The fixed size of the table.
	 * @see TableLayout#size(int, int) */
	public Table size (int width, int height) {
		layout.size(width, height);
		return this;
	}

	/** The fixed width of the table.
	 * @see TableLayout#width(int) */
	public Table width (int width) {
		layout.width(width);
		return this;
	}

	/** The fixed height of the table.
	 * @see TableLayout#height(int) */
	public Table height (int height) {
		layout.height(height);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(String) */
	public Table pad (String pad) {
		layout.pad(pad);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(String, String, String, String) */
	public Table pad (String top, String left, String bottom, String right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top of the table.
	 * @see TableLayout#padTop(String) */
	public Table padTop (String padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left of the table.
	 * @see TableLayout#padLeft(String) */
	public Table padLeft (String padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom of the table.
	 * @see TableLayout#padBottom(String) */
	public Table padBottom (String padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right of the table.
	 * @see TableLayout#padRight(String) */
	public Table padRight (String padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(int) */
	public Table pad (int pad) {
		layout.pad(pad);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(int, int, int, int) */
	public Table pad (int top, int left, int bottom, int right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top of the table.
	 * @see TableLayout#padTop(int) */
	public Table padTop (int padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left of the table.
	 * @see TableLayout#padLeft(int) */
	public Table padLeft (int padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom of the table.
	 * @see TableLayout#padBottom(int) */
	public Table padBottom (int padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right of the table.
	 * @see TableLayout#padRight(int) */
	public Table padRight (int padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Alignment of the table within the actor being laid out. Set to {@link #CENTER}, {@link #TOP}, {@link #BOTTOM}, {@link #LEFT}
	 * , {@link #RIGHT}, or any combination of those.
	 * @see TableLayout#align(int) */
	public Table align (int align) {
		layout.align(align);
		return this;
	}

	/** Alignment of the table within the actor being laid out. Set to "center", "top", "bottom", "left", "right", or a string
	 * containing any combination of those.
	 * @see TableLayout#align(String) */
	public Table align (String value) {
		layout.align(value);
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link #CENTER}.
	 * @see TableLayout#center() */
	public Table center () {
		layout.center();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link #TOP}.
	 * @see TableLayout#top() */
	public Table top () {
		layout.top();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link #LEFT}.
	 * @see TableLayout#left() */
	public Table left () {
		layout.left();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link #BOTTOM}.
	 * @see TableLayout#bottom() */
	public Table bottom () {
		layout.bottom();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link #RIGHT}.
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

	/** Turns on debug lines. Set to {@value #DEBUG_ALL}, {@value #DEBUG_TABLE}, {@value #DEBUG_CELL}, {@value #DEBUG_WIDGET}, or
	 * any combination of those. Set to {@value #DEBUG_NONE} to disable.
	 * @see TableLayout#debug() */
	public Table debug (int debug) {
		layout.debug(debug);
		return this;
	}

	/** Turns on debug lines. Set to "all", "table", "cell", "widget", or a string containing any combination of those. Set to null
	 * to disable.
	 * @see TableLayout#debug(String) */
	public Table debug (String value) {
		layout.debug(value);
		return this;
	}

	public int getDebug () {
		return layout.getDebug();
	}

	public String getHeight () {
		return layout.getHeight();
	}

	public String getPadTop () {
		return layout.getPadTop();
	}

	public String getPadLeft () {
		return layout.getPadLeft();
	}

	public String getPadBottom () {
		return layout.getPadBottom();
	}

	public String getPadRight () {
		return layout.getPadRight();
	}

	public int getAlign () {
		return layout.getAlign();
	}

	/** Draws the debug lines for all TableLayouts in the stage. If this method is not called each frame, no debug lines will be
	 * drawn. */
	static public void drawDebug (Stage stage) {
		drawDebug(stage.getActors(), stage.getSpriteBatch());
	}

	static private void drawDebug (List<Actor> actors, SpriteBatch batch) {
		for (int i = 0, n = actors.size(); i < n; i++) {
			Actor actor = actors.get(i);
			if (actor instanceof Table) ((Table)actor).layout.drawDebug(batch);
			if (actor instanceof Group) drawDebug(((Group)actor).getActors(), batch);
		}
	}
}

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

package com.badlogic.gdx.scenes.scene2d.ui;

import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Toolkit;
import com.esotericsoftware.tablelayout.Value;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

import java.util.List;

/** A group that sizes and positions children using table constraints. By default, {@link #getTouchable()} is
 * {@link Touchable#childrenOnly}.
 * <p>
 * The preferred and minimum sizes are that of the children when laid out in columns and rows.
 * @author Nathan Sweet */
public class Table extends WidgetGroup {
	static {
		if (Toolkit.instance == null) Toolkit.instance = new TableToolkit();
	}

	private final TableLayout layout;
	private Drawable background;
	private boolean clip;
	private Skin skin;

	public Table () {
		this(null);
	}

	/** Creates a table with a skin, which enables the {@link #add(String)} and {@link #add(String, String)} methods to be used. */
	public Table (Skin skin) {
		this.skin = skin;
		layout = new TableLayout();
		layout.setTable(this);
		setTransform(false);
		setTouchable(Touchable.childrenOnly);
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
					x = layout.getPadLeft();
					y = layout.getPadBottom();
					width -= layout.getPadLeft() + layout.getPadRight();
					height -= layout.getPadBottom() + layout.getPadTop();
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

	public void invalidate () {
		layout.invalidate();
		super.invalidate();
	}

	public float getPrefWidth () {
		if (background != null) return Math.max(layout.getPrefWidth(), background.getMinWidth());
		return layout.getPrefWidth();
	}

	public float getPrefHeight () {
		if (background != null) return Math.max(layout.getPrefHeight(), background.getMinHeight());
		return layout.getPrefHeight();
	}

	public float getMinWidth () {
		return layout.getMinWidth();
	}

	public float getMinHeight () {
		return layout.getMinHeight();
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

	/** Causes the contents to be clipped if they exceed the table widget bounds. Enabling clipping will set
	 * {@link #setTransform(boolean)} to true. */
	public void setClip (boolean enabled) {
		clip = enabled;
		setTransform(enabled);
		invalidate();
	}

	public boolean getClip () {
		return clip;
	}

	/** Returns the row index for the y coordinate. */
	public int getRow (float y) {
		return layout.getRow(y);
	}

	/** Removes all actors and cells from the table. */
	public void clearChildren () {
		super.clearChildren();
		layout.clear();
		invalidate();
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

	/** Adds a cell without a widget. */
	public Cell<Actor> add () {
		return layout.add(null);
	}

	/** Adds a new cell to the table with the specified actor.
	 * @param actor May be null to add a cell without an actor. */
	public <T extends Actor> Cell<T> add (T actor) {
		return (Cell<T>)layout.add(actor);
	}

	public void add (Actor... actors) {
		for (int i = 0, n = actors.length; i < n; i++)
			layout.add(actors[i]);
	}

	public boolean removeActor (Actor actor) {
		if (!super.removeActor(actor)) return false;
		Cell cell = getCell(actor);
		if (cell != null) cell.setWidget(null);
		return true;
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

	/** Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row. */
	public Cell row () {
		return layout.row();
	}

	/** Gets the cell values that will be used as the defaults for all cells in the specified column. Columns are indexed starting
	 * at 0. */
	public Cell columnDefaults (int column) {
		return layout.columnDefaults(column);
	}

	/** The cell values that will be used as the defaults for all cells. */
	public Cell defaults () {
		return layout.defaults();
	}

	public void layout () {
		layout.layout();
	}

	/** Removes all actors and cells from the table (same as {@link #clearChildren()}) and additionally resets all table properties
	 * and cell, column, and row defaults. */
	public void reset () {
		layout.reset();
	}

	/** Returns the cell for the specified widget in this table, or null. */
	public <T extends Actor> Cell<T> getCell (T actor) {
		return (Cell<T>)layout.getCell(actor);
	}

	/** Returns the cells for this table. */
	public List<Cell> getCells () {
		return layout.getCells();
	}

	/** Sets the padTop, padLeft, padBottom, and padRight around the table to the specified value. */
	public Table pad (Value pad) {
		layout.pad(pad);
		return this;
	}

	public Table pad (Value top, Value left, Value bottom, Value right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top edge of the table. */
	public Table padTop (Value padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left edge of the table. */
	public Table padLeft (Value padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom edge of the table. */
	public Table padBottom (Value padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right edge of the table. */
	public Table padRight (Value padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Sets the padTop, padLeft, padBottom, and padRight around the table to the specified value. */
	public Table pad (float pad) {
		layout.pad(pad);
		return this;
	}

	public Table pad (float top, float left, float bottom, float right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top edge of the table. */
	public Table padTop (float padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left edge of the table. */
	public Table padLeft (float padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom edge of the table. */
	public Table padBottom (float padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right edge of the table. */
	public Table padRight (float padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Sets the alignment of the logical table within the table widget. Set to {@link Align#center}, {@link Align#top},
	 * {@link Align#bottom} , {@link Align#left} , {@link Align#right}, or any combination of those. */
	public Table align (int align) {
		layout.align(align);
		return this;
	}

	/** Sets the alignment of the logical table within the table widget to {@link Align#center}. This clears any other alignment. */
	public Table center () {
		layout.center();
		return this;
	}

	/** Adds {@link Align#top} and clears {@link Align#bottom} for the alignment of the logical table within the table widget. */
	public Table top () {
		layout.top();
		return this;
	}

	/** Adds {@link Align#left} and clears {@link Align#right} for the alignment of the logical table within the table widget. */
	public Table left () {
		layout.left();
		return this;
	}

	/** Adds {@link Align#bottom} and clears {@link Align#top} for the alignment of the logical table within the table widget. */
	public Table bottom () {
		layout.bottom();
		return this;
	}

	/** Adds {@link Align#right} and clears {@link Align#left} for the alignment of the logical table within the table widget. */
	public Table right () {
		layout.right();
		return this;
	}

	/** Turns on all debug lines. */
	public Table debug () {
		layout.debug();
		return this;
	}

	/** Turns on table debug lines. */
	public Table debugTable () {
		layout.debugTable();
		return this;
	}

	/** Turns on cell debug lines. */
	public Table debugCell () {
		layout.debugCell();
		return this;
	}

	/** Turns on widget debug lines. */
	public Table debugWidget () {
		layout.debugWidget();
		return this;
	}

	/** Turns on debug lines. */
	public Table debug (Debug debug) {
		layout.debug(debug);
		return this;
	}

	public Debug getDebug () {
		return layout.getDebug();
	}

	public Value getPadTopValue () {
		return layout.getPadTopValue();
	}

	public float getPadTop () {
		return layout.getPadTop();
	}

	public Value getPadLeftValue () {
		return layout.getPadLeftValue();
	}

	public float getPadLeft () {
		return layout.getPadLeft();
	}

	public Value getPadBottomValue () {
		return layout.getPadBottomValue();
	}

	public float getPadBottom () {
		return layout.getPadBottom();
	}

	public Value getPadRightValue () {
		return layout.getPadRightValue();
	}

	public float getPadRight () {
		return layout.getPadRight();
	}

	/** Returns {@link #getPadLeft()} plus {@link #getPadRight()}. */
	public float getPadX () {
		return layout.getPadLeft() + layout.getPadRight();
	}

	/** Returns {@link #getPadTop()} plus {@link #getPadBottom()}. */
	public float getPadY () {
		return layout.getPadTop() + layout.getPadBottom();
	}

	public int getAlign () {
		return layout.getAlign();
	}

	public void setSkin (Skin skin) {
		this.skin = skin;
	}

	/** If true (the default), positions and sizes are rounded to integers. */
	public void setRound (boolean round) {
		layout.round = round;
	}

	/** Draws the debug lines for all tables in the stage. If this method is not called each frame, no debug lines will be drawn. If
	 * debug is never turned on for any table in the application, calling this method will have no effect. If a table has ever had
	 * debug set, calling this method causes an expensive traversal of all actors in the stage. */
	static public void drawDebug (Stage stage) {
		if (!TableToolkit.drawDebug) return;
		drawDebug(stage.getActors(), stage.getSpriteBatch());
	}

	static private void drawDebug (Array<Actor> actors, Batch batch) {
		for (int i = 0, n = actors.size; i < n; i++) {
			Actor actor = actors.get(i);
			if (!actor.isVisible()) continue;
			if (actor instanceof Table) ((Table)actor).layout.drawDebug(batch);
			if (actor instanceof Group) drawDebug(((Group)actor).getChildren(), batch);
		}
	}
}

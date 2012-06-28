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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Value;

/** A group that sizes and positions children using table constraints.
 * <p>
 * The preferred and minimum sizes are that of the children when laid out in columns and rows.
 * @author Nathan Sweet */
public class Table extends WidgetGroup {
	private final TableLayout layout;
	private Drawable backgroundDrawable;
	private final Rectangle tableBounds = new Rectangle();
	private final Rectangle scissors = new Rectangle();
	private boolean clip;
	private Skin skin;

	public Table () {
		this(null);
	}

	public Table (Skin skin) {
		this.skin = skin;
		layout = new TableLayout();
		layout.setTable(this);
		setTransform(false);
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		validate();
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		drawBackground(batch, parentAlpha);

		if (isTransform()) {
			applyTransform(batch);
			if (clip) {
				calculateScissors(batch.getTransformMatrix());
				if (ScissorStack.pushScissors(scissors)) {
					drawChildren(batch, parentAlpha);
					ScissorStack.popScissors();
				}
			} else
				drawChildren(batch, parentAlpha);
			resetTransform(batch);
		} else
			super.draw(batch, parentAlpha);
	}

	/** Called to draw the background, before clipping is applied (if enabled). Default implementation draws the background
	 * drawable. */
	protected void drawBackground (SpriteBatch batch, float parentAlpha) {
		if (backgroundDrawable != null) {
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			backgroundDrawable.draw(batch, getX(), getY(), getWidth(), getHeight());
		}
	}

	private void calculateScissors (Matrix4 transform) {
		tableBounds.x = 0;
		tableBounds.y = 0;
		tableBounds.width = getWidth();
		tableBounds.height = getHeight();
		if (backgroundDrawable != null) {
			tableBounds.x += layout.getToolkit().width(layout.getPadLeft().get());
			tableBounds.y += layout.getToolkit().height(layout.getPadBottom().get());
			tableBounds.width -= tableBounds.x + layout.getToolkit().width(layout.getPadRight().get());
			tableBounds.height -= tableBounds.y + layout.getToolkit().height(layout.getPadTop().get());
		}
		ScissorStack.calculateScissors(getStage().getCamera(), transform, tableBounds, scissors);
	}

	public void invalidate () {
		layout.invalidate();
		super.invalidate();
	}

	public float getPrefWidth () {
		if (backgroundDrawable != null) return Math.max(layout.getPrefWidth(), backgroundDrawable.getMinWidth());
		return layout.getPrefWidth();
	}

	public float getPrefHeight () {
		if (backgroundDrawable != null) return Math.max(layout.getPrefHeight(), backgroundDrawable.getMinHeight());
		return layout.getPrefHeight();
	}

	public float getMinWidth () {
		return layout.getMinWidth();
	}

	public float getMinHeight () {
		return layout.getMinHeight();
	}

	/** Sets the background drawable and sets the table's padding to {@link Drawable#getBottomHeight()} ,
	 * {@link Drawable#getTopHeight()}, {@link Drawable#getLeftWidth()}, and {@link Drawable#getRightWidth()}.
	 * @param background If null, no background will be set and all padding is removed. */
	public void setBackground (Drawable background) {
		if (this.backgroundDrawable == background) return;
		this.backgroundDrawable = background;
		if (background == null)
			pad(null);
		else {
			padBottom(background.getBottomHeight());
			padTop(background.getTopHeight());
			padLeft(background.getLeftWidth());
			padRight(background.getRightWidth());
			invalidate();
		}
	}

	public Drawable getBackground () {
		return backgroundDrawable;
	}

	/** Causes the contents to be clipped if they exceed the table bounds. Enabling clipping will set {@link #setTransform(boolean)}
	 * to true. */
	public void setClip (boolean enabled) {
		clip = enabled;
		setTransform(enabled);
		invalidate();
	}

	/** Returns the row index for the y coordinate. */
	public int getRow (float y) {
		return layout.getRow(y);
	}

	public TableLayout getTableLayout () {
		return layout;
	}

	/** Removes all actors and cells from the table. */
	public void clear () {
		super.clear();
		layout.clear();
		invalidate();
	}

	/** Adds a new cell with a label. This may only be called if {@link Table#Table(Skin)} or {@link #setSkin(Skin)} was used. */
	public Cell add (String text) {
		if (skin == null) throw new IllegalStateException("Table must have a skin set to use this method.");
		return add(new Label(text, skin));
	}

	/** Adds a new cell with a label. This may only be called if {@link Table#Table(Skin)} or {@link #setSkin(Skin)} was used. */
	public Cell add (String text, String labelStyleName) {
		if (skin == null) throw new IllegalStateException("Table must have a skin set to use this method.");
		return add(new Label(text, skin.getStyle(labelStyleName, LabelStyle.class)));
	}

	/** Adds a cell with a placeholder actor. */
	public Cell add () {
		return add((Actor)null);
	}

	/** Adds a new cell to the table with the specified actor.
	 * @see TableLayout#add(Actor)
	 * @param actor May be null to add a cell without an actor. */
	public Cell add (Actor actor) {
		return layout.add(actor);
	}

	/** Adds a new cell to the table with the specified actors in a {@link Stack}.
	 * @param actors May be null to add a stack without any actors. */
	public Cell stack (Actor... actors) {
		Stack stack = new Stack();
		if (actors != null) {
			for (int i = 0, n = actors.length; i < n; i++)
				stack.addActor(actors[i]);
		}
		return add(stack);
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

	/** Positions and sizes children of the actor being laid out using the cell associated with each child.
	 * @see TableLayout#layout() */
	public void layout () {
		layout.layout();
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

	/** Returns the cells for this table.
	 * @see TableLayout#getCells() */
	public List<Cell> getCells () {
		return layout.getCells();
	}

	/** The fixed size of the table.
	 * @see TableLayout#size(Value, Value) */
	public Table tableSize (Value width, Value height) {
		layout.size(width, height);
		return this;
	}

	/** The fixed width of the table, or null.
	 * @see TableLayout#width(Value) */
	public Table tableWidth (Value width) {
		layout.width(width);
		return this;
	}

	/** The fixed height of the table, or null.
	 * @see TableLayout#height(Value) */
	public Table tableHeight (Value height) {
		layout.height(height);
		return this;
	}

	/** The fixed size of the table.
	 * @see TableLayout#size(float, float) */
	public Table tableSize (float width, float height) {
		layout.size(width, height);
		return this;
	}

	/** The fixed width of the table.
	 * @see TableLayout#width(float) */
	public Table tableWidth (float width) {
		layout.width(width);
		return this;
	}

	/** The fixed height of the table.
	 * @see TableLayout#height(float) */
	public Table tableHeight (float height) {
		layout.height(height);
		return this;
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
	public Table pad (float pad) {
		layout.pad(pad);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(float, float, float, float) */
	public Table pad (float top, float left, float bottom, float right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top of the table.
	 * @see TableLayout#padTop(float) */
	public Table padTop (float padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left of the table.
	 * @see TableLayout#padLeft(float) */
	public Table padLeft (float padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom of the table.
	 * @see TableLayout#padBottom(float) */
	public Table padBottom (float padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right of the table.
	 * @see TableLayout#padRight(float) */
	public Table padRight (float padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Alignment of the table within the actor being laid out. Set to {@link Align#CENTER}, {@link Align#TOP}, {@link Align#BOTTOM}
	 * , {@link Align#LEFT} , {@link Align#RIGHT}, or any combination of those.
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

	/** Sets the alignment of the table within the actor being laid out to {@link Align#CENTER}.
	 * @see TableLayout#center() */
	public Table center () {
		layout.center();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#TOP}.
	 * @see TableLayout#top() */
	public Table top () {
		layout.top();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#LEFT}.
	 * @see TableLayout#left() */
	public Table left () {
		layout.left();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#BOTTOM}.
	 * @see TableLayout#bottom() */
	public Table bottom () {
		layout.bottom();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#RIGHT}.
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
		return layout.getPadTop();
	}

	public Value getPadLeft () {
		return layout.getPadLeft();
	}

	public Value getPadBottom () {
		return layout.getPadBottom();
	}

	public Value getPadRight () {
		return layout.getPadRight();
	}

	public int getAlign () {
		return layout.getAlign();
	}

	public void setSkin (Skin skin) {
		this.skin = skin;
	}

	/** Draws the debug lines for all TableLayouts in the stage. If this method is not called each frame, no debug lines will be
	 * drawn. If debug is never turned on for any table in the application, calling this method will have no effect. If a table has
	 * ever had debug set, calling this method causes an expensive traversal of all actors in the stage. */
	static public void drawDebug (Stage stage) {
		if (!LibgdxToolkit.drawDebug) return;
		drawDebug(stage.getActors(), stage.getSpriteBatch());
	}

	static private void drawDebug (Array<Actor> actors, SpriteBatch batch) {
		for (int i = 0, n = actors.size; i < n; i++) {
			Actor actor = actors.get(i);
			if (actor instanceof Table) ((Table)actor).layout.drawDebug(batch);
			if (actor instanceof Group) drawDebug(((Group)actor).getChildren(), batch);
		}
	}
}

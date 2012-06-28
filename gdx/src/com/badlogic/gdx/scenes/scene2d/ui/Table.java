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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table.LibgdxToolkit.DebugRect;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Toolkit;
import com.esotericsoftware.tablelayout.Value;

/** A group that sizes and positions children using table constraints.
 * <p>
 * The preferred and minimum sizes are that of the children when laid out in columns and rows.
 * @author Nathan Sweet */
public class Table extends WidgetGroup {
	static {
		Toolkit.instance = new LibgdxToolkit();
	}

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
			tableBounds.x += layout.getPadLeft().width(this);
			tableBounds.y += layout.getPadBottom().height(this);
			tableBounds.width -= tableBounds.x + layout.getPadRight().width(this);
			tableBounds.height -= tableBounds.y + layout.getPadTop().height(this);
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

	/** Alignment of the table within the actor being laid out. Set to {@link Align#center}, {@link Align#top}, {@link Align#bottom}
	 * , {@link Align#left} , {@link Align#right}, or any combination of those.
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

	/** Sets the alignment of the table within the actor being laid out to {@link Align#center}.
	 * @see TableLayout#center() */
	public Table center () {
		layout.center();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#top}.
	 * @see TableLayout#top() */
	public Table top () {
		layout.top();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#left}.
	 * @see TableLayout#left() */
	public Table left () {
		layout.left();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#bottom}.
	 * @see TableLayout#bottom() */
	public Table bottom () {
		layout.bottom();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#right}.
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

	/** The libgdx implementation of the table layout functionality.
	 * @author Nathan Sweet */
	static class LibgdxToolkit extends Toolkit<Actor, Table, TableLayout> {
		static boolean drawDebug;

		public void addChild (Actor parent, Actor child, String layoutString) {
			child.remove();
			try {
				parent.getClass().getMethod("setWidget", Actor.class).invoke(parent, child);
				return;
			} catch (InvocationTargetException ex) {
				throw new RuntimeException("Error calling setWidget.", ex);
			} catch (Exception ignored) {
			}
			((Group)parent).addActor(child);
		}

		public void removeChild (Actor parent, Actor child) {
			((Group)parent).removeActor(child);
		}

		public float getMinWidth (Actor actor) {
			if (actor instanceof Layout) return ((Layout)actor).getMinWidth();
			return actor.getWidth();
		}

		public float getMinHeight (Actor actor) {
			if (actor instanceof Layout) return ((Layout)actor).getMinHeight();
			return actor.getHeight();
		}

		public float getPrefWidth (Actor actor) {
			if (actor instanceof Layout) return ((Layout)actor).getPrefWidth();
			return actor.getWidth();
		}

		public float getPrefHeight (Actor actor) {
			if (actor instanceof Layout) return ((Layout)actor).getPrefHeight();
			return actor.getHeight();
		}

		public float getMaxWidth (Actor actor) {
			if (actor instanceof Layout) return ((Layout)actor).getMaxWidth();
			return 0;
		}

		public float getMaxHeight (Actor actor) {
			if (actor instanceof Layout) return ((Layout)actor).getMaxHeight();
			return 0;
		}

		public float getWidth (Actor widget) {
			return widget.getWidth();
		}

		public float getHeight (Actor widget) {
			return widget.getHeight();
		}

		public void clearDebugRectangles (TableLayout layout) {
			if (layout.debugRects != null) layout.debugRects.clear();
		}

		public void addDebugRectangle (TableLayout layout, Debug type, float x, float y, float w, float h) {
			drawDebug = true;
			if (layout.debugRects == null) layout.debugRects = new Array();
			layout.debugRects.add(new DebugRect(type, x, layout.getTable().getHeight() - y, w, h));
		}

		static class DebugRect extends Rectangle {
			final Debug type;

			public DebugRect (Debug type, float x, float y, float width, float height) {
				super(x, y, width, height);
				this.type = type;
			}
		}
	}

	/** The libgdx implementation to apply a table layout.
	 * @author Nathan Sweet */
	class TableLayout extends BaseTableLayout<Actor, Table, TableLayout, LibgdxToolkit> {
		Array<DebugRect> debugRects;
		private ImmediateModeRenderer debugRenderer;

		public TableLayout () {
			super((LibgdxToolkit)Toolkit.instance);
		}

		public void layout () {
			Table table = getTable();
			float width = table.getWidth();
			float height = table.getHeight();

			super.layout(0, 0, width, height);

			List<Cell> cells = getCells();
			for (int i = 0, n = cells.size(); i < n; i++) {
				Cell c = cells.get(i);
				if (c.getIgnore()) continue;
				Actor actor = (Actor)c.getWidget();
				float widgetHeight = c.getWidgetHeight();
				actor.setBounds(c.getWidgetX(), height - c.getWidgetY() - widgetHeight, c.getWidgetWidth(), widgetHeight);
			}
			Array<Actor> children = table.getChildren();
			for (int i = 0, n = children.size; i < n; i++) {
				Actor child = children.get(i);
				if (child instanceof Layout) {
					Layout layout = (Layout)child;
					layout.invalidate();
					layout.validate();
				}
			}
		}

		/** Invalides the layout of this widget and every parent widget to the root of the hierarchy. */
		public void invalidateHierarchy () {
			super.invalidate();
			getTable().invalidateHierarchy();
		}

		private void toStageCoordinates (Actor actor, Vector2 point) {
			point.x += actor.getX();
			point.y += actor.getY();
			toStageCoordinates(actor.getParent(), point);
		}

		public void drawDebug (SpriteBatch batch) {
			if (getDebug() == Debug.none || debugRects == null) return;
			if (debugRenderer == null) {
				if (Gdx.graphics.isGL20Available())
					debugRenderer = new ImmediateModeRenderer20(64, false, true, 0);
				else
					debugRenderer = new ImmediateModeRenderer10(64);
			}

			float x = 0, y = 0;
			Actor parent = getTable();
			while (parent != null) {
				if (parent instanceof Group) {
					x += parent.getX();
					y += parent.getY();
				}
				parent = parent.getParent();
			}

			debugRenderer.begin(batch.getProjectionMatrix(), GL10.GL_LINES);
			for (int i = 0, n = debugRects.size; i < n; i++) {
				DebugRect rect = debugRects.get(i);
				float x1 = x + rect.x;
				float y1 = y + rect.y - rect.height;
				float x2 = x1 + rect.width;
				float y2 = y1 + rect.height;
				float r = rect.type == Debug.cell ? 1 : 0;
				float g = rect.type == Debug.widget ? 1 : 0;
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

				if (debugRenderer.getNumVertices() == 64) {
					debugRenderer.end();
					debugRenderer.begin(batch.getProjectionMatrix(), GL10.GL_LINES);
				}
			}
			debugRenderer.end();
		}
	}
}

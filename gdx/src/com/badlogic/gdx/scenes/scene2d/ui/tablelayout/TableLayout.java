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

import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.actors.Button;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.scenes.scene2d.actors.Label;
import com.badlogic.gdx.utils.Array;

public class TableLayout extends BaseTableLayout<Actor> {
	static {
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.");
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.actors.");
	}

	static public BitmapFont defaultFont;
	static private HashMap<String, BitmapFont> fonts = new HashMap();

	/** The atlas to use to find texture regions. */
	public TextureAtlas atlas;

	Table table;
	boolean needsLayout = true;

	private Array<DebugRect> debugRects;
	private ImmediateModeRenderer debugRenderer;

	public void parse (FileHandle file) {
		super.parse(file.readString());
	}

	/**
	 * Calls {@link #register(String, Actor)} with the name of the actor.
	 */
	public Actor register (Actor actor) {
		if (actor.name == null) throw new IllegalArgumentException("Actor must have a name: " + actor.getClass());
		return register(actor.name, actor);
	}

	/**
	 * Finds the texture region in the {@link #atlas}, creates an {@link Image} and registers it with the specified name.
	 */
	public Actor registerImage (String name) {
		return register(new Image(name, atlas.findRegion(name)));
	}

	public Actor getWidget (String name) {
		Actor actor = super.getWidget(name);
		if (actor == null) actor = table.findActor(name);
		return actor;
	}

	public void layout () {
		if (!needsLayout) return;
		needsLayout = false;

		tableLayoutWidth = (int)table.width;
		int height = (int)table.height;
		tableLayoutHeight = height;

		super.layout();

		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			Actor actor = (Actor)c.widget;
			actor.x = c.widgetX;
			actor.y = height - c.widgetY - c.widgetHeight;
			actor.width = c.widgetWidth;
			actor.height = c.widgetHeight;
			if (actor instanceof Layout) {
				Layout layout = (Layout)actor;
				layout.invalidate();
				layout.layout();
			}
		}
	}

	public Actor wrap (Object object) {
		if (object instanceof String) {
			if (defaultFont == null) throw new IllegalStateException("No default font has been set.");
			return new Label(null, defaultFont, (String)object);
		}
		if (object == null) return new Group();
		return super.wrap(object);
	}

	public Actor newWidget (String className) {
		AtlasRegion region = atlas.findRegion(className);
		if (region != null) return new Image(className, region);
		if (className.equals("button")) return new Button(null);
		return super.newWidget(className);
	}

	public void setProperty (Actor object, String name, List<String> values) {
		if (object instanceof Label) {
			Label label = ((Label)object);
			String value = values.get(0);
			if (name.equals("wrappedText") && values.size() > 0) {
				HAlignment alignment = HAlignment.LEFT;
				if (values.size() > 1) alignment = HAlignment.valueOf(values.get(1).toUpperCase());
				label.setWrappedText((String)values.get(0), alignment);
				return;
			}
			if (name.equals("font")) {
				label.setFont(getFont(value));
				return;
			}
		}

		if (object instanceof Button) {
			Button button = (Button)object;
			if (name.equals("up")) {
				button.unpressedRegion = atlas.findRegion(values.get(0));
				return;
			}
			if (name.equals("down")) {
				button.pressedRegion = atlas.findRegion(values.get(0));
				return;
			}
		}

		super.setProperty(object, name, values);
	}

	public BaseTableLayout newTableLayout () {
		TableLayout layout = new Table().layout;
		layout.setParent(this);
		return layout;
	}

	public Actor newStack () {
		return new Stack();
	}

	public void addChild (Actor parent, Actor child, String layoutString) {
		if (child.parent != null) child.remove();
		((Group)parent).addActor(child);
	}

	public void removeChild (Actor parent, Actor child) {
		((Group)parent).removeActor(child);
	}

	public int getMinWidth (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefWidth();
		return (int)actor.width;
	}

	public int getMinHeight (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefHeight();
		return (int)actor.height;
	}

	public int getPrefWidth (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefWidth();
		return (int)actor.width;
	}

	public int getPrefHeight (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefHeight();
		return (int)actor.height;
	}

	public int getMaxWidth (Actor actor) {
		return 0;
	}

	public int getMaxHeight (Actor actor) {
		return 0;
	}

	public void invalidate () {
		needsLayout = true;
	}

	public void drawDebug () {
		if (debug == null || debugRects == null) return;
		if (debugRenderer == null) debugRenderer = new ImmediateModeRenderer(64);

		float x = 0, y = table.height;
		Actor parent = table;
		while (parent != null) {
			if (parent instanceof Table) {
				x += parent.x;
				y += parent.y;
			} else {
				x += parent.x;
				y += parent.y;
			}
			parent = parent.parent;
		}

		int viewHeight = Gdx.graphics.getHeight();

		debugRenderer.begin(GL10.GL_LINES);
		for (int i = 0, n = debugRects.size; i < n; i++) {
			DebugRect rect = debugRects.get(i);
			float x1 = x + rect.x;
			float y1 = y - rect.y - rect.height;
			float x2 = x1 + rect.width;
			float y2 = y1 + rect.height;
			float r = rect.type.equals(DEBUG_CELL) ? 1 : 0;
			float g = rect.type.equals(DEBUG_WIDGET) ? 1 : 0;
			float b = rect.type.equals(DEBUG_TABLE) ? 1 : 0;

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
				debugRenderer.begin(GL10.GL_LINES);
			}
		}
		debugRenderer.end();
	}

	public void clearDebugRectangles () {
		if (debugRects != null) debugRects.clear();
	}

	public void addDebugRectangle (String type, int x, int y, int w, int h) {
		if (debugRects == null) debugRects = new Array();
		debugRects.add(new DebugRect(type, x, y, w, h));
	}

	public Table getTable () {
		return table;
	}

	/**
	 * Sets the name of a font.
	 */
	static public void registerFont (String name, BitmapFont font) {
		fonts.put(name, font);
		if (defaultFont == null) defaultFont = font;
	}

	static public BitmapFont getFont (String name) {
		BitmapFont font = fonts.get(name);
		if (font == null) throw new IllegalArgumentException("Font not found: " + name);
		return font;
	}

	class Stack extends Group implements Layout {
		private boolean needsLayout = true;

		public void layout () {
			if (!needsLayout) return;
			needsLayout = false;
			for (int i = 0, n = children.size(); i < n; i++) {
				Actor actor = children.get(i);
				actor.width = width;
				actor.height = height;
				if (actor instanceof Layout) {
					Layout layout = (Layout)actor;
					layout.invalidate();
					layout.layout();
				}
			}
		}

		public void invalidate () {
			needsLayout = true;
		}

		public float getPrefWidth () {
			float width = 0;
			for (int i = 0, n = children.size(); i < n; i++)
				width = Math.max(width, TableLayout.this.getPrefWidth(children.get(i)));
			return width * scaleX;
		}

		public float getPrefHeight () {
			float height = 0;
			for (int i = 0, n = children.size(); i < n; i++)
				height = Math.max(height, TableLayout.this.getPrefHeight(children.get(i)));
			return height * scaleY;
		}
	}

	static private class DebugRect extends Rectangle {
		final String type;

		public DebugRect (String type, int x, int y, int width, int height) {
			super(x, y, width, height);
			this.type = type;
		}
	}
}

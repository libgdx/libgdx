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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.LibgdxToolkit.DebugRect;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;

/** @author Nathan Sweet */
public class TableLayout extends BaseTableLayout<Actor, Table, TableLayout, LibgdxToolkit> {
	/** The atlas to use to find texture regions. */
	public TextureAtlas atlas;

	boolean needsLayout = true;
	Array<DebugRect> debugRects;
	private ImmediateModeRenderer debugRenderer;

	public TableLayout () {
		super(LibgdxToolkit.instance);
	}

	public TableLayout (LibgdxToolkit toolkit) {
		super(toolkit);
	}

	/** Calls {@link #register(String, Actor)} with the name of the actor. */
	public Actor register (Actor actor) {
		if (actor.name == null) throw new IllegalArgumentException("Actor must have a name: " + actor.getClass());
		return register(actor.name, actor);
	}

	/** Finds the texture region in the {@link #atlas}, creates an {@link Image} and registers it with the specified name. */
	public Actor registerImage (String name) {
		return register(new Image(name, atlas.findRegion(name)));
	}

	public Actor getWidget (String name) {
		Actor actor = super.getWidget(name);
		if (actor == null) actor = getTable().findActor(name);
		return actor;
	}

	public void layout () {
		if (!needsLayout) return;
		needsLayout = false;

		Table table = getTable();
		setLayoutSize(0, 0, (int)table.width, (int)table.height);

		super.layout();

		List<Cell> cells = getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.getIgnore()) continue;
			Actor actor = (Actor)c.getWidget();
			actor.x = c.getWidgetX();
			int widgetHeight = c.getWidgetHeight();
			actor.y = table.height - c.getWidgetY() - widgetHeight;
			actor.width = c.getWidgetWidth();
			actor.height = widgetHeight;
			if (actor instanceof Layout) {
				Layout layout = (Layout)actor;
				layout.invalidate();
				layout.layout();
			}
		}
	}

	/** Invalidates the layout, forcing the next call to {@link #layout()} to relayout. If a widget is resized or otherwise changed
	 * in a way that affects its layout, {@link #invalidate()} should be called. */
	public void invalidate () {
		needsLayout = true;
	}

	/** Invalides the layout of this widget and every parent widget to the root of the hierarchy. */
	public void invalidateHierarchy () {
		invalidate();
		Actor parent = getTable().parent;
		while (parent != null) {
			if (parent instanceof Layout) ((Layout)parent).invalidate();
			parent = parent.parent;
		}
	}

	public void drawDebug (SpriteBatch batch) {
		if (getDebug() == DEBUG_NONE || debugRects == null) return;
		if (debugRenderer == null) {
			if (Gdx.graphics.isGL20Available())
				debugRenderer = new ImmediateModeRenderer20(64, false, true, 0);
			else
				debugRenderer = new ImmediateModeRenderer10(64);
		}

		Table table = getTable();
		Actor parent = table.parent;
		float x = table.x, y = 0;
		while (parent != null) {
			if (parent instanceof Group) {
				x += parent.x;
				y += parent.y;
			}
			parent = parent.parent;
		}
		y = table.y + table.height - y;

		int viewHeight = Gdx.graphics.getHeight();

		debugRenderer.begin(batch.getProjectionMatrix(), GL10.GL_LINES);
		for (int i = 0, n = debugRects.size; i < n; i++) {
			DebugRect rect = debugRects.get(i);
			float x1 = x + rect.x;
			float y1 = y - rect.y - rect.height;
			float x2 = x1 + rect.width;
			float y2 = y1 + rect.height;
			float r = (rect.type & DEBUG_CELL) != 0 ? 1 : 0;
			float g = (rect.type & DEBUG_WIDGET) != 0 ? 1 : 0;
			float b = (rect.type & DEBUG_TABLE) != 0 ? 1 : 0;

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

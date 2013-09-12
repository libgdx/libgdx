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

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Toolkit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.TableToolkit.DebugRect;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Array;

/** The libgdx implementation to apply a table layout.
 * @author Nathan Sweet */
class TableLayout extends BaseTableLayout<Actor, Table, TableLayout, TableToolkit> {
	Array<DebugRect> debugRects;
	private ImmediateModeRenderer debugRenderer;
	boolean round = true;

	public TableLayout () {
		super((TableToolkit)Toolkit.instance);
	}

	public void layout () {
		Table table = getTable();
		float width = table.getWidth();
		float height = table.getHeight();

		super.layout(0, 0, width, height);

		java.util.List<Cell> cells = getCells();
		if (round) {
			for (int i = 0, n = cells.size(); i < n; i++) {
				Cell c = cells.get(i);
				if (c.getIgnore()) continue;
				float widgetWidth = Math.round(c.getWidgetWidth());
				float widgetHeight = Math.round(c.getWidgetHeight());
				float widgetX = Math.round(c.getWidgetX());
				float widgetY = height - Math.round(c.getWidgetY()) - widgetHeight;
				c.setWidgetX(widgetX);
				c.setWidgetY(widgetY);
				c.setWidgetWidth(widgetWidth);
				c.setWidgetHeight(widgetHeight);
				Actor actor = (Actor)c.getWidget();
				if (actor != null) {
					actor.setX(widgetX);
					actor.setY(widgetY);
					if (actor.getWidth() != widgetWidth || actor.getHeight() != widgetHeight) {
						actor.setWidth(widgetWidth);
						actor.setHeight(widgetHeight);
						if (actor instanceof Layout) ((Layout)actor).invalidate();
					}
				}
			}
		} else {
			for (int i = 0, n = cells.size(); i < n; i++) {
				Cell c = cells.get(i);
				if (c.getIgnore()) continue;
				float widgetWidth = c.getWidgetWidth();
				float widgetHeight = c.getWidgetHeight();
				float widgetX = c.getWidgetX();
				float widgetY = height - c.getWidgetY() - widgetHeight;
				c.setWidgetX(widgetX);
				c.setWidgetY(widgetY);
				c.setWidgetWidth(widgetWidth);
				c.setWidgetHeight(widgetHeight);
				Actor actor = (Actor)c.getWidget();
				if (actor != null) {
					actor.setX(widgetX);
					actor.setY(widgetY);
					if (actor.getWidth() != widgetWidth || actor.getHeight() != widgetHeight) {
						actor.setWidth(widgetWidth);
						actor.setHeight(widgetHeight);
						if (actor instanceof Layout) ((Layout)actor).invalidate();
					}
				}
			}
		}
		// Validate children separately from sizing actors to ensure actors without a cell are validated.
		Array<Actor> children = table.getChildren();
		for (int i = 0, n = children.size; i < n; i++) {
			Actor child = children.get(i);
			if (child instanceof Layout) ((Layout)child).validate();
		}
	}

	/** Invalidates the layout of this widget and every parent widget to the root of the hierarchy. */
	public void invalidateHierarchy () {
		super.invalidate();
		getTable().invalidateHierarchy();
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

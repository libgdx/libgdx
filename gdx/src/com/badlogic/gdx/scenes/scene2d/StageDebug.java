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

package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.ReflectionPool;

/** Renders debug rectangles for actors in a stage. The debug rectangles are requested from actors using
 * {@link Actor#getDebugRects(Array)} and are drawn rotated and scaled to match the actor.
 * @author Daniel Holderbaum */
public class StageDebug {
	static public Color debugColor = new Color(0, 1, 0, 1);

	/** @see #obtainRect() */
	static public final Pool<DebugRect> debugRectPool = new ReflectionPool(DebugRect.class);

	static private final Array<DebugRect> usedRects = new Array();

	private final Stage stage;
	private final Array<DebugRect> debugRects = new Array();
	private final ShapeRenderer shapes = new ShapeRenderer();

	private boolean invisibleActors, allActors, disabled;

	private final Vector2 topLeft = new Vector2();
	private final Vector2 topRight = new Vector2();
	private final Vector2 bottomRight = new Vector2();
	private final Vector2 bottomLeft = new Vector2();

	public StageDebug (Stage stage) {
		this.stage = stage;
	}

	/** Draws the debug rects for all actors in the stage. */
	public void draw () {
		draw(stage.getRoot());
	}

	/** Draws the debug rects for the specified actor and any children, recursively. */
	public void draw (Actor actor) {
		if (disabled) return;
		shapes.setProjectionMatrix(stage.getCamera().projection);
		shapes.setTransformMatrix(stage.getCamera().view);
		shapes.begin(ShapeType.Line);
		drawRecursive(actor);
		shapes.end();
	}

	private void drawRecursive (Actor actor) {
		if (!invisibleActors && !actor.isVisible()) return;

		if (allActors) actor.debug();

		if (actor.getDebug()) {
			actor.getDebugRects(debugRects);
			for (DebugRect debugRect : debugRects)
				drawRect(actor, debugRect);
			debugRects.clear();
			debugRectPool.freeAll(usedRects);
			usedRects.clear();
		}

		boolean draw = true;
		Rectangle scissorBounds = null;
		if (actor instanceof Group) scissorBounds = ((Group)actor).getScissorBounds();
		if (scissorBounds != null) {
			shapes.flush();
			draw = ScissorStack.pushScissors(scissorBounds);
		}
		if (draw) {
			// Children are still rendered, even if the group has no debugging enabled.
			if (actor instanceof Group) {
				Group group = (Group)actor;
				for (Actor child : group.getChildren())
					drawRecursive(child);
			}

			if (scissorBounds != null) {
				shapes.flush();
				ScissorStack.popScissors();
			}
		}
	}

	private void drawRect (Actor actor, DebugRect debugRect) {
		topLeft.set(debugRect.bottomLeft.x, debugRect.topRight.y);
		topRight.set(debugRect.topRight.x, debugRect.topRight.y);
		bottomRight.set(debugRect.topRight.x, debugRect.bottomLeft.y);
		bottomLeft.set(debugRect.bottomLeft.x, debugRect.bottomLeft.y);

		// Transform to stage coordinates using the scale, rotation and translation of the entire ancestor hierarchy.
		actor.localToStageCoordinates(topLeft);
		actor.localToStageCoordinates(topRight);
		actor.localToStageCoordinates(bottomRight);
		actor.localToStageCoordinates(bottomLeft);

		shapes.setColor(debugRect.color);
		shapes.line(topLeft, topRight);
		shapes.line(topRight, bottomRight);
		shapes.line(bottomRight, bottomLeft);
		shapes.line(bottomLeft, topLeft);
	}

	/** If true, debug rects will be drawn for actors that are not visible ({@link Actor#isVisible()} is false for the actor or any
	 * parent). Default is false. */
	public void setInvisibleActors (boolean invisibleActors) {
		this.invisibleActors = invisibleActors;
	}

	/** If true, debug rects are rendered even for actors where {@link Actor#getDebug()} is false. */
	public void setAllActors (boolean allActors) {
		if (allActors == this.allActors) return;
		Actor.debugEnabled += allActors ? 1 : -1;
		this.allActors = allActors;
	}

	/** If true, no debug rects will be rendered. */
	public void setDisabled (boolean disabled) {
		this.disabled = disabled;
	}

	/** Returns a pooled debug rect that is automatically freed after drawing. The pool can be {@link #debugRectPool accessed
	 * directly} to obtain debug rects that are not freed after drawing. */
	static public DebugRect obtainRect () {
		DebugRect rect = debugRectPool.obtain();
		usedRects.add(rect);
		return rect;
	}

	static public class DebugRect implements Poolable {
		public final Vector2 bottomLeft = new Vector2();
		public final Vector2 topRight = new Vector2();
		public final Color color;

		public DebugRect () {
			color = new Color(debugColor);
		}

		@Override
		public void reset () {
			bottomLeft.setZero();
			topRight.setZero();
			color.set(debugColor);
		}
	}
}

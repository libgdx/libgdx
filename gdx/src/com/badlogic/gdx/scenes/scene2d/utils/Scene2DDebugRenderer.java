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

package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/** This debugging utility takes a {@link Stage} and renders all {@link Actor}s bounding boxes via a {@link ShapeRenderer}. The
 * bounding boxes are not axis-aligned but rotated and scaled just like the actor. All you need to do is calling the
 * {@link #render()} method each frame.
 * 
 * @author Daniel Holderbaum */
public class Scene2DDebugRenderer {

	/** Set to {@code true} if you want to render the actors that are not visible ({@code visible=false} of the actor or any parent)
	 * as well. */
	public boolean renderInvisibleActors = false;

	/** Set to {@code false} to not render group actors. They usually act only as invisible in the scene graph. */
	public boolean renderGroups = true;

	/** Set to {@code false} to not render "leaf" actors. Those are the ones that are no {@link Group}s. */
	public boolean renderActors = true;

	/** Set to {@code false} to not render table cells. This will not prevent the cell's actor to be rendered. */
	public boolean renderCells = true;

	/** Set to {@code false} to not render the table outlines. This will not prevent the cells to be rendered. */
	public boolean renderTable = true;

	/** Used for the outline of any {@link Actor} that is not a {@link Group} of any kind. */
	public Color actorColor = new Color(1, 1, 1, 1);

	/** Used in case the actor is a {@link Group}, but not a {@link Table}. */
	public Color groupColor = new Color(1, 0, 0, 1);

	/** Used for the outline of the individual {@link Cell}s of a {@link Table}. */
	public Color cellColor = new Color(0, 1, 0, 1);

	/** Used for the outline of a {@link Table}. */
	public Color tableColor = new Color(0, 0, 1, 1);

	private Stage stage;

	private ShapeRenderer shapeRenderer;

	public Scene2DDebugRenderer (Stage stage) {
		this.stage = stage;
		this.shapeRenderer = new ShapeRenderer();
	}

	/** Renders the bounding boxes of all actors of the stage. */
	public void render () {
		shapeRenderer.setProjectionMatrix(stage.getCamera().projection);
		shapeRenderer.setTransformMatrix(stage.getCamera().view);
		shapeRenderer.begin(ShapeType.Line);
		for (Actor actor : stage.getActors()) {
			renderRecursive(actor);
		}
		shapeRenderer.end();
	}

	/** Acts like a switch and delegates the real rendering call to the method with the most precise type. */
	private void renderRecursive (Actor actor) {
		if (renderInvisibleActors || isActorVisible(actor)) {
			if (actor instanceof Table) {
				Table table = (Table)actor;
				if (renderTable) {
					render(table);
				}
				for (Cell<Actor> cell : table.getCells()) {
					if (renderCells) {
						render(cell);
					}
					if (cell.hasActor()) {
						renderRecursive(cell.getActor());
					}
				}
			} else if (actor instanceof Group) {
				Group group = (Group)actor;
				if (renderGroups) {
					render(group);
				}
				for (Actor child : group.getChildren()) {
					renderRecursive(child);
				}
			} else {
				if (renderActors) {
					render(actor);
				}
			}
		}
	}

	private Vector2 topLeft = new Vector2();
	private Vector2 topRight = new Vector2();
	private Vector2 bottomRight = new Vector2();
	private Vector2 bottomLeft = new Vector2();

	/** Works for both {@link Group} and {@link Actor}. */
	private void render (Actor actor) {
		topLeft.set(0, actor.getHeight());
		topRight.set(actor.getWidth(), actor.getHeight());
		bottomRight.set(actor.getWidth(), 0);
		bottomLeft.set(0, 0);

		Color color = getColorByType(actor);
		renderBoundingBox(actor, topLeft, topRight, bottomRight, bottomLeft, color);
	}

	private void render (Cell cell) {
		Table table = cell.getTable();

		// render an outline of the cell
		// it's an outline around the actor + the cell's padding
		topLeft.set(cell.getActorX() - cell.getPadLeft(), cell.getActorY() + cell.getActorHeight() + cell.getPadTop());
		topRight.set(cell.getActorX() + cell.getActorWidth() + cell.getPadRight(),
			cell.getActorY() + cell.getActorHeight() + cell.getPadTop());
		bottomRight.set(cell.getActorX() + cell.getActorWidth() + cell.getPadRight(), cell.getActorY() - cell.getPadBottom());
		bottomLeft.set(cell.getActorX() - cell.getPadLeft(), cell.getActorY() - cell.getPadBottom());

		renderBoundingBox(table, topLeft, topRight, bottomRight, bottomLeft, cellColor);
	}

	private void render (Table table) {
		// table outline
		topLeft.set(0, table.getHeight());
		topRight.set(table.getWidth(), table.getHeight());
		bottomRight.set(table.getWidth(), 0);
		bottomLeft.set(0, 0);

		renderBoundingBox(table, topLeft, topRight, bottomRight, bottomLeft, tableColor);

		// table outline minus the table's padding
		topLeft.set(table.getPadLeft(), table.getHeight() - table.getPadTop());
		topRight.set(table.getWidth() - table.getPadRight(), table.getHeight() - table.getPadTop());
		bottomRight.set(table.getWidth() - table.getPadRight(), table.getPadBottom());
		bottomLeft.set(table.getPadLeft(), table.getPadBottom());

		renderBoundingBox(table, topLeft, topRight, bottomRight, bottomLeft, tableColor);
	}

	private void renderBoundingBox (Actor actor, Vector2 topLeft, Vector2 topRight, Vector2 bottomRight, Vector2 bottomLeft,
		Color color) {
		// transform to stage coordinates using offsets, scales,
		// rotations etc of the whole ancestor hierarchy until the root
		actor.localToStageCoordinates(topLeft);
		actor.localToStageCoordinates(topRight);
		actor.localToStageCoordinates(bottomRight);
		actor.localToStageCoordinates(bottomLeft);

		shapeRenderer.setColor(color);
		shapeRenderer.line(topLeft, topRight);
		shapeRenderer.line(topRight, bottomRight);
		shapeRenderer.line(bottomRight, bottomLeft);
		shapeRenderer.line(bottomLeft, topLeft);
	}

	/** Returns {@code true} in case the given actor and all of its ancestors are visible. {@code False} otherwise. */
	private boolean isActorVisible (Actor actor) {
		Actor currentActor = actor;
		while (currentActor != null) {
			if (currentActor.isVisible() == false) {
				return false;
			} else {
				currentActor = currentActor.getParent();
			}
		}

		return true;
	}

	private Color getColorByType (Actor actor) {
		Color color;
		if (actor instanceof Group) {
			color = groupColor;
		} else {
			color = actorColor;
		}
		return color;
	}
}

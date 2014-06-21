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

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

/** This debugging utility takes a {@link Stage} and renders all {@link Actor}s bounding boxes via a {@link ShapeRenderer}. The
 * bounding boxes are not axis-aligned but rotated and scaled just like the actor. All you need to do is calling the
 * {@link #render()} method each frame.
 * 
 * @author Daniel Holderbaum */
public class Scene2DDebugRenderer {

	/** Set to {@code true} if you want to render the actors that are not visible ({@code visible=false} of the actor or any parent)
	 * as well. */
	public boolean renderInvisibleActors = false;

	/** Set to {@code true} to also render group actors. They usually act only as invisible in the scene graph. */
	public boolean renderGroups = true;

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
			render(actor);
		}
		shapeRenderer.end();
	}

	private Vector2 topLeft = new Vector2();
	private Vector2 topRight = new Vector2();
	private Vector2 bottomRight = new Vector2();
	private Vector2 bottomLeft = new Vector2();

	private void render (Actor actor) {
		if (renderInvisibleActors || isActorVisible(actor)) {
			if (renderGroups || !(actor instanceof Group)) {
				// local coordinates
				topLeft.set(0, actor.getHeight());
				topRight.set(actor.getWidth(), actor.getHeight());
				bottomRight.set(actor.getWidth(), 0);
				bottomLeft.set(0, 0);

				// transform to stage coordinates using offsets, scales,
				// rotations etc of the whole ancestor hierarchy until the root
				actor.localToStageCoordinates(topLeft);
				actor.localToStageCoordinates(topRight);
				actor.localToStageCoordinates(bottomRight);
				actor.localToStageCoordinates(bottomLeft);

				shapeRenderer.line(topLeft, topRight);
				shapeRenderer.line(topRight, bottomRight);
				shapeRenderer.line(bottomRight, bottomLeft);
				shapeRenderer.line(bottomLeft, topLeft);
			}
		}

		if (actor instanceof Group) {
			for (Actor child : ((Group)actor).getChildren()) {
				render(child);
			}
		}
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
}

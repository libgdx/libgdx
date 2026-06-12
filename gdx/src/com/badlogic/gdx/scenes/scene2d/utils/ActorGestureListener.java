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

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;

/** A listener that combines {@link GestureDetector} with scene2d event handling for actors. The actor is stored so that the
 * gesture methods can be called with the actor available, allowing a single listener to be used with multiple actors.
 * @author Nathan Sweet */
public class ActorGestureListener implements EventListener {
	static final Vector2 tmpCoords = new Vector2(), tmpCoords2 = new Vector2();

	final GestureDetector detector;
	private Actor actor;
	private InputEvent event;
	private Actor touchDownTarget;

	/** Uses the default settings of {@link GestureDetector}. */
	public ActorGestureListener () {
		this(20, 0.4f, 1.1f, 0.15f);
	}

	public ActorGestureListener (float halfTapSquareSize, float tapCountInterval, float longPressDuration, float maxFlingDelay) {
		detector = new GestureDetector(halfTapSquareSize, tapCountInterval, longPressDuration, maxFlingDelay, new GestureAdapter() {
			@Override
			public boolean tap (float stageX, float stageY, int count, int button) {
				if (actor == null) return false;
				actor.stageToLocalCoordinates(tmpCoords.set(stageX, stageY));
				return ActorGestureListener.this.tap(event, tmpCoords.x, tmpCoords.y, count, button);
			}

			@Override
			public boolean longPress (float stageX, float stageY) {
				if (actor == null) return false;
				actor.stageToLocalCoordinates(tmpCoords.set(stageX, stageY));
				return ActorGestureListener.this.longPress(event, tmpCoords.x, tmpCoords.y);
			}

			@Override
			public boolean fling (float velocityX, float velocityY, int button) {
				if (actor == null) return false;
				return ActorGestureListener.this.fling(event, velocityX, velocityY, button);
			}

			@Override
			public boolean pan (float stageX, float stageY, float deltaX, float deltaY) {
				if (actor == null) return false;
				actor.stageToLocalCoordinates(tmpCoords.set(stageX, stageY));
				return ActorGestureListener.this.pan(event, tmpCoords.x, tmpCoords.y, deltaX, deltaY);
			}

			@Override
			public boolean panStop (float stageX, float stageY, int pointer, int button) {
				if (actor == null) return false;
				actor.stageToLocalCoordinates(tmpCoords.set(stageX, stageY));
				return ActorGestureListener.this.panStop(event, tmpCoords.x, tmpCoords.y, pointer, button);
			}

			@Override
			public boolean zoom (float initialDistance, float distance) {
				if (actor == null) return false;
				return ActorGestureListener.this.zoom(event, initialDistance, distance);
			}

			@Override
			public boolean pinch (Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
				if (actor == null) return false;
				return ActorGestureListener.this.pinch(event, initialPointer1, initialPointer2, pointer1, pointer2);
			}

			@Override
			public void pinchStop () {
				ActorGestureListener.this.pinchStop(event);
			}
		});
	}

	@Override
	public boolean handle (Event e) {
		if (!(e instanceof InputEvent)) return false;
		InputEvent event = (InputEvent)e;
		this.event = event;

		switch (event.getType()) {
		case touchDown:
			actor = event.getListenerActor();
			touchDownTarget = actor;
			detector.touchDown(event.getStageX(), event.getStageY(), event.getPointer(), event.getButton());
			touchDown(event, event.getStageX(), event.getStageY(), event.getPointer(), event.getButton());
			return true;
		case touchUp:
			// Save actor reference before any reentrant calls (e.g., cancelTouchFocus) might set actor to null.
			Actor touchUpActor = event.getListenerActor();
			touchDownTarget = null;
			detector.touchUp(event.getStageX(), event.getStageY(), event.getPointer(), event.getButton());
			if (touchUpActor == null) return false;
			touchUpActor.stageToLocalCoordinates(tmpCoords.set(event.getStageX(), event.getStageY()));
			touchUp(event, tmpCoords.x, tmpCoords.y, event.getPointer(), event.getButton());
			actor = null;
			this.event = null;
			touchDownTarget = null;
			return true;
		case touchDragged:
			if (actor == null) return false;
			detector.touchDragged(event.getStageX(), event.getStageY(), event.getPointer());
			actor.stageToLocalCoordinates(tmpCoords.set(event.getStageX(), event.getStageY()));
			touchDragged(event, tmpCoords.x, tmpCoords.y, event.getPointer());
			return true;
		}
		return false;
	}

	/** Called when a mouse button or finger is pressed on the actor. The coordinates are in the actor's local coordinate system.
	 * The default implementation does nothing. */
	public void touchDown (InputEvent event, float x, float y, int pointer, int button) {
	}

	/** Called when a mouse button or finger is released on the actor. The coordinates are in the actor's local coordinate system.
	 * The default implementation does nothing. */
	public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
	}

	/** Called when a mouse button or finger is dragged on the actor. The coordinates are in the actor's local coordinate system.
	 * The default implementation does nothing. */
	public void touchDragged (InputEvent event, float x, float y, int pointer) {
	}

	/** Called when the user taps the actor. The coordinates are in the actor's local coordinate system. The default implementation
	 * does nothing. */
	public boolean tap (InputEvent event, float x, float y, int count, int button) {
		return false;
	}

	/** Called when the user long presses the actor. The coordinates are in the actor's local coordinate system. The default
	 * implementation does nothing. */
	public boolean longPress (InputEvent event, float x, float y) {
		return false;
	}

	/** Called when the user flings the actor. The default implementation does nothing. */
	public boolean fling (InputEvent event, float velocityX, float velocityY, int button) {
		return false;
	}

	/** Called when the user pans the actor. The coordinates are in the actor's local coordinate system. The default implementation
	 * does nothing. */
	public boolean pan (InputEvent event, float x, float y, float deltaX, float deltaY) {
		return false;
	}

	/** Called when the user stops panning the actor. The coordinates are in the actor's local coordinate system. The default
	 * implementation does nothing. */
	public boolean panStop (InputEvent event, float x, float y, int pointer, int button) {
		return false;
	}

	/** Called when the user zooms the actor. The default implementation does nothing. */
	public boolean zoom (InputEvent event, float initialDistance, float distance) {
		return false;
	}

	/** Called when the user pinches the actor. The default implementation does nothing. */
	public boolean pinch (InputEvent event, Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		return false;
	}

	/** Called when the user stops pinching the actor. The default implementation does nothing. */
	public void pinchStop (InputEvent event) {
	}

	public GestureDetector getGestureDetector () {
		return detector;
	}

	public Actor getActor () {
		return actor;
	}

	public InputEvent getEvent () {
		return event;
	}

	public Actor getTouchDownTarget () {
		return touchDownTarget;
	}
}
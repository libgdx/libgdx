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
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

/** Detects tap, longPress, fling, pan, zoom, and pinch gestures on an actor.
 * @see GestureDetector
 * @author Nathan Sweet */
public class ActorGestureListener implements EventListener {
	private final GestureDetector detector = new GestureDetector(new GestureAdapter() {
		private final Vector2 initialPointer1 = new Vector2(), initialPointer2 = new Vector2();
		private final Vector2 pointer1 = new Vector2(), pointer2 = new Vector2();

		public boolean tap (float stageX, float stageY, int count) {
			actor.stageToLocalCoordinates(Vector2.tmp.set(stageX, stageY));
			ActorGestureListener.this.tap(event, Vector2.tmp.x, Vector2.tmp.y, count);
			return true;
		}

		public boolean longPress (float stageX, float stageY) {
			actor.stageToLocalCoordinates(Vector2.tmp.set(stageX, stageY));
			return ActorGestureListener.this.longPress(actor, Vector2.tmp.x, Vector2.tmp.y);
		}

		public boolean fling (float velocityX, float velocityY) {
			ActorGestureListener.this.fling(event, velocityX, velocityY);
			return true;
		}

		public boolean pan (float stageX, float stageY, float deltaX, float deltaY) {
			actor.stageToLocalCoordinates(Vector2.tmp.set(stageX, stageY));
			ActorGestureListener.this.pan(event, Vector2.tmp.x, Vector2.tmp.y, deltaX, deltaY);
			return true;
		}

		public boolean zoom (float initialDistance, float distance) {
			ActorGestureListener.this.zoom(event, initialDistance, distance);
			return true;
		}

		public boolean pinch (Vector2 stageInitialPointer1, Vector2 stageInitialPointer2, Vector2 stagePointer1,
			Vector2 stagePointer2) {
			actor.stageToLocalCoordinates(initialPointer1.set(stageInitialPointer1));
			actor.stageToLocalCoordinates(initialPointer2.set(stageInitialPointer2));
			actor.stageToLocalCoordinates(pointer1.set(stagePointer1));
			actor.stageToLocalCoordinates(pointer2.set(stagePointer2));
			ActorGestureListener.this.pinch(event, initialPointer1, initialPointer2, pointer1, pointer2);
			return true;
		}
	});

	ActorEvent event;
	Actor actor;

	public boolean handle (Event e) {
		if (!(e instanceof ActorEvent)) return false;
		ActorEvent event = (ActorEvent)e;

		switch (event.getType()) {
		case touchDown:
			actor = event.getListenerActor();
			detector.touchDown(event.getStageX(), event.getStageY(), event.getPointer(), event.getButton());
			return true;
		case touchUp:
			this.event = event;
			actor = event.getListenerActor();
			detector.touchUp(event.getStageX(), event.getStageY(), event.getPointer(), event.getButton());
			return true;
		case touchDragged:
			this.event = event;
			actor = event.getListenerActor();
			detector.touchDragged(event.getStageX(), event.getStageY(), event.getPointer());
			return true;
		}
		return false;
	}

	public void tap (ActorEvent event, float x, float y, int count) {
	}

	/** If true is returned, additional gestures will not be triggered. No event is provided because this event is triggered by time
	 * passing, not by an ActorEvent. */
	public boolean longPress (Actor actor, float x, float y) {
		return false;
	}

	public void fling (ActorEvent event, float velocityX, float velocityY) {
	}

	public void pan (ActorEvent event, float x, float y, float deltaX, float deltaY) {
	}

	public void zoom (ActorEvent event, float initialDistance, float distance) {
	}

	public void pinch (ActorEvent event, Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
	}

	public GestureDetector getGestureDetector () {
		return detector;
	}
}
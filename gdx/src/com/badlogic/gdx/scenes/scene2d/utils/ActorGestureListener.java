
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

/** Detects tap, longPress, fling, pan, zoom, and pinch gestures on an actor.
 * @see GestureDetector
 * @author Nathan Sweet */
public class ActorGestureListener implements EventListener {
	private final GestureDetector detector = new GestureDetector(new GestureAdapter() {
		private Vector2 localCoords () {
			Vector2 coords = Vector2.tmp.set(event.getStageX(), event.getStageY());
			event.getCurrentTarget().stageToLocalCoordinates(coords);
			return coords;
		}

		public boolean tap (float x, float y, int count) {
			Vector2 localCoords = localCoords();
			ActorGestureListener.this.tap(event, localCoords.x, localCoords.y, count);
			return true;
		}

		public boolean longPress (float x, float y) {
			Vector2 localCoords = localCoords();
			return ActorGestureListener.this.longPress(event, localCoords.x, localCoords.y);
		}

		public boolean fling (float velocityX, float velocityY) {
			ActorGestureListener.this.fling(event, velocityX, velocityY);
			return true;
		}

		public boolean pan (float x, float y, float deltaX, float deltaY) {
			Vector2 localCoords = localCoords();
			ActorGestureListener.this.pan(event, localCoords.x, localCoords.y, deltaX, deltaY);
			return true;
		}

		public boolean zoom (float initialDistance, float distance) {
			ActorGestureListener.this.zoom(event, initialDistance, distance);
			return true;
		}

		public boolean pinch (Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
			ActorGestureListener.this.pinch(event, initialPointer1, initialPointer2, pointer1, pointer2);
			return true;
		}
	});

	ActorEvent event;

	public boolean handle (Event e) {
		if (!(e instanceof ActorEvent)) return false;
		ActorEvent event = (ActorEvent)e;

		switch (event.getType()) {
		case touchDown:
			this.event = event;
			detector.touchDown(event.getStageX(), event.getStageY(), event.getPointer(), event.getButton());
			return true;
		case touchUp:
			this.event = event;
			detector.touchUp(event.getStageX(), event.getStageY(), event.getPointer(), event.getButton());
			return true;
		case touchDragged:
			this.event = event;
			detector.touchDragged(event.getStageX(), event.getStageY(), event.getPointer());
			return true;
		}
		return false;
	}

	public void tap (ActorEvent event, float x, float y, int count) {
	}

	/** If true is returned, additional gestures will not be triggered. */
	public boolean longPress (ActorEvent event, float x, float y) {
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

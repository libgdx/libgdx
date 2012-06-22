
package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector2;

/** Detects tap, longPress, fling, pan, zoom, and pinch gestures.
 * @see GestureDetector
 * @author Nathan Sweet */
public class ActorGestureListener extends ActorListener {
	/** The GestureDetector that is used for all ActorGestureListeners. This may be configured to customize the detector settings. */
	static public final GestureDetector detector = new GestureDetector(new GestureAdapter() {
		public boolean tap (float x, float y, int count) {
			if (event.getCurrentTarget().hit(x, y) != null) listener.tap(event, x, y, count);
			return true;
		}

		public boolean longPress (float x, float y) {
			listener.longPress(event, x, y);
			return true;
		}

		public boolean fling (float velocityX, float velocityY) {
			listener.fling(event, velocityX, velocityY);
			return true;
		}

		public boolean pan (float x, float y, float deltaX, float deltaY) {
			listener.pan(event, x, y, deltaX, deltaY);
			return true;
		}

		public boolean zoom (float initialDistance, float distance) {
			listener.zoom(event, initialDistance, distance);
			return true;
		}

		public boolean pinch (Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
			listener.pinch(event, initialPointer1, initialPointer2, pointer1, pointer2);
			return true;
		}
	});

	static ActorGestureListener listener;
	static ActorEvent event;

	public boolean touchDown (ActorEvent event, float x, float y, int pointer) {
		ActorGestureListener.listener = this;
		ActorGestureListener.event = event;
		detector.touchDown(x, y, pointer, 0);
		return true;
	}

	public boolean touchUp (ActorEvent event, float x, float y, int pointer) {
		ActorGestureListener.listener = this;
		ActorGestureListener.event = event;
		detector.touchUp(x, y, pointer, 0);
		return true;
	}

	public boolean touchDragged (ActorEvent event, float x, float y, int pointer) {
		ActorGestureListener.listener = this;
		ActorGestureListener.event = event;
		detector.touchDragged(x, y, pointer);
		return true;
	}

	public void tap (ActorEvent event, float x, float y, int count) {
	}

	public void longPress (ActorEvent event, float x, float y) {
	}

	public void fling (ActorEvent event, float velocityX, float velocityY) {
	}

	public void pan (ActorEvent event, float x, float y, float deltaX, float deltaY) {
	}

	public void zoom (ActorEvent event, float initialDistance, float distance) {
	}

	public void pinch (ActorEvent event, Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
	}
}


package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ActorEvent.Type;

public class ActorListener implements EventListener {
	Actor captureTouchUp;
	private EventListener touchUpListener;

	public boolean handle (Event e) {
		if (!(e instanceof ActorEvent)) return false;
		ActorEvent event = (ActorEvent)e;

		switch (event.getType()) {
		case keyDown:
			return keyDown(event, event.getKeyCode());
		case keyUp:
			return keyUp(event, event.getKeyCode());
		case keyTyped:
			return keyTyped(event, event.getCharacter());
		case scrolled:
			return scrolled(event, event.getScrollAmount());
		}

		Vector2 coords = event.getCurrentTarget().toLocalCoordinates(event.getStageX(), event.getStageY());

		switch (event.getType()) {
		case touchDown:
			return touchDown(event, coords.x, coords.y, event.getPointer(), event.getButton());
		case touchUp:
			return touchUp(event, coords.x, coords.y, event.getPointer(), event.getButton());
		case touchDragged:
			return touchDragged(event, coords.x, coords.y, event.getPointer());
		case touchMoved:
			return touchMoved(event, coords.x, coords.y);
		case enter:
			return enter(event, coords.x, coords.y, event.getPointer(), event.getRelatedActor());
		case exit:
			return exit(event, coords.x, coords.y, event.getPointer(), event.getRelatedActor());
		}
		return false;
	}

	/** Intercepts all the touch dragged and touch up events until a touch up event is received. Typically this is used in
	 * {@link #touchDown(ActorEvent, float, float, int, int)} to ensure the {@link #touchUp(ActorEvent, float, float, int, int)}
	 * occurs, even if it occurs outside the actor.
	 * <p>
	 * A capture listener that {@link Event#stop() stops} touchDragged and touchUp events is added to the stage root, so no other
	 * actors will receive these events. The {@link #touchDragged(ActorEvent, float, float, int)} and
	 * {@link #touchUp(ActorEvent, float, float, int, int)} methods will be called with the same current target as the specified
	 * event. */
	public void captureTouchUp (ActorEvent event) {
		if (touchUpListener == null) {
			touchUpListener = new EventListener() {
				public boolean handle (Event e) {
					if (!(e instanceof ActorEvent)) return false;
					ActorEvent event = (ActorEvent)e;
					Type type = event.getType();
					if (type != Type.touchUp && type != Type.touchDragged) return false;

					event.stop();
					event.setCurrentTarget(captureTouchUp);
					Vector2 coords = captureTouchUp.toLocalCoordinates(event.getStageX(), event.getStageY());

					if (event.getType() == Type.touchDragged) return touchDragged(event, coords.x, coords.y, event.getPointer());

					captureTouchUp = null;
					event.getStage().removeCaptureListener(this);
					return touchUp(event, coords.x, coords.y, event.getPointer(), event.getButton());
				}
			};
		}
		if (event.getStage().addCaptureListener(touchUpListener)) captureTouchUp = event.getCurrentTarget();
	}

	public boolean touchDown (ActorEvent event, float x, float y, int pointer, int button) {
		return false;
	}

	public boolean touchUp (ActorEvent event, float x, float y, int pointer, int button) {
		return false;
	}

	public boolean touchDragged (ActorEvent event, float x, float y, int pointer) {
		return false;
	}

	public boolean touchMoved (ActorEvent event, float x, float y) {
		return false;
	}

	public boolean enter (ActorEvent event, float x, float y, int pointer, Actor fromActor) {
		return false;
	}

	public boolean exit (ActorEvent event, float x, float y, int pointer, Actor toActor) {
		return false;
	}

	public boolean scrolled (ActorEvent event, int amount) {
		return false;
	}

	public boolean keyDown (ActorEvent event, int keycode) {
		return false;
	}

	public boolean keyUp (ActorEvent event, int keycode) {
		return false;
	}

	public boolean keyTyped (ActorEvent event, char character) {
		return false;
	}
}

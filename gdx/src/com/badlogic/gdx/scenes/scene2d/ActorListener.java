
package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.math.Vector2;

public class ActorListener implements EventListener {
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

		Vector2 coords = Vector2.tmp.set(event.getStageX(), event.getStageY());
		event.getContextActor().toLocalCoordinates(coords);

		switch (event.getType()) {
		case touchDown:
			return touchDown(event, coords.x, coords.y, event.getPointer(), event.getButton());
		case touchUp:
			touchUp(event, coords.x, coords.y, event.getPointer(), event.getButton());
			return true;
		case touchDragged:
			touchDragged(event, coords.x, coords.y, event.getPointer());
			return true;
		case mouseMoved:
			mouseMoved(event, coords.x, coords.y);
			return false;
		case enter:
			enter(event, coords.x, coords.y, event.getPointer(), event.getRelatedActor());
			return false;
		case exit:
			exit(event, coords.x, coords.y, event.getPointer(), event.getRelatedActor());
			return false;
		}
		return false;
	}

	public boolean touchDown (ActorEvent event, float x, float y, int pointer, int button) {
		return false;
	}

	public void touchUp (ActorEvent event, float x, float y, int pointer, int button) {
	}

	public void touchDragged (ActorEvent event, float x, float y, int pointer) {
	}

	public boolean mouseMoved (ActorEvent event, float x, float y) {
		return false;
	}

	public void enter (ActorEvent event, float x, float y, int pointer, Actor fromActor) {
	}

	public void exit (ActorEvent event, float x, float y, int pointer, Actor toActor) {
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

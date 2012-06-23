
package com.badlogic.gdx.scenes.scene2d.listeners;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public class ActorListener implements EventListener {
	public void handle (Event e) {
		if (!(e instanceof ActorEvent)) return;
		ActorEvent event = (ActorEvent)e;

		switch (event.getType()) {
		case keyDown:
			if (keyDown(event, event.getKeyCode())) event.handled();
			return;
		case keyUp:
			if (keyUp(event, event.getKeyCode())) event.handled();
			return;
		case keyTyped:
			if (keyTyped(event, event.getCharacter())) event.handled();
			return;
		case scrolled:
			if (scrolled(event, event.getScrollAmount())) event.handled();
			return;
		}

		Vector2 coords = event.getContextActor().toLocalCoordinates(event.getStageX(), event.getStageY());

		switch (event.getType()) {
		case touchDown:
			if (touchDown(event, coords.x, coords.y, event.getPointer(), event.getButton())) event.handled();
			return;
		case touchUp:
			if (touchUp(event, coords.x, coords.y, event.getPointer(), event.getButton())) event.handled();
			return;
		case touchDragged:
			if (touchDragged(event, coords.x, coords.y, event.getPointer())) event.handled();
			return;
		case touchMoved:
			touchMoved(event, coords.x, coords.y);
			return;
		case enter:
			enter(event, coords.x, coords.y, event.getPointer(), event.getRelatedActor());
			return;
		case exit:
			exit(event, coords.x, coords.y, event.getPointer(), event.getRelatedActor());
			return;
		}
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

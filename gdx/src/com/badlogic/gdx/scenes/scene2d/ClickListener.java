
package com.badlogic.gdx.scenes.scene2d;

/** Detects a click on an actor. The touch must go down over the actor and go up over the actor for the click to occur.
 * @author Nathan Sweet */
abstract public class ClickListener extends ActorListener {
	private boolean down;

	public boolean touchDown (ActorEvent event, float x, float y, int pointer, int button) {
		down = true;
		return true;
	}

	public boolean touchUp (ActorEvent event, float x, float y, int pointer, int button) {
		if (down) {
			down = false;
			if (event.getCurrentTarget().hit(x, y) != null) {
				clicked(event, x, y);
				return true;
			}
		}
		return false;
	}

	abstract public void clicked (ActorEvent event, float x, float y);
}

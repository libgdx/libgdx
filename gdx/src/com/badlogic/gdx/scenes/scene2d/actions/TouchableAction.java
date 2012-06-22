
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Sets the actor's {@link Actor#setTouchable(boolean) touchability}. 
 * @author Nathan Sweet */
public class TouchableAction extends Action {
	private boolean touchable;

	public boolean act (float delta) {
		actor.setTouchable(touchable);
		return true;
	}

	public boolean isTouchable () {
		return touchable;
	}

	public void setTouchable (boolean touchable) {
		this.touchable = touchable;
	}
}

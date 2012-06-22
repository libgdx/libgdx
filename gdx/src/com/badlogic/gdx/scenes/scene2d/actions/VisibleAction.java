
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Sets the actor's {@link Actor#setVisible(boolean) visibility}.
 * @author Nathan Sweet */
public class VisibleAction extends Action {
	private boolean visible;

	public boolean act (float delta) {
		actor.setVisible(visible);
		return true;
	}

	public boolean isVisible () {
		return visible;
	}

	public void setVisible (boolean visible) {
		this.visible = visible;
	}
}

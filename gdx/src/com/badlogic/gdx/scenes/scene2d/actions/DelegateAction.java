
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Base class for an action that wraps another action. 
 * @author Nathan Sweet */
abstract public class DelegateAction extends Action {
	protected Action action;

	/** Sets the wrapped action. */
	public void setAction (Action action) {
		this.action = action;
	}

	public Action getAction () {
		return action;
	}

	public void restart () {
		if (action != null) action.restart();
	}

	public void setActor (Actor actor) {
		super.setActor(actor);
		if (action != null) action.setActor(actor);
	}

	public String toString () {
		return super.toString() + (action == null ? "" : "(" + action + ")");
	}
}

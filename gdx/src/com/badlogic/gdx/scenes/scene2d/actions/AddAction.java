
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Adds an action to an actor.
 * @author Nathan Sweet */
public class AddAction extends Action {
	private Actor targetActor;
	private Action action;

	public boolean act (float delta) {
		targetActor.addAction(action);
		return true;
	}

	public Actor getTargetActor () {
		return targetActor;
	}

	public void setTargetActor (Actor actor) {
		this.targetActor = actor;
	}

	public Action getAction () {
		return action;
	}

	public void setAction (Action action) {
		this.action = action;
	}

	public void restart () {
		if (action != null) action.restart();
	}

	public void reset () {
		super.reset();
		targetActor = null;
		action = null;
	}
}

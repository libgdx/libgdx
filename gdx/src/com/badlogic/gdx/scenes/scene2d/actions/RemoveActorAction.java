
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Removes the actor the action is attached to or another actor. 
 * @author Nathan Sweet */
public class RemoveActorAction extends Action {
	private Actor removeActor;
	private boolean removed;

	public boolean act (float delta) {
		if (!removed) {
			removed = true;
			(removeActor != null ? removeActor : actor).remove();
		}
		return true;
	}

	public void restart () {
		removed = false;
	}

	public void reset () {
		super.reset();
		removeActor = null;
	}

	public Actor getRemoveActor () {
		return removeActor;
	}

	/** Sets the actor to remove. If null (the default), the {@link #getActor() actor} will be removed. */
	public void setRemoveActor (Actor removeActor) {
		this.removeActor = removeActor;
	}
}

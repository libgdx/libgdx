
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

/** Executes an action only after all other actions on the actor at the time this action was added have finished.
 * @author Nathan Sweet */
public class AfterAction extends DelegateAction {
	private Array<Action> waitForActions = new Array(false, 4);

	public void setActor (Actor actor) {
		if (actor != null) waitForActions.addAll(actor.getActions());
		super.setActor(actor);
	}

	public void restart () {
		super.restart();
		waitForActions.clear();
	}

	public boolean act (float delta) {
		Array<Action> currentActions = actor.getActions();
		if (currentActions.size == 1) waitForActions.clear();
		for (int i = waitForActions.size - 1; i >= 0; i--) {
			Action action = waitForActions.get(i);
			int index = currentActions.indexOf(action, true);
			if (index == -1) waitForActions.removeIndex(i);
		}
		if (waitForActions.size > 0) return false;
		return action.act(delta);
	}
}

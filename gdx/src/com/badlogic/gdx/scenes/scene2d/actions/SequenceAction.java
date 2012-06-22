
package com.badlogic.gdx.scenes.scene2d.actions;

/** Executes a number of actions one at a time. 
 * @author Nathan Sweet */
public class SequenceAction extends ParallelAction {
	private int index;

	public boolean act (float delta) {
		if (index >= actions.size) return true;
		if (actions.get(index).act(delta)) {
			index++;
			if (index > actions.size) return true;
		}
		return false;
	}

	public void restart () {
		super.restart();
		index = 0;
	}
}

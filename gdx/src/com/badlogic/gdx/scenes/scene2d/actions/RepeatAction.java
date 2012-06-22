
package com.badlogic.gdx.scenes.scene2d.actions;

/** Repeats an action a number of times or forever. 
 * @author Nathan Sweet */
public class RepeatAction extends DelegateAction {
	static public final int FOREVER = -1;

	private int repeatCount, executedCount;

	public boolean act (float delta) {
		if (executedCount == repeatCount) return true;
		if (action.act(delta)) {
			if (repeatCount > 0) executedCount++;
			if (executedCount == repeatCount) return true;
			action.restart();
		}
		return false;
	}

	public void restart () {
		super.restart();
		executedCount = 0;
	}

	/** Sets the number of times to repeat. Can be set to {@link #FOREVER}. */
	public void setCount (int count) {
		this.repeatCount = count;
	}

	public int getCount () {
		return repeatCount;
	}
}

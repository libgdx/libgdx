
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;

/** An action that runs a Runnable. 
 * @author Nathan Sweet */
public class RunnableAction extends Action {
	private Runnable runnable;
	private boolean ran;

	public boolean act (float delta) {
		if (!ran) {
			runnable.run();
			ran = true;
		}
		return true;
	}

	public void restart () {
		ran = false;
	}

	public Runnable getRunnable () {
		return runnable;
	}

	public void setRunnable (Runnable runnable) {
		this.runnable = runnable;
	}
}

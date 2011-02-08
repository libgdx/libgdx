package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.scenes.scene2d.actions.Repeat;

/**
 * Listener interface called by an {@link Action} when it was completed. Use this
 * with {@link Action#setCompletionListener(OnActionCompleted)};
 * @author mzechner
 *
 */
public interface OnActionCompleted {
	/**
	 * Called when the {@link Action} is completed. Note that this
	 * might get called multiple times in case the Action is part of
	 * a {@link Repeat} Action. If the Action is an {@link AnimationAction}
	 * then you can cast to this class and receive the targeted {link Actor}
	 * via {@link AnimationAction#getTarget()}.
	 * @param action the Action.
	 */
	public void completed(Action action);
}

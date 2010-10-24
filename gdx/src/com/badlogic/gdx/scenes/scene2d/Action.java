
package com.badlogic.gdx.scenes.scene2d;

public interface Action {
	/**
	 * Sets the {@link Actor} of this action. Will be called when the Action is added to an Actor via {@link Actor.action()} before
	 * any other call to this interface.
	 * 
	 * @param actor the actor.
	 */
	public void setTarget (Actor actor);

	/**
	 * Apply the action.
	 * 
	 * @param delta delta time in seconds
	 */
	public void act (float delta);

	/**
	 * @return whether the action is done or not
	 */
	public boolean isDone ();

	/**
	 * Called by the owner of the action when it can release all its resources, e.g. put itself back into a pool.
	 */
	public void finish ();

	/**
	 * Creates a copy of this action. The action must be in a state independent of the original and one must be able to call
	 * {@link #setTarget()} on it without any side effects.
	 */
	public Action copy ();
}

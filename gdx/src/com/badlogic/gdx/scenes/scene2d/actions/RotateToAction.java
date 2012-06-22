
package com.badlogic.gdx.scenes.scene2d.actions;

/** Sets the actor's rotation from its current value to a specific value. 
 * @author Nathan Sweet */
public class RotateToAction extends TemporalAction {
	private float start, end;

	protected void initialize () {
		start = actor.getRotation();
	}

	protected void update (float percent) {
		actor.setRotation(start + (end - start) * percent);
	}

	public float getRotation () {
		return end;
	}

	public void setRotation (float rotation) {
		this.end = rotation;
	}
}


package com.badlogic.gdx.scenes.scene2d.actions;

/** Sets the actor's rotation from its current value to a relative value. 
 * @author Nathan Sweet */
public class RotateByAction extends TemporalAction {
	private float start, amount;

	protected void initialize () {
		start = actor.getRotation();
	}

	protected void update (float percent) {
		actor.setRotation(start + amount * percent);
	}

	public float getAmount () {
		return amount;
	}

	public void setAmount (float rotationAmount) {
		amount = rotationAmount;
	}
}


package com.badlogic.gdx.scenes.scene2d.actions;

/** Moves an actor from its current position to a relative position. 
 * @author Nathan Sweet */
public class SizeByAction extends TemporalAction {
	private float startWidth, startHeight;
	private float amountWidth, amountHeight;

	protected void initialize () {
		startWidth = actor.getWidth();
		startHeight = actor.getHeight();
	}

	protected void update (float percent) {
		actor.setSize(startWidth + amountWidth * percent, startHeight + amountHeight * percent);
	}

	public void setAmount (float width, float height) {
		amountWidth = width;
		amountHeight = height;
	}

	public float getAmountWidth () {
		return amountWidth;
	}

	public void setAmountWidth (float width) {
		amountWidth = width;
	}

	public float getAmountHeight () {
		return amountHeight;
	}

	public void setAmountHeight (float height) {
		amountHeight = height;
	}
}

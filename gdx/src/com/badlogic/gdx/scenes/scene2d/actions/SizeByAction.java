
package com.badlogic.gdx.scenes.scene2d.actions;

/** Moves an actor from its current position to a relative position.
 * @author Nathan Sweet */
public class SizeByAction extends RelativeTemporalAction {
	private float amountWidth, amountHeight;

	protected void updateRelative (float percentDelta) {
		actor.size(amountWidth * percentDelta, amountHeight * percentDelta);
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

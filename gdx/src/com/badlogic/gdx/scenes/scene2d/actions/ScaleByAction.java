
package com.badlogic.gdx.scenes.scene2d.actions;

/** Sets the actor's scale from its current value to a relative value. 
 * @author Nathan Sweet */
public class ScaleByAction extends TemporalAction {
	private float startX, startY;
	private float amountX, amountY;

	protected void initialize () {
		startX = actor.getScaleX();
		startY = actor.getScaleY();
	}

	protected void update (float percent) {
		actor.setScale(startX + amountX * percent, startY + amountY * percent);
	}

	public void setAmount (float x, float y) {
		amountX = x;
		amountY = y;
	}

	public void setAmount (float scale) {
		amountX = scale;
		amountY = scale;
	}

	public float getAmountX () {
		return amountX;
	}

	public void setAmountX (float x) {
		this.amountX = x;
	}

	public float getAmountY () {
		return amountY;
	}

	public void setAmountY (float y) {
		this.amountY = y;
	}

}

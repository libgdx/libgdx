
package com.badlogic.gdx.scenes.scene2d.actions;

/** Moves an actor from its current position to a relative position. 
 * @author Nathan Sweet */
public class MoveByAction extends TemporalAction {
	private float startX, startY;
	private float amountX, amountY;

	protected void initialize () {
		startX = actor.getX();
		startY = actor.getY();
	}

	protected void update (float percent) {
		actor.setPosition(startX + amountX * percent, startY + amountY * percent);
	}

	public void setAmount (float x, float y) {
		amountX = x;
		amountY = y;
	}

	public float getAmountX () {
		return amountX;
	}

	public void setAmountX (float x) {
		amountX = x;
	}

	public float getAmountY () {
		return amountY;
	}

	public void setAmountY (float y) {
		amountY = y;
	}
}

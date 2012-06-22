
package com.badlogic.gdx.scenes.scene2d.actions;

/** Moves an actor from its current position to a specific position. 
 * @author Nathan Sweet */
public class MoveToAction extends TemporalAction {
	private float startX, startY;
	private float endX, endY;

	protected void initialize () {
		startX = actor.getX();
		startY = actor.getY();
	}

	protected void update (float percent) {
		actor.setPosition(startX + (endX - startX) * percent, startY + (endY - startY) * percent);
	}

	public void setPosition (float x, float y) {
		endX = x;
		endY = y;
	}

	public float getX () {
		return endX;
	}

	public void setX (float x) {
		endX = x;
	}

	public float getY () {
		return endY;
	}

	public void setY (float y) {
		endY = y;
	}
}

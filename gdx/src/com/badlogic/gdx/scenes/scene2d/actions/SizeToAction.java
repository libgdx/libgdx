
package com.badlogic.gdx.scenes.scene2d.actions;

/** Moves an actor from its current position to a specific position. 
 * @author Nathan Sweet */
public class SizeToAction extends TemporalAction {
	private float startWidth, startHeight;
	private float endWidth, endHeight;

	protected void initialize () {
		startWidth = actor.getWidth();
		startHeight = actor.getHeight();
	}

	protected void update (float percent) {
		actor.setSize(startWidth + (endWidth - startWidth) * percent, startHeight + (endHeight - startHeight) * percent);
	}

	public void setSize (float width, float height) {
		endWidth = width;
		endHeight = height;
	}

	public float getWidth () {
		return endWidth;
	}

	public void setWidth (float width) {
		endWidth = width;
	}

	public float getHeight () {
		return endHeight;
	}

	public void setHeight (float height) {
		endHeight = height;
	}
}

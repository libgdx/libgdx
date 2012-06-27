
package com.badlogic.gdx.scenes.scene2d.actions;

/** Base class for actions that transition over time using the percent compelte since the last frame.
 * @author Nathan Sweet */
abstract public class RelativeTemporalAction extends TemporalAction {
	private float lastPercent;

	protected void initialize () {
	}

	protected void update (float percent) {
		updateRelative(percent - lastPercent);
		lastPercent = percent;
	}

	abstract protected void updateRelative (float percentDelta);
}

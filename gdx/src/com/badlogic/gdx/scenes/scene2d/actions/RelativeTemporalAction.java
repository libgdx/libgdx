
package com.badlogic.gdx.scenes.scene2d.actions;

abstract public class RelativeTemporalAction extends TemporalAction {
	private float lastPercent;

	protected void update (float percent) {
		updateRelative(percent - lastPercent);
		lastPercent = percent;
	}

	abstract protected void updateRelative (float percent);
}

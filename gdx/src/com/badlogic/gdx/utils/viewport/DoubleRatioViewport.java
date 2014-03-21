
package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Scaling;

/** A viewport that can choose between minimum and maximum dimensions. It will pick the closest one to the screen aspect ratio and
 * use {@link Scaling#fit}. This will result in black bars (letterboxing) if the screen doesn't exactly match any of the two
 * supported ratios.
 * 
 * @author David Saltares */
public class DoubleRatioViewport extends MinMaxViewportBase {

	public DoubleRatioViewport (float minWorldWidth, float minWorldHeight, float maxWorldWidth, float maxWorldHeight, Camera camera) {
		super(minWorldWidth, minWorldHeight, maxWorldWidth, maxWorldHeight, camera);
	}

	@Override
	public String toString () {
		return getClass().getSimpleName();
	}

	@Override
	protected void calculateWorldSize (float screenAspectRatio) {
		if (Math.abs(screenAspectRatio - minAspectRatio) < Math.abs(screenAspectRatio - maxAspectRatio)) {
			this.setWorldSize(minWorldWidth, minWorldHeight);
		} else {
			this.setWorldSize(maxWorldWidth, maxWorldHeight);
		}
	}
}


package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Scaling;

/** A viewport that defines minimum and maximum dimensions. If the screen aspect ratio falls within the supported range, it will
 * keep it and scale the image. Otherwise, it will pick the closest supported aspect ratio and use {@link Scaling#fit}, which will
 * result in black bars (letterboxing).
 * @author David Saltares */
public class MinMaxViewport extends MinMaxViewportBase {
	public MinMaxViewport (float minWorldWidth, float minWorldHeight, float maxWorldWidth, float maxWorldHeight, Camera camera) {
		super(minWorldWidth, minWorldHeight, maxWorldWidth, maxWorldHeight, camera);
	}

	@Override
	protected void update (float screenAspectRatio) {
		if (screenAspectRatio < minAspectRatio) {
			setWorldSize(minWorldWidth, minWorldHeight);
		} else if (screenAspectRatio > maxAspectRatio) {
			setWorldSize(maxWorldWidth, maxWorldHeight);
		} else if (Math.abs(screenAspectRatio - minAspectRatio) < Math.abs(screenAspectRatio - maxAspectRatio)) {
			setWorldSize(minWorldWidth, minWorldWidth / screenAspectRatio);
		} else {
			setWorldSize(maxWorldWidth, maxWorldWidth / screenAspectRatio);
		}
	}
}

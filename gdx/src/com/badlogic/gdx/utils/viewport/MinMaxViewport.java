
package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Scaling;

/** A viewport that defines minimum and maximum dimensions.
 * <p>
 * When snap is enabled, it will pick the closest dimensions to the screen aspect ratio and use {@link Scaling#fit}. This will
 * result in black bars (letterboxing) if the screen doesn't exactly match any of the two supported ratios.
 * <p>
 * When snap is disabled and the screen aspect ratio falls within the supported range, it will keep it and scale the image.
 * Otherwise, it will pick the closest supported aspect ratio and use {@link Scaling#fit}, which will result in black bars
 * (letterboxing).
 * @author David Saltares */
public class MinMaxViewport extends ScalingViewport {
	private final float minWorldWidth, minWorldHeight;
	private final float maxWorldWidth, maxWorldHeight;
	private final float minAspectRatio, maxAspectRatio;
	private final boolean snap;

	public MinMaxViewport (float minWorldWidth, float minWorldHeight, float maxWorldWidth, float maxWorldHeight, boolean snap,
		Camera camera) {
		super(Scaling.fit, minWorldWidth, minWorldHeight, camera);

		this.minWorldWidth = minWorldWidth;
		this.minWorldHeight = minWorldHeight;
		this.maxWorldWidth = maxWorldWidth;
		this.maxWorldHeight = maxWorldHeight;
		this.snap = snap;
		minAspectRatio = minWorldWidth / minWorldHeight;
		maxAspectRatio = maxWorldWidth / maxWorldHeight;

		if (minAspectRatio > maxAspectRatio) {
			throw new IllegalArgumentException("Minimum aspect ratio has to be smaller than maximum aspect ratio: " + minAspectRatio
				+ " < " + maxAspectRatio);
		}
	}

	@Override
	public void update (int screenWidth, int screenHeight, boolean centerCamera) {
		float screenAspectRatio = (float)screenWidth / (float)screenHeight;

		if (snap) {
			if (Math.abs(screenAspectRatio - minAspectRatio) < Math.abs(screenAspectRatio - maxAspectRatio)) {
				setScaling(Scaling.fit);
				setWorldSize(minWorldWidth, minWorldHeight);
			} else {
				setWorldSize(maxWorldWidth, maxWorldHeight);
			}
		} else {
			if (screenAspectRatio < minAspectRatio) {
				setScaling(Scaling.fit);
				setWorldSize(minWorldWidth, minWorldHeight);
			} else if (screenAspectRatio > maxAspectRatio) {
				setScaling(Scaling.fit);
				setWorldSize(maxWorldWidth, maxWorldHeight);
			} else {
				setScaling(Scaling.fill);
				setWorldSize(maxWorldWidth, maxWorldWidth / screenAspectRatio);
			}
		}

		super.update(screenWidth, screenHeight, centerCamera);
	}
}

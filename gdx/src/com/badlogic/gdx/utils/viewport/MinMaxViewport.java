
package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Scaling;

/** A viewport that defines minimum and maximum dimensions.
 * 
 * When the snap flag is enabled, it will pick the closest dimensions to the screen aspect ratio and use {@link Scaling#fit}. This
 * will result in black bars (letterboxing) if the screen doesn't exactly match any of the two supported ratios.
 * 
 * When the snap flag is disabled and the screen aspect ratio falls within the supported range, it will keep it and scale the
 * image. Otherwise, it will pick the closest supported aspect ratio and use {@link Scaling#fit}, which will result in black bars
 * (letterboxing).
 * 
 * @author David Saltares */
public class MinMaxViewport extends ScalingViewport {

	private final float minWorldWidth;
	private final float minWorldHeight;
	private final float maxWorldWidth;
	private final float maxWorldHeight;
	private final float minAspectRatio;
	private final float maxAspectRatio;
	private final boolean snap;

	public MinMaxViewport (float minWorldWidth, float minWorldHeight, float maxWorldWidth, float maxWorldHeight, boolean snap,
		Camera camera) {
		super(Scaling.fit, minWorldWidth, minWorldHeight, camera);

		this.minWorldWidth = minWorldWidth;
		this.minWorldHeight = minWorldHeight;
		this.maxWorldWidth = maxWorldWidth;
		this.maxWorldHeight = maxWorldHeight;
		this.minAspectRatio = minWorldWidth / minWorldHeight;
		this.maxAspectRatio = maxWorldWidth / maxWorldHeight;
		this.snap = snap;

		if (this.minAspectRatio > this.maxAspectRatio) {
			throw new IllegalArgumentException("Minimum aspect ratio has to be smaller than Maximum aspect ratio");
		}
	}

	@Override
	public void update (int screenWidth, int screenHeight, boolean centerCamera) {
		float screenAspectRatio = (float)screenWidth / (float)screenHeight;

		if (snap) {
			if (Math.abs(screenAspectRatio - minAspectRatio) < Math.abs(screenAspectRatio - maxAspectRatio)) {
				this.setWorldSize(minWorldWidth, minWorldHeight);
			} else {
				this.setWorldSize(maxWorldWidth, maxWorldHeight);
			}
		} else {
			if (screenAspectRatio < minAspectRatio) {
				setWorldSize(minWorldWidth, minWorldHeight);
			} else if (screenAspectRatio > maxAspectRatio) {
				setWorldSize(maxWorldWidth, maxWorldHeight);
			} else {
				if (Math.abs(screenAspectRatio - minAspectRatio) < Math.abs(screenAspectRatio - maxAspectRatio)) {
					setWorldSize(minWorldWidth, minWorldWidth / screenAspectRatio);
				} else {
					setWorldSize(maxWorldWidth, maxWorldWidth / screenAspectRatio);
				}
			}
		}

		super.update(screenWidth, screenHeight, centerCamera);
	}

	@Override
	public String toString () {
		return getClass().getSimpleName() + (snap ? " (snapped)" : " (non snapped)");
	}
}

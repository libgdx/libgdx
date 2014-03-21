
package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Scaling;

/** Abstract viewport class that defines minimum and maximum dimensions.
 * @author David Saltares */
public abstract class MinMaxViewportBase extends ScalingViewport {
	protected final float minWorldWidth, minWorldHeight;
	protected final float maxWorldWidth, maxWorldHeight;
	protected final float minAspectRatio, maxAspectRatio;

	public MinMaxViewportBase (float minWorldWidth, float minWorldHeight, float maxWorldWidth, float maxWorldHeight, Camera camera) {
		super(Scaling.fit, minWorldWidth, minWorldHeight, camera);

		this.minWorldWidth = minWorldWidth;
		this.minWorldHeight = minWorldHeight;
		this.maxWorldWidth = maxWorldWidth;
		this.maxWorldHeight = maxWorldHeight;
		minAspectRatio = minWorldWidth / minWorldHeight;
		maxAspectRatio = maxWorldWidth / maxWorldHeight;

		if (minAspectRatio > maxAspectRatio) {
			throw new IllegalArgumentException("Minimum aspect ratio has to be smaller than Maximum aspect ratio");
		}
	}

	@Override
	public void update (int screenWidth, int screenHeight, boolean centerCamera) {
		update((float)screenWidth / (float)screenHeight);
		super.update(screenWidth, screenHeight, centerCamera);
	}

	/** Sets the viewport world dimensions for a given screen aspect ratio. */
	protected abstract void update (float screenAspectRatio);
}

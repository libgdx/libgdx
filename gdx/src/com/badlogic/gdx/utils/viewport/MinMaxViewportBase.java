
package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Scaling;

/** Abstract viewport class that defines minimum and maximum dimensions.
 * 
 * @author David Saltares */
public abstract class MinMaxViewportBase extends ScalingViewport {

	protected final float minWorldWidth;
	protected final float minWorldHeight;
	protected final float maxWorldWidth;
	protected final float maxWorldHeight;
	protected final float minAspectRatio;
	protected final float maxAspectRatio;

	public MinMaxViewportBase (float minWorldWidth, float minWorldHeight, float maxWorldWidth, float maxWorldHeight, Camera camera) {
		super(Scaling.fit, minWorldWidth, minWorldHeight, camera);

		this.minWorldWidth = minWorldWidth;
		this.minWorldHeight = minWorldHeight;
		this.maxWorldWidth = maxWorldWidth;
		this.maxWorldHeight = maxWorldHeight;
		this.minAspectRatio = minWorldWidth / minWorldHeight;
		this.maxAspectRatio = maxWorldWidth / maxWorldHeight;

		if (this.minAspectRatio > this.maxAspectRatio) {
			throw new IllegalArgumentException("Minimum aspect ratio has to be smaller than Maximum aspect ratio");
		}
	}

	@Override
	public void update (int screenWidth, int screenHeight, boolean centerCamera) {
		calculateWorldSize((float)screenWidth / (float)screenHeight);
		super.update(screenWidth, screenHeight, centerCamera);
	}

	/** Sets the viewport world dimensions for a given screen aspect ratio. */
	protected abstract void calculateWorldSize (float screenAspectRatio);
}

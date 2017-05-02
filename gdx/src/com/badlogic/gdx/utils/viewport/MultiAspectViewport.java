package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;

/**
 * Sets up a new FitViewport everytime screen size is changed to account for multiple aspect ratios.
 * Uses {@link Scaling#fit} to scale the virtual viewport.
 * @author Glen Murdock
 */
public class MultiAspectViewport extends Viewport {

	private Scaling scaling;

	private float minWidth;
	private float minHeight;
	private float maxWidth;
	private float maxHeight;

	private float minAspect;
	private float maxAspect;

	/**
	 * Creates a MutliAspectViewport using a new {@link OrthographicCamera}.
	 */
	public MultiAspectViewport() {
		this(new OrthographicCamera());
	}

	/**
	 * Creates a MultiAspectViewport.
	 * @param camera {@link Camera} is for using {@link OrthographicCamera} or {@link PerspectiveCamera}.
	 */
	public MultiAspectViewport(Camera camera) {
		this.scaling = Scaling.fit;
		this.camera = camera;
	}

	/**
	 * This class must be called before MultiAspectViewport can be used. Sets up the minimum and maximum size for virtual
	 * viewport along with the minimum and maximum aspect supported by game or application.
	 * @param minWidth Minimum width of the virtual viewport.
	 * @param minHeight Minimum height of the virtual viewport.
	 * @param maxWidth Maximum width of the virtual viewport.
	 * @param maxHeight Maximum height of the virtual viewport.
	 * @param minAspect Minimum aspect supported by minWidth, minHeight, maxWidth, maxHeight.
	 * @param maxAspect Maximum aspect supported by minWidth, minHeight, maxWidth, maxHeight.
	 */
	public void setup(float minWidth, float minHeight, float maxWidth, float maxHeight,
		float minAspect, float maxAspect) {
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.minAspect = minAspect;
		this.maxAspect = maxAspect;
		update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
	}

	@Override
	public void update (int screenWidth, int screenHeight, boolean centerCamera) {
		createVirtualSize(screenWidth, screenHeight); // Make changes to virtual size
		Vector2 scaled = scaling.apply(worldWidth, worldHeight, screenWidth, screenHeight);
		viewportWidth = Math.round(scaled.x);
		viewportHeight = Math.round(scaled.y);
		//center the viewport in the middle of the screen
		viewportX = (screenWidth - viewportWidth) / 2;
		viewportY = (screenHeight - viewportHeight) / 2;
		super.update(screenWidth, screenHeight, centerCamera);
	}

	/**
	 * Tests to see which screen size within a minimum and maximum size of width, height and aspect would be allowed
	 * for current screen size.
	 * @param width Width of application screen
	 * @param height Height of application screen
	 */
	public void createVirtualSize(int width, int height) {
		float aspect = (float) width / (float) height;

		if(aspect > maxAspect) {
			aspect = maxAspect;
		} else if(aspect < minAspect) {
			aspect = minAspect;
		}

		boolean foundVirtual = false;
		for(float i = maxWidth; i >= minWidth; i--) {
			float scaleForSize = i / (float) width;
			float virtualViewportWidth = (float) width * scaleForSize;
			float virtualViewportHeight = virtualViewportWidth / aspect;
			virtualViewportWidth = (float) MathUtils.round(virtualViewportWidth);
			virtualViewportHeight = (float) MathUtils.round(virtualViewportHeight);
			if(insideBounds(virtualViewportWidth, virtualViewportHeight)) {
				worldWidth = virtualViewportWidth;
				worldHeight = virtualViewportHeight;
				foundVirtual = true;
				break;
			}
		}

		if(!foundVirtual) {
			worldWidth = minWidth;
			worldHeight = minHeight;
		}
	}

	private boolean insideBounds(float width, float height) {
		if (width < minWidth || width > maxWidth)
			return false;
		if (height < minHeight || height > maxHeight)
			return false;
		return true;
	}

}

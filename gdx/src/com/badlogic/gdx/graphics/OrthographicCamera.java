package com.badlogic.gdx.graphics;

import com.badlogic.gdx.math.Vector3;

/**
 * A camera with orthographic projection.
 * 
 * @author mzechner
 *
 */
public class OrthographicCamera extends Camera {
	/** the zoom of the camera **/
	public float zoom = 1;
	
	/**
	 * Constructs a new OrthographicCamera, using the given viewport
	 * width and height. For pixel perfect 2D rendering just supply
	 * the screen size, for other unit scales (e.g. meters for box2d)
	 * proceed accordingly. 
	 * 
	 * @param viewportWidth the viewport width
	 * @param viewportHeight the viewport height
	 */
	public OrthographicCamera(float viewportWidth, float viewportHeight) {
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;		
		this.near = 0;
	}
	
	private final Vector3 tmp = new Vector3();
	@Override	
	public void update() {
		projection.setToOrtho(zoom * -viewportWidth / 2, zoom * viewportWidth / 2, zoom * -viewportHeight / 2, zoom * viewportHeight / 2, Math.abs(near), Math.abs(far));
		view.setToLookAt(position, tmp.set(position).add(direction), up);	
		combined.set(projection).mul(view);
		invProjectionView.set(combined);
		invProjectionView.inv();		
		frustum.update(combined);
	}
}

package com.badlogic.gdx.maps;

import com.badlogic.gdx.math.Matrix4;


public interface MapRenderer {

	public void setProjectionMatrix(Matrix4 projectionMatrix);
	
	/**
	 * Begin rendering.
	 */
	public void begin();
	
	/**
	 * End rendering.
	 */
	public void end();
	
	/**
	 * Renders all the layers of a map using the given viewbounds.
	 * 
	 * @param viewboundsX
	 * @param viewboundsY
	 * @param viewboundsWidth
	 * @param viewboundsHeight
	 */
	public void render(float viewboundsX, float viewboundsY, float viewboundsWidth, float viewboundsHeight);
	
	/** Renders the given layers of a map using the given viewbounds.
	 * 
	 * @param viewboundsX
	 * @param viewboundsY
	 * @param viewboundsWidth
	 * @param viewboundsHeight
	 * @param layers
	 */
	public void render(float viewboundsX, float viewboundsY, float viewboundsWidth, float viewboundsHeight, int[] layers);
	
}

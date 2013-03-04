package com.badlogic.gdx.maps;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;

/**
 * @brief models a common way of rendering Map objects
 */
public interface MapRenderer {
	/**
	 * Sets the projection matrix and viewbounds from the given camera. If
	 * the camera changes, you have to call this method again. The viewbounds
	 * are taken from the camera's position and viewport size as well as the 
	 * scale. This method will only work if the camera's direction vector
	 * is (0,0,-1) and its up vector is (0, 1, 0), which are the defaults. 
	 * @param camera the {@link OrthographicCamera}
	 */
	public void setView(OrthographicCamera camera);
	
	/**
	 * Sets the projection matrix for rendering, as well as the bounds of the map
	 * which should be rendered. Make sure that the frustum spanned by the projection
	 * matrix coincides with the viewbounds.
	 * @param projectionMatrix
	 * @param viewboundsX
	 * @param viewboundsY
	 * @param viewboundsWidth
	 * @param viewboundsHeight
	 */
	public void setView(Matrix4 projectionMatrix, float viewboundsX, float viewboundsY, float viewboundsWidth, float viewboundsHeight);
	
	/**
	 * Renders all the layers of a map.
	 */
	public void render();
	
	/** Renders the given layers of a map.
	 * 
	 * @param layers
	 */
	public void render(int[] layers);
}

package com.badlogic.gdx.graphics.tmp;

import java.util.Arrays;

import com.badlogic.gdx.math.Vector3;

public class PerspectiveCamera extends Camera {	
	/** the field of view in degrees **/
	public float fieldOfView = 67;
	
	/**
	 * Constructs a new {@link PerspectiveCamera} with the given field of view and viewport
	 * size. The apsect ratio is derrived from the viewport size.
	 * 
	 * @param fieldOfView the field of view in degrees
	 * @param viewportWidth the viewport width
	 * @param viewportHeight the viewport height
	 */
	public PerspectiveCamera(float fieldOfView, float viewportWidth, float viewportHeight) {
		this.fieldOfView = fieldOfView;
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
	}
	
	final Vector3 tmp = new Vector3();
	@Override
	public void update() {
		float aspect = viewportWidth / viewportHeight;				
		projection.setToProjection(Math.abs(near), Math.abs(far), fieldOfView, aspect);
		view.setToLookAt(position, tmp.set(position).add(direction), up);	
		combined.set(projection).mul(view);
		invProjectionView.set(combined);
		invProjectionView.inv();
		frustum.update(invProjectionView);
	}	
}

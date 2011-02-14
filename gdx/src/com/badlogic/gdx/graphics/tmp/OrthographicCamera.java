package com.badlogic.gdx.graphics.tmp;

import java.util.Arrays;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class OrthographicCamera extends BaseCamera {
	public float zoom = 1;
	
	public OrthographicCamera(float viewportWidth, float viewportHeight) {
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;		
		this.near = 0;
	}
	
	private final Vector3 tmp = new Vector3();
	@Override	
	public void update() {
		projection.setToOrtho(-viewportWidth / 2, viewportWidth / 2, -viewportHeight / 2, viewportHeight / 2, -Math.abs(near), -Math.abs(far));
		view.setToLookAt(position, tmp.set(position).add(direction), up);	
		combined.set(projection).mul(view);
		frustum.update(combined);
	}
}

package com.badlogic.gdx.graphics.g3d.shadow.directional;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

/**
 * Contains the result of the directional analyzer
 * @author result
 */
public class DirectionalResult {
	public Vector3 direction = new Vector3();
	public Vector3 position = new Vector3();
	public Vector3 up = new Vector3();
	public float near;
	public float far;
	public float viewportWidth;
	public float viewportHeight;

	public void set(Camera cam) {
		cam.direction.set(direction);
		cam.position.set(position);
		cam.up.set(up);
		cam.near = near;
		cam.far = far;
		cam.viewportWidth = viewportWidth;
		cam.viewportHeight = viewportHeight;
	}
}

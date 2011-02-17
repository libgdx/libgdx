package com.badlogic.gdx.graphics.tmp;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public abstract class Camera {				
	/** the position of the camera **/
	public final Vector3 position = new Vector3();
	/** the unit length direction vector of the camera **/
	public final Vector3 direction = new Vector3(0, 0, -1);
	/** the unit length up vector of the camera **/
	public final Vector3 up = new Vector3(0, 1, 0);
	
	/** the projection matrix **/
	public final Matrix4 projection = new Matrix4();
	/** the view matrix **/
	public final Matrix4 view = new Matrix4();
	/** the combined projection and view matrix **/
	public final Matrix4 combined = new Matrix4();
	
	/** the near clipping plane distance, has to be positive **/
	public float near = 1;	
	/** the far clipping plane distance, has to be positive **/
	public float far = 100;
	
	/** the viewport width **/
	public float viewportWidth = 0;
	/** the viewport height **/
	public float viewportHeight = 0;
	
	/** the frustum **/
	public final Frustum frustum = new Frustum();
	
	private final Matrix4 tmpMat = new Matrix4();
	private final Vector3 tmpVec = new Vector3();
	
	/**
	 * Recalculates the projection and view matrix of this
	 * camera and the frustum planes. Use this after you've manipulated
	 * any of the attributes of the camera.
	 */
	public abstract void update();	
	
	/**
	 * Recalculates the direction of the camera to look at the point
	 * (x, y, z).
	 * @param x the x-coordinate of the point to look at
	 * @param y the x-coordinate of the point to look at
	 * @param z the x-coordinate of the point to look at
	 */
	public void lookAt(float x, float y, float z) {	
		direction.set(x, y, z).sub(position).nor();
	}
	
	/**
	 * Rotates the direction and up vector of this camera by the
	 * given angle around the given axis. The direction and up
	 * vector will not be orthogonalized.
	 * 
	 * @param angle the angle
	 * @param axisX the x-component of the axis
	 * @param axisY the y-component of the axis
	 * @param axisZ the z-component of the axis
	 */
	public void rotate(float angle, float axisX, float axisY, float axisZ) {		
		tmpMat.setToRotation(tmpVec.set(axisX, axisY, axisZ), angle);
		direction.mul(tmpMat).nor();
		up.mul(tmpMat).nor();
	}
	
	/**
	 * Moves the camera by the given amount on each axis.
	 * @param x the displacement on the x-axis
	 * @param y the displacement on the y-axis
	 * @param z the displacement on the z-axis
	 */
	public void translate(float x, float y, float z) {
		position.add(x, y, z);
	}
}

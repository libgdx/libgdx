package com.badlogic.gdx.graphics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class EulerCamera extends Camera{
	// rotate around 0y
	private float yaw;
	//rotate around 0x
	private float pitch;

	//	==========================================

	public float fieldOfView = 67;
	private float aspectRatio;
	
	//	==========================================

	private static final Vector3 tmp = new Vector3();
	private static final Matrix4 tmpMat = new Matrix4();
	
	public EulerCamera (float fieldOfView,float aspectRatio,float near,float far){
		this.fieldOfView = fieldOfView;
		this.aspectRatio = aspectRatio;
		
		this.near = near;
		this.far = far;
		update();
	}
	
	public EulerCamera (float fieldOfView,float viewPortWidth,float viewPortHeight,float near,float far){
		this.fieldOfView = fieldOfView;
		this.viewportHeight = viewPortHeight;
		this.viewportWidth = viewPortWidth;
		this.aspectRatio = viewPortWidth/viewPortHeight;
		
		this.near = near;
		this.far = far;
		update();
	}

	public EulerCamera (float fieldOfView,float viewPortWidth,float viewPortHeight){
		this.fieldOfView = fieldOfView;
		this.viewportWidth = viewPortWidth;
		this.viewportHeight = viewPortHeight;
		
		this.aspectRatio = viewPortWidth/viewPortHeight;
		update();
	}
	
	public float getPitch(){
		return pitch;
	}
	
	public float getYaw(){
		return yaw;
	}
	
	public void rotate(float yaw,float pitch){
		this.yaw = yaw;
		this.pitch = pitch;
		if(pitch < -90)
			pitch =- 90;
		if(pitch > 90)
			pitch = 90;
		
		tmpMat.setToRotation(Vector3.X, pitch);
		direction.mul(tmpMat).nor();
		up.mul(tmpMat).nor();

		tmpMat.setToRotation(Vector3.Y, -yaw);
		direction.mul(tmpMat).nor();
		up.mul(tmpMat).nor();
	}
	

	public EulerCamera yaw(float degrees){
		this.yaw = degrees;
		
		tmpMat.setToRotation(Vector3.Y, -yaw);
		direction.mul(tmpMat).nor();
		up.mul(tmpMat).nor();
		
		return this;
	}

	public EulerCamera pitch(float degrees){
		this.pitch += degrees;
		if(pitch < -90)
			pitch =- 90;
		if(pitch > 90)
			pitch = 90;
		
		tmpMat.setToRotation(Vector3.X, pitch);
		direction.mul(tmpMat).nor();
		up.mul(tmpMat).nor();
		return this;
	}

	@Deprecated
	public void rotate (float angle, float axisX, float axisY, float axisZ) {
	}
	
	@Deprecated
	public void rotate (Vector3 axis, float angle) {
	}

	@Override
	public void update () {
		projection.setToProjection(Math.abs(near), Math.abs(far), fieldOfView, aspectRatio);
		view.setToLookAt(position, tmp.set(position).add(direction), up);
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);
		invProjectionView.set(combined);
		Matrix4.inv(invProjectionView.val);
		frustum.update(invProjectionView);
	}

	@Override
	public void update (boolean updateFrustum) {
		projection.setToProjection(Math.abs(near), Math.abs(far), fieldOfView, aspectRatio);
		view.setToLookAt(position, tmp.set(position).add(direction), up);
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);

		if (updateFrustum) {
			invProjectionView.set(combined);
			Matrix4.inv(invProjectionView.val);
			frustum.update(invProjectionView);
		}
	}

}

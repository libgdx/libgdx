package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

public abstract class ParticleSystem<T> implements Disposable {
	protected static final Vector3 TMP_V1 = new Vector3(), 
		 TMP_V2 = new Vector3(), 
		 TMP_V3 = new Vector3(), 
		 TMP_V4 = new Vector3(), 
		 TMP_V5 = new Vector3(),
		 TMP_V6 = new Vector3();
	protected static final Quaternion 	TMP_Q = new Quaternion(),
													TMP_Q2 = new Quaternion();
	protected static final Matrix3 TMP_M3 = new Matrix3();
	protected static final Matrix4 TMP_M4 = new Matrix4();
	protected ParticleController<T> controller;
	/** Called to initialize new emitted particles.
	 * Should be called by the Emitter */
	public void initParticles (int startIndex, int count){};
	/** Called to notify which particles have been killed .
	 * Should be called by the Emitter*/
	public void killParticles (int startIndex, int count){};
	/** Updates the simulation */
	public void update (){};
	/** Called to allocate each resource needed 
	 * Must be called before any other method. */
	public void init (){};
	/** Called to begin the simulation.
	 * Must be called after init and before update.
	 * Must be called by the emitter on the controller. */
	public void start(){};
	/** Called when it's time to release each allocated resource*/
	public void dispose(){}
	public abstract ParticleSystem<T> copy();
	
	public void bind (ParticleController particleController) {
		controller = particleController;
	}
	
	
}

package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;


/** It's the base class of every {@link ParticleController} component.
 * A component duty is to participate in one or some events during the simulation.
 * (i.e it can handle the particles emission or modify particle properties, etc.).
 * @author inferno */
public abstract class ParticleControllerComponent implements Disposable, Json.Serializable, ResourceData.Configurable {
	protected static final Vector3 TMP_V1 = new Vector3(), 
		 TMP_V2 = new Vector3(), 
		 TMP_V3 = new Vector3(), 
		 TMP_V4 = new Vector3(), 
		 TMP_V5 = new Vector3(),
		 TMP_V6 = new Vector3();
	protected static final Quaternion TMP_Q = new Quaternion(), TMP_Q2 = new Quaternion();
	protected static final Matrix3 TMP_M3 = new Matrix3();
	protected static final Matrix4 TMP_M4 = new Matrix4();
	protected ParticleController controller;
	/** Called to initialize new emitted particles. */
	public void activateParticles (int startIndex, int count){};
	/** Called to notify which particles have been killed. */
	public void killParticles (int startIndex, int count){};
	/** Called to execute the component behavior. */
	public void update (){};
	/** Called once during intialization */
	public void init (){};
	/** Called at the start of the simulation. */
	public void start(){};
	/** Called at the end of the simulation. */
	public void end(){};
	public void dispose(){}
	public abstract ParticleControllerComponent copy();
	/** Called during initialization to allocate additional particles channels*/
	public void allocateChannels(){}
	public void set(ParticleController particleController) {
		controller = particleController;
	}
	
	@Override
	public void save (AssetManager manager, ResourceData data) {}
	@Override
	public void load (AssetManager manager, ResourceData data) {}
	@Override
	public void write (Json json) {}
	@Override
	public void read (Json json, JsonValue jsonData) {}
	
}

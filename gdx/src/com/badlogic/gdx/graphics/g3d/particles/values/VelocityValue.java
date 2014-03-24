package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's a value which should specifies the variations and properties 
 * of a velocity (rotational or directional) in time and space. */
/** @author Inferno */
public abstract class VelocityValue<P, D extends VelocityData> extends ParticleValue{
	protected static final Vector3 	TMP_V1 = new Vector3(), 
		 										TMP_V2 = new Vector3(), 
		 										TMP_V3 = new Vector3();
	protected static final Quaternion TMP_Q = new Quaternion();
	
	public boolean isGlobal = false;
	
	/** It's called by the {@link VelocityInfluencer} to allocate per particle data relative to this velocity */
	public abstract D allocData ();
	/** It's called by the {@link VelocityInfluencer} to initialize per particle data relative to this velocity */
	public abstract void initData (D velocityData);
	/** It's called by the {@link VelocityInfluencer} to apply this velocity to a particle. */
	public abstract void addVelocity (ParticleController<P> controller, P particle, D data);
	
	public VelocityValue(){}
	
	public VelocityValue (VelocityValue<P, VelocityData> value) {
		super(value);
		this.isGlobal = value.isGlobal;
	}
	
	@Override
	public void load (ParticleValue value) {
		super.load(value);
		isGlobal = ((VelocityValue)value).isGlobal;
	}
	public abstract VelocityValue<P, D> copy ();

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("isGlobal", isGlobal);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		isGlobal = json.readValue("isGlobal", boolean.class, jsonData);
	}
	
}


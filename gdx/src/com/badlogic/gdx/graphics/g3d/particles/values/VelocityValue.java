package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class VelocityValue<P, D extends VelocityData> extends ParticleValue{
	protected static final Vector3 	TMP_V1 = new Vector3(), 
		 										TMP_V2 = new Vector3(), 
		 										TMP_V3 = new Vector3();
	protected static final Quaternion TMP_Q = new Quaternion();//, TMP_Q2 = new Quaternion();
	
	public boolean isGlobal = false;
	
	public abstract D allocData ();
	public abstract void initData (D velocityData);
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


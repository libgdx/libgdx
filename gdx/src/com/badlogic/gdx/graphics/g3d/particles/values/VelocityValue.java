package com.badlogic.gdx.graphics.g3d.particles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.Utils;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public abstract class VelocityValue<P, D extends VelocityData> extends ParticleValue{
	protected static final Vector3 	TMP_V1 = new Vector3(), 
		 										TMP_V2 = new Vector3(), 
		 										TMP_V3 = new Vector3();
	protected static final Quaternion TMP_Q = new Quaternion();
	
	public boolean isGlobal = false;
	
	public abstract D allocData ();
	public abstract void initData (D velocityData);
	public abstract void addVelocity (ParticleController<P> controller, P particle, D data);
	
	@Override
	public void save (Writer output) throws IOException {
		super.save(output);
		output.write("global: " + isGlobal+ "\n");
	}
	
	@Override
	public void load (BufferedReader reader) throws IOException {
		super.load(reader);
		isGlobal = Utils.readBoolean(reader, "global");
	}
	
	public void load (VelocityValue value) {
		super.load(value);
		isGlobal = value.isGlobal;
	}

}


package com.badlogic.gdx.graphics.g3d.particles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.Utils;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.CommonVelocityData;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.StrengthVelocityData;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public abstract class AngularVelocityValue<P> extends StrengthVelocityValue<P, CommonVelocityData>{
	public ScaledNumericValue thetaValue;
	public ScaledNumericValue phiValue;

	public AngularVelocityValue(){
		thetaValue = new ScaledNumericValue();
		phiValue = new ScaledNumericValue();
	}

	public ScaledNumericValue getTheta(){
		return thetaValue;
	}

	public ScaledNumericValue getPhi(){
		return phiValue;
	}

	public void save (Writer output) throws IOException {
		super.save(output);
		if (!active) return;
		output.write("theta:\n");
		thetaValue.save(output);
		output.write("phi:\n");
		phiValue.save(output);
	}

	public void load (BufferedReader reader) throws IOException {
		super.load(reader);
		if (!active) return;
		reader.readLine();
		thetaValue.load(reader);
		reader.readLine();
		phiValue.load(reader);
	}

	public void load (AngularVelocityValue value) {
		super.load(value);
		thetaValue.load(value.thetaValue);
		phiValue.load(value.phiValue);
	}


	@Override
	public CommonVelocityData allocData () {
		return new CommonVelocityData();
	}

	@Override
	public void initData (CommonVelocityData data) {
		super.initData(data);
		data.strengthStart = strengthValue.newLowValue();
		data.strengthDiff = strengthValue.newHighValue();
		if (!strengthValue.isRelative()) 
			data.strengthDiff -= data.strengthStart;

		//Theta
		data.thetaStart = thetaValue.newLowValue();
		data.thetaDiff = thetaValue.newHighValue();
		if (!thetaValue.isRelative()) 
			data.thetaDiff -= data.thetaStart;

		//Phi
		data.phistart = phiValue.newLowValue();
		data.phiDiff = phiValue.newHighValue();
		if (!phiValue.isRelative())  
			data.phiDiff -= data.phistart;
	}

}


package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.AngularVelocityData;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's a class which represents a velocity in the space.
 * {@link AngularVelocityValue#thetaValue} expresses the rotation angle variations around the Y axis
 * on the XZ plane, while {@link AngularVelocityValue#phiValue} expresses the rotation angle variations
 * starting from Y axis around the orbit defined by {@link AngularVelocityValue#thetaValue}. 
 * @see <a href="http://en.wikipedia.org/wiki/Spherical_coordinate_system"> link </a> 
 * for a detailed explanation.*/
/** @author Inferno */
public abstract class AngularVelocityValue<P> extends StrengthVelocityValue<P, AngularVelocityData>{
	/** Polar angle, XZ plane */
	public ScaledNumericValue thetaValue;
	/** Azimuth, Y */
	public ScaledNumericValue phiValue;

	public AngularVelocityValue(){
		thetaValue = new ScaledNumericValue();
		phiValue = new ScaledNumericValue();
	}
	
	public AngularVelocityValue(AngularVelocityValue value){
		super(value);
		thetaValue = new ScaledNumericValue();
		phiValue = new ScaledNumericValue();
		thetaValue.load(value.thetaValue);
		phiValue.load(value.phiValue);
	}

	public ScaledNumericValue getTheta(){
		return thetaValue;
	}

	public ScaledNumericValue getPhi(){
		return phiValue;
	}

	public void load (AngularVelocityValue value) {
		super.load(value);
		thetaValue.load(value.thetaValue);
		phiValue.load(value.phiValue);
	}


	@Override
	public AngularVelocityData allocData () {
		return new AngularVelocityData();
	}

	@Override
	public void initData (AngularVelocityData data) {
		super.initData(data);
		
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

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("thetaValue", thetaValue);
		json.writeValue("phiValue", phiValue);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		thetaValue = json.readValue("thetaValue", ScaledNumericValue.class, jsonData);
		phiValue = json.readValue("phiValue", ScaledNumericValue.class, jsonData);
	}
}


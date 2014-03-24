package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.StrengthVelocityData;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's a class which represents the magnitude of a velocity in the space.
 * It should be the base class of all the {@link VelocityValue} values that use the strength of
 * the velocity somehow (Note: not all the values are interested in direction and/or magnitude of a velocity).*/
/** @author Inferno */
public abstract class StrengthVelocityValue<T, D extends StrengthVelocityData> extends VelocityValue<T, D>{
	public ScaledNumericValue strengthValue;

	public StrengthVelocityValue(){
		strengthValue = new ScaledNumericValue();
	}
	
	public StrengthVelocityValue(StrengthVelocityValue value){
		super(value);
		strengthValue = new ScaledNumericValue();
		strengthValue.load(value.strengthValue);
	}

	public ScaledNumericValue getStrength(){
		return strengthValue;
	}

	@Override
	public void load (ParticleValue value) {
		super.load(value);
		strengthValue.load( ((StrengthVelocityValue)value).strengthValue);
	}

	public void initData (StrengthVelocityData data) {
		data.strengthStart = strengthValue.newLowValue();
		data.strengthDiff = strengthValue.newHighValue();
		if (!strengthValue.isRelative()) 
			data.strengthDiff -= data.strengthStart;
	}
	
	@Override
	public D allocData () {
		return (D) new StrengthVelocityData();
	}
	
	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("strengthValue", strengthValue);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		strengthValue = json.readValue("strengthValue", ScaledNumericValue.class, jsonData);
	}
	
}
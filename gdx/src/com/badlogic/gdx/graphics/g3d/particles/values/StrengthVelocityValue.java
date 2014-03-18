package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.StrengthVelocityData;

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
}
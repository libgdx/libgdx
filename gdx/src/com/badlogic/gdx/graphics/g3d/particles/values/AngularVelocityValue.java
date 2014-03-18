package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.CommonVelocityData;

public abstract class AngularVelocityValue<P> extends StrengthVelocityValue<P, CommonVelocityData>{
	/** Polar angle, XZ plane */
	public ScaledNumericValue thetaValue;
	/** Azimuth, Z */
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


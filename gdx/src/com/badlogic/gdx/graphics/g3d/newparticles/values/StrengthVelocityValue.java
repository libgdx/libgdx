package com.badlogic.gdx.graphics.g3d.newparticles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.newparticles.values.VelocityDatas.StrengthVelocityData;
import com.badlogic.gdx.graphics.g3d.newparticles.values.VelocityDatas.VelocityData;

public abstract class StrengthVelocityValue<T, D extends StrengthVelocityData> extends VelocityValue<T, D>{
	public ScaledNumericValue strengthValue;

	public StrengthVelocityValue(){
		strengthValue = new ScaledNumericValue();
	}

	public ScaledNumericValue getStrength(){
		return strengthValue;
	}

	public void save (Writer output) throws IOException {
		super.save(output);
		if (!active) return;
		output.write("strength:\n");
		strengthValue.save(output);
	}

	public void load (BufferedReader reader) throws IOException {
		super.load(reader);
		if (!active) return;
		reader.readLine();
		strengthValue.load(reader);
	}

	public void load (StrengthVelocityValue value) {
		super.load(value);
		strengthValue.load(value.strengthValue);
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
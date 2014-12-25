package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** A value which has a defined minimum and maximum bounds.
* @author Inferno */
public class RangedNumericValue extends ParticleValue {
	private float lowMin, lowMax;

	public float newLowValue () {
		return lowMin + (lowMax - lowMin) * MathUtils.random();
	}

	public void setLow (float value) {
		lowMin = value;
		lowMax = value;
	}

	public void setLow (float min, float max) {
		lowMin = min;
		lowMax = max;
	}

	public float getLowMin () {
		return lowMin;
	}

	public void setLowMin (float lowMin) {
		this.lowMin = lowMin;
	}

	public float getLowMax () {
		return lowMax;
	}

	public void setLowMax (float lowMax) {
		this.lowMax = lowMax;
	}

	public void load (RangedNumericValue value) {
		super.load(value);
		lowMax = value.lowMax;
		lowMin = value.lowMin;
	}
	
	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("lowMin", lowMin);
		json.writeValue("lowMax", lowMax);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		lowMin = json.readValue("lowMin", float.class, jsonData);
		lowMax = json.readValue("lowMax", float.class, jsonData);
	}
	
}
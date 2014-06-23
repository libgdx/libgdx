package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** A value which contains a single float variable. 
 * @author Inferno */
public class NumericValue extends ParticleValue {
	private float value;

	public float getValue () {
		return value;
	}

	public void setValue (float value) {
		this.value = value;
	}

	public void load (NumericValue value) {
		super.load(value);
		this.value = value.value;
	}
	
	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("value", value);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		value = json.readValue("value", float.class, jsonData);
	}
	
}
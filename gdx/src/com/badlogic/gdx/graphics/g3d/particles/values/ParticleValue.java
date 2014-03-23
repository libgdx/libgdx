package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;


public class ParticleValue implements Json.Serializable {
	public boolean active;

	public ParticleValue(){	}
	
	public ParticleValue (ParticleValue value) {
		this.active =value.active;
	}
	
	public boolean isActive () {
		return active;
	}

	public void setActive (boolean active) {
		this.active = active;
	}

	public void load (ParticleValue value) {
		active = value.active;
	}
	
	@Override
	public void write (Json json) {
		json.writeValue("active", active);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		active = json.readValue("active", Boolean.class, jsonData);
	}
}
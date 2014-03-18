package com.badlogic.gdx.graphics.g3d.particles.values;


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
}
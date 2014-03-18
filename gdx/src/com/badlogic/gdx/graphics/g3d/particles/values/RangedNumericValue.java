package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.MathUtils;

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
}
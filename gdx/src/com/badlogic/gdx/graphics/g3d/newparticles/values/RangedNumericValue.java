package com.badlogic.gdx.graphics.g3d.newparticles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.newparticles.Utils;
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

	public void save (Writer output) throws IOException {
		super.save(output);
		if (!active) return;
		output.write("lowMin: " + lowMin + "\n");
		output.write("lowMax: " + lowMax + "\n");
	}

	public void load (BufferedReader reader) throws IOException {
		super.load(reader);
		if (!active) return;
		lowMin = Utils.readFloat(reader, "lowMin");
		lowMax = Utils.readFloat(reader, "lowMax");
	}

	public void load (RangedNumericValue value) {
		super.load(value);
		lowMax = value.lowMax;
		lowMin = value.lowMin;
	}
}
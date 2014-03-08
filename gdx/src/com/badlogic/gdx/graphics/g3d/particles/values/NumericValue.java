package com.badlogic.gdx.graphics.g3d.particles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.particles.Utils;

public class NumericValue extends ParticleValue {
	private float value;

	public float getValue () {
		return value;
	}

	public void setValue (float value) {
		this.value = value;
	}

	public void save (Writer output) throws IOException {
		super.save(output);
		if (!active) return;
		output.write("value: " + value + "\n");
	}

	public void load (BufferedReader reader) throws IOException {
		super.load(reader);
		if (!active) return;
		value = Utils.readFloat(reader, "value");
	}

	public void load (NumericValue value) {
		super.load(value);
		this.value = value.value;
	}
}
package com.badlogic.gdx.graphics.g3d.particles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.particles.Utils;

public class AlignmentValue extends ParticleValue {
	static public enum Align {
		screen, viewPoint, particleDirection
	}
	public Align align = Align.screen;

	public Align getAlign () {
		return align;
	}

	public void setAlign (Align aAlign) {
		align = aAlign;
	}

	public void save (Writer output) throws IOException {
		super.save(output);
		if (!active) return;
		output.write("align: " + align+ "\n");
	}

	public void load (BufferedReader reader) throws IOException {
		super.load(reader);
		if (!active) return;
		align = Align.valueOf(Utils.readString(reader, "align"));
	}

	public void load (AlignmentValue value) 
	{
		super.load(value);
		align = value.align;
	}
}
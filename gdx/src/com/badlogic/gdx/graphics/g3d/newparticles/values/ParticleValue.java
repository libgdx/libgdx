package com.badlogic.gdx.graphics.g3d.newparticles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.newparticles.Utils;

public class ParticleValue {
	public boolean active;

	public boolean isActive () 
	{
		return active;
	}

	public void setActive (boolean active) {
		this.active = active;
	}

	public void save (Writer output) throws IOException {
			output.write("active: " + active + "\n");
	}

	public void load (BufferedReader reader) throws IOException {
			active = Utils.readBoolean(reader, "active");
	}

	public void load (ParticleValue value) 
	{
		active = value.active;
	}
}
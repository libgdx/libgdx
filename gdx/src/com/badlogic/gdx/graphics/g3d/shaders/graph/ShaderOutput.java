package com.badlogic.gdx.graphics.g3d.shaders.graph;

public class ShaderOutput {
	private final String name;
	private final String type;
	private final boolean isVarying;
	
	ShaderOutput(String name, String type, boolean isVarying) {
		this.name = name;
		this.type = type;
		this.isVarying = isVarying;
	}

	public String getName () {
		return name;
	}

	public String getType () {
		return type;
	}

	public boolean isVarying () {
		return isVarying;
	}

	public ShaderOutput copy () {
		return new ShaderOutput(name, type, isVarying);
	}
}

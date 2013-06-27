package com.badlogic.gdx.graphics.g3d.shaders.graph;

public class ShaderDefine {
	private final String name;
	private final int value;
	
	ShaderDefine(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public String getName () {
		return name;
	}

	public int getValue () {
		return value;
	}
}

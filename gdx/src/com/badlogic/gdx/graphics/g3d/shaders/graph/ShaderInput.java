package com.badlogic.gdx.graphics.g3d.shaders.graph;

public class ShaderInput {
	public static enum ShaderInputQualifier {
		Attribute,
		Uniform,
		Local
	}
	
	private final String name;
	private final String type;
	private final ShaderInputQualifier qualifier;
	
	ShaderInput(String name, String type, ShaderInputQualifier qualifier) {
		this.name = name;
		this.type = type;
		this.qualifier = qualifier;
	}

	public String getName () {
		return name;
	}

	public String getType () {
		return type;
	}

	public ShaderInputQualifier getQualifier () {
		return qualifier;
	}

	public ShaderInput copy () {
		return new ShaderInput(name, type, qualifier);
	}
}

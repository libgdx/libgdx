package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public abstract class MaterialAttribute {
	String name;
	
	public MaterialAttribute(String name) {
		this.name = name;
	}
	
	public abstract void bind();
	public abstract void bind(ShaderProgram program);
	public abstract MaterialAttribute copy();
}

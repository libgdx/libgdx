package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ColorAttribute extends MaterialAttribute {
	Color color;
	
	public ColorAttribute(Color color, String name) {
		super(name);
		this.color = new Color(color);
	}
	
	@Override public void bind () {
		if(Gdx.gl10 == null) throw new RuntimeException("Can't call ColorAttribute.bind() in a GL20 context");
		Gdx.gl10.glColor4f(color.r, color.g, color.b, color.a);
	}

	@Override public void bind (ShaderProgram program) {
		program.setUniformf(name, color.r, color.g, color.b, color.a);
	}

	@Override public MaterialAttribute copy () {
		return new ColorAttribute(color, name);
	}
}

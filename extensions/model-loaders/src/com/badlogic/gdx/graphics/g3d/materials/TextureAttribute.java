package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class TextureAttribute extends MaterialAttribute {
	public Texture texture;
	public int unit;
	
	public TextureAttribute (Texture texture, int unit, String name) {
		super(name);
		this.texture = texture;
		this.unit = unit;
	}

	@Override public void bind () {
		texture.bind(unit);
	}

	@Override public void bind (ShaderProgram program) {	
		texture.bind(unit);
		program.setUniformi(name, unit);
	}

	@Override public MaterialAttribute copy () {
		return new TextureAttribute(texture, unit, name);
	}
}

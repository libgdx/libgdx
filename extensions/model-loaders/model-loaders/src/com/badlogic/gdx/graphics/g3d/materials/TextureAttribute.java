package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class TextureAttribute extends MaterialAttribute {
	public Texture texture;
	public int unit;
	public TextureFilter minFilter;
	public TextureFilter magFilter;
	public TextureWrap uWrap;
	public TextureWrap vWrap;
	
	public TextureAttribute (Texture texture, int unit, String name, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
		super(name);
		this.texture = texture;
		this.unit = unit;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
	}
	
	public TextureAttribute (Texture texture, int unit, String name) {
		this(texture, unit, name, texture.getMinFilter(), texture.getMagFilter(), texture.getUWrap(), texture.getVWrap());		
	}

	@Override public void bind () {		
		texture.bind(unit);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, texture.getTextureFilter(minFilter));
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, texture.getTextureFilter(magFilter));
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, uWrap == TextureWrap.ClampToEdge?GL10.GL_CLAMP_TO_EDGE:GL10.GL_REPEAT);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, vWrap == TextureWrap.ClampToEdge?GL10.GL_CLAMP_TO_EDGE:GL10.GL_REPEAT);		
	}

	@Override public void bind (ShaderProgram program) {		
		texture.bind(unit);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, texture.getTextureFilter(minFilter));
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, texture.getTextureFilter(magFilter));
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, uWrap == TextureWrap.ClampToEdge?GL10.GL_CLAMP_TO_EDGE:GL10.GL_REPEAT);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, vWrap == TextureWrap.ClampToEdge?GL10.GL_CLAMP_TO_EDGE:GL10.GL_REPEAT);
		program.setUniformi(name, unit);
	}

	@Override public MaterialAttribute copy () {
		return new TextureAttribute(texture, unit, name, minFilter, magFilter, uWrap, vWrap);
	}
}

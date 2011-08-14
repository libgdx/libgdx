
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
	public int minFilter;
	public int magFilter;
	public int uWrap;
	public int vWrap;

	public TextureAttribute (Texture texture, int unit, String name, TextureFilter minFilter, TextureFilter magFilter,
		TextureWrap uWrap, TextureWrap vWrap) {
		this(texture, unit, name, minFilter.getGLEnum(), magFilter.getGLEnum(), uWrap.getGLEnum(), vWrap.getGLEnum());
	}

	public TextureAttribute (Texture texture, int unit, String name, int minFilter, int magFilter, int uWrap, int vWrap) {
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

	@Override
	public void bind () {
		texture.bind(unit);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, minFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, magFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, uWrap);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, vWrap);
	}

	@Override
	public void bind (ShaderProgram program) {
		texture.bind(unit);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, minFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, magFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, uWrap);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, vWrap);
		program.setUniformi(name, unit);
	}

	@Override
	public MaterialAttribute copy () {
		return new TextureAttribute(texture, unit, name, minFilter, magFilter, uWrap, vWrap);
	}
}

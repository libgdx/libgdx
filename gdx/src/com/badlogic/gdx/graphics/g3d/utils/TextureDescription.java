package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;

public class TextureDescription {
	public Texture texture = null;
	public int minFilter = GL10.GL_INVALID_VALUE;
	public int magFilter = GL10.GL_INVALID_VALUE;
	public int uWrap = GL10.GL_INVALID_VALUE;
	public int vWrap = GL10.GL_INVALID_VALUE;
	// TODO add other values, see http://www.opengl.org/sdk/docs/man/xhtml/glTexParameter.xml
	
	public TextureDescription(final Texture texture, final int minFilter, final int magFilter, final int uWrap, final int vWrap) {
		set(texture, minFilter, magFilter, uWrap, vWrap);
	}
	
	public TextureDescription(final Texture texture) {
		this.texture = texture;
	}
	
	public TextureDescription() {
	}
	
	public void set(final Texture texture, final int minFilter, final int magFilter, final int uWrap, final int vWrap) {
		this.texture = texture;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
	}
	
	public void set(final TextureDescription other) {
		texture = other.texture;
		minFilter = other.minFilter;
		magFilter = other.magFilter;
		uWrap = other.uWrap;
		vWrap = other.vWrap;
	}
	
	public void reset() {
		texture = null;
		minFilter = GL10.GL_INVALID_VALUE;
		magFilter = GL10.GL_INVALID_VALUE;
		uWrap = GL10.GL_INVALID_VALUE;
		vWrap = GL10.GL_INVALID_VALUE;
	}
	
	@Override
	public boolean equals (Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof TextureDescription)) return false;
		final TextureDescription other = (TextureDescription)obj;
		return other.texture == texture && other.minFilter == minFilter && other.magFilter == magFilter &&
			other.uWrap == uWrap && other.vWrap == vWrap;
	}
}

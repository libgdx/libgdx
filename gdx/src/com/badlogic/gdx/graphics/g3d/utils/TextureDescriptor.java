package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;

public class TextureDescriptor {
	public GLTexture texture = null;
	public Texture.TextureFilter minFilter;
	public Texture.TextureFilter magFilter;
	public Texture.TextureWrap uWrap;
	public Texture.TextureWrap vWrap;
	// TODO add other values, see http://www.opengl.org/sdk/docs/man/xhtml/glTexParameter.xml
	
	public TextureDescriptor(final Texture texture, final Texture.TextureFilter minFilter, final Texture.TextureFilter magFilter, final Texture.TextureWrap uWrap, final Texture.TextureWrap vWrap) {
		set(texture, minFilter, magFilter, uWrap, vWrap);
	}
	
	public TextureDescriptor(final Texture texture) {
		this(texture, null, null, null, null);
	}
	
	public TextureDescriptor() {
	}

	public void set(final GLTexture texture, final Texture.TextureFilter minFilter, final Texture.TextureFilter magFilter, final Texture.TextureWrap uWrap, final Texture.TextureWrap vWrap) {
		this.texture = texture;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
	}
	
	public void set(final TextureDescriptor other) {
		texture = other.texture;
		minFilter = other.minFilter;
		magFilter = other.magFilter;
		uWrap = other.uWrap;
		vWrap = other.vWrap;
	}
	
	@Override
	public boolean equals (Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof TextureDescriptor)) return false;
		final TextureDescriptor other = (TextureDescriptor)obj;
		return other.texture == texture && other.minFilter == minFilter && other.magFilter == magFilter &&
			other.uWrap == uWrap && other.vWrap == vWrap;
	}
}

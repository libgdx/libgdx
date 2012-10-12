package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * This is a {@link FrameBuffer} variant backed by a float texture.
 *
 */
public class FloatFrameBuffer extends FrameBuffer {

	/** Creates a new FrameBuffer with a float backing texture, having the given dimensions and potentially a depth buffer attached.
	 * 
	 * @param width the width of the framebuffer in pixels
	 * @param height the height of the framebuffer in pixels
	 * @param hasDepth whether to attach a depth buffer
	 * @throws GdxRuntimeException in case the FrameBuffer could not be created */
	public FloatFrameBuffer (int width, int height, boolean hasDepth) {
		super(null, width, height, hasDepth);
	}
	
	/**
	 * Override this method in a derived class to set up the backing texture as you like.
	 */
	protected void setupTexture() {
		FloatTextureData data = new FloatTextureData(width, height);
		colorTexture = new Texture(data);
		if (Gdx.app.getType() == ApplicationType.Desktop
			|| Gdx.app.getType() == ApplicationType.Applet)
			colorTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		else
			//no filtering for float textures in OpenGL ES
			colorTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		colorTexture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
	}
}

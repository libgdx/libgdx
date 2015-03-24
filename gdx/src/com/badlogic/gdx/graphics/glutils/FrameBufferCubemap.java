/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses. It will
 * automatically create a texture for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * texture by {@link FrameBufferCubemap#getColorBufferTexture()}. This class will only work with OpenGL ES 2.0.
 * </p>
 *
 * <p>
 * FrameBuffers are managed. In case of an OpenGL context loss, which only happens on Android when a user switches to another
 * application or receives an incoming call, the framebuffer will be automatically recreated.
 * </p>
 *
 * <p>
 * A FrameBuffer must be disposed if it is no longer needed
 * </p>
 *
 * <p>
 * Typical use: <br />
 * FrameBufferCubemap frameBuffer = new FrameBufferCubemap(Format.RGBA8888, fSize, fSize, fSize, true); <br />
 * frameBuffer.begin(); <br />
 * while( frameBuffer.nextSide() ) { <br />
 * camera.up.set(frameBuffer.getSide().getUp()); <br />
 * camera.direction.set(frameBuffer.getSide().getDirection()); <br />
 * camera.update(); <br />
 *
 * Gdx.gl.glClearColor(0, 0, 0, 1); <br />
 * Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT); <br />
 * modelBatch.begin(camera); <br />
 * modelBatch.render(renderableProviders); <br />
 * modelBatch.end(); <br />
 * } <br />
 * frameBuffer.end(); <br />
 * Cubemap cubemap = frameBuffer.getColorBufferCubemap();
 * </p>
 *
 * @author realitix */
public class FrameBufferCubemap extends FrameBuffer {
	/** the index of last side rendered **/
	protected int nbSides;

	/** Creates a new FrameBuffer having the given dimensions and potentially a depth buffer attached.
	 *
	 * @param format
	 * @param width
	 * @param height
	 * @param hasDepth
	 */
	public FrameBufferCubemap (Pixmap.Format format, int width, int height, boolean hasDepth) {
		this(format, width, height, hasDepth, false);
	}


	/** Creates a new FrameBuffer having the given dimensions and potentially a depth and a stencil buffer attached.
	 *
	 * @param format the format of the color buffer; according to the OpenGL ES 2.0 spec, only RGB565, RGBA4444 and RGB5_A1 are
	 *           color-renderable
	 * @param width the width of the cubemap in pixels
	 * @param height the height of the cubemap in pixels
	 * @param hasDepth whether to attach a depth buffer
	 * @param hasStencil whether to attach a stencil buffer
	 * @throws com.badlogic.gdx.utils.GdxRuntimeException in case the FrameBuffer could not be created */
	public FrameBufferCubemap (Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil) {
		super(format, width, height, hasDepth, hasStencil);
	}

	/** Override this method in a derived class to set up the backing texture as you like. */
	@Override
	protected void setupTexture () {
		colorTexture = new Cubemap(width, height, width, format);
		colorTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		colorTexture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
	}

	/**
	 * Binds the frame buffer and sets the viewport accordingly, so everything gets drawn to it.
	 */
	@Override
	public void begin() {
		nbSides = -1;
		super.begin();
	}

	/** Bind the next side of cubemap and return false if no more side. */
	public boolean nextSide() {
		if( nbSides > 5 ) {
			throw new GdxRuntimeException("No remaining sides.");
		}
		else if ( nbSides == 5 ) {
			return false;
		}

		nbSides++;
		bindSide(getSide());
		return true;
	}

	/**
	 * Bind the side.
	 * @param side Side to bind
	 */
	protected void bindSide(final Cubemap.CubemapSide side) {
		Gdx.gl20.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0,
			side.glEnum, colorTexture.getTextureObjectHandle(), 0);
	}

	/**
	 * Get binded side.
	 */
	public Cubemap.CubemapSide getSide() {
		return Cubemap.CubemapSide.values()[nbSides];
	}

	/** Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on. */
	@Override
	public void end() {
		end(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}


	/** Unbinds the framebuffer and sets viewport sizes, all drawing will be performed to the normal framebuffer from here on.
	 *
	 * @param x the x-axis position of the viewport in pixels
	 * @param y the y-asis position of the viewport in pixels
	 * @param width the width of the viewport in pixels
	 * @param height the height of the viewport in pixels */
	@Override
	public void end (int x, int y, int width, int height) {
		if( nbSides < 5 ) throw new GdxRuntimeException("Not all sides have been rendered.");
		super.end(x, y, width, height);
	}

	/** @return the color buffer cubemap */
	public Cubemap getColorBufferCubemap () {
		return (Cubemap) colorTexture;
	}
}

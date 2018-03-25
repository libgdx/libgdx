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
 * automatically create a cubemap for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * cubemap by {@link FrameBufferCubemap#getColorBufferTexture()}. This class will only work with OpenGL ES 2.0.
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
 * FrameBufferCubemap frameBuffer = new FrameBufferCubemap(Format.RGBA8888, fSize, fSize, true); <br />
 * frameBuffer.begin(); <br />
 * while( frameBuffer.nextSide() ) { <br />
 * frameBuffer.getSide().getUp(camera.up); <br />
 * frameBuffer.getSide().getDirection(camera.direction);<br />
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
public class FrameBufferCubemap extends GLFrameBuffer<Cubemap> {

	/** the zero-based index of the active side **/
	private int currentSide;

	/** cubemap sides cache */
	private static final Cubemap.CubemapSide[] cubemapSides = Cubemap.CubemapSide.values();

	FrameBufferCubemap () {}

	/**
	 * Creates a GLFrameBuffer from the specifications provided by bufferBuilder
	 *
	 * @param bufferBuilder
	 **/
	protected FrameBufferCubemap (GLFrameBufferBuilder<? extends GLFrameBuffer<Cubemap>> bufferBuilder) {
		super(bufferBuilder);
	}

	/** Creates a new FrameBuffer having the given dimensions and potentially a depth buffer attached.
	 *
	 * @param format
	 * @param width
	 * @param height
	 * @param hasDepth */
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
		FrameBufferCubemapBuilder frameBufferBuilder = new FrameBufferCubemapBuilder(width, height);
		frameBufferBuilder.addBasicColorTextureAttachment(format);
		if (hasDepth) frameBufferBuilder.addBasicDepthRenderBuffer();
		if (hasStencil) frameBufferBuilder.addBasicStencilRenderBuffer();
		this.bufferBuilder = frameBufferBuilder;

		build();
	}


	@Override
	protected Cubemap createTexture (FrameBufferTextureAttachmentSpec attachmentSpec) {
		GLOnlyTextureData data = new GLOnlyTextureData(bufferBuilder.width, bufferBuilder.height, 0, attachmentSpec.internalFormat, attachmentSpec.format, attachmentSpec.type);
		Cubemap result = new Cubemap(data, data, data, data, data, data);
		result.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		result.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		return result;
	}

	@Override
	protected void disposeColorTexture (Cubemap colorTexture) {
		colorTexture.dispose();
	}

	@Override
	protected void attachFrameBufferColorTexture (Cubemap texture) {
		GL20 gl = Gdx.gl20;
		int glHandle = texture.getTextureObjectHandle();
		Cubemap.CubemapSide[] sides = Cubemap.CubemapSide.values();
		for (Cubemap.CubemapSide side : sides) {
			gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, side.glEnum,
				glHandle, 0);
		}
	}

	/** Makes the frame buffer current so everything gets drawn to it, must be followed by call to either {@link #nextSide()} or
	 * {@link #bindSide(com.badlogic.gdx.graphics.Cubemap.CubemapSide)} to activate the side to render onto. */
	@Override
	public void bind () {
		currentSide = -1;
		super.bind();
	}

	/** Bind the next side of cubemap and return false if no more side. Should be called in between a call to {@link #begin()} and
	 * #end to cycle to each side of the cubemap to render on. */
	public boolean nextSide () {
		if (currentSide > 5) {
			throw new GdxRuntimeException("No remaining sides.");
		} else if (currentSide == 5) {
			return false;
		}

		currentSide++;
		bindSide(getSide());
		return true;
	}

	/** Bind the side, making it active to render on. Should be called in between a call to {@link #begin()} and {@link #end()}.
	 * @param side The side to bind */
	protected void bindSide (final Cubemap.CubemapSide side) {
		Gdx.gl20.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, side.glEnum, getColorBufferTexture().getTextureObjectHandle(), 0);
	}

	/** Get the currently bound side. */
	public Cubemap.CubemapSide getSide () {
		return currentSide < 0 ? null : cubemapSides [currentSide];
	}
}

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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses. It will
 * automatically create a texture for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * texture by {@link FrameBuffer#getColorBufferTexture()}. This class will only work with OpenGL ES 2.0.
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
 * @author mzechner, realitix
 */
public class FrameBuffer extends GLFrameBuffer<Texture> {
	
	private static final IntBuffer TEMP_BUFFER = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asIntBuffer();
	
	private int previousFrameBuffer;
	private int[] previousViewport;
	private boolean bound;
	
	FrameBuffer() {
		previousFrameBuffer = -1;
		previousViewport = new int[4];
		bound = false;
	}
	
	/**
	 * Creates a GLFrameBuffer from the specifications provided by bufferBuilder
	 *
	 * @param bufferBuilder
	 **/
	protected FrameBuffer(GLFrameBufferBuilder<? extends GLFrameBuffer<Texture>> bufferBuilder) {
		super(bufferBuilder);
		
		previousFrameBuffer = -1;
		previousViewport = new int[4];
		bound = false;
	}
	
	/**
	 * Creates a new FrameBuffer having the given dimensions and potentially a depth buffer attached.
	 */
	public FrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth) {
		this(format, width, height, hasDepth, false);
	}
	
	/**
	 * Creates a new FrameBuffer having the given dimensions and potentially a depth and a stencil buffer attached.
	 *
	 * @param format   the format of the color buffer; according to the OpenGL ES 2.0 spec, only RGB565, RGBA4444 and RGB5_A1 are
	 *                 color-renderable
	 * @param width    the width of the framebuffer in pixels
	 * @param height   the height of the framebuffer in pixels
	 * @param hasDepth whether to attach a depth buffer
	 * @throws com.badlogic.gdx.utils.GdxRuntimeException in case the FrameBuffer could not be created
	 */
	public FrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil) {
		FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(width, height);
		frameBufferBuilder.addBasicColorTextureAttachment(format);
		if (hasDepth)
			frameBufferBuilder.addBasicDepthRenderBuffer();
		if (hasStencil)
			frameBufferBuilder.addBasicStencilRenderBuffer();
		this.bufferBuilder = frameBufferBuilder;
		
		previousFrameBuffer = -1;
		previousViewport = new int[4];
		bound = false;
		
		build();
	}
	
	/**
	 * @return Returns the previously bound framebuffer handle, or -1 if non-existent
	 */
	public int getPreviousFrameBuffer() {
		return previousFrameBuffer;
	}
	
	@Override
	protected Texture createTexture(FrameBufferTextureAttachmentSpec attachmentSpec) {
		GLOnlyTextureData data = new GLOnlyTextureData(bufferBuilder.width, bufferBuilder.height, 0, attachmentSpec.internalFormat,
				attachmentSpec.format, attachmentSpec.type);
		Texture result = new Texture(data);
		// Filtering support for depth textures on WebGL is spotty https://github.com/KhronosGroup/OpenGL-API/issues/84
		boolean webGLDepth = attachmentSpec.isDepth && Gdx.app.getType() == Application.ApplicationType.WebGL;
		if (!webGLDepth) {
			result.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}
		result.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		return result;
	}
	
	@Override
	protected void disposeColorTexture(Texture colorTexture) {
		colorTexture.dispose();
	}
	
	@Override
	protected void attachFrameBufferColorTexture(Texture texture) {
		Gdx.gl20.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D,
				texture.getTextureObjectHandle(), 0);
	}
	
	@Override
	public void begin() {
		if (bound)
			throw new IllegalStateException("FrameBuffer.end must be called before beginning again.");
		bound = true;
		
		previousFrameBuffer = FrameBuffer.getCurrentFrameBuffer();
		bind();
		
		FrameBuffer.getCurrentViewport(previousViewport);
		setFrameBufferViewport();
	}
	
	@Override
	public void end() {
		end(previousViewport[0], previousViewport[1], previousViewport[2], previousViewport[3]);
	}
	
	@Override
	public void end(int x, int y, int width, int height) {
		if (!bound)
			throw new IllegalStateException("FrameBuffer.begin must be called before ending.");
		bound = false;
		
		if (FrameBuffer.getCurrentFrameBuffer() != framebufferHandle)
			throw new IllegalStateException("Invalid framebuffer handle. Make sure framebuffers are closed in the same binding order.");
		
		Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, previousFrameBuffer);
		Gdx.gl20.glViewport(x, y, width, height);
	}
	
	/**
	 * @return Returns the currently bound framebuffer handle
	 */
	private static synchronized int getCurrentFrameBuffer() {
		Gdx.gl20.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, TEMP_BUFFER);
		return TEMP_BUFFER.get(0);
	}
	
	/**
	 * Writes the currently set viewport to an int-array containing
	 * the x, y, width, and height.
	 *
	 * @param viewport The array to write to
	 */
	private static synchronized void getCurrentViewport(final int[] viewport) {
		Gdx.gl20.glGetIntegerv(GL20.GL_VIEWPORT, TEMP_BUFFER);
		viewport[0] = TEMP_BUFFER.get(0);
		viewport[1] = TEMP_BUFFER.get(1);
		viewport[2] = TEMP_BUFFER.get(2);
		viewport[3] = TEMP_BUFFER.get(3);
	}
	
	/**
	 * See {@link GLFrameBuffer#unbind()}
	 */
	public static void unbind() {
		GLFrameBuffer.unbind();
	}
	
}

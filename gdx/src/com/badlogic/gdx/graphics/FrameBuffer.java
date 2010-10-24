/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

/**
 * <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses. It will
 * automatically create a texture for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * texture by {@link FrameBuffer.getColorBufferTexture()}. This class will only work with OpenGL ES 2.0.
 * </p>
 * 
 * <p>
 * FrameBuffers can be managed. In case of an OpenGL context loss, which only happens on Android when a user switches to another
 * application or receives an incoming call, the framebuffer will be automatically recreated. This will essentially double the
 * size of the memory used so use this feature with care. Future versions will fix this and not eat up additional memory.
 * </p>
 * 
 * @author mzechner
 * 
 */
public class FrameBuffer {
	/** the frame buffers **/
	private final static ArrayList<FrameBuffer> buffers = new ArrayList<FrameBuffer>();

	/** the color buffer texture **/
	private Texture colorTexture;

	/** the framebuffer handle **/
	private int framebufferHandle;

	/** the depthbuffer render object handle **/
	private int depthbufferHandle;

	/** width **/
	private final int width;

	/** height **/
	private final int height;

	/** format **/
	private final Pixmap.Format format;

	/**
	 * Creates a new FrameBuffer having the given dimensions and potentially a depth buffer attached.
	 * 
	 * @param format the format of the color buffer
	 * @param width the width of the framebuffer in pixels
	 * @param height the height of the framebuffer in pixels
	 * @param hasDepth whether to attach a depth buffer
	 * @param managed whether this framebuffer should be managed
	 * @throws GdxRuntimeException in case the FraeBuffer could not be created
	 */
	public FrameBuffer (Pixmap.Format format, int width, int height, boolean hasDepth) {
		this.width = width;
		this.height = height;
		this.format = format;
		build();

		buffers.add(this);
	}

	private void build () {
		colorTexture = Gdx.graphics.newUnmanagedTexture(width, height, format, TextureFilter.Linear, TextureFilter.Linear,
			TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		GL20 gl = Gdx.graphics.getGL20();

		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer handle = tmp.asIntBuffer();

		gl.glGenFramebuffers(1, handle);
		framebufferHandle = handle.get(0);

		gl.glGenRenderbuffers(1, handle);
		depthbufferHandle = handle.get(0);

		gl.glBindTexture(GL20.GL_TEXTURE_2D, colorTexture.getTextureObjectHandle());
		gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, depthbufferHandle);
		gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL20.GL_DEPTH_COMPONENT16, colorTexture.getWidth(), colorTexture.getHeight());

		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
		gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D,
			colorTexture.getTextureObjectHandle(), 0);
		gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_RENDERBUFFER, depthbufferHandle);
		int result = gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);

		gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);
		gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, 0);

		if (result != GL20.GL_FRAMEBUFFER_COMPLETE) {
			colorTexture.dispose();
			handle.put(depthbufferHandle);
			handle.flip();
			gl.glDeleteRenderbuffers(1, handle);

			handle.put(framebufferHandle);
			handle.flip();
			gl.glDeleteFramebuffers(1, handle);

			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)
				throw new IllegalStateException("frame buffer couldn't be constructed: incomplete attachment");
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS)
				throw new IllegalStateException("frame buffer couldn't be constructed: incomplete dimensions");
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)
				throw new IllegalStateException("frame buffer couldn't be constructed: missing attachment");
		}
	}

	/**
	 * Releases all resources associated with the FrameBuffer.
	 */
	public void dispose () {
		GL20 gl = Gdx.graphics.getGL20();

		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer handle = tmp.asIntBuffer();

		colorTexture.dispose();
		handle.put(depthbufferHandle);
		handle.flip();
		gl.glDeleteRenderbuffers(1, handle);

		handle.put(framebufferHandle);
		handle.flip();
		gl.glDeleteFramebuffers(1, handle);

		buffers.remove(this);
	}

	/**
	 * Makes the frame buffer current so everything gets drawn to it.
	 */
	public void begin () {
		Gdx.graphics.getGL20().glViewport(0, 0, colorTexture.getWidth(), colorTexture.getHeight());
		Gdx.graphics.getGL20().glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
	}

	/**
	 * Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on.
	 */
	public void end () {
		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.graphics.getGL20().glBindFramebuffer(GL20.GL_FRAMEBUFFER, 0);
	}

	/**
	 * Invalidates all frame buffers. This can be used when the OpenGL context is lost to rebuild all managed frame buffers. This
	 * assumes that the texture attached to this buffer has already been rebuild! Use with care.
	 */
	public static void invalidateAllFrameBuffers () {
		if (Gdx.graphics.getGL20() == null) return;

		for (int i = 0; i < buffers.size(); i++) {
			buffers.get(i).build();
		}
	}

	public static void clearAllFrameBuffers () {
		buffers.clear();
	}

	/**
	 * @return the color buffer texture
	 */
	public Texture getColorBufferTexture () {
		return colorTexture;
	}

	/**
	 * @return the height of the framebuffer in pixels
	 */
	public int getHeight () {
		return colorTexture.getHeight();
	}

	/**
	 * @return the width of the framebuffer in pixels
	 */
	public int getWidth () {
		return colorTexture.getWidth();
	}
}

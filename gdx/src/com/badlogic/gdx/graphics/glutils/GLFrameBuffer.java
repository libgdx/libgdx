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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/** <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses. It will
 * automatically create a gltexture for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * gltexture by {@link GLFrameBuffer#getColorBufferTexture()}. This class will only work with OpenGL ES 2.0.
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
 * @author mzechner, realitix */
public abstract class GLFrameBuffer<T extends GLTexture> implements Disposable {
	/** the frame buffers **/
	private final static Map<Application, Array<GLFrameBuffer>> buffers = new HashMap<Application, Array<GLFrameBuffer>>();

	private final static int GL_DEPTH24_STENCIL8_OES = 0x88F0;

	/** the color buffer texture **/
	protected T colorTexture;

	/** the default framebuffer handle, a.k.a screen. */
	private static int defaultFramebufferHandle;
	/** true if we have polled for the default handle already. */
	private static boolean defaultFramebufferHandleInitialized = false;

	/** the framebuffer handle **/
	private int framebufferHandle;

	/** the depthbuffer render object handle **/
	private int depthbufferHandle;

	/** the stencilbuffer render object handle **/
	private int stencilbufferHandle;

	/** the depth stencil packed render buffer object handle **/
	private int depthStencilPackedBufferHandle;

	/** width **/
	protected final int width;

	/** height **/
	protected final int height;

	/** depth **/
	protected final boolean hasDepth;

	/** stencil **/
	protected final boolean hasStencil;

	/** if has depth stencil packed buffer **/
	private boolean hasDepthStencilPackedBuffer;

	/** format **/
	protected final Pixmap.Format format;

	/** Creates a new FrameBuffer having the given dimensions and potentially a depth buffer attached.
	 *
	 * @param format
	 * @param width
	 * @param height
	 * @param hasDepth */
	public GLFrameBuffer (Pixmap.Format format, int width, int height, boolean hasDepth) {
		this(format, width, height, hasDepth, false);
	}

	/** Creates a new FrameBuffer having the given dimensions and potentially a depth and a stencil buffer attached.
	 *
	 * @param format the format of the color buffer; according to the OpenGL ES 2.0 spec, only RGB565, RGBA4444 and RGB5_A1 are
	 *           color-renderable
	 * @param width the width of the framebuffer in pixels
	 * @param height the height of the framebuffer in pixels
	 * @param hasDepth whether to attach a depth buffer
	 * @throws com.badlogic.gdx.utils.GdxRuntimeException in case the FrameBuffer could not be created */
	public GLFrameBuffer (Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil) {
		this.width = width;
		this.height = height;
		this.format = format;
		this.hasDepth = hasDepth;
		this.hasStencil = hasStencil;
		build();

		addManagedFrameBuffer(Gdx.app, this);
	}

	/** Override this method in a derived class to set up the backing texture as you like. */
	protected abstract T createColorTexture ();
	
	/** Override this method in a derived class to dispose the backing texture as you like. */
	protected abstract void disposeColorTexture (T colorTexture);

	private void build () {
		GL20 gl = Gdx.gl20;

		// iOS uses a different framebuffer handle! (not necessarily 0)
		if (!defaultFramebufferHandleInitialized) {
			defaultFramebufferHandleInitialized = true;
			if (Gdx.app.getType() == ApplicationType.iOS) {
				IntBuffer intbuf = ByteBuffer.allocateDirect(16 * Integer.SIZE / 8).order(ByteOrder.nativeOrder()).asIntBuffer();
				gl.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, intbuf);
				defaultFramebufferHandle = intbuf.get(0);
			} else {
				defaultFramebufferHandle = 0;
			}
		}

		colorTexture = createColorTexture();

		framebufferHandle = gl.glGenFramebuffer();

		if (hasDepth) {
			depthbufferHandle = gl.glGenRenderbuffer();
		}

		if (hasStencil) {
			stencilbufferHandle = gl.glGenRenderbuffer();
		}

		gl.glBindTexture(GL20.GL_TEXTURE_2D, colorTexture.getTextureObjectHandle());

		if (hasDepth) {
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, depthbufferHandle);
			gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL20.GL_DEPTH_COMPONENT16, colorTexture.getWidth(),
				colorTexture.getHeight());
		}

		if (hasStencil) {
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, stencilbufferHandle);
			gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL20.GL_STENCIL_INDEX8, colorTexture.getWidth(), colorTexture.getHeight());
		}

		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
		gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D,
			colorTexture.getTextureObjectHandle(), 0);
		if (hasDepth) {
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_RENDERBUFFER, depthbufferHandle);
		}

		if (hasStencil) {
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER, stencilbufferHandle);
		}

		gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);
		gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);

		int result = gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);

		if (result == GL20.GL_FRAMEBUFFER_UNSUPPORTED && hasDepth && hasStencil
			&& (Gdx.graphics.supportsExtension("GL_OES_packed_depth_stencil") ||
				Gdx.graphics.supportsExtension("GL_EXT_packed_depth_stencil"))) {
			if (hasDepth) {
				gl.glDeleteRenderbuffer(depthbufferHandle);
				depthbufferHandle = 0;
			}
			if (hasStencil) {
				gl.glDeleteRenderbuffer(stencilbufferHandle);
				stencilbufferHandle = 0;
			}

			depthStencilPackedBufferHandle = gl.glGenRenderbuffer();
			hasDepthStencilPackedBuffer = true;
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, depthStencilPackedBufferHandle);
			gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL_DEPTH24_STENCIL8_OES, colorTexture.getWidth(), colorTexture.getHeight());
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);

			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_RENDERBUFFER, depthStencilPackedBufferHandle);
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER, depthStencilPackedBufferHandle);
			result = gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);
		}

		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultFramebufferHandle);

		if (result != GL20.GL_FRAMEBUFFER_COMPLETE) {
			disposeColorTexture(colorTexture);

			if (hasDepthStencilPackedBuffer) {
				gl.glDeleteBuffer(depthStencilPackedBufferHandle);
			} else {
				if (hasDepth) gl.glDeleteRenderbuffer(depthbufferHandle);
				if (hasStencil) gl.glDeleteRenderbuffer(stencilbufferHandle);
			}

			gl.glDeleteFramebuffer(framebufferHandle);

			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)
				throw new IllegalStateException("frame buffer couldn't be constructed: incomplete attachment");
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS)
				throw new IllegalStateException("frame buffer couldn't be constructed: incomplete dimensions");
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)
				throw new IllegalStateException("frame buffer couldn't be constructed: missing attachment");
			if (result == GL20.GL_FRAMEBUFFER_UNSUPPORTED)
				throw new IllegalStateException("frame buffer couldn't be constructed: unsupported combination of formats");
			throw new IllegalStateException("frame buffer couldn't be constructed: unknown error " + result);
		}
	}

	/** Releases all resources associated with the FrameBuffer. */
	@Override
	public void dispose () {
		GL20 gl = Gdx.gl20;

		disposeColorTexture(colorTexture);

		if (hasDepthStencilPackedBuffer) {
			gl.glDeleteRenderbuffer(depthStencilPackedBufferHandle);
		} else {
			if (hasDepth) gl.glDeleteRenderbuffer(depthbufferHandle);
			if (hasStencil) gl.glDeleteRenderbuffer(stencilbufferHandle);
		}

		gl.glDeleteFramebuffer(framebufferHandle);

		if (buffers.get(Gdx.app) != null) buffers.get(Gdx.app).removeValue(this, true);
	}

	/** Makes the frame buffer current so everything gets drawn to it. */
	public void bind () {
		Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
	}

	/** Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on. */
	public static void unbind () {
		Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultFramebufferHandle);
	}

	/** Binds the frame buffer and sets the viewport accordingly, so everything gets drawn to it. */
	public void begin () {
		bind();
		setFrameBufferViewport();
	}

	/** Sets viewport to the dimensions of framebuffer. Called by {@link #begin()}. */
	protected void setFrameBufferViewport () {
		Gdx.gl20.glViewport(0, 0, colorTexture.getWidth(), colorTexture.getHeight());
	}

	/** Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on. */
	public void end () {
		end(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
	}

	/** Unbinds the framebuffer and sets viewport sizes, all drawing will be performed to the normal framebuffer from here on.
	 *
	 * @param x the x-axis position of the viewport in pixels
	 * @param y the y-asis position of the viewport in pixels
	 * @param width the width of the viewport in pixels
	 * @param height the height of the viewport in pixels */
	public void end (int x, int y, int width, int height) {
		unbind();
		Gdx.gl20.glViewport(x, y, width, height);
	}

	/** @return the gl texture */
	public T getColorBufferTexture () {
		return colorTexture;
	}

	/** @return The OpenGL handle of the framebuffer (see {@link GL20#glGenFramebuffer()}) */
	public int getFramebufferHandle () {
		return framebufferHandle;
	}

	/** @return The OpenGL handle of the (optional) depth buffer (see {@link GL20#glGenRenderbuffer()}). May return 0 even if depth buffer enabled */
	public int getDepthBufferHandle () {
		return depthbufferHandle;
	}

	/** @return The OpenGL handle of the (optional) stencil buffer (see {@link GL20#glGenRenderbuffer()}). May return 0 even if stencil buffer enabled */
	public int getStencilBufferHandle () {
		return stencilbufferHandle;
	}
	
	/** @return The OpenGL handle of the packed depth & stencil buffer (GL_DEPTH24_STENCIL8_OES) or 0 if not used. **/
	protected int getDepthStencilPackedBuffer () {
		return depthStencilPackedBufferHandle;
	}

	/** @return the height of the framebuffer in pixels */
	public int getHeight () {
		return colorTexture.getHeight();
	}

	/** @return the width of the framebuffer in pixels */
	public int getWidth () {
		return colorTexture.getWidth();
	}

	/** @return the depth of the framebuffer in pixels (if applicable) */
	public int getDepth () {
		return colorTexture.getDepth();
	}

	private static void addManagedFrameBuffer (Application app, GLFrameBuffer frameBuffer) {
		Array<GLFrameBuffer> managedResources = buffers.get(app);
		if (managedResources == null) managedResources = new Array<GLFrameBuffer>();
		managedResources.add(frameBuffer);
		buffers.put(app, managedResources);
	}

	/** Invalidates all frame buffers. This can be used when the OpenGL context is lost to rebuild all managed frame buffers. This
	 * assumes that the texture attached to this buffer has already been rebuild! Use with care. */
	public static void invalidateAllFrameBuffers (Application app) {
		if (Gdx.gl20 == null) return;

		Array<GLFrameBuffer> bufferArray = buffers.get(app);
		if (bufferArray == null) return;
		for (int i = 0; i < bufferArray.size; i++) {
			bufferArray.get(i).build();
		}
	}

	public static void clearAllFrameBuffers (Application app) {
		buffers.remove(app);
	}

	public static StringBuilder getManagedStatus (final StringBuilder builder) {
		builder.append("Managed buffers/app: { ");
		for (Application app : buffers.keySet()) {
			builder.append(buffers.get(app).size);
			builder.append(" ");
		}
		builder.append("}");
		return builder;
	}

	public static String getManagedStatus () {
		return getManagedStatus(new StringBuilder()).toString();
	}
}

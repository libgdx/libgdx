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
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * <p>
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
	protected final static Map<Application, Array<GLFrameBuffer>> buffers = new HashMap<Application, Array<GLFrameBuffer>>();

	protected final static int GL_DEPTH24_STENCIL8_OES = 0x88F0;

	/** the color buffer texture **/
	protected Array<T> textureAttachments = new Array<T>();

	/** the default framebuffer handle, a.k.a screen. */
	protected static int defaultFramebufferHandle;
	/** true if we have polled for the default handle already. */
	protected static boolean defaultFramebufferHandleInitialized = false;

	/** the framebuffer handle **/
	protected int framebufferHandle;
	/** the depthbuffer render object handle **/
	protected int depthbufferHandle;
	/** the stencilbuffer render object handle **/
	protected int stencilbufferHandle;
	/** the depth stencil packed render buffer object handle **/
	protected int depthStencilPackedBufferHandle;
	/** if has depth stencil packed buffer **/
	protected boolean hasDepthStencilPackedBuffer;

	/** if multiple texture attachments are present **/
	protected boolean isMRT;

	protected GLFrameBufferBuilder<? extends GLFrameBuffer<T>> bufferBuilder;

	GLFrameBuffer () {
	}

	/** Creates a GLFrameBuffer from the specifications provided by bufferBuilder **/
	protected GLFrameBuffer (GLFrameBufferBuilder<? extends GLFrameBuffer<T>> bufferBuilder) {
		this.bufferBuilder = bufferBuilder;
		build();
	}

	/** Convenience method to return the first Texture attachment present in the fbo **/
	public T getColorBufferTexture () {
		return textureAttachments.first();
	}

	/** Return the Texture attachments attached to the fbo **/
	public Array<T> getTextureAttachments () {
		return textureAttachments;
	}

	/** Override this method in a derived class to set up the backing texture as you like. */
	protected abstract T createTexture (FrameBufferTextureAttachmentSpec attachmentSpec);

	/** Override this method in a derived class to dispose the backing texture as you like. */
	protected abstract void disposeColorTexture (T colorTexture);

	/** Override this method in a derived class to attach the backing texture to the GL framebuffer object. */
	protected abstract void attachFrameBufferColorTexture (T texture);

	protected void build () {
		GL20 gl = Gdx.gl20;

		checkValidBuilder();

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

		framebufferHandle = gl.glGenFramebuffer();
		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);

		int width = bufferBuilder.width;
		int height = bufferBuilder.height;

		if (bufferBuilder.hasDepthRenderBuffer) {
			depthbufferHandle = gl.glGenRenderbuffer();
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, depthbufferHandle);
			gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, bufferBuilder.depthRenderBufferSpec.internalFormat, width, height);
		}

		if (bufferBuilder.hasStencilRenderBuffer) {
			stencilbufferHandle = gl.glGenRenderbuffer();
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, stencilbufferHandle);
			gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, bufferBuilder.stencilRenderBufferSpec.internalFormat, width, height);
		}

		if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
			depthStencilPackedBufferHandle = gl.glGenRenderbuffer();
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, depthStencilPackedBufferHandle);
			gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, bufferBuilder.packedStencilDepthRenderBufferSpec.internalFormat, width,
				height);
		}

		isMRT = bufferBuilder.textureAttachmentSpecs.size > 1;
		int colorTextureCounter = 0;
		if (isMRT) {
			for (FrameBufferTextureAttachmentSpec attachmentSpec : bufferBuilder.textureAttachmentSpecs) {
				T texture = createTexture(attachmentSpec);
				textureAttachments.add(texture);
				if (attachmentSpec.isColorTexture()) {
					gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + colorTextureCounter, GL30.GL_TEXTURE_2D,
						texture.getTextureObjectHandle(), 0);
					colorTextureCounter++;
				} else if (attachmentSpec.isDepth) {
					gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_TEXTURE_2D,
						texture.getTextureObjectHandle(), 0);
				} else if (attachmentSpec.isStencil) {
					gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_STENCIL_ATTACHMENT, GL20.GL_TEXTURE_2D,
						texture.getTextureObjectHandle(), 0);
				}
			}
		} else {
			T texture = createTexture(bufferBuilder.textureAttachmentSpecs.first());
			textureAttachments.add(texture);
			gl.glBindTexture(texture.glTarget, texture.getTextureObjectHandle());
		}

		if (isMRT) {
			IntBuffer buffer = BufferUtils.newIntBuffer(colorTextureCounter);
			for (int i = 0; i < colorTextureCounter; i++) {
				buffer.put(GL30.GL_COLOR_ATTACHMENT0 + i);
			}
			buffer.position(0);
			Gdx.gl30.glDrawBuffers(colorTextureCounter, buffer);
		} else {
			attachFrameBufferColorTexture(textureAttachments.first());
		}

		if (bufferBuilder.hasDepthRenderBuffer) {
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_RENDERBUFFER, depthbufferHandle);
		}

		if (bufferBuilder.hasStencilRenderBuffer) {
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER, stencilbufferHandle);
		}

		if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER,
				depthStencilPackedBufferHandle);
		}

		gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);
		for (T texture : textureAttachments) {
			gl.glBindTexture(texture.glTarget, 0);
		}

		int result = gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);

		if (result == GL20.GL_FRAMEBUFFER_UNSUPPORTED && bufferBuilder.hasDepthRenderBuffer && bufferBuilder.hasStencilRenderBuffer
			&& (Gdx.graphics.supportsExtension("GL_OES_packed_depth_stencil")
				|| Gdx.graphics.supportsExtension("GL_EXT_packed_depth_stencil"))) {
			if (bufferBuilder.hasDepthRenderBuffer) {
				gl.glDeleteRenderbuffer(depthbufferHandle);
				depthbufferHandle = 0;
			}
			if (bufferBuilder.hasStencilRenderBuffer) {
				gl.glDeleteRenderbuffer(stencilbufferHandle);
				stencilbufferHandle = 0;
			}
			if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
				gl.glDeleteRenderbuffer(depthStencilPackedBufferHandle);
				depthStencilPackedBufferHandle = 0;
			}

			depthStencilPackedBufferHandle = gl.glGenRenderbuffer();
			hasDepthStencilPackedBuffer = true;
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, depthStencilPackedBufferHandle);
			gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL_DEPTH24_STENCIL8_OES, width, height);
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);

			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_RENDERBUFFER,
				depthStencilPackedBufferHandle);
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER,
				depthStencilPackedBufferHandle);
			result = gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);
		}

		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultFramebufferHandle);

		if (result != GL20.GL_FRAMEBUFFER_COMPLETE) {
			for (T texture : textureAttachments) {
				disposeColorTexture(texture);
			}

			if (hasDepthStencilPackedBuffer) {
				gl.glDeleteBuffer(depthStencilPackedBufferHandle);
			} else {
				if (bufferBuilder.hasDepthRenderBuffer) gl.glDeleteRenderbuffer(depthbufferHandle);
				if (bufferBuilder.hasStencilRenderBuffer) gl.glDeleteRenderbuffer(stencilbufferHandle);
			}

			gl.glDeleteFramebuffer(framebufferHandle);

			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)
				throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete attachment");
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS)
				throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete dimensions");
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)
				throw new IllegalStateException("Frame buffer couldn't be constructed: missing attachment");
			if (result == GL20.GL_FRAMEBUFFER_UNSUPPORTED)
				throw new IllegalStateException("Frame buffer couldn't be constructed: unsupported combination of formats");
			throw new IllegalStateException("Frame buffer couldn't be constructed: unknown error " + result);
		}

		addManagedFrameBuffer(Gdx.app, this);
	}

	private void checkValidBuilder () {
		boolean runningGL30 = Gdx.graphics.isGL30Available();

		if (!runningGL30) {
			if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
				throw new GdxRuntimeException("Packed Stencil/Render render buffers are not available on GLES 2.0");
			}
			if (bufferBuilder.textureAttachmentSpecs.size > 1) {
				throw new GdxRuntimeException("Multiple render targets not available on GLES 2.0");
			}
			for (FrameBufferTextureAttachmentSpec spec : bufferBuilder.textureAttachmentSpecs) {
				if (spec.isDepth) throw new GdxRuntimeException("Depth texture FrameBuffer Attachment not available on GLES 2.0");
				if (spec.isStencil) throw new GdxRuntimeException("Stencil texture FrameBuffer Attachment not available on GLES 2.0");
				if (spec.isFloat) {
					if (!Gdx.graphics.supportsExtension("OES_texture_float")) {
						throw new GdxRuntimeException("Float texture FrameBuffer Attachment not available on GLES 2.0");
					}
				}
			}
		}
	}

	/** Releases all resources associated with the FrameBuffer. */
	@Override
	public void dispose () {
		GL20 gl = Gdx.gl20;

		for (T texture : textureAttachments) {
			disposeColorTexture(texture);
		}

		if (hasDepthStencilPackedBuffer) {
			gl.glDeleteRenderbuffer(depthStencilPackedBufferHandle);
		} else {
			if (bufferBuilder.hasDepthRenderBuffer) gl.glDeleteRenderbuffer(depthbufferHandle);
			if (bufferBuilder.hasStencilRenderBuffer) gl.glDeleteRenderbuffer(stencilbufferHandle);
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
		Gdx.gl20.glViewport(0, 0, bufferBuilder.width, bufferBuilder.height);
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

	/** @return The OpenGL handle of the framebuffer (see {@link GL20#glGenFramebuffer()}) */
	public int getFramebufferHandle () {
		return framebufferHandle;
	}

	/** @return The OpenGL handle of the (optional) depth buffer (see {@link GL20#glGenRenderbuffer()}). May return 0 even if depth
	 *         buffer enabled */
	public int getDepthBufferHandle () {
		return depthbufferHandle;
	}

	/** @return The OpenGL handle of the (optional) stencil buffer (see {@link GL20#glGenRenderbuffer()}). May return 0 even if
	 *         stencil buffer enabled */
	public int getStencilBufferHandle () {
		return stencilbufferHandle;
	}

	/** @return The OpenGL handle of the packed depth & stencil buffer (GL_DEPTH24_STENCIL8_OES) or 0 if not used. **/
	protected int getDepthStencilPackedBuffer () {
		return depthStencilPackedBufferHandle;
	}

	/** @return the height of the framebuffer in pixels */
	public int getHeight () {
		return bufferBuilder.height;
	}

	/** @return the width of the framebuffer in pixels */
	public int getWidth () {
		return bufferBuilder.width;
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

	protected static class FrameBufferTextureAttachmentSpec {
		int internalFormat, format, type;
		boolean isFloat, isGpuOnly;
		boolean isDepth;
		boolean isStencil;

		public FrameBufferTextureAttachmentSpec (int internalformat, int format, int type) {
			this.internalFormat = internalformat;
			this.format = format;
			this.type = type;
		}

		public boolean isColorTexture () {
			return !isDepth && !isStencil;
		}
	}

	protected static class FrameBufferRenderBufferAttachmentSpec {
		int internalFormat;

		public FrameBufferRenderBufferAttachmentSpec (int internalFormat) {
			this.internalFormat = internalFormat;
		}
	}

	protected static abstract class GLFrameBufferBuilder<U extends GLFrameBuffer<? extends GLTexture>> {
		protected int width, height;

		protected Array<FrameBufferTextureAttachmentSpec> textureAttachmentSpecs = new Array<FrameBufferTextureAttachmentSpec>();

		protected FrameBufferRenderBufferAttachmentSpec stencilRenderBufferSpec;
		protected FrameBufferRenderBufferAttachmentSpec depthRenderBufferSpec;
		protected FrameBufferRenderBufferAttachmentSpec packedStencilDepthRenderBufferSpec;

		protected boolean hasStencilRenderBuffer;
		protected boolean hasDepthRenderBuffer;
		protected boolean hasPackedStencilDepthRenderBuffer;

		public GLFrameBufferBuilder (int width, int height) {
			this.width = width;
			this.height = height;
		}

		public GLFrameBufferBuilder<U> addColorTextureAttachment (int internalFormat, int format, int type) {
			textureAttachmentSpecs.add(new FrameBufferTextureAttachmentSpec(internalFormat, format, type));
			return this;
		}

		public GLFrameBufferBuilder<U> addBasicColorTextureAttachment (Pixmap.Format format) {
			int glFormat = Pixmap.Format.toGlFormat(format);
			int glType = Pixmap.Format.toGlType(format);
			return addColorTextureAttachment(glFormat, glFormat, glType);
		}

		public GLFrameBufferBuilder<U> addFloatAttachment (int internalFormat, int format, int type, boolean gpuOnly) {
			FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat, format, type);
			spec.isFloat = true;
			spec.isGpuOnly = gpuOnly;
			textureAttachmentSpecs.add(spec);
			return this;
		}

		public GLFrameBufferBuilder<U> addDepthTextureAttachment (int internalFormat, int type) {
			FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat, GL30.GL_DEPTH_COMPONENT,
				type);
			spec.isDepth = true;
			textureAttachmentSpecs.add(spec);
			return this;
		}

		public GLFrameBufferBuilder<U> addStencilTextureAttachment (int internalFormat, int type) {
			FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat, GL30.GL_STENCIL_ATTACHMENT,
				type);
			spec.isStencil = true;
			textureAttachmentSpecs.add(spec);
			return this;
		}

		public GLFrameBufferBuilder<U> addDepthRenderBuffer (int internalFormat) {
			depthRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
			hasDepthRenderBuffer = true;
			return this;
		}

		public GLFrameBufferBuilder<U> addStencilRenderBuffer (int internalFormat) {
			stencilRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
			hasStencilRenderBuffer = true;
			return this;
		}

		public GLFrameBufferBuilder<U> addStencilDepthPackedRenderBuffer (int internalFormat) {
			packedStencilDepthRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
			hasPackedStencilDepthRenderBuffer = true;
			return this;
		}

		public GLFrameBufferBuilder<U> addBasicDepthRenderBuffer () {
			return addDepthRenderBuffer(GL20.GL_DEPTH_COMPONENT16);
		}

		public GLFrameBufferBuilder<U> addBasicStencilRenderBuffer () {
			return addStencilRenderBuffer(GL20.GL_STENCIL_INDEX8);
		}

		public GLFrameBufferBuilder<U> addBasicStencilDepthPackedRenderBuffer () {
			return addStencilDepthPackedRenderBuffer(GL30.GL_DEPTH24_STENCIL8);
		}

		public abstract U build ();
	}

	public static class FrameBufferBuilder extends GLFrameBufferBuilder<FrameBuffer> {
		public FrameBufferBuilder (int width, int height) {
			super(width, height);
		}

		@Override
		public FrameBuffer build () {
			return new FrameBuffer(this);
		}
	}

	public static class FloatFrameBufferBuilder extends GLFrameBufferBuilder<FloatFrameBuffer> {
		public FloatFrameBufferBuilder (int width, int height) {
			super(width, height);
		}

		@Override
		public FloatFrameBuffer build () {
			return new FloatFrameBuffer(this);
		}
	}

	public static class FrameBufferCubemapBuilder extends GLFrameBufferBuilder<FrameBufferCubemap> {
		public FrameBufferCubemapBuilder (int width, int height) {
			super(width, height);
		}

		@Override
		public FrameBufferCubemap build () {
			return new FrameBufferCubemap(this);
		}
	}
}

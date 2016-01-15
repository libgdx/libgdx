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
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/** <p>
 * Encapsulates OpenGL ES 2.0/3.0 frame buffer objects.
 * This class should cover all framebuffer uses, including MRT, depth and depth+stencil textures/renderbuffers, and floating-point formats.
 * Multi-sample FBOs are currently not supported.
 * </p>
 *
 * <p>
 * FrameBufferPluses are managed. In case of an OpenGL context loss, which only happens on Android when a user switches to another
 * application or receives an incoming call, the framebuffer will be automatically recreated.
 * </p>
 *
 * <p>
 * A FrameBufferPlus must be disposed if it is no longer needed
 * </p>
 *
 * @author mzechner, realitix, sastraxi */
public class FrameBufferPlus implements Disposable {

    /** allowed color formats and their respective sample types **/
    protected static final int[] COLOR_FORMATS = {
            GL30.GL_RGBA32F, GL30.GL_RGBA16F,
            GL30.GL_RGB32F, GL30.GL_RGB16F,
            GL30.GL_RGBA8, GL30.GL_RGBA,
            GL30.GL_SRGB8_ALPHA8,
            GL30.GL_SRGB8,
            GL30.GL_RG32F, GL30.GL_RG16F,
            GL30.GL_R32F, GL30.GL_R16F
    };
    protected static final int[] COLOR_TYPES = {
            GL30.GL_FLOAT, GL30.GL_HALF_FLOAT,
            GL30.GL_FLOAT, GL30.GL_HALF_FLOAT,
            GL30.GL_UNSIGNED_BYTE, GL30.GL_UNSIGNED_BYTE,
            GL30.GL_UNSIGNED_BYTE,
            GL30.GL_UNSIGNED_BYTE,
            GL30.GL_FLOAT, GL30.GL_HALF_FLOAT,
            GL30.GL_FLOAT, GL30.GL_HALF_FLOAT
    };

    /** allowed depth formats and their respective sample types **/
    protected static final int[] DEPTH_FORMATS = {
            GL30.GL_DEPTH_COMPONENT24,
            GL30.GL_DEPTH_COMPONENT32F,
            GL30.GL_DEPTH_COMPONENT16
    };
    protected static final int[] DEPTH_TYPES = {
            GL30.GL_UNSIGNED_INT,
            GL30.GL_FLOAT,
            GL30.GL_UNSIGNED_SHORT
    };

    /** allowed depth+stencil formats and their respective sample types **/
    protected static final int[] DEPTH_STENCIL_FORMATS = {
            GL30.GL_DEPTH24_STENCIL8,
            GL30.GL_DEPTH32F_STENCIL8
    };
    protected static final int[] DEPTH_STENCIL_TYPES = {
            GL30.GL_UNSIGNED_INT_24_8,
            GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV
    };

    /** allowed stencil render buffer formats **/
    protected static final int STENCIL_FORMAT = GL20.GL_STENCIL_INDEX8;

    protected static int find(int sought, int[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == sought) return i;
        }
        return -1;
    }

    protected static class Attachment implements Pool.Poolable {
        /* set to define format */
        public boolean isTexture, isDepthStencil;
        public int format, type;
        public int attachment;

        /* set by the build() method -- the "concrete" implementation of this contract */
        public int handle = -1;
        public Texture texture = null;

        /**
         * Make sure the texture (if any) has been disposed before returning to pool!
         */
        @Override
        public void reset() {
			/* -1 because OpenGL handles are unsigned */
            this.handle = -1;
            this.isDepthStencil = false;
            this.texture = null;
        }
    }

    protected static Pool<Attachment> attachmentPool = new ReflectionPool<Attachment>(Attachment.class, 32);

    public static class Builder {
        private final int width;
        private final int height;

        private int colorAttachment;
        private Attachment[] colorAttachments = new Attachment[32];
        private Attachment depthAttachment;

        /** stencil buffer **/
        private boolean hasStencil = false;

        protected boolean isStencilInDepth() {
            return depthAttachment != null && depthAttachment.isDepthStencil;
        }

        public Builder(int width, int height) {
            this.width = width;
            this.height = height;
            this.colorAttachment = 0;
        }

        public Builder addColorTexture(int format) {
            int colorIdx = find(format, COLOR_FORMATS);
            if (colorIdx == -1) {
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: " + format + " is not a valid color format");
            }
            if (colorAttachment > 0 && !Gdx.graphics.isGL30Available()) {
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: GLES 3.0+ is required for MRT");
            }
            this.colorAttachments[this.colorAttachment] = genAttachment(true, COLOR_FORMATS[colorIdx], COLOR_TYPES[colorIdx], GL30.GL_COLOR_ATTACHMENT0 + this.colorAttachment);
            this.colorAttachment++;
            return this;
        }

        public Builder addColorRenderbuffer(int format) {
            int colorIdx = find(format, COLOR_FORMATS);
            if (colorIdx == -1) {
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: " + format + " is not a valid color format");
            }
            if (colorAttachment > 0 && !Gdx.graphics.isGL30Available()) {
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: GLES 3.0+ is required for MRT");
            }
            this.colorAttachments[this.colorAttachment] = genAttachment(false, COLOR_FORMATS[colorIdx], COLOR_TYPES[colorIdx], GL30.GL_COLOR_ATTACHMENT0 + this.colorAttachment);
            this.colorAttachment++;
            return this;
        }

        private Attachment genAttachment(boolean isTexture, int format, int type, int attachment) {
            Attachment a = attachmentPool.obtain();
            a.isTexture = isTexture;
            a.format = format;
            a.type = type;
            a.attachment = attachment;
            return a;
        }

        public Builder addDepthRenderBuffer(int preferredFormat) {
            if (this.depthAttachment != null) {
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: more than one depth attachment specified");
            }
            if (this.hasStencil) {
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: if using depth and stencil, specify a depth+stencil format instead of supplying them separately");
            }

            int depthIdx =  find(preferredFormat, DEPTH_FORMATS);
            int depthStencilIdx = find(preferredFormat, DEPTH_STENCIL_FORMATS);
            if (depthIdx == -1 && depthStencilIdx == -1) {
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: " + preferredFormat + " is not a valid depth or depth+stencil format");
            }

            if (depthStencilIdx != -1) {
                this.hasStencil = true;
                this.depthAttachment = genAttachment(false, DEPTH_STENCIL_FORMATS[depthStencilIdx], DEPTH_STENCIL_TYPES[depthStencilIdx], GL30.GL_DEPTH_ATTACHMENT);
                this.depthAttachment.isDepthStencil = true;
            } else {
                this.depthAttachment = genAttachment(false, DEPTH_FORMATS[depthIdx], DEPTH_TYPES[depthIdx], GL30.GL_DEPTH_ATTACHMENT);
                this.depthAttachment.isDepthStencil = false;
            }
            return this;
        }

        public Builder addDepthTexture(int preferredFormat) {
            if (!Gdx.graphics.isGL30Available()) {
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: GLES 3.0+ is required for depth texture attachment");
            }
            addDepthTexture(preferredFormat);
            this.depthAttachment.isTexture = true;
            return this;
        }

        public Builder addStencilRenderbuffer() {
            if (this.hasStencil) {
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: more than one stencil attachment specified");
            }
            if (this.depthAttachment != null) {
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: if using depth and stencil, specify a depth+stencil format instead of supplying them separately");
            }
            this.hasStencil = true;
            return this;
        }

        public FrameBufferPlus build() {
            Attachment[] finalAttachments = new Attachment[this.colorAttachment + (this.depthAttachment != null || this.hasStencil ? 1 : 0)];
            int i;
            for (i = 0; i < this.colorAttachment; ++i) {
                finalAttachments[i] = this.colorAttachments[i];
            }
            if (this.depthAttachment != null) {
                finalAttachments[i++] = this.depthAttachment;
            } else if (this.hasStencil) {
                finalAttachments[i++] = genAttachment(false, STENCIL_FORMAT, 0, GL30.GL_STENCIL_ATTACHMENT);
            }
            return new FrameBufferPlus(width, height, finalAttachments);
        }

    }

    /** the frame buffers **/
    private final static Map<Application, Array<FrameBufferPlus>> buffers = new HashMap<Application, Array<FrameBufferPlus>>();

    /** the default framebuffer handle, a.k.a screen. */
    private static int defaultFramebufferHandle;
    /** true if we have polled for the default handle already. */
    private static boolean defaultFramebufferHandleInitialized = false;

    /** our framebuffer handle **/
    protected int framebufferHandle;

    /** width **/
    protected final int width;

    /** height **/
    protected final int height;

    /** attachments **/
    protected final Attachment[] attachments;

    /** Creates a new FrameBufferPlus having the given dimensions and attachments.
     *
     * @param width in pixels
     * @param height in pixels
     * @param attachments renderbuffers/textures to attach
     * */
    protected FrameBufferPlus(int width, int height, Attachment[] attachments) {
        this.width = width;
        this.height = height;
        this.attachments = attachments;
        build();
    }

    /**
     * FrameBuffer-like constructor.
     * @param width
     * @param height
     * @param hasDepth
     * @param hasStencil
     */
    public static FrameBufferPlus create(int format, int width, int height, boolean hasDepth, boolean hasStencil) {
        Builder builder = new FrameBufferPlus.Builder(width, height).addColorTexture(format);
        if (hasDepth && hasStencil) {
            builder.addDepthRenderBuffer(GL30.GL_DEPTH24_STENCIL8);
        } else if (hasDepth) {
            builder.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24);
        } else if (hasStencil) {
            builder.addStencilRenderbuffer();
        }
        return builder.build();
    }

    protected void build () {
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

        framebufferHandle = gl.glGenFramebuffer();

        // generate attachment handles (textures/renderbuffers)
        for (Attachment a: attachments) {

            // create the texture/renderbuffer
            if (a.isTexture) {
                a.texture = new Texture(new GLOnlyTextureData(width, height, 0, a.format, a.format, a.type));
                a.texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
                a.texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
            }
            a.handle = a.isTexture ? a.texture.getTextureObjectHandle() : gl.glGenRenderbuffer();
            if (!a.isTexture) {
                gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, a.handle);
                gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, a.format, width, height);
            }

        }

        // bind them to the framebuffer
        gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
        for (Attachment a: attachments) {

            // attach to this framebuffer
            if (a.isTexture) {
                gl.glBindTexture(GL20.GL_TEXTURE_2D, a.handle);
                gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, a.attachment, GL20.GL_TEXTURE_2D, a.handle, 0);
                if (a.isDepthStencil) {
                    // special case for depth+stencil; attachment is depth but need to also do stencil
                    gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL20.GL_TEXTURE_2D, a.handle, 0);
                }
                gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
            } else {
                gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, a.handle);
                gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, a.attachment, GL20.GL_RENDERBUFFER, a.handle);
                if (a.isDepthStencil) {
                    // special case for depth+stencil; attachment is depth but need to also do stencil
                    gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER, a.handle);
                }
                gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);
            }

        }

        // did it all work?
        int result = gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);
        gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultFramebufferHandle);

        if (result != GL20.GL_FRAMEBUFFER_COMPLETE) {

            dispose();

            if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: incomplete attachment");
            if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS)
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: incomplete dimensions");
            if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: missing attachment");
            if (result == GL20.GL_FRAMEBUFFER_UNSUPPORTED)
                throw new IllegalStateException("FrameBufferPlus couldn't be constructed: unsupported combination of formats");
            throw new IllegalStateException("FrameBufferPlus couldn't be constructed: unknown error " + result);
        }
    }

    /** Releases all resources associated with the FrameBufferPlus. */
    @Override
    public void dispose () {
        GL20 gl = Gdx.gl20;

        for (Attachment a: attachments) {
            if (a.isTexture) {
                a.texture.dispose();
                a.texture = null;
            } else {
                gl.glDeleteRenderbuffer(a.handle);
            }
            attachmentPool.free(a);
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
        Gdx.gl20.glViewport(0, 0, width, height);
    }

    /** Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on. */
    public void end () {
        end(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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


    /** It is suggested that you store this reference.
     */
    public Texture getColorTexture(int sought) {
        int i = 0;
        for (Attachment a: attachments) {
            if (a.attachment >= GL30.GL_COLOR_ATTACHMENT0 && a.attachment <= GL30.GL_COLOR_ATTACHMENT15) {
                if (i == sought) return a.texture;
                i++;
            }
        }
        throw new GdxRuntimeException("Invalid colour texture attachment: " + sought);
    }

    /** It is suggested that you store this reference.
     */
    public Texture getDepthTexture() {
        for (Attachment a: attachments) {
            if (a.attachment == GL30.GL_DEPTH_ATTACHMENT) {
                return a.texture;
            }
        }
        throw new GdxRuntimeException("No depth texture attachments");
    }

    /** @return the height of the framebuffer in pixels */
    public int getHeight () {
        return width;
    }

    /** @return the width of the framebuffer in pixels */
    public int getWidth () {
        return height;
    }

    private static void addManagedFrameBuffer (Application app, FrameBufferPlus frameBuffer) {
        Array<FrameBufferPlus> managedResources = buffers.get(app);
        if (managedResources == null) managedResources = new Array<FrameBufferPlus>();
        managedResources.add(frameBuffer);
        buffers.put(app, managedResources);
    }

    /** Invalidates all frame buffers. This can be used when the OpenGL context is lost to rebuild all managed frame buffers. This
     * assumes that the texture attached to this buffer has already been rebuild! Use with care. */
    public static void invalidateAllFrameBuffers (Application app) {
        if (Gdx.gl20 == null) return;

        Array<FrameBufferPlus> bufferArray = buffers.get(app);
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

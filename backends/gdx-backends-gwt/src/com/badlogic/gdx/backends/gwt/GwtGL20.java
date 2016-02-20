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

package com.badlogic.gdx.backends.gwt;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.HasArrayBufferView;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.typedarrays.client.Uint8ArrayNative;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int16Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.webgl.client.WebGLActiveInfo;
import com.google.gwt.webgl.client.WebGLBuffer;
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLProgram;
import com.google.gwt.webgl.client.WebGLRenderbuffer;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLShader;
import com.google.gwt.webgl.client.WebGLTexture;
import com.google.gwt.webgl.client.WebGLUniformLocation;

public class GwtGL20 implements GL20 {
	final Map<Integer, WebGLProgram> programs = new HashMap<Integer, WebGLProgram>();
	int nextProgramId = 1;
	final Map<Integer, WebGLShader> shaders = new HashMap<Integer, WebGLShader>();
	int nextShaderId = 1;
	final Map<Integer, WebGLBuffer> buffers = new HashMap<Integer, WebGLBuffer>();
	int nextBufferId = 1;
	final Map<Integer, WebGLFramebuffer> frameBuffers = new HashMap<Integer, WebGLFramebuffer>();
	int nextFrameBufferId = 1;
	final Map<Integer, WebGLRenderbuffer> renderBuffers = new HashMap<Integer, WebGLRenderbuffer>();
	int nextRenderBufferId = 1;
	final Map<Integer, WebGLTexture> textures = new HashMap<Integer, WebGLTexture>();
	int nextTextureId = 1;
	final Map<Integer, Map<Integer, WebGLUniformLocation>> uniforms = new HashMap<Integer, Map<Integer, WebGLUniformLocation>>();
	int nextUniformId = 1;
	int currProgram = 0;

	Float32Array floatBuffer = TypedArrays.createFloat32Array(2000 * 20);
	Int32Array intBuffer = TypedArrays.createInt32Array(2000 * 6);
	Int16Array shortBuffer = TypedArrays.createInt16Array(2000 * 6);
	float[] floatArray = new float[16000];

	final WebGLRenderingContext gl;

	protected GwtGL20 (WebGLRenderingContext gl) {
		;
		this.gl = gl;
		this.gl.pixelStorei(WebGLRenderingContext.UNPACK_PREMULTIPLY_ALPHA_WEBGL, 0);
	}

	private void ensureCapacity (FloatBuffer buffer) {
		if (buffer.remaining() > floatBuffer.length()) {
			floatBuffer = TypedArrays.createFloat32Array(buffer.remaining());
		}
	}

	private void ensureCapacity (ShortBuffer buffer) {
		if (buffer.remaining() > shortBuffer.length()) {
			shortBuffer = TypedArrays.createInt16Array(buffer.remaining());
		}
	}

	private void ensureCapacity (IntBuffer buffer) {
		if (buffer.remaining() > intBuffer.length()) {
			intBuffer = TypedArrays.createInt32Array(buffer.remaining());
		}
	}

	public Float32Array copy (FloatBuffer buffer) {
		if (GWT.isProdMode()) {
			return ((Float32Array)((HasArrayBufferView)buffer).getTypedArray()).subarray(buffer.position(), buffer.remaining());
		} else {
			ensureCapacity(buffer);
			for (int i = buffer.position(), j = 0; i < buffer.limit(); i++, j++) {
				floatBuffer.set(j, buffer.get(i));
			}
			return floatBuffer.subarray(0, buffer.remaining());
		}
	}

	public Int16Array copy (ShortBuffer buffer) {
		if (GWT.isProdMode()) {
			return ((Int16Array)((HasArrayBufferView)buffer).getTypedArray()).subarray(buffer.position(), buffer.remaining());
		} else {
			ensureCapacity(buffer);
			for (int i = buffer.position(), j = 0; i < buffer.limit(); i++, j++) {
				shortBuffer.set(j, buffer.get(i));
			}
			return shortBuffer.subarray(0, buffer.remaining());
		}
	}

	public Int32Array copy (IntBuffer buffer) {
		if (GWT.isProdMode()) {
			return ((Int32Array)((HasArrayBufferView)buffer).getTypedArray()).subarray(buffer.position(), buffer.remaining());
		} else {
			ensureCapacity(buffer);
			for (int i = buffer.position(), j = 0; i < buffer.limit(); i++, j++) {
				intBuffer.set(j, buffer.get(i));
			}
			return intBuffer.subarray(0, buffer.remaining());
		}
	}

	private int allocateUniformLocationId (int program, WebGLUniformLocation location) {
		Map<Integer, WebGLUniformLocation> progUniforms = uniforms.get(program);
		if (progUniforms == null) {
			progUniforms = new HashMap<Integer, WebGLUniformLocation>();
			uniforms.put(program, progUniforms);
		}
		// FIXME check if uniform already stored.
		int id = nextUniformId++;
		progUniforms.put(id, location);
		return id;
	}

	private WebGLUniformLocation getUniformLocation (int location) {
		return uniforms.get(currProgram).get(location);
	}

	private int allocateShaderId (WebGLShader shader) {
		int id = nextShaderId++;
		shaders.put(id, shader);
		return id;
	}

	private void deallocateShaderId (int id) {
		shaders.remove(id);
	}

	private int allocateProgramId (WebGLProgram program) {
		int id = nextProgramId++;
		programs.put(id, program);
		return id;
	}

	private void deallocateProgramId (int id) {
		uniforms.remove(id);
		programs.remove(id);
	}

	private int allocateBufferId (WebGLBuffer buffer) {
		int id = nextBufferId++;
		buffers.put(id, buffer);
		return id;
	}

	private void deallocateBufferId (int id) {
		buffers.remove(id);
	}

	private int allocateFrameBufferId (WebGLFramebuffer frameBuffer) {
		int id = nextBufferId++;
		frameBuffers.put(id, frameBuffer);
		return id;
	}

	private void deallocateFrameBufferId (int id) {
		frameBuffers.remove(id);
	}

	private int allocateRenderBufferId (WebGLRenderbuffer renderBuffer) {
		int id = nextRenderBufferId++;
		renderBuffers.put(id, renderBuffer);
		return id;
	}

	private void deallocateRenderBufferId (int id) {
		renderBuffers.remove(id);
	}

	private int allocateTextureId (WebGLTexture texture) {
		int id = nextTextureId++;
		textures.put(id, texture);
		return id;
	}

	private void deallocateTextureId (int id) {
		textures.remove(id);
	}

	@Override
	public void glActiveTexture (int texture) {
		gl.activeTexture(texture);
	}

	@Override
	public void glBindTexture (int target, int texture) {
		gl.bindTexture(target, textures.get(texture));
	}

	@Override
	public void glBlendFunc (int sfactor, int dfactor) {
		gl.blendFunc(sfactor, dfactor);
	}

	@Override
	public void glClear (int mask) {
		gl.clear(mask);
	}

	@Override
	public void glClearColor (float red, float green, float blue, float alpha) {
		gl.clearColor(red, green, blue, alpha);
	}

	@Override
	public void glClearDepthf (float depth) {
		gl.clearDepth(depth);
	}

	@Override
	public void glClearStencil (int s) {
		gl.clearStencil(s);
	}

	@Override
	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		gl.colorMask(red, green, blue, alpha);
	}

	@Override
	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		throw new GdxRuntimeException("compressed textures not supported by GWT WebGL backend");
	}

	@Override
	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		throw new GdxRuntimeException("compressed textures not supported by GWT WebGL backend");
	}

	@Override
	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		gl.copyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	@Override
	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		gl.copyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	@Override
	public void glCullFace (int mode) {
		gl.cullFace(mode);
	}

	@Override
	public void glDeleteTextures (int n, IntBuffer textures) {
		for (int i = 0; i < n; i++) {
			int id = textures.get();
			WebGLTexture texture = this.textures.get(id);
			deallocateTextureId(id);
			gl.deleteTexture(texture);
		}
	}
	
	@Override
	public void glDeleteTexture (int id) {
		WebGLTexture texture = this.textures.get(id);
		deallocateTextureId(id);
		gl.deleteTexture(texture);
	}

	@Override
	public void glDepthFunc (int func) {
		gl.depthFunc(func);
	}

	@Override
	public void glDepthMask (boolean flag) {
		gl.depthMask(flag);
	}

	@Override
	public void glDepthRangef (float zNear, float zFar) {
		gl.depthRange(zNear, zFar);
	}

	@Override
	public void glDisable (int cap) {
		gl.disable(cap);
	}

	@Override
	public void glDrawArrays (int mode, int first, int count) {
		gl.drawArrays(mode, first, count);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		gl.drawElements(mode, count, type, indices.position()); // FIXME this is assuming WebGL supports client side buffers...
	}

	@Override
	public void glEnable (int cap) {
		gl.enable(cap);
	}

	@Override
	public void glFinish () {
		gl.finish();
	}

	@Override
	public void glFlush () {
		gl.flush();
	}

	@Override
	public void glFrontFace (int mode) {
		gl.frontFace(mode);
	}

	@Override
	public void glGenTextures (int n, IntBuffer textures) {
		WebGLTexture texture = gl.createTexture();
		int id = allocateTextureId(texture);
		textures.put(id);
	}
	
	@Override
	public int glGenTexture () {
		WebGLTexture texture = gl.createTexture();
		return allocateTextureId(texture);
	}

	@Override
	public int glGetError () {
		return gl.getError();
	}

	@Override
	public void glGetIntegerv (int pname, IntBuffer params) {
		if (pname == GL20.GL_ACTIVE_TEXTURE || pname == GL20.GL_ALPHA_BITS || pname == GL20.GL_BLEND_DST_ALPHA
			|| pname == GL20.GL_BLEND_DST_RGB || pname == GL20.GL_BLEND_EQUATION_ALPHA || pname == GL20.GL_BLEND_EQUATION_RGB
			|| pname == GL20.GL_BLEND_SRC_ALPHA || pname == GL20.GL_BLEND_SRC_RGB || pname == GL20.GL_BLUE_BITS
			|| pname == GL20.GL_CULL_FACE_MODE || pname == GL20.GL_DEPTH_BITS || pname == GL20.GL_DEPTH_FUNC
			|| pname == GL20.GL_FRONT_FACE || pname == GL20.GL_GENERATE_MIPMAP_HINT || pname == GL20.GL_GREEN_BITS
			|| pname == GL20.GL_IMPLEMENTATION_COLOR_READ_FORMAT || pname == GL20.GL_IMPLEMENTATION_COLOR_READ_TYPE
			|| pname == GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS || pname == GL20.GL_MAX_CUBE_MAP_TEXTURE_SIZE
			|| pname == GL20.GL_MAX_FRAGMENT_UNIFORM_VECTORS || pname == GL20.GL_MAX_RENDERBUFFER_SIZE
			|| pname == GL20.GL_MAX_TEXTURE_IMAGE_UNITS || pname == GL20.GL_MAX_TEXTURE_SIZE || pname == GL20.GL_MAX_VARYING_VECTORS
			|| pname == GL20.GL_MAX_VERTEX_ATTRIBS || pname == GL20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS
			|| pname == GL20.GL_MAX_VERTEX_UNIFORM_VECTORS || pname == GL20.GL_NUM_COMPRESSED_TEXTURE_FORMATS
			|| pname == GL20.GL_PACK_ALIGNMENT || pname == GL20.GL_RED_BITS || pname == GL20.GL_SAMPLE_BUFFERS
			|| pname == GL20.GL_SAMPLES || pname == GL20.GL_STENCIL_BACK_FAIL || pname == GL20.GL_STENCIL_BACK_FUNC
			|| pname == GL20.GL_STENCIL_BACK_PASS_DEPTH_FAIL || pname == GL20.GL_STENCIL_BACK_PASS_DEPTH_PASS
			|| pname == GL20.GL_STENCIL_BACK_REF || pname == GL20.GL_STENCIL_BACK_VALUE_MASK
			|| pname == GL20.GL_STENCIL_BACK_WRITEMASK || pname == GL20.GL_STENCIL_BITS || pname == GL20.GL_STENCIL_CLEAR_VALUE
			|| pname == GL20.GL_STENCIL_FAIL || pname == GL20.GL_STENCIL_FUNC || pname == GL20.GL_STENCIL_PASS_DEPTH_FAIL
			|| pname == GL20.GL_STENCIL_PASS_DEPTH_PASS || pname == GL20.GL_STENCIL_REF || pname == GL20.GL_STENCIL_VALUE_MASK
			|| pname == GL20.GL_STENCIL_WRITEMASK || pname == GL20.GL_SUBPIXEL_BITS || pname == GL20.GL_UNPACK_ALIGNMENT)
			params.put(0, gl.getParameteri(pname));
		else
			throw new GdxRuntimeException("glGetFloat not supported by GWT WebGL backend");
	}

	@Override
	public String glGetString (int name) {
		return gl.getParameterString(name);
	}

	@Override
	public void glHint (int target, int mode) {
		gl.hint(target, mode);
	}

	@Override
	public void glLineWidth (float width) {
		gl.lineWidth(width);
	}

	@Override
	public void glPixelStorei (int pname, int param) {
		gl.pixelStorei(pname, param);
	}

	@Override
	public void glPolygonOffset (float factor, float units) {
		gl.polygonOffset(factor, units);
	}

	@Override
	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		// verify request
		if ((format != WebGLRenderingContext.RGBA) || (type != WebGLRenderingContext.UNSIGNED_BYTE)) {
			throw new GdxRuntimeException("Only format RGBA and type UNSIGNED_BYTE are currently supported for glReadPixels(...).");
		}
		if (!(pixels instanceof ByteBuffer)) {
			throw new GdxRuntimeException("Inputed pixels buffer needs to be of type ByteBuffer for glReadPixels(...).");
		}

		// create new ArrayBufferView (4 bytes per pixel)
		int size = 4 * width * height;
		Uint8Array buffer = Uint8ArrayNative.create(size);

		// read bytes to ArrayBufferView
		gl.readPixels(x, y, width, height, format, type, buffer);

		// copy ArrayBufferView to our pixels array
		ByteBuffer pixelsByte = (ByteBuffer)pixels;
		for (int i = 0; i < size; i++) {
			pixelsByte.put((byte)(buffer.get(i) & 0x000000ff));
		}
	}

	@Override
	public void glScissor (int x, int y, int width, int height) {
		gl.scissor(x, y, width, height);
	}

	@Override
	public void glStencilFunc (int func, int ref, int mask) {
		gl.stencilFunc(func, ref, mask);
	}

	@Override
	public void glStencilMask (int mask) {
		gl.stencilMask(mask);
	}

	@Override
	public void glStencilOp (int fail, int zfail, int zpass) {
		gl.stencilOp(fail, zfail, zpass);
	}

	@Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		if (pixels == null) {
			gl.texImage2D(target, level, internalformat, width, height, border, format, type, null);
		} else {
			if (pixels.limit() > 1) {
				HasArrayBufferView arrayHolder = (HasArrayBufferView)pixels;

				ArrayBufferView webGLArray = arrayHolder.getTypedArray();
				int remainingBytes = pixels.remaining() * 4;

				int byteOffset = webGLArray.byteOffset() + pixels.position() * 4;

				Uint8Array buffer = Uint8ArrayNative.create(webGLArray.buffer(), byteOffset, remainingBytes);

				gl.texImage2D(target, level, internalformat, width, height, border, format, type, buffer);
			} else {
				Pixmap pixmap = Pixmap.pixmaps.get(((IntBuffer)pixels).get(0));
				gl.texImage2D(target, level, internalformat, format, type, pixmap.getCanvasElement());
			}
		}
	}

	@Override
	public void glTexParameterf (int target, int pname, float param) {
		gl.texParameterf(target, pname, param);
	}

	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
        if (pixels.limit() > 1) {
            HasArrayBufferView arrayHolder = (HasArrayBufferView) pixels;

            ArrayBufferView webGLArray = arrayHolder.getTypedArray();
            int remainingBytes = pixels.remaining() * 4;

            int byteOffset = webGLArray.byteOffset() + pixels.position() * 4;

            Uint8Array buffer = Uint8ArrayNative.create(webGLArray.buffer(), byteOffset, remainingBytes);

            gl.texSubImage2D(target, level, xoffset, yoffset, width, height, format, type, buffer);
        } else {
            Pixmap pixmap = Pixmap.pixmaps.get(((IntBuffer) pixels).get(0));
            gl.texSubImage2D(target, level, xoffset, yoffset, format, type, pixmap.getCanvasElement());
        }
	}

	@Override
	public void glViewport (int x, int y, int width, int height) {
		gl.viewport(x, y, width, height);
	}

	@Override
	public void glAttachShader (int program, int shader) {
		WebGLProgram glProgram = programs.get(program);
		WebGLShader glShader = shaders.get(shader);
		gl.attachShader(glProgram, glShader);
	}

	@Override
	public void glBindAttribLocation (int program, int index, String name) {
		WebGLProgram glProgram = programs.get(program);
		gl.bindAttribLocation(glProgram, index, name);
	}

	@Override
	public void glBindBuffer (int target, int buffer) {
		gl.bindBuffer(target, buffers.get(buffer));
	}

	@Override
	public void glBindFramebuffer (int target, int framebuffer) {
		gl.bindFramebuffer(target, frameBuffers.get(framebuffer));
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		gl.bindRenderbuffer(target, renderBuffers.get(renderbuffer));
	}

	@Override
	public void glBlendColor (float red, float green, float blue, float alpha) {
		gl.blendColor(red, green, blue, alpha);
	}

	@Override
	public void glBlendEquation (int mode) {
		gl.blendEquation(mode);
	}

	@Override
	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		gl.blendEquationSeparate(modeRGB, modeAlpha);
	}

	@Override
	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		gl.blendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	@Override
	public void glBufferData (int target, int size, Buffer data, int usage) {
		if (data instanceof FloatBuffer) {
			gl.bufferData(target, copy((FloatBuffer)data), usage);
		} else if (data instanceof ShortBuffer) {
			gl.bufferData(target, copy((ShortBuffer)data), usage);
		} else {
			throw new GdxRuntimeException("Can only cope with FloatBuffer and ShortBuffer at the moment");
		}
	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		if (data instanceof FloatBuffer) {
			gl.bufferSubData(target, offset, copy((FloatBuffer)data));
		} else if (data instanceof ShortBuffer) {
			gl.bufferSubData(target, offset, copy((ShortBuffer)data));
		} else {
			throw new GdxRuntimeException("Can only cope with FloatBuffer and ShortBuffer at the moment");
		}
	}

	@Override
	public int glCheckFramebufferStatus (int target) {
		return gl.checkFramebufferStatus(target);
	}

	@Override
	public void glCompileShader (int shader) {
		WebGLShader glShader = shaders.get(shader);
		gl.compileShader(glShader);
	}

	@Override
	public int glCreateProgram () {
		WebGLProgram program = gl.createProgram();
		return allocateProgramId(program);
	}

	@Override
	public int glCreateShader (int type) {
		WebGLShader shader = gl.createShader(type);
		return allocateShaderId(shader);
	}

	@Override
	public void glDeleteBuffers (int n, IntBuffer buffers) {
		for (int i = 0; i < n; i++) {
			int id = buffers.get();
			WebGLBuffer buffer = this.buffers.get(id);
			deallocateBufferId(id);
			gl.deleteBuffer(buffer);
		}
	}
	
	@Override
	public void glDeleteBuffer (int id) {
		WebGLBuffer buffer = this.buffers.get(id);
		deallocateBufferId(id);
		gl.deleteBuffer(buffer);
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		for (int i = 0; i < n; i++) {
			int id = framebuffers.get();
			WebGLFramebuffer fb = this.frameBuffers.get(id);
			deallocateFrameBufferId(id);
			gl.deleteFramebuffer(fb);
		}
	}

	@Override
	public void glDeleteFramebuffer (int id) {
		WebGLFramebuffer fb = this.frameBuffers.get(id);
		deallocateFrameBufferId(id);
		gl.deleteFramebuffer(fb);
	}
	
	@Override
	public void glDeleteProgram (int program) {
		WebGLProgram prog = programs.get(program);
		deallocateProgramId(program);
		gl.deleteProgram(prog);
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		for (int i = 0; i < n; i++) {
			int id = renderbuffers.get();
			WebGLRenderbuffer rb = this.renderBuffers.get(id);
			deallocateRenderBufferId(id);
			gl.deleteRenderbuffer(rb);
		}
	}
	
	@Override
	public void glDeleteRenderbuffer (int id) {
		WebGLRenderbuffer rb = this.renderBuffers.get(id);
		deallocateRenderBufferId(id);
		gl.deleteRenderbuffer(rb);
	}

	@Override
	public void glDeleteShader (int shader) {
		WebGLShader sh = shaders.get(shader);
		deallocateShaderId(shader);
		gl.deleteShader(sh);
	}

	@Override
	public void glDetachShader (int program, int shader) {
		gl.detachShader(programs.get(program), shaders.get(shader));
	}

	@Override
	public void glDisableVertexAttribArray (int index) {
		gl.disableVertexAttribArray(index);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		gl.drawElements(mode, count, type, indices);
	}

	@Override
	public void glEnableVertexAttribArray (int index) {
		gl.enableVertexAttribArray(index);
	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		gl.framebufferRenderbuffer(target, attachment, renderbuffertarget, renderBuffers.get(renderbuffer));
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		gl.framebufferTexture2D(target, attachment, textarget, textures.get(texture), level);
	}

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		for (int i = 0; i < n; i++) {
			WebGLBuffer buffer = gl.createBuffer();
			int id = allocateBufferId(buffer);
			buffers.put(id);
		}
	}
	
	@Override
	public int glGenBuffer () {
		WebGLBuffer buffer = gl.createBuffer();
		return allocateBufferId(buffer);
	}

	@Override
	public void glGenerateMipmap (int target) {
		gl.generateMipmap(target);
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		for (int i = 0; i < n; i++) {
			WebGLFramebuffer fb = gl.createFramebuffer();
			int id = allocateFrameBufferId(fb);
			framebuffers.put(id);
		}
	}
	
	@Override
	public int glGenFramebuffer () {
		WebGLFramebuffer fb = gl.createFramebuffer();
		return allocateFrameBufferId(fb);
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		for (int i = 0; i < n; i++) {
			WebGLRenderbuffer rb = gl.createRenderbuffer();
			int id = allocateRenderBufferId(rb);
			renderbuffers.put(id);
		}
	}
	
	@Override
	public int glGenRenderbuffer () {
		WebGLRenderbuffer rb = gl.createRenderbuffer();
		return allocateRenderBufferId(rb);
	}

	@Override
	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		WebGLActiveInfo activeAttrib = gl.getActiveAttrib(programs.get(program), index);
		size.put(activeAttrib.getSize());
		((IntBuffer)type).put(activeAttrib.getType());
		return activeAttrib.getName();
	}

	@Override
	public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		WebGLActiveInfo activeUniform = gl.getActiveUniform(programs.get(program), index);
		size.put(activeUniform.getSize());
		((IntBuffer)type).put(activeUniform.getType());
		return activeUniform.getName();
	}

	@Override
	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public int glGetAttribLocation (int program, String name) {
		WebGLProgram prog = programs.get(program);
		return gl.getAttribLocation(prog, name);
	}

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		throw new GdxRuntimeException("glGetBoolean not supported by GWT WebGL backend");
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		if (pname == GL20.GL_DEPTH_CLEAR_VALUE || pname == GL20.GL_LINE_WIDTH || pname == GL20.GL_POLYGON_OFFSET_FACTOR
			|| pname == GL20.GL_POLYGON_OFFSET_UNITS || pname == GL20.GL_SAMPLE_COVERAGE_VALUE)
			params.put(0, gl.getParameterf(pname));
		else
			throw new GdxRuntimeException("glGetFloat not supported by GWT WebGL backend");
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		if (pname == GL20.GL_DELETE_STATUS || pname == GL20.GL_LINK_STATUS || pname == GL20.GL_VALIDATE_STATUS) {
			boolean result = gl.getProgramParameterb(programs.get(program), pname);
			params.put(result ? GL20.GL_TRUE : GL20.GL_FALSE);
		} else {
			params.put(gl.getProgramParameteri(programs.get(program), pname));
		}
	}

	@Override
	public String glGetProgramInfoLog (int program) {
		return gl.getProgramInfoLog(programs.get(program));
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		if (pname == GL20.GL_COMPILE_STATUS || pname == GL20.GL_DELETE_STATUS) {
			boolean result = gl.getShaderParameterb(shaders.get(shader), pname);
			params.put(result ? GL20.GL_TRUE : GL20.GL_FALSE);
		} else {
			int result = gl.getShaderParameteri(shaders.get(shader), pname);
			params.put(result);
		}
	}

	@Override
	public String glGetShaderInfoLog (int shader) {
		return gl.getShaderInfoLog(shaders.get(shader));
	}

	@Override
	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		throw new GdxRuntimeException("glGetShaderPrecisionFormat not supported by GWT WebGL backend");
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		throw new GdxRuntimeException("glGetTexParameter not supported by GWT WebGL backend");
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		throw new GdxRuntimeException("glGetTexParameter not supported by GWT WebGL backend");
	}

	@Override
	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetUniformiv (int program, int location, IntBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public int glGetUniformLocation (int program, String name) {
		WebGLUniformLocation location = gl.getUniformLocation(programs.get(program), name);
		return allocateUniformLocationId(program, location);
	}

	@Override
	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		throw new GdxRuntimeException("glGetVertexAttribPointer not supported by GWT WebGL backend");
	}

	@Override
	public boolean glIsBuffer (int buffer) {
		return gl.isBuffer(buffers.get(buffer));
	}

	@Override
	public boolean glIsEnabled (int cap) {
		return gl.isEnabled(cap);
	}

	@Override
	public boolean glIsFramebuffer (int framebuffer) {
		return gl.isFramebuffer(frameBuffers.get(framebuffer));
	}

	@Override
	public boolean glIsProgram (int program) {
		return gl.isProgram(programs.get(program));
	}

	@Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		return gl.isRenderbuffer(renderBuffers.get(renderbuffer));
	}

	@Override
	public boolean glIsShader (int shader) {
		return gl.isShader(shaders.get(shader));
	}

	@Override
	public boolean glIsTexture (int texture) {
		return gl.isTexture(textures.get(texture));
	}

	@Override
	public void glLinkProgram (int program) {
		gl.linkProgram(programs.get(program));
	}

	@Override
	public void glReleaseShaderCompiler () {
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		gl.renderbufferStorage(target, internalformat, width, height);
	}

	@Override
	public void glSampleCoverage (float value, boolean invert) {
		gl.sampleCoverage(value, invert);
	}

	@Override
	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		throw new GdxRuntimeException("glShaderBinary not supported by GWT WebGL backend");
	}

	@Override
	public void glShaderSource (int shader, String source) {
		gl.shaderSource(shaders.get(shader), source);
	}

	@Override
	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		gl.stencilFuncSeparate(face, func, ref, mask);
	}

	@Override
	public void glStencilMaskSeparate (int face, int mask) {
		gl.stencilMaskSeparate(face, mask);
	}

	@Override
	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		gl.stencilOpSeparate(face, fail, zfail, zpass);
	}

	@Override
	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		gl.texParameterf(target, pname, params.get());
	}

	@Override
	public void glTexParameteri (int target, int pname, int param) {
		gl.texParameterf(target, pname, param);
	}

	@Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		gl.texParameterf(target, pname, params.get());
	}

	@Override
	public void glUniform1f (int location, float x) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform1f(loc, x);
	}

	@Override
	public void glUniform1fv (int location, int count, FloatBuffer v) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform1fv(loc, copy(v));
	}
	
	@Override
	public void glUniform1fv (int location, int count, float[] v, int offset) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform1fv(loc, v);
	}

	@Override
	public void glUniform1i (int location, int x) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform1i(loc, x);
	}

	@Override
	public void glUniform1iv (int location, int count, IntBuffer v) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform1iv(loc, copy(v));
	}
	
	@Override
	public void glUniform1iv (int location, int count, int[] v, int offset) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform1iv(loc, v);
	}

	@Override
	public void glUniform2f (int location, float x, float y) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform2f(loc, x, y);
	}

	@Override
	public void glUniform2fv (int location, int count, FloatBuffer v) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform2fv(loc, copy(v));
	}
	
	@Override
	public void glUniform2fv (int location, int count, float[] v, int offset) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform2fv(loc, v);
	}

	@Override
	public void glUniform2i (int location, int x, int y) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform2i(loc, x, y);
	}

	@Override
	public void glUniform2iv (int location, int count, IntBuffer v) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform2iv(loc, copy(v));
	}
	
	@Override
	public void glUniform2iv (int location, int count, int[] v, int offset) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform2iv(loc, v);
	}

	@Override
	public void glUniform3f (int location, float x, float y, float z) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform3f(loc, x, y, z);
	}

	@Override
	public void glUniform3fv (int location, int count, FloatBuffer v) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform3fv(loc, copy(v));
	}
	
	@Override
	public void glUniform3fv (int location, int count, float[] v, int offset) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform3fv(loc, v);
	}

	@Override
	public void glUniform3i (int location, int x, int y, int z) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform3i(loc, x, y, z);
	}

	@Override
	public void glUniform3iv (int location, int count, IntBuffer v) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform3iv(loc, copy(v));
	}
	
	@Override
	public void glUniform3iv (int location, int count, int[] v, int offset) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform3iv(loc, v);
	}

	@Override
	public void glUniform4f (int location, float x, float y, float z, float w) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform4f(loc, x, y, z, w);
	}

	@Override
	public void glUniform4fv (int location, int count, FloatBuffer v) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform4fv(loc, copy(v));
	}

	@Override
	public void glUniform4fv (int location, int count, float[] v, int offset) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform4fv(loc, v);
	}
	
	@Override
	public void glUniform4i (int location, int x, int y, int z, int w) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform4i(loc, x, y, z, w);
	}

	@Override
	public void glUniform4iv (int location, int count, IntBuffer v) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform4iv(loc, copy(v));
	}

	@Override
	public void glUniform4iv (int location, int count, int[] v, int offset) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform4iv(loc, v);
	}
	
	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix2fv(loc, transpose, copy(value));
	}
	
	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, float[] value, int offset) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix2fv(loc, transpose, value);
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix3fv(loc, transpose, copy(value));
	}
	
	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, float[] value, int offset) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix3fv(loc, transpose, value);
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix4fv(loc, transpose, copy(value));
	}
	
	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, float[] value, int offset) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix4fv(loc, transpose, value);
	}

	@Override
	public void glUseProgram (int program) {
		currProgram = program;
		gl.useProgram(programs.get(program));
	}

	@Override
	public void glValidateProgram (int program) {
		gl.validateProgram(programs.get(program));
	}

	@Override
	public void glVertexAttrib1f (int indx, float x) {
		gl.vertexAttrib1f(indx, x);
	}

	@Override
	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		gl.vertexAttrib1fv(indx, copy(values));
	}

	@Override
	public void glVertexAttrib2f (int indx, float x, float y) {
		gl.vertexAttrib2f(indx, x, y);
	}

	@Override
	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		gl.vertexAttrib2fv(indx, copy(values));
	}

	@Override
	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		gl.vertexAttrib3f(indx, x, y, z);
	}

	@Override
	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		gl.vertexAttrib3fv(indx, copy(values));
	}

	@Override
	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		gl.vertexAttrib4f(indx, x, y, z, w);
	}

	@Override
	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		gl.vertexAttrib4fv(indx, copy(values));
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		throw new GdxRuntimeException("not implemented, vertex arrays aren't support in WebGL it seems");
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		gl.vertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}
}

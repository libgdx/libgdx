package com.badlogic.gdx.backends.gwt;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.webgl.client.WebGLRenderingContext;

public class GwtGL20Debug extends GwtGL20 {

	protected GwtGL20Debug (WebGLRenderingContext gl) {
		super(gl);
	}

	private void checkError() {
		int error = 0;
		if((error = gl.getError()) != GL_NO_ERROR) {
			throw new GdxRuntimeException("GL error: " + error + ", " + Integer.toHexString(error));
		}
	}
	
	@Override
	public void glActiveTexture (int texture) {
		super.glActiveTexture(texture);
		checkError();
	}

	@Override
	public void glBindTexture (int target, int texture) {
		// TODO Auto-generated method stub
		super.glBindTexture(target, texture);
		checkError();
	}

	@Override
	public void glBlendFunc (int sfactor, int dfactor) {
		// TODO Auto-generated method stub
		super.glBlendFunc(sfactor, dfactor);
		checkError();
	}

	@Override
	public void glClear (int mask) {
		// TODO Auto-generated method stub
		super.glClear(mask);
		checkError();
	}

	@Override
	public void glClearColor (float red, float green, float blue, float alpha) {
		// TODO Auto-generated method stub
		super.glClearColor(red, green, blue, alpha);
		checkError();
	}

	@Override
	public void glClearDepthf (float depth) {
		// TODO Auto-generated method stub
		super.glClearDepthf(depth);
		checkError();
	}

	@Override
	public void glClearStencil (int s) {
		// TODO Auto-generated method stub
		super.glClearStencil(s);
		checkError();
	}

	@Override
	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		// TODO Auto-generated method stub
		super.glColorMask(red, green, blue, alpha);
		checkError();
	}

	@Override
	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		// TODO Auto-generated method stub
		super.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
		checkError();
	}

	@Override
	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		// TODO Auto-generated method stub
		super.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
		checkError();
	}

	@Override
	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		// TODO Auto-generated method stub
		super.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
		checkError();
	}

	@Override
	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		super.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
		checkError();
	}

	@Override
	public void glCullFace (int mode) {
		// TODO Auto-generated method stub
		super.glCullFace(mode);
		checkError();
	}

	@Override
	public void glDeleteTextures (int n, IntBuffer textures) {
		// TODO Auto-generated method stub
		super.glDeleteTextures(n, textures);
		checkError();
	}

	@Override
	public void glDepthFunc (int func) {
		// TODO Auto-generated method stub
		super.glDepthFunc(func);
		checkError();
	}

	@Override
	public void glDepthMask (boolean flag) {
		// TODO Auto-generated method stub
		super.glDepthMask(flag);
		checkError();
	}

	@Override
	public void glDepthRangef (float zNear, float zFar) {
		// TODO Auto-generated method stub
		super.glDepthRangef(zNear, zFar);
		checkError();
	}

	@Override
	public void glDisable (int cap) {
		// TODO Auto-generated method stub
		super.glDisable(cap);
		checkError();
	}

	@Override
	public void glDrawArrays (int mode, int first, int count) {
		// TODO Auto-generated method stub
		super.glDrawArrays(mode, first, count);
		checkError();
	}

	@Override
	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		// TODO Auto-generated method stub
		super.glDrawElements(mode, count, type, indices);
		checkError();
	}

	@Override
	public void glEnable (int cap) {
		// TODO Auto-generated method stub
		super.glEnable(cap);
		checkError();
	}

	@Override
	public void glFinish () {
		// TODO Auto-generated method stub
		super.glFinish();
		checkError();
	}

	@Override
	public void glFlush () {
		// TODO Auto-generated method stub
		super.glFlush();
		checkError();
	}

	@Override
	public void glFrontFace (int mode) {
		// TODO Auto-generated method stub
		super.glFrontFace(mode);
		checkError();
	}

	@Override
	public void glGenTextures (int n, IntBuffer textures) {
		// TODO Auto-generated method stub
		super.glGenTextures(n, textures);
		checkError();
	}

	@Override
	public int glGetError () {
		// TODO Auto-generated method stub
		return super.glGetError();
	}

	@Override
	public void glGetIntegerv (int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		super.glGetIntegerv(pname, params);
		checkError();
	}

	@Override
	public String glGetString (int name) {
		// TODO Auto-generated method stub
		return super.glGetString(name);
	}

	@Override
	public void glHint (int target, int mode) {
		// TODO Auto-generated method stub
		super.glHint(target, mode);
		checkError();
	}

	@Override
	public void glLineWidth (float width) {
		// TODO Auto-generated method stub
		super.glLineWidth(width);
		checkError();
	}

	@Override
	public void glPixelStorei (int pname, int param) {
		// TODO Auto-generated method stub
		super.glPixelStorei(pname, param);
		checkError();
	}

	@Override
	public void glPolygonOffset (float factor, float units) {
		// TODO Auto-generated method stub
		super.glPolygonOffset(factor, units);
		checkError();
	}

	@Override
	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		// TODO Auto-generated method stub
		super.glReadPixels(x, y, width, height, format, type, pixels);
		checkError();
	}

	@Override
	public void glScissor (int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		super.glScissor(x, y, width, height);
		checkError();
	}

	@Override
	public void glStencilFunc (int func, int ref, int mask) {
		// TODO Auto-generated method stub
		super.glStencilFunc(func, ref, mask);
		checkError();
	}

	@Override
	public void glStencilMask (int mask) {
		// TODO Auto-generated method stub
		super.glStencilMask(mask);
		checkError();
	}

	@Override
	public void glStencilOp (int fail, int zfail, int zpass) {
		// TODO Auto-generated method stub
		super.glStencilOp(fail, zfail, zpass);
		checkError();
	}

	@Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		// TODO Auto-generated method stub
		super.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
		checkError();
	}

	@Override
	public void glTexParameterf (int target, int pname, float param) {
		// TODO Auto-generated method stub
		super.glTexParameterf(target, pname, param);
		checkError();
	}

	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		// TODO Auto-generated method stub
		super.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
		checkError();
	}

	@Override
	public void glViewport (int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		super.glViewport(x, y, width, height);
		checkError();
	}

	@Override
	public void glAttachShader (int program, int shader) {
		// TODO Auto-generated method stub
		super.glAttachShader(program, shader);
		checkError();
	}

	@Override
	public void glBindAttribLocation (int program, int index, String name) {
		// TODO Auto-generated method stub
		super.glBindAttribLocation(program, index, name);
		checkError();
	}

	@Override
	public void glBindBuffer (int target, int buffer) {
		// TODO Auto-generated method stub
		super.glBindBuffer(target, buffer);
		checkError();
	}

	@Override
	public void glBindFramebuffer (int target, int framebuffer) {
		// TODO Auto-generated method stub
		super.glBindFramebuffer(target, framebuffer);
		checkError();
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		// TODO Auto-generated method stub
		super.glBindRenderbuffer(target, renderbuffer);
		checkError();
	}

	@Override
	public void glBlendColor (float red, float green, float blue, float alpha) {
		// TODO Auto-generated method stub
		super.glBlendColor(red, green, blue, alpha);
		checkError();
	}

	@Override
	public void glBlendEquation (int mode) {
		// TODO Auto-generated method stub
		super.glBlendEquation(mode);
		checkError();
	}

	@Override
	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		// TODO Auto-generated method stub
		super.glBlendEquationSeparate(modeRGB, modeAlpha);
		checkError();
	}

	@Override
	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		// TODO Auto-generated method stub
		super.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
		checkError();
	}

	@Override
	public void glBufferData (int target, int size, Buffer data, int usage) {
		// TODO Auto-generated method stub
		super.glBufferData(target, size, data, usage);
		checkError();
	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		// TODO Auto-generated method stub
		super.glBufferSubData(target, offset, size, data);
		checkError();
	}

	@Override
	public int glCheckFramebufferStatus (int target) {
		// TODO Auto-generated method stub
		return super.glCheckFramebufferStatus(target);
	}

	@Override
	public void glCompileShader (int shader) {
		// TODO Auto-generated method stub
		super.glCompileShader(shader);
		checkError();
	}

	@Override
	public int glCreateProgram () {
		// TODO Auto-generated method stub
		int program = super.glCreateProgram();
		checkError();
		return program;
	}

	@Override
	public int glCreateShader (int type) {
		// TODO Auto-generated method stub
		int shader = super.glCreateShader(type);
		checkError();
		return shader;
	}

	@Override
	public void glDeleteBuffers (int n, IntBuffer buffers) {
		// TODO Auto-generated method stub
		super.glDeleteBuffers(n, buffers);
		checkError();
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		// TODO Auto-generated method stub
		super.glDeleteFramebuffers(n, framebuffers);
		checkError();
	}

	@Override
	public void glDeleteProgram (int program) {
		// TODO Auto-generated method stub
		super.glDeleteProgram(program);
		checkError();
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		// TODO Auto-generated method stub
		super.glDeleteRenderbuffers(n, renderbuffers);
		checkError();
	}

	@Override
	public void glDeleteShader (int shader) {
		// TODO Auto-generated method stub
		super.glDeleteShader(shader);
		checkError();
	}

	@Override
	public void glDetachShader (int program, int shader) {
		// TODO Auto-generated method stub
		super.glDetachShader(program, shader);
		checkError();
	}

	@Override
	public void glDisableVertexAttribArray (int index) {
		// TODO Auto-generated method stub
		super.glDisableVertexAttribArray(index);
		checkError();
	}

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		// TODO Auto-generated method stub
		super.glDrawElements(mode, count, type, indices);
		checkError();
	}

	@Override
	public void glEnableVertexAttribArray (int index) {
		// TODO Auto-generated method stub
		super.glEnableVertexAttribArray(index);
		checkError();
	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		// TODO Auto-generated method stub
		super.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
		checkError();
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		// TODO Auto-generated method stub
		super.glFramebufferTexture2D(target, attachment, textarget, texture, level);
		checkError();
	}

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		// TODO Auto-generated method stub
		super.glGenBuffers(n, buffers);
		checkError();
	}

	@Override
	public void glGenerateMipmap (int target) {
		// TODO Auto-generated method stub
		super.glGenerateMipmap(target);
		checkError();
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		// TODO Auto-generated method stub
		super.glGenFramebuffers(n, framebuffers);
		checkError();
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		// TODO Auto-generated method stub
		super.glGenRenderbuffers(n, renderbuffers);
		checkError();
	}

	@Override
	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		// TODO Auto-generated method stub
		String attrib = super.glGetActiveAttrib(program, index, size, type);
		checkError();
		return attrib;
	}

	@Override
	public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		// TODO Auto-generated method stub
		String uniform = super.glGetActiveUniform(program, index, size, type);
		checkError();
		return uniform;
	}

	@Override
	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		// TODO Auto-generated method stub
		super.glGetAttachedShaders(program, maxcount, count, shaders);
		checkError();
	}

	@Override
	public int glGetAttribLocation (int program, String name) {
		// TODO Auto-generated method stub
		int loc = super.glGetAttribLocation(program, name);
		checkError();
		return loc;
	}

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		// TODO Auto-generated method stub
		super.glGetBooleanv(pname, params);
		checkError();
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		super.glGetBufferParameteriv(target, pname, params);
		checkError();
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		super.glGetFloatv(pname, params);
		checkError();
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		super.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
		checkError();
	}

	@Override
	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		super.glGetProgramiv(program, pname, params);
		checkError();
	}

	@Override
	public String glGetProgramInfoLog (int program) {
		// TODO Auto-generated method stub
		String info = super.glGetProgramInfoLog(program);
		checkError();
		return info;
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		super.glGetRenderbufferParameteriv(target, pname, params);
		checkError();
	}

	@Override
	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		super.glGetShaderiv(shader, pname, params);
		checkError();
	}

	@Override
	public String glGetShaderInfoLog (int shader) {
		// TODO Auto-generated method stub
		String info = super.glGetShaderInfoLog(shader);
		checkError();
		return info;
	}

	@Override
	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		// TODO Auto-generated method stub
		super.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
		checkError();
	}

	@Override
	public void glGetShaderSource (int shader, int bufsize, Buffer length, String source) {
		// TODO Auto-generated method stub
		super.glGetShaderSource(shader, bufsize, length, source);
		checkError();
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		super.glGetTexParameterfv(target, pname, params);
		checkError();
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		super.glGetTexParameteriv(target, pname, params);
		checkError();
	}

	@Override
	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		// TODO Auto-generated method stub
		super.glGetUniformfv(program, location, params);
		checkError();
	}

	@Override
	public void glGetUniformiv (int program, int location, IntBuffer params) {
		// TODO Auto-generated method stub
		super.glGetUniformiv(program, location, params);
		checkError();
	}

	@Override
	public int glGetUniformLocation (int program, String name) {
		// TODO Auto-generated method stub
		int loc = super.glGetUniformLocation(program, name);
		checkError();
		return loc;
	}

	@Override
	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		super.glGetVertexAttribfv(index, pname, params);
		checkError();
	}

	@Override
	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		super.glGetVertexAttribiv(index, pname, params);
		checkError();
	}

	@Override
	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		// TODO Auto-generated method stub
		super.glGetVertexAttribPointerv(index, pname, pointer);
		checkError();
	}

	@Override
	public boolean glIsBuffer (int buffer) {
		// TODO Auto-generated method stub
		boolean res = super.glIsBuffer(buffer);
		checkError();
		return res;
	}

	@Override
	public boolean glIsEnabled (int cap) {
		// TODO Auto-generated method stub
		boolean res = super.glIsEnabled(cap);
		checkError();
		return res;
	}

	@Override
	public boolean glIsFramebuffer (int framebuffer) {
		// TODO Auto-generated method stub
		boolean res = super.glIsFramebuffer(framebuffer);
		checkError();
		return res;
	}

	@Override
	public boolean glIsProgram (int program) {
		// TODO Auto-generated method stub
		boolean res = super.glIsProgram(program);
		checkError();
		return res;
	}

	@Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		// TODO Auto-generated method stub
		boolean res = super.glIsRenderbuffer(renderbuffer);
		checkError();
		return res;
	}

	@Override
	public boolean glIsShader (int shader) {
		// TODO Auto-generated method stub
		boolean res = super.glIsShader(shader);
		checkError();
		return res;
	}

	@Override
	public boolean glIsTexture (int texture) {
		// TODO Auto-generated method stub
		boolean res = super.glIsTexture(texture);
		checkError();
		return res;
	}

	@Override
	public void glLinkProgram (int program) {
		// TODO Auto-generated method stub
		super.glLinkProgram(program);
		checkError();
	}

	@Override
	public void glReleaseShaderCompiler () {
		// TODO Auto-generated method stub
		super.glReleaseShaderCompiler();
		checkError();
	}

	@Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		// TODO Auto-generated method stub
		super.glRenderbufferStorage(target, internalformat, width, height);
		checkError();
	}

	@Override
	public void glSampleCoverage (float value, boolean invert) {
		// TODO Auto-generated method stub
		super.glSampleCoverage(value, invert);
		checkError();
	}

	@Override
	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		// TODO Auto-generated method stub
		super.glShaderBinary(n, shaders, binaryformat, binary, length);
		checkError();
	}

	@Override
	public void glShaderSource (int shader, String source) {
		// TODO Auto-generated method stub
		super.glShaderSource(shader, source);
		checkError();
	}

	@Override
	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		// TODO Auto-generated method stub
		super.glStencilFuncSeparate(face, func, ref, mask);
		checkError();
	}

	@Override
	public void glStencilMaskSeparate (int face, int mask) {
		// TODO Auto-generated method stub
		super.glStencilMaskSeparate(face, mask);
		checkError();
	}

	@Override
	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		// TODO Auto-generated method stub
		super.glStencilOpSeparate(face, fail, zfail, zpass);
		checkError();
	}

	@Override
	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		super.glTexParameterfv(target, pname, params);
		checkError();
	}

	@Override
	public void glTexParameteri (int target, int pname, int param) {
		// TODO Auto-generated method stub
		super.glTexParameteri(target, pname, param);
		checkError();
	}

	@Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		super.glTexParameteriv(target, pname, params);
		checkError();
	}

	@Override
	public void glUniform1f (int location, float x) {
		// TODO Auto-generated method stub
		super.glUniform1f(location, x);
		checkError();
	}

	@Override
	public void glUniform1fv (int location, int count, FloatBuffer v) {
		// TODO Auto-generated method stub
		super.glUniform1fv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform1i (int location, int x) {
		// TODO Auto-generated method stub
		super.glUniform1i(location, x);
		checkError();
	}

	@Override
	public void glUniform1iv (int location, int count, IntBuffer v) {
		// TODO Auto-generated method stub
		super.glUniform1iv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform2f (int location, float x, float y) {
		// TODO Auto-generated method stub
		super.glUniform2f(location, x, y);
		checkError();
	}

	@Override
	public void glUniform2fv (int location, int count, FloatBuffer v) {
		// TODO Auto-generated method stub
		super.glUniform2fv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform2i (int location, int x, int y) {
		// TODO Auto-generated method stub
		super.glUniform2i(location, x, y);
		checkError();
	}

	@Override
	public void glUniform2iv (int location, int count, IntBuffer v) {
		// TODO Auto-generated method stub
		super.glUniform2iv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform3f (int location, float x, float y, float z) {
		// TODO Auto-generated method stub
		super.glUniform3f(location, x, y, z);
		checkError();
	}

	@Override
	public void glUniform3fv (int location, int count, FloatBuffer v) {
		// TODO Auto-generated method stub
		super.glUniform3fv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform3i (int location, int x, int y, int z) {
		// TODO Auto-generated method stub
		super.glUniform3i(location, x, y, z);
		checkError();
	}

	@Override
	public void glUniform3iv (int location, int count, IntBuffer v) {
		// TODO Auto-generated method stub
		super.glUniform3iv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform4f (int location, float x, float y, float z, float w) {
		// TODO Auto-generated method stub
		super.glUniform4f(location, x, y, z, w);
		checkError();
	}

	@Override
	public void glUniform4fv (int location, int count, FloatBuffer v) {
		// TODO Auto-generated method stub
		super.glUniform4fv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform4i (int location, int x, int y, int z, int w) {
		// TODO Auto-generated method stub
		super.glUniform4i(location, x, y, z, w);
		checkError();
	}

	@Override
	public void glUniform4iv (int location, int count, IntBuffer v) {
		// TODO Auto-generated method stub
		super.glUniform4iv(location, count, v);
		checkError();
	}

	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub
		super.glUniformMatrix2fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub
		super.glUniformMatrix3fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub
		super.glUniformMatrix4fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public void glUseProgram (int program) {
		// TODO Auto-generated method stub
		super.glUseProgram(program);
		checkError();
	}

	@Override
	public void glValidateProgram (int program) {
		// TODO Auto-generated method stub
		super.glValidateProgram(program);
		checkError();
	}

	@Override
	public void glVertexAttrib1f (int indx, float x) {
		// TODO Auto-generated method stub
		super.glVertexAttrib1f(indx, x);
		checkError();
	}

	@Override
	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		// TODO Auto-generated method stub
		super.glVertexAttrib1fv(indx, values);
		checkError();
	}

	@Override
	public void glVertexAttrib2f (int indx, float x, float y) {
		// TODO Auto-generated method stub
		super.glVertexAttrib2f(indx, x, y);
		checkError();
	}

	@Override
	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		// TODO Auto-generated method stub
		super.glVertexAttrib2fv(indx, values);
		checkError();
	}

	@Override
	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		// TODO Auto-generated method stub
		super.glVertexAttrib3f(indx, x, y, z);
		checkError();
	}

	@Override
	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		// TODO Auto-generated method stub
		super.glVertexAttrib3fv(indx, values);
		checkError();
	}

	@Override
	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		// TODO Auto-generated method stub
		super.glVertexAttrib4f(indx, x, y, z, w);
		checkError();
	}

	@Override
	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		// TODO Auto-generated method stub
		super.glVertexAttrib4fv(indx, values);
		checkError();
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		// TODO Auto-generated method stub
		super.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
		checkError();
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		// TODO Auto-generated method stub
		super.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
		checkError();
	}
}

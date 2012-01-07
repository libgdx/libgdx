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

package com.badlogic.gdx.graphics;

import java.nio.Buffer;
import java.nio.IntBuffer;

/** This interface defines methods common to GL10, GL11 and GL20.
 * @author mzechner */
public interface GLCommon {
	public static final int GL_GENERATE_MIPMAP = 0x8191;
	public static final int GL_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FE;
	public static final int GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FF;

	public void glActiveTexture (int texture);

	public void glBindTexture (int target, int texture);

	public void glBlendFunc (int sfactor, int dfactor);

	public void glClear (int mask);

	public void glClearColor (float red, float green, float blue, float alpha);

	public void glClearDepthf (float depth);

	public void glClearStencil (int s);

	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha);

	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data);

	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data);

	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border);

	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height);

	public void glCullFace (int mode);

	public void glDeleteTextures (int n, IntBuffer textures);

	public void glDepthFunc (int func);

	public void glDepthMask (boolean flag);

	public void glDepthRangef (float zNear, float zFar);

	public void glDisable (int cap);

	public void glDrawArrays (int mode, int first, int count);

	public void glDrawElements (int mode, int count, int type, Buffer indices);

	public void glEnable (int cap);

	public void glFinish ();

	public void glFlush ();

	public void glFrontFace (int mode);

	public void glGenTextures (int n, IntBuffer textures);

	public int glGetError ();

	public void glGetIntegerv (int pname, IntBuffer params);

	public String glGetString (int name);

	public void glHint (int target, int mode);

	public void glLineWidth (float width);

	public void glPixelStorei (int pname, int param);

	public void glPolygonOffset (float factor, float units);

	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels);

	public void glScissor (int x, int y, int width, int height);

	public void glStencilFunc (int func, int ref, int mask);

	public void glStencilMask (int mask);

	public void glStencilOp (int fail, int zfail, int zpass);

	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels);

	public void glTexParameterf (int target, int pname, float param);

	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels);

	public void glViewport (int x, int y, int width, int height);
}

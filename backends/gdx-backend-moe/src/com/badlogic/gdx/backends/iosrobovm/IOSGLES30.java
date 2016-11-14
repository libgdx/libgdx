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

package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.graphics.GL30;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class IOSGLES30 extends IOSGLES20 implements GL30 {

    public IOSGLES30() {
        init();
    }

    private static native void init( );

    public native void glReadBuffer(int mode);

    public native void glDrawRangeElements(int mode, int start, int end, int count, int type, Buffer indices);

    public native void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset);

    public native void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels);

    public native void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, int offset);

    public native void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels);

    public native void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, int offset);

    public native void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height);

    public native void glGenQueries(int n, int[] ids, int offset);

    public native void glGenQueries(int n, IntBuffer ids);

    public native void glDeleteQueries(int n, int[] ids, int offset);

    public native void glDeleteQueries(int n, IntBuffer ids);

    public native boolean glIsQuery(int id);

    public native void glBeginQuery(int target, int id);

    public native void glEndQuery(int target);

    public native void glGetQueryiv(int target, int pname, IntBuffer params);

    public native void glGetQueryObjectuiv(int id, int pname, IntBuffer params);

    public native boolean glUnmapBuffer(int target);

    public native Buffer glGetBufferPointerv(int target, int pname);

    public native void glDrawBuffers(int n, IntBuffer bufs);

    public native void glUniformMatrix2x3fv(int location, int count, boolean transpose, FloatBuffer value);

    public native void glUniformMatrix3x2fv(int location, int count, boolean transpose, FloatBuffer value);

    public native void glUniformMatrix2x4fv(int location, int count, boolean transpose, FloatBuffer value);

    public native void glUniformMatrix4x2fv(int location, int count, boolean transpose, FloatBuffer value);

    public native void glUniformMatrix3x4fv(int location, int count, boolean transpose, FloatBuffer value);

    public native void glUniformMatrix4x3fv(int location, int count, boolean transpose, FloatBuffer value);

    public native void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter);

    public native void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height);

    public native void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer);

    public native void glFlushMappedBufferRange(int target, int offset, int length);

    public native void glBindVertexArray(int array);

    public native void glDeleteVertexArrays(int n, int[] arrays, int offset);

    public native void glDeleteVertexArrays(int n, IntBuffer arrays);

    public native void glGenVertexArrays(int n, int[] arrays, int offset);

    public native void glGenVertexArrays(int n, IntBuffer arrays);

    public native boolean glIsVertexArray(int array);

    public native void glBeginTransformFeedback(int primitiveMode);

    public native void glEndTransformFeedback();

    public native void glBindBufferRange(int target, int index, int buffer, int offset, int size);

    public native void glBindBufferBase(int target, int index, int buffer);

    public native void glTransformFeedbackVaryings(int program, String[] varyings, int bufferMode);

    public native void glVertexAttribIPointer(int index, int size, int type, int stride, int offset);

    public native void glGetVertexAttribIiv(int index, int pname, IntBuffer params);

    public native void glGetVertexAttribIuiv(int index, int pname, IntBuffer params);

    public native void glVertexAttribI4i(int index, int x, int y, int z, int w);

    public native void glVertexAttribI4ui(int index, int x, int y, int z, int w);

    public native void glGetUniformuiv(int program, int location, IntBuffer params);

    public native int glGetFragDataLocation(int program, String name);

    public native void glUniform1uiv(int location, int count, IntBuffer value);

    public native void glUniform3uiv(int location, int count, IntBuffer value);

    public native void glUniform4uiv(int location, int count, IntBuffer value);

    public native void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value);

    public native void glClearBufferuiv(int buffer, int drawbuffer, IntBuffer value);

    public native void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value);

    public native void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil);

    public native String glGetStringi(int name, int index);

    public native void glCopyBufferSubData(int readTarget, int writeTarget, int readOffset, int writeOffset, int size);

    public native void glGetUniformIndices(int program, String[] uniformNames, IntBuffer uniformIndices);

    public native void glGetActiveUniformsiv(int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params);

    public native int glGetUniformBlockIndex(int program, String uniformBlockName);

    public native void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params);

    public native void glGetActiveUniformBlockName(int program, int uniformBlockIndex, Buffer length, Buffer uniformBlockName);

    public native String glGetActiveUniformBlockName(int program, int uniformBlockIndex);

    public native void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding);

    public native void glDrawArraysInstanced(int mode, int first, int count, int instanceCount);

    public native void glDrawElementsInstanced(int mode, int count, int type, int indicesOffset, int instanceCount);

    public native void glGetInteger64v(int pname, LongBuffer params);

    public native void glGetBufferParameteri64v(int target, int pname, LongBuffer params);

    public native void glGenSamplers(int count, int[] samplers, int offset);

    public native void glGenSamplers(int count, IntBuffer samplers);

    public native void glDeleteSamplers(int count, int[] samplers, int offset);

    public native void glDeleteSamplers(int count, IntBuffer samplers);

    public native boolean glIsSampler(int sampler);

    public native void glBindSampler(int unit, int sampler);

    public native void glSamplerParameteri(int sampler, int pname, int param);

    public native void glSamplerParameteriv(int sampler, int pname, IntBuffer param);

    public native void glSamplerParameterf(int sampler, int pname, float param);

    public native void glSamplerParameterfv(int sampler, int pname, FloatBuffer param);

    public native void glGetSamplerParameteriv(int sampler, int pname, IntBuffer params);

    public native void glGetSamplerParameterfv(int sampler, int pname, FloatBuffer params);

    public native void glVertexAttribDivisor(int index, int divisor);

    public native void glBindTransformFeedback(int target, int id);

    public native void glDeleteTransformFeedbacks(int n, int[] ids, int offset);

    public native void glDeleteTransformFeedbacks(int n, IntBuffer ids);

    public native void glGenTransformFeedbacks(int n, int[] ids, int offset);

    public native void glGenTransformFeedbacks(int n, IntBuffer ids);

    public native boolean glIsTransformFeedback(int id);

    public native void glPauseTransformFeedback();

    public native void glResumeTransformFeedback();

    public native void glProgramParameteri(int program, int pname, int value);

    public native void glInvalidateFramebuffer(int target, int numAttachments, IntBuffer attachments);

    public native void glInvalidateSubFramebuffer(int target, int numAttachments, IntBuffer attachments, int x, int y, int width, int height);
}

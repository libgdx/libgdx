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

package com.badlogic.gdx.tests.gles3;

import java.nio.Buffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;

/** A basic implementation of a shader program with some OpenGL ES 3.0 function support.
 * <p>
 * List of functionality:
 * <li>Arbitrary attributes
 * <li>Uniform blocks (through {@link UniformBufferObject})
 * 
 * @author mattijs driel */
public class ShaderProgramES3 implements Disposable {

	int program;

	public final String vertexShaderSource;
	public final String fragmentShaderSource;

	static final IntBuffer ints = BufferUtils.newIntBuffer(512);

	/** Log containing any gl error encountered during construction. */
	private String log = null;

	private ObjectMap<String, UniformBlockInfo> uniformBlocks = new ObjectMap<String, UniformBlockInfo>();
	private ObjectMap<String, SamplerInfo> samplers = new ObjectMap<String, ShaderProgramES3.SamplerInfo>();

	/** Create a shader with a vertex and fragment shader. **/
	public ShaderProgramES3 (String vertexSource, String fragmentSource) {
		GL30 gl = Gdx.graphics.getGL30();
		this.vertexShaderSource = vertexSource;
		this.fragmentShaderSource = fragmentSource;

		if (gl == null)
			log = "This program requires ES 3.0";
		else if (vertexSource == null)
			log = "Vertex shader source is null";
		else if (fragmentSource == null)
			log = "Fragment shader source is null";
		else
			initProgram(gl);

		if (!isCompiled()) dispose();
	}

	private void initProgram (GL30 gl) {

		program = gl.glCreateProgram();
		if (program == -1) {
			log = "Could not create a program name";
			return;
		}

		// compile individual shader sources
		int vertexShader, fragmentShader;
		if ((vertexShader = initShader(gl, vertexShaderSource, GL20.GL_VERTEX_SHADER)) == 0) return;
		if ((fragmentShader = initShader(gl, fragmentShaderSource, GL20.GL_FRAGMENT_SHADER)) == 0) return;

		gl.glLinkProgram(program);
		ints.position(0);
		gl.glGetProgramiv(program, GL20.GL_LINK_STATUS, ints);
		if (ints.get(0) == 0) {
			log = gl.glGetProgramInfoLog(program);
			return;
		}

		// remove shaders, program will remain
		gl.glDetachShader(program, vertexShader);
		gl.glDetachShader(program, fragmentShader);
		gl.glDeleteShader(vertexShader);
		gl.glDeleteShader(fragmentShader);
	}

	private int initShader (GL30 gl, String source, int type) {
		int id = gl.glCreateShader(type);
		gl.glShaderSource(id, source);
		gl.glCompileShader(id);
		ints.position(0);
		gl.glGetShaderiv(id, GL20.GL_COMPILE_STATUS, ints);
		if (ints.get(0) == 0) {
			log = gl.glGetShaderInfoLog(id);
			gl.glDeleteShader(id);
			return 0;
		}
		gl.glAttachShader(program, id);
		return id;
	}

	public UniformBlockInfo registerUniformBlock (String name) {
		UniformBlockInfo info = uniformBlocks.get(name);
		if (info != null)
			throw new GdxRuntimeException("Uniform block \""+name+"\" already registered.");
		
		int index = Gdx.gl30.glGetUniformBlockIndex(program, name);
		if (index == GL30.GL_INVALID_INDEX)
			throw new GdxRuntimeException("Uniform block \""+name+"\" not found. Make sure it is in use in the program. ");
		
		info = new UniformBlockInfo(this, index);
		uniformBlocks.put(name, info);
		return info;
	}

	public SamplerInfo registerTextureSampler (String name) {
		SamplerInfo info = samplers.get(name);
		if (info != null)
			throw new GdxRuntimeException("Sampler \""+name+"\" already registered.");
		
		int index = Gdx.gl30.glGetUniformLocation(program, name);
		if (index == GL30.GL_INVALID_INDEX)
			throw new GdxRuntimeException("Sampler uniform \""+name+"\" not found. Make sure it is in use in the program. ");
		
		info = new SamplerInfo(this, index);
		samplers.put(name, info);
		return info;
	}
	
	public boolean isCompiled () {
		return log == null;
	}

	public String getErrorLog () {
		return log;
	}

	public int getProgramName () {
		return program;
	}

	public void use () {
		GL30 gl = Gdx.graphics.getGL30();
		if (gl == null) return;

		gl.glUseProgram(program);
		
		for (SamplerInfo info : samplers.values())
			gl.glUniform1i(info.uniformIndex, info.unit);
	}

	public UniformBlockInfo getUniformBlock (String blockName) {
		return uniformBlocks.get(blockName);
	}

	@Override
	public void dispose () {
		GL30 gl = Gdx.graphics.getGL30();
		if (gl == null) return;

		gl.glDeleteProgram(program);
	}

	public static final class SamplerInfo{
		public final ShaderProgramES3 owner;
		public final int uniformIndex;
		int unit;
		
		SamplerInfo(ShaderProgramES3 owner, int uniformIndex)
		{
			this.owner = owner;
			this.uniformIndex = uniformIndex;
		}
		
		public int getBinding(){
			return unit;
		}
		
		public void setBinding(int textureUnit) {
			this.unit = textureUnit;
		}
	}
	
	public static class UniformBlockInfo {
		public final ShaderProgramES3 owner;
		public final int blockIndex;

		int currentBindingPoint;

		UniformBlockInfo (ShaderProgramES3 owner, int index) {
			this.owner = owner;
			this.blockIndex = index;
		}

		public int getBinding () {
			return currentBindingPoint;
		}

		public void setBinding (int bindingPoint) {
			currentBindingPoint = bindingPoint;
			Gdx.gl30.glUniformBlockBinding(owner.program, blockIndex, currentBindingPoint);
		}
		
		public int getByteSize(){
			Gdx.gl30.glGetActiveUniformBlockiv(owner.program, blockIndex, GL30.GL_UNIFORM_BLOCK_DATA_SIZE, ints);
			return ints.get(0);
		}
	}

}

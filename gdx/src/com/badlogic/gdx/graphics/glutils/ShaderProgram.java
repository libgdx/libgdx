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

package com.badlogic.gdx.graphics.glutils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

/**
 * <p>
 * A shader program encapsulates a vertex and fragment shader pair linked to form a shader program useable with OpenGL ES 2.0.
 * </p>
 * 
 * <p>
 * After construction a ShaderProgram can be used to draw {@link Mesh} or sprites via a {@link SpriteBatch}. To make the GPU use
 * a specific ShaderProgram the programs {@link ShaderProgram#begin()} method must be used which effectively binds the program.
 * </p>
 * 
 * <p>
 * When a ShaderProgram is bound one can set uniforms, vertex attributes and attributes as needed via the respective methods.
 * </p>
 * 
 * <p>
 * A ShaderProgram can be unbound with a call to {@link ShaderProgram#end()}
 * </p>
 * 
 * <p>
 * A ShaderProgram must be disposed via a call to {@link ShaderProgram#dispose()} when it is no longer needed
 * </p>
 * 
 * <p>
 * ShaderPrograms are managed. In case the OpenGL context is lost all shaders get invalidated and have to be reloaded. This
 * happens on Android when a user switches to another application or receives an incoming call. Managed ShaderPrograms are
 * automatically reloaded when the OpenGL context is recreated so you don't have to do this manually.
 * </p>
 * 
 * @author mzechner
 * 
 */
public class ShaderProgram {
	/** the list of currently available shaders **/
	private final static ArrayList<ShaderProgram> shaders = new ArrayList<ShaderProgram>();

	/** the log **/
	private String log = "";

	/** whether this program compiled succesfully **/
	private boolean isCompiled;

	/** uniform lookup **/
	private final HashMap<String, Integer> uniforms = new HashMap<String, Integer>();

	/** attribute lookup **/
	private final HashMap<String, Integer> attributes = new HashMap<String, Integer>();

	/** program handle **/
	private int program;

	/** vertex shader handle **/
	private int vertexShaderHandle;

	/** fragment shader handle **/
	private int fragmentShaderHandle;

	/** matrix float buffer **/
	private final FloatBuffer matrix;

	/** vertex shader source **/
	private final String vertexShaderSource;

	/** fragment shader source **/
	private final String fragmentShaderSource;

	/** whether this shader was invalidated **/
	private boolean invalidated;

	/**
	 * Construcs a new JOglShaderProgram and immediatly compiles it.
	 * 
	 * @param vertexShader the vertex shader
	 * @param fragmentShader the fragment shader
	 */

	public ShaderProgram (String vertexShader, String fragmentShader) {
		if (vertexShader == null) throw new IllegalArgumentException("vertex shader must not be null");
		if (fragmentShader == null) throw new IllegalArgumentException("fragment shader must not be null");

		this.vertexShaderSource = vertexShader;
		this.fragmentShaderSource = fragmentShader;

		compileShaders(vertexShader, fragmentShader);

		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 16);
		buffer.order(ByteOrder.nativeOrder());
		matrix = buffer.asFloatBuffer();
		shaders.add(this);
	}

	/**
	 * Loads and compiles the shaders, creates a new program and links the shaders.
	 * @param vertexShader
	 * @param fragmentShader
	 */
	private void compileShaders (String vertexShader, String fragmentShader) {
		vertexShaderHandle = loadShader(GL20.GL_VERTEX_SHADER, vertexShader);
		fragmentShaderHandle = loadShader(GL20.GL_FRAGMENT_SHADER, fragmentShader);

		if (vertexShaderHandle == -1 || fragmentShaderHandle == -1) {
			isCompiled = false;
			return;
		}

		program = linkProgram();
		if (program == -1) {
			isCompiled = false;
			return;
		}

		isCompiled = true;
	}

	private int loadShader (int type, String source) {
		GL20 gl = Gdx.graphics.getGL20();
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intbuf = tmp.asIntBuffer();

		int shader = gl.glCreateShader(type);
		if (shader == 0) return -1;

		gl.glShaderSource(shader, source);
		gl.glCompileShader(shader);
		gl.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, intbuf);

		int compiled = intbuf.get(0);
		if (compiled == 0) {
			gl.glGetShaderiv(shader, GL20.GL_INFO_LOG_LENGTH, intbuf);
			int infoLogLength = intbuf.get(0);
			if (infoLogLength > 1) {
				String infoLog = gl.glGetShaderInfoLog(shader);
				log += infoLog;
			}
			return -1;
		}

		return shader;
	}

	private int linkProgram () {
		GL20 gl = Gdx.graphics.getGL20();
		int program = gl.glCreateProgram();
		if (program == 0) return -1;

		gl.glAttachShader(program, vertexShaderHandle);
		gl.glAttachShader(program, fragmentShaderHandle);
		gl.glLinkProgram(program);

		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intbuf = tmp.asIntBuffer();

		gl.glGetProgramiv(program, GL20.GL_LINK_STATUS, intbuf);
		int linked = intbuf.get(0);
		if (linked == 0) {
			gl.glGetProgramiv(program, GL20.GL_INFO_LOG_LENGTH, intbuf);
			int infoLogLength = intbuf.get(0);
			if (infoLogLength > 1) log += gl.glGetProgramInfoLog(program);

			return -1;
		}

		return program;
	}

	/**
	 * @return the log info for the shader compilation and program linking stage. Returns an empty string if the shader program
	 *         compiled successfully.
	 */
	public String getLog () {
		return log;
	}

	/**
	 * @return whether this ShaderProgram compiled successfully.
	 */
	public boolean isCompiled () {
		return isCompiled;
	}

	private int fetchAttributeLocation (String name) {
		GL20 gl = Gdx.graphics.getGL20();
		Integer location;
		if ((location = attributes.get(name)) == null) {
			location = gl.glGetAttribLocation(program, name);
			if (location != -1) attributes.put(name, location);
		}
		return location;
	}

	private int fetchUniformLocation (String name) {
		GL20 gl = Gdx.graphics.getGL20();
		Integer location;
		if ((location = uniforms.get(name)) == null) {
			location = gl.glGetUniformLocation(program, name);
			if (location == -1) throw new IllegalArgumentException("no uniform with name '" + name + "' in shader");
			uniforms.put(name, location);
		}
		return location;
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a {@link
	 * #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value the value
	 */
	public void setUniformi (String name, int value) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform1i(location, value);
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a {@link
	 * #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 */
	public void setUniformi (String name, int value1, int value2) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform2i(location, value1, value2);
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a {@link
	 * #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 */
	public void setUniformi (String name, int value1, int value2, int value3) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform3i(location, value1, value2, value3);
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a {@link
	 * #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 * @param value4 the fourth value
	 */
	public void setUniformi (String name, int value1, int value2, int value3, int value4) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform4i(location, value1, value2, value3, value4);
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a {@link
	 * #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value the value
	 */
	public void setUniformf (String name, float value) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform1f(location, value);
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a {@link
	 * #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 */
	public void setUniformf (String name, float value1, float value2) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform2f(location, value1, value2);
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a {@link
	 * #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 */
	public void setUniformf (String name, float value1, float value2, float value3) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform3f(location, value1, value2, value3);
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a {@link
	 * #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 * @param value4 the fourth value
	 */
	public void setUniformf (String name, float value1, float value2, float value3, float value4) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform4f(location, value1, value2, value3, value4);
	}

	/**
	 * Sets the uniform matrix with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * @param name the name of the uniform
	 * @param matrix the matrix
	 */
	public void setUniformMatrix (String name, Matrix4 matrix) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchUniformLocation(name);
		this.matrix.put(matrix.val);
		this.matrix.position(0);
		gl.glUniformMatrix4fv(location, 1, false, this.matrix);
	}

	/**
	 * Sets the vertex attribute with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the attribute name
	 * @param size the number of components, must be >= 1 and <= 4
	 * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT,
	 *           GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
	 * @param normalize whether fixed point data should be normalized. Will not work on the desktop
	 * @param stride the stride in bytes between successive attributes
	 * @param buffer the buffer containing the vertex attributes.
	 */
	public void setVertexAttribute (String name, int size, int type, boolean normalize, int stride, FloatBuffer buffer) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchAttributeLocation(name);
		gl.glVertexAttribPointer(location, size, type, normalize, stride, buffer);
	}

	/**
	 * Sets the vertex attribute with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the attribute name
	 * @param size the number of components, must be >= 1 and <= 4
	 * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT,
	 *           GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
	 * @param normalize whether fixed point data should be normalized. Will not work on the desktop
	 * @param stride the stride in bytes between successive attributes
	 * @param offset byte offset into the vertex buffer object bound to GL20.GL_ARRAY_BUFFER.
	 */
	public void setVertexAttribute (String name, int size, int type, boolean normalize, int stride, int offset) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchAttributeLocation(name);
		if (location == -1) return;
		gl.glVertexAttribPointer(location, size, type, normalize, stride, offset);
	}

	/**
	 * Makes OpenGL ES 2.0 use this vertex and fragment shader pair. When you are done with this shader you have to call {@link
	 * ShaderProgram#end()}.
	 */
	public void begin () {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		gl.glUseProgram(program);
	}

	/**
	 * Disables this shader. Must be called when one is done with the shader. Don't mix it with dispose, that will release the
	 * shader resources.
	 */
	public void end () {
		GL20 gl = Gdx.graphics.getGL20();
		gl.glUseProgram(0);
	}

	/**
	 * Disposes all resources associated with this shader. Must be called when the shader is no longer used.
	 */
	public void dispose () {
		GL20 gl = Gdx.graphics.getGL20();
		gl.glUseProgram(0);
		gl.glDeleteShader(vertexShaderHandle);
		gl.glDeleteShader(fragmentShaderHandle);
		gl.glDeleteProgram(program);
		shaders.remove(this);
	}

	/**
	 * Disables the vertex attribute with the given name
	 * @param name the vertex attribute name
	 */
	public void disableVertexAttribute (String name) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchAttributeLocation(name);
		if (location == -1) return;
		gl.glDisableVertexAttribArray(location);
	}

	/**
	 * Enables the vertex attribute with the given name
	 * @param name the vertex attribute name
	 */
	public void enableVertexAttribute (String name) {
		GL20 gl = Gdx.graphics.getGL20();
		checkManaged();
		int location = fetchAttributeLocation(name);
		if (location == -1) return;
		gl.glEnableVertexAttribArray(location);
	}

	private void checkManaged () {
		if (invalidated) {
			compileShaders(vertexShaderSource, fragmentShaderSource);
			invalidated = false;
		}		
	}

	/**
	 * Invalidates all shaders so the next time they are used new handles are generated
	 */
	public static void invalidateAllShaderPrograms () {
		if (Gdx.graphics.getGL20() == null) return;

		for (int i = 0; i < shaders.size(); i++) {
			shaders.get(i).invalidated = true;
			shaders.get(i).checkManaged();
		}
	}

	public static void clearAllShaderPrograms () {
		shaders.clear();
	}

	/**
	 * Sets the given attribute
	 * 
	 * @param name the name of the attribute
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 * @param value4 the fourth value
	 */
	public void setAttributef (String name, float value1, float value2, float value3, float value4) {
		GL20 gl = Gdx.graphics.getGL20();
		int location = fetchAttributeLocation(name);
		gl.glVertexAttrib4f(location, value1, value2, value3, value4);
	}
}

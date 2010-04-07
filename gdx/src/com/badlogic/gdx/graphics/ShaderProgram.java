/*
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with libgdx.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import com.badlogic.gdx.math.Matrix;

/**
 * A shader program encapsulates a vertex and fragment shader
 * pair linked to form a shader program useable with OpenGL ES 2.0.
 * @author mzechner
 *
 */
public class ShaderProgram 
{
	/** the gl instance **/
	private final GL20 gl;

	/** the log **/
	private String log = "";
	
	/** whether this program compiled succesfully **/
	private boolean isCompiled;
	
	/** uniform lookup **/
	private final HashMap<String, Integer> uniforms = new HashMap<String, Integer>();
	
	/** attribute lookup **/
	private final HashMap<String, Integer> attributes = new HashMap<String, Integer>( );

	/** program handle **/
	private int program;
	
	/** vertex shader handle **/
	private int vertexShaderHandle;
	
	/** fragment shader handle **/
	private int fragmentShaderHandle;
	
	/** matrix float buffer **/
	private final FloatBuffer matrix;
	
	/** managed? **/
	private final boolean managed;
	
	/** vertex shader source **/
	private final String vertexShaderSource;
	
	/** fragment shader source **/
	private final String fragmentShaderSource;
	
	/**
	 * Construcs a new JOglShaderProgram and immediatly compiles it. 
	 * 
	 * @param gl the GL20 instance
	 * @param vertexShader the vertex shader
	 * @param fragmentShader the fragment shader
	 */
	
	public ShaderProgram( GL20 gl, String vertexShader, String fragmentShader, boolean managed )
	{
		if( gl == null )
			throw new IllegalArgumentException( "gl must not be null" );
		if( vertexShader == null )
			throw new IllegalArgumentException( "vertex shader must not be null" );
		if( fragmentShader == null )
			throw new IllegalArgumentException( "fragment shader must not be null" );
		this.gl = gl;
		
		this.managed = managed;
		this.vertexShaderSource = vertexShader;
		this.fragmentShaderSource = fragmentShader;
		
		compileShaders( vertexShader, fragmentShader );
		
		ByteBuffer buffer = ByteBuffer.allocateDirect( 4 * 16 );
		buffer.order(ByteOrder.nativeOrder());
		matrix = buffer.asFloatBuffer();
	}
	
	/**
	 * Loads and compiles the shaders, creates a new program and links the shaders.
	 * @param vertexShader
	 * @param fragmentShader
	 */
	private void compileShaders( String vertexShader, String fragmentShader )
	{
		vertexShaderHandle = loadShader( GL20.GL_VERTEX_SHADER, vertexShader );
		fragmentShaderHandle = loadShader( GL20.GL_FRAGMENT_SHADER, fragmentShader );
		
		if( vertexShaderHandle == -1 || fragmentShaderHandle == -1 )
		{
			isCompiled = false;
			return;
		}
		
		program = linkProgram( );
		if( program == -1 )
		{
			isCompiled = false;
			return;
		}
		
		isCompiled = true;
	}
	
	private int loadShader( int type, String source )
	{
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intbuf = tmp.asIntBuffer();
		
		int shader = gl.glCreateShader( type );
		if( shader == 0 )
			return -1;
		
		gl.glShaderSource( shader, source );
		gl.glCompileShader( shader );
		gl.glGetShaderiv( shader, GL20.GL_COMPILE_STATUS, intbuf );
		
		int compiled = intbuf.get(0);
		if( compiled == 0 )
		{					
			gl.glGetShaderiv( shader, GL20.GL_INFO_LOG_LENGTH, intbuf );
			int infoLogLength = intbuf.get(0);
			if( infoLogLength > 1 )
			{
				String infoLog = gl.glGetShaderInfoLog( shader );
				log += infoLog;
			}
			return -1;
		}
		
		return shader;
	}
	
	private int linkProgram( )
	{
		int program = gl.glCreateProgram();
		if( program == 0 )		
			return -1;		
		
		gl.glAttachShader( program, vertexShaderHandle );
		gl.glAttachShader( program, fragmentShaderHandle );				
		gl.glLinkProgram( program );
		
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intbuf = tmp.asIntBuffer();
		
		gl.glGetProgramiv( program, GL20.GL_LINK_STATUS, intbuf );
		int linked = intbuf.get(0);
		if( linked == 0 )
		{
			gl.glGetProgramiv( program, GL20.GL_INFO_LOG_LENGTH, intbuf );
			int infoLogLength = intbuf.get(0);
			if( infoLogLength > 1 )			
				log += gl.glGetProgramInfoLog( program );								
			
			return -1;
		}
		
		return program;
	}
	
	/**
	 * @return the log info for the shader compilation and program linking stage. Returns an empty string if the shader program compiled successfully.
	 */	
	public String getLog() 
	{	
		return log;
	}

	/**
	 * @return whether this ShaderProgram compiled successfully.
	 */	
	public boolean isCompiled() 
	{	
		return isCompiled;
	}

	private int fetchAttributeLocation( String name )
	{
		Integer location;
		if( (location = attributes.get( name )) == null )
		{
			location = gl.glGetAttribLocation( program, name );
			if( location == - 1 )
				throw new IllegalArgumentException( "no attribute with name '" + name + "' in shader" );
			attributes.put( name, location );			
		}
		return location;
	}
	
	private int fetchUniformLocation( String name )
	{
		Integer location;
		if( (location = uniforms.get( name )) == null )
		{
			location = gl.glGetUniformLocation( program, name );
			if( location == - 1 )
				throw new IllegalArgumentException( "no uniform with name '" + name + "' in shader" );
			uniforms.put( name, location );			
		}
		return location;
	}
	
	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case
	 * it is not called in between a {@link begin()}/{@link end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value the value
	 */	
	public void setUniformi(String name, int value) 
	{	
		checkManaged( );
		int location = fetchUniformLocation(name);		
		gl.glUniform1i( location, value );
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case
	 * it is not called in between a {@link begin()}/{@link end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value 
	 */
	public void setUniformi(String name, int value1, int value2) 
	{	
		checkManaged( );
		int location = fetchUniformLocation(name);
		gl.glUniform2i( location, value1, value2 );
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case
	 * it is not called in between a {@link begin()}/{@link end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value 
	 */
	public void setUniformi(String name, int value1, int value2, int value3) 
	{	
		checkManaged( );
		int location = fetchUniformLocation(name);
		gl.glUniform3i( location, value1, value2, value3 );
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case
	 * it is not called in between a {@link begin()}/{@link end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value 
	 * @param value4 the fourth value
	 */
	public void setUniformi(String name, int value1, int value2, int value3, int value4) 
	{	
		checkManaged( );
		int location = fetchUniformLocation(name);
		gl.glUniform4i( location, value1, value2, value3, value4 );
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case
	 * it is not called in between a {@link begin()}/{@link end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value the value
	 */	
	public void setUniformf(String name, float value) 
	{	
		checkManaged( );
		int location = fetchUniformLocation(name);
		gl.glUniform1f( location, value );
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case
	 * it is not called in between a {@link begin()}/{@link end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value 
	 */
	public void setUniformf(String name, float value1, float value2) 
	{	
		checkManaged( );
		int location = fetchUniformLocation(name);
		gl.glUniform2f( location, value1, value2 );
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case
	 * it is not called in between a {@link begin()}/{@link end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value 
	 */
	public void setUniformf(String name, float value1, float value2, float value3) 
	{	
		checkManaged( );
		int location = fetchUniformLocation(name);
		gl.glUniform3f( location, value1, value2, value3 );
	}

	/**
	 * Sets the uniform with the given name. Throws an IllegalArgumentException in case
	 * it is not called in between a {@link begin()}/{@link end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value 
	 * @param value4 the fourth value
	 */
	public void setUniformf(String name, float value1, float value2, float value3, float value4) 
	{	
		checkManaged( );
		int location = fetchUniformLocation(name);
		gl.glUniform4f( location, value1, value2, value3, value4 );
	}

	/**
	 * Sets the uniform matrix with the given name. Throws an IllegalArgumentException in case
	 * it is not called in between a {@link begin()}/{@link end()} block.
	 * @param name the name of the uniform
	 * @param matrix the matrix
	 */
	public void setUniformMatrix(String name, Matrix matrix) 
	{	
		checkManaged( );
		int location = fetchUniformLocation(name);
		this.matrix.put( matrix.val );
		this.matrix.position(0);
		gl.glUniformMatrix4fv(location, 1, false, this.matrix );
	}

	/**
	 * Sets the vertex attribute with the given name. Throws an IllegalArgumentException in case
	 * it is not called in between a {@link begin()}/{@link end()} block.
	 * 
	 * @param name the attribute name
	 * @param size the number of components, must be >= 1 and <= 4
	 * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT, GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
	 * @param normalize whether fixed point data should be normalized. Will not work on the desktop
	 * @param stride the stride in bytes between successive attributes
	 * @param buffer the buffer containing the vertex attributes.
	 */
	public void setVertexAttribute(String name, int size, int type, boolean normalize, int stride, FloatBuffer buffer) 
	{	
		checkManaged( );
		int location = fetchAttributeLocation(name);
		gl.glVertexAttribPointer( location, size, type, normalize, stride, buffer );
	}

	/**
	 * Sets the vertex attribute with the given name. Throws an IllegalArgumentException in case
	 * it is not called in between a {@link begin()}/{@link end()} block.
	 * 
	 * @param name the attribute name
	 * @param size the number of components, must be >= 1 and <= 4
	 * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT, GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
	 * @param normalize whether fixed point data should be normalized. Will not work on the desktop
	 * @param stride the stride in bytes between successive attributes
	 * @param offset byte offset into the vertex buffer object bound to GL20.GL_ARRAY_BUFFER.
	 */
	public void setVertexAttribute(String name, int size, int type, boolean normalize, int stride, int offset) 
	{	
		checkManaged( );
		int location = fetchAttributeLocation(name);
		gl.glVertexAttribPointer( location, size, type, normalize, stride, offset );
	}
	
	/**
	 * Makes OpenGL ES 2.0 use this vertex and fragment 
	 * shader pair. When you are done with this shader you
	 * have to call {@link ShaderProgram.end()}.
	 */
	public void begin() 
	{	
		checkManaged( );
		gl.glUseProgram( program );
	}

	/**
	 * Disables this shader. Must be called when one is done
	 * with the shader. Don't mix it with dispose, that will 
	 * release the shader resources.
	 */
	public void end() 
	{	
		gl.glUseProgram( 0 );
	}
	
	/**
	 * Disposes all resources associated with this shader. Must be
	 * called when the shader is no longer used.
	 */
	public void dispose() 
	{	
		gl.glDeleteShader(vertexShaderHandle);
		gl.glDeleteShader(fragmentShaderHandle);
		gl.glDeleteProgram( program );
	}

	/**
	 * Disables the vertex attribute with the given name
	 * @param name the vertex attribute name
	 */
	public void disableVertexAttribute(String name) 
	{	
		checkManaged( );
		int location = fetchAttributeLocation( name );
		gl.glDisableVertexAttribArray( location );
	}

	/**
	 * Enables the vertex attribute with the given name
	 * @param name the vertex attribute name
	 */
	public void enableVertexAttribute(String name) 
	{	
		checkManaged( );
		int location = fetchAttributeLocation( name );
		gl.glEnableVertexAttribArray( location );
	}
	
	private void checkManaged( )
	{
		if( !managed )
			return;
		
		if( gl.glIsProgram(program) == false )
			compileShaders(vertexShaderSource, fragmentShaderSource );
	}
}

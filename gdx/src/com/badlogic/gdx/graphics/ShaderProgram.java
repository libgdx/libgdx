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

import java.nio.FloatBuffer;

import com.badlogic.gdx.math.Matrix;

/**
 * A shader program encapsulates a vertex and fragment shader
 * pair linked to form a shader program useable with OpenGL ES 2.0.
 * @author mzechner
 *
 */
public interface ShaderProgram 
{
	/**
	 * @return whether this ShaderProgram compiled successfully.
	 */
	public boolean isCompiled( );
	
	/**
	 * @return the log info for the shader compilation and program linking stage. Returns an empty string if the shader program compiled successfully.
	 */
	public String getLog( );
	
	/**
	 * Makes OpenGL ES 2.0 use this vertex and fragment 
	 * shader pair. When you are done with this shader you
	 * have to call {@link ShaderProgram.end()}.
	 */
	public void begin( );
	
	/**
	 * Disables this shader. Must be called when one is done
	 * with the shader. Don't mix it with dispose, that will 
	 * release the shader resources.
	 */
	public void end( );
	
	/**
	 * Sets the vertex attribute with the given name.
	 * 
	 * @param name the attribute name
	 * @param size the number of components, must be >= 1 and <= 4
	 * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT, GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
	 * @param normalize whether fixed point data should be normalized. Will not work on the desktop
	 * @param stride the stride in bytes between successive attributes
	 * @param buffer the buffer containing the vertex attributes.
	 */
	public void setVertexAttribute( String name, int size, int type, boolean normalize, int stride, FloatBuffer buffer );
	
	/**
	 * Sets the vertex attribute with the given name. 
	 * 
	 * @param name the attribute name
	 * @param size the number of components, must be >= 1 and <= 4
	 * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT, GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
	 * @param normalize whether fixed point data should be normalized. Will not work on the desktop
	 * @param stride the stride in bytes between successive attributes
	 * @param offset byte offset into the vertex buffer object bound to GL20.GL_ARRAY_BUFFER.
	 */
	public void setVertexAttribute( String name, int size, int type, boolean normalize, int offset );
	
	/**
	 * Sets the uniform with the given name
	 * @param name the name of the uniform
	 * @param value the value
	 */
	public void setUniform( String name, int value );
	
	/**
	 * Sets the uniform with the given name
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value 
	 */
	public void setUniform( String name, int value1, int value2 );
	
	/**
	 * Sets the uniform with the given name
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value 
	 */
	public void setUniform( String name, int value1, int value2, int value3 );
	
	/**
	 * Sets the uniform with the given name
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 * @param value4 the fourth value 
	 */
	public void setUniform( String name, int value1, int value2, int value3, int value4 );
	
	/**
	 * Sets the uniform with the given name
	 * 
	 * @param name the name of the uniform
	 * @param value the value
	 */
	public void setUniform( String name, float value );
	
	/**
	 * Sets the uniform with the given name
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 */
	public void setUniform( String name, float value1, float value2 );
	
	/**
	 * Sets the uniform with the given name
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value 
	 */
	public void setUniform( String name, float value1, float value2, float value3 );
	
	/**
	 * Sets the uniform with the given name
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 * @param value4 the fourth value 
	 */ 
	public void setUniform( String name, float value1, float value2, float value3, float value4 );
	
	/**
	 * Sets the uniform matrix with the given name
	 * @param name the name of the uniform
	 * @param matrix the matrix
	 */
	public void setUniformMatrix( String name, Matrix matrix );
	
	/**
	 * Disposes all resources associated with this shader. Must be
	 * called when the shader is no longer used.
	 */
	public void dispose( );
}

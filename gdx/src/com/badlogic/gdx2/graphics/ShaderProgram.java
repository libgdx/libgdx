package com.badlogic.gdx2.graphics;

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
	 * shader pair.
	 */
	public void use( );
}

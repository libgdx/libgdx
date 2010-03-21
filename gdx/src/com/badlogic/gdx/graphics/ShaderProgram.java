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

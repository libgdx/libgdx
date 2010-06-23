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
package com.badlogic.gdx.backends.applet;

import java.io.File;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;

/**
 * A {@link FileHandle} implementation for the desktop. 
 * 
 * @author mzechner
 *
 */
public class AppletFileHandle implements FileHandle
{
	/** the file **/
	private final String uri;
	
	AppletFileHandle( String uri )
	{
		this.uri = uri;
	}
	
	/**
	 * @return the underlying {@link InputStream}. Needs to be closed when the handle is no longer used.
	 */
	public InputStream getInputStream( )
	{
		return getClass().getResourceAsStream( "/" + uri );
	}
}

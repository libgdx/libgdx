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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

/**
 * Implementation for a desktop application of {@link Files}. Internal
 * resources are relative to the application root directory, external files
 * are relative to the user's home directory.
 *  
 * @author mzechner
 *
 */
final class AppletFiles implements Files
{	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileHandle getFileHandle(String filename, FileType type) 
	{			
		if( type == FileType.Absolut || type == FileType.External )
			return null;
		else
			return new AppletFileHandle( filename );			
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] listDirectory(String directory, FileType type) 
	{
		return new String[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean makeDirectory(String directory, FileType type) 
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream readFile(String fileName, FileType type) 
	{
		if( type == FileType.Absolut || type == FileType.External )
			return null;
		else
			return new AppletFileHandle( fileName ).getInputStream();	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream writeFile(String filename, FileType type) 
	{
		return null;
	}
}

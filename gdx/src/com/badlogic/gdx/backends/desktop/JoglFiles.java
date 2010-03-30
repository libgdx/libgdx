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
package com.badlogic.gdx.backends.desktop;

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
final class JoglFiles implements Files
{
	private final String externalPath = System.getProperty("user.home") + "/";		
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileHandle getFileHandle(String filename, FileType type) 
	{	
		File file = null;
		if( type == FileType.Absolut || type == FileType.Internal )
			file = new File( filename );
		else
			file = new File( this.externalPath + filename );
			
		if( file.exists() == false )
			return null;
		else
			return new JoglFileHandle( file );			
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] listDirectory(String directory, FileType type) 
	{
		File file = null;
		if( type == FileType.Absolut || type == FileType.Internal )
			file = new File( directory );
		else
			file = new File( this.externalPath + directory );
		
		return file.list();			
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean makeDirectory(String directory, FileType type) 
	{
		File file = null;
		
		if( type == FileType.Internal )
			return false;
		
		if( type == FileType.Absolut )
			file = new File( directory );
		else
			file = new File( this.externalPath + directory );
		return file.mkdirs();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream readFile(String fileName, FileType type) 
	{
		File file = null;
		InputStream in = null;
		if( type == FileType.Absolut || type == FileType.Internal )
			file = new File( fileName );
		else
			file = new File( this.externalPath + fileName );

		try 
		{
			in = new FileInputStream( file );
		} 
		catch (FileNotFoundException e) 
		{		
			// fall through
		}		
		
		return in;		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream writeFile(String filename, FileType type) 
	{
		File file = null;
		FileOutputStream out = null;
		
		if( type == FileType.Internal )
			return null;
		
		if( type == FileType.Absolut )
			file = new File( filename );
		else
			file = new File( this.externalPath + filename );
		
		
		try 
		{
			out = new FileOutputStream( file );
		} 
		catch (FileNotFoundException e) 
		{		
			// fall through
		}		
		
		return out;
	}
}

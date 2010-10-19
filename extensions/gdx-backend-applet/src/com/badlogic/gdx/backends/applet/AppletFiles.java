/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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
package com.badlogic.gdx.backends.applet;

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

package com.badlogic.gdx.backends.desktop;

import java.io.File;

import com.badlogic.gdx.files.FileHandle;

/**
 * A {@link FileHandle} implementation for the desktop. 
 * 
 * @author mzechner
 *
 */
public class JoglFileHandle implements FileHandle
{
	/** the file **/
	private final File file;
	
	JoglFileHandle( File file )
	{
		this.file = file;
	}
	
	/**
	 * @return the underlying {@link File}.
	 */
	public File getFile( )
	{
		return file;
	}
}

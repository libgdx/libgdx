package com.badlogic.gdx.backends.desktop;

import java.io.File;
import java.io.FileDescriptor;
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
	public boolean makeDirectory(String directory) 
	{	
		return new File( externalPath + directory ).mkdirs();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream readExternalFile(String fileName) 
	{	
		FileInputStream in = null;
				
		try 
		{
			in = new FileInputStream( externalPath + fileName );
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
	public InputStream readInternalFile(String fileName) 
	{	
		FileInputStream in = null;
		
		try 
		{
			in = new FileInputStream( fileName );
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
	public OutputStream writeExternalFile(String filename) 
	{	
		FileOutputStream out = null;
		
		try 
		{
			out = new FileOutputStream( externalPath + filename );
		} 
		catch (FileNotFoundException e) 
		{		
			// fall through
		}		
		
		return out;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileHandle getInternalFileHandle(String filename) 
	{
		File file = new File( filename );
		if( file.exists() == false )
			return null;
		else 
			return new JoglFileHandle( file );			
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileHandle getExternalFileHandle(String filename) 
	{		
		File file = new File( externalPath + filename );
		if( file.exists() == false )
			return null;
		else 
			return new JoglFileHandle( file );		
	}
}

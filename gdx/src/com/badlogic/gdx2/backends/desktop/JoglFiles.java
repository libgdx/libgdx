package com.badlogic.gdx2.backends.desktop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx2.Files;

/**
 * Implementation for a desktop application of {@link Files}. Internal
 * resources are relative to the application root directory, external files
 * are relative to the user's home directory.
 *  
 * @author mzechner
 *
 */
public class JoglFiles implements Files
{
	private final String externalPath = System.getProperty("user.home") + "/";		
	
	@Override
	public boolean makeDirectory(String directory) 
	{	
		return new File( externalPath + directory ).mkdirs();
	}

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

}

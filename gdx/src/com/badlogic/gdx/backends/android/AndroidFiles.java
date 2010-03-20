package com.badlogic.gdx.backends.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;
import android.os.Environment;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

/**
 * An implementation of the {@link Files} interface for Android. External files are stored and accessed
 * relative to Environment.getExternalStorageDirectory().getAbsolutePath(). Internal files are accessed
 * relative to the assets directory.
 * 
 * @author mzechner
 *
 */
final class AndroidFiles implements Files
{
	/** external storage path **/
	private final String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	
	/** asset manager **/
	private final AssetManager assets;
	
	AndroidFiles( AssetManager assets )
	{
		this.assets = assets;
	}
	
	/**
	 * @return the asset manager.
	 */
	protected AssetManager getAssetManager( )
	{
		return assets;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean makeDirectory(String directory) 
	{	
		return new File( sdcard + directory ).mkdirs();
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
			in = new FileInputStream( sdcard + fileName );
		}
		catch( FileNotFoundException ex )
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
		InputStream in = null;
		try
		{
			in = assets.open( fileName );
		}
		catch( Exception ex )
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
			out = new FileOutputStream( sdcard + filename );
		}
		catch( FileNotFoundException ex )
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
		boolean exists = true;
		
		try
		{
			InputStream in = assets.open( filename );
			in.close();
		}
		catch( Exception ex )
		{
			exists = false;
		}
		
		if( !exists )
			return null;
		else
			return new AndroidFileHandle( assets, filename);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileHandle getExternalFileHandle(String filename) 
	{
		if( new File( sdcard + filename ).exists() == false )
			return null;
		else
			return new AndroidFileHandle( null, sdcard + filename );
	}
}

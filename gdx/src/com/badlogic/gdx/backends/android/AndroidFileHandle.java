package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.files.FileHandle;

import android.content.res.AssetManager;

/**
 * A {@link FileHandle} implementation for Android. Encapsulates
 * assets and files on the external storage device.
 * 
 * @author mzechner
 *
 */
public class AndroidFileHandle implements FileHandle
{
	/** the asset manager or null if this is an external file **/
	private final AssetManager manager;
	
	/** the filename **/
	private final String filename;
	
	AndroidFileHandle( AssetManager manager, String filename )
	{
		this.manager = manager;
		this.filename = filename;
	}
	
	/**
	 * @return whether this is an asset file or an external file
	 */
	public boolean isAsset( )
	{
		return manager != null;
	}
	
	/**
	 * @return the {@link AssetManager} or null
	 */
	public AssetManager getAssetManager( )
	{
		return manager;
	}
	
	/**
	 * @return the filename
	 */
	public String getFileName( )
	{
		return filename;
	}
}

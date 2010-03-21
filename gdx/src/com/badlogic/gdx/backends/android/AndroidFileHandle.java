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

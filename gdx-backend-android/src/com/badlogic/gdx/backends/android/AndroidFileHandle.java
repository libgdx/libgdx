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
	
	public String toString( )
	{
		return filename;
	}
}

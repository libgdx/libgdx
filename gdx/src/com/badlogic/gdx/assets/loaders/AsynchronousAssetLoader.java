/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;

/** Base class for asynchronous {@link AssetLoader} instances. Such loaders try to load parts of an OpenGL resource, like the
 * Pixmap, on a separate thread to then load the actual resource on the thread the OpenGL context is active on.
 * @author mzechner
 * 
 * @param <T>
 * @param <P> */
public abstract class AsynchronousAssetLoader<T, P extends AssetLoaderParameters<T>> extends AssetLoader<T, P> {

	public AsynchronousAssetLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	/** Loads the non-OpenGL part of the asset and injects any dependencies of the asset into the AssetManager.
	 * @param manager
	 * @param fileName the name of the asset to load
	 * @param file the resolved file to load
	 * @param parameter the parameters to use for loading the asset */
	public abstract void loadAsync (AssetManager manager, String fileName, FileHandle file, P parameter);

	/** Called if this task is unloaded before {@link #loadSync(AssetManager, String, FileHandle, AssetLoaderParameters) loadSync}
	 * is called. This method may be invoked on any thread, but will not be invoked during or after
	 * {@link #loadSync(AssetManager, String, FileHandle, AssetLoaderParameters) loadSync}. This method is not invoked when a task
	 * is cancelled because it threw an exception, only when the asset is unloaded before loading is complete.
	 * <p>
	 * The default implementation does nothing. Subclasses should release any resources acquired in
	 * {@link #loadAsync(AssetManager, String, FileHandle, AssetLoaderParameters) loadAsync}, which may or may not have been called
	 * before this method, but never during or after this method. Note that
	 * {@link #loadAsync(AssetManager, String, FileHandle, AssetLoaderParameters) loadAsync} may still be executing when this
	 * method is called and must release any resources it allocated. */
	public void unloadAsync (AssetManager manager, String fileName, FileHandle file, P parameter) {
	}

	/** Loads the OpenGL part of the asset.
	 * @param manager
	 * @param fileName
	 * @param file the resolved file to load
	 * @param parameter */
	public abstract T loadSync (AssetManager manager, String fileName, FileHandle file, P parameter);
}

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

package com.badlogic.gdx.assets;

import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.async.AsyncTask;

/** Responsible for loading an asset through an {@link AssetLoader} based on an {@link AssetDescriptor}.
 * 
 * @author mzechner */
class AssetLoadingTask implements AsyncTask<Void> {
	AssetManager manager;
	final AssetDescriptor assetDesc;
	final AssetLoader loader;
	final AsyncExecutor executor;
	final long startTime;

	volatile boolean asyncDone = false;
	volatile boolean dependenciesLoaded = false;
	volatile Array<AssetDescriptor> dependencies;
	volatile AsyncResult<Void> depsFuture = null;
	volatile AsyncResult<Void> loadFuture = null;
	volatile Object asset = null;

	int ticks = 0;
	volatile boolean cancel = false;

	public AssetLoadingTask (AssetManager manager, AssetDescriptor assetDesc, AssetLoader loader, AsyncExecutor threadPool) {
		this.manager = manager;
		this.assetDesc = assetDesc;
		this.loader = loader;
		this.executor = threadPool;
		startTime = manager.log.getLevel() == Logger.DEBUG ? TimeUtils.nanoTime() : 0;
	}

	/** Loads parts of the asset asynchronously if the loader is an {@link AsynchronousAssetLoader}. */
	@Override
	public Void call () throws Exception {
		AsynchronousAssetLoader asyncLoader = (AsynchronousAssetLoader)loader;
		if (dependenciesLoaded == false) {
			dependencies = asyncLoader.getDependencies(assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
			if (dependencies != null) {
				removeDuplicates(dependencies);
				manager.injectDependencies(assetDesc.fileName, dependencies);
			} else {
				// if we have no dependencies, we load the async part of the task immediately.
				asyncLoader.loadAsync(manager, assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
				asyncDone = true;
			}
		} else {
			asyncLoader.loadAsync(manager, assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
		}
		return null;
	}

	/** Updates the loading of the asset. In case the asset is loaded with an {@link AsynchronousAssetLoader}, the loaders
	 * {@link AsynchronousAssetLoader#loadAsync(AssetManager, String, FileHandle, AssetLoaderParameters)} method is first called on
	 * a worker thread. Once this method returns, the rest of the asset is loaded on the rendering thread via
	 * {@link AsynchronousAssetLoader#loadSync(AssetManager, String, FileHandle, AssetLoaderParameters)}.
	 * @return true in case the asset was fully loaded, false otherwise
	 * @throws GdxRuntimeException */
	public boolean update () {
		ticks++;
		if (loader instanceof SynchronousAssetLoader) {
			handleSyncLoader();
		} else {
			handleAsyncLoader();
		}
		return asset != null;
	}

	private void handleSyncLoader () {
		SynchronousAssetLoader syncLoader = (SynchronousAssetLoader)loader;
		if (!dependenciesLoaded) {
			dependenciesLoaded = true;
			dependencies = syncLoader.getDependencies(assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
			if (dependencies == null) {
				asset = syncLoader.load(manager, assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
				return;
			}
			removeDuplicates(dependencies);
			manager.injectDependencies(assetDesc.fileName, dependencies);
		} else {
			asset = syncLoader.load(manager, assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
		}
	}

	private void handleAsyncLoader () {
		AsynchronousAssetLoader asyncLoader = (AsynchronousAssetLoader)loader;
		if (!dependenciesLoaded) {
			if (depsFuture == null) {
				depsFuture = executor.submit(this);
			} else {
				if (depsFuture.isDone()) {
					try {
						depsFuture.get();
					} catch (Exception e) {
						throw new GdxRuntimeException("Couldn't load dependencies of asset: " + assetDesc.fileName, e);
					}
					dependenciesLoaded = true;
					if (asyncDone) {
						asset = asyncLoader.loadSync(manager, assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
					}
				}
			}
		} else {
			if (loadFuture == null && !asyncDone) {
				loadFuture = executor.submit(this);
			} else {
				if (asyncDone) {
					asset = asyncLoader.loadSync(manager, assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
				} else if (loadFuture.isDone()) {
					try {
						loadFuture.get();
					} catch (Exception e) {
						throw new GdxRuntimeException("Couldn't load asset: " + assetDesc.fileName, e);
					}
					asset = asyncLoader.loadSync(manager, assetDesc.fileName, resolve(loader, assetDesc), assetDesc.params);
				}
			}
		}
	}

	private FileHandle resolve (AssetLoader loader, AssetDescriptor assetDesc) {
		if (assetDesc.file == null) assetDesc.file = loader.resolve(assetDesc.fileName);
		return assetDesc.file;
	}

	public Object getAsset () {
		return asset;
	}
	
	private void removeDuplicates(Array<AssetDescriptor> array) {
		boolean ordered = array.ordered;
		array.ordered = true;
		for (int i = 0; i < array.size; ++i) {
			final String fn = array.get(i).fileName;
			final Class type = array.get(i).type;
			for (int j = array.size - 1; j > i; --j) {
				if (type == array.get(j).type && fn.equals(array.get(j).fileName))
					array.removeIndex(j);
			}
		}
		array.ordered = ordered;
	}
}

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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;

/** Responsible for loading an asset through an {@link AssetLoader} based on an {@link AssetDescriptor}. Implements
 * {@link Callable} and is used with an {@link ExecutorService threadpool} to load parts of an asset asynchronously if the asset is
 * loaded with an {@link AsynchronousAssetLoader}.
 * 
 * @author mzechner */
class AssetLoadingTask implements Callable<Void> {
	AssetManager manager;
	final AssetDescriptor assetDesc;
	final AssetLoader loader;
	final ExecutorService threadPool;
	final long startTime;

	volatile boolean asyncDone = false;
	boolean dependenciesLoaded = false;
	Array<AssetDescriptor> dependencies;
	Future<Void> depsFuture = null;

	Future<Void> loadFuture = null;
	Object asset = null;

	int ticks = 0;
	boolean cancel = false;

	public AssetLoadingTask (AssetManager manager, AssetDescriptor assetDesc, AssetLoader loader, ExecutorService threadPool) {
		this.manager = manager;
		this.assetDesc = assetDesc;
		this.loader = loader;
		this.threadPool = threadPool;
		startTime = manager.log.getLevel() == Logger.DEBUG ? TimeUtils.nanoTime() : 0;
	}

	/** Loads parts of the asset asynchronously if the loader is an {@link AsynchronousAssetLoader}. */
	@Override
	public Void call () throws Exception {
		AsynchronousAssetLoader asyncLoader = (AsynchronousAssetLoader)loader;
		if (dependenciesLoaded == false) {
			dependencies = asyncLoader.getDependencies(assetDesc.fileName, assetDesc.params);
			if (dependencies != null) {
				for (AssetDescriptor desc : dependencies) {
					manager.injectDependency(assetDesc.fileName, desc);
				}
			} else {
				// if we have no dependencies, we load the async part of the task immediately.
				asyncLoader.loadAsync(manager, assetDesc.fileName, assetDesc.params);
				asyncDone = true;
			}
		} else {
			asyncLoader.loadAsync(manager, assetDesc.fileName, assetDesc.params);
		}
		return null;
	}

	/** Updates the loading of the asset. In case the asset is loaded with an {@link AsynchronousAssetLoader}, the loaders
	 * {@link AsynchronousAssetLoader#loadAsync(AssetManager, String, AssetLoaderParameters)} method is first called on a worker
	 * thread. Once this method returns, the rest of the asset is loaded on the rendering thread via
	 * {@link AsynchronousAssetLoader#loadSync(AssetManager, String, AssetLoaderParameters)}.
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
			dependencies = syncLoader.getDependencies(assetDesc.fileName, assetDesc.params);
			if (dependencies == null) {
				asset = syncLoader.load(manager, assetDesc.fileName, assetDesc.params);
				return;
			}
			for (AssetDescriptor desc : dependencies) {
				manager.injectDependency(assetDesc.fileName, desc);
			}
		} else {
			asset = syncLoader.load(manager, assetDesc.fileName, assetDesc.params);
		}
	}

	private void handleAsyncLoader () {
		AsynchronousAssetLoader asyncLoader = (AsynchronousAssetLoader)loader;
		if (!dependenciesLoaded) {
			if (depsFuture == null) {
				depsFuture = threadPool.submit(this);
			} else {
				if (depsFuture.isDone()) {
					try {
						depsFuture.get();
					} catch (Exception e) {
						throw new GdxRuntimeException("Couldn't load dependencies of asset '" + assetDesc.fileName + "'", e);
					}
					dependenciesLoaded = true;
					if(asyncDone) {
						asset = asyncLoader.loadSync(manager, assetDesc.fileName, assetDesc.params);
					}
				}
			}
		} else {
			if (loadFuture == null && !asyncDone) {
				loadFuture = threadPool.submit(this);
			} else {
				if(asyncDone) {
					asset = asyncLoader.loadSync(manager, assetDesc.fileName, assetDesc.params);
				} else if (loadFuture.isDone()) {
					try {
						loadFuture.get();
					} catch (Exception e) {
						throw new GdxRuntimeException("Couldn't load asset '" + assetDesc.fileName + "'", e);
					}
					asset = asyncLoader.loadSync(manager, assetDesc.fileName, assetDesc.params);
				}
			}
		}
	}

	public Object getAsset () {
		return asset;
	}
}

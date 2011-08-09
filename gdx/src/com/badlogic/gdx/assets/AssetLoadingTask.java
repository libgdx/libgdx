package com.badlogic.gdx.assets;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Responsible for loading an asset through an {@link AssetLoader} based on 
 * an {@link AssetDescriptor}. Implements {@link Callable} and is used with
 * an {@link ExecutorService threadpool} to load parts of an asset asynchronously 
 * if the asset is loaded with an {@link AsynchronousAssetLoader}. 
 * 
 * @author mzechner
 *
 */
class AssetLoadingTask implements Callable<Void> {
	final AssetDescriptor assetDesc;
	final AssetLoader loader;
	final ExecutorService threadPool;
	boolean dependenciesLoaded = false;
	Future<Void> loadFuture = null;
	Future<Void> depsFuture = null;
	boolean updateOnRenderThread = false;
	Object asset = null;
	AssetManager manager;
	
	public AssetLoadingTask(AssetManager manager, AssetDescriptor assetDesc, AssetLoader loader, ExecutorService threadPool) {
		this.manager = manager;
		this.assetDesc = assetDesc;
		this.loader = loader;
		this.threadPool = threadPool;
	}
	
	/**
	 * Loads parts of the asset asynchronously if the loader is an 
	 * {@link AsynchronousAssetLoader}.
	 */
	@Override
	public Void call () throws Exception {
		AsynchronousAssetLoader asyncLoader = (AsynchronousAssetLoader)loader;
		if(dependenciesLoaded == false) {
			Array<AssetDescriptor> dependencies = asyncLoader.getDependencies(assetDesc.fileName, assetDesc.params);
			if(dependencies != null) {
				for(AssetDescriptor desc: dependencies) {
					manager.injectTask(desc);
				}
			}
		} else {
			asyncLoader.loadAsync(manager, assetDesc.fileName, assetDesc.params);
		}
		return null;
	}
	
	/**
	 * Updates the loading of the asset. In case the asset is loaded with an
	 * {@link AsynchronousAssetLoader}, the loaders {@link AsynchronousAssetLoader#loadAsync(AssetManager, String, Object)}
	 * method is first called on a worker thread. Once this method returns, the rest 
	 * of the asset is loaded on the rendering thread via {@link AsynchronousAssetLoader#loadSync()}. 
	 * @return true in case the asset was fully loaded, false otherwise
	 * @throws GdxRuntimeException
	 */
	public boolean update() {
		if(loader instanceof SynchronousAssetLoader) {
			handleSyncLoader();
		} else {
			handleAsyncLoader();
		}
		return asset != null;
	}
	
	private void handleSyncLoader() {
		SynchronousAssetLoader syncLoader = (SynchronousAssetLoader)loader;
		if(!dependenciesLoaded) {
			dependenciesLoaded = true;
			Array<AssetDescriptor> dependencies = syncLoader.getDependencies(assetDesc.fileName, assetDesc.params);
			if(dependencies == null) {
				asset = syncLoader.load(manager, assetDesc.fileName, assetDesc.params);
				return;
			}
			for(AssetDescriptor desc: dependencies) {
				manager.injectTask(desc);
			}
		} else {
			asset = syncLoader.load(manager, assetDesc.fileName, assetDesc.params);
		}
	}
	
	private void handleAsyncLoader() {
		AsynchronousAssetLoader asyncLoader = (AsynchronousAssetLoader)loader;
		if(!dependenciesLoaded) {
			if(depsFuture == null) {
				depsFuture = threadPool.submit(this);
			} else {
				if(depsFuture.isDone()) {
					try {
						depsFuture.get();
					} catch (Exception e) {
						throw new GdxRuntimeException("Couldn't load dependencies of asset '" + assetDesc.fileName + "'", e);
					}
					dependenciesLoaded = true;
				}
			}
		} else {
			if(loadFuture == null) {
				loadFuture = threadPool.submit(this);
			} else {
				if(loadFuture.isDone()) {
					try {
						loadFuture.get();
					} catch (Exception e) {
						throw new GdxRuntimeException("Couldn't load asset '" + assetDesc.fileName + "'", e);
					}
					asset = asyncLoader.loadSync();
				}
			}
		}
	}
	
	public Object getAsset() {
		return asset;
	}
}
package com.badlogic.gdx.assets;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
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
	Future<Void> future = null;
	boolean updateOnRenderThread = false;
	Object asset = null;
	
	public AssetLoadingTask(AssetDescriptor assetDesc, AssetLoader loader, ExecutorService threadPool) {
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
		asyncLoader.loadAsync(assetDesc.fileName, assetDesc.params);
		return null;
	}
	
	/**
	 * Updates the loading of the asset. In case the asset is loaded with an
	 * {@link AsynchronousAssetLoader}, the loaders {@link AsynchronousAssetLoader#loadAsync(String, Object)}
	 * method is first called on a worker thread. Once this method returns, the rest 
	 * of the asset is loaded on the rendering thread via {@link AsynchronousAssetLoader#loadSync()}. 
	 * @return true in case the asset was fully loaded, false otherwise
	 * @throws GdxRuntimeException
	 */
	public boolean update() {
		if(loader instanceof SynchronousAssetLoader) {
			SynchronousAssetLoader syncLoader = (SynchronousAssetLoader)loader;
			asset = syncLoader.load(assetDesc.fileName, assetDesc.params);
		} else {
			AsynchronousAssetLoader asyncLoader = (AsynchronousAssetLoader)loader;
			if(future == null) {
				future = threadPool.submit(this);
			} else {
				if(future.isDone()) {
					if(!updateOnRenderThread) {
						try {
							future.get();
							updateOnRenderThread = true;
						} catch (Exception e) {
							throw new GdxRuntimeException("Couldn't load asset '" + assetDesc.fileName + "'", e);
						}
					} else {
						asset = asyncLoader.loadSync();
					}
				}
			}
		}
		return asset != null;
	}
	
	public Object getAsset() {
		return asset;
	}
}
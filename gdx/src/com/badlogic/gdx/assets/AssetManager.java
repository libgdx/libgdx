package com.badlogic.gdx.assets;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.PixmapLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

public class AssetManager implements Disposable {
	final ObjectMap<String, Class> assetTypes = new ObjectMap<String, Class>();
	final ObjectMap<Class, ObjectMap<String, Object>> assets = new ObjectMap<Class, ObjectMap<String,Object>>();
	final ObjectMap<Class, AssetLoader> loaders = new ObjectMap<Class, AssetLoader>();
	final Array<AssetDescriptor> preloadQueue = new Array<AssetDescriptor>();
	final ExecutorService threadPool;
	AssetLoadingTask task = null;
	AssetErrorListener listener = null;
	int loaded = 0;
	int toLoad = 0;
	
	public AssetManager() {
		setLoader(BitmapFont.class, new BitmapFontLoader());
		setLoader(Music.class, new MusicLoader());
		setLoader(Pixmap.class, new PixmapLoader());
		setLoader(Sound.class, new SoundLoader());
		setLoader(TextureAtlas.class, new TextureAtlasLoader());
		setLoader(Texture.class, new TextureLoader());
		threadPool = Executors.newFixedThreadPool(1, new ThreadFactory() {			
			@Override
			public Thread newThread (Runnable r) {
				Thread thread = new Thread(r, "AssetManager-Loader-Thread");
				thread.setDaemon(true);
				return thread;
			}
		});
	}
	
	public synchronized <T> T get (String fileName, Class<T> type) {
		ObjectMap<String, Object> assetsByType = assets.get(type);
		if(assetsByType == null) throw new GdxRuntimeException("Asset '" + fileName + "' not loaded");
		T asset = (T)assetsByType.get(fileName);
		if(asset == null) throw new GdxRuntimeException("Asset '" + fileName + "' not loaded");
		return asset;
	}

	public synchronized void remove (String fileName) {
		Class type = assetTypes.get(fileName);
		if(type == null) throw new GdxRuntimeException("Asset '" + fileName + "' not loaded");
		assetTypes.remove(fileName);
		Object asset = assets.get(type).remove(fileName);
		if(asset instanceof Disposable) ((Disposable)asset).dispose();
	}

	public synchronized <T> boolean containsAsset(T asset) {
		ObjectMap<String, Object> typedAssets = assets.get(asset.getClass());
		for(String fileName: typedAssets.keys()) {
			Object otherAsset = typedAssets.get(fileName);
			if(otherAsset == asset || asset.equals(otherAsset)) return true;
		}
		return false;
	}
	
	public synchronized <T> String getAssetFileName (T asset) {
		ObjectMap<String, Object> typedAssets = assets.get(asset.getClass());
		for(String fileName: typedAssets.keys()) {
			Object otherAsset = typedAssets.get(fileName);
			if(otherAsset == asset || asset.equals(otherAsset)) return fileName;
		}
		return null;
	}

	public synchronized boolean isLoaded (String fileName) {
		return assetTypes.containsKey(fileName);
	}

	public synchronized <T> void preload (String fileName, Class<T> type) {
		preload(fileName, type, null);
	}

	public synchronized <T> void preload (String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
		AssetLoader loader = loaders.get(type);
		if(loader == null) throw new GdxRuntimeException("No loader for type '" + type.getSimpleName() + "'");
		if(preloadQueue.size == 0) {
			loaded = 0;
			toLoad = 0;
		}
		toLoad++;
		preloadQueue.add(new AssetDescriptor(fileName, type, parameter));
	}

	public synchronized boolean update () {
		try {
			if(task == null) {
				if(preloadQueue.size == 0) return true;
				nextTask();
			}
			return updateTask() && preloadQueue.size == 0;
		} catch(Throwable t) {
			return handleTaskError(t);
		}
	}
	
	private void nextTask() {
		AssetDescriptor assetDesc = preloadQueue.removeIndex(0);
		AssetLoader loader = loaders.get(assetDesc.type);
		if(loader == null) throw new GdxRuntimeException("No loader for type '" + assetDesc.type.getSimpleName() + "'");
		task = new AssetLoadingTask(assetDesc, loader, threadPool);
	}
	
	private boolean updateTask() {
		if(task.update()) {
			assetTypes.put(task.assetDesc.fileName, task.assetDesc.type);
			ObjectMap<String, Object> typeToAssets = assets.get(task.assetDesc.type);
			if(typeToAssets == null) {
				typeToAssets = new ObjectMap<String, Object>();
				assets.put(task.assetDesc.type, typeToAssets);
			}
			typeToAssets.put(task.assetDesc.fileName, task.getAsset());
			task = null;
			loaded++;
			return true;
		} else {
			return false;
		}
	}
	
	private boolean handleTaskError(Throwable t) {
		AssetDescriptor assetDesc = task.assetDesc;
		task = null;
		if(listener != null) {
			listener.error(assetDesc.fileName, assetDesc.type, t);
			return preloadQueue.size == 0;
		} else {
			throw new GdxRuntimeException(t);
		}
	}
	
	public synchronized <T, P> void setLoader(Class<T> type, AssetLoader<T, P> loader) {
		loaders.put(type, loader);
	}

	public synchronized int getLoadedAssets () {
		return assetTypes.size;
	}

	public synchronized int getQueuedAssets () {
		return preloadQueue.size + (task == null?0:1);
	}
	
	public synchronized float getProgress() {
		return loaded / (float)toLoad;
	}

	public synchronized void setErrorListener(AssetErrorListener listener) {
		this.listener = listener;
	}
	
	public synchronized void dispose () {
		threadPool.shutdown();
		Array<String> assets = assetTypes.keys().toArray();
		for(String asset: assets) {
			remove(asset);
		}
	}

	public synchronized void clear () {
		if(preloadQueue.size > 0 || task != null) {
			try {
				while(!updateTask());
			} catch(Throwable t) {
				handleTaskError(t);
			}
			preloadQueue.clear();
			task = null;
		}
		
		Array<String> assets = assetTypes.keys().toArray();
		for(String asset: assets) {
			remove(asset);
		}
		
		this.assets.clear();
		this.assetTypes.clear();
		this.loaded = 0;
	}
}

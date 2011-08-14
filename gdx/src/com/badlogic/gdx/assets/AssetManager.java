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

import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.PixmapLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;

public class AssetManager implements Disposable {
	final ObjectMap<Class, ObjectMap<String, Object>> assets = new ObjectMap<Class, ObjectMap<String, Object>>();
	final ObjectMap<String, Class> assetTypes = new ObjectMap<String, Class>();
	final ObjectMap<String, Array<String>> assetDependencies = new ObjectMap<String, Array<String>>();

	final ObjectMap<Class, AssetLoader> loaders = new ObjectMap<Class, AssetLoader>();
	final Array<AssetDescriptor> preloadQueue = new Array<AssetDescriptor>();
	final ExecutorService threadPool;

	Stack<AssetLoadingTask> tasks = new Stack<AssetLoadingTask>();
	AssetErrorListener listener = null;
	int loaded = 0;
	int toLoad = 0;

	Logger log = new Logger(AssetManager.class.getSimpleName());

	/** Creates a new AssetManager. */
	public AssetManager () {
		log.setEnabled(false);
		setLoader(BitmapFont.class, new BitmapFontLoader(new InternalFileHandleResolver()));
		setLoader(Music.class, new MusicLoader(new InternalFileHandleResolver()));
		setLoader(Pixmap.class, new PixmapLoader(new InternalFileHandleResolver()));
		setLoader(Sound.class, new SoundLoader(new InternalFileHandleResolver()));
		setLoader(TextureAtlas.class, new TextureAtlasLoader(new InternalFileHandleResolver()));
		setLoader(Texture.class, new TextureLoader(new InternalFileHandleResolver()));
		threadPool = Executors.newFixedThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread (Runnable r) {
				Thread thread = new Thread(r, "AssetManager-Loader-Thread");
				thread.setDaemon(true);
				return thread;
			}
		});
	}

	/** @param fileName the asset file name
	 * @param type the asset type
	 * @return the asset or null */
	public synchronized <T> T get (String fileName, Class<T> type) {
		ObjectMap<String, Object> assetsByType = assets.get(type);
		if (assetsByType == null) throw new GdxRuntimeException("Asset '" + fileName + "' not loaded");
		T asset = (T)assetsByType.get(fileName);
		if (asset == null) throw new GdxRuntimeException("Asset '" + fileName + "' not loaded");
		return asset;
	}

	/** Removes the asset and all its dependencies if they are not used by other assets.
	 * @param fileName the file name */
	public synchronized void remove (String fileName) {
		// get the asset and its type
		Class type = assetTypes.get(fileName);
		if (type == null) throw new GdxRuntimeException("Asset '" + fileName + "' not loaded");
		Object asset = assets.get(type).get(fileName);

		// if it is disposable dispose it
		if (asset instanceof Disposable) ((Disposable)asset).dispose();

		// if it is reference counted, check if we can really get rid of it.
		if (asset instanceof ReferenceCountedAsset) {
			if (((ReferenceCountedAsset)asset).getRefCount() > 0) return;
		}

		// remove the asset from the manager.
		assetTypes.remove(fileName);
		assets.get(type).remove(fileName);

		// remove any dependencies (which might also be reference counted)
		Array<String> dependencies = assetDependencies.remove(fileName);
		if (dependencies != null) {
			for (String dependency : dependencies) {
				remove(dependency);
			}
		}
	}

	/** @param asset the asset
	 * @return whether the asset is contained in this manager */
	public synchronized <T> boolean containsAsset (T asset) {
		ObjectMap<String, Object> typedAssets = assets.get(asset.getClass());
		for (String fileName : typedAssets.keys()) {
			Object otherAsset = typedAssets.get(fileName);
			if (otherAsset == asset || asset.equals(otherAsset)) return true;
		}
		return false;
	}

	/** @param asset the asset
	 * @return whether the filename of the asset or null */
	public synchronized <T> String getAssetFileName (T asset) {
		ObjectMap<String, Object> typedAssets = assets.get(asset.getClass());
		for (String fileName : typedAssets.keys()) {
			Object otherAsset = typedAssets.get(fileName);
			if (otherAsset == asset || asset.equals(otherAsset)) return fileName;
		}
		return null;
	}

	/** @param fileName the file name of the asset
	 * @return whether the asset is loaded */
	public synchronized boolean isLoaded (String fileName) {
		return assetTypes.containsKey(fileName);
	}

	/** Adds the given asset to the loading queue of the AssetManager.
	 * @param fileName the file name (interpretation depends on {@link AssetLoader})
	 * @param type the type of the asset. */
	public synchronized <T> void preload (String fileName, Class<T> type) {
		preload(fileName, type, null);
	}

	/** Adds the given asset to the loading queue of the AssetManager.
	 * @param fileName the file name (interpretation depends on {@link AssetLoader})
	 * @param type the type of the asset.
	 * @param parameter parameters for the AssetLoader. */
	public synchronized <T> void preload (String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
		AssetLoader loader = loaders.get(type);
		if (loader == null) throw new GdxRuntimeException("No loader for type '" + type.getSimpleName() + "'");
		if (isLoaded(fileName)) throw new GdxRuntimeException("Asset '" + fileName + "' already loaded");
		for (AssetDescriptor desc : preloadQueue) {
			if (desc.fileName.equals(fileName)) {
				throw new GdxRuntimeException("Asset '" + fileName + "' already in preload queue");
			}
		}
		if (preloadQueue.size == 0) {
			loaded = 0;
			toLoad = 0;
		}
		toLoad++;
		AssetDescriptor assetDesc = new AssetDescriptor(fileName, type, parameter);
		preloadQueue.add(assetDesc);
		log.log("Added asset '" + assetDesc + "' to preload queue");
	}

	/** Updates the AssetManager, keeping it loading any assets in the preload queue.
	 * @return true if all loading is finished. */
	public synchronized boolean update () {
		try {
			if (tasks.size() == 0) {
				if (preloadQueue.size == 0) return true;
				nextTask();
			}
			return updateTask() && preloadQueue.size == 0;
		} catch (Throwable t) {
			handleTaskError(t);
			return preloadQueue.size == 0;
		}
	}

	synchronized void injectDependency (String parentAssetFilename, AssetDescriptor dependendAssetDesc) {
		// add the asset as a dependency of the parent asset
		Array<String> dependencies = assetDependencies.get(parentAssetFilename);
		if (dependencies == null) {
			dependencies = new Array<String>();
			assetDependencies.put(parentAssetFilename, dependencies);
		}
		dependencies.add(dependendAssetDesc.fileName);

		// if the asset is already loaded, increase its reference count if needed.
		if (isLoaded(dependendAssetDesc.fileName)) {
			Class type = assetTypes.get(dependendAssetDesc.fileName);
			Object asset = assets.get(type).get(dependendAssetDesc.fileName);
			if (asset instanceof ReferenceCountedAsset) ((ReferenceCountedAsset)asset).incRefCount();
		}
		// else add a new task for the asset. if the asset is already on the preloading queue
		else {
			addTask(dependendAssetDesc);
		}

		log.log("Injected dependency '" + dependendAssetDesc + "' for asset '" + parentAssetFilename + "'");
	}

	/** Removes a task from the preloadQueue and adds it to the task stack. If the asset is already loaded (which can happen if it
	 * was a dependency of a previously loaded asset) and if it is a {@link ReferenceCountedAsset} its reference count will be
	 * increased. */
	private void nextTask () {
		AssetDescriptor assetDesc = preloadQueue.removeIndex(0);

		if (isLoaded(assetDesc.fileName)) {
			Class type = assetTypes.get(assetDesc.fileName);
			Object asset = assets.get(type).get(assetDesc.fileName);
			if (asset instanceof ReferenceCountedAsset) ((ReferenceCountedAsset)asset).incRefCount();
		} else {
			addTask(assetDesc);
		}
	}

	/** Adds a {@link AssetLoadingTask} to the task stack for the given asset.
	 * @param assetDesc */
	private void addTask (AssetDescriptor assetDesc) {
		AssetLoader loader = loaders.get(assetDesc.type);
		if (loader == null) throw new GdxRuntimeException("No loader for type '" + assetDesc.type.getSimpleName() + "'");
		tasks.push(new AssetLoadingTask(this, assetDesc, loader, threadPool));
	}

	/** Updates the current task on the top of the task stack.
	 * @return true if the asset is loaded. */
	private boolean updateTask () {
		AssetLoadingTask task = tasks.peek();
		// if the task has finished loading
		if (task.update()) {
			// add the asset to the filename lookup
			assetTypes.put(task.assetDesc.fileName, task.assetDesc.type);

			// add the asset to the type lookup
			ObjectMap<String, Object> typeToAssets = assets.get(task.assetDesc.type);
			if (typeToAssets == null) {
				typeToAssets = new ObjectMap<String, Object>();
				assets.put(task.assetDesc.type, typeToAssets);
			}
			typeToAssets.put(task.assetDesc.fileName, task.getAsset());

			// increase the ref count of all dependencies (and their dependencies)
			incrementRefCountedDependencies(task.assetDesc.fileName);

			// increase the number of loaded assets and pop the task from the stack
			loaded++;
			tasks.pop();
			return true;
		} else {
			return false;
		}
	}

	private void incrementRefCountedDependencies (String parent) {
		Array<String> dependencies = assetDependencies.get(parent);
		if (dependencies == null) return;

		for (String dependency : dependencies) {
			Class type = assetTypes.get(dependency);
			if (type == null) throw new GdxRuntimeException("Asset '" + dependency + "' not loaded");
			// if we found a reference counted dependency we increase the ref count
			Object asset = assets.get(type).get(dependency);
			if (asset instanceof ReferenceCountedAsset) {
				((ReferenceCountedAsset)asset).incRefCount();
			}
			// otherwise we go deeper down the rabbit hole
			else {
				incrementRefCountedDependencies(dependency);
			}
		}
	}

	/** Handles a runtime/loading error in {@link #update()} by optionally invoking the {@link AssetErrorListener}.
	 * @param t */
	private void handleTaskError (Throwable t) {
		// pop the faulty task from the stack
		AssetLoadingTask task = tasks.pop();
		AssetDescriptor assetDesc = task.assetDesc;

		if (listener != null) {
			listener.error(assetDesc.fileName, assetDesc.type, t);
		} else {
			throw new GdxRuntimeException(t);
		}
	}

	/** Sets a new {@link AssetLoader} for the given type.
	 * @param type the type of the asset
	 * @param loader the loader */
	public synchronized <T, P> void setLoader (Class<T> type, AssetLoader<T, P> loader) {
		loaders.put(type, loader);
	}

	/** @return the number of loaded assets */
	public synchronized int getLoadedAssets () {
		return assetTypes.size;
	}

	/** @return the number of currently queued assets */
	public synchronized int getQueuedAssets () {
		return preloadQueue.size + (tasks.size());
	}

	/** @return the progress in percent of completion. */
	public synchronized float getProgress () {
		return loaded / (float)toLoad;
	}

	/** Sets an {@link AssetErrorListener} to be invoked in case loading an asset failed.
	 * @param listener the listener or null */
	public synchronized void setErrorListener (AssetErrorListener listener) {
		this.listener = listener;
	}

	/** Disposes all assets in the manager and stops all asynchronous loading. */
	public synchronized void dispose () {
		clear();
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			new GdxRuntimeException("Couldn't shutdown loading thread");
		}
	}

	/** Clears and disposes all assets and the preloading queue. */
	public synchronized void clear () {
		if (preloadQueue.size > 0 || tasks.size() != 0) {
			try {
				while (!updateTask())
					;
			} catch (Throwable t) {
				handleTaskError(t);
			}
		}

		Array<String> assets = assetTypes.keys().toArray();
		for (String asset : assets) {
			remove(asset);
		}

		this.assets.clear();
		this.assetTypes.clear();
		this.assetDependencies.clear();
		this.loaded = 0;
		this.preloadQueue.clear();
		this.tasks.clear();
	}

	/** @return the {@link Logger} used by the {@link AssetManager} */
	public Logger getLogger () {
		return log;
	}

	/** @return a string containg ref count and dependency information for all assets. */
	public synchronized String getDiagonistics () {
		StringBuffer buffer = new StringBuffer();
		for (String fileName : assetTypes.keys()) {
			buffer.append(fileName);
			buffer.append(", ");

			Class type = assetTypes.get(fileName);
			Object asset = assets.get(type).get(fileName);
			Array<String> dependencies = assetDependencies.get(fileName);

			buffer.append(type.getSimpleName());

			if (asset instanceof ReferenceCountedAsset) {
				buffer.append(", refs: ");
				buffer.append(((ReferenceCountedAsset)asset).getRefCount());
			}

			if (dependencies != null) {
				buffer.append(", deps: [");
				for (String dep : dependencies) {
					buffer.append(dep);
					buffer.append(",");
				}
				buffer.append("]");
			}
			buffer.append("\n");
		}
		return buffer.toString();
	}
}
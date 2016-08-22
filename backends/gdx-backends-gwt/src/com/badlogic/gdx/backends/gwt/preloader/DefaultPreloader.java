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

package com.badlogic.gdx.backends.gwt.preloader;

import com.badlogic.gdx.backends.gwt.preloader.AssetDownloader.AssetLoaderListener;
import com.badlogic.gdx.backends.gwt.preloader.AssetFilter.AssetType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;

public class DefaultPreloader implements Preloader {

	private final PreloadedAssetManager preloadedAssetManager = new PreloadedAssetManager();
	private final String baseUrl;
	private final String assetFileUrl;

	public DefaultPreloader(String assetFileUrl, String newBaseURL) {
		this.assetFileUrl = assetFileUrl;
		this.baseUrl = newBaseURL;

		// trigger copying of assets and creation of assets.txt
		GWT.create(PreloaderBundle.class);
	}

	@Override
	public PreloadedAssetManager getPreloadedAssetManager() {
		return preloadedAssetManager;
	}

	@Override
	public void preload (final Preloader.PreloaderCallback callback) {
		final AssetDownloader loader = new AssetDownloader();
		
		loader.loadText(baseUrl + assetFileUrl, new AssetLoaderListener<String>() {
			@Override
			public void onProgress (double amount) {
			}
			@Override
			public void onFailure () {
				callback.error(assetFileUrl);
			}
			@Override
			public void onSuccess (String result) {
				String[] lines = result.split("\n");
				Array<Asset> assets = new Array<Asset>(lines.length);
				for (String line : lines) {
					String[] tokens = line.split(":");
					if (tokens.length != 4) {
						throw new GdxRuntimeException("Invalid assets description file.");
					}
					AssetType type = AssetType.Text;
					if (tokens[0].equals("i")) type = AssetType.Image;
					if (tokens[0].equals("b")) type = AssetType.Binary;
					if (tokens[0].equals("a")) type = AssetType.Audio;
					if (tokens[0].equals("d")) type = AssetType.Directory;
					long size = Long.parseLong(tokens[2]);
					if (type == AssetType.Audio && !loader.isUseBrowserCache()) {
						size = 0;
					}
					assets.add(new Asset(tokens[1].trim(), type, size, tokens[3]));
				}
				final PreloaderState state = new PreloaderState(assets);
				for (int i = 0; i < assets.size; i++) {
					final Asset asset = assets.get(i);
					
					if (preloadedAssetManager.contains(asset.url)) {
						asset.loaded = asset.size;
						asset.succeed = true;
						continue;
					}
					
					loader.load(baseUrl + asset.url, asset.type, asset.mimeType, new AssetLoaderListener<Object>() {
						@Override
						public void onProgress (double amount) {
							asset.loaded = (long) amount;
							callback.update(state);
						}
						@Override
						public void onFailure () {
							asset.failed = true;
							callback.error(asset.url);
							callback.update(state);
						}
						@Override
						public void onSuccess (Object result) {
							switch (asset.type) {
							case Text:
								preloadedAssetManager.texts.put(asset.url, (String) result);
								break;
							case Image:
								preloadedAssetManager.images.put(asset.url, (ImageElement) result);
								break;
							case Binary:
								preloadedAssetManager.binaries.put(asset.url, (Blob) result);
								break;
							case Audio:
								preloadedAssetManager.audio.put(asset.url, null);
								break;
							case Directory:
								preloadedAssetManager.directories.put(asset.url, null);
								break;
							}
							asset.succeed = true;
							callback.update(state);
						}
					});
				}
				callback.update(state);
			}
		});
	}


}

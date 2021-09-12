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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtFileHandle;
import com.badlogic.gdx.backends.gwt.preloader.AssetDownloader.AssetLoaderListener;
import com.badlogic.gdx.backends.gwt.preloader.AssetFilter.AssetType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;

public class Preloader {

	private final AssetDownloader loader = new AssetDownloader();

	public interface PreloaderCallback {

		public void update (PreloaderState state);

		public void error (String file);

	}

	public ObjectMap<String, Void> directories = new ObjectMap<String, Void>();
	public ObjectMap<String, ImageElement> images = new ObjectMap<String, ImageElement>();
	public ObjectMap<String, Blob> audio = new ObjectMap<String, Blob>();
	public ObjectMap<String, String> texts = new ObjectMap<String, String>();
	public ObjectMap<String, Blob> binaries = new ObjectMap<String, Blob>();
	private ObjectMap<String, Asset> stillToFetchAssets = new ObjectMap<String, Asset>();
	public ObjectMap<String, String> assetNames = new ObjectMap<String, String>();

	public static class Asset {
		public Asset (String file, String url, AssetType type, long size, String mimeType) {
			this.file = file;
			this.url = url;
			this.type = type;
			this.size = size;
			this.mimeType = mimeType;
		}

		public boolean succeed;
		public boolean failed;
		public boolean downloadStarted;
		public long loaded;
		public final String file;
		public final String url;
		public final AssetType type;
		public final long size;
		public final String mimeType;
	}

	public static class PreloaderState {

		public PreloaderState (Array<Asset> assets) {
			this.assets = assets;
		}

		public long getDownloadedSize () {
			long size = 0;
			for (int i = 0; i < assets.size; i++) {
				Asset asset = assets.get(i);
				size += (asset.succeed || asset.failed) ? asset.size : Math.min(asset.size, asset.loaded);
			}
			return size;
		}

		public long getTotalSize () {
			long size = 0;
			for (int i = 0; i < assets.size; i++) {
				Asset asset = assets.get(i);
				size += asset.size;
			}
			return size;
		}

		public float getProgress () {
			long total = getTotalSize();
			return total == 0 ? 1 : (getDownloadedSize() / (float)total);
		}

		public boolean hasEnded () {
			return getDownloadedSize() == getTotalSize();
		}

		public final Array<Asset> assets;

	}

	public final String baseUrl;

	public Preloader (String newBaseURL) {

		baseUrl = newBaseURL;

		// trigger copying of assets and creation of assets.txt
		GWT.create(PreloaderBundle.class);
	}

	public void preload (final String assetFileUrl, final PreloaderCallback callback) {

		loader.loadText(baseUrl + assetFileUrl + "?etag=" + System.currentTimeMillis(), new AssetLoaderListener<String>() {
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
					if (tokens.length != 6) {
						throw new GdxRuntimeException("Invalid assets description file.");
					}

					String assetTypeCode = tokens[0];
					String assetPathOrig = tokens[1];
					String assetPathMd5 = tokens[2];
					long size = Long.parseLong(tokens[3]);
					String assetMimeType = tokens[4];
					boolean assetPreload = tokens[5].equals("1");

					AssetType type = AssetType.Text;
					if (assetTypeCode.equals("i")) type = AssetType.Image;
					if (assetTypeCode.equals("b")) type = AssetType.Binary;
					if (assetTypeCode.equals("a")) type = AssetType.Audio;
					if (assetTypeCode.equals("d")) type = AssetType.Directory;
					if (type == AssetType.Audio && !loader.isUseBrowserCache()) {
						size = 0;
					}
					Asset asset = new Asset(assetPathOrig.trim(), assetPathMd5.trim(), type, size, assetMimeType);
					assetNames.put(asset.file, asset.url);
					if (assetPreload || asset.file.startsWith("com/badlogic/"))
						assets.add(asset);
					else
						stillToFetchAssets.put(asset.file, asset);
				}
				final PreloaderState state = new PreloaderState(assets);
				for (int i = 0; i < assets.size; i++) {
					final Asset asset = assets.get(i);

					if (contains(asset.file)) {
						asset.loaded = asset.size;
						asset.succeed = true;
						continue;
					}

					asset.downloadStarted = true;
					loader.load(baseUrl + asset.url, asset.type, asset.mimeType, new AssetLoaderListener<Object>() {
						@Override
						public void onProgress (double amount) {
							asset.loaded = (long)amount;
							callback.update(state);
						}

						@Override
						public void onFailure () {
							asset.failed = true;
							callback.error(asset.file);
							callback.update(state);
						}

						@Override
						public void onSuccess (Object result) {
							putAssetInMap(result, asset);
							asset.succeed = true;
							callback.update(state);
						}
					});
				}
				callback.update(state);
			}
		});
	}

	public void preloadSingleFile (final String file) {
		if (!isNotFetchedYet(file)) return;

		final Asset asset = stillToFetchAssets.get(file);

		if (asset.downloadStarted) return;

		Gdx.app.log("Preloader", "Downloading " + baseUrl + asset.file);

		asset.downloadStarted = true;

		loader.load(baseUrl + asset.url, asset.type, asset.mimeType, new AssetLoaderListener<Object>() {
			@Override
			public void onProgress (double amount) {
				asset.loaded = (long)amount;
			}

			@Override
			public void onFailure () {
				asset.failed = true;
				stillToFetchAssets.remove(file);
			}

			@Override
			public void onSuccess (Object result) {
				putAssetInMap(result, asset);
				stillToFetchAssets.remove(file);
				asset.succeed = true;
			}
		});

	}

	protected void putAssetInMap (Object result, Asset asset) {
		switch (asset.type) {
		case Text:
			texts.put(asset.file, (String)result);
			break;
		case Image:
			images.put(asset.file, (ImageElement)result);
			break;
		case Binary:
			binaries.put(asset.file, (Blob)result);
			break;
		case Audio:
			audio.put(asset.file, (Blob)result);
			break;
		case Directory:
			directories.put(asset.file, null);
			break;
		}
	}

	public InputStream read (String file) {
		if (texts.containsKey(file)) {
			try {
				return new ByteArrayInputStream(texts.get(file).getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}
		if (images.containsKey(file)) {
			return new ByteArrayInputStream(new byte[1]); // FIXME, sensible?
		}
		if (binaries.containsKey(file)) {
			return binaries.get(file).read();
		}
		if (audio.containsKey(file)) {
			return audio.get(file).read();
		}
		return null;
	}

	public boolean contains (String file) {
		return texts.containsKey(file) || images.containsKey(file) || binaries.containsKey(file) || audio.containsKey(file)
			|| directories.containsKey(file);
	}

	public boolean isNotFetchedYet (String file) {
		return stillToFetchAssets.containsKey(file);
	}

	public boolean isText (String file) {
		return texts.containsKey(file);
	}

	public boolean isImage (String file) {
		return images.containsKey(file);
	}

	public boolean isBinary (String file) {
		return binaries.containsKey(file);
	}

	public boolean isAudio (String file) {
		return audio.containsKey(file);
	}

	public boolean isDirectory (String file) {
		return directories.containsKey(file);
	}

	private boolean isChild (String filePath, String directory) {
		return filePath.startsWith(directory + "/") && (filePath.indexOf('/', directory.length() + 1) < 0);
	}

	public FileHandle[] list (final String file) {
		return getMatchedAssetFiles(new FilePathFilter() {
			@Override
			public boolean accept (String path) {
				return isChild(path, file);
			}
		});
	}

	public FileHandle[] list (final String file, final FileFilter filter) {
		return getMatchedAssetFiles(new FilePathFilter() {
			@Override
			public boolean accept (String path) {
				return isChild(path, file) && filter.accept(new File(path));
			}
		});
	}

	public FileHandle[] list (final String file, final FilenameFilter filter) {
		return getMatchedAssetFiles(new FilePathFilter() {
			@Override
			public boolean accept (String path) {
				return isChild(path, file) && filter.accept(new File(file), path.substring(file.length() + 1));
			}
		});
	}

	public FileHandle[] list (final String file, final String suffix) {
		return getMatchedAssetFiles(new FilePathFilter() {
			@Override
			public boolean accept (String path) {
				return isChild(path, file) && path.endsWith(suffix);
			}
		});
	}

	public long length (String file) {
		if (texts.containsKey(file)) {
			try {
				return texts.get(file).getBytes("UTF-8").length;
			} catch (UnsupportedEncodingException e) {
				return texts.get(file).getBytes().length;
			}
		}
		if (images.containsKey(file)) {
			return 1; // FIXME, sensible?
		}
		if (binaries.containsKey(file)) {
			return binaries.get(file).length();
		}
		if (audio.containsKey(file)) {
			return audio.get(file).length();
		}
		return 0;
	}

	private interface FilePathFilter {
		boolean accept (String path);
	}

	private FileHandle[] getMatchedAssetFiles (FilePathFilter filter) {
		Array<FileHandle> files = new Array<FileHandle>();
		for (String file : assetNames.keys()) {
			if (filter.accept(file)) {
				files.add(new GwtFileHandle(this, file, FileType.Internal));
			}
		}

		FileHandle[] filesArray = new FileHandle[files.size];
		System.arraycopy(files.items, 0, filesArray, 0, filesArray.length);
		return filesArray;
	}
}

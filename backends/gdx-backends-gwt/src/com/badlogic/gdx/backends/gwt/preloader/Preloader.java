package com.badlogic.gdx.backends.gwt.preloader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;

public class Preloader {
	public interface PreloaderCallback {
		public void done();
		public void loaded(String file, int loaded, int total);
		public void error(String file);
	}
	
	public ObjectMap<String, ImageElement> images = new ObjectMap<String, ImageElement>();
	public ObjectMap<String, String> texts = new ObjectMap<String, String>();
	public ObjectMap<String, ByteBuffer> binaries = new ObjectMap<String, ByteBuffer>();
	
	enum AssetType {
		Image,
		Text,
		Binary
	}
	
	private class Asset {
		String url;
		AssetType type;
		
		public Asset(String url, AssetType type) {
			this.url = url;
			this.type = type;
		}
	}
	
	final String baseUrl;
	
	public Preloader() {
		baseUrl = GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "");
	}
	
	public void preload(final String assetFileUrl, final PreloaderCallback callback) {
		new TextLoader(baseUrl + assetFileUrl, new LoaderCallback<String>() {
			@Override
			public void success (String result) {
				String[] lines = result.split("\n");
				Array<Asset> assets = new Array<Asset>();
				for(String line: lines) {
					String[] tokens = line.split(":");
					if(tokens.length != 2) continue; // FIXME :p
					AssetType type = AssetType.Text;
					if(tokens[0].equals("i")) type = AssetType.Image;
					if(tokens[0].equals("b")) type = AssetType.Binary;
					assets.add(new Asset(tokens[1].trim(), type));
				}
				
				loadNextAsset(assets, 0, callback);
			}

			@Override
			public void error () {
				callback.error(assetFileUrl);
			}
		});
	}
	
	private void loadNextAsset(final Array<Asset> assets, final int next, final PreloaderCallback callback) {
		
		if(next == assets.size) {
			callback.done();
			return;
		}
		
		final Asset asset = assets.get(next);
		if(asset.type == AssetType.Text) {
			new TextLoader(baseUrl + asset.url, new LoaderCallback<String>() {
				@Override
				public void success (String result) {
					texts.put(asset.url, result);
					callback.loaded(asset.url, next + 1, assets.size);
					loadNextAsset(assets, next + 1, callback);
				}

				@Override
				public void error () {
					callback.error(asset.url);
					loadNextAsset(assets, next + 1, callback);
				}
			});
		}
		
		if(asset.type == AssetType.Image) {
			new ImageLoader(baseUrl + asset.url, new LoaderCallback<ImageElement>() {
				@Override
				public void success (ImageElement result) {
					images.put(asset.url, result);
					callback.loaded(asset.url, next + 1, assets.size);
					loadNextAsset(assets, next + 1, callback);
				}

				@Override
				public void error () {
					callback.error(asset.url);
					loadNextAsset(assets, next + 1, callback);
				}
			});
		}
	}
	
	public InputStream read(String url) {
		if(texts.containsKey(url)) {
			return new ByteArrayInputStream(texts.get(url).getBytes()); // FIXME, should use UTF-8
		}
		if(images.containsKey(url)) {
			return new ByteArrayInputStream(new byte[1]); // FIXME, sensible?
		}
		if(binaries.containsKey(url)) {
			throw new GdxRuntimeException("Not implemented");
		}
		return null;
	}
	
	public boolean contains(String url) {
		// FIXME should also check if directory exists
		return texts.containsKey(url) || images.containsKey(url) || binaries.containsKey(url);
	}
	
	public boolean isText(String url) {
		return texts.containsKey(url);
	}
	
	public boolean isImage(String url) {
		return images.containsKey(url);
	}
	
	public boolean isBinary(String url) {
		return binaries.containsKey(url);
	}

	public FileHandle[] list (String url) {
		throw new GdxRuntimeException("Not implemented"); // FIXME
	}

	public boolean isDirectory (String url) {
		throw new GdxRuntimeException("Not implemented"); // FIXME
	}

	public long length (String url) {
		if(texts.containsKey(url)) {
			return texts.get(url).getBytes().length; // FIXME should use UTF-8
		}
		if(images.containsKey(url)) {
			return 1; // FIXME, sensible?
		}
		if(binaries.containsKey(url)) {
			throw new GdxRuntimeException("Not implemented");
		}
		return 0;
	}
}
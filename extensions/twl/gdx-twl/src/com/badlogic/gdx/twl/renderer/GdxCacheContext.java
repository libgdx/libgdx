
package com.badlogic.gdx.twl.renderer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.graphics.BitmapFont;

import de.matthiasmann.twl.renderer.CacheContext;

/**
 * @author Nathan Sweet <misc@n4te.com>
 */
class GdxCacheContext implements CacheContext {
	final TwlRenderer renderer;
	private final HashMap<String, GdxTexture> textures = new HashMap();
	private final HashMap<String, BitmapFont> fonts = new HashMap();
	private boolean valid = true;

	GdxCacheContext (TwlRenderer renderer) {
		this.renderer = renderer;
	}

	// BOZO - This URL stuff sucks. Look at URLStreamHandler.

	GdxTexture loadTexture (URL url) throws IOException {
		String urlString = url.toString();
		GdxTexture texture = textures.get(urlString);
		if (texture == null) {
			if (!valid) throw new IllegalStateException("CacheContext has been destroyed.");
			String path;
			try {
				path = new File(url.toURI()).getPath();
			} catch (Exception ex) {
				path = new File(url.getPath()).getPath();
			}
			if (path.startsWith(File.separator)) path = path.substring(1);
			int index = path.indexOf('!');
			if (index != -1) path = path.substring(index + 1);
			texture = new GdxTexture(renderer, path);
			textures.put(urlString, texture);
		}
		return texture;
	}

	BitmapFont loadBitmapFont (URL url) throws IOException {
		String urlString = url.toExternalForm();
		BitmapFont bitmapFont = fonts.get(urlString);
		if (bitmapFont == null) {
			String fontFile;
			try {
				fontFile = new File(url.toURI().getPath()).toString();
			} catch (URISyntaxException ex) {
				throw new GdxRuntimeException(ex);
			}
			if (fontFile.startsWith(File.separator)) fontFile = fontFile.substring(1);
			String textureFile = fontFile.endsWith(".fnt") ? fontFile.substring(0, fontFile.length() - 3) + "png" : fontFile
				+ ".png";
			bitmapFont = new BitmapFont(Gdx.files.getFileHandle(fontFile, FileType.Internal), Gdx.files.getFileHandle(textureFile,
				FileType.Internal), true);
			fonts.put(urlString, bitmapFont);
		}
		return bitmapFont;
	}

	public boolean isValid () {
		return valid;
	}

	public void destroy () {
		try {
			for (GdxTexture texture : textures.values())
				texture.destroy();
			for (BitmapFont bitmapFont : fonts.values())
				bitmapFont.dispose();
		} finally {
			textures.clear();
			fonts.clear();
			valid = false;
		}
	}
}

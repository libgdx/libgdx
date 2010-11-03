
package com.badlogic.gdx.twl.renderer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.utils.GdxRuntimeException;

import de.matthiasmann.twl.renderer.CacheContext;

/**
 * @author Nathan Sweet <misc@n4te.com>
 */
class GdxCacheContext implements CacheContext {
	final GdxRenderer renderer;
	private final HashMap<String, GdxTexture> textures = new HashMap();
	private final HashMap<String, BitmapFont> fonts = new HashMap();
	private boolean valid = true;

	GdxCacheContext (GdxRenderer renderer) {
		this.renderer = renderer;
	}

	// BOZO - This URL stuff sucks. Look at URLStreamHandler.

	GdxTexture loadTexture (URL url) throws IOException {
		String urlString = url.toString();
		GdxTexture texture = textures.get(urlString);
		if (texture == null) {
			if (!valid) throw new IllegalStateException("CacheContext has been destroyed.");
			FileHandle textureFile = (FileHandle)url.getContent();
			texture = new GdxTexture(renderer, textureFile);
			textures.put(urlString, texture);
		}
		return texture;
	}

	BitmapFont loadBitmapFont (URL url) throws IOException {
		String urlString = url.toExternalForm();
		BitmapFont bitmapFont = fonts.get(urlString);
		if (bitmapFont == null) {
			FileHandle fontFile = (FileHandle)url.getContent();
			FileHandle textureFile = (FileHandle)new URL(url, url.getPath().replace(".fnt", ".png")).getContent();
			bitmapFont = new BitmapFont(fontFile, textureFile, true);
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

	private FileHandle urlToFileHandle (URL url) {
		String relativePath;
		try {
			relativePath = new File(url.toURI()).getPath();
		} catch (Exception ex) {
			relativePath = new File(url.getPath()).getPath();
		}
		if (relativePath.startsWith(File.separator)) relativePath = relativePath.substring(1);
		// BOZO - How to get the theme file to construct the full path?
		return null;
	}
}

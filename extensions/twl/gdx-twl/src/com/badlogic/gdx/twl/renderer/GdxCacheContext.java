/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.twl.renderer;

import java.io.IOException;
import java.net.URL;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.utils.ObjectMap;

import de.matthiasmann.twl.renderer.CacheContext;

/**
 * @author Nathan Sweet
 */
class GdxCacheContext implements CacheContext {
	final GdxRenderer renderer;
	private final ObjectMap<String, GdxTexture> textures = new ObjectMap();
	private final ObjectMap<String, BitmapFont> fonts = new ObjectMap();
	private boolean valid = true;

	GdxCacheContext (GdxRenderer renderer) {
		this.renderer = renderer;
	}

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
}

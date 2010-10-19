/*
 * Copyright (c) 2008-2010, Matthias Mann
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution. * Neither the name of Matthias Mann nor
 * the names of its contributors may be used to endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.badlogic.gdx.twl.renderer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import de.matthiasmann.twl.renderer.CacheContext;

class GdxCacheContext implements CacheContext {
	final GdxRenderer renderer;
	final HashMap<String, GdxTexture> textures;
	final HashMap<String, BitmapFont> fontCache;
	final ArrayList<GdxTexture> allTextures;
	boolean valid;

	protected GdxCacheContext (GdxRenderer renderer) {
		this.renderer = renderer;
		this.textures = new HashMap<String, GdxTexture>();
		this.fontCache = new HashMap<String, BitmapFont>();
		this.allTextures = new ArrayList<GdxTexture>();
		valid = true;
	}

	GdxTexture loadTexture (URL url) throws IOException {
		String urlString = url.toString();
		GdxTexture texture = textures.get(urlString);
		if (texture == null) {
			texture = createTexture(url);
			textures.put(urlString, texture);
		}
		return texture;
	}

	GdxTexture createTexture (URL textureUrl) throws IOException {
		if (!valid) {
			throw new IllegalStateException("CacheContext already destroyed");
		}
		File file;
		try {
			file = new File(textureUrl.toURI());
		} catch (Exception ex) {
			file = new File(textureUrl.getPath());
		}
		String path = file.getPath();
		int index = path.indexOf('!');
		if (index != -1) path = path.substring(index + 1);
		GdxTexture texture = new GdxTexture(renderer, path);
		allTextures.add(texture);
		return texture;
	}

	BitmapFont loadBitmapFont (URL url) throws IOException {
		String urlString = url.toString();
		BitmapFont bmFont = fontCache.get(urlString);
		if (bmFont == null) {
			bmFont = BitmapFont.loadFont(renderer, url);
			fontCache.put(urlString, bmFont);
		}
		return bmFont;
	}

	public boolean isValid () {
		return valid;
	}

	public void destroy () {
		try {
			for (GdxTexture t : allTextures) {
				t.destroy();
			}
			for (BitmapFont f : fontCache.values()) {
				f.destroy();
			}
		} finally {
			textures.clear();
			fontCache.clear();
			allTextures.clear();
			valid = false;
		}
	}
}

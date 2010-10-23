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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.graphics.BitmapFont;

import de.matthiasmann.twl.renderer.CacheContext;

class GdxCacheContext implements CacheContext {
	final TwlRenderer renderer;
	final HashMap<String, GdxTexture> textures = new HashMap();
	final HashMap<String, BitmapFont> fonts = new HashMap();
	boolean valid = true;

	GdxCacheContext (TwlRenderer renderer) {
		this.renderer = renderer;
	}

	// BOZO - This URL stuff sucks.

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
				FileType.Internal));
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

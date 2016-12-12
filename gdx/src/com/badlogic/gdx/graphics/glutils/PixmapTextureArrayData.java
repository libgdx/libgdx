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

package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureArrayData;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @author cypherdare */
public class PixmapTextureArrayData implements TextureArrayData {
	final Pixmap[] pixmaps;
	final Format format;
	final boolean useMipMaps;
	final boolean disposePixmaps;
	private boolean prepared;
	
	public PixmapTextureArrayData (Format format, boolean useMipMaps, int width, int height, int depth) {
		this.format = format;
		this.useMipMaps = useMipMaps;
		pixmaps = new Pixmap[depth];
		for (int i=0; i<depth; i++)
			pixmaps[i] = new Pixmap(width, height, format);
		disposePixmaps = true;
	}

	public PixmapTextureArrayData (Format format, boolean useMipMaps, boolean disposePixmaps, Pixmap... pixmaps) {
		this.pixmaps = pixmaps;
		this.format = format == null ? pixmaps[0].getFormat() : format;
		this.useMipMaps = useMipMaps;
		this.disposePixmaps = disposePixmaps;
	}

	@Override
	public void prepare () {
		int width = -1;
		int height = -1;
		for (Pixmap pixmap : pixmaps) {
			if (width == -1) {
				width = pixmap.getWidth();
				height = pixmap.getHeight();
				continue;
			}
			if (width != pixmap.getWidth() || height != pixmap.getHeight()) {
				throw new GdxRuntimeException(
					"Error whilst preparing TextureArray: TextureArray Pixmaps must have equal dimensions.");
			}
		}
		prepared = true;
	}

	@Override
	public void consumeTextureArrayData () {
		for (int i = 0; i < pixmaps.length; i++) {
			Pixmap pixmap = pixmaps[i];
			boolean disposePixmap = disposePixmaps;
			if (format != pixmap.getFormat()) {
				Pixmap temp = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), format);
				temp.setBlending(Pixmap.Blending.None);
				temp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
				if (disposePixmaps) {
					pixmap.dispose();
				}
				pixmap = temp;
				disposePixmap = true;
			}
			Gdx.gl30.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, pixmap.getWidth(), pixmap.getHeight(), 1,
				pixmap.getGLInternalFormat(), pixmap.getGLType(), pixmap.getPixels());
			if (disposePixmap) pixmap.dispose();
		}
	}

	@Override
	public int getWidth () {
		return pixmaps[0].getWidth();
	}

	@Override
	public int getHeight () {
		return pixmaps[0].getHeight();
	}

	@Override
	public int getDepth () {
		return pixmaps.length;
	}

	@Override
	public Format getFormat () {
		return format;
	}

	@Override
	public boolean useMipMaps () {
		return useMipMaps;
	}

	@Override
	public boolean isManaged () {
		return false;
	}
	
	@Override 
	public String[] getFiles (){
		return null;
	}

	@Override
	public TextureDataType getType () {
		return TextureDataType.PixmapArray;
	}

	@Override
	public boolean isPrepared () {
		return prepared;
	}

	@Override
	public Pixmap consumePixmap () {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public boolean disposePixmap () {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public void consumeCustomData (int target) {
		throw new GdxRuntimeException("This TextureData implementation does not upload data itself");
	}
}

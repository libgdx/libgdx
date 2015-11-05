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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A {@link TextureData} implementation which should be used to create gl only textures. This TextureData fits perfectly for
 * FrameBuffer */
public class GlOnlyTextureData implements TextureData {

	/** width and height */
	int width = 0;
	int height = 0;
	boolean isPrepared = false;

	/** properties of opengl texture */
	int mipLevel = 0;
	int internalFormat;
	int format;
	int type;

	/** @param w Width
	 * @param h Height
	 * @param mipLevel MipMapLevel
	 * @param internalFormat OpenGL internal format
	 * @param format Opengl format
	 * @param type Opengl type */
	public GlOnlyTextureData (int w, int h, int mipLevel, int internalFormat, int format, int type) {
		this.width = w;
		this.height = h;
		this.mipLevel = mipLevel;
		this.internalFormat = internalFormat;
		this.format = format;
		this.type = type;
	}

	@Override
	public TextureDataType getType () {
		return TextureDataType.Custom;
	}

	@Override
	public boolean isPrepared () {
		return isPrepared;
	}

	@Override
	public void prepare () {
		if (isPrepared) throw new GdxRuntimeException("Already prepared");
		isPrepared = true;
	}

	@Override
	public void consumeCustomData (int target) {
		Gdx.gl.glTexImage2D(target, mipLevel, internalFormat, width, height, 0, format, type, null);
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
	public int getWidth () {
		return width;
	}

	@Override
	public int getHeight () {
		return height;
	}

	@Override
	public Format getFormat () {
		return Format.RGBA8888;
	}

	@Override
	public boolean useMipMaps () {
		return false;
	}

	@Override
	public boolean isManaged () {
		return true;
	}
}

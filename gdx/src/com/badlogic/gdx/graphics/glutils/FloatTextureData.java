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

import java.nio.FloatBuffer;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A {@link TextureData} implementation which should be used to create float textures. */
public class FloatTextureData implements TextureData {

	int width = 0;
	int height = 0;
	boolean isPrepared = false;
	FloatBuffer buffer;

	public FloatTextureData (int w, int h) {
		this.width = w;
		this.height = h;
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
		this.buffer = BufferUtils.newFloatBuffer(width * height * 4);
		isPrepared = true;
	}

	@Override
	public void consumeCustomData (int target) {
		if (!Gdx.graphics.supportsExtension("texture_float"))
			throw new GdxRuntimeException("Extension OES_TEXTURE_FLOAT not supported!");

		// this is a const from GL 3.0, used only on desktops
		final int GL_RGBA32F = 34836;

		// GLES and WebGL defines texture format by 3rd and 8th argument,
		// so to get a float texture one needs to supply GL_RGBA and GL_FLOAT there.
		if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS
			|| Gdx.app.getType() == ApplicationType.WebGL) {
			Gdx.gl.glTexImage2D(target, 0, GL20.GL_RGBA, width, height, 0, GL20.GL_RGBA, GL20.GL_FLOAT, buffer);
		} else {
			// in desktop OpenGL the texture format is defined only by the third argument,
			// hence we need to use GL_RGBA32F there (this constant is unavailable in GLES/WebGL)
			Gdx.gl.glTexImage2D(target, 0, GL_RGBA32F, width, height, 0, GL20.GL_RGBA, GL20.GL_FLOAT, buffer);
		}
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
		return Format.RGBA8888; // it's not true, but FloatTextureData.getFormat() isn't used anywhere
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

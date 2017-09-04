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
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A {@link TextureData} implementation which should be used to create float textures. */
public class FloatTextureData implements TextureData {

	int width = 0;
	int height = 0;

	int internalFormat;
	int format;
	int type;

	boolean isGpuOnly;

	boolean isPrepared = false;
	FloatBuffer buffer;

	public FloatTextureData (int w, int h, int internalFormat, int format, int type, boolean isGpuOnly) {
		this.width = w;
		this.height = h;
		this.internalFormat = internalFormat;
		this.format = format;
		this.type = type;
		this.isGpuOnly = isGpuOnly;
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
		if (!isGpuOnly) {
			int amountOfFloats = 4;
			if (Gdx.graphics.getGLVersion().getType().equals(GLVersion.Type.OpenGL)) {
				if (internalFormat == GL30.GL_RGBA16F || internalFormat ==  GL30.GL_RGBA32F) amountOfFloats = 4;
				if (internalFormat == GL30.GL_RGB16F || internalFormat ==  GL30.GL_RGB32F) amountOfFloats = 3;
				if (internalFormat == GL30.GL_RG16F || internalFormat ==  GL30.GL_RG32F) amountOfFloats = 2;
				if (internalFormat == GL30.GL_R16F || internalFormat ==  GL30.GL_R32F) amountOfFloats = 1;
			}
			this.buffer = BufferUtils.newFloatBuffer(width * height * amountOfFloats);
		}
		isPrepared = true;
	}

	@Override
	public void consumeCustomData (int target) {
		if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS
			|| Gdx.app.getType() == ApplicationType.WebGL) {

			if (!Gdx.graphics.supportsExtension("OES_texture_float"))
				throw new GdxRuntimeException("Extension OES_texture_float not supported!");

			// GLES and WebGL defines texture format by 3rd and 8th argument,
			// so to get a float texture one needs to supply GL_RGBA and GL_FLOAT there.
			Gdx.gl.glTexImage2D(target, 0, GL20.GL_RGBA, width, height, 0, GL20.GL_RGBA, GL20.GL_FLOAT, buffer);

		} else {
			if (!Gdx.graphics.supportsExtension("GL_ARB_texture_float"))
				throw new GdxRuntimeException("Extension GL_ARB_texture_float not supported!");

			// in desktop OpenGL the texture format is defined only by the third argument,
			// hence we need to use GL_RGBA32F there (this constant is unavailable in GLES/WebGL)
			Gdx.gl.glTexImage2D(target, 0, internalFormat, width, height, 0, format, GL20.GL_FLOAT, buffer);
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

	public FloatBuffer getBuffer () {
		return buffer;
	}
}

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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

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
		return TextureDataType.Float;
	}

	@Override
	public boolean isPrepared () {
		return isPrepared;
	}

	@Override
	public void prepare () {
		if (isPrepared) throw new GdxRuntimeException("Already prepared");
		this.buffer = BufferUtils.newFloatBuffer(width*height*4);
		isPrepared = true;
	}

	@Override
	public void consumeCompressedData () {
		if (!Gdx.graphics.supportsExtension("texture_float"))
			throw new GdxRuntimeException("Extension OES_TEXTURE_FLOAT not supported!");
		Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, width, height, 0,
				GL10.GL_RGBA, GL10.GL_FLOAT, buffer);
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
		return Format.Float;
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

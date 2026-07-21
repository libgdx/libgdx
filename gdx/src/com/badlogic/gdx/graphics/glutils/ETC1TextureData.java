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
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ETC1TextureData implements TextureData {
	FileHandle file;
	ETC1Data data;
	boolean useMipMaps;
	int width = 0;
	int height = 0;
	boolean isPrepared = false;
	private Graphics graphics = Gdx.graphics;

	public ETC1TextureData (FileHandle file) {
		this(Gdx.graphics, file, false);
	}

	public ETC1TextureData (Graphics graphics, FileHandle file) {
		this(graphics, file, false);
	}

	public ETC1TextureData (FileHandle file, boolean useMipMaps) {
		this(Gdx.graphics, file, useMipMaps);
	}

	public ETC1TextureData (Graphics graphics, FileHandle file, boolean useMipMaps) {
		this.graphics = graphics;
		this.file = file;
		this.useMipMaps = useMipMaps;
	}

	public ETC1TextureData (ETC1Data encodedImage, boolean useMipMaps) {
		this(Gdx.graphics, encodedImage, useMipMaps);
	}

	public ETC1TextureData (Graphics graphics, ETC1Data encodedImage, boolean useMipMaps) {
		this.graphics = graphics;
		this.data = encodedImage;
		this.useMipMaps = useMipMaps;
	}

	@Override
	public void setGraphics (Graphics graphics) {
		this.graphics = graphics;
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
		if (file == null && data == null) throw new GdxRuntimeException("Can only load once from ETC1Data");
		if (file != null) {
			data = new ETC1Data(file);
		}
		width = data.width;
		height = data.height;
		isPrepared = true;
	}

	@Override
	public void consumeCustomData (int target) {
		if (!isPrepared) throw new GdxRuntimeException("Call prepare() before calling consumeCompressedData()");

		GL20 gl = graphics.getGL20();
		if (!graphics.supportsExtension("GL_OES_compressed_ETC1_RGB8_texture")) {
			Pixmap pixmap = ETC1.decodeImage(data, Format.RGB565);
			gl.glTexImage2D(target, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
				pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
			if (useMipMaps) MipMapGenerator.generateMipMap(graphics, target, pixmap, pixmap.getWidth(), pixmap.getHeight());
			pixmap.dispose();
			useMipMaps = false;
		} else {
			gl.glCompressedTexImage2D(target, 0, ETC1.ETC1_RGB8_OES, width, height, 0,
				data.compressedData.capacity() - data.dataOffset, data.compressedData);
			if (useMipMaps()) gl.glGenerateMipmap(GL20.GL_TEXTURE_2D);
		}
		data.dispose();
		data = null;
		isPrepared = false;
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
		return Format.RGB565;
	}

	@Override
	public boolean useMipMaps () {
		return useMipMaps;
	}

	@Override
	public boolean isManaged () {
		return true;
	}
}

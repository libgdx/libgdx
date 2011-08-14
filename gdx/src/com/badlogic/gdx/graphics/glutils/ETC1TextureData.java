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

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ETC1TextureData implements TextureData {
	FileHandle file;
	boolean useMipMaps;
	int width = 0;
	int height = 0;

	public ETC1TextureData (FileHandle file) {
		this(file, false);
	}

	public ETC1TextureData (FileHandle file, boolean useMipMaps) {
		this.file = file;
		this.useMipMaps = useMipMaps;
	}

	@Override
	public TextureDataType getType () {
		return TextureDataType.Compressed;
	}

	@Override
	public Pixmap getPixmap () {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public boolean disposePixmap () {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public void uploadCompressedData () {
		ETC1Data data = new ETC1Data(file);
		width = data.width;
		height = data.height;

		if (Gdx.app.getType() == ApplicationType.Desktop || Gdx.graphics.isGL20Available() == false) {
			Pixmap pixmap = ETC1.decodeImage(data, Format.RGB565);
			Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
				pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
			if (useMipMaps) MipMapGenerator.generateMipMap(pixmap, pixmap.getWidth(), pixmap.getHeight(), false);
			pixmap.dispose();
			useMipMaps = false;
		} else {
			Gdx.gl.glCompressedTexImage2D(GL10.GL_TEXTURE_2D, 0, ETC1.ETC1_RGB8_OES, width, height, 0,
				data.compressedData.capacity() - data.dataOffset, data.compressedData);
			if (useMipMaps()) Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_2D);
		}
		data.dispose();
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
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

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.TextureArrayLoader.TextureArrayParameter;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.PixmapTextureArrayData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.HashMap;
import java.util.Map;

/** A TextureArray is a Texture containing an array of 2D images that all have the same dimensions. It is supported only in a GL30
 * environment.
 * @author Tomski */
public class TextureArray extends Texture {

	private TextureArrayData arrayData;
	private int drawLayer;

	public TextureArray (String... internalPaths) {
		this(getInternalHandles(internalPaths));
	}

	public TextureArray (FileHandle... files) {
		this(false, files);
	}

	public TextureArray (boolean useMipMaps, FileHandle... files) {
		this(useMipMaps, Pixmap.Format.RGBA8888, files);
	}

	public TextureArray (boolean useMipMaps, Pixmap.Format format, FileHandle... files) {
		this(TextureArrayData.Factory.loadFromFiles(format, useMipMaps, files));
	}

	public TextureArray (Pixmap... pixmaps) {
		this(new PixmapTextureArrayData(null, false, false, pixmaps));
	}

	public TextureArray (boolean useMipMaps, Pixmap... pixmaps) {
		this(new PixmapTextureArrayData(null, useMipMaps, false, pixmaps));
	}

	public TextureArray (Format format, boolean useMipMaps, Pixmap... pixmaps) {
		this(new PixmapTextureArrayData(format, useMipMaps, false, pixmaps));
	}

	public TextureArray (Format format, boolean useMipMaps, int width, int height, int depth) {
		this(new PixmapTextureArrayData(format, useMipMaps, width, height, depth));
	}

	public TextureArray (TextureArrayData data) {
		super(GL30.GL_TEXTURE_2D_ARRAY, Gdx.gl.glGenTexture(), data);
	}

	private static FileHandle[] getInternalHandles (String... internalPaths) {
		FileHandle[] handles = new FileHandle[internalPaths.length];
		for (int i = 0; i < internalPaths.length; i++) {
			handles[i] = Gdx.files.internal(internalPaths[i]);
		}
		return handles;
	}

	@Override
	public void load (TextureData data) {
		if (!(data instanceof TextureArrayData)) throw new GdxRuntimeException("TextureArray only supports TextureArrayData");
		load((TextureArrayData)data);
	}

	public void load (TextureArrayData data) {
		if (this.data != null && data.isManaged() != this.data.isManaged())
			throw new GdxRuntimeException("New data must have the same managed status as the old data");
		this.data = data;
		arrayData = (TextureArrayData)data;

		bind();
		int glFormat = Pixmap.Format.toGlFormat(data.getFormat());
		Gdx.gl30.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, glFormat, data.getWidth(), data.getHeight(), data.getDepth(), 0,
			glFormat, Pixmap.Format.toGlType(data.getFormat()), null);

		if (!data.isPrepared()) data.prepare();

		data.consumeTextureArrayData();

		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
		Gdx.gl.glBindTexture(glTarget, 0);
	}

	@Override
	public int getDepth () {
		return arrayData.getDepth();
	}

	/** Sets which layer is drawn to with {@link #draw(Pixmap, int, int)}. The layer must be less than the {@link #getDepth()
	 * depth}. */
	public void setDrawLayer (int drawLayer) {
		this.drawLayer = drawLayer;
	}

	@Override
	public void draw (Pixmap pixmap, int x, int y) {
		if (data.isManaged()) throw new GdxRuntimeException("can't draw to a managed texture");

		bind();
		Gdx.gl30.glTexSubImage3D(glTarget, 0, x, y, drawLayer, pixmap.getWidth(), pixmap.getHeight(), 1, pixmap.getGLFormat(),
			pixmap.getGLType(), pixmap.getPixels());
	}
	
	@Override
	protected TextureParameter getParametersForAssetManagerReload(){
		TextureArrayParameter params = new TextureArrayParameter();
		params.fileNames = arrayData.getFiles();
		return params;
	}

}

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

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.HashMap;
import java.util.Map;

/** Open GLES wrapper for TextureArray
 * @author Tomski */
public class TextureArray extends GLTexture {

	final static Map<Graphics, Array<TextureArray>> managedTextureArrays = new HashMap<Graphics, Array<TextureArray>>();

	private TextureArrayData data;

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

	public TextureArray (TextureArrayData data) {
		this(Gdx.graphics, data);
	}

	public TextureArray (Graphics graphics, TextureArrayData data) {
		super(graphics, GL30.GL_TEXTURE_2D_ARRAY);

		if (graphics.getGL30() == null) {
			throw new GdxRuntimeException("TextureArray requires a device running with GLES 3.0 compatibilty");
		}

		load(data);

		if (data.isManaged()) addManagedTexture(graphics, this);
	}

	private static FileHandle[] getInternalHandles (String... internalPaths) {
		FileHandle[] handles = new FileHandle[internalPaths.length];
		for (int i = 0; i < internalPaths.length; i++) {
			handles[i] = Gdx.files.internal(internalPaths[i]);
		}
		return handles;
	}

	private void load (TextureArrayData data) {
		if (this.data != null && data.isManaged() != this.data.isManaged())
			throw new GdxRuntimeException("New data must have the same managed status as the old data");
		this.data = data;

		bind();
		gl30().glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, data.getInternalFormat(), data.getWidth(), data.getHeight(),
			data.getDepth(), 0, data.getInternalFormat(), data.getGLType(), null);

		if (!data.isPrepared()) data.prepare();

		data.consumeTextureArrayData();

		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
		gl().glBindTexture(glTarget, 0);
	}

	@Override
	public int getWidth () {
		return data.getWidth();
	}

	@Override
	public int getHeight () {
		return data.getHeight();
	}

	@Override
	public int getDepth () {
		return data.getDepth();
	}

	@Override
	public boolean isManaged () {
		return data.isManaged();
	}

	@Override
	protected void reload () {
		if (!isManaged()) throw new GdxRuntimeException("Tried to reload an unmanaged TextureArray");
		glHandle = gl().glGenTexture();
		load(data);
	}

	private static void addManagedTexture (Graphics graphics, TextureArray texture) {
		Array<TextureArray> managedTextureArray = managedTextureArrays.get(graphics);
		if (managedTextureArray == null) managedTextureArray = new Array<TextureArray>();
		managedTextureArray.add(texture);
		managedTextureArrays.put(graphics, managedTextureArray);
	}

	/** Clears all managed TextureArrays. This is an internal method. Do not use it! */
	public static void clearAllTextureArrays (Graphics graphics) {
		managedTextureArrays.remove(graphics);
	}

	/** Invalidate all managed TextureArrays. This is an internal method. Do not use it! */
	public static void invalidateAllTextureArrays (Graphics graphics) {
		Array<TextureArray> managedTextureArray = managedTextureArrays.get(graphics);
		if (managedTextureArray == null) return;

		for (int i = 0; i < managedTextureArray.size; i++) {
			TextureArray textureArray = managedTextureArray.get(i);
			textureArray.reload();
		}
	}

	public static String getManagedStatus () {
		StringBuilder builder = new StringBuilder();
		builder.append("Managed TextureArrays/graphics: { ");
		for (Graphics graphics : managedTextureArrays.keySet()) {
			builder.append(managedTextureArrays.get(graphics).size);
			builder.append(" ");
		}
		builder.append("}");
		return builder.toString();
	}

	/** @return the number of managed TextureArrays currently loaded */
	public static int getNumManagedTextureArrays () {
		return managedTextureArrays.get(Gdx.graphics).size;
	}

}

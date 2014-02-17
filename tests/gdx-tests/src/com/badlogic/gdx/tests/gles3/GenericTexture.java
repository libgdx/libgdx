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

package com.badlogic.gdx.tests.gles3;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.tests.gles3.PixelFormatES3.GLInternalFormat;
import com.badlogic.gdx.tests.gles3.TextureFormatES3.TextureParameters;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** This class utilizes Libgdx functionality of loading textures, and adds the ability for the texture to be constructed according
 * to a specific pixel format as described by the ES 3.0 spec. 3d texture is supported in theory, but this is not yet tested.
 * <p>
 * The implementation is not very clean, as it uses both Libgdx's Pixmap.Format and my specific TextureFormatES3. I would prefer
 * to have the option to have a file be loaded into a specific pixel format (if supported), but this would require significant
 * nontrivial refactoring on the texture loading implementation of Libgdx.
 * @author Mattijs Driel */
public class GenericTexture extends GLTexture {

	public final FileHandle sourceFile;
	public final Format sourceFormat;

	public final boolean is2d;

	private TextureFormatES3 localFormat;

	public GenericTexture (FileHandle file) {
		this(file, null);
	}

	public GenericTexture (FileHandle file, Format format) {
		super(GL20.GL_TEXTURE_2D);
		this.sourceFile = file;
		this.sourceFormat = format;
		this.localFormat = new TextureFormatES3();

		setData(file, format);
		this.is2d = true;
	}

	public GenericTexture (TextureFormatES3 format) {
		super(format.glTarget);
		this.sourceFile = null;
		this.sourceFormat = null;
		this.localFormat = format.copy();

		if (format.width <= 0)
			throw new GdxRuntimeException(
				"Invalid texture format dimensions. At least \"width\" must be a positive non-zero integer.");

		if (format.height <= 0) // 1D
			localFormat.height = 1;

		if (format.depth > 0) {
			is2d = false;
		} else {
			localFormat.depth = 0;
			is2d = true;
		}

		setData();
	}

	public void setTexParameters (TextureParameters params) {
		bind();
		Gdx.gl20.glTexParameteri(glTarget, GL20.GL_TEXTURE_MIN_FILTER, params.minFilter);
		Gdx.gl20.glTexParameteri(glTarget, GL20.GL_TEXTURE_MAG_FILTER, params.magFilter);
		Gdx.gl20.glTexParameteri(glTarget, GL20.GL_TEXTURE_WRAP_S, params.wrapS);
		Gdx.gl20.glTexParameteri(glTarget, GL20.GL_TEXTURE_WRAP_T, params.wrapT);
		localFormat.params.copyFrom(params);
	}

	@Override
	public void setFilter (TextureFilter minFilter, TextureFilter magFilter) {
		System.err.println("method \"GLTexture.setFilter\" is ignored. Use \"GenericTexture.setTexParameters\" instead.");
	}

	@Override
	public void setWrap (TextureWrap u, TextureWrap v) {
		System.err.println("method \"GLTexture.setFilter\" is ignored. Use \"GenericTexture.setTexParameters\" instead.");
	}

	private void setData () {
		setTexParameters(localFormat.params);

		// texture is bound by setTexParameters, no need to rebind.
		if (is2d)
			Gdx.gl20.glTexImage2D(glTarget, 0, localFormat.pixelFormat.internalFormat, localFormat.width, localFormat.height, 0,
				localFormat.pixelFormat.format, localFormat.pixelFormat.type, null);
		else
			Gdx.gl30.glTexImage3D(glTarget, 0, localFormat.pixelFormat.internalFormat, localFormat.width, localFormat.height,
				localFormat.depth, 0, localFormat.pixelFormat.format, localFormat.pixelFormat.type, null);
	}

	private void setData (FileHandle file, Format format) {
		setTexParameters(localFormat.params);

		// texture is bound by setTexParameters, no need to rebind.
		TextureData data = createTextureData(file, format, false);
		localFormat.width = data.getWidth();
		localFormat.height = data.getHeight();
		uploadImageData(glTarget, data);

		getLoadedFormatAndType(data.getFormat());
	}

	/** needs to be replaced with something more dynamic, based on texture metadata or something */
	private void getLoadedFormatAndType (Format format) {
		switch (format) {
		case Alpha:
			localFormat.pixelFormat.set(GLInternalFormat.GL_ALPHA);
			break;
		case LuminanceAlpha:
			localFormat.pixelFormat.set(GLInternalFormat.GL_LUMINANCE_ALPHA);
			break;
		case RGB565:
			localFormat.pixelFormat.set(GLInternalFormat.GL_RGB565);
			break;
		case RGB888:
			localFormat.pixelFormat.set(GLInternalFormat.GL_RGB);
			break;
		case RGBA4444:
			localFormat.pixelFormat.set(GLInternalFormat.GL_RGBA4);
			break;
		case RGBA8888:
			localFormat.pixelFormat.set(GLInternalFormat.GL_RGBA8);
			break;
		default:
			throw new GdxRuntimeException("unknown format: " + format);
		}
	}

	public void setSubData (ByteBuffer pixels, int level, int x, int y, int width, int height) {
		if (is2d) throw new GdxRuntimeException("Can't set data of a 3d texture with 2d data. ");
		bind();
		Gdx.gl20.glTexSubImage2D(glTarget, level, x, y, width, height, localFormat.pixelFormat.format,
			localFormat.pixelFormat.type, pixels);
	}

	public void setSubData (ByteBuffer pixels, int level, int x, int y, int z, int width, int height, int depth) {
		if (!is2d) throw new GdxRuntimeException("Can't set data of a 2d texture with 3d data. ");
		bind();
		Gdx.gl30.glTexSubImage3D(glTarget, level, x, y, z, width, height, depth, localFormat.pixelFormat.format,
			localFormat.pixelFormat.type, pixels);
	}

	@Override
	protected void reload () {
		createGLHandle();
		if (sourceFile == null)
			setData();
		else
			setData(sourceFile, sourceFormat);
	}

	public void setFBOBinding (int attachment) {
		if (!is2d) throw new GdxRuntimeException("Can not bind a 3d texture to a framebuffer");
		Gdx.gl30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, glTarget, glHandle, 0);
	}

	@Override
	public int getWidth () {
		return localFormat.width;
	}

	@Override
	public int getHeight () {
		return localFormat.height;
	}

	@Override
	public int getDepth () {
		return localFormat.depth;
	}

	@Override
	public boolean isManaged () {
		return false;
	}

}

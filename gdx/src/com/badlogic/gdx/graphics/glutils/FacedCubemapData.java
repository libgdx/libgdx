
package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.CubemapData;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A FacedCubemapData holds a cubemap data definition based on a {@link TextureData} per face.
 * 
 * @author Vincent Nousquet */
public class FacedCubemapData implements CubemapData {

	protected final TextureData[] data = new TextureData[6];

	/** Construct an empty Cubemap. Use the load(...) methods to set the texture of each side. Every side of the cubemap must be set
	 * before it can be used. */
	public FacedCubemapData () {
		this((TextureData)null, (TextureData)null, (TextureData)null, (TextureData)null, (TextureData)null, (TextureData)null);
	}

	/** Construct a Cubemap with the specified texture files for the sides, optionally generating mipmaps. */
	public FacedCubemapData (FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY,
		FileHandle positiveZ, FileHandle negativeZ) {
		this(TextureData.Factory.loadFromFile(positiveX, false), TextureData.Factory.loadFromFile(negativeX,
			false), TextureData.Factory.loadFromFile(positiveY, false), TextureData.Factory.loadFromFile(
			negativeY, false), TextureData.Factory.loadFromFile(positiveZ, false), TextureData.Factory
			.loadFromFile(negativeZ, false));
	}

	/** Construct a Cubemap with the specified texture files for the sides, optionally generating mipmaps. */
	public FacedCubemapData (FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY,
		FileHandle positiveZ, FileHandle negativeZ, boolean useMipMaps) {
		this(TextureData.Factory.loadFromFile(positiveX, useMipMaps), TextureData.Factory.loadFromFile(
			negativeX, useMipMaps), TextureData.Factory.loadFromFile(positiveY, useMipMaps), TextureData.Factory
			.loadFromFile(negativeY, useMipMaps), TextureData.Factory.loadFromFile(positiveZ, useMipMaps),
			TextureData.Factory.loadFromFile(negativeZ, useMipMaps));
	}

	/** Construct a Cubemap with the specified {@link Pixmap}s for the sides, does not generate mipmaps. */
	public FacedCubemapData (Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ,
		Pixmap negativeZ) {
		this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
	}

	/** Construct a Cubemap with the specified {@link Pixmap}s for the sides, optionally generating mipmaps. */
	public FacedCubemapData (Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ,
		Pixmap negativeZ, boolean useMipMaps) {
		this(positiveX == null ? null : new PixmapTextureData(positiveX, null, useMipMaps, false), negativeX == null ? null
			: new PixmapTextureData(negativeX, null, useMipMaps, false), positiveY == null ? null : new PixmapTextureData(positiveY,
			null, useMipMaps, false), negativeY == null ? null : new PixmapTextureData(negativeY, null, useMipMaps, false),
			positiveZ == null ? null : new PixmapTextureData(positiveZ, null, useMipMaps, false), negativeZ == null ? null
				: new PixmapTextureData(negativeZ, null, useMipMaps, false));
	}

	/** Construct a Cubemap with {@link Pixmap}s for each side of the specified size. */
	public FacedCubemapData (int width, int height, int depth, Format format) {
		this(new PixmapTextureData(new Pixmap(depth, height, format), null, false, true), new PixmapTextureData(new Pixmap(depth,
			height, format), null, false, true), new PixmapTextureData(new Pixmap(width, depth, format), null, false, true),
			new PixmapTextureData(new Pixmap(width, depth, format), null, false, true), new PixmapTextureData(new Pixmap(width,
				height, format), null, false, true), new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
	}

	/** Construct a Cubemap with the specified {@link TextureData}'s for the sides */
	public FacedCubemapData (TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY,
		TextureData positiveZ, TextureData negativeZ) {
		data[0] = positiveX;
		data[1] = negativeX;
		data[2] = positiveY;
		data[3] = negativeY;
		data[4] = positiveZ;
		data[5] = negativeZ;
	}

	@Override
	public boolean isManaged () {
		for (TextureData data : this.data)
			if (!data.isManaged()) return false;
		return true;
	}

	/** Loads the texture specified using the {@link FileHandle} and sets it to specified side, overwriting any previous data set to
	 * that side. Note that you need to reload through {@link Cubemap#load(CubemapData)} any cubemap using this data for the change
	 * to be taken in account.
	 * @param side The {@link CubemapSide}
	 * @param file The texture {@link FileHandle} */
	public void load (CubemapSide side, FileHandle file) {
		data[side.index] = TextureData.Factory.loadFromFile(file, false);
	}

	/** Sets the specified side of this cubemap to the specified {@link Pixmap}, overwriting any previous data set to that side.
	 * Note that you need to reload through {@link Cubemap#load(CubemapData)} any cubemap using this data for the change to be
	 * taken in account.
	 * @param side The {@link CubemapSide}
	 * @param pixmap The {@link Pixmap} */
	public void load (CubemapSide side, Pixmap pixmap) {
		data[side.index] = pixmap == null ? null : new PixmapTextureData(pixmap, null, false, false);
	}

	/** @return True if all sides of this cubemap are set, false otherwise. */
	public boolean isComplete () {
		for (int i = 0; i < data.length; i++)
			if (data[i] == null) return false;
		return true;
	}

	/** @return The {@link TextureData} for the specified side, can be null if the cubemap is incomplete. */
	public TextureData getTextureData (CubemapSide side) {
		return data[side.index];
	}

	@Override
	public int getWidth () {
		int tmp, width = 0;
		if (data[CubemapSide.PositiveZ.index] != null && (tmp = data[CubemapSide.PositiveZ.index].getWidth()) > width) width = tmp;
		if (data[CubemapSide.NegativeZ.index] != null && (tmp = data[CubemapSide.NegativeZ.index].getWidth()) > width) width = tmp;
		if (data[CubemapSide.PositiveY.index] != null && (tmp = data[CubemapSide.PositiveY.index].getWidth()) > width) width = tmp;
		if (data[CubemapSide.NegativeY.index] != null && (tmp = data[CubemapSide.NegativeY.index].getWidth()) > width) width = tmp;
		return width;
	}

	@Override
	public int getHeight () {
		int tmp, height = 0;
		if (data[CubemapSide.PositiveZ.index] != null && (tmp = data[CubemapSide.PositiveZ.index].getHeight()) > height)
			height = tmp;
		if (data[CubemapSide.NegativeZ.index] != null && (tmp = data[CubemapSide.NegativeZ.index].getHeight()) > height)
			height = tmp;
		if (data[CubemapSide.PositiveX.index] != null && (tmp = data[CubemapSide.PositiveX.index].getHeight()) > height)
			height = tmp;
		if (data[CubemapSide.NegativeX.index] != null && (tmp = data[CubemapSide.NegativeX.index].getHeight()) > height)
			height = tmp;
		return height;
	}

	@Override
	public boolean isPrepared () {
		return false;
	}

	@Override
	public void prepare () {
		if (!isComplete()) throw new GdxRuntimeException("You need to complete your cubemap data before using it");
		for (int i = 0; i < data.length; i++)
			if (!data[i].isPrepared()) data[i].prepare();
	}

	@Override
	public void consumeCubemapData () {
		for (int i = 0; i < data.length; i++) {
			if (data[i].getType() == TextureData.TextureDataType.Custom) {
				data[i].consumeCustomData(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i);
			} else {
				Pixmap pixmap = data[i].consumePixmap();
				boolean disposePixmap = data[i].disposePixmap();
				if (data[i].getFormat() != pixmap.getFormat()) {
					Pixmap tmp = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), data[i].getFormat());
					Blending blend = Pixmap.getBlending();
					Pixmap.setBlending(Blending.None);
					tmp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
					Pixmap.setBlending(blend);
					if (data[i].disposePixmap()) pixmap.dispose();
					pixmap = tmp;
					disposePixmap = true;
				}
				Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
				Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(),
					pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
			}
		}
	}

}

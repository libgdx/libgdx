package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.ETC1TextureData;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.glutils.MipMapGenerator;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Wraps a standard OpenGL ES Cubemap. Must be disposed when it is no longer used.
 * @author Xoppa */
public class Cubemap extends GLTexture {
	/** Enum to identify each side of a Cubemap */
	public enum CubemapSide {
		/** The positive X and first side of the cubemap */ 
		PositiveX(0, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X),
		/** The negative X and second side of the cubemap */
		NegativeX(1, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X),
		/** The positive Y and third side of the cubemap */
		PositiveY(2, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y),
		/** The negative Y and fourth side of the cubemap */
		NegativeY(3, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y),
		/** The positive Z and fifth side of the cubemap */
		PositiveZ(4, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z),
		/** The negative Z and sixth side of the cubemap */
		NegativeZ(5, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);

		/** The zero based index of the side in the cubemap */
		public final int index;
		/** The OpenGL target (used for glTexImage2D) of the side. */
		public final int glEnum;

		CubemapSide (int index, int glEnum) {
			this.index = index;
			this.glEnum = glEnum;
		}

		/** @return The OpenGL target (used for glTexImage2D) of the side. */
		public int getGLEnum () {
			return glEnum;
		}
	}
	
	protected final TextureData[] data = new TextureData[6];
	
	/** Construct an empty Cubemap. Use the load(...) methods to set the texture of each side. Every side of the cubemap must be
	 * set before it can be used. */
	public Cubemap () {
		this((TextureData)null, (TextureData)null, (TextureData)null, (TextureData)null, (TextureData)null, (TextureData)null);
	}
	
	/** Construct a Cubemap with the specified texture files for the sides, does not generate mipmaps. */
	public Cubemap (FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ) {
		this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
	}

	/** Construct a Cubemap with the specified texture files for the sides, optionally generating mipmaps. */
	public Cubemap (FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ, boolean useMipMaps) {
		this(createTextureData(positiveX, useMipMaps), 
			createTextureData(negativeX, useMipMaps),
			createTextureData(positiveY, useMipMaps),
			createTextureData(negativeY, useMipMaps),
			createTextureData(positiveZ, useMipMaps),
			createTextureData(negativeZ, useMipMaps));
	}
	
	/** Construct a Cubemap with the specified {@link Pixmap}s for the sides, does not generate mipmaps. */
	public Cubemap (Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ) {
		this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
	}
	
	/** Construct a Cubemap with the specified {@link Pixmap}s for the sides, optionally generating mipmaps. */
	public Cubemap (Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ, boolean useMipMaps) {
		this(positiveX == null ? null : new PixmapTextureData(positiveX, null, useMipMaps, false),
			negativeX == null ? null : new PixmapTextureData(negativeX, null, useMipMaps, false),
			positiveY == null ? null : new PixmapTextureData(positiveY, null, useMipMaps, false),
			negativeY == null ? null : new PixmapTextureData(negativeY, null, useMipMaps, false),
			positiveZ == null ? null : new PixmapTextureData(positiveZ, null, useMipMaps, false),
			negativeZ == null? null : new PixmapTextureData(negativeZ, null, useMipMaps, false));
	}
	
	/** Construct a Cubemap with {@link Pixmap}s for each side of the specified size. */
	public Cubemap (int width, int height, int depth, Format format) {
		this(new PixmapTextureData(new Pixmap(depth, height, format), null, false, true),
			new PixmapTextureData(new Pixmap(depth, height, format), null, false, true),
			new PixmapTextureData(new Pixmap(width, depth, format), null, false, true),
			new PixmapTextureData(new Pixmap(width, depth, format), null, false, true),
			new PixmapTextureData(new Pixmap(width, height, format), null, false, true),
			new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
	}

	/** Construct a Cubemap with the specified {@link TextureData}'s for the sides */
	public Cubemap (TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY, TextureData positiveZ, TextureData negativeZ) {
		super(GL20.GL_TEXTURE_CUBE_MAP);
		minFilter = TextureFilter.Nearest;
		magFilter = TextureFilter.Nearest;
		uWrap = TextureWrap.ClampToEdge;
		vWrap = TextureWrap.ClampToEdge;
		load(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ);
	}
	
	/** Sets the sides of this cubemap to the specified {@link TextureData}. */
	public void load (TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY, TextureData positiveZ, TextureData negativeZ) {
		bind();
		unsafeSetFilter(minFilter, magFilter, true);
		unsafeSetWrap(uWrap, vWrap, true);
		unsafeLoad(CubemapSide.PositiveX, positiveX);
		unsafeLoad(CubemapSide.NegativeX, negativeX);
		unsafeLoad(CubemapSide.PositiveY, positiveY);
		unsafeLoad(CubemapSide.NegativeY, negativeY);
		unsafeLoad(CubemapSide.PositiveZ, positiveZ);
		unsafeLoad(CubemapSide.NegativeZ, negativeZ);
		Gdx.gl.glBindTexture(glTarget, 0);
	}
	
	@Override
	public boolean isManaged () {
		for (TextureData data : this.data)
			if (!data.isManaged())
				return false;
		return true;
	}
	
	@Override
	protected void reload () {
		if (!isManaged()) throw new GdxRuntimeException("Tried to reload an unmanaged Cubemap");
		glHandle = createGLHandle();
		load(data[CubemapSide.PositiveX.index],
			data[CubemapSide.NegativeX.index],
			data[CubemapSide.PositiveY.index],
			data[CubemapSide.NegativeY.index],
			data[CubemapSide.PositiveZ.index],
			data[CubemapSide.NegativeZ.index]);
	}
	
	/** Loads the texture specified using the {@link FileHandle} and sets it to specified side,
	 * overwriting any previous data set to that side. Does not generate mipmaps.
	 * This method binds the Cubemap to the active unit! 
	 * @param side The {@link CubemapSide}
	 * @param file The texture {@link FileHandle} */
	public void load (CubemapSide side, FileHandle file) {
		load(side, file, false);
	}
	
	/** Loads the texture specified using the {@link FileHandle} and sets it to specified side,
	 * overwriting any previous data set to that side. 
	 * This method binds the Cubemap to the active unit! 
	 * @param side The {@link CubemapSide}
	 * @param file The texture {@link FileHandle}
	 * @param useMipMaps True to generate mipmaps. */
	public void load (CubemapSide side, FileHandle file, boolean useMipMaps) {
		load(side, createTextureData(file, useMipMaps));
	}
	
	/** Sets the specified side of this cubemap to the specified {@link Pixmap}, overwriting any previous
	 * data set to that side. Does not generate mipmaps.
	 * This method binds the Cubemap to the active unit! 
	 * @param side The {@link CubemapSide}
	 * @param pixmap The {@link Pixmap} */
	public void load (CubemapSide side, Pixmap pixmap) {
		load(side, pixmap == null ? null : new PixmapTextureData(pixmap, null, false, false));
	}

	/** Sets the specified side of this cubemap to the specified {@link Pixmap}, overwriting any previous
	 * data set to that side. 
	 * This method binds the Cubemap to the active unit! 
	 * @param side The {@link CubemapSide}
	 * @param pixmap The {@link Pixmap}
	 * @param useMipMaps True to generate mipmaps. */
	public void load (CubemapSide side, Pixmap pixmap, boolean useMipMaps) {
		load(side, pixmap == null ? null : new PixmapTextureData(pixmap, null, useMipMaps, false));
	}
	
	/** Sets the specified side of this cubemap to the specified {@link TextureData}, overwriting any previous
	 * data set to that side. 
	 * This method binds the Cubemap to the active unit! 
	 * @param side The {@link CubemapSide} 
	 * @param data The {@link TextureData} */
	public void load (CubemapSide side, TextureData data) {
		bind();
		unsafeLoad(side, data);
		Gdx.gl.glBindTexture(glTarget, 0);
	}
	
	/** Sets the specified side of this cubemap to the specified {@link TextureData}, overwriting any previous
	 * data set to that side. 
	 * Assumes that the cubemap is bound and active! See also: {@link #load(CubemapSide, TextureData)} 
	 * @param side The {@link CubemapSide} 
	 * @param data The {@link TextureData} */
	protected void unsafeLoad (CubemapSide side, TextureData data) {
		final int idx = side.index;
		if (this.data[idx] != null && data != null && data.isManaged() != this.data[idx].isManaged())
			throw new GdxRuntimeException("New data must have the same managed status as the old data");

		uploadImageData(side.glEnum, data);
		
		this.data[idx] = data;
	}
	
	/** @return True if all sides of this cubemap are set, false otherwise. */
	public boolean isComplete() {
		for (int i = 0; i < data.length; i++)
			if (data[i] == null)
				return false;
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
		if (data[CubemapSide.PositiveZ.index] != null && (tmp = data[CubemapSide.PositiveZ.index].getHeight()) > height) height = tmp;
		if (data[CubemapSide.NegativeZ.index] != null && (tmp = data[CubemapSide.NegativeZ.index].getHeight()) > height) height = tmp;
		if (data[CubemapSide.PositiveX.index] != null && (tmp = data[CubemapSide.PositiveX.index].getHeight()) > height) height = tmp;
		if (data[CubemapSide.NegativeX.index] != null && (tmp = data[CubemapSide.NegativeX.index].getHeight()) > height) height = tmp;
		return height;
	}
	
	@Override
	public int getDepth () {
		int tmp, depth = 0;
		if (data[CubemapSide.PositiveX.index] != null && (tmp = data[CubemapSide.PositiveX.index].getWidth()) > depth) depth = tmp;
		if (data[CubemapSide.NegativeX.index] != null && (tmp = data[CubemapSide.NegativeX.index].getWidth()) > depth) depth = tmp;
		if (data[CubemapSide.PositiveY.index] != null && (tmp = data[CubemapSide.PositiveY.index].getHeight()) > depth) depth = tmp;
		if (data[CubemapSide.NegativeY.index] != null && (tmp = data[CubemapSide.NegativeY.index].getHeight()) > depth) depth = tmp;
		return depth;
	}
}

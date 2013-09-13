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

public class Cubemap extends GLTexture {
	public enum CubemapSide {
		PositiveX(0, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X),
		NegativeX(1, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X),
		PositiveY(2, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y),
		NegativeY(3, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y),
		PositiveZ(4, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z),
		NegativeZ(5, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);

		public final int index;
		public final int glEnum;

		CubemapSide (int index, int glEnum) {
			this.index = index;
			this.glEnum = glEnum;
		}

		public int getGLEnum () {
			return glEnum;
		}
	}
	
	protected final TextureData[] data = new TextureData[6];
	
	public Cubemap (FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ) {
		this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
	}

	public Cubemap (FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ, boolean useMipMaps) {
		this(createTextureData(positiveX, useMipMaps), 
			createTextureData(negativeX, useMipMaps),
			createTextureData(positiveY, useMipMaps),
			createTextureData(negativeY, useMipMaps),
			createTextureData(positiveZ, useMipMaps),
			createTextureData(negativeZ, useMipMaps));
	}
	
	public Cubemap (Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ) {
		this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
	}
	
	public Cubemap (Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ, boolean useMipMaps) {
		this(positiveX == null ? null : new PixmapTextureData(positiveX, null, useMipMaps, false),
			negativeX == null ? null : new PixmapTextureData(negativeX, null, useMipMaps, false),
			positiveY == null ? null : new PixmapTextureData(positiveY, null, useMipMaps, false),
			negativeY == null ? null : new PixmapTextureData(negativeY, null, useMipMaps, false),
			positiveZ == null ? null : new PixmapTextureData(positiveZ, null, useMipMaps, false),
			negativeZ == null? null : new PixmapTextureData(negativeZ, null, useMipMaps, false));
	}
	
	public Cubemap (int width, int height, int depth, Format format) {
		this(new PixmapTextureData(new Pixmap(depth, height, format), null, false, true),
			new PixmapTextureData(new Pixmap(depth, height, format), null, false, true),
			new PixmapTextureData(new Pixmap(width, depth, format), null, false, true),
			new PixmapTextureData(new Pixmap(width, depth, format), null, false, true),
			new PixmapTextureData(new Pixmap(width, height, format), null, false, true),
			new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
	}

	public Cubemap (TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY, TextureData positiveZ, TextureData negativeZ) {
		super(GL20.GL_TEXTURE_CUBE_MAP);
		minFilter = TextureFilter.Nearest;
		magFilter = TextureFilter.Nearest;
		uWrap = TextureWrap.ClampToEdge;
		vWrap = TextureWrap.ClampToEdge;
		load(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ);
	}
	
	public void load (TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY, TextureData positiveZ, TextureData negativeZ) {
		bind();
		unsafeSetFilter(minFilter, magFilter, true);
		unsafeSetWrap(uWrap, vWrap, true);
		load(CubemapSide.PositiveX, positiveX);
		load(CubemapSide.NegativeX, negativeX);
		load(CubemapSide.PositiveY, positiveY);
		load(CubemapSide.NegativeY, negativeY);
		load(CubemapSide.PositiveZ, positiveZ);
		load(CubemapSide.NegativeZ, negativeZ);
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
	
	public void load (CubemapSide side, TextureData data) {
		final int idx = side.index;
		if (this.data[idx] != null && data != null && data.isManaged() != this.data[idx].isManaged())
			throw new GdxRuntimeException("New data must have the same managed status as the old data");

		uploadImageData(side.glEnum, data);
		
		this.data[idx] = data;
	}
	
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

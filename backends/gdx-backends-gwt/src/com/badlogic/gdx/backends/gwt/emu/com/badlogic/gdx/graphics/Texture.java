package com.badlogic.gdx.graphics;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData.TextureDataType;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Texture {
	static IntBuffer buffer = BufferUtils.newIntBuffer(1);
	
	public enum TextureFilter {
		Nearest(GL10.GL_NEAREST), Linear(GL10.GL_LINEAR), MipMap(GL10.GL_LINEAR_MIPMAP_LINEAR), MipMapNearestNearest(
			GL10.GL_NEAREST_MIPMAP_NEAREST), MipMapLinearNearest(GL10.GL_LINEAR_MIPMAP_NEAREST), MipMapNearestLinear(
			GL10.GL_NEAREST_MIPMAP_LINEAR), MipMapLinearLinear(GL10.GL_LINEAR_MIPMAP_LINEAR);

		final int glEnum;

		TextureFilter (int glEnum) {
			this.glEnum = glEnum;
		}

		public boolean isMipMap () {
			return glEnum != GL10.GL_NEAREST && glEnum != GL10.GL_LINEAR;
		}

		public int getGLEnum () {
			return glEnum;
		}
	}

	public enum TextureWrap {
		ClampToEdge(GL10.GL_CLAMP_TO_EDGE), Repeat(GL10.GL_REPEAT);

		final int glEnum;

		TextureWrap (int glEnum) {
			this.glEnum = glEnum;
		}

		public int getGLEnum () {
			return glEnum;
		}
	}

	TextureFilter minFilter = TextureFilter.Nearest;
	TextureFilter magFilter = TextureFilter.Nearest;
	TextureWrap uWrap = TextureWrap.ClampToEdge;
	TextureWrap vWrap = TextureWrap.ClampToEdge;
	int glHandle;
	TextureData data;
	
	public Texture (String internalPath) {
		this(Gdx.files.internal(internalPath));
	}

	public Texture (FileHandle file) {
		this(file, null, false);
	}

	public Texture (FileHandle file, boolean useMipMaps) {
		this(file, null, useMipMaps);
	}

	public Texture (FileHandle file, Format format, boolean useMipMaps) {
		create(new FileTextureData(file, null, format, useMipMaps));
	}

	public Texture (Pixmap pixmap) {
		this(new PixmapTextureData(pixmap, null, false, false));
	}

	public Texture (Pixmap pixmap, boolean useMipMaps) {
		this(new PixmapTextureData(pixmap, null, useMipMaps, false));
	}

	public Texture (Pixmap pixmap, Format format, boolean useMipMaps) {
		this(new PixmapTextureData(pixmap, format, useMipMaps, false));
	}

	public Texture (int width, int height, Format format) {
		this(new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
	}

	public Texture (TextureData data) {
		create(data);
	}

	private void create (TextureData data) {
		glHandle = createGLHandle();
		load(data);
	}

	public static int createGLHandle () {
		buffer.position(0);
		buffer.limit(buffer.capacity());
		Gdx.gl.glGenTextures(1, buffer);
		return buffer.get(0);
	}

	public void load (TextureData data) {
		this.data = data;
		if (data.getType() == TextureDataType.Pixmap) {
			if(!data.isPrepared()) data.prepare();
			Pixmap pixmap = data.consumePixmap();
			uploadImageData(pixmap);
			if (data.disposePixmap()) pixmap.dispose();
			setFilter(minFilter, magFilter);
			setWrap(uWrap, vWrap);
			if(data.useMipMaps()) Gdx.gl20.glGenerateMipmap(GL10.GL_TEXTURE_2D);
		}
		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
	}

	private void uploadImageData (Pixmap pixmap) {
		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);
		Gdx.gl.glPixelStorei(GL10.GL_UNPACK_ALIGNMENT, 1);
		Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, pixmap.getWidth(), pixmap.getHeight(), 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixmap.getPixels());
	}
	
	/** Binds this texture. The texture will be bound to the currently active texture unit specified via
	 * {@link GLCommon#glActiveTexture(int)}. */
	public void bind () {
		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);
	}

	/** Binds the texture to the given texture unit. Sets the currently active texture unit via
	 * {@link GLCommon#glActiveTexture(int)}.
	 * @param unit the unit (0 to MAX_TEXTURE_UNITS). */
	public void bind (int unit) {
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0 + unit);
		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);
	}

	/** Draws the given {@link Pixmap} to the texture at position x, y. No clipping is performed so you have to make sure that you
	 * draw only inside the texture region. Note that this will only draw to mipmap level 0!
	 * 
	 * @param pixmap The Pixmap
	 * @param x The x coordinate in pixels
	 * @param y The y coordinate in pixels */
	public void draw (Pixmap pixmap, int x, int y) {
		if (data.isManaged()) throw new GdxRuntimeException("can't draw to a managed texture");

		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);
		Gdx.gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, x, y, pixmap.getWidth(), pixmap.getHeight(), pixmap.getGLFormat(),
			pixmap.getGLType(), pixmap.getPixels());
	}

	/** @return the width of the texture in pixels */
	public int getWidth () {
		return data.getWidth();
	}

	/** @return the height of the texture in pixels */
	public int getHeight () {
		return data.getHeight();
	}

	public TextureFilter getMinFilter () {
		return minFilter;
	}

	public TextureFilter getMagFilter () {
		return magFilter;
	}

	public TextureWrap getUWrap () {
		return uWrap;
	}

	public TextureWrap getVWrap () {
		return vWrap;
	}

	public TextureData getTextureData () {
		return data;
	}

	/** @return whether this texture is managed or not. */
	public boolean isManaged () {
		return data.isManaged();
	}

	public int getTextureObjectHandle () {
		return glHandle;
	}

	/** Sets the {@link TextureWrap} for this texture on the u and v axis. This will bind this texture!
	 * 
	 * @param u the u wrap
	 * @param v the v wrap */
	public void setWrap (TextureWrap u, TextureWrap v) {
		this.uWrap = u;
		this.vWrap = v;
		bind();
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, u.getGLEnum());
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, v.getGLEnum());
	}

	public void setFilter (TextureFilter minFilter, TextureFilter magFilter) {
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		bind();
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, minFilter.getGLEnum());
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, magFilter.getGLEnum());
	}

	/** Disposes all resources associated with the texture */
	public void dispose () {
		if (glHandle == 0) return;
		buffer.clear();
		buffer.put(glHandle);
		buffer.flip();
		Gdx.gl.glDeleteTextures(1, buffer);
		glHandle = 0;
	}
	
	public static void setAssetManager(AssetManager manager) {
		// FIXME well not really, no pause/resume cycle
	}
}

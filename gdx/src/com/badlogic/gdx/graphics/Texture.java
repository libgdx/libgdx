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

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters.LoadedCallback;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData.TextureDataType;
import com.badlogic.gdx.graphics.glutils.ETC1TextureData;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.glutils.MipMapGenerator;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** <p>
 * A Texture wraps a standard OpenGL ES texture.
 * </p>
 * 
 * <p>
 * A Texture can be managed. If the OpenGL context is lost all managed textures get invalidated. This happens when a user switches
 * to another application or receives an incoming call. Managed textures get reloaded automatically.
 * </p>
 * 
 * <p>
 * A Texture has to be bound via the {@link Texture#bind()} method in order for it to be applied to geometry. The texture will be
 * bound to the currently active texture unit specified via {@link GLCommon#glActiveTexture(int)}.
 * </p>
 * 
 * <p>
 * You can draw {@link Pixmap}s to a texture at any time. The changes will be automatically uploaded to texture memory. This is of
 * course not extremely fast so use it with care. It also only works with unmanaged textures.
 * </p>
 * 
 * <p>
 * A Texture must be disposed when it is no longer used
 * </p>
 * 
 * @author badlogicgames@gmail.com */
public class Texture implements Disposable {
	static private boolean enforcePotImages = true;
	static private boolean useHWMipMap = true;
	private static AssetManager assetManager;
	final static Map<Application, List<Texture>> managedTextures = new HashMap<Application, List<Texture>>();

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

	private static final IntBuffer buffer = BufferUtils.newIntBuffer(1);

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
		if (file.name().contains(".etc1")) {
			create(new ETC1TextureData(file, useMipMaps));
		} else {
			create(new FileTextureData(file, null, format, useMipMaps));
		}
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
		if (data.isManaged()) addManagedTexture(Gdx.app, this);
	}

	public static int createGLHandle () {
		buffer.position(0);
		buffer.limit(buffer.capacity());
		Gdx.gl.glGenTextures(1, buffer);
		return buffer.get(0);
	}

	public void load (TextureData data) {
		if (this.data != null && data.isManaged() != this.data.isManaged())
			throw new GdxRuntimeException("New data must have the same managed status as the old data");
		this.data = data;

		if (!data.isPrepared()) data.prepare();

		if (data.getType() == TextureDataType.Pixmap) {
			Pixmap pixmap = data.consumePixmap();
			uploadImageData(pixmap);
			if (data.disposePixmap()) pixmap.dispose();
			setFilter(minFilter, magFilter);
			setWrap(uWrap, vWrap);
		}

		if (data.getType() == TextureDataType.Compressed) {
			Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);
			data.consumeCompressedData();
			setFilter(minFilter, magFilter);
			setWrap(uWrap, vWrap);
		}
	}

	private void uploadImageData (Pixmap pixmap) {
		if (enforcePotImages && Gdx.gl20 == null
			&& (!MathUtils.isPowerOfTwo(data.getWidth()) || !MathUtils.isPowerOfTwo(data.getHeight()))) {
			throw new GdxRuntimeException("Texture width and height must be powers of two: " + data.getWidth() + "x"
				+ data.getHeight());
		}

		boolean disposePixmap = false;
		if (data.getFormat() != pixmap.getFormat()) {
			Pixmap tmp = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), data.getFormat());
			Blending blend = Pixmap.getBlending();
			Pixmap.setBlending(Blending.None);
			tmp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
			Pixmap.setBlending(blend);
			pixmap = tmp;
			disposePixmap = true;
		}

		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);
		Gdx.gl.glPixelStorei(GL10.GL_UNPACK_ALIGNMENT, 1);
		if (data.useMipMaps()) {
			MipMapGenerator.generateMipMap(pixmap, pixmap.getWidth(), pixmap.getHeight(), disposePixmap);
		} else {
			Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
				pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
			if (disposePixmap) {
				pixmap.dispose();
			}
		}
	}

	/** Used internally to reload after context loss. Creates a new GL handle then calls {@link #load(TextureData)}. */
	private void reload () {
		if (!data.isManaged()) throw new GdxRuntimeException("Tried to reload unmanaged Texture");
		createGLHandle();
		load(data);
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
		// this is a hack. reason: we have to set the glHandle to 0 for textures that are
		// reloaded through the asset manager as we first remove (and thus dispose) the texture
		// and then reload it. the glHandle is set to 0 in invalidateAllTextures prior to
		// removal from the asset manager.
		if (glHandle == 0) return;
		buffer.put(0, glHandle);
		Gdx.gl.glDeleteTextures(1, buffer);
		if (data.isManaged()) {
			if (managedTextures.get(Gdx.app) != null) managedTextures.get(Gdx.app).remove(this);
		}
		glHandle = 0;
	}

	/** @param enforcePotImages whether to enforce power of two images in OpenGL ES 1.0 or not. */
	static public void setEnforcePotImages (boolean enforcePotImages) {
		Texture.enforcePotImages = enforcePotImages;
	}

	private static void addManagedTexture (Application app, Texture texture) {
		List<Texture> managedTexureList = managedTextures.get(app);
		if (managedTexureList == null) managedTexureList = new ArrayList<Texture>();
		managedTexureList.add(texture);
		managedTextures.put(app, managedTexureList);
	}

	/** Clears all managed textures. This is an internal method. Do not use it! */
	public static void clearAllTextures (Application app) {
		managedTextures.remove(app);
	}

	/** Invalidate all managed textures. This is an internal method. Do not use it! */
	public static void invalidateAllTextures (Application app) {
		List<Texture> managedTexureList = managedTextures.get(app);
		if (managedTexureList == null) return;

		if (assetManager == null) {
			for (int i = 0; i < managedTexureList.size(); i++) {
				Texture texture = managedTexureList.get(i);
				texture.reload();
			}
		} else {
			// first we have to make sure the AssetManager isn't loading anything anymore,
			// otherwise the ref counting trick below wouldn't work (when a texture is
			// currently on the task stack of the manager.)
			assetManager.finishLoading();

			// next we go through each texture and reload either directly or via the
			// asset manager.
			List<Texture> textures = new ArrayList<Texture>(managedTexureList);
			for (Texture texture : textures) {
				String fileName = assetManager.getAssetFileName(texture);
				if (fileName == null) {
					texture.reload();
				} else {
					// get the ref count of the texture, then set it to 0 so we
					// can actually remove it from the assetmanager. Also set the
					// handle to zero, otherwise we might accidentially dispose
					// already reloaded textures.
					final int refCount = assetManager.getReferenceCount(fileName);
					assetManager.setReferenceCount(fileName, 0);
					texture.glHandle = 0;

					// create the parameters, passing the reference to the texture as
					// well as a callback that sets the ref count.
					TextureParameter params = new TextureParameter();
					params.textureData = texture.getTextureData();
					params.texture = texture; // special parameter which will ensure that the references stay the same.
					params.loadedCallback = new LoadedCallback() {
						@Override
						public void finishedLoading (AssetManager assetManager, String fileName, Class type) {
							assetManager.setReferenceCount(fileName, refCount);
						}
					};

					// unload the texture, create a new gl handle then reload it.
					assetManager.unload(fileName);
					texture.glHandle = Texture.createGLHandle();
					assetManager.load(fileName, Texture.class, params);
				}
			}
			managedTexureList.clear();
			managedTexureList.addAll(textures);
		}
	}

	/** Sets the {@link AssetManager}. When the context is lost, textures managed by the asset manager are reloaded by the manager
	 * on a separate thread (provided that a suitable {@link AssetLoader} is registered with the manager). Textures not managed by
	 * the AssetManager are reloaded via the usual means on the rendering thread.
	 * @param manager the asset manager. */
	public static void setAssetManager (AssetManager manager) {
		Texture.assetManager = manager;
	}

	public static String getManagedStatus () {
		StringBuilder builder = new StringBuilder();
		builder.append("Managed textures/app: { ");
		for (Application app : managedTextures.keySet()) {
			builder.append(managedTextures.get(app).size());
			builder.append(" ");
		}
		builder.append("}");
		return builder.toString();
	}
}

package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.PaintedTextureLoader.PaintedTextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

/** {@link AssetLoader} for {@link Texture} instances. The pixel data is loaded asynchronously. The texture is then created on the
 * rendering thread, synchronously. Passing a {@link PaintedTextureParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows one to specify a {@link Painter} to draw freely the texture data.<br>
 * This Loader must be registered with a custom prefix through {@link AssetManager#setLoader(Class, String, AssetLoader)} to avoid
 * conflicts with other registered {@link Texture} {@link AssetLoader}. FileNames passed to {@link AssetManager#load(String, Class, AssetLoaderParameters)}
 * must use that prefix. FileNames are only informative and does not refer to actual files on the file system.
 * @author https://github.com/avianey */
public class PaintedTextureLoader extends AsynchronousAssetLoader<Texture, PaintedTextureParameter> {
	
	/** Implement this interface to paint the {@link Pixmap} that will be used to create the loaded {@link Texture}
	 * Use methods like {@link Pixmap#drawPixel(int, int)}, {@link Pixmap#drawCircle(int, int, int)}, ...
	 * @author https://github.com/avianey */
   public interface Painter {
       /** Everything you wan't to draw in the texture must be implemented here.
        * @param pixmap the {@link Pixmap} to draw into */
       void draw(final Pixmap pixmap);
   }

	public static class PaintedTextureParameter extends AssetLoaderParameters<Texture> {
		public PaintedTextureParameter() {}
		public PaintedTextureParameter(final Painter render, int width, int height, Pixmap.Format format) {
			this.width = width;
			this.height = height;
			this.render = render;
			this.format = format;
		}
		public PaintedTextureParameter(final Painter render, int width, int height) {
			this.width = width;
			this.height = height;
			this.render = render;
			this.format = Pixmap.Format.RGBA8888;
		}
		/** The desired height of the {@link Texture}. Should be power of two depending on the backend... */
      public int height;
		/** The desired width of the {@link Texture}. Should be power of two depending on the backend... */
		public int width;
		/** The desired pixel format of the {@link Texture}. */
		public Pixmap.Format format;
		/** The {@link Painter} implementation. MUST be not null. */
		public Painter render;
	}

	private Texture texture;
	private Pixmap pixmap;
	
	public PaintedTextureLoader(final FileHandleResolver resolver) {
		super(resolver);
	}

	public void loadAsync(AssetManager manager, String fileName, FileHandle fileHandle, PaintedTextureParameter parameter) {
		pixmap = new Pixmap(parameter.width, parameter.height, parameter.format);
	   parameter.render.draw(pixmap);
	}

	public Texture loadSync(AssetManager manager, String fileName, FileHandle fileHandle, PaintedTextureParameter parameter) {
		texture = new Texture(pixmap);
		return texture;
	}

	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle fileHandle, PaintedTextureParameter parameter) {
		return null;
	}

}

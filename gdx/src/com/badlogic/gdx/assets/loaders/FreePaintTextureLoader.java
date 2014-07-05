package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FreePaintTextureLoader.FreePaintTexture;
import com.badlogic.gdx.assets.loaders.FreePaintTextureLoader.FreePaintTextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.utils.Array;

/** {@link AssetLoader} for {@link FreePaintTexture} instances. The pixel data is loaded asynchronously. The texture is then created on the
 * rendering thread, synchronously. Passing a {@link FreePaintTextureParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows one to specify a {@link Painter} to draw freely the texture data.
 * @author https://github.com/avianey */
public class FreePaintTextureLoader extends AsynchronousAssetLoader<FreePaintTexture, FreePaintTextureParameter> {
	
	/** A {@link Texture} derived class to use with {@link FreePaintTextureLoader}.<br>
	 * This class is only useful to register the loader with the {@link AssetManager}
	 * @author https://github.com/avianey */
	public static final class FreePaintTexture extends Texture {

	   public FreePaintTexture(Pixmap pixmap) {
	       super(pixmap);
	   }
	   
	}
	
	/** This class must be passed to the {@link FreePaintTextureParameter} and will be called by the {@link AsynchronousAssetLoader}
	 * to create the texture data in background. It provides utilities to render custom effects and/or shapes...
	 * @author https://github.com/avianey
	 */
   public static abstract class Painter {
   	
       /** Everything you wan't to draw in the texture must be implemented here!
        * @param pixmap the {@link Gdx2DPixmap} to draw into
        */
       public abstract void draw(final Gdx2DPixmap pixmap);
       
       /*===========*
        * UTILITIES *
        *===========*/
       
       /** Apply noise on a part of the {@link Texture}
        * @param opacity
        * @param pixmap
        * @param x
        * @param y
        * @param width
        * @param height
        */
       public void applyNoise(float opacity, Gdx2DPixmap pixmap, int x, int y, int width, int height) {
           int o = ((int) (opacity * 256)) & 0x000000FF;
           for (int i = x; i < width + x; i++) {
               for (int j = y; j < height + y; j++) {
                   int color = (int) (Math.random() * 256) & 0x000000FF;
                   pixmap.setPixel(i, j, (color << 8) | (color << 16) | (color << 24) | o);
               }
           }
       }

       /** Apply noise on the whole {@link Texture}
        * @param opacity
        * @param pixmap
        */
       public void applyNoise(float opacity, Gdx2DPixmap pixmap) {
           applyNoise(opacity, pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight());
       }
   }

	public static class FreePaintTextureParameter extends AssetLoaderParameters<FreePaintTexture> {
		public FreePaintTextureParameter() {}
		public FreePaintTextureParameter(final Painter render, int desiredWidth, int desiredHeight) {
			this.desiredWidth = desiredWidth;
			this.desiredHeight = desiredHeight;
			this.render = render;
		}
		/** The desired height of the {@link Texture}. Should be power of two depending on the backend... */
      public int desiredHeight;
		/** The desired width of the {@link Texture}. Should be power of two depending on the backend... */
		public int desiredWidth;
		/** The {@link Painter} implementation. MUST be not null. */
		public Painter render;
	}

	private FreePaintTexture texture;
	private Gdx2DPixmap pixmap2d;
	
	public FreePaintTextureLoader(final FileHandleResolver resolver) {
		super(resolver);
	}

	public void loadAsync(AssetManager manager, String fileName, FileHandle fileHandle, FreePaintTextureParameter parameter) {
		pixmap2d = new Gdx2DPixmap(parameter.desiredWidth, parameter.desiredHeight, Gdx2DPixmap.GDX2D_FORMAT_RGBA8888);
	   parameter.render.draw(pixmap2d);
	}

	public FreePaintTexture loadSync(AssetManager manager, String fileName, FileHandle fileHandle, FreePaintTextureParameter parameter) {
		texture = new FreePaintTexture(new Pixmap(pixmap2d));
		return texture;
	}

	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle fileHandle, FreePaintTextureParameter parameter) {
		return null;
	}

}

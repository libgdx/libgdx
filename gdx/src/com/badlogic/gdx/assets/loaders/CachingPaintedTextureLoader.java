package com.badlogic.gdx.assets.loaders;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.CachingPaintedTextureLoader;
import com.badlogic.gdx.assets.loaders.PaintedTextureLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.CachingPaintedTextureLoader.CachingPaintedTextureParameter;
import com.badlogic.gdx.assets.loaders.PaintedTextureLoader.Painter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.Array;

/** {@link AssetLoader} for {@link Texture} instances. The pixel data is loaded asynchronously from another {@link AsynchronousAssetLoader}
 * and cached as PNG file on the file system to speed-up re-loading of the {@link Texture}. Use {@link CachingPaintedTextureParameter} to specify an 
 * {@link AsynchronousAssetLoader} and a {@link AssetLoaderParameters} to use if the cached version does not exist.<br>
 * Useful when used with {@link PaintedTextureLoader}.
 * @author https://github.com/avianey */
public class CachingPaintedTextureLoader extends AsynchronousAssetLoader<Texture, CachingPaintedTextureParameter> {

   private static final String CACHE_EXTENSION = ".png";
   
	public static class CachingPaintedTextureParameter extends AssetLoaderParameters<Texture> {
		public CachingPaintedTextureParameter() {}
	   /** Create a {@link AssetLoaderParameters} for the {@link CachingPaintedTextureLoader}.<br>
		 * Use this when {@link PaintedTextureLoader} is not the default loader for {@link Texture}.
		 * @param parameters parameters for the {@link AsynchronousAssetLoader} associated with the loaderClass and the loaderSuffix
		 * @param loaderClass the class associated with the {@link AsynchronousAssetLoader} in the {@link AssetManager}
		 * @param fileName the file name of the resource to cache
		 * @see AssetManager#setLoader(Class, String, AssetLoader) */
		public CachingPaintedTextureParameter(AssetLoaderParameters<? extends Texture> parameters, Class<? extends Texture> loaderClass, String fileName) {
		    this.parameters = parameters;
		    this.loaderClass = loaderClass;
		    this.fileName = fileName;
		}
		public AssetLoaderParameters<? extends Texture> parameters;
		public Class<? extends Texture> loaderClass;
		public String fileName;
	}
	
	public CachingPaintedTextureLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle fileHandle, CachingPaintedTextureParameter parameter) {
	    if (!Gdx.files.local(fileName + CACHE_EXTENSION).exists()) {
	   	 // the cached file does not exists, delegate the loading to the encapsulated loader
	   	 ((AsynchronousAssetLoader) manager.getLoader(parameter.loaderClass, parameter.fileName)).loadAsync(manager, fileName, fileHandle, parameter.parameters);
	    }
	}

	@Override
	public Texture loadSync(AssetManager manager, String fileName, FileHandle fileHandle, CachingPaintedTextureParameter parameter) {
	    final String name = fileName + CACHE_EXTENSION;
	    if (Gdx.files.local(name).exists()) {
	        // use cached version of the data
	        Pixmap pixmap = new Pixmap(Gdx.files.local(name));
	        return new Texture(pixmap);
	    } else {
	        // get the texture from the encapsulated loader and cache it
	        Texture texture = ((AsynchronousAssetLoader<? extends Texture, AssetLoaderParameters<? extends Texture>>) manager.getLoader(parameter.loaderClass, parameter.fileName))
	      	  .loadSync(manager, fileName, fileHandle, parameter.parameters);
	        TextureData data = texture.getTextureData();
            PixmapIO.writePNG(Gdx.files.local(name), data.consumePixmap());
	        return new Texture(data);
	    }
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle fileHandle, CachingPaintedTextureParameter parameter) {
		return null;
	}
	
}

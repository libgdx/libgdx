package com.badlogic.gdx.assets.loaders;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ProxyTextureLoader.ProxyTextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.ProxyTexture;
import com.badlogic.gdx.utils.Array;

/** {@link AssetLoader} for {@link ProxyTexture} instances. The pixel data is loaded asynchronously from another {@link AsynchronousAssetLoader}
 * and cached as PNG on the file system to speed-up the following Texture loading. use {@link ProxyTextureParameter} to specify an 
 * {@link AsynchronousAssetLoader} and a {@link AssetLoaderParameters} to use if the cached version does not exist. 
 * Useful when used with {@link FreePaintTextureLoader}.
 * @author https://github.com/avianey
 */
public class ProxyTextureLoader extends AsynchronousAssetLoader<ProxyTexture, ProxyTextureParameter> {

   private static final String SAVE_EXTENSION = ".png";
   
	public static class ProxyTextureParameter extends AssetLoaderParameters<ProxyTexture> {
		public ProxyTextureParameter() {}
		/** Create a {@link AssetLoaderParameters} for the {@link ProxyTextureLoader}
		 * @param parameters parameters for the {@link AsynchronousAssetLoader} associated with the loaderClass
		 * @param loaderClass the class to fetch an {@link AsynchronousAssetLoader} from the {@link AssetManager}
		 * @see AssetManager#setLoader(Class, AssetLoader)
		 */
		public ProxyTextureParameter(AssetLoaderParameters<? extends Texture> parameters, Class<? extends Texture> loaderClass) {
		    this.parameters = parameters;
		    this.loaderClass = loaderClass;
		}
		/** Create a {@link AssetLoaderParameters} for the {@link ProxyTextureLoader}
		 * @param parameters parameters for the {@link AsynchronousAssetLoader} associated with the loaderClass and the loaderSuffix
		 * @param loaderClass the class to fetch an {@link AsynchronousAssetLoader} from the {@link AssetManager}
		 * @param loaderSuffix the name of the {@link AsynchronousAssetLoader} associated with the loaderClass in the {@link AssetManager}
		 * @see AssetManager#setLoader(Class, String, AssetLoader)
		 */
		public ProxyTextureParameter(AssetLoaderParameters<? extends Texture> parameters, Class<? extends Texture> loaderClass, String loaderSuffix) {
		    this.parameters = parameters;
		    this.loaderClass = loaderClass;
		    this.loaderSuffix = loaderSuffix;
		}
		public AssetLoaderParameters<? extends Texture> parameters;
		public Class<? extends Texture> loaderClass;
		public String loaderSuffix;
	}
	
	public ProxyTextureLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle fileHandle, ProxyTextureParameter parameter) {
	    if (!Gdx.files.local(fileName + SAVE_EXTENSION).exists()) {
	   	 ((AsynchronousAssetLoader) manager.getLoader(parameter.loaderClass, parameter.loaderSuffix)).loadAsync(manager, fileName, fileHandle, parameter.parameters);
	    }
	}

	@Override
	public ProxyTexture loadSync(AssetManager manager, String fileName, FileHandle fileHandle, ProxyTextureParameter parameter) {
	    final String name = fileName + SAVE_EXTENSION;
	    if (Gdx.files.local(name).exists()) {
	        // use cached version of the data
	        Pixmap pixmap = new Pixmap(Gdx.files.local(name));
	        return new ProxyTexture(pixmap);
	    } else {
	        // get the texture and cache it
	        Texture texture = ((AsynchronousAssetLoader<? extends Texture, AssetLoaderParameters<? extends Texture>>) manager.getLoader(parameter.loaderClass, parameter.loaderSuffix))
	      	  .loadSync(manager, fileName, fileHandle, parameter.parameters);
	        TextureData data = texture.getTextureData();
           PixmapIO.writePNG(Gdx.files.local(name), data.consumePixmap());
	        return new ProxyTexture(data);
	    }
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle fileHandle, ProxyTextureParameter parameter) {
		return null;
	}
	
}

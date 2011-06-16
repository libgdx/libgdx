package com.badlogic.gdx.graphics.g3d.loaders;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.loaders.collada.ColladaLoader;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dLoader.G3dStillModelLoader;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dtLoader.G3dtKeyframedModelLoader;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dtLoader.G3dtStillModelLoader;
import com.badlogic.gdx.graphics.g3d.loaders.md2.MD2Loader;
import com.badlogic.gdx.graphics.g3d.loaders.md2.MD2Loader.MD2LoaderHints;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.OgreXmlLoader;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedModel;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Simple "pluggable" class for loading models. Keeps a list of {@link ModelLoader} instances
 * on a per file suffix basis. Use one of the static methods to load a {@link Model}. The registry
 * will then try out all the registered loaders for that extension and eventually return a Model
 * or throw a {@link GdxRuntimeException}. Per default all loaders of libgdx except the {@link OgreXmlLoader}
 * which won't work on Android due to the JAXB dependency.
 * 
 * @author mzechner
 *
 */
public class ModelLoaderRegistry {
	private static Map<String, Array<ModelLoader>> loaders = new HashMap<String, Array<ModelLoader>>();
	private static Map<String, Array<ModelLoaderHints>> defaultHints = new HashMap<String, Array<ModelLoaderHints>>();

	// registering the default loaders here
	static {
		registerLoader("dae", new ColladaLoader(), new ModelLoaderHints(false));
		registerLoader("obj", new ObjLoader(), new ModelLoaderHints(false));
		registerLoader("g3dt", new G3dtStillModelLoader(), new ModelLoaderHints(true));
		registerLoader("g3dt", new G3dtKeyframedModelLoader(), new ModelLoaderHints(true));
		registerLoader("g3d", new G3dStillModelLoader(), new ModelLoaderHints(false));		
		registerLoader("md2", new MD2Loader(), new MD2LoaderHints(0.2f) );
	}
	
	/**
	 * Registers a new loader with the registry. The extension will be used
	 * to match the loader against a file to be loaded. The extension will
	 * be compared case insensitive. If multiple loaders are registered per
	 * extension they will be tried on a file in the sequence they have been
	 * registered until one succeeds or none succeed.
	 * 
	 * @param extension the extension string, e.g. "dae" or "obj"
	 * @param loader the {@link ModelLoader}
	 * @param defaultHints the default {@link ModelLoaderHints} to be used with this loader.
	 */
	public static void registerLoader(String extension, ModelLoader loader, ModelLoaderHints defaultHints) {
		Array<ModelLoader> loaders = ModelLoaderRegistry.loaders.get(extension);
		if(loaders == null) {
			loaders = new Array<ModelLoader>();
			ModelLoaderRegistry.loaders.put(extension.toLowerCase(), loaders);
		}			
		loaders.add(loader);
		
		Array<ModelLoaderHints> hints = ModelLoaderRegistry.defaultHints.get(extension);
		if(hints == null) {
			hints = new Array<ModelLoaderHints>();
			ModelLoaderRegistry.defaultHints.put(extension.toLowerCase(), hints);
		}
		hints.add(defaultHints);
	}
	
	/**
	 * Loads the specified file with one of the loaders registered with this
	 * ModelLoaderRegistry. Uses the extension to determine which loader to use.
	 * The comparison of extensions is done case insensitive.
	 * @param file the file to be loaded 
	 * @return the {@link Model}
	 * @throws GdxRuntimeException in case the model could not be loaded.
	 */
	public static Model load(FileHandle file) {
		String name = file.name();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) throw new GdxRuntimeException("file '" + file.name() + "' does not have an extension that can be matched to a ModelLoader");
		String extension = name.substring(dotIndex + 1).toLowerCase();
		
		Array<ModelLoader> loaders = ModelLoaderRegistry.loaders.get(extension);
		Array<ModelLoaderHints> hints = ModelLoaderRegistry.defaultHints.get(extension);
		if(loaders == null) throw new GdxRuntimeException("no loaders for extension '" + extension + "'");
		if(hints == null) throw new GdxRuntimeException("no default hints for extension '" + extension + "'");
		
		Model model = null;
		StringBuilder errors = new StringBuilder();
		for(int i = 0; i < loaders.size; i++) {
			ModelLoader loader = loaders.get(i);
			ModelLoaderHints hint = hints.get(i);
			try {				
				model = loader.load(file, hint);
			} catch(GdxRuntimeException e) {
				errors.append("Couldn't load '" + file.name() + "' with loader of type " + loader.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
			}
		}
		
		if(model == null) throw new GdxRuntimeException(errors.toString());
		else return model;
	}
	
	/**
	 * Loads the specified file with one of the loaders registered with this
	 * ModelLoaderRegistry. Uses the extension to determine which loader to use.
	 * The comparison of extensions is done case insensitive.
	 * @param file the file to be loaded 
	 * @param hints the {@link ModelLoaderHints} to use
	 * @return the {@link Model}
	 * @throws GdxRuntimeException in case the model could not be loaded.
	 */
	public static Model load(FileHandle file, ModelLoaderHints hints) {
		String name = file.name();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) throw new GdxRuntimeException("file '" + file.name() + "' does not have an extension that can be matched to a ModelLoader");
		String extension = name.substring(dotIndex + 1).toLowerCase();
		
		Array<ModelLoader> loaders = ModelLoaderRegistry.loaders.get(extension);		
		if(loaders == null) throw new GdxRuntimeException("no loaders for extension '" + extension + "'");		
		
		Model model = null;
		StringBuilder errors = new StringBuilder();
		for(int i = 0; i < loaders.size; i++) {
			ModelLoader loader = loaders.get(i);			
			try {				
				model = loader.load(file, hints);
			} catch(GdxRuntimeException e) {
				errors.append("Couldn't load '" + file.name() + "' with loader of type " + loader.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
			}
		}
		
		if(model == null) throw new GdxRuntimeException(errors.toString());
		else return model;
	}
	
	/**
	 * Loads the specified file with one of the loaders registered with this
	 * ModelLoaderRegistry. Uses the extension to determine which loader to use.
	 * The comparison of extensions is done case insensitive. Uses only
	 * {@link StillModelLoader} instances.
	 * @param file the file to be loaded 
	 * @return the {@link Model}
	 * @throws GdxRuntimeException in case the model could not be loaded.
	 */
	public static StillModel loadStillModel(FileHandle file) {
		String name = file.name();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) throw new GdxRuntimeException("file '" + file.name() + "' does not have an extension that can be matched to a ModelLoader");
		String extension = name.substring(dotIndex + 1).toLowerCase();
		
		Array<ModelLoader> loaders = ModelLoaderRegistry.loaders.get(extension);
		Array<ModelLoaderHints> hints = ModelLoaderRegistry.defaultHints.get(extension);
		if(loaders == null) throw new GdxRuntimeException("no loaders for extension '" + extension + "'");
		if(hints == null) throw new GdxRuntimeException("no default hints for extension '" + extension + "'");
		
		StillModel model = null;
		StringBuilder errors = new StringBuilder();
		for(int i = 0; i < loaders.size; i++) {
			ModelLoader loader = loaders.get(i);
			ModelLoaderHints hint = hints.get(i);
			try {												
				if(loader instanceof StillModelLoader) {
					model = ((StillModelLoader)loader).load(file, hint);
				}
			} catch(GdxRuntimeException e) {
				errors.append("Couldn't load '" + file.name() + "' with loader of type " + loader.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
			}
		}
		
		if(model == null) throw new GdxRuntimeException("Couldn't load model '" + file.name() + "', " + errors.toString());
		else return model;
	}
	
	/**
	 * Loads the specified file with one of the loaders registered with this
	 * ModelLoaderRegistry. Uses the extension to determine which loader to use.
	 * The comparison of extensions is done case insensitive. Uses only
	 * {@link StillModelLoader} instances.
	 * @param file the file to be loaded 
	 * @oaram hints the ModelLoaderHints to be used.
	 * @return the {@link Model}
	 * @throws GdxRuntimeException in case the model could not be loaded.
	 */
	public static StillModel loadStillModel(FileHandle file, ModelLoaderHints hints) {
		String name = file.name();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) throw new GdxRuntimeException("file '" + file.name() + "' does not have an extension that can be matched to a ModelLoader");
		String extension = name.substring(dotIndex + 1).toLowerCase();
		
		Array<ModelLoader> loaders = ModelLoaderRegistry.loaders.get(extension);		
		if(loaders == null) throw new GdxRuntimeException("no loaders for extension '" + extension + "'");		
		
		StillModel model = null;
		StringBuilder errors = new StringBuilder();
		for(int i = 0; i < loaders.size; i++) {
			ModelLoader loader = loaders.get(i);			
			try {												
				if(loader instanceof StillModelLoader) {
					model = ((StillModelLoader)loader).load(file, hints);
				}
			} catch(GdxRuntimeException e) {
				errors.append("Couldn't load '" + file.name() + "' with loader of type " + loader.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
			}
		}
		
		if(model == null) throw new GdxRuntimeException("Couldn't load model '" + file.name() + "', " + errors.toString());
		else return model;
	}
	
	/**
	 * Loads the specified file with one of the loaders registered with this
	 * ModelLoaderRegistry. Uses the extension to determine which loader to use.
	 * The comparison of extensions is done case insensitive. Uses only
	 * {@link KeyframedModelLoader} instances.
	 * @param file the file to be loaded 
	 * @return the {@link Model}
	 * @throws GdxRuntimeException in case the model could not be loaded.
	 */
	public static KeyframedModel loadKeyframedModel(FileHandle file) {
		String name = file.name();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) throw new GdxRuntimeException("file '" + file.name() + "' does not have an extension that can be matched to a ModelLoader");
		String extension = name.substring(dotIndex + 1).toLowerCase();
		
		Array<ModelLoader> loaders = ModelLoaderRegistry.loaders.get(extension);
		Array<ModelLoaderHints> hints = ModelLoaderRegistry.defaultHints.get(extension);
		if(loaders == null) throw new GdxRuntimeException("no loaders for extension '" + extension + "'");
		if(hints == null) throw new GdxRuntimeException("no default hints for extension '" + extension + "'");
		
		KeyframedModel model = null;
		StringBuilder errors = new StringBuilder();
		for(int i = 0; i < loaders.size; i++) {
			ModelLoader loader = loaders.get(i);
			ModelLoaderHints hint = hints.get(i);
			try {
				if(loader instanceof KeyframedModelLoader) {
					model = ((KeyframedModelLoader)loader).load(file, hint);
				}
			} catch(GdxRuntimeException e) {
				errors.append("Couldn't load '" + file.name() + "' with loader of type " + loader.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
			}
		}
		
		if(model == null) throw new GdxRuntimeException(errors.toString());
		else return model;
	}
	
	/**
	 * Loads the specified file with one of the loaders registered with this
	 * ModelLoaderRegistry. Uses the extension to determine which loader to use.
	 * The comparison of extensions is done case insensitive. Uses only
	 * {@link KeyframedModelLoader} instances.
	 * @param file the file to be loaded
	 * @param hints the Model 
	 * @return the {@link Model}
	 * @throws GdxRuntimeException in case the model could not be loaded.
	 */
	public static KeyframedModel loadKeyframedModel(FileHandle file, ModelLoaderHints hints) {
		String name = file.name();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) throw new GdxRuntimeException("file '" + file.name() + "' does not have an extension that can be matched to a ModelLoader");
		String extension = name.substring(dotIndex + 1).toLowerCase();
		
		Array<ModelLoader> loaders = ModelLoaderRegistry.loaders.get(extension);		
		if(loaders == null) throw new GdxRuntimeException("no loaders for extension '" + extension + "'");		
		
		KeyframedModel model = null;
		StringBuilder errors = new StringBuilder();
		for(int i = 0; i < loaders.size; i++) {
			ModelLoader loader = loaders.get(i);			
			try {
				if(loader instanceof KeyframedModelLoader) {
					model = ((KeyframedModelLoader)loader).load(file, hints);
				}
			} catch(GdxRuntimeException e) {
				errors.append("Couldn't load '" + file.name() + "' with loader of type " + loader.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
			}
		}
		
		if(model == null) throw new GdxRuntimeException(errors.toString());
		else return model;
	}
	
	/**
	 * Loads the specified file with one of the loaders registered with this
	 * ModelLoaderRegistry. Uses the extension to determine which loader to use.
	 * The comparison of extensions is done case insensitive. Uses only
	 * {@link SkeletonModelLoader} instances.
	 * @param file the file to be loaded 
	 * @return the {@link Model}
	 * @throws GdxRuntimeException in case the model could not be loaded.
	 */
	public static SkeletonModel loadSkeletonModel(FileHandle file) {
		String name = file.name();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) throw new GdxRuntimeException("file '" + file.name() + "' does not have an extension that can be matched to a ModelLoader");
		String extension = name.substring(dotIndex + 1).toLowerCase();
		
		Array<ModelLoader> loaders = ModelLoaderRegistry.loaders.get(extension);
		Array<ModelLoaderHints> hints = ModelLoaderRegistry.defaultHints.get(extension);
		if(loaders == null) throw new GdxRuntimeException("no loaders for extension '" + extension + "'");
		if(hints == null) throw new GdxRuntimeException("no default hints for extension '" + extension + "'");
		
		SkeletonModel model = null;
		StringBuilder errors = new StringBuilder();
		for(int i = 0; i < loaders.size; i++) {
			ModelLoader loader = loaders.get(i);
			ModelLoaderHints hint = hints.get(i);
			try {
				if(loader instanceof SkeletonModelLoader) {
					model = ((SkeletonModelLoader)loader).load(file, hint);
				}
			} catch(GdxRuntimeException e) {
				errors.append("Couldn't load '" + file.name() + "' with loader of type " + loader.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
		
		if(model == null) throw new GdxRuntimeException(errors.toString());
		else return model;
	}
	
	/**
	 * Loads the specified file with one of the loaders registered with this
	 * ModelLoaderRegistry. Uses the extension to determine which loader to use.
	 * The comparison of extensions is done case insensitive. Uses only
	 * {@link SkeletonModelLoader} instances.
	 * @param file the file to be loaded 
	 * @param hints the ModelLoaderHints to use
	 * @return the {@link Model}
	 * @throws GdxRuntimeException in case the model could not be loaded.
	 */
	public static SkeletonModel loadSkeletonModel(FileHandle file, ModelLoaderHints hints) {
		String name = file.name();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) throw new GdxRuntimeException("file '" + file.name() + "' does not have an extension that can be matched to a ModelLoader");
		String extension = name.substring(dotIndex + 1).toLowerCase();
		
		Array<ModelLoader> loaders = ModelLoaderRegistry.loaders.get(extension);		
		if(loaders == null) throw new GdxRuntimeException("no loaders for extension '" + extension + "'");		
		
		SkeletonModel model = null;
		StringBuilder errors = new StringBuilder();
		for(int i = 0; i < loaders.size; i++) {
			ModelLoader loader = loaders.get(i);			
			try {
				if(loader instanceof SkeletonModelLoader) {
					model = ((SkeletonModelLoader)loader).load(file, hints);
				}
			} catch(GdxRuntimeException e) {
				errors.append("Couldn't load '" + file.name() + "' with loader of type " + loader.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
		
		if(model == null) throw new GdxRuntimeException(errors.toString());
		else return model;
	}
}

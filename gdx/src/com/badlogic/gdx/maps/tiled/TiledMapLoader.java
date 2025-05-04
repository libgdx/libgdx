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

package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;

/** A universal TiledMap loader that supports all currently available map formats in libGDX. This loader automatically delegates
 * to the appropriate underlying map loader {@link TmxMapLoader}, {@link AtlasTmxMapLoader}, {@link TmjMapLoader}, or
 * {@link AtlasTmjMapLoader} based solely on the map file's extension and content. A primary use case is for projects that need to
 * load a mix of TMX and TMJ maps (with or without atlases) using a single loader instance inside an {@link AssetManager}. For TMX
 * and TMJ files, this loader checks for the presence of an {@code "atlas"} property. If found, it uses an atlas-based loader;
 * otherwise, it falls back to the standard loader. */
public class TiledMapLoader extends AsynchronousAssetLoader<TiledMap, BaseTiledMapLoader.Parameters> {

	private final TmxMapLoader tmxMapLoader;
	private final TmjMapLoader tmjMapLoader;

	private final AtlasTmxMapLoader atlasTmxMapLoader;
	private final XmlReader xmlReader;

	private final AtlasTmjMapLoader atlasTmjMapLoader;
	private final JsonReader jsonReader;

	public TiledMapLoader () {
		this(new InternalFileHandleResolver());
	}

	public TiledMapLoader (FileHandleResolver resolver) {
		super(resolver);
		tmxMapLoader = new TmxMapLoader(resolver);
		tmjMapLoader = new TmjMapLoader(resolver);
		atlasTmxMapLoader = new AtlasTmxMapLoader(resolver);
		xmlReader = new XmlReader();
		atlasTmjMapLoader = new AtlasTmjMapLoader(resolver);
		jsonReader = new JsonReader();
	}

	/** Universal synchronous loader. This method is a thin wrapper that picks the correct underlying loader (TMX vs TMJ, atlas vs
	 * non‑atlas) and then delegates straight through to its synchronous {@code load(...)} implementation.
	 * @param fileName path to a .tmx or .tmj file
	 * @return a loaded {@link TiledMap}
	 * @throws GdxRuntimeException on unsupported formats or parse errors */
	public TiledMap load (String fileName) {
		return load(fileName, new BaseTiledMapLoader.Parameters());
	}

	/** Universal synchronous loader with custom parameters. Resolves the file and Inspects the extension (tmx vs tmj). Check
	 * whether the ‘atlas’ property exists in the map and delegates to the appropriate loader’s {@code load(...)}.
	 * @param fileName path to a .tmx or .tmj file
	 * @param parameter existing Parameters object
	 * @return a loaded {@link TiledMap}
	 * @throws GdxRuntimeException on unsupported formats or parse errors */
	public TiledMap load (String fileName, BaseTiledMapLoader.Parameters parameter) {

		if (parameter == null) parameter = new BaseTiledMapLoader.Parameters();
		FileHandle file = resolve(fileName);
		String extension = file.extension().toLowerCase();
		if (extension.equals("tmx")) {
			if (usesAtlas(file)) {
				// atlas‑backed TMX
				return atlasTmxMapLoader.load(fileName,
					convertParameters(parameter, AtlasTmxMapLoader.AtlasTiledMapLoaderParameters.class));
			} else {
				// plain TMX
				return tmxMapLoader.load(fileName, convertParameters(parameter, TmxMapLoader.Parameters.class));
			}
		} else if (extension.equals("tmj")) {
			if (usesAtlas(file)) {
				// atlas‑backed TMJ
				return atlasTmjMapLoader.load(fileName,
					convertParameters(parameter, AtlasTmjMapLoader.AtlasTiledMapLoaderParameters.class));
			} else {
				// plain TMJ
				return tmjMapLoader.load(fileName, convertParameters(parameter, TmjMapLoader.Parameters.class));
			}
		} else {
			// no other formats supported
			throw new GdxRuntimeException("Unsupported map format: '" + extension + "' in file: " + fileName);
		}
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, BaseTiledMapLoader.Parameters parameter) {

		if (parameter == null) parameter = new BaseTiledMapLoader.Parameters();
		String extension = file.extension().toLowerCase();
		if (extension.equals("tmx")) {
			if (usesAtlas(file)) {
				return atlasTmxMapLoader.getDependencies(fileName, file,
					convertParameters(parameter, AtlasTmxMapLoader.AtlasTiledMapLoaderParameters.class));
			} else {
				return tmxMapLoader.getDependencies(fileName, file, convertParameters(parameter, TmxMapLoader.Parameters.class));
			}
		} else if (extension.equals("tmj")) {
			if (usesAtlas(file)) {
				return atlasTmjMapLoader.getDependencies(fileName, file,
					convertParameters(parameter, AtlasTmjMapLoader.AtlasTiledMapLoaderParameters.class));
			} else {
				return tmjMapLoader.getDependencies(fileName, file, convertParameters(parameter, TmjMapLoader.Parameters.class));
			}
		} else {
			throw new IllegalArgumentException("Unsupported map format: " + extension);
		}
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, BaseTiledMapLoader.Parameters parameter) {

		if (parameter == null) parameter = new BaseTiledMapLoader.Parameters();
		String extension = file.extension().toLowerCase();
		if (extension.equals("tmx")) {
			if (usesAtlas(file)) {
				atlasTmxMapLoader.loadAsync(manager, fileName, file,
					convertParameters(parameter, AtlasTmxMapLoader.AtlasTiledMapLoaderParameters.class));
			} else {
				tmxMapLoader.loadAsync(manager, fileName, file, convertParameters(parameter, TmxMapLoader.Parameters.class));
			}
		} else if (extension.equals("tmj")) {
			if (usesAtlas(file)) {
				atlasTmjMapLoader.loadAsync(manager, fileName, file,
					convertParameters(parameter, AtlasTmjMapLoader.AtlasTiledMapLoaderParameters.class));
			} else {
				tmjMapLoader.loadAsync(manager, fileName, file, convertParameters(parameter, TmjMapLoader.Parameters.class));
			}

		} else {
			throw new IllegalArgumentException("Unsupported map format: " + extension);
		}
	}

	@Override
	public TiledMap loadSync (AssetManager manager, String fileName, FileHandle file, BaseTiledMapLoader.Parameters parameter) {
		TiledMap map;
		if (parameter == null) parameter = new BaseTiledMapLoader.Parameters();

		String extension = file.extension().toLowerCase();
		if (extension.equals("tmx")) {
			if (usesAtlas(file)) {
				map = atlasTmxMapLoader.loadSync(manager, fileName, file,
					convertParameters(parameter, AtlasTmxMapLoader.AtlasTiledMapLoaderParameters.class));
			} else {
				map = tmxMapLoader.loadSync(manager, fileName, file, convertParameters(parameter, TmxMapLoader.Parameters.class));
			}
		} else if (extension.equals("tmj")) {
			if (usesAtlas(file)) {
				map = atlasTmjMapLoader.loadSync(manager, fileName, file,
					convertParameters(parameter, AtlasTmjMapLoader.AtlasTiledMapLoaderParameters.class));
			} else {
				map = tmjMapLoader.loadSync(manager, fileName, file, convertParameters(parameter, TmjMapLoader.Parameters.class));
			}
		} else {
			throw new IllegalArgumentException("Unsupported map format: " + extension);
		}
		return map;
	}

	private boolean usesAtlas (FileHandle file) {
		String extension = file.extension().toLowerCase();
		if (extension.equals("tmx")) {
			XmlReader.Element root = xmlReader.parse(file);
			XmlReader.Element properties = root.getChildByName("properties");
			if (properties != null) {
				for (XmlReader.Element property : properties.getChildrenByName("property")) {
					String name = property.getAttribute("name", "");
					if ("atlas".equals(name)) {
						return true;
					}
				}
			}
		} else if (extension.equals("tmj")) {
			JsonValue root = jsonReader.parse(file);
			JsonValue properties = root.get("properties");
			if (properties != null) {
				for (JsonValue property : properties) {
					String name = property.getString("name", "");
					if ("atlas".equals(name)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** Each individual map loader has its own subclass of Parameters extended from BaseTiledMapLoader.Parameters This method will
	 * make sure we are returning the proper Parameters that the loader expects. If BaseTiledMapLoader.Parameters are passed in, we
	 * will create the appropriate Parameters for the user and pass them back.
	 *
	 * @param incomingParameters whatever the user supplied to the TiledMapLoader
	 * @param targetParameterClass the concrete Parameters type required by the specific loader
	 * @param <T> same as {@code targetParameterClass}
	 * @return a non‑null instance of {@code targetParameterClass}, to be used by the loader
	 * @throws RuntimeException if the supplied parameters are of an incompatible subclass */
	private <T extends BaseTiledMapLoader.Parameters> T convertParameters (BaseTiledMapLoader.Parameters incomingParameters,
		Class<T> targetParameterClass) {

		// If the parameters passed in are already the appropriate subclass. We return them as is.
		if (targetParameterClass.isInstance(incomingParameters)) return (T)incomingParameters;

		// It's possible a user has decided to pass in Map Parameters in the form of Class BaseTiledMapLoader.Parameters
		// Instead of throwing an error we convert them to the correct format instead.
		BaseTiledMapLoader.Parameters convertedParameters = null;

		// Check to see if Parameter class is an instance of BaseTiledMapLoader.Parameters
		if (incomingParameters.getClass() == BaseTiledMapLoader.Parameters.class) {
			// Check to see what type of Map Loader Parameters we should be looking to convert to
			if (targetParameterClass == TmxMapLoader.Parameters.class) {
				convertedParameters = new TmxMapLoader.Parameters();
			} else if (targetParameterClass == AtlasTmxMapLoader.AtlasTiledMapLoaderParameters.class) {
				convertedParameters = new AtlasTmxMapLoader.AtlasTiledMapLoaderParameters();
			} else if (targetParameterClass == TmjMapLoader.Parameters.class) {
				convertedParameters = new TmjMapLoader.Parameters();
			} else if (targetParameterClass == AtlasTmjMapLoader.AtlasTiledMapLoaderParameters.class) {
				convertedParameters = new AtlasTmjMapLoader.AtlasTiledMapLoaderParameters();
			} else {
				throw new IllegalArgumentException("Unsupported map parameter class: " + targetParameterClass);
			}
		} else {
			// Throw an error if user has explicitly passed in Parameters of incorrect map type
			throw new IllegalArgumentException("Parameters passed in: " + incomingParameters.getClass()
				+ ", does not match the expected parameter type: " + targetParameterClass);
		}

		// Copy over the base parameters found in BaseTiledMapLoader.Parameters to the newly built parameters object
		copyBaseParams(incomingParameters, convertedParameters);
		return (T)convertedParameters;
	}

	/** Copies all fields that exist in the BaseTiledMapLoader.Parameters class. */
	private void copyBaseParams (BaseTiledMapLoader.Parameters oldParameters, BaseTiledMapLoader.Parameters newParameters) {
		newParameters.textureMinFilter = oldParameters.textureMinFilter;
		newParameters.textureMagFilter = oldParameters.textureMagFilter;
		newParameters.generateMipMaps = oldParameters.generateMipMaps;
		newParameters.flipY = oldParameters.flipY;
		newParameters.convertObjectToTileSpace = oldParameters.convertObjectToTileSpace;
		newParameters.projectFilePath = oldParameters.projectFilePath;
	}

}

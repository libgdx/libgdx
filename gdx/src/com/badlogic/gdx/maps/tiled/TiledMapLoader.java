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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.*;

/**
 * A Convenient Universal Map Loader Wrapper class meant to load all map types we currently support.
 * Specific use case would be situations where you would like to load multiple maps of different types
 * together in the same AssetManager. e.g. TMX, TSJ
 *
 * All .tmx files are parsed and checked for the 'atlas' property to determine if they should be loaded with the AtlasTmxMapLoader
 * otherwise it will be loaded with the normal TmxMapLoader
 */
public class TiledMapLoader extends AsynchronousAssetLoader<TiledMap, TiledMapLoader.Parameters> {

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

	 /** Universal synchronous loader.
	  * This method is a thin wrapper that picks the correct underlying loader
	  * (TMX vs TMJ, atlas vs non‑atlas) and then delegates straight through
	  * to its synchronous {@code load(...)} implementation.
	  * @param fileName  path to a .tmx or .tmj file
	  * @return          a loaded {@link TiledMap}
	  * @throws GdxRuntimeException on unsupported formats or parse errors */
	 public TiledMap load(String fileName) {
		  return load(fileName, new Parameters());
	 }

	 /** Universal synchronous loader with custom parameters.
	  *  Resolves the file and Inspects the extension (tmx vs tmj).
	  *  Check weather the “atlas” property exits in the map and
	  *  delegates to the appropriate loader’s {@code load(...)}.
	  * @param fileName   path to a .tmx or .tmj file
	  * @param parameter  existing Parameters object
	  * @return           a loaded {@link TiledMap}
	  * @throws GdxRuntimeException on unsupported formats or parse errors*/
	 public TiledMap load(String fileName, Parameters parameter) {

		  if (parameter == null) parameter = new Parameters();
		  FileHandle file = resolve(fileName);
		  String extension = file.extension().toLowerCase();
		  if (extension.equals("tmx")) {
				if (usesAtlas(file)) {
					 // atlas‑backed TMX
					 return atlasTmxMapLoader.load(fileName, parameter.getAtlasTmxParameters());
				} else {
					 // plain TMX
					 return tmxMapLoader.load(fileName, parameter.getTmxParameters());
				}
		  } else if (extension.equals("tmj")) {
				if (usesAtlas(file)) {
					 // atlas‑backed TMJ
					 return atlasTmjMapLoader.load(fileName, parameter.getAtlasTmjParameters());
				} else {
					 // plain TMJ
					 return tmjMapLoader.load(fileName, parameter.getTmjParameters());
				}
		  } else {
				// no other formats supported
				throw new GdxRuntimeException("Unsupported map format: '" + extension + "' in file: " + fileName);
		  }
	 }


	 @Override
	 public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, Parameters parameter) {

		  if (parameter == null) parameter = new Parameters();
		  String extension = file.extension().toLowerCase();
		  if (extension.equals("tmx")) {
				if (usesAtlas(file)) {
					 return atlasTmxMapLoader.getDependencies(fileName, file, parameter.getAtlasTmxParameters());
				} else {
					 return tmxMapLoader.getDependencies(fileName, file, parameter.getTmxParameters());
				}
		  } else if (extension.equals("tmj")) {
				return tmjMapLoader.getDependencies(fileName, file, parameter.getTmjParameters());
		  } else {
				throw new IllegalArgumentException("Unsupported map format: " + extension);
		  }
	 }

	 @Override
	 public void loadAsync(AssetManager manager, String fileName, FileHandle file,  Parameters parameter) {

		  if (parameter == null) parameter = new Parameters();
		  String extension = file.extension().toLowerCase();
		  if (extension.equals("tmx")) {
				if (usesAtlas(file)) {
					 atlasTmxMapLoader.loadAsync(manager, fileName, file, parameter.getAtlasTmxParameters());
				} else {
					 tmxMapLoader.loadAsync(manager, fileName, file, parameter.getTmxParameters());
				}
		  } else if (extension.equals("tmj")) {
				tmjMapLoader.loadAsync(manager, fileName, file, parameter.getTmjParameters());
		  } else {
				throw new IllegalArgumentException("Unsupported map format: " + extension);
		  }
	 }

	 @Override
	 public TiledMap loadSync(AssetManager manager, String fileName, FileHandle file, Parameters parameter) {
		  TiledMap map;
		  if (parameter == null) parameter = new Parameters();

		  String extension = file.extension().toLowerCase();
		  if (extension.equals("tmx")) {
				if (usesAtlas(file)) {
					 map = atlasTmxMapLoader.loadSync(manager, fileName, file, parameter.getAtlasTmxParameters());
				} else {
					 map = tmxMapLoader.loadSync(manager, fileName, file, parameter.getTmxParameters());
				}
		  } else if (extension.equals("tmj")) {
				if(usesAtlas(file)){
					 map = atlasTmjMapLoader.loadSync(manager, fileName, file, parameter.getAtlasTmjParameters());
				} else {
					 map = tmjMapLoader.loadSync(manager, fileName, file, parameter.getTmjParameters());
				}
		  } else {
				throw new IllegalArgumentException("Unsupported map format: " + extension);
		  }
		  return map;
	 }


	 private boolean usesAtlas(FileHandle file) {

		  String extension = file.extension().toLowerCase();

		  if (extension.equals("tmx")) {
				XmlReader.Element root = xmlReader.parse(file);
				XmlReader.Element properties = root.getChildByName("properties");
				if (properties != null) {
					 for (XmlReader.Element property : properties.getChildrenByName("property")) {
						  String name = property.getAttribute("name","");
						  if ("atlas".equals(name)) {
								return true;
						  }
					 }
				}
		  }
		  else if (extension.equals("tmj")) {
				JsonValue root = jsonReader.parse(file);
				JsonValue properties = root.get("properties");
				if (properties != null) {
					 for (JsonValue property : properties) {
						  String name = property.getString("name","");
						  if ("atlas".equals(name)) {
								return true;
						  }
					 }
				}
		  }
		  return false;
	 }

	 //TODO: Maybe refactor map parameters inside the atlas maps to be in inside the BaseTiledMapLoader?
	 // Would cause a breaking change but it would be cleaner and worth it in the long run. Also... very few people probably even
	 // seem to be using the atlas map loaders so it wont be that bad.

	 /**
	  * BELOW are 2 Possible ways to handle parameters, (the current class is setup using the second one because I made that first)
	  * The benefits of using MapLoaderParameters instead of the Parameters class is by exposing the actual parameter classes
	  * we will never need to continually update this class if parameters are added in the future.
	  * But it will also be up to the user to make sure they set the properties in the correct map type.
	  * But it is more future proof and maybe looks a bit cleaner	  */



	 /** A class which exposes the parameter objects of all possible map loader types.
	  * Meant to be used with the {@link TiledMapLoader}
	  * Based on what type of map you plan to load.
	  * Also contains convenience setters for any options you want to apply everywhere.
	  * Example Use:
	  * UniversalTiledMapLoader mapLoader = new UniversalTiledMapLoader();
	  * UniversalTiledMapLoader.MapLoaderParameters parameters = new UniversalTiledMapLoader.MapLoaderParameters();
	  *
	  * parameters.tmx.flipY=true;
	  * parameters.tmx.projectFilePath="/folder/somefile.tiled-project";
	  * OR:
	  * parameters.flipY(true).projectFilePath("/folder/somefile.tiled-project");
	  *
	  * mapLoader.load("maps/map.tmx", parameters);
	  *
	  */
	 public static final class MapLoaderParameters {

		  // The parameter objects used by each loader
		  public final TmxMapLoader.Parameters tmx = new TmxMapLoader.Parameters();
		  public final AtlasTmxMapLoader.AtlasTiledMapLoaderParameters atlasTmx = new AtlasTmxMapLoader.AtlasTiledMapLoaderParameters();

		  public final TmjMapLoader.Parameters tmj = new TmjMapLoader.Parameters();
		  public final AtlasTmjMapLoader.AtlasTiledMapLoaderParameters atlasTmj = new AtlasTmjMapLoader.AtlasTiledMapLoaderParameters();

		  // Convenience setters for all parameter objects

		  /** Enable or disable mipmap generation everywhere. */
		  public MapLoaderParameters generateMipMaps(boolean generateMipMaps) {
				tmx.generateMipMaps = generateMipMaps;
				tmj.generateMipMaps = generateMipMaps;
				atlasTmx.generateMipMaps = generateMipMaps;
				atlasTmj.generateMipMaps = generateMipMaps;
				return this;
		  }

		  /** Set the minification filter everywhere. */
		  public MapLoaderParameters minFilter(Texture.TextureFilter textureFilter) {
				tmx.textureMinFilter = textureFilter;
				tmj.textureMinFilter = textureFilter;
				atlasTmx.textureMinFilter = textureFilter;
				atlasTmj.textureMinFilter = textureFilter;
				return this;
		  }

		  /** Set the magnification filter everywhere. */
		  public MapLoaderParameters magFilter(Texture.TextureFilter textureFilter) {
				tmx.textureMagFilter = textureFilter;
				tmj.textureMagFilter = textureFilter;
				atlasTmx.textureMagFilter = textureFilter;
				atlasTmj.textureMagFilter = textureFilter;
				return this;
		  }

		  /** Enable or disable converting object pixel coords to tile‑space everywhere. */
		  public MapLoaderParameters convertObjectToTileSpace(boolean convertObjectToTileSpace) {
				tmx.convertObjectToTileSpace = convertObjectToTileSpace;
				tmj.convertObjectToTileSpace = convertObjectToTileSpace;
				atlasTmx.convertObjectToTileSpace = convertObjectToTileSpace;
				atlasTmj.convertObjectToTileSpace = convertObjectToTileSpace;
				return this;
		  }

		  /** Enable or disable flipping Y coordinates everywhere. */
		  public MapLoaderParameters flipY(boolean flipY) {
				tmx.flipY = flipY;
				tmj.flipY = flipY;
				atlasTmx.flipY = flipY;
				atlasTmj.flipY = flipY;
				return this;
		  }

		  /** Set the project file path everywhere (for class‑property support). */
		  public MapLoaderParameters projectFilePath(String projectFilePath) {
				tmx.projectFilePath = projectFilePath;
				tmj.projectFilePath = projectFilePath;
				atlasTmx.projectFilePath = projectFilePath;
				atlasTmj.projectFilePath = projectFilePath;
				return this;
		  }

		  /** Enable or disable atlas‑texture filtering in the Atlas loaders. */
		  public MapLoaderParameters forceAtlasFilters(boolean forceTextureFilters) {
				atlasTmx.forceTextureFilters = forceTextureFilters;
				atlasTmj.forceTextureFilters = forceTextureFilters;
				return this;
		  }

	 }


	 /** All possible parameters for each map loader this class handles.
	  * Should be represented here, so they can be passed along.
	  * Parameters cover both BaseTiledMapLoader parameters
	  * and the AtlasTmxMapLoaderParameters parameters
	  *
	  * Example use:
	  * UniversalTiledMapLoader mapLoader = new UniversalTiledMapLoader();
	  *
	  * UniversalTiledMapLoader.Parameters parameters = new UniversalTiledMapLoader.Parameters();
	  * parameters.flipY=true;
	  * parameters.projectFilePath="/folder/somefile.tiled-project";
	  *
	  * mapLoader.load("maps/map.tmx", parameters);
	  *
	  * */
	 public static class Parameters extends BaseTiledMapLoader.Parameters {

		  /** Parameters from BaseTiledMapLoader**/
		  /** Generate mipmaps? **/
		  public boolean generateMipMaps = false;
		  /** The TextureFilter to use for minification **/
		  public Texture.TextureFilter textureMinFilter = Texture.TextureFilter.Nearest;
		  /** The TextureFilter to use for magnification **/
		  public Texture.TextureFilter textureMagFilter = Texture.TextureFilter.Nearest;
		  /** Whether to convert the objects' pixel position and size to the equivalent in tile space. **/
		  public boolean convertObjectToTileSpace = false;
		  /** Whether to flip all Y coordinates so that Y positive is up. **/
		  public boolean flipY = true;
		  /** Path to Tiled project file. Needed when using class properties. */
		  public String projectFilePath = null;

		  /** Parameters from AtlasTmxMapLoaderParameters**/
		  /** force texture filters? **/
		  public boolean forceTextureFilters = false;

		  public TmxMapLoader.Parameters getTmxParameters() {
				TmxMapLoader.Parameters tmxParameters = new TmxMapLoader.Parameters();
				// Copy parameters to TmxMapLoader.Parameters
				tmxParameters.generateMipMaps = this.generateMipMaps;
				tmxParameters.textureMinFilter = this.textureMinFilter;
				tmxParameters.textureMagFilter = this.textureMagFilter;
				tmxParameters.convertObjectToTileSpace = this.convertObjectToTileSpace;
				tmxParameters.flipY = this.flipY;
				tmxParameters.projectFilePath = this.projectFilePath;
				return tmxParameters;
		  }

		  public TmjMapLoader.Parameters getTmjParameters() {
				TmjMapLoader.Parameters tmjParameters = new TmjMapLoader.Parameters();
				// Copy parameters to TmjMapLoader.Parameters
				tmjParameters.generateMipMaps = this.generateMipMaps;
				tmjParameters.textureMinFilter = this.textureMinFilter;
				tmjParameters.textureMagFilter = this.textureMagFilter;
				tmjParameters.convertObjectToTileSpace = this.convertObjectToTileSpace;
				tmjParameters.flipY = this.flipY;
				tmjParameters.projectFilePath = this.projectFilePath;
				return tmjParameters;
		  }

		  public AtlasTmxMapLoader.AtlasTiledMapLoaderParameters getAtlasTmxParameters() {
				AtlasTmxMapLoader.AtlasTiledMapLoaderParameters atlasTmxParameters = new AtlasTmxMapLoader.AtlasTiledMapLoaderParameters();
				// Copy parameters to AtlasTiledMapLoaderParameters.Parameters
				atlasTmxParameters.generateMipMaps = this.generateMipMaps;
				atlasTmxParameters.textureMinFilter = this.textureMinFilter;
				atlasTmxParameters.textureMagFilter = this.textureMagFilter;
				atlasTmxParameters.convertObjectToTileSpace = this.convertObjectToTileSpace;
				atlasTmxParameters.flipY = this.flipY;
				atlasTmxParameters.projectFilePath = this.projectFilePath;

				atlasTmxParameters.forceTextureFilters = this.forceTextureFilters;

				return atlasTmxParameters;
		  }

		  public AtlasTmjMapLoader.AtlasTiledMapLoaderParameters getAtlasTmjParameters() {
				AtlasTmjMapLoader.AtlasTiledMapLoaderParameters atlasTmjParameters = new AtlasTmjMapLoader.AtlasTiledMapLoaderParameters();
				// Copy parameters to AtlasTiledMapLoaderParameters.Parameters
				atlasTmjParameters.generateMipMaps = this.generateMipMaps;
				atlasTmjParameters.textureMinFilter = this.textureMinFilter;
				atlasTmjParameters.textureMagFilter = this.textureMagFilter;
				atlasTmjParameters.convertObjectToTileSpace = this.convertObjectToTileSpace;
				atlasTmjParameters.flipY = this.flipY;
				atlasTmjParameters.projectFilePath = this.projectFilePath;

				atlasTmjParameters.forceTextureFilters = this.forceTextureFilters;

				return atlasTmjParameters;
		  }

	 }
}

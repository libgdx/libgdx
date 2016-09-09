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
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.ImageResolver.AssetManagerImageResolver;
import com.badlogic.gdx.maps.ImageResolver.DirectImageResolver;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import java.io.IOException;

/** @brief synchronous loader for TMX maps created with the Tiled tool */
public class TmxMapLoader extends BaseTmxMapLoader<TmxMapLoader.Parameters> {

	public static class Parameters extends BaseTmxMapLoader.Parameters {

	}

	public TmxMapLoader () {
		super(new InternalFileHandleResolver());
	}

	/** Creates loader
	 * 
	 * @param resolver */
	public TmxMapLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	/** Loads the {@link TiledMap} from the given file. The file is resolved via the {@link FileHandleResolver} set in the
	 * constructor of this class. By default it will resolve to an internal file. The map will be loaded for a y-up coordinate
	 * system.
	 * @param fileName the filename
	 * @return the TiledMap */
	public TiledMap load (String fileName) {
		return load(fileName, new TmxMapLoader.Parameters());
	}

	/** Loads the {@link TiledMap} from the given file. The file is resolved via the {@link FileHandleResolver} set in the
	 * constructor of this class. By default it will resolve to an internal file.
	 * @param fileName the filename
	 * @param parameters specifies whether to use y-up, generate mip maps etc.
	 * @return the TiledMap */
	public TiledMap load (String fileName, TmxMapLoader.Parameters parameters) {
		try {
			this.convertObjectToTileSpace = parameters.convertObjectToTileSpace;
			this.flipY = parameters.flipY;
			FileHandle tmxFile = resolve(fileName);
			root = xml.parse(tmxFile);
			ObjectMap<String, Texture> textures = new ObjectMap<String, Texture>();
			Array<FileHandle> textureFiles = loadTilesets(root, tmxFile);
			textureFiles.addAll(loadImages(root, tmxFile));
			
			for (FileHandle textureFile : textureFiles) {
				Texture texture = new Texture(textureFile, parameters.generateMipMaps);
				texture.setFilter(parameters.textureMinFilter, parameters.textureMagFilter);
				textures.put(textureFile.path(), texture);
			}

			DirectImageResolver imageResolver = new DirectImageResolver(textures);
			TiledMap map = loadTilemap(root, tmxFile, imageResolver);
			map.setOwnedResources(textures.values().toArray());
			return map;
		} catch (IOException e) {
			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
		}
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle tmxFile, TmxMapLoader.Parameters parameter) {
		map = null;

		if (parameter != null) {
			convertObjectToTileSpace = parameter.convertObjectToTileSpace;
			flipY = parameter.flipY;
		} else {
			convertObjectToTileSpace = false;
			flipY = true;
		}
		try {
			map = loadTilemap(root, tmxFile, new AssetManagerImageResolver(manager));
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
		}
	}

	@Override
	public TiledMap loadSync (AssetManager manager, String fileName, FileHandle file, TmxMapLoader.Parameters parameter) {
		return map;
	}

	/** Retrieves TiledMap resource dependencies
	 * 
	 * @param fileName
	 * @param parameter not used for now
	 * @return dependencies for the given .tmx file */
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle tmxFile, Parameters parameter) {
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		try {
			root = xml.parse(tmxFile);
			boolean generateMipMaps = (parameter != null ? parameter.generateMipMaps : false);
			TextureLoader.TextureParameter texParams = new TextureParameter();
			texParams.genMipMaps = generateMipMaps;
			if (parameter != null) {
				texParams.minFilter = parameter.textureMinFilter;
				texParams.magFilter = parameter.textureMagFilter;
			}
			for (FileHandle image : loadTilesets(root, tmxFile)) {
				dependencies.add(new AssetDescriptor(image, Texture.class, texParams));
			}
			for (FileHandle image : loadImages(root, tmxFile)) {
				dependencies.add(new AssetDescriptor(image, Texture.class, texParams));
			}
			return dependencies;
		} catch (IOException e) {
			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
		}
	}

	/** Loads the map data, given the XML root element and an {@link ImageResolver} used to return the tileset Textures
	 * @param root the XML root element
	 * @param tmxFile the Filehandle of the tmx file
	 * @param imageResolver the {@link ImageResolver}
	 * @return the {@link TiledMap} */
	protected TiledMap loadTilemap (Element root, FileHandle tmxFile, ImageResolver imageResolver) {
		TiledMap map = new TiledMap();

		String mapOrientation = root.getAttribute("orientation", null);
		int mapWidth = root.getIntAttribute("width", 0);
		int mapHeight = root.getIntAttribute("height", 0);
		int tileWidth = root.getIntAttribute("tilewidth", 0);
		int tileHeight = root.getIntAttribute("tileheight", 0);
		int hexSideLength = root.getIntAttribute("hexsidelength", 0);
		String staggerAxis = root.getAttribute("staggeraxis", null);
		String staggerIndex = root.getAttribute("staggerindex", null);
		String mapBackgroundColor = root.getAttribute("backgroundcolor", null);

		MapProperties mapProperties = map.getProperties();
		if (mapOrientation != null) {
			mapProperties.put("orientation", mapOrientation);
		}
		mapProperties.put("width", mapWidth);
		mapProperties.put("height", mapHeight);
		mapProperties.put("tilewidth", tileWidth);
		mapProperties.put("tileheight", tileHeight);
		mapProperties.put("hexsidelength", hexSideLength);
		if (staggerAxis != null) {
			mapProperties.put("staggeraxis", staggerAxis);
		}
		if (staggerIndex != null) {
			mapProperties.put("staggerindex", staggerIndex);
		}
		if (mapBackgroundColor != null) {
			mapProperties.put("backgroundcolor", mapBackgroundColor);
		}
		mapTileWidth = tileWidth;
		mapTileHeight = tileHeight;
		mapWidthInPixels = mapWidth * tileWidth;
		mapHeightInPixels = mapHeight * tileHeight;

		if (mapOrientation != null) {
			if ("staggered".equals(mapOrientation)) {
				if (mapHeight > 1) {
					mapWidthInPixels += tileWidth / 2;
					mapHeightInPixels = mapHeightInPixels / 2 + tileHeight / 2;
				}
			}
		}
		
		Element properties = root.getChildByName("properties");
		if (properties != null) {
			loadProperties(map.getProperties(), properties);
		}
		Array<Element> tilesets = root.getChildrenByName("tileset");
		for (Element element : tilesets) {
			loadTileSet(map, element, tmxFile, imageResolver);
			root.removeChild(element);
		}
		for (int i = 0, j = root.getChildCount(); i < j; i++) {
			Element element = root.getChild(i);
			String name = element.getName();
			if (name.equals("layer")) {
				loadTileLayer(map, element);
			} else if (name.equals("objectgroup")) {
				loadObjectGroup(map, element);
			}
			else if (name.equals("imagelayer")) {
				loadImageLayer(map, element, tmxFile, imageResolver);
			}
		}
		return map;
	}

	/** Loads the tilesets
	 * @param root the root XML element
	 * @return a list of filenames for images containing tiles
	 * @throws IOException */
	protected Array<FileHandle> loadTilesets (Element root, FileHandle tmxFile) throws IOException {
		Array<FileHandle> images = new Array<FileHandle>();
		for (Element tileset : root.getChildrenByName("tileset")) {
			String source = tileset.getAttribute("source", null);
			if (source != null) {
				FileHandle tsxFile = getRelativeFileHandle(tmxFile, source);
				tileset = xml.parse(tsxFile);
				Element imageElement = tileset.getChildByName("image");
				if (imageElement != null) {
					String imageSource = tileset.getChildByName("image").getAttribute("source");
					FileHandle image = getRelativeFileHandle(tsxFile, imageSource);
					images.add(image);
				} else {
					for (Element tile : tileset.getChildrenByName("tile")) {
						String imageSource = tile.getChildByName("image").getAttribute("source");
						FileHandle image = getRelativeFileHandle(tsxFile, imageSource);
						images.add(image);
					}
				}
			} else {
				Element imageElement = tileset.getChildByName("image");
				if (imageElement != null) {
					String imageSource = tileset.getChildByName("image").getAttribute("source");
					FileHandle image = getRelativeFileHandle(tmxFile, imageSource);
					images.add(image);
				} else {
					for (Element tile : tileset.getChildrenByName("tile")) {
						String imageSource = tile.getChildByName("image").getAttribute("source");
						FileHandle image = getRelativeFileHandle(tmxFile, imageSource);
						images.add(image);
					}
				}
			}
		}
		return images;
	}
	
	/** Loads the images in image layers
	 * @param root the root XML element
	 * @return a list of filenames for images inside image layers
	 * @throws IOException */
	protected Array<FileHandle> loadImages (Element root, FileHandle tmxFile) throws IOException {
		Array<FileHandle> images = new Array<FileHandle>();
		
		for (Element imageLayer : root.getChildrenByName("imagelayer")) {
			Element image = imageLayer.getChildByName("image");
			String source = image.getAttribute("source", null);

			if (source != null) {
				FileHandle handle = getRelativeFileHandle(tmxFile, source);
				
				if (!images.contains(handle, false)) {
					images.add(handle);
				}
			}
		}
		
		return images;
	}

	/** Loads the specified tileset data, adding it to the collection of the specified map, given the XML element, the tmxFile and
	 * an {@link ImageResolver} used to retrieve the tileset Textures.
	 * 
	 * <p>
	 * Default tileset's property keys that are loaded by default are:
	 * </p>
	 * 
	 * <ul>
	 * <li><em>firstgid</em>, (int, defaults to 1) the first valid global id used for tile numbering</li>
	 * <li><em>imagesource</em>, (String, defaults to empty string) the tileset source image filename</li>
	 * <li><em>imagewidth</em>, (int, defaults to 0) the tileset source image width</li>
	 * <li><em>imageheight</em>, (int, defaults to 0) the tileset source image height</li>
	 * <li><em>tilewidth</em>, (int, defaults to 0) the tile width</li>
	 * <li><em>tileheight</em>, (int, defaults to 0) the tile height</li>
	 * <li><em>margin</em>, (int, defaults to 0) the tileset margin</li>
	 * <li><em>spacing</em>, (int, defaults to 0) the tileset spacing</li>
	 * </ul>
	 * 
	 * <p>
	 * The values are extracted from the specified Tmx file, if a value can't be found then the default is used.
	 * </p>
	 * @param map the Map whose tilesets collection will be populated
	 * @param element the XML element identifying the tileset to load
	 * @param tmxFile the Filehandle of the tmx file
	 * @param imageResolver the {@link ImageResolver} */
	protected void loadTileSet (TiledMap map, Element element, FileHandle tmxFile, ImageResolver imageResolver) {
		if (element.getName().equals("tileset")) {
			String name = element.get("name", null);
			int firstgid = element.getIntAttribute("firstgid", 1);
			int tilewidth = element.getIntAttribute("tilewidth", 0);
			int tileheight = element.getIntAttribute("tileheight", 0);
			int spacing = element.getIntAttribute("spacing", 0);
			int margin = element.getIntAttribute("margin", 0);
			String source = element.getAttribute("source", null);

			int offsetX = 0;
			int offsetY = 0;

			String imageSource = "";
			int imageWidth = 0, imageHeight = 0;

			FileHandle image = null;
			if (source != null) {
				FileHandle tsx = getRelativeFileHandle(tmxFile, source);
				try {
					element = xml.parse(tsx);
					name = element.get("name", null);
					tilewidth = element.getIntAttribute("tilewidth", 0);
					tileheight = element.getIntAttribute("tileheight", 0);
					spacing = element.getIntAttribute("spacing", 0);
					margin = element.getIntAttribute("margin", 0);
					Element offset = element.getChildByName("tileoffset");
					if (offset != null) {
						offsetX = offset.getIntAttribute("x", 0);
						offsetY = offset.getIntAttribute("y", 0);
					}
					Element imageElement = element.getChildByName("image");
					if (imageElement != null) {
						imageSource = imageElement.getAttribute("source");
						imageWidth = imageElement.getIntAttribute("width", 0);
						imageHeight = imageElement.getIntAttribute("height", 0);
						image = getRelativeFileHandle(tsx, imageSource);
					}					
				} catch (IOException e) {
					throw new GdxRuntimeException("Error parsing external tileset.");
				}
			} else {
				Element offset = element.getChildByName("tileoffset");
				if (offset != null) {
					offsetX = offset.getIntAttribute("x", 0);
					offsetY = offset.getIntAttribute("y", 0);
				}
				Element imageElement = element.getChildByName("image");
				if (imageElement != null) {
					imageSource = imageElement.getAttribute("source");
					imageWidth = imageElement.getIntAttribute("width", 0);
					imageHeight = imageElement.getIntAttribute("height", 0);
					image = getRelativeFileHandle(tmxFile, imageSource);
				}
			}

			TiledMapTileSet tileset = new TiledMapTileSet();
			tileset.setName(name);
			tileset.getProperties().put("firstgid", firstgid);
			if (image != null) {
				TextureRegion texture = imageResolver.getImage(image.path());
	
				MapProperties props = tileset.getProperties();
				props.put("imagesource", imageSource);
				props.put("imagewidth", imageWidth);
				props.put("imageheight", imageHeight);
				props.put("tilewidth", tilewidth);
				props.put("tileheight", tileheight);
				props.put("margin", margin);
				props.put("spacing", spacing);
	
				int stopWidth = texture.getRegionWidth() - tilewidth;
				int stopHeight = texture.getRegionHeight() - tileheight;
	
				int id = firstgid;
	
				for (int y = margin; y <= stopHeight; y += tileheight + spacing) {
					for (int x = margin; x <= stopWidth; x += tilewidth + spacing) {
						TextureRegion tileRegion = new TextureRegion(texture, x, y, tilewidth, tileheight);
						TiledMapTile tile = new StaticTiledMapTile(tileRegion);
						tile.setId(id);
						tile.setOffsetX(offsetX);
						tile.setOffsetY(flipY ? -offsetY : offsetY);
						tileset.putTile(id++, tile);
					}
				}
			} else {
				Array<Element> tileElements = element.getChildrenByName("tile");
				for (Element tileElement : tileElements) {
					Element imageElement = tileElement.getChildByName("image");
					if (imageElement != null) {
						imageSource = imageElement.getAttribute("source");
						imageWidth = imageElement.getIntAttribute("width", 0);
						imageHeight = imageElement.getIntAttribute("height", 0);
						image = getRelativeFileHandle(tmxFile, imageSource);
					}
					TextureRegion texture = imageResolver.getImage(image.path());
					TiledMapTile tile = new StaticTiledMapTile(texture);
					tile.setId(firstgid + tileElement.getIntAttribute("id"));
					tile.setOffsetX(offsetX);
					tile.setOffsetY(flipY ? -offsetY : offsetY);
					tileset.putTile(tile.getId(), tile);
				}
			}
			Array<Element> tileElements = element.getChildrenByName("tile");

			Array<AnimatedTiledMapTile> animatedTiles = new Array<AnimatedTiledMapTile>();

			for (Element tileElement : tileElements) {
				int localtid = tileElement.getIntAttribute("id", 0);
				TiledMapTile tile = tileset.getTile(firstgid + localtid);
				if (tile != null) {
					Element animationElement = tileElement.getChildByName("animation");
					if (animationElement != null) {

						Array<StaticTiledMapTile> staticTiles = new Array<StaticTiledMapTile>();
						IntArray intervals = new IntArray();
						for (Element frameElement: animationElement.getChildrenByName("frame")) {
							staticTiles.add((StaticTiledMapTile) tileset.getTile(firstgid + frameElement.getIntAttribute("tileid")));
							intervals.add(frameElement.getIntAttribute("duration"));
						}

						AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(intervals, staticTiles);
						animatedTile.setId(tile.getId());
						animatedTiles.add(animatedTile);
						tile = animatedTile;
					}

					String terrain = tileElement.getAttribute("terrain", null);
					if (terrain != null) {
						tile.getProperties().put("terrain", terrain);
					}
					String probability = tileElement.getAttribute("probability", null);
					if (probability != null) {
						tile.getProperties().put("probability", probability);
					}
					Element properties = tileElement.getChildByName("properties");
					if (properties != null) {
						loadProperties(tile.getProperties(), properties);
					}
				}
			}

			for (AnimatedTiledMapTile tile : animatedTiles) {
				tileset.putTile(tile.getId(), tile);
			}

			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(tileset.getProperties(), properties);
			}
			map.getTileSets().addTileSet(tileset);
		}
	}

}

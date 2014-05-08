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

import java.io.IOException;
import java.util.StringTokenizer;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/** A TiledMap Loader which loads tiles from a TextureAtlas instead of separate images.
 * 
 * It requires a map-level property called 'atlas' with its value being the relative path to the TextureAtlas. The atlas must have
 * in it indexed regions named after the tilesets used in the map. The indexes shall be local to the tileset (not the global id).
 * Strip whitespace and rotation should not be used when creating the atlas.
 * 
 * @author Justin Shapcott
 * @author Manuel Bua */
public class AtlasTmxMapLoader extends AsynchronousAssetLoader<TiledMap, AtlasTmxMapLoader.AtlasTiledMapLoaderParameters> {

	public static class AtlasTiledMapLoaderParameters extends AssetLoaderParameters<TiledMap> {
		/** force texture filters? **/
		public boolean forceTextureFilters = false;
		/** The TextureFilter to use for minification, if forceTextureFilter is enabled **/
		public TextureFilter textureMinFilter = TextureFilter.Nearest;
		/** The TextureFilter to use for magnification, if forceTextureFilter is enabled **/
		public TextureFilter textureMagFilter = TextureFilter.Nearest;
		/** Whether to convert the objects' pixel position and size to the equivalent in tile space. **/
		public boolean convertObjectToTileSpace = false;
	}

	protected static final int FLAG_FLIP_HORIZONTALLY = 0x80000000;
	protected static final int FLAG_FLIP_VERTICALLY = 0x40000000;
	protected static final int FLAG_FLIP_DIAGONALLY = 0x20000000;
	protected static final int MASK_CLEAR = 0xE0000000;

	protected XmlReader xml = new XmlReader();
	protected Element root;
	protected boolean convertObjectToTileSpace;

	protected int mapTileWidth;
	protected int mapTileHeight;
	protected int mapWidthInPixels;
	protected int mapHeightInPixels;

	protected TiledMap map;
	protected Array<Texture> trackedTextures = new Array<Texture>();

	private interface AtlasResolver {

		public TextureAtlas getAtlas (String name);

		public static class DirectAtlasResolver implements AtlasResolver {

			private final ObjectMap<String, TextureAtlas> atlases;

			public DirectAtlasResolver (ObjectMap<String, TextureAtlas> atlases) {
				this.atlases = atlases;
			}

			@Override
			public TextureAtlas getAtlas (String name) {
				return atlases.get(name);
			}

		}

		public static class AssetManagerAtlasResolver implements AtlasResolver {
			private final AssetManager assetManager;

			public AssetManagerAtlasResolver (AssetManager assetManager) {
				this.assetManager = assetManager;
			}

			@Override
			public TextureAtlas getAtlas (String name) {
				return assetManager.get(name, TextureAtlas.class);
			}
		}
	}

	public AtlasTmxMapLoader () {
		super(new InternalFileHandleResolver());
	}

	public AtlasTmxMapLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	public TiledMap load (String fileName) {
		return load(fileName, new AtlasTiledMapLoaderParameters());
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle tmxFile, AtlasTiledMapLoaderParameters parameter) {
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		try {
			root = xml.parse(tmxFile);

			Element properties = root.getChildByName("properties");
			if (properties != null) {
				for (Element property : properties.getChildrenByName("property")) {
					String name = property.getAttribute("name");
					String value = property.getAttribute("value");
					if (name.startsWith("atlas")) {
						FileHandle atlasHandle = getRelativeFileHandle(tmxFile, value);
						dependencies.add(new AssetDescriptor(atlasHandle, TextureAtlas.class));
					}
				}
			}
		} catch (IOException e) {
			throw new GdxRuntimeException("Unable to parse .tmx file.");
		}
		return dependencies;
	}

	public TiledMap load (String fileName, AtlasTiledMapLoaderParameters parameter) {
		try {
			if (parameter != null) {
				convertObjectToTileSpace = parameter.convertObjectToTileSpace;
			} else {
				convertObjectToTileSpace = false;
			}

			FileHandle tmxFile = resolve(fileName);
			root = xml.parse(tmxFile);
			ObjectMap<String, TextureAtlas> atlases = new ObjectMap<String, TextureAtlas>();
			FileHandle atlasFile = loadAtlas(root, tmxFile);
			if (atlasFile == null) {
				throw new GdxRuntimeException("Couldn't load atlas");
			}

			TextureAtlas atlas = new TextureAtlas(atlasFile);
			atlases.put(atlasFile.path(), atlas);

			AtlasResolver.DirectAtlasResolver atlasResolver = new AtlasResolver.DirectAtlasResolver(atlases);
			TiledMap map = loadMap(root, tmxFile, atlasResolver, parameter);
			map.setOwnedResources(atlases.values().toArray());
			setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);

			return map;
		} catch (IOException e) {
			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
		}
	}

	/** May return null. */
	protected FileHandle loadAtlas (Element root, FileHandle tmxFile) throws IOException {
		Element e = root.getChildByName("properties");

		if (e != null) {
			for (Element property : e.getChildrenByName("property")) {
				String name = property.getAttribute("name", null);
				String value = property.getAttribute("value", null);
				if (name.equals("atlas")) {
					if (value == null) {
						value = property.getText();
					}

					if (value == null || value.length() == 0) {
						// keep trying until there are no more atlas properties
						continue;
					}

					return getRelativeFileHandle(tmxFile, value);
				}
			}
		} else {
			FileHandle atlasFile = tmxFile.sibling(tmxFile.nameWithoutExtension() + ".atlas");
			return atlasFile.exists() ? atlasFile : null;
		}

		return null;
	}

	private void setTextureFilters (TextureFilter min, TextureFilter mag) {
		for (Texture texture : trackedTextures) {
			texture.setFilter(min, mag);
		}
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle tmxFile, AtlasTiledMapLoaderParameters parameter) {
		map = null;

		if (parameter != null) {
			convertObjectToTileSpace = parameter.convertObjectToTileSpace;
		} else {
			convertObjectToTileSpace = false;
		}

		try {
			map = loadMap(root, tmxFile, new AtlasResolver.AssetManagerAtlasResolver(manager), parameter);
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
		}
	}

	@Override
	public TiledMap loadSync (AssetManager manager, String fileName, FileHandle file, AtlasTiledMapLoaderParameters parameter) {
		if (parameter != null) {
			setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
		}

		return map;
	}

	protected TiledMap loadMap (Element root, FileHandle tmxFile, AtlasResolver resolver, AtlasTiledMapLoaderParameters parameter) {
		TiledMap map = new TiledMap();

		String mapOrientation = root.getAttribute("orientation", null);
		int mapWidth = root.getIntAttribute("width", 0);
		int mapHeight = root.getIntAttribute("height", 0);
		int tileWidth = root.getIntAttribute("tilewidth", 0);
		int tileHeight = root.getIntAttribute("tileheight", 0);
		String mapBackgroundColor = root.getAttribute("backgroundcolor", null);

		MapProperties mapProperties = map.getProperties();
		if (mapOrientation != null) {
			mapProperties.put("orientation", mapOrientation);
		}
		mapProperties.put("width", mapWidth);
		mapProperties.put("height", mapHeight);
		mapProperties.put("tilewidth", tileWidth);
		mapProperties.put("tileheight", tileHeight);
		if (mapBackgroundColor != null) {
			mapProperties.put("backgroundcolor", mapBackgroundColor);
		}

		mapTileWidth = tileWidth;
		mapTileHeight = tileHeight;
		mapWidthInPixels = mapWidth * tileWidth;
		mapHeightInPixels = mapHeight * tileHeight;

		for (int i = 0, j = root.getChildCount(); i < j; i++) {
			Element element = root.getChild(i);
			String elementName = element.getName();
			if (elementName.equals("properties")) {
				loadProperties(map.getProperties(), element);
			} else if (elementName.equals("tileset")) {
				loadTileset(map, element, tmxFile, resolver, parameter);
			} else if (elementName.equals("layer")) {
				loadTileLayer(map, element);
			} else if (elementName.equals("objectgroup")) {
				loadObjectGroup(map, element);
			}
		}
		return map;
	}

	protected void loadTileset (TiledMap map, Element element, FileHandle tmxFile, AtlasResolver resolver,
		AtlasTiledMapLoaderParameters parameter) {
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
					imageSource = imageElement.getAttribute("source");
					imageWidth = imageElement.getIntAttribute("width", 0);
					imageHeight = imageElement.getIntAttribute("height", 0);
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
				}
			}

			String atlasFilePath = map.getProperties().get("atlas", String.class);
			if (atlasFilePath == null) {
				FileHandle atlasFile = tmxFile.sibling(tmxFile.nameWithoutExtension() + ".atlas");
				if (atlasFile.exists()) atlasFilePath = atlasFile.name();
			}
			if (atlasFilePath == null) {
				throw new GdxRuntimeException("The map is missing the 'atlas' property");
			}

			// get the TextureAtlas for this tileset
			FileHandle atlasHandle = getRelativeFileHandle(tmxFile, atlasFilePath);
			atlasHandle = resolve(atlasHandle.path());
			TextureAtlas atlas = resolver.getAtlas(atlasHandle.path());
			String regionsName = atlasHandle.nameWithoutExtension();

			if (parameter != null && parameter.forceTextureFilters) {
				for (Texture texture : atlas.getTextures()) {
					trackedTextures.add(texture);
				}
			}

			TiledMapTileSet tileset = new TiledMapTileSet();
			MapProperties props = tileset.getProperties();
			tileset.setName(name);
			props.put("firstgid", firstgid);
			props.put("imagesource", imageSource);
			props.put("imagewidth", imageWidth);
			props.put("imageheight", imageHeight);
			props.put("tilewidth", tilewidth);
			props.put("tileheight", tileheight);
			props.put("margin", margin);
			props.put("spacing", spacing);

			for (AtlasRegion region : atlas.findRegions(regionsName)) {
				// handle unused tile ids
				if (region != null) {
					StaticTiledMapTile tile = new StaticTiledMapTile(region);
					int tileid = firstgid + region.index;
					tile.setId(tileid);
					tile.setOffsetX(offsetX);
					tile.setOffsetY(-offsetY);
					tileset.putTile(tileid, tile);
				}
			}

			for (Element tileElement : element.getChildrenByName("tile")) {
				int tileid = firstgid + tileElement.getIntAttribute("id", 0);
				TiledMapTile tile = tileset.getTile(tileid);
				if (tile == null) {
					Element imageElement = tileElement.getChildByName("image");
					if (imageElement != null) {
						// Is a tilemap with individual images.
						String regionName = imageElement.getAttribute("source");
						regionName = regionName.substring(0, regionName.lastIndexOf('.'));
						AtlasRegion region = atlas.findRegion(regionName);
						if (region == null) throw new GdxRuntimeException("Tileset region not found: " + regionName);
						tile = new StaticTiledMapTile(region);
						tile.setId(tileid);
						tile.setOffsetX(offsetX);
						tile.setOffsetY(-offsetY);
						tileset.putTile(tileid, tile);
					}
				}
				if (tile != null) {
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

			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(tileset.getProperties(), properties);
			}
			map.getTileSets().addTileSet(tileset);
		}
	}

	protected void loadTileLayer (TiledMap map, Element element) {
		if (element.getName().equals("layer")) {
			String name = element.getAttribute("name", null);
			int width = element.getIntAttribute("width", 0);
			int height = element.getIntAttribute("height", 0);
			int tileWidth = element.getParent().getIntAttribute("tilewidth", 0);
			int tileHeight = element.getParent().getIntAttribute("tileheight", 0);
			boolean visible = element.getIntAttribute("visible", 1) == 1;
			float opacity = element.getFloatAttribute("opacity", 1.0f);
			TiledMapTileLayer layer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
			layer.setVisible(visible);
			layer.setOpacity(opacity);
			layer.setName(name);

			int[] ids = TmxMapHelper.getTileIds(element, width, height);
			TiledMapTileSets tilesets = map.getTileSets();
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int id = ids[y * width + x];
					boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
					boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);
					boolean flipDiagonally = ((id & FLAG_FLIP_DIAGONALLY) != 0);

					TiledMapTile tile = tilesets.getTile(id & ~MASK_CLEAR);
					if (tile != null) {
						Cell cell = createTileLayerCell(flipHorizontally, flipVertically, flipDiagonally);
						cell.setTile(tile);
						layer.setCell(x, height - 1 - y, cell);
					}
				}
			}

			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(layer.getProperties(), properties);
			}
			map.getLayers().add(layer);
		}
	}

	protected void loadObjectGroup (TiledMap map, Element element) {
		if (element.getName().equals("objectgroup")) {
			String name = element.getAttribute("name", null);
			MapLayer layer = new MapLayer();
			layer.setName(name);
			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(layer.getProperties(), properties);
			}

			for (Element objectElement : element.getChildrenByName("object")) {
				loadObject(layer, objectElement);
			}

			map.getLayers().add(layer);
		}
	}

	protected void loadObject (MapLayer layer, Element element) {
		if (element.getName().equals("object")) {
			MapObject object = null;

			float scaleX = convertObjectToTileSpace ? 1.0f / mapTileWidth : 1.0f;
			float scaleY = convertObjectToTileSpace ? 1.0f / mapTileHeight : 1.0f;

			float x = element.getFloatAttribute("x", 0) * scaleX;
			float y = (mapHeightInPixels - element.getFloatAttribute("y", 0)) * scaleY;

			float width = element.getFloatAttribute("width", 0) * scaleX;
			float height = element.getFloatAttribute("height", 0) * scaleY;

			if (element.getChildCount() > 0) {
				Element child = null;
				if ((child = element.getChildByName("polygon")) != null) {
					String[] points = child.getAttribute("points").split(" ");
					float[] vertices = new float[points.length * 2];
					for (int i = 0; i < points.length; i++) {
						String[] point = points[i].split(",");
						vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
						vertices[i * 2 + 1] = -Float.parseFloat(point[1]) * scaleY;
					}
					Polygon polygon = new Polygon(vertices);
					polygon.setPosition(x, y);
					object = new PolygonMapObject(polygon);
				} else if ((child = element.getChildByName("polyline")) != null) {
					String[] points = child.getAttribute("points").split(" ");
					float[] vertices = new float[points.length * 2];
					for (int i = 0; i < points.length; i++) {
						String[] point = points[i].split(",");
						vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
						vertices[i * 2 + 1] = -Float.parseFloat(point[1]) * scaleY;
					}
					Polyline polyline = new Polyline(vertices);
					polyline.setPosition(x, y);
					object = new PolylineMapObject(polyline);
				} else if ((child = element.getChildByName("ellipse")) != null) {
					object = new EllipseMapObject(x, y - height, width, height);
				}
			}
			if (object == null) {
				object = new RectangleMapObject(x, y - height, width, height);
			}
			object.setName(element.getAttribute("name", null));
			String type = element.getAttribute("type", null);
			if (type != null) {
				object.getProperties().put("type", type);
			}
			int gid = element.getIntAttribute("gid", -1);
			if (gid != -1) {
				object.getProperties().put("gid", gid);
			}
			object.getProperties().put("x", x * scaleX);
			object.getProperties().put("y", (y - height) * scaleY);
			object.setVisible(element.getIntAttribute("visible", 1) == 1);
			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(object.getProperties(), properties);
			}
			layer.getObjects().add(object);
		}
	}

	protected void loadProperties (MapProperties properties, Element element) {
		if (element.getName().equals("properties")) {
			for (Element property : element.getChildrenByName("property")) {
				String name = property.getAttribute("name", null);
				String value = property.getAttribute("value", null);
				if (value == null) {
					value = property.getText();
				}
				properties.put(name, value);
			}
		}
	}

	protected Cell createTileLayerCell (boolean flipHorizontally, boolean flipVertically, boolean flipDiagonally) {
		Cell cell = new Cell();
		if (flipDiagonally) {
			if (flipHorizontally && flipVertically) {
				cell.setFlipHorizontally(true);
				cell.setRotation(Cell.ROTATE_270);
			} else if (flipHorizontally) {
				cell.setRotation(Cell.ROTATE_270);
			} else if (flipVertically) {
				cell.setRotation(Cell.ROTATE_90);
			} else {
				cell.setFlipVertically(true);
				cell.setRotation(Cell.ROTATE_270);
			}
		} else {
			cell.setFlipHorizontally(flipHorizontally);
			cell.setFlipVertically(flipVertically);
		}
		return cell;
	}

	public static FileHandle getRelativeFileHandle (FileHandle file, String path) {
		StringTokenizer tokenizer = new StringTokenizer(path, "\\/");
		FileHandle result = file.parent();
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			if (token.equals(".."))
				result = result.parent();
			else {
				result = result.child(token);
			}
		}
		return result;
	}
}

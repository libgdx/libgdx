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
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.utils.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public abstract class BaseTmjMapLoader<P extends BaseTiledMapLoader.Parameters> extends BaseTiledMapLoader<P> {

	protected JsonReader json = new JsonReader();
	protected JsonValue root;

	public BaseTmjMapLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle tmjFile, P parameter) {
		this.root = json.parse(tmjFile);

		TextureLoader.TextureParameter textureParameter = new TextureLoader.TextureParameter();
		if (parameter != null) {
			textureParameter.genMipMaps = parameter.generateMipMaps;
			textureParameter.minFilter = parameter.textureMinFilter;
			textureParameter.magFilter = parameter.textureMagFilter;
		}

		return getDependencyAssetDescriptors(tmjFile, textureParameter);
	}

	/** Loads the map data, given the JSON root element
	 *
	 * @param tmjFile the Filehandle of the tmj file
	 * @param parameter
	 * @param imageResolver
	 * @return the {@link TiledMap} */
	protected TiledMap loadTiledMap (FileHandle tmjFile, P parameter, ImageResolver imageResolver) {
		this.map = new TiledMap();
		this.idToObject = new IntMap<>();
		this.runOnEndOfLoadTiled = new Array<>();

		if (parameter != null) {
			this.convertObjectToTileSpace = parameter.convertObjectToTileSpace;
			this.flipY = parameter.flipY;
			loadProjectFile(parameter.projectFilePath);
		} else {
			this.convertObjectToTileSpace = false;
			this.flipY = true;
		}
		String mapOrientation = root.getString("orientation", null);
		int mapWidth = root.getInt("width", 0);
		int mapHeight = root.getInt("height", 0);
		int tileWidth = root.getInt("tilewidth", 0);
		int tileHeight = root.getInt("tileheight", 0);
		int hexSideLength = root.getInt("hexsidelength", 0);
		String staggerAxis = root.getString("staggeraxis", null);
		String staggerIndex = root.getString("staggerindex", null);
		String mapBackgroundColor = root.getString("backgroundcolor", null);

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
		this.mapTileWidth = tileWidth;
		this.mapTileHeight = tileHeight;
		this.mapWidthInPixels = mapWidth * tileWidth;
		this.mapHeightInPixels = mapHeight * tileHeight;

		if (mapOrientation != null) {
			if ("staggered".equals(mapOrientation)) {
				if (mapHeight > 1) {
					this.mapWidthInPixels += tileWidth / 2;
					this.mapHeightInPixels = mapHeightInPixels / 2 + tileHeight / 2;
				}
			}
		}

		JsonValue properties = root.get("properties");
		if (properties != null) {
			loadProperties(map.getProperties(), properties);
		}

		JsonValue tileSets = root.get("tilesets");
		for (JsonValue element : tileSets) {
			loadTileSet(element, tmjFile, imageResolver);

		}
		JsonValue layers = root.get("layers");

		for (JsonValue element : layers) {
			loadLayer(map, map.getLayers(), element, tmjFile, imageResolver);
		}

		// update hierarchical parallax scrolling factors
		// in Tiled the final parallax scrolling factor of a layer is the multiplication of its factor with all its parents
		// 1) get top level groups
		final Array<MapGroupLayer> groups = map.getLayers().getByType(MapGroupLayer.class);
		while (groups.notEmpty()) {
			final MapGroupLayer group = groups.first();
			groups.removeIndex(0);

			for (MapLayer child : group.getLayers()) {
				child.setParallaxX(child.getParallaxX() * group.getParallaxX());
				child.setParallaxY(child.getParallaxY() * group.getParallaxY());
				if (child instanceof MapGroupLayer) {
					// 2) handle any child groups
					groups.add((MapGroupLayer)child);
				}
			}
		}

		for (Runnable runnable : runOnEndOfLoadTiled) {
			runnable.run();
		}
		runOnEndOfLoadTiled = null;

		return map;
	}

	protected void loadLayer (TiledMap map, MapLayers parentLayers, JsonValue element, FileHandle tmjFile,
		ImageResolver imageResolver) {
		String type = element.getString("type", "");
		switch (type) {
		case "group":
			loadLayerGroup(map, parentLayers, element, tmjFile, imageResolver);
			break;
		case "tilelayer":
			loadTileLayer(map, parentLayers, element);
			break;
		case "objectgroup":
			loadObjectGroup(map, parentLayers, element);
			break;
		case "imagelayer":
			loadImageLayer(map, parentLayers, element, tmjFile, imageResolver);
			break;
		}
	}

	protected void loadLayerGroup (TiledMap map, MapLayers parentLayers, JsonValue element, FileHandle tmjFile,
		ImageResolver imageResolver) {
		if (element.getString("type", "").equals("group")) {
			MapGroupLayer groupLayer = new MapGroupLayer();
			loadBasicLayerInfo(groupLayer, element);

			JsonValue properties = element.get("properties");
			if (properties != null) {
				loadProperties(groupLayer.getProperties(), properties);
			}

			JsonValue layers = element.get("layers");
			if (layers != null) {
				for (JsonValue child : layers) {
					loadLayer(map, groupLayer.getLayers(), child, tmjFile, imageResolver);
				}
			}

			for (MapLayer layer : groupLayer.getLayers()) {
				layer.setParent(groupLayer);
			}

			parentLayers.add(groupLayer);
		}
	}

	protected void loadTileLayer (TiledMap map, MapLayers parentLayers, JsonValue element) {

		if (element.getString("type", "").equals("tilelayer")) {
			int width = element.getInt("width", 0);
			int height = element.getInt("height", 0);
			int tileWidth = map.getProperties().get("tilewidth", Integer.class);
			int tileHeight = map.getProperties().get("tileheight", Integer.class);
			TiledMapTileLayer layer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);

			loadBasicLayerInfo(layer, element);

			int[] ids = getTileIds(element, width, height);
			TiledMapTileSets tileSets = map.getTileSets();
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int id = ids[y * width + x];
					boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
					boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);
					boolean flipDiagonally = ((id & FLAG_FLIP_DIAGONALLY) != 0);

					TiledMapTile tile = tileSets.getTile(id & ~MASK_CLEAR);
					if (tile != null) {
						Cell cell = createTileLayerCell(flipHorizontally, flipVertically, flipDiagonally);
						cell.setTile(tile);
						layer.setCell(x, flipY ? height - 1 - y : y, cell);
					}
				}
			}
			JsonValue properties = element.get("properties");
			if (properties != null) {
				loadProperties(layer.getProperties(), properties);
			}
			parentLayers.add(layer);
		}
	}

	protected void loadObjectGroup (TiledMap map, MapLayers parentLayers, JsonValue element) {
		if (element.getString("type", "").equals("objectgroup")) {
			MapLayer layer = new MapLayer();
			loadBasicLayerInfo(layer, element);
			JsonValue properties = element.get("properties");
			if (properties != null) {
				loadProperties(layer.getProperties(), properties);
			}

			for (JsonValue objectElement : element.get("objects")) {
				loadObject(map, layer, objectElement);
			}

			parentLayers.add(layer);
		}
	}

	protected void loadImageLayer (TiledMap map, MapLayers parentLayers, JsonValue element, FileHandle tmjFile,
		ImageResolver imageResolver) {
		if (element.getString("type", "").equals("imagelayer")) {
			float x = element.getFloat("offsetx", 0);
			float y = element.getFloat("offsety", 0);
			if (flipY) y = mapHeightInPixels - y;

			String imageSrc = element.getString("image", "");

			boolean repeatX = element.getInt("repeatx", 0) == 1;
			boolean repeatY = element.getInt("repeaty", 0) == 1;

			TextureRegion texture = null;

			if (!imageSrc.isEmpty()) {
				FileHandle handle = getRelativeFileHandle(tmjFile, imageSrc);
				texture = imageResolver.getImage(handle.path());
				y -= texture.getRegionHeight();
			}

			TiledMapImageLayer layer = new TiledMapImageLayer(texture, x, y, repeatX, repeatY);

			loadBasicLayerInfo(layer, element);

			JsonValue properties = element.get("properties");
			if (properties != null) {
				loadProperties(layer.getProperties(), properties);
			}

			parentLayers.add(layer);
		}
	}

	protected void loadBasicLayerInfo (MapLayer layer, JsonValue element) {
		String name = element.getString("name");
		float opacity = element.getFloat("opacity", 1.0f);
		String tintColor = element.getString("tintcolor", "#ffffffff");
		boolean visible = element.getBoolean("visible", true);
		float offsetX = element.getFloat("offsetx", 0);
		float offsetY = element.getFloat("offsety", 0);
		float parallaxX = element.getFloat("parallaxx", 1f);
		float parallaxY = element.getFloat("parallaxy", 1f);

		layer.setName(name);
		layer.setOpacity(opacity);
		layer.setVisible(visible);
		layer.setOffsetX(offsetX);
		layer.setOffsetY(offsetY);
		layer.setParallaxX(parallaxX);
		layer.setParallaxY(parallaxY);

		// set layer tint color after converting from #AARRGGBB to #RRGGBBAA
		layer.setTintColor(Color.valueOf(tiledColorToLibGDXColor(tintColor)));
	}

	protected void loadObject (TiledMap map, MapLayer layer, JsonValue element) {
		loadObject(map, layer.getObjects(), element, mapHeightInPixels);
	}

	protected void loadObject (TiledMap map, TiledMapTile tile, JsonValue element) {
		loadObject(map, tile.getObjects(), element, tile.getTextureRegion().getRegionHeight());
	}

	protected void loadObject (TiledMap map, MapObjects objects, JsonValue element, float heightInPixels) {

		MapObject object = null;

		float scaleX = convertObjectToTileSpace ? 1.0f / mapTileWidth : 1.0f;
		float scaleY = convertObjectToTileSpace ? 1.0f / mapTileHeight : 1.0f;

		float x = element.getFloat("x", 0) * scaleX;
		float y = (flipY ? (heightInPixels - element.getFloat("y", 0)) : element.getFloat("y", 0)) * scaleY;

		float width = element.getFloat("width", 0) * scaleX;
		float height = element.getFloat("height", 0) * scaleY;

		JsonValue child;
		if ((child = element.get("polygon")) != null) {
			float[] vertices = new float[child.size * 2];
			int index = 0;
			for (JsonValue point : child) {
				// Apply scale and flip transformations
				vertices[index++] = point.getFloat("x", 0) * scaleX; // Scaled X
				vertices[index++] = point.getFloat("y", 0) * scaleY * (flipY ? -1 : 1); // Scaled/flipped Y
			}
			Polygon polygon = new Polygon(vertices);
			polygon.setPosition(x, y);
			object = new PolygonMapObject(polygon);
		} else if ((child = element.get("polyline")) != null) {
			float[] vertices = new float[child.size * 2];
			int index = 0;
			for (JsonValue point : child) {
				// Apply scale and flip transformations
				vertices[index++] = point.getFloat("x", 0) * scaleX; // Scaled X
				vertices[index++] = point.getFloat("y", 0) * scaleY * (flipY ? -1 : 1); // Scaled/flipped Y
			}
			Polyline polyline = new Polyline(vertices);
			polyline.setPosition(x, y);
			object = new PolylineMapObject(polyline);
		} else if (element.get("ellipse") != null) {
			object = new EllipseMapObject(x, flipY ? y - height : y, width, height);
		}

		if (object == null) {
			String gid;
			if ((gid = element.getString("gid", null)) != null) {
				int id = (int)Long.parseLong(gid);
				boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
				boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);

				TiledMapTile tile = map.getTileSets().getTile(id & ~MASK_CLEAR);
				TiledMapTileMapObject tiledMapTileMapObject = new TiledMapTileMapObject(tile, flipHorizontally, flipVertically);
				TextureRegion textureRegion = tiledMapTileMapObject.getTextureRegion();
				tiledMapTileMapObject.getProperties().put("gid", id);
				tiledMapTileMapObject.setX(x);
				tiledMapTileMapObject.setY(flipY ? y : y - height);
				float objectWidth = element.getFloat("width", textureRegion.getRegionWidth());
				float objectHeight = element.getFloat("height", textureRegion.getRegionHeight());
				tiledMapTileMapObject.setScaleX(scaleX * (objectWidth / textureRegion.getRegionWidth()));
				tiledMapTileMapObject.setScaleY(scaleY * (objectHeight / textureRegion.getRegionHeight()));
				tiledMapTileMapObject.setRotation(element.getFloat("rotation", 0));
				object = tiledMapTileMapObject;
			} else {
				object = new RectangleMapObject(x, flipY ? y - height : y, width, height);
			}
		}
		object.setName(element.getString("name", null));
		String rotation = element.getString("rotation", null);
		if (rotation != null) {
			object.getProperties().put("rotation", Float.parseFloat(rotation));
		}
		String type = element.getString("type", null);
		if (type != null) {
			object.getProperties().put("type", type);
		}
		int id = element.getInt("id", 0);
		if (id != 0) {
			object.getProperties().put("id", id);
		}
		object.getProperties().put("x", x);

		if (object instanceof TiledMapTileMapObject) {
			object.getProperties().put("y", y);
		} else {
			object.getProperties().put("y", (flipY ? y - height : y));
		}
		object.getProperties().put("width", width);
		object.getProperties().put("height", height);
		object.setVisible(element.getBoolean("visible", true));
		JsonValue properties = element.get("properties");
		if (properties != null) {
			loadProperties(object.getProperties(), properties);
		}
		idToObject.put(id, object);
		objects.add(object);

	}

	private void loadProperties (final MapProperties properties, JsonValue element) {
		if (element == null || !"properties".equals(element.name())) return;

		for (JsonValue property : element) {
			final String name = property.getString("name", null);
			String value = property.getString("value", null);
			String type = property.getString("type", null);
			if (value == null && !"class".equals(type)) {
				value = property.asString();
			}
			switch (type) {
			case "object":
				loadObjectProperty(properties, name, value);
				break;
			case "class":
				// A 'class' property is a property which is itself a set of properties
				MapProperties classProperties = new MapProperties();
				String className = property.getString("propertytype");
				classProperties.put("type", className);
				// the actual properties of a 'class' property are stored as a new properties tag
				properties.put(name, classProperties);
				loadJsonClassProperties(className, classProperties, property.get("value"));
				break;
			default:
				loadBasicProperty(properties, name, value, type);
				break;
			}
		}
	}

	static public int[] getTileIds (JsonValue element, int width, int height) {
		JsonValue data = element.get("data");
		String encoding = element.getString("encoding", null);

		int[] ids;
		if (encoding == null || encoding.isEmpty() || encoding.equals("csv")) {
			ids = data.asIntArray();
		} else if (encoding.equals("base64")) {
			InputStream is = null;
			try {
				String compression = element.getString("compression", null);
				byte[] bytes = Base64Coder.decode(data.asString());
				if (compression == null || compression.isEmpty())
					is = new ByteArrayInputStream(bytes);
				else if (compression.equals("gzip"))
					is = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes), bytes.length));
				else if (compression.equals("zlib"))
					is = new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(bytes)));
				else
					throw new GdxRuntimeException("Unrecognised compression (" + compression + ") for TMJ Layer Data");

				byte[] temp = new byte[4];
				ids = new int[width * height];
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int read = is.read(temp);
						while (read < temp.length) {
							int curr = is.read(temp, read, temp.length - read);
							if (curr == -1) break;
							read += curr;
						}
						if (read != temp.length)
							throw new GdxRuntimeException("Error Reading TMJ Layer Data: Premature end of tile data");
						ids[y * width + x] = unsignedByteToInt(temp[0]) | unsignedByteToInt(temp[1]) << 8
							| unsignedByteToInt(temp[2]) << 16 | unsignedByteToInt(temp[3]) << 24;
					}
				}
			} catch (IOException e) {
				throw new GdxRuntimeException("Error Reading TMJ Layer Data - IOException: " + e.getMessage());
			} finally {
				StreamUtils.closeQuietly(is);
			}
		} else {
			// any other value of 'encoding' is one we're not aware of, probably a feature of a future version of Tiled
			// or another editor
			throw new GdxRuntimeException("Unrecognised encoding (" + encoding + ") for TMJ Layer Data");
		}

		return ids;
	}

	protected void loadTileSet (JsonValue element, FileHandle tmjFile, ImageResolver imageResolver) {
		if (element.getString("firstgid") != null) {
			int firstgid = element.getInt("firstgid", 1);
			String imageSource = "";
			int imageWidth = 0;
			int imageHeight = 0;
			FileHandle image = null;

			String source = element.getString("source", null);
			if (source != null) {
				FileHandle tsj = getRelativeFileHandle(tmjFile, source);
				try {
					element = json.parse(tsj);
					if (element.has("image")) {
						imageSource = element.getString("image");
						imageWidth = element.getInt("imagewidth", 0);
						imageHeight = element.getInt("imageheight", 0);
						image = getRelativeFileHandle(tsj, imageSource);
					}
				} catch (SerializationException e) {
					throw new GdxRuntimeException("Error parsing external tileSet.");
				}
			} else {
				if (element.has("image")) {
					imageSource = element.getString("image");
					imageWidth = element.getInt("imagewidth", 0);
					imageHeight = element.getInt("imageheight", 0);
					image = getRelativeFileHandle(tmjFile, imageSource);
				}
			}
			String name = element.getString("name", null);
			int tilewidth = element.getInt("tilewidth", 0);
			int tileheight = element.getInt("tileheight", 0);
			int spacing = element.getInt("spacing", 0);
			int margin = element.getInt("margin", 0);

			JsonValue offset = element.get("tileoffset");
			int offsetX = 0;
			int offsetY = 0;
			if (offset != null) {
				offsetX = offset.getInt("x", 0);
				offsetY = offset.getInt("y", 0);
			}
			TiledMapTileSet tileSet = new TiledMapTileSet();

			// TileSet
			tileSet.setName(name);
			final MapProperties tileSetProperties = tileSet.getProperties();
			JsonValue properties = element.get("properties");
			if (properties != null) {
				loadProperties(tileSetProperties, properties);
			}
			tileSetProperties.put("firstgid", firstgid);

			// Tiles
			JsonValue tiles = element.get("tiles");

			if (tiles == null) {
				tiles = new JsonValue(JsonValue.ValueType.array);
			}

			addStaticTiles(tmjFile, imageResolver, tileSet, element, tiles, name, firstgid, tilewidth, tileheight, spacing, margin,
				source, offsetX, offsetY, imageSource, imageWidth, imageHeight, image);

			Array<AnimatedTiledMapTile> animatedTiles = new Array<>();

			for (JsonValue tileElement : tiles) {
				int localtid = tileElement.getInt("id", 0);
				TiledMapTile tile = tileSet.getTile(firstgid + localtid);
				if (tile != null) {
					AnimatedTiledMapTile animatedTile = createAnimatedTile(tileSet, tile, tileElement, firstgid);
					if (animatedTile != null) {
						animatedTiles.add(animatedTile);
						tile = animatedTile;
					}
					addTileProperties(tile, tileElement);
					addTileObjectGroup(tile, tileElement);
				}
			}
			// replace original static tiles by animated tiles
			for (AnimatedTiledMapTile animatedTile : animatedTiles) {
				tileSet.putTile(animatedTile.getId(), animatedTile);
			}

			map.getTileSets().addTileSet(tileSet);

		}
	}

	protected abstract void addStaticTiles (FileHandle tmjFile, ImageResolver imageResolver, TiledMapTileSet tileSet,
		JsonValue element, JsonValue tiles, String name, int firstgid, int tilewidth, int tileheight, int spacing, int margin,
		String source, int offsetX, int offsetY, String imageSource, int imageWidth, int imageHeight, FileHandle image);

	private void addTileProperties (TiledMapTile tile, JsonValue tileElement) {
		String terrain = tileElement.getString("terrain", null);
		if (terrain != null) {
			tile.getProperties().put("terrain", terrain);
		}
		String probability = tileElement.getString("probability", null);
		if (probability != null) {
			tile.getProperties().put("probability", probability);
		}
		String type = tileElement.getString("type", null);
		if (type != null) {
			tile.getProperties().put("type", type);
		}
		JsonValue properties = tileElement.get("properties");
		if (properties != null) {
			loadProperties(tile.getProperties(), properties);
		}
	}

	private void addTileObjectGroup (TiledMapTile tile, JsonValue tileElement) {
		JsonValue objectgroupElement = tileElement.get("objectgroup");
		if (objectgroupElement != null) {
			for (JsonValue objectElement : objectgroupElement.get("objects")) {
				loadObject(this.map, tile, objectElement);
			}
		}
	}

	private AnimatedTiledMapTile createAnimatedTile (TiledMapTileSet tileSet, TiledMapTile tile, JsonValue tileElement,
		int firstgid) {
		JsonValue animationElement = tileElement.get("animation");
		if (animationElement != null) {
			Array<StaticTiledMapTile> staticTiles = new Array<>();
			IntArray intervals = new IntArray();
			for (JsonValue frameValue : animationElement) {
				staticTiles.add((StaticTiledMapTile)tileSet.getTile(firstgid + frameValue.getInt("tileid")));
				intervals.add(frameValue.getInt("duration"));
			}

			AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(intervals, staticTiles);
			animatedTile.setId(tile.getId());
			return animatedTile;
		}
		return null;
	}

}

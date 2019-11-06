
package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
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
import com.badlogic.gdx.utils.XmlReader.Element;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public abstract class BaseTmxMapLoader<P extends BaseTmxMapLoader.Parameters> extends AsynchronousAssetLoader<TiledMap, P> {

	public static class Parameters extends AssetLoaderParameters<TiledMap> {
		/** generate mipmaps? **/
		public boolean generateMipMaps = false;
		/** The TextureFilter to use for minification **/
		public TextureFilter textureMinFilter = TextureFilter.Nearest;
		/** The TextureFilter to use for magnification **/
		public TextureFilter textureMagFilter = TextureFilter.Nearest;
		/** Whether to convert the objects' pixel position and size to the equivalent in tile space. **/
		public boolean convertObjectToTileSpace = false;
		/** Whether to flip all Y coordinates so that Y positive is up. All LibGDX renderers require flipped Y coordinates, and
		 * thus flipY set to true. This parameter is included for non-rendering related purposes of TMX files, or custom renderers. */
		public boolean flipY = true;
	}

	protected static final int FLAG_FLIP_HORIZONTALLY = 0x80000000;
	protected static final int FLAG_FLIP_VERTICALLY = 0x40000000;
	protected static final int FLAG_FLIP_DIAGONALLY = 0x20000000;
	protected static final int MASK_CLEAR = 0xE0000000;

	protected XmlReader xml = new XmlReader();
	protected Element root;
	protected boolean convertObjectToTileSpace;
	protected boolean flipY = true;

	protected int mapTileWidth;
	protected int mapTileHeight;
	protected int mapWidthInPixels;
	protected int mapHeightInPixels;

	protected TiledMap map;

	public BaseTmxMapLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle tmxFile, P parameter) {
		this.root = xml.parse(tmxFile);

		TextureLoader.TextureParameter textureParameter = new TextureLoader.TextureParameter();
		if (parameter != null) {
			textureParameter.genMipMaps = parameter.generateMipMaps;
			textureParameter.minFilter = parameter.textureMinFilter;
			textureParameter.magFilter = parameter.textureMagFilter;
		}

		return getDependencyAssetDescriptors(tmxFile, textureParameter);
	}

	protected abstract Array<AssetDescriptor> getDependencyAssetDescriptors (FileHandle tmxFile, TextureLoader.TextureParameter textureParameter);

	/**
	 * Loads the map data, given the XML root element
	 *
	 * @param tmxFile       the Filehandle of the tmx file
	 * @param parameter
	 * @param imageResolver
	 * @return the {@link TiledMap}
	 */
	protected TiledMap loadTiledMap (FileHandle tmxFile, P parameter, ImageResolver imageResolver) {
		this.map = new TiledMap();

		if (parameter != null) {
			this.convertObjectToTileSpace = parameter.convertObjectToTileSpace;
			this.flipY = parameter.flipY;
		} else {
			this.convertObjectToTileSpace = false;
			this.flipY = true;
		}

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

		Element properties = root.getChildByName("properties");
		if (properties != null) {
			loadProperties(map.getProperties(), properties);
		}

		Array<Element> tilesets = root.getChildrenByName("tileset");
		for (Element element : tilesets) {
			loadTileSet(element, tmxFile, imageResolver);
			root.removeChild(element);
		}

		for (int i = 0, j = root.getChildCount(); i < j; i++) {
			Element element = root.getChild(i);
			loadLayer(map, map.getLayers(), element, tmxFile, imageResolver);
		}
		return map;
	}

	protected void loadLayer (TiledMap map, MapLayers parentLayers, Element element, FileHandle tmxFile, ImageResolver imageResolver) {
		String name = element.getName();
		if (name.equals("group")) {
			loadLayerGroup(map, parentLayers, element, tmxFile, imageResolver);
		} else if (name.equals("layer")) {
			loadTileLayer(map, parentLayers, element);
		} else if (name.equals("objectgroup")) {
			loadObjectGroup(map, parentLayers, element);
		} else if (name.equals("imagelayer")) {
			loadImageLayer(map, parentLayers, element, tmxFile, imageResolver);
		}
	}

	protected void loadLayerGroup (TiledMap map, MapLayers parentLayers, Element element, FileHandle tmxFile, ImageResolver imageResolver) {
		if (element.getName().equals("group")) {
			MapGroupLayer groupLayer = new MapGroupLayer();
			loadBasicLayerInfo(groupLayer, element);

			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(groupLayer.getProperties(), properties);
			}

			for (int i = 0, j = element.getChildCount(); i < j; i++) {
				Element child = element.getChild(i);
				loadLayer(map, groupLayer.getLayers(), child, tmxFile, imageResolver);
			}

			for (MapLayer layer : groupLayer.getLayers()) {
				layer.setParent(groupLayer);
			}

			parentLayers.add(groupLayer);
		}
	}

	protected void loadTileLayer (TiledMap map, MapLayers parentLayers, Element element) {
		if (element.getName().equals("layer")) {
			int width = element.getIntAttribute("width", 0);
			int height = element.getIntAttribute("height", 0);
			int tileWidth = map.getProperties().get("tilewidth", Integer.class);
			int tileHeight = map.getProperties().get("tileheight", Integer.class);
			TiledMapTileLayer layer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);

			loadBasicLayerInfo(layer, element);

			int[] ids = getTileIds(element, width, height);
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
						layer.setCell(x, flipY ? height - 1 - y : y, cell);
					}
				}
			}

			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(layer.getProperties(), properties);
			}
			parentLayers.add(layer);
		}
	}

	protected void loadObjectGroup (TiledMap map, MapLayers parentLayers, Element element) {
		if (element.getName().equals("objectgroup")) {
			MapLayer layer = new MapLayer();
			loadBasicLayerInfo(layer, element);
			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(layer.getProperties(), properties);
			}

			for (Element objectElement : element.getChildrenByName("object")) {
				loadObject(map, layer, objectElement);
			}

			parentLayers.add(layer);
		}
	}

	protected void loadImageLayer (TiledMap map, MapLayers parentLayers, Element element, FileHandle tmxFile, ImageResolver imageResolver) {
		if (element.getName().equals("imagelayer")) {
			float x = 0;
			float y = 0;
			if (element.hasAttribute("offsetx")) {
				x = Float.parseFloat(element.getAttribute("offsetx", "0"));
			} else {
				x = Float.parseFloat(element.getAttribute("x", "0"));
			}
			if (element.hasAttribute("offsety")) {
				y = Float.parseFloat(element.getAttribute("offsety", "0"));
			} else {
				y = Float.parseFloat(element.getAttribute("y", "0"));
			}
			if (flipY) y = mapHeightInPixels - y;

			TextureRegion texture = null;

			Element image = element.getChildByName("image");

			if (image != null) {
				String source = image.getAttribute("source");
				FileHandle handle = getRelativeFileHandle(tmxFile, source);
				texture = imageResolver.getImage(handle.path());
				y -= texture.getRegionHeight();
			}

			TiledMapImageLayer layer = new TiledMapImageLayer(texture, x, y);

			loadBasicLayerInfo(layer, element);

			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(layer.getProperties(), properties);
			}

			parentLayers.add(layer);
		}
	}

	protected void loadBasicLayerInfo (MapLayer layer, Element element) {
		String name = element.getAttribute("name", null);
		float opacity = Float.parseFloat(element.getAttribute("opacity", "1.0"));
		boolean visible = element.getIntAttribute("visible", 1) == 1;
		float offsetX = element.getFloatAttribute("offsetx", 0);
		float offsetY = element.getFloatAttribute("offsety", 0);

		layer.setName(name);
		layer.setOpacity(opacity);
		layer.setVisible(visible);
		layer.setOffsetX(offsetX);
		layer.setOffsetY(offsetY);
	}

	protected void loadObject (TiledMap map, MapLayer layer, Element element) {
		loadObject(map, layer.getObjects(), element, mapHeightInPixels);
	}

	protected void loadObject (TiledMap map, TiledMapTile tile, Element element) {
		loadObject(map, tile.getObjects(), element, tile.getTextureRegion().getRegionHeight());
	}

	protected void loadObject (TiledMap map, MapObjects objects, Element element, float heightInPixels) {
		if (element.getName().equals("object")) {
			MapObject object = null;

			float scaleX = convertObjectToTileSpace ? 1.0f / mapTileWidth : 1.0f;
			float scaleY = convertObjectToTileSpace ? 1.0f / mapTileHeight : 1.0f;

			float x = element.getFloatAttribute("x", 0) * scaleX;
			float y = (flipY ? (heightInPixels - element.getFloatAttribute("y", 0)) : element.getFloatAttribute("y", 0)) * scaleY;

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
						vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
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
						vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
					}
					Polyline polyline = new Polyline(vertices);
					polyline.setPosition(x, y);
					object = new PolylineMapObject(polyline);
				} else if ((child = element.getChildByName("ellipse")) != null) {
					object = new EllipseMapObject(x, flipY ? y - height : y, width, height);
				}
			}
			if (object == null) {
				String gid = null;
				if ((gid = element.getAttribute("gid", null)) != null) {
					int id = (int)Long.parseLong(gid);
					boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
					boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);

					TiledMapTile tile = map.getTileSets().getTile(id & ~MASK_CLEAR);
					TiledMapTileMapObject tiledMapTileMapObject = new TiledMapTileMapObject(tile, flipHorizontally, flipVertically);
					TextureRegion textureRegion = tiledMapTileMapObject.getTextureRegion();
					tiledMapTileMapObject.getProperties().put("gid", id);
					tiledMapTileMapObject.setX(x);
					tiledMapTileMapObject.setY(flipY ? y : y - height);
					float objectWidth = element.getFloatAttribute("width", textureRegion.getRegionWidth());
					float objectHeight = element.getFloatAttribute("height", textureRegion.getRegionHeight());
					tiledMapTileMapObject.setScaleX(scaleX * (objectWidth / textureRegion.getRegionWidth()));
					tiledMapTileMapObject.setScaleY(scaleY * (objectHeight / textureRegion.getRegionHeight()));
					tiledMapTileMapObject.setRotation(element.getFloatAttribute("rotation", 0));
					object = tiledMapTileMapObject;
				} else {
					object = new RectangleMapObject(x, flipY ? y - height : y, width, height);
				}
			}
			object.setName(element.getAttribute("name", null));
			String rotation = element.getAttribute("rotation", null);
			if (rotation != null) {
				object.getProperties().put("rotation", Float.parseFloat(rotation));
			}
			String type = element.getAttribute("type", null);
			if (type != null) {
				object.getProperties().put("type", type);
			}
			int id = element.getIntAttribute("id", 0);
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
			object.setVisible(element.getIntAttribute("visible", 1) == 1);
			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(object.getProperties(), properties);
			}
			objects.add(object);
		}
	}

	protected void loadProperties (MapProperties properties, Element element) {
		if (element == null) return;
		if (element.getName().equals("properties")) {
			for (Element property : element.getChildrenByName("property")) {
				String name = property.getAttribute("name", null);
				String value = property.getAttribute("value", null);
				String type = property.getAttribute("type", null);
				if (value == null) {
					value = property.getText();
				}
				Object castValue = castProperty(name, value, type);
				properties.put(name, castValue);
			}
		}
	}

	private Object castProperty (String name, String value, String type) {
		if (type == null) {
			return value;
		} else if (type.equals("int")) {
			return Integer.valueOf(value);
		} else if (type.equals("float")) {
			return Float.valueOf(value);
		} else if (type.equals("bool")) {
			return Boolean.valueOf(value);
		} else if (type.equals("color")) {
			// Tiled uses the format #AARRGGBB
			String opaqueColor = value.substring(3);
			String alpha = value.substring(1, 3);
			return Color.valueOf(opaqueColor + alpha);
		} else {
			throw new GdxRuntimeException("Wrong type given for property " + name + ", given : " + type
				+ ", supported : string, bool, int, float, color");
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

	static public int[] getTileIds (Element element, int width, int height) {
		Element data = element.getChildByName("data");
		String encoding = data.getAttribute("encoding", null);
		if (encoding == null) { // no 'encoding' attribute means that the encoding is XML
			throw new GdxRuntimeException("Unsupported encoding (XML) for TMX Layer Data");
		}
		int[] ids = new int[width * height];
		if (encoding.equals("csv")) {
			String[] array = data.getText().split(",");
			for (int i = 0; i < array.length; i++)
				ids[i] = (int)Long.parseLong(array[i].trim());
		} else {
			if (true)
				if (encoding.equals("base64")) {
					InputStream is = null;
					try {
						String compression = data.getAttribute("compression", null);
						byte[] bytes = Base64Coder.decode(data.getText());
						if (compression == null)
							is = new ByteArrayInputStream(bytes);
						else if (compression.equals("gzip"))
							is = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes), bytes.length));
						else if (compression.equals("zlib"))
							is = new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(bytes)));
						else
							throw new GdxRuntimeException("Unrecognised compression (" + compression + ") for TMX Layer Data");

						byte[] temp = new byte[4];
						for (int y = 0; y < height; y++) {
							for (int x = 0; x < width; x++) {
								int read = is.read(temp);
								while (read < temp.length) {
									int curr = is.read(temp, read, temp.length - read);
									if (curr == -1) break;
									read += curr;
								}
								if (read != temp.length)
									throw new GdxRuntimeException("Error Reading TMX Layer Data: Premature end of tile data");
								ids[y * width + x] = unsignedByteToInt(temp[0]) | unsignedByteToInt(temp[1]) << 8
									| unsignedByteToInt(temp[2]) << 16 | unsignedByteToInt(temp[3]) << 24;
							}
						}
					} catch (IOException e) {
						throw new GdxRuntimeException("Error Reading TMX Layer Data - IOException: " + e.getMessage());
					} finally {
						StreamUtils.closeQuietly(is);
					}
				} else {
					// any other value of 'encoding' is one we're not aware of, probably a feature of a future version of Tiled
					// or another editor
					throw new GdxRuntimeException("Unrecognised encoding (" + encoding + ") for TMX Layer Data");
				}
		}
		return ids;
	}

	protected static int unsignedByteToInt (byte b) {
		return b & 0xFF;
	}

	protected static FileHandle getRelativeFileHandle (FileHandle file, String path) {
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

	protected void loadTileSet (Element element, FileHandle tmxFile, ImageResolver imageResolver) {
		if (element.getName().equals("tileset")) {
			int firstgid = element.getIntAttribute("firstgid", 1);
			String imageSource = "";
			int imageWidth = 0;
			int imageHeight = 0;
			FileHandle image = null;

			String source = element.getAttribute("source", null);
			if (source != null) {
				FileHandle tsx = getRelativeFileHandle(tmxFile, source);
				try {
					element = xml.parse(tsx);
					Element imageElement = element.getChildByName("image");
					if (imageElement != null) {
						imageSource = imageElement.getAttribute("source");
						imageWidth = imageElement.getIntAttribute("width", 0);
						imageHeight = imageElement.getIntAttribute("height", 0);
						image = getRelativeFileHandle(tsx, imageSource);
					}
				} catch (SerializationException e) {
					throw new GdxRuntimeException("Error parsing external tileset.");
				}
			} else {
				Element imageElement = element.getChildByName("image");
				if (imageElement != null) {
					imageSource = imageElement.getAttribute("source");
					imageWidth = imageElement.getIntAttribute("width", 0);
					imageHeight = imageElement.getIntAttribute("height", 0);
					image = getRelativeFileHandle(tmxFile, imageSource);
				}
			}
			String name = element.get("name", null);
			int tilewidth = element.getIntAttribute("tilewidth", 0);
			int tileheight = element.getIntAttribute("tileheight", 0);
			int spacing = element.getIntAttribute("spacing", 0);
			int margin = element.getIntAttribute("margin", 0);

			Element offset = element.getChildByName("tileoffset");
			int offsetX = 0;
			int offsetY = 0;
			if (offset != null) {
				offsetX = offset.getIntAttribute("x", 0);
				offsetY = offset.getIntAttribute("y", 0);
			}
			TiledMapTileSet tileSet = new TiledMapTileSet();

			// TileSet
			tileSet.setName(name);
			final MapProperties tileSetProperties = tileSet.getProperties();
			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(tileSetProperties, properties);
			}
			tileSetProperties.put("firstgid", firstgid);

			// Tiles
			Array<Element> tileElements = element.getChildrenByName("tile");

			addStaticTiles(tmxFile, imageResolver, tileSet, element, tileElements, name, firstgid, tilewidth, tileheight, spacing,
				margin, source, offsetX, offsetY, imageSource, imageWidth, imageHeight, image);

			Array<AnimatedTiledMapTile> animatedTiles = new Array<AnimatedTiledMapTile>();

			for (Element tileElement : tileElements) {
				int localtid = tileElement.getIntAttribute("id", 0);
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

	protected abstract void addStaticTiles (FileHandle tmxFile, ImageResolver imageResolver, TiledMapTileSet tileset,
		Element element, Array<Element> tileElements, String name, int firstgid, int tilewidth, int tileheight, int spacing,
		int margin, String source, int offsetX, int offsetY, String imageSource, int imageWidth, int imageHeight, FileHandle image);

	protected void addTileProperties (TiledMapTile tile, Element tileElement) {
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

	protected void addTileObjectGroup (TiledMapTile tile, Element tileElement) {
		Element objectgroupElement = tileElement.getChildByName("objectgroup");
		if (objectgroupElement != null) {
			for (Element objectElement : objectgroupElement.getChildrenByName("object")) {
				loadObject(map, tile, objectElement);
			}
		}
	}

	protected AnimatedTiledMapTile createAnimatedTile (TiledMapTileSet tileSet, TiledMapTile tile, Element tileElement,
		int firstgid) {
		Element animationElement = tileElement.getChildByName("animation");
		if (animationElement != null) {
			Array<StaticTiledMapTile> staticTiles = new Array<StaticTiledMapTile>();
			IntArray intervals = new IntArray();
			for (Element frameElement : animationElement.getChildrenByName("frame")) {
				staticTiles.add((StaticTiledMapTile)tileSet.getTile(firstgid + frameElement.getIntAttribute("tileid")));
				intervals.add(frameElement.getIntAttribute("duration"));
			}

			AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(intervals, staticTiles);
			animatedTile.setId(tile.getId());
			return animatedTile;
		}
		return null;
	}

	protected void addStaticTiledMapTile (TiledMapTileSet tileSet, TextureRegion textureRegion, int tileId, float offsetX,
		float offsetY) {
		TiledMapTile tile = new StaticTiledMapTile(textureRegion);
		tile.setId(tileId);
		tile.setOffsetX(offsetX);
		tile.setOffsetY(flipY ? -offsetY : offsetY);
		tileSet.putTile(tileId, tile);
	}
}

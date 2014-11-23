package com.badlogic.gdx.maps.tiled;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class BaseTmxMapLoader<P extends AssetLoaderParameters<TiledMap>> extends AsynchronousAssetLoader<TiledMap, P> {

	public static class Parameters extends AssetLoaderParameters<TiledMap> {
		/** generate mipmaps? **/
		public boolean generateMipMaps = false;
		/** The TextureFilter to use for minification **/
		public TextureFilter textureMinFilter = TextureFilter.Nearest;
		/** The TextureFilter to use for magnification **/
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
	
	public BaseTmxMapLoader (FileHandleResolver resolver) {
		super(resolver);
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
			String rotation = element.getAttribute("rotation", null);
			if (rotation != null) {
				object.getProperties().put("rotation", Float.parseFloat(rotation));
			}
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
		if (element == null) return;
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
							is = new GZIPInputStream(new ByteArrayInputStream(bytes), bytes.length);
						else if (compression.equals("zlib"))
							is = new InflaterInputStream(new ByteArrayInputStream(bytes));
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
		return (int)b & 0xFF;
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
	
}

package com.badlogic.gdx.maps.loaders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * @brief synchronous loader for TMX maps created with the Tiled tool
 */
public class TmxMapLoader extends SynchronousAssetLoader<TiledMap, TmxMapLoader.Parameters> {

	public static class Parameters extends AssetLoaderParameters<TiledMap> {
		
	}
	
	private static final int FLAG_FLIP_HORIZONTALLY = 0x80000000;
	private static final int FLAG_FLIP_VERTICALLY = 0x40000000;
	private static final int FLAG_FLIP_DIAGONALLY = 0x20000000;		
	private static final int MASK_CLEAR  = 0xE0000000;
	
	private AssetManager assetManager;
	private FileHandle tmx;
	private XmlReader xml;
	
	/**
	 * Creates loader 
	 *  
	 * @param resolver
	 */
	public TmxMapLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	/**
	 * Loads a .tmx file
	 * 
	 * @param assetManager
	 * @param fileName
	 * @param parameter not used for now
	 * @return loaded TiledMap instance
	 */
	@Override
	public TiledMap load(AssetManager assetManager, String fileName, Parameters parameter) {
		this.assetManager = assetManager;
		this.tmx = resolve(fileName);
		this.xml = new XmlReader();
		try {
			XmlReader.Element root = xml.parse(tmx);
			TiledMap map = new TiledMap();
			Element properties = root.getChildByName("properties");
			if (properties != null) {
				loadProperties(map.getProperties(), properties);
			}
			Array<Element> tilesets = root.getChildrenByName("tileset");
			for (Element element : tilesets) {
				loadTileSet(map, element);
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
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retrieves TiledMap resource dependencies
	 * 
	 * @param fileName
	 * @param parameter not used for now
	 * @return dependencies for the given .tmx file
	 */
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, Parameters parameter) {
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		XmlReader xml = new XmlReader();
		try {
			FileHandle tmx = resolve(fileName);
			Element root = xml.parse(tmx);
			Array<Element> tilesets = root.getChildrenByName("tileset");
			for (Element tileset : tilesets) {
				String source = tileset.getAttribute("source", null);
				FileHandle image = null;
				if (source != null) {
					FileHandle tsx = getRelativeFileHandle(tmx, source);
					tileset = xml.parse(tsx);
					String imageSource = tileset.getChildByName("image").getAttribute("source");
					image = getRelativeFileHandle(tsx, imageSource);
				} else {
					String imageSource = tileset.getChildByName("image").getAttribute("source");
					image = getRelativeFileHandle(tmx, imageSource);
				}
				dependencies.add(new AssetDescriptor(image.path(), Texture.class));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dependencies;
	}

	public void loadTileSet(TiledMap map, Element element) {
		if (element.getName().equals("tileset")) {
			String name = element.get("name", null);
			int firstgid = element.getIntAttribute("firstgid", 1);
			int tilewidth = element.getIntAttribute("tilewidth", 0);
			int tileheight = element.getIntAttribute("tileheight", 0);
			int spacing = element.getIntAttribute("spacing", 0);
			int margin = element.getIntAttribute("margin", 0);			
			String source = element.getAttribute("source", null);
			FileHandle image = null;
			if (source != null) {
				FileHandle tsx = getRelativeFileHandle(tmx, source);
				try {
					element = xml.parse(tsx);
					name = element.get("name", null);
					tilewidth = element.getIntAttribute("tilewidth", 0);
					tileheight = element.getIntAttribute("tileheight", 0);
					spacing = element.getIntAttribute("spacing", 0);
					margin = element.getIntAttribute("margin", 0);
					String imageSource = element.getChildByName("image").getAttribute("source");
					image = getRelativeFileHandle(tsx, imageSource);
				} catch (IOException e) {
					throw new GdxRuntimeException("Error parsing external tileset.");
				}
			} else {
				String imageSource = element.getChildByName("image").getAttribute("source");
				image = getRelativeFileHandle(tmx, imageSource);
			}

			Texture texture = assetManager.get(image.path());

			TiledMapTileSet tileset = new TiledMapTileSet();
			tileset.setName(name);
			
			int stopWidth = texture.getWidth() - tilewidth;
			int stopHeight = texture.getHeight() - tileheight;

			int id = firstgid;
			
			for (int y = margin; y <= stopHeight; y += tileheight + spacing) {
				for (int x = margin; x <= stopWidth; x += tilewidth + spacing) {
					TiledMapTile tile = new StaticTiledMapTile(new TextureRegion(texture, x, y, tilewidth, tileheight));
					tileset.putTile(id++, tile);
				}
			}
			
			Array<Element> tileElements = element.getChildrenByName("tile");
			
			for (Element tileElement : tileElements) {
				int localtid = tileElement.getIntAttribute("id", 0);
				TiledMapTile tile = tileset.getTile(firstgid + localtid);
				if (tile!= null) {
					Element properties = element.getChildByName("properties");
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
	
	public void loadTileLayer(TiledMap map, Element element) {
		if (element.getName().equals("layer")) {
			String name = element.getAttribute("name", null);
			int width = element.getIntAttribute("width", 0);
			int height = element.getIntAttribute("height", 0);
			int tileWidth = element.getParent().getIntAttribute("tilewidth", 0);
			int tileHeight = element.getParent().getIntAttribute("tilewidth", 0);
			TiledMapTileLayer layer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
			layer.setName(name);
			
			TiledMapTileSets tilesets = map.getTileSets();
			
			Element data = element.getChildByName("data");
			String encoding = data.getAttribute("encoding", null);
			String compression = data.getAttribute("compression", null);
			if (encoding.equals("csv")) {
				String[] array = data.getText().split(",");
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int id = (int) Long.parseLong(array[y * width + x].trim());
						
						final boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
						final boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);
						final boolean flipDiagonally = ((id & FLAG_FLIP_DIAGONALLY) != 0);

						id = id & ~MASK_CLEAR;
						
						tilesets.getTile(id);
						TiledMapTile tile = tilesets.getTile(id);
						if (tile != null) {
							Cell cell = layer.getCell(x, height - 1 - y);
							if (flipDiagonally) {
								if (flipHorizontally && flipVertically) {
									cell.setFlipHorizontally(true);
									cell.setRotation(-90);
								} else if (flipHorizontally) {
									cell.setRotation(-90);
								} else if (flipVertically) {
									cell.setRotation(+90);
								} else {
									cell.setFlipVertically(true);
									cell.setRotation(-90);
								}
							} else {
								cell.setFlipHorizontally(flipHorizontally);
								cell.setFlipVertically(flipVertically);
							}
							cell.setTile(tile);
						}
					}
				}
			} else {
				if(encoding.equals("base64")) {
					byte[] bytes = Base64Coder.decode(data.getText());
					if (compression == null) {
						int read = 0;
						for (int y = 0; y < height; y++) {
							for (int x = 0; x < width; x++) {
								
								int id =
								unsignedByteToInt(bytes[read++]) |
								unsignedByteToInt(bytes[read++]) << 8 |
								unsignedByteToInt(bytes[read++]) << 16 |
								unsignedByteToInt(bytes[read++]) << 24;
								
								final boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
								final boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);
								final boolean flipDiagonally = ((id & FLAG_FLIP_DIAGONALLY) != 0);

								id = id & ~MASK_CLEAR;
								
								tilesets.getTile(id);
								TiledMapTile tile = tilesets.getTile(id);
								if (tile != null) {
									Cell cell = layer.getCell(x, height - 1 - y);
									if (flipDiagonally) {
										if (flipHorizontally && flipVertically) {
											cell.setFlipHorizontally(true);
											cell.setRotation(-90);
										} else if (flipHorizontally) {
											cell.setRotation(-90);
										} else if (flipVertically) {
											cell.setRotation(+90);
										} else {
											cell.setFlipVertically(true);
											cell.setRotation(-90);
										}
									} else {
										cell.setFlipHorizontally(flipHorizontally);
										cell.setFlipVertically(flipVertically);
									}
									cell.setTile(tile);
								}
							}
						}
					} else if (compression.equals("gzip")) {
						GZIPInputStream GZIS = null;
						try {
							GZIS = new GZIPInputStream(new ByteArrayInputStream(bytes), bytes.length);
						} catch (IOException e) {
							throw new GdxRuntimeException("Error Reading TMX Layer Data - IOException: " + e.getMessage());
						}

						byte[] temp = new byte[4];
						for (int y = 0; y < height; y++) {
							for (int x = 0; x < width; x++) {
								try {
									GZIS.read(temp, 0, 4);
									int id =
									unsignedByteToInt(temp[0]) |
									unsignedByteToInt(temp[1]) << 8 |
									unsignedByteToInt(temp[2]) << 16 |
									unsignedByteToInt(temp[3]) << 24;

									final boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
									final boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);
									final boolean flipDiagonally = ((id & FLAG_FLIP_DIAGONALLY) != 0);

									id = id & ~MASK_CLEAR;
									
									tilesets.getTile(id);
									TiledMapTile tile = tilesets.getTile(id);
									if (tile != null) {
										Cell cell = layer.getCell(x, height - 1 - y);
										if (flipDiagonally) {
											if (flipHorizontally && flipVertically) {
												cell.setFlipHorizontally(true);
												cell.setRotation(-90);
											} else if (flipHorizontally) {
												cell.setRotation(-90);
											} else if (flipVertically) {
												cell.setRotation(+90);
											} else {
												cell.setFlipVertically(true);
												cell.setRotation(-90);
											}
										} else {
											cell.setFlipHorizontally(flipHorizontally);
											cell.setFlipVertically(flipVertically);
										}
										cell.setTile(tile);
									}
								} catch (IOException e) {
									throw new GdxRuntimeException("Error Reading TMX Layer Data.", e);
								}
							}
						}
					} else if (compression.equals("zlib")) {
						Inflater zlib = new Inflater();
						
						byte[] temp = new byte[4];

						zlib.setInput(bytes, 0, bytes.length);

						for (int y = 0; y < height; y++) {
							for (int x = 0; x < width; x++) {
								try {
									zlib.inflate(temp, 0, 4);
									int id =
									unsignedByteToInt(temp[0]) |
									unsignedByteToInt(temp[1]) << 8 |
									unsignedByteToInt(temp[2]) << 16 |
									unsignedByteToInt(temp[3]) << 24;
									
									final boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
									final boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);
									final boolean flipDiagonally = ((id & FLAG_FLIP_DIAGONALLY) != 0);

									id = id & ~MASK_CLEAR;
									
									tilesets.getTile(id);
									TiledMapTile tile = tilesets.getTile(id);
									if (tile != null) {
										Cell cell = layer.getCell(x, height - 1 - y);
										if (flipDiagonally) {
											if (flipHorizontally && flipVertically) {
												cell.setFlipHorizontally(true);
												cell.setRotation(-90);
											} else if (flipHorizontally) {
												cell.setRotation(-90);
											} else if (flipVertically) {
												cell.setRotation(+90);
											} else {
												cell.setFlipVertically(true);
												cell.setRotation(-90);
											}
										} else {
											cell.setFlipHorizontally(flipHorizontally);
											cell.setFlipVertically(flipVertically);
										}
										cell.setTile(tile);
									}
			
								} catch (DataFormatException e) {
									throw new GdxRuntimeException("Error Reading TMX Layer Data.", e);
								}
							}
						}
					}
				}
			}
			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(layer.getProperties(), properties);
			}
			map.getLayers().addLayer(layer);
		}		
	}
	
	public void loadObjectGroup(TiledMap map, Element element) {
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

			map.getLayers().addLayer(layer);
		}
	}
	
	public void loadObject(MapLayer layer, Element element) {
		if (element.getName().equals("object")) {
			MapObject object = null;
			
			int x = element.getIntAttribute("x", 0);
			int y = element.getIntAttribute("y", 0);
			
			int width = element.getIntAttribute("width", 0);
			int height = element.getIntAttribute("height", 0);
			
			if (element.getChildCount() > 0) {
				Element child = element.getChildByName("polygon");
				if (child != null) {
					String[] points = child.getAttribute("points").split(" ");
					float[] vertices = new float[points.length * 2];
					for (int i = 0; i < points.length; i++) {
						String[] point = points[i].split(",");
						vertices[i * 2] = x + Integer.parseInt(point[0]);
						vertices[i * 2 + 1] = y + Integer.parseInt(point[1]);
					}
					object = new PolygonMapObject(vertices);
				} else {
					child = element.getChildByName("polyline");
					if (child != null) {
						String[] points = child.getAttribute("points").split(" ");
						float[] vertices = new float[points.length * 2];
						for (int i = 0; i < points.length; i++) {
							String[] point = points[i].split(",");
							vertices[i * 2] = x + Integer.parseInt(point[0]);
							vertices[i * 2 + 1] = y + Integer.parseInt(point[1]);
						}
						object = new PolylineMapObject(vertices);
					}
				}
			}
			if (object == null) {
				object = new RectangleMapObject(x, y, width, height);
			}
			object.setName(element.getAttribute("name", null));
			String type = element.getAttribute("type", null);
			if (type != null) {
				object.getProperties().put("type", type);
			}
			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(object.getProperties(), properties);
			}
			layer.getObjects().addObject(object);
		}
	}
	
	public void loadProperties(MapProperties properties, Element element) {
		if (element.getName().equals("properties")) {
			for (Element property : element.getChildrenByName("property")) {
				String name = property.getAttribute("name", null);
				String value = property.getAttribute(name, null);
				if (value == null) {
					value = property.getText();
				}
				properties.put(name, value);
			}
		}
	}
	
	public static FileHandle getRelativeFileHandle(FileHandle file, String path) {
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
	
	public static int unsignedByteToInt (byte b) {
		return (int) b & 0xFF;
	}

}

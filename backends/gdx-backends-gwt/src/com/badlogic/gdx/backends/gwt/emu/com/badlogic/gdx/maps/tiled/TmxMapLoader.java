package com.badlogic.gdx.maps.tiled;

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
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.ImageResolver.AssetManagerImageResolver;
import com.badlogic.gdx.maps.ImageResolver.DirectImageResolver;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
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
	
	private XmlReader xml = new XmlReader();
	private Element root;
	
	public TmxMapLoader() {
		super(new InternalFileHandleResolver());
	}
	
	/**
	 * Creates loader 
	 *  
	 * @param resolver
	 */
	public TmxMapLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	/**
	 * Loads the {@link TiledMap} from the given file. The file is
	 * resolved via the {@link FileHandleResolver} set in the constructor
	 * of this class. By default it will resolve to an internal file.
	 * @param fileName the filename
	 * @return the TiledMap
	 */
	public TiledMap load(String fileName) {
		try {
			FileHandle tmxFile = resolve(fileName);
			root = xml.parse(tmxFile);
			ObjectMap<String, Texture> textures = new ObjectMap<String, Texture>();
			for(FileHandle textureFile: loadTilesets(root, tmxFile)) {
				textures.put(textureFile.path(), new Texture(textureFile));
			}
			DirectImageResolver imageResolver = new DirectImageResolver(textures);
			TiledMap map = loadTilemap(root, tmxFile, imageResolver);
			map.setOwnedTextures(textures.values().toArray());
			return map;
		} catch(IOException e) {
			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
		}
	}
	
	@Override
	public TiledMap load(AssetManager assetManager, String fileName, Parameters parameter) {
		FileHandle tmxFile = resolve(fileName);
		try {
			return loadTilemap(root, tmxFile, new AssetManagerImageResolver(assetManager));
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
		}
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
		try {
			FileHandle tmxFile = resolve(fileName);
			root = xml.parse(tmxFile);
			for(FileHandle image: loadTilesets(root, tmxFile)) {
				dependencies.add(new AssetDescriptor(image.path(), Texture.class));
			}
			return dependencies;
		} catch (IOException e) {
			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
		}
	}
	
	/**
	 * Loads the map data, given the XML root element and an {@link ImageResolver} used
	 * to return the tileset Textures
	 * @param root the XML root element 
	 * @param tmxFile the Filehandle of the tmx file
	 * @param imageResolver the {@link ImageResolver}
	 * @return the {@link TiledMap}
	 */
	private TiledMap loadTilemap(Element root, FileHandle tmxFile, ImageResolver imageResolver) {
		TiledMap map = new TiledMap();
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
		}
		return map;
	}
	
	/**
	 * Loads the tilesets
	 * @param root the root XML element
	 * @return a list of filenames for images containing tiles
	 * @throws IOException 
	 */
	private Array<FileHandle> loadTilesets(Element root, FileHandle tmxFile) throws IOException {
		Array<FileHandle> images = new Array<FileHandle>();
		for (Element tileset : root.getChildrenByName("tileset")) {
			String source = tileset.getAttribute("source", null);
			FileHandle image = null;
			if (source != null) {
				FileHandle tsx = getRelativeFileHandle(tmxFile, source);
				tileset = xml.parse(tsx);
				String imageSource = tileset.getChildByName("image").getAttribute("source");
				image = getRelativeFileHandle(tsx, imageSource);
			} else {
				String imageSource = tileset.getChildByName("image").getAttribute("source");
				image = getRelativeFileHandle(tmxFile, imageSource);
			}
			images.add(image);
		}
		return images;
	}

	private void loadTileSet(TiledMap map, Element element, FileHandle tmxFile, ImageResolver imageResolver) {
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
				FileHandle tsx = getRelativeFileHandle(tmxFile, source);
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
				image = getRelativeFileHandle(tmxFile, imageSource);
			}

			TextureRegion texture = imageResolver.getImage(image.path());

			TiledMapTileSet tileset = new TiledMapTileSet();
			tileset.setName(name);
			
			int stopWidth = texture.getRegionWidth() - tilewidth;
			int stopHeight = texture.getRegionHeight() - tileheight;

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
	
	private void loadTileLayer(TiledMap map, Element element) {
		if (element.getName().equals("layer")) {
			String name = element.getAttribute("name", null);
			int width = element.getIntAttribute("width", 0);
			int height = element.getIntAttribute("height", 0);
			int tileWidth = element.getParent().getIntAttribute("tilewidth", 0);
			int tileHeight = element.getParent().getIntAttribute("tileheight", 0);
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
							Cell cell = new Cell();
							if (flipDiagonally) {
								if (flipHorizontally && flipVertically) {
									cell.setFlipHorizontally(true);
									cell.setRotation(Cell.ROTATE_90);
								} else if (flipHorizontally) {
									cell.setRotation(Cell.ROTATE_90);
								} else if (flipVertically) {
									cell.setRotation(Cell.ROTATE_270);
								} else {
									cell.setFlipVertically(true);
									cell.setRotation(Cell.ROTATE_90);
								}
							} else {
								cell.setFlipHorizontally(flipHorizontally);
								cell.setFlipVertically(flipVertically);
							}
							cell.setTile(tile);
							layer.setCell(x, height - 1 - y, cell);
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
									Cell cell = new Cell();
									if (flipDiagonally) {
										if (flipHorizontally && flipVertically) {
											cell.setFlipHorizontally(true);
											cell.setRotation(Cell.ROTATE_90);
										} else if (flipHorizontally) {
											cell.setRotation(Cell.ROTATE_90);
										} else if (flipVertically) {
											cell.setRotation(Cell.ROTATE_270);
										} else {
											cell.setFlipVertically(true);
											cell.setRotation(Cell.ROTATE_90);
										}
									} else {
										cell.setFlipHorizontally(flipHorizontally);
										cell.setFlipVertically(flipVertically);
									}
									cell.setTile(tile);
									layer.setCell(x, height - 1 - y, cell);
								}
							}
						}
					} else if (compression.equals("gzip")) {
						throw new GdxRuntimeException("Not supported in GWT backend");
					} else if (compression.equals("zlib")) {
						throw new GdxRuntimeException("Not supported in GWT backend");
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
	
	private void loadObject(MapLayer layer, Element element) {
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
	
	private void loadProperties(MapProperties properties, Element element) {
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
	
	private static FileHandle getRelativeFileHandle(FileHandle file, String path) {
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
	
	private static int unsignedByteToInt (byte b) {
		return (int) b & 0xFF;
	}

}

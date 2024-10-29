
package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class TmjMapLoader extends BaseTmjMapLoader<BaseTmjMapLoader.Parameters> {

	 public TmjMapLoader () {
		  super(new InternalFileHandleResolver());
	 }

	 /** Creates loader
	  *
	  * @param resolver */
	 public TmjMapLoader (FileHandleResolver resolver) {
		  super(resolver);
	 }

	 /** Loads the {@link TiledMap} from the given file. The file is resolved via the {@link FileHandleResolver} set in the
	  * constructor of this class. By default it will resolve to an internal file. The map will be loaded for a y-up coordinate
	  * system.
	  *
	  * @param fileName the filename
	  * @return the TiledMap */
	 public TiledMap load (String fileName) {
		  return load(fileName, new TmjMapLoader.Parameters());
	 }

	 /** Loads the {@link TiledMap} from the given file. The file is resolved via the {@link FileHandleResolver} set in the
	  * constructor of this class. By default it will resolve to an internal file.
	  *
	  * @param fileName the filename
	  * @param parameter specifies whether to use y-up, generate mip maps etc.
	  * @return the TiledMap */
	 public TiledMap load (String fileName, TmjMapLoader.Parameters parameter) {
		  FileHandle tmjFile = resolve(fileName);

		  this.root = json.parse(tmjFile);

		  ObjectMap<String, Texture> textures = new ObjectMap<>();

		  final Array<FileHandle> textureFiles = getDependencyFileHandles(tmjFile);
		  for (FileHandle textureFile : textureFiles) {
				Texture texture = new Texture(textureFile, parameter.generateMipMaps);
				texture.setFilter(parameter.textureMinFilter, parameter.textureMagFilter);
				textures.put(textureFile.path(), texture);
		  }

		  TiledMap map = loadTiledMap(tmjFile, parameter, new ImageResolver.DirectImageResolver(textures));
		  map.setOwnedResources(textures.values().toArray());
		  return map;
	 }

	 /** Loads a tile set from the given file. The file is resolved via the {@link FileHandleResolver} set in the constructor of
	  * this class. By default it will resolve to an internal file.
	  *
	  * @param fileName the filename of the tile set
	  * @param map the TiledMap to which the tile set will be added
	  * @return the TiledMap with the loaded tile set */
	 public TiledMap loadCustomTileSet (String fileName, TiledMap map) {
		  return loadCustomTileSet(fileName, map, new TmjMapLoader.Parameters());
	 }

	 /** Loads a tile set from the given file. The file is resolved via the {@link FileHandleResolver} set in the constructor of
	  * this class. By default it will resolve to an internal file.
	  *
	  * @param fileName the filename of the tile set
	  * @return the TiledMap with the loaded tile set */
	 public TiledMap loadCustomTileSet (String fileName) {
		  TiledMap map = new TiledMap();
		  return loadCustomTileSet(fileName, new TiledMap(), new TmjMapLoader.Parameters());
	 }

	 /** Loads a tile set from the given file. The file is resolved via the {@link FileHandleResolver} set in the constructor of
	  * this class. By default it will resolve to an internal file.
	  *
	  * @param fileName the filename of the tile set
	  * @param map the TiledMap to which the tile set will be added
	  * @param parameter specifies whether to use y-up, generate mip maps etc.
	  * @return the TiledMap with the loaded tile set */
	 public TiledMap loadCustomTileSet (String fileName, TiledMap map, TmjMapLoader.Parameters parameter) {
		  FileHandle tmjFile = resolve(fileName);
		  JsonValue tileSet = json.parse(tmjFile);
		  ObjectMap<String, Texture> textures = new ObjectMap<>();
		  this.map = map;

		  final Array<FileHandle> textureFiles = getTileSetDependencyFileHandle(tmjFile, tileSet);
		  for (FileHandle textureFile : textureFiles) {
				Texture texture = new Texture(textureFile, parameter.generateMipMaps);
				texture.setFilter(parameter.textureMinFilter, parameter.textureMagFilter);
				textures.put(textureFile.path(), texture);
		  }
		  loadTileSet(tileSet, tmjFile, new ImageResolver.DirectImageResolver(textures));
		  map.setOwnedResources(textures.values().toArray());

		  return map;
	 }

	 @Override
	 public void loadAsync (AssetManager manager, String fileName, FileHandle tmjFile, BaseTmjMapLoader.Parameters parameter) {
		  this.map = loadTiledMap(tmjFile, parameter, new ImageResolver.AssetManagerImageResolver(manager));
	 }

	 @Override
	 public TiledMap loadSync (AssetManager manager, String fileName, FileHandle file, BaseTmjMapLoader.Parameters parameter) {
		  return map;
	 }

	 @Override
	 protected Array<AssetDescriptor> getDependencyAssetDescriptors (FileHandle tmjFile,
		 TextureLoader.TextureParameter textureParameter) {
		  Array<AssetDescriptor> descriptors = new Array<>();

		  final Array<FileHandle> fileHandles = getDependencyFileHandles(tmjFile);
		  for (FileHandle handle : fileHandles) {
				descriptors.add(new AssetDescriptor(handle, Texture.class, textureParameter));
		  }

		  return descriptors;
	 }

	 protected Array<FileHandle> getDependencyFileHandles (FileHandle tmjFile) {
		  Array<FileHandle> fileHandles = new Array<>();

		  // TileSet descriptors
		  for (JsonValue tileSet : root.get("tileSets")) {
				getTileSetDependencyFileHandle(fileHandles, tmjFile, tileSet);
		  }

		  // ImageLayer descriptors
		  for (JsonValue layer : root.get("layers")) {
				if (!layer.getString("type").equals("imagelayer")) continue;
				String source = layer.getString("image");

				if (source != null) {
					 FileHandle handle = getRelativeFileHandle(tmjFile, source);
					 fileHandles.add(handle);
				}
		  }

		  return fileHandles;
	 }

	 protected Array<FileHandle> getTileSetDependencyFileHandle (FileHandle tmjFile, JsonValue tileSet) {
		  Array<FileHandle> fileHandles = new Array<>();
		  return getTileSetDependencyFileHandle(fileHandles, tmjFile, tileSet);
	 }

	 protected Array<FileHandle> getTileSetDependencyFileHandle (Array<FileHandle> fileHandles, FileHandle tmjFile,
		 JsonValue tileSet) {
		  String source = tileSet.getString("source", null);
		  if (source != null) {
				FileHandle tsxFile = getRelativeFileHandle(tmjFile, source);
				tileSet = json.parse(tsxFile);
				if (tileSet.has("image")) {
					 String imageSource = tileSet.getString("image");
					 FileHandle image = getRelativeFileHandle(tsxFile, imageSource);
					 fileHandles.add(image);
				} else {
					 for (JsonValue tile : tileSet.get("tile")) {
						  String imageSource = tile.getString("image");
						  FileHandle image = getRelativeFileHandle(tsxFile, imageSource);
						  fileHandles.add(image);
					 }
				}
		  } else {
				if (tileSet.has("image")) {
					 String imageSource = tileSet.getString("image");
					 FileHandle image = getRelativeFileHandle(tmjFile, imageSource);
					 fileHandles.add(image);
				} else {
					 for (JsonValue tile : tileSet.get("tile")) {
						  String imageSource = tile.getString("image");
						  FileHandle image = getRelativeFileHandle(tmjFile, imageSource);
						  fileHandles.add(image);
					 }
				}
		  }
		  return fileHandles;
	 }

	 @Override
	 protected void addStaticTiles (FileHandle tmjFile, ImageResolver imageResolver, TiledMapTileSet tileSet, JsonValue element,
		 JsonValue tiles, String name, int firstgid, int tilewidth, int tileheight, int spacing, int margin, String source,
		 int offsetX, int offsetY, String imageSource, int imageWidth, int imageHeight, FileHandle image) {

		  MapProperties props = tileSet.getProperties();
		  if (image != null) {
				// One image for the whole tileSet
				TextureRegion texture = imageResolver.getImage(image.path());

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
						  int tileId = id++;
						  addStaticTiledMapTile(tileSet, tileRegion, tileId, offsetX, offsetY);
					 }
				}
		  } else {
				// Every tile has its own image source
				for (JsonValue tile : tiles) {
					 if (tile.has("image")) {
						  imageSource = tile.getString("image");

						  if (source != null) {
								image = getRelativeFileHandle(getRelativeFileHandle(tmjFile, source), imageSource);
						  } else {
								image = getRelativeFileHandle(tmjFile, imageSource);
						  }
					 }
					 TextureRegion texture = imageResolver.getImage(image.path());
					 int tileId = firstgid + tile.getInt("id");
					 addStaticTiledMapTile(tileSet, texture, tileId, offsetX, offsetY);
				}
		  }
	 }

	 public static class Parameters extends BaseTmjMapLoader.Parameters {

	 }

}

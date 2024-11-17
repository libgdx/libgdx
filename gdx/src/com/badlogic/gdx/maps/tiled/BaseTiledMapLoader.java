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
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Null;

import java.util.StringTokenizer;

public abstract class BaseTiledMapLoader<P extends BaseTiledMapLoader.Parameters> extends AsynchronousAssetLoader<TiledMap, P> {

	 public static class Parameters extends AssetLoaderParameters<TiledMap> {
		  /** generate mipmaps? **/
		  public boolean generateMipMaps = false;
		  /** The TextureFilter to use for minification **/
		  public Texture.TextureFilter textureMinFilter = Texture.TextureFilter.Nearest;
		  /** The TextureFilter to use for magnification **/
		  public Texture.TextureFilter textureMagFilter = Texture.TextureFilter.Nearest;
		  /** Whether to convert the objects' pixel position and size to the equivalent in tile space. **/
		  public boolean convertObjectToTileSpace = false;
		  /** Whether to flip all Y coordinates so that Y positive is up. All libGDX renderers require flipped Y coordinates, and thus
			* flipY set to true. This parameter is included for non-rendering related purposes of TMX files, or custom renderers. */
		  public boolean flipY = true;
	 }

	 protected static final int FLAG_FLIP_HORIZONTALLY = 0x80000000;
	 protected static final int FLAG_FLIP_VERTICALLY = 0x40000000;
	 protected static final int FLAG_FLIP_DIAGONALLY = 0x20000000;
	 protected static final int MASK_CLEAR = 0xE0000000;

	 protected boolean convertObjectToTileSpace;
	 protected boolean flipY = true;

	 protected int mapTileWidth;
	 protected int mapTileHeight;
	 protected int mapWidthInPixels;
	 protected int mapHeightInPixels;

	 protected TiledMap map;
	 protected IntMap<MapObject> idToObject;
	 protected Array<Runnable> runOnEndOfLoadTiled;

	 public BaseTiledMapLoader (FileHandleResolver resolver) {
		  super(resolver);
	 }

	 /** Meant to be called within getDependencies() of a child class  */
	 protected abstract Array<AssetDescriptor> getDependencyAssetDescriptors (FileHandle mapFile,
		 TextureLoader.TextureParameter textureParameter);

	 /** Loads the map data, given the root element
	  *
	  * @param mapFile the Filehandle of the map file, .tmx or .tmj supported
	  * @param parameter
	  * @param imageResolver
	  * @return the {@link TiledMap} */
	 protected abstract TiledMap loadTiledMap (FileHandle mapFile, P parameter, ImageResolver imageResolver);


	 /** Gets a map of the object ids to the {@link MapObject} instances. Returns null if
	  * {@link #loadTiledMap(FileHandle, Parameters, ImageResolver)} has not been called yet.
	  *
	  * @return the map of the ids to {@link MapObject}, or null if {@link #loadTiledMap(FileHandle, Parameters, ImageResolver)}
	  *         method has not been called yet. */
	 public @Null IntMap<MapObject> getIdToObject () {
		  return idToObject;
	 }

	 protected Object castProperty (String name, String value, String type) {
		  if (type == null || "string".equals(type)) {
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
				throw new GdxRuntimeException(
					"Wrong type given for property " + name + ", given : " + type + ", supported : string, bool, int, float, color");
		  }
	 }

	 protected TiledMapTileLayer.Cell createTileLayerCell (boolean flipHorizontally, boolean flipVertically, boolean flipDiagonally) {
		  TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
		  if (flipDiagonally) {
				if (flipHorizontally && flipVertically) {
					 cell.setFlipHorizontally(true);
					 cell.setRotation(TiledMapTileLayer.Cell.ROTATE_270);
				} else if (flipHorizontally) {
					 cell.setRotation(TiledMapTileLayer.Cell.ROTATE_270);
				} else if (flipVertically) {
					 cell.setRotation(TiledMapTileLayer.Cell.ROTATE_90);
				} else {
					 cell.setFlipVertically(true);
					 cell.setRotation(TiledMapTileLayer.Cell.ROTATE_270);
				}
		  } else {
				cell.setFlipHorizontally(flipHorizontally);
				cell.setFlipVertically(flipVertically);
		  }
		  return cell;
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

	 protected void addStaticTiledMapTile (TiledMapTileSet tileSet, TextureRegion textureRegion, int tileId, float offsetX,
		 float offsetY) {
		  TiledMapTile tile = new StaticTiledMapTile(textureRegion);
		  tile.setId(tileId);
		  tile.setOffsetX(offsetX);
		  tile.setOffsetY(flipY ? -offsetY : offsetY);
		  tileSet.putTile(tileId, tile);
	 }

}

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
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;

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
		/** Path to Tiled project file. Needed when using class properties. */
		public String projectFilePath = null;
	}

	/** Representation of a single Tiled class property. A property has:
	 * <ul>
	 * <li>a property {@code name}</li>
	 * <li>a property {@code type} like string, int, ...</li>
	 * <li>an optional {@code propertyType} for class and enum types to refer to a specific class/enum</li>
	 * <li>a {@code defaultValue}</li>
	 * </ul>
	 */
	protected static class ProjectClassMember {
		public String name;
		public String type;
		public String propertyType;
		public JsonValue defaultValue;

		@Override
		public String toString () {
			return "ProjectClassMember{" //
				+ "name='" + name + "'" //
				+ ", type='" + type + "'" //
				+ ", propertyType='" + propertyType + "'" //
				+ ", defaultValue=" + defaultValue + "}";
		}
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
	/** Optional Tiled project class information. Key is the classname and value is an array of class members (=class
	 * properties) */
	protected ObjectMap<String, Array<ProjectClassMember>> projectClassInfo;

	public BaseTiledMapLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	/** Meant to be called within getDependencies() of a child class */
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
		if (type == null || "string".equals(type) || "file".equals(type)) {
			return value;
		} else if (type.equals("int")) {
			return Integer.valueOf(value);
		} else if (type.equals("float")) {
			return Float.valueOf(value);
		} else if (type.equals("bool")) {
			return Boolean.valueOf(value);
		} else if (type.equals("color")) {
			// return color after converting from #AARRGGBB to #RRGGBBAA
			return Color.valueOf(tiledColorToLibGDXColor(value));
		} else {
			throw new GdxRuntimeException("Wrong type given for property " + name + ", given : " + type
				+ ", supported : string, file, bool, int, float, color");
		}
	}

	protected TiledMapTileLayer.Cell createTileLayerCell (boolean flipHorizontally, boolean flipVertically,
		boolean flipDiagonally) {
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

	protected void loadObjectProperty (final MapProperties properties, final String name, String value) {
		// Wait until the end of [loadTiledMap] to fetch the object
		try {
			// Value should be the id of the object being pointed to
			final int id = Integer.parseInt(value);
			// Create [Runnable] to fetch object and add it to props
			Runnable fetch = new Runnable() {
				@Override
				public void run () {
					MapObject object = idToObject.get(id);
					properties.put(name, object);
				}
			};
			// [Runnable] should not run until the end of [loadTiledMap]
			runOnEndOfLoadTiled.add(fetch);
		} catch (Exception exception) {
			throw new GdxRuntimeException("Error parsing property [\" + name + \"] of type \"object\" with value: [" + value + "]",
				exception);
		}
	}

	protected void loadBasicProperty (MapProperties properties, String name, String value, String type) {
		Object castValue = castProperty(name, value, type);
		properties.put(name, castValue);
	}

	/** Parses the given Tiled project file for class property information. A class can have multiple members. Refer to
	 * {@link ProjectClassMember}. */
	protected void loadProjectFile (String projectFilePath) {
		projectClassInfo = new ObjectMap<>();
		if (projectFilePath == null || projectFilePath.trim().isEmpty()) {
			return;
		}

		FileHandle projectFile = resolve(projectFilePath);
		JsonValue projectRoot = new JsonReader().parse(projectFile);
		JsonValue propertyTypes = projectRoot.get("propertyTypes");
		if (propertyTypes == null) {
			// no custom property types in project -> nothing to parse
			return;
		}

		for (JsonValue propertyType : propertyTypes) {
			if (!"class".equals(propertyType.getString("type"))) {
				continue;
			}
			String className = propertyType.getString("name");
			JsonValue members = propertyType.get("members");
			if (members.isEmpty()) {
				continue;
			}

			Array<ProjectClassMember> projectClassMembers = new Array<>();
			projectClassInfo.put(className, projectClassMembers);
			for (JsonValue member : members) {
				BaseTmjMapLoader.ProjectClassMember projectClassMember = new BaseTmjMapLoader.ProjectClassMember();
				projectClassMember.name = member.getString("name");
				projectClassMember.type = member.getString("type");
				projectClassMember.propertyType = member.getString("propertyType", null);
				projectClassMember.defaultValue = member.get("value");
				projectClassMembers.add(projectClassMember);
			}
		}
	}

	protected void loadJsonClassProperties (String className, MapProperties classProperties, JsonValue classElement) {
		if (projectClassInfo == null) {
			throw new GdxRuntimeException(
				"No class information loaded to support class properties. Did you set the 'projectFilePath' parameter?");
		}
		if (projectClassInfo.isEmpty()) {
			throw new GdxRuntimeException(
				"No class information available. Did you set the correct Tiled project path in the 'projectFilePath' parameter?");
		}
		Array<ProjectClassMember> projectClassMembers = projectClassInfo.get(className);
		if (projectClassMembers == null) {
			throw new GdxRuntimeException("There is no class with name '" + className + "' in given Tiled project file.");
		}

		for (ProjectClassMember projectClassMember : projectClassMembers) {
			String propName = projectClassMember.name;
			JsonValue classProp = classElement.get(propName);
			switch (projectClassMember.type) {
			case "object": {
				String value = classProp == null ? projectClassMember.defaultValue.asString() : classProp.asString();
				loadObjectProperty(classProperties, propName, value);
				break;
			}
			case "class": {
				// A 'class' property is a property which is itself a set of properties
				if (classProp == null) {
					classProp = projectClassMember.defaultValue;
				}
				MapProperties nestedClassProperties = new MapProperties();
				String nestedClassName = projectClassMember.propertyType;
				nestedClassProperties.put("type", nestedClassName);
				// the actual properties of a 'class' property are stored as a new properties tag
				classProperties.put(propName, nestedClassProperties);
				loadJsonClassProperties(nestedClassName, nestedClassProperties, classProp);
				break;
			}
			default: {
				String value = classProp == null ? projectClassMember.defaultValue.asString() : classProp.asString();
				loadBasicProperty(classProperties, propName, value, projectClassMember.type);
				break;
			}
			}
		}
	}

	/** Converts Tiled's color format #AARRGGBB to a libGDX appropriate #RRGGBBAA The Tiled Map Editor uses the color format
	 * #AARRGGBB But note, if the alpha of the color is set to 255, Tiled does not include it as part of the color code in the .tmx
	 * ex. Red (r:255,g:0,b:0,a:255) becomes #ff0000, Red (r:255,g:0,b:0,a:127) becomes #7fff0000
	 *
	 * @param tiledColor A String representing a color in Tiled's #AARRGGBB format
	 * @return A String representing the color in the #RRGGBBAA format */
	public static String tiledColorToLibGDXColor (String tiledColor) {
		String alpha = tiledColor.length() == 9 ? tiledColor.substring(1, 3) : "ff";
		String color = tiledColor.length() == 9 ? tiledColor.substring(3) : tiledColor.substring(1);
		return color + alpha;
	}

}

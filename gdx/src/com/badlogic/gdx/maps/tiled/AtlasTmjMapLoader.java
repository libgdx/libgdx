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
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;

/** A TiledMap Loader which loads tiles from a TextureAtlas instead of separate images.
 *
 * It requires a map-level property called 'atlas' with its value being the relative path to the TextureAtlas. The atlas must have
 * in it indexed regions named after the tilesets used in the map. The indexes shall be local to the tileset (not the global id).
 * Strip whitespace and rotation should not be used when creating the atlas.
 *
 * @author Justin Shapcott
 * @author Manuel Bua */
public class AtlasTmjMapLoader extends BaseTmjMapLoader<AtlasTmjMapLoader.AtlasTiledMapLoaderParameters> {
	// TODO: too much duplicate functionality between atlas loaders, any options without making breaking changes?
	public static class AtlasTiledMapLoaderParameters extends BaseTmjMapLoader.Parameters {
		/** force texture filters? **/
		public boolean forceTextureFilters = false;
	}

	protected interface AtlasResolver extends ImageResolver {

		public TextureAtlas getAtlas ();

		public static class DirectAtlasResolver implements AtlasTmjMapLoader.AtlasResolver {
			private final TextureAtlas atlas;

			public DirectAtlasResolver (TextureAtlas atlas) {
				this.atlas = atlas;
			}

			@Override
			public TextureAtlas getAtlas () {
				return atlas;
			}

			@Override
			public TextureRegion getImage (String name) {
				// check for imagelayer and strip if needed
				String regionName = parseRegionName(name);
				return atlas.findRegion(regionName);
			}
		}

		public static class AssetManagerAtlasResolver implements AtlasTmjMapLoader.AtlasResolver {
			private final AssetManager assetManager;
			private final String atlasName;

			public AssetManagerAtlasResolver (AssetManager assetManager, String atlasName) {
				this.assetManager = assetManager;
				this.atlasName = atlasName;
			}

			@Override
			public TextureAtlas getAtlas () {
				return assetManager.get(atlasName, TextureAtlas.class);
			}

			@Override
			public TextureRegion getImage (String name) {
				// check for imagelayer and strip if needed
				String regionName = parseRegionName(name);
				return getAtlas().findRegion(regionName);
			}
		}
	}

	protected Array<Texture> trackedTextures = new Array<Texture>();

	protected AtlasResolver atlasResolver;

	public AtlasTmjMapLoader () {
		super(new InternalFileHandleResolver());
	}

	public AtlasTmjMapLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	public TiledMap load (String fileName) {
		return load(fileName, new AtlasTiledMapLoaderParameters());
	}

	public TiledMap load (String fileName, AtlasTiledMapLoaderParameters parameter) {
		FileHandle tmjFile = resolve(fileName);

		this.root = json.parse(tmjFile);

		final FileHandle atlasFileHandle = getAtlasFileHandle(tmjFile);
		TextureAtlas atlas = new TextureAtlas(atlasFileHandle);
		this.atlasResolver = new AtlasResolver.DirectAtlasResolver(atlas);

		TiledMap map = loadTiledMap(tmjFile, parameter, atlasResolver);
		map.setOwnedResources(new Array<TextureAtlas>(new TextureAtlas[] {atlas}));
		setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
		return map;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle tmjFile, AtlasTiledMapLoaderParameters parameter) {
		FileHandle atlasHandle = getAtlasFileHandle(tmjFile);
		this.atlasResolver = new AtlasResolver.AssetManagerAtlasResolver(manager, atlasHandle.path());

		this.map = loadTiledMap(tmjFile, parameter, atlasResolver);
	}

	@Override
	public TiledMap loadSync (AssetManager manager, String fileName, FileHandle file, AtlasTiledMapLoaderParameters parameter) {
		if (parameter != null) {
			setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
		}

		return map;
	}

	@Override
	protected Array<AssetDescriptor> getDependencyAssetDescriptors (FileHandle tmxFile,
		TextureLoader.TextureParameter textureParameter) {
		Array<AssetDescriptor> descriptors = new Array<AssetDescriptor>();

		// Atlas dependencies
		final FileHandle atlasFileHandle = getAtlasFileHandle(tmxFile);
		if (atlasFileHandle != null) {
			descriptors.add(new AssetDescriptor(atlasFileHandle, TextureAtlas.class));
		}

		return descriptors;
	}

	@Override
	protected void addStaticTiles (FileHandle tmjFile, ImageResolver imageResolver, TiledMapTileSet tileSet, JsonValue element,
		JsonValue tiles, String name, int firstgid, int tilewidth, int tileheight, int spacing, int margin, String source,
		int offsetX, int offsetY, String imageSource, int imageWidth, int imageHeight, FileHandle image) {

		TextureAtlas atlas = atlasResolver.getAtlas();
		String regionsName = name;

		for (Texture texture : atlas.getTextures()) {
			trackedTextures.add(texture);
		}

		MapProperties props = tileSet.getProperties();
		props.put("imagesource", imageSource);
		props.put("imagewidth", imageWidth);
		props.put("imageheight", imageHeight);
		props.put("tilewidth", tilewidth);
		props.put("tileheight", tileheight);
		props.put("margin", margin);
		props.put("spacing", spacing);

		if (imageSource != null && imageSource.length() > 0) {
			int lastgid = firstgid + ((imageWidth / tilewidth) * (imageHeight / tileheight)) - 1;
			for (AtlasRegion region : atlas.findRegions(regionsName)) {
				// Handle unused tileIds
				if (region != null) {
					int tileId = firstgid + region.index;
					if (tileId >= firstgid && tileId <= lastgid) {
						addStaticTiledMapTile(tileSet, region, tileId, offsetX, offsetY);
					}
				}
			}
		}

		// Add tiles with individual image sources
		for (JsonValue tileElement : tiles) {
			int tileId = firstgid + tileElement.getInt("id", 0);
			TiledMapTile tile = tileSet.getTile(tileId);
			if (tile == null) {
				JsonValue imageElement = tileElement.get("image");
				if (imageElement != null) {
					String regionName = imageElement.asString();
					regionName = regionName.substring(0, regionName.lastIndexOf('.'));
					AtlasRegion region = atlas.findRegion(regionName);
					if (region == null) throw new GdxRuntimeException("Tileset atlasRegion not found: " + regionName);
					addStaticTiledMapTile(tileSet, region, tileId, offsetX, offsetY);
				}
			}
		}
	}

	protected FileHandle getAtlasFileHandle (FileHandle tmjFile) {
		JsonValue properties = root.get("properties");

		String atlasFilePath = null;
		if (properties != null) {
			for (JsonValue property : properties) {
				String name = property.getString("name", "");
				if (name.startsWith("atlas")) {
					atlasFilePath = property.getString("value", "");
					break;
				}
			}
		}

		if (atlasFilePath == null || atlasFilePath.isEmpty()) {
			throw new GdxRuntimeException("The map is missing the 'atlas' property");
		} else {
			final FileHandle fileHandle = getRelativeFileHandle(tmjFile, atlasFilePath);
			if (!fileHandle.exists()) {
				throw new GdxRuntimeException("The 'atlas' file could not be found: '" + atlasFilePath + "'");
			}
			return fileHandle;
		}
	}

	protected void setTextureFilters (Texture.TextureFilter min, Texture.TextureFilter mag) {
		for (Texture texture : trackedTextures) {
			texture.setFilter(min, mag);
		}
		trackedTextures.clear();
	}

	/** Parse incoming region name to check for 'atlas_imagelayer' within the String These are regions representing Image Layers
	 * that have been packed into the atlas ImageLayer Image names include the relative assets path, so it must be stripped.
	 * @param name Name to check
	 * @return The name of the region to pass into an atlas */
	private static String parseRegionName (String name) {
		if (name.contains("atlas_imagelayer")) {
			// Extract the name of region from path
			return new FileHandle(name).name();
		} else {
			return name;
		}
	}
}

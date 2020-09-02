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
import com.badlogic.gdx.utils.XmlReader.Element;

/** A TiledMap Loader which loads tiles from a TextureAtlas instead of separate images.
 * 
 * It requires a map-level property called 'atlas' with its value being the relative path to the TextureAtlas. The atlas must have
 * in it indexed regions named after the tilesets used in the map. The indexes shall be local to the tileset (not the global id).
 * Strip whitespace and rotation should not be used when creating the atlas.
 * 
 * @author Justin Shapcott
 * @author Manuel Bua */
public class AtlasTmxMapLoader extends BaseTmxMapLoader<AtlasTmxMapLoader.AtlasTiledMapLoaderParameters> {

	public static class AtlasTiledMapLoaderParameters extends BaseTmxMapLoader.Parameters {
		/** force texture filters? **/
		public boolean forceTextureFilters = false;
	}

	private interface AtlasResolver extends ImageResolver {

		public TextureAtlas getAtlas ();

		public static class DirectAtlasResolver implements AtlasTmxMapLoader.AtlasResolver {
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
				return atlas.findRegion(name);
			}
		}

		public static class AssetManagerAtlasResolver implements AtlasTmxMapLoader.AtlasResolver {
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
				return getAtlas().findRegion(name);
			}
		}
	}

	protected Array<Texture> trackedTextures = new Array<Texture>();

	protected AtlasResolver atlasResolver;

	public AtlasTmxMapLoader () {
		super(new InternalFileHandleResolver());
	}

	public AtlasTmxMapLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	public TiledMap load (String fileName) {
		return load(fileName, new AtlasTiledMapLoaderParameters());
	}

	public TiledMap load (String fileName, AtlasTiledMapLoaderParameters parameter) {
		FileHandle tmxFile = resolve(fileName);

		this.root = xml.parse(tmxFile);

		final FileHandle atlasFileHandle = getAtlasFileHandle(tmxFile);
		TextureAtlas atlas = new TextureAtlas(atlasFileHandle);
		this.atlasResolver = new AtlasResolver.DirectAtlasResolver(atlas);

		TiledMap map = loadTiledMap(tmxFile, parameter, atlasResolver);
		map.setOwnedResources(new Array<TextureAtlas>(new TextureAtlas[] {atlas}));
		setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
		return map;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle tmxFile, AtlasTiledMapLoaderParameters parameter) {
		FileHandle atlasHandle = getAtlasFileHandle(tmxFile);
		this.atlasResolver = new AtlasResolver.AssetManagerAtlasResolver(manager, atlasHandle.path());

		this.map = loadTiledMap(tmxFile, parameter, atlasResolver);
	}

	@Override
	public TiledMap loadSync (AssetManager manager, String fileName, FileHandle file, AtlasTiledMapLoaderParameters parameter) {
		if (parameter != null) {
			setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
		}

		return map;
	}

	@Override
	protected Array<AssetDescriptor> getDependencyAssetDescriptors (FileHandle tmxFile, TextureLoader.TextureParameter textureParameter) {
		Array<AssetDescriptor> descriptors = new Array<AssetDescriptor>();

		// Atlas dependencies
		final FileHandle atlasFileHandle = getAtlasFileHandle(tmxFile);
		if (atlasFileHandle != null) {
			descriptors.add(new AssetDescriptor(atlasFileHandle, TextureAtlas.class));
		}

		return descriptors;
	}

	@Override
	protected void addStaticTiles (FileHandle tmxFile, ImageResolver imageResolver, TiledMapTileSet tileSet, Element element,
		Array<Element> tileElements, String name, int firstgid, int tilewidth, int tileheight, int spacing, int margin,
		String source, int offsetX, int offsetY, String imageSource, int imageWidth, int imageHeight, FileHandle image) {

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
		for (Element tileElement : tileElements) {
			int tileId = firstgid + tileElement.getIntAttribute("id", 0);
			TiledMapTile tile = tileSet.getTile(tileId);
			if (tile == null) {
				Element imageElement = tileElement.getChildByName("image");
				if (imageElement != null) {
					String regionName = imageElement.getAttribute("source");
					regionName = regionName.substring(0, regionName.lastIndexOf('.'));
					AtlasRegion region = atlas.findRegion(regionName);
					if (region == null)
						throw new GdxRuntimeException("Tileset atlasRegion not found: " + regionName);
					addStaticTiledMapTile(tileSet, region, tileId, offsetX, offsetY);
				}
			}
		}
	}

	private FileHandle getAtlasFileHandle (FileHandle tmxFile) {
		Element properties = root.getChildByName("properties");

		String atlasFilePath = null;
		if (properties != null) {
			for (Element property : properties.getChildrenByName("property")) {
				String name = property.getAttribute("name");
				if (name.startsWith("atlas")) {
					atlasFilePath = property.getAttribute("value");
					break;
				}
			}
		}
		if (atlasFilePath == null) {
			throw new GdxRuntimeException("The map is missing the 'atlas' property");
		} else {
			final FileHandle fileHandle = getRelativeFileHandle(tmxFile, atlasFilePath);
			if (!fileHandle.exists()) {
				throw new GdxRuntimeException("The 'atlas' file could not be found: '" + atlasFilePath + "'");
			}
			return fileHandle;
		}
	}

	private void setTextureFilters (Texture.TextureFilter min, Texture.TextureFilter mag) {
		for (Texture texture : trackedTextures) {
			texture.setFilter(min, mag);
		}
		trackedTextures.clear();
	}
}

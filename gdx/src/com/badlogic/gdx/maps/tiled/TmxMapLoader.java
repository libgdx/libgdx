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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.ImageResolver.AssetManagerImageResolver;
import com.badlogic.gdx.maps.ImageResolver.DirectImageResolver;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;

/** @brief synchronous loader for TMX maps created with the Tiled tool */
public class TmxMapLoader extends BaseTmxMapLoader<TmxMapLoader.Parameters> {

	public static class Parameters extends BaseTmxMapLoader.Parameters {

	}

	public TmxMapLoader () {
		super(new InternalFileHandleResolver());
	}

	/** Creates loader
	 * 
	 * @param resolver */
	public TmxMapLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	/** Loads the {@link TiledMap} from the given file. The file is resolved via the {@link FileHandleResolver} set in the
	 * constructor of this class. By default it will resolve to an internal file. The map will be loaded for a y-up coordinate
	 * system.
	 * @param fileName the filename
	 * @return the TiledMap */
	public TiledMap load (String fileName) {
		return load(fileName, new TmxMapLoader.Parameters());
	}

	/** Loads the {@link TiledMap} from the given file. The file is resolved via the {@link FileHandleResolver} set in the
	 * constructor of this class. By default it will resolve to an internal file.
	 * @param fileName the filename
	 * @param parameter specifies whether to use y-up, generate mip maps etc.
	 * @return the TiledMap */
	public TiledMap load (String fileName, TmxMapLoader.Parameters parameter) {
		FileHandle tmxFile = resolve(fileName);

		this.root = xml.parse(tmxFile);

		ObjectMap<String, Texture> textures = new ObjectMap<String, Texture>();

		final Array<FileHandle> textureFiles = getDependencyFileHandles(tmxFile);
		for (FileHandle textureFile : textureFiles) {
			Texture texture = new Texture(textureFile, parameter.generateMipMaps);
			texture.setFilter(parameter.textureMinFilter, parameter.textureMagFilter);
			textures.put(textureFile.path(), texture);
		}

		TiledMap map = loadTiledMap(tmxFile, parameter, new DirectImageResolver(textures));
		map.setOwnedResources(textures.values().toArray());
		return map;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle tmxFile, Parameters parameter) {
		this.map = loadTiledMap(tmxFile, parameter, new AssetManagerImageResolver(manager));
	}

	@Override
	public TiledMap loadSync (AssetManager manager, String fileName, FileHandle file, Parameters parameter) {
		return map;
	}

	@Override
	protected Array<AssetDescriptor> getDependencyAssetDescriptors (FileHandle tmxFile,	TextureLoader.TextureParameter textureParameter) {
		Array<AssetDescriptor> descriptors = new Array<AssetDescriptor>();

		final Array<FileHandle> fileHandles = getDependencyFileHandles(tmxFile);
		for (FileHandle handle : fileHandles) {
			descriptors.add(new AssetDescriptor(handle, Texture.class, textureParameter));
		}

		return descriptors;
	}

	private Array<FileHandle> getDependencyFileHandles (FileHandle tmxFile) {
		Array<FileHandle> fileHandles = new Array<FileHandle>();

		// TileSet descriptors
		for (Element tileset : root.getChildrenByName("tileset")) {
			String source = tileset.getAttribute("source", null);
			if (source != null) {
				FileHandle tsxFile = getRelativeFileHandle(tmxFile, source);
				tileset = xml.parse(tsxFile);
				Element imageElement = tileset.getChildByName("image");
				if (imageElement != null) {
					String imageSource = tileset.getChildByName("image").getAttribute("source");
					FileHandle image = getRelativeFileHandle(tsxFile, imageSource);
					fileHandles.add(image);
				} else {
					for (Element tile : tileset.getChildrenByName("tile")) {
						String imageSource = tile.getChildByName("image").getAttribute("source");
						FileHandle image = getRelativeFileHandle(tsxFile, imageSource);
						fileHandles.add(image);
					}
				}
			} else {
				Element imageElement = tileset.getChildByName("image");
				if (imageElement != null) {
					String imageSource = tileset.getChildByName("image").getAttribute("source");
					FileHandle image = getRelativeFileHandle(tmxFile, imageSource);
					fileHandles.add(image);
				} else {
					for (Element tile : tileset.getChildrenByName("tile")) {
						String imageSource = tile.getChildByName("image").getAttribute("source");
						FileHandle image = getRelativeFileHandle(tmxFile, imageSource);
						fileHandles.add(image);
					}
				}
			}
		}

		// ImageLayer descriptors
		for (Element imageLayer : root.getChildrenByName("imagelayer")) {
			Element image = imageLayer.getChildByName("image");
			String source = image.getAttribute("source", null);

			if (source != null) {
				FileHandle handle = getRelativeFileHandle(tmxFile, source);
				fileHandles.add(handle);
			}
		}

		return fileHandles;
	}

	@Override
	protected void addStaticTiles (FileHandle tmxFile, ImageResolver imageResolver, TiledMapTileSet tileSet,	Element element,
		Array<Element> tileElements, String name, int firstgid, int tilewidth, int tileheight, int spacing, int margin,
		String source, int offsetX, int offsetY, String imageSource, int imageWidth, int imageHeight, FileHandle image) {

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
			for (Element tileElement : tileElements) {
				Element imageElement = tileElement.getChildByName("image");
				if (imageElement != null) {
					imageSource = imageElement.getAttribute("source");

					if (source != null) {
						image = getRelativeFileHandle(getRelativeFileHandle(tmxFile, source), imageSource);
					} else {
						image = getRelativeFileHandle(tmxFile, imageSource);
					}
				}
				TextureRegion texture = imageResolver.getImage(image.path());
				int tileId = firstgid + tileElement.getIntAttribute("id");
				addStaticTiledMapTile(tileSet, texture, tileId, offsetX, offsetY);
			}
		}
	}
}

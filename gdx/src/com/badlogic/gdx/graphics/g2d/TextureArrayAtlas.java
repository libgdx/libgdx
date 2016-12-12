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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

/** A {@link TextureAtlas} that uses {@link TextureArray TextureArrays} to reduce the number of GLTextures used. Usable only in a
 * GL30 context. Any pages that have the same size, mip mapping, filters, and wrapping will share a single TextureArray. Returned
 * AtlasRegions have the array layer baked into the texture coordinates. See {@link TextureRegion#putLayerInCoordinates(TextureRegion, int)}.
 * <p>
 * A TextureArrayAtlas must be disposed to free up the resources consumed by the backing textures.
 * @author cypherdare */
public class TextureArrayAtlas extends TextureAtlas {
	
	private ObjectIntMap<Page> pagesToLayers;

	/** Creates an empty atlas to which regions can be added. */
	public TextureArrayAtlas () {
	}

	/** Loads the specified pack file using {@link FileType#Internal}, using the parent directory of the pack file to find the page
	 * images. */
	public TextureArrayAtlas (String internalPackFile) {
		this(Gdx.files.internal(internalPackFile));
	}

	/** Loads the specified pack file, using the parent directory of the pack file to find the page images. */
	public TextureArrayAtlas (FileHandle packFile) {
		this(packFile, packFile.parent());
	}

	/** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
	 * @see #TextureArrayAtlas(FileHandle) */
	public TextureArrayAtlas (FileHandle packFile, boolean flip) {
		this(packFile, packFile.parent(), flip);
	}

	public TextureArrayAtlas (FileHandle packFile, FileHandle imagesDir) {
		this(packFile, imagesDir, false);
	}

	/** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner. */
	public TextureArrayAtlas (FileHandle packFile, FileHandle imagesDir, boolean flip) {
		this(new TextureAtlasData(packFile, imagesDir, flip));
	}

	/** @param data May be null. */
	public TextureArrayAtlas (TextureAtlasData data) {
		super(data);
	}

	@Override
	protected ObjectMap<Page, Texture> readPageTextures (TextureAtlasData data) {
		ObjectMap<Page, Texture> pageToTexture = new ObjectMap<Page, Texture>();
		
		ObjectSet<Array<Page>> pageArrays = groupPagesBySharableTexture(data.pages);
		
		pagesToLayers = new ObjectIntMap<Page>();
		for (Array<Page> pageArray : pageArrays) {
			Array<FileHandle> files = new Array<FileHandle>(true, pageArray.size, FileHandle.class);
			for (int i = 0; i < pageArray.size; i++) {
				Page page = pageArray.get(i);
				files.add(page.textureFile);
				pagesToLayers.put(page, i);
			}
			Page templatePage = pageArray.first();
			Texture texture;
			if (templatePage.texture == null) {
				texture = new TextureArray(templatePage.useMipMaps, templatePage.format, files.toArray());
				texture.setFilter(templatePage.minFilter, templatePage.magFilter);
				texture.setWrap(templatePage.uWrap, templatePage.vWrap);
			} else {
				texture = templatePage.texture;
				texture.setFilter(templatePage.minFilter, templatePage.magFilter);
				texture.setWrap(templatePage.uWrap, templatePage.vWrap);
			}
			textures.add(texture);
			for (Page page : pageArray)
				pageToTexture.put(page, texture);
		}
		return pageToTexture;
	}

	@Override
	protected AtlasRegion generateAtlasRegionFromData (Region region, Texture texture) {
		AtlasRegion atlasRegion = super.generateAtlasRegionFromData(region, texture);
		int layer = pagesToLayers.get(region.page, 0);
		TextureRegion.putLayerInCoordinates(atlasRegion, layer);
		return atlasRegion;
	}

	/** Adds a region of a Texture to the atlas, with a layer baked into the texture coordinates. The specified 
	 * texture will be disposed when the atlas is disposed. */
	public AtlasRegion addRegion (String name, Texture texture, int x, int y, int width, int height, int layer) {
		AtlasRegion region = addRegion(name, texture, x, y, width, height);
		TextureRegion.putLayerInCoordinates(region, layer);
		return region;
	}
	
	public static ObjectSet<Array<Page>> groupPagesBySharableTexture (Array<Page> pages){
		ObjectSet<Array<Page>> pageArrays = new ObjectSet<Array<Page>>(); 
		for (Page page : pages) {
			Array<Page> compatiblePages = null;
			for (Array<Page> pageArray : pageArrays) {
				if (canPagesShareTextureArray(pageArray.first(), page)) {
					compatiblePages = pageArray;
					break;
				}
			}
			if (compatiblePages == null) {
				compatiblePages = new Array<Page>(4);
				pageArrays.add(compatiblePages);
			}
			compatiblePages.add(page);
		}
		return pageArrays;
	}
	
	private static boolean canPagesShareTextureArray (Page page0, Page page1){
		return page0.width == page1.width && page0.height == page1.height
			&& page0.useMipMaps == page1.useMipMaps && page0.minFilter == page1.minFilter && page0.magFilter == page1.magFilter
			&& page0.uWrap == page1.uWrap && page0.vWrap == page1.vWrap;
	}
	
}

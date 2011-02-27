/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.graphics.g2d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.*;

/**
 * Loads images from texture atlases created by TexturePacker.<br>
 * <br>
 * A TextureAtlas must be disposed to free up the resources consumed by the backing textures.
 * @author Nathan Sweet
 */
public class TextureAtlas implements Disposable {
	static private final String[] tuple = new String[2];

	private final HashSet<Texture> textures = new HashSet(4);
	private final ArrayList<AtlasRegion> regions;

	/**
	 * Creates an empty atlas to which regions can be added.
	 */
	public TextureAtlas () {
		regions = new ArrayList();
	}

	/**
	 * Loads the specified pack file, using the parent directory of the pack file to find the page images.
	 */
	public TextureAtlas (FileHandle packFile) {
		this(packFile, packFile.parent());
	}

	/**
	 * @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
	 * @see #TextureAtlas(FileHandle)
	 */
	public TextureAtlas (FileHandle packFile, boolean flip) {
		this(packFile, packFile.parent(), flip);
	}

	public TextureAtlas (FileHandle packFile, FileHandle imagesDir) {
		this(packFile, imagesDir, false);
	}

	/**
	 * @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
	 */
	public TextureAtlas (FileHandle packFile, FileHandle imagesDir, boolean flip) {
		PriorityQueue<AtlasRegion> sortedRegions = new PriorityQueue(16, indexComparator);

		BufferedReader reader = new BufferedReader(new InputStreamReader(packFile.read()), 64);
		try {
			Texture pageImage = null;
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				if (line.trim().length() == 0)
					pageImage = null;
				else if (pageImage == null) {
					FileHandle file = imagesDir.child(line);

					// FIXME - Actually load in the requested format.
					Format format = Format.valueOf(readValue(reader));

					readTuple(reader);
					TextureFilter min = TextureFilter.valueOf(tuple[0]);
					TextureFilter max = TextureFilter.valueOf(tuple[1]);

					String direction = readValue(reader);
					TextureWrap repeatX = ClampToEdge;
					TextureWrap repeatY = ClampToEdge;
					if (direction.equals("x"))
						repeatX = Repeat;
					else if (direction.equals("y"))
						repeatY = Repeat;
					else if (direction.equals("xy")) {
						repeatX = Repeat;
						repeatY = Repeat;
					}

					pageImage = new Texture(file, TextureFilter.isMipMap(min) || TextureFilter.isMipMap(max) ? true : false);
					pageImage.setFilter(min, max);
					pageImage.setWrap(repeatX, repeatY);
					textures.add(pageImage);
				} else {
					boolean rotate = Boolean.valueOf(readValue(reader));

					readTuple(reader);
					int left = Integer.parseInt(tuple[0]);
					int top = Integer.parseInt(tuple[1]);

					readTuple(reader);
					int width = Integer.parseInt(tuple[0]);
					int height = Integer.parseInt(tuple[1]);

					AtlasRegion region = new AtlasRegion(pageImage, left, top, width, height);
					region.name = line;
					region.rotate = rotate;

					readTuple(reader);
					region.originalWidth = Integer.parseInt(tuple[0]);
					region.originalHeight = Integer.parseInt(tuple[1]);

					readTuple(reader);
					region.offsetX = Integer.parseInt(tuple[0]);
					region.offsetY = Integer.parseInt(tuple[1]);

					region.index = Integer.parseInt(readValue(reader));

					if (flip) region.flip(false, true);

					sortedRegions.add(region);
				}
			}
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error reading pack file: " + packFile);
		} finally {
			try {
				reader.close();
			} catch (IOException ignored) {
			}
		}

		int n = sortedRegions.size();
		regions = new ArrayList(n);
		for (int i = 0; i < n; i++)
			regions.add(sortedRegions.poll());
	}

	/**
	 * Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed.
	 */
	public AtlasRegion addRegion (String name, Texture texture, int x, int y, int width, int height) {
		textures.add(texture);
		AtlasRegion region = new AtlasRegion(texture, x, y, width, height);
		region.name = name;
		region.originalWidth = width;
		region.originalHeight = height;
		region.index = -1;
		regions.add(region);
		return region;
	}

	/**
	 * Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed.
	 */
	public AtlasRegion addRegion (String name, TextureRegion textureRegion) {
		return addRegion(name, textureRegion.texture, textureRegion.getRegionX(), textureRegion.getRegionY(),
			textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
	}

	/**
	 * Returns all regions in the atlas.
	 */
	public List<AtlasRegion> getRegions () {
		return regions;
	}

	/**
	 * Returns the first region found with the specified name. This method uses string comparison to find the region, so the result
	 * should be cached rather than calling this method multiple times.
	 * @return The region, or null.
	 */
	public AtlasRegion findRegion (String name) {
		for (int i = 0, n = regions.size(); i < n; i++)
			if (regions.get(i).name.equals(name)) return regions.get(i);
		return null;
	}

	/**
	 * Returns the first region found with the specified name and index. This method uses string comparison to find the region, so
	 * the result should be cached rather than calling this method multiple times.
	 * @return The region, or null.
	 */
	public AtlasRegion findRegion (String name, int index) {
		for (int i = 0, n = regions.size(); i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (!region.name.equals(name)) continue;
			if (region.index != index) continue;
			return region;
		}
		return null;
	}

	/**
	 * Returns all regions with the specified name, ordered by smallest to largest {@link AtlasRegion#index index}. This method
	 * uses string comparison to find the regions, so the result should be cached rather than calling this method multiple times.
	 */
	public List<AtlasRegion> findRegions (String name) {
		ArrayList<AtlasRegion> matched = new ArrayList();
		for (int i = 0, n = regions.size(); i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (region.name.equals(name)) matched.add(new AtlasRegion(region));
		}
		return matched;
	}

	/**
	 * Returns all regions in the atlas as sprites. This method creates a new sprite for each region, so the result should be
	 * stored rather than calling this method multiple times.
	 * @see #createSprite(String)
	 */
	public List<Sprite> createSprites () {
		ArrayList sprites = new ArrayList(regions.size());
		for (int i = 0, n = regions.size(); i < n; i++)
			sprites.add(newSprite(regions.get(i)));
		return sprites;
	}

	/**
	 * Returns the first region found with the specified name as a sprite. If whitespace was stripped from the region when it was
	 * packed, the sprite is automatically positioned as if whitespace had not been stripped. This method uses string comparison to
	 * find the region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
	 * @return The sprite, or null.
	 */
	public Sprite createSprite (String name) {
		for (int i = 0, n = regions.size(); i < n; i++)
			if (regions.get(i).name.equals(name)) return newSprite(regions.get(i));
		return null;
	}

	/**
	 * Returns the first region found with the specified name and index as a sprite. This method uses string comparison to find the
	 * region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
	 * @return The sprite, or null.
	 * @see #createSprite(String)
	 */
	public Sprite createSprite (String name, int index) {
		for (int i = 0, n = regions.size(); i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (!region.name.equals(name)) continue;
			if (region.index != index) continue;
			return newSprite(regions.get(i));
		}
		return null;
	}

	/**
	 * Returns all regions with the specified name as sprites, ordered by smallest to largest {@link AtlasRegion#index index}. This
	 * method uses string comparison to find the regions and constructs new sprites, so the result should be cached rather than
	 * calling this method multiple times.
	 * @see #createSprite(String)
	 */
	public List<Sprite> createSprites (String name) {
		ArrayList<Sprite> matched = new ArrayList();
		for (int i = 0, n = regions.size(); i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (region.name.equals(name)) matched.add(newSprite(region));
		}
		return matched;
	}

	private Sprite newSprite (AtlasRegion region) {
		if (region.packedWidth == region.originalWidth && region.packedHeight == region.originalHeight) {
			Sprite sprite = new Sprite(region);
			if (region.rotate) sprite.rotate90(true);
			return sprite;
		}
		return new AtlasSprite(region);
	}

	/**
	 * Releases all resources associated with this TextureAtlas instance. This releases all the textures backing all TextureRegions
	 * and Sprites, which should no longer be used after calling dispose.
	 */
	public void dispose () {
		for (Texture texture : textures)
			texture.dispose();
		textures.clear();
	}

	static private final Comparator<AtlasRegion> indexComparator = new Comparator<AtlasRegion>() {
		public int compare (AtlasRegion region1, AtlasRegion region2) {
			int i1 = region1.index;
			if (i1 == -1) i1 = Integer.MAX_VALUE;
			int i2 = region2.index;
			if (i2 == -1) i2 = Integer.MAX_VALUE;
			return i1 - i2;
		}
	};

	static private String readValue (BufferedReader reader) throws IOException {
		String line = reader.readLine();
		int colon = line.indexOf(':');
		if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
		return line.substring(colon + 1).trim();
	}

	static private void readTuple (BufferedReader reader) throws IOException {
		String line = reader.readLine();
		int colon = line.indexOf(':');
		int comma = line.indexOf(',');
		if (colon == -1 || comma == -1 || comma < colon + 1) throw new GdxRuntimeException("Invalid line: " + line);
		tuple[0] = line.substring(colon + 1, comma).trim();
		tuple[1] = line.substring(comma + 1).trim();
	}

	/**
	 * Describes the region of a packed image and provides information about the original image before it was packed.
	 */
	static public class AtlasRegion extends TextureRegion {
		/**
		 * The number at the end of the original image file name, or -1 if none.<br>
		 * <br>
		 * When sprites are packed, if the original file name ends with a number, it is stored as the index and is not considered as
		 * part of the sprite's name. This is useful for keeping animation frames in order.
		 * @see TextureAtlas#findRegions(String)
		 */
		public int index;

		/**
		 * The name of the original image file, up to the first underscore. Underscores denote special instructions to the texture
		 * packer.
		 */
		public String name;

		/**
		 * The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing.
		 */
		public float offsetX;

		/**
		 * The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
		 * packing.
		 */
		public float offsetY;

		/**
		 * The width of the image, after whitespace was removed for packing.
		 */
		public int packedWidth;

		/**
		 * The height of the image, after whitespace was removed for packing.
		 */
		public int packedHeight;

		/**
		 * The width of the image, before whitespace was removed for packing.
		 */
		public int originalWidth;

		/**
		 * The height of the image, before whitespace was removed for packing.
		 */
		public int originalHeight;

		/**
		 * If true, the region has been rotated 90 degrees counter clockwise.
		 */
		public boolean rotate;

		public AtlasRegion (Texture texture, int x, int y, int width, int height) {
			super(texture, x, y, width, height);
			packedWidth = width;
			packedHeight = height;
		}

		public AtlasRegion (AtlasRegion region) {
			setRegion(region);
			index = region.index;
			name = region.name;
			offsetX = region.offsetX;
			offsetY = region.offsetY;
			packedWidth = region.packedWidth;
			packedHeight = region.packedHeight;
			originalWidth = region.originalWidth;
			originalHeight = region.originalHeight;
		}

		/**
		 * Flips the region, adjusting the offset so the image appears to be flipped as if no whitespace has been removed for
		 * packing.
		 */
		public void flip (boolean x, boolean y) {
			super.flip(x, y);
			if (x) offsetX = originalWidth - offsetX - packedWidth;
			if (y) offsetY = originalHeight - offsetY - packedHeight;
		}
	}

	/**
	 * A sprite that, if whitespace was stripped from the region when it was packed, is automatically positioned as if whitespace
	 * had not been stripped.
	 */
	static public class AtlasSprite extends Sprite {
		final AtlasRegion region;

		public AtlasSprite (AtlasRegion region) {
			this.region = new AtlasRegion(region);
			setRegion(region);
			if (region.rotate) rotate90(true);
			setOrigin(region.originalWidth / 2, region.originalHeight / 2);
			super.setBounds(region.offsetX, region.offsetY, Math.abs(region.getRegionWidth()), Math.abs(region.getRegionHeight()));
			setColor(1, 1, 1, 1);
		}

		public void setPosition (float x, float y) {
			super.setPosition(x + region.offsetX, y + region.offsetY);
		}

		public void setBounds (float x, float y, float width, float height) {
			super.setBounds(x + region.offsetX, y + region.offsetY, width, height);
		}

		public void setOrigin (float originX, float originY) {
			super.setOrigin(originX + region.offsetX, originY + region.offsetY);
		}

		public void flip (boolean x, boolean y) {
			// Flip texture.
			super.flip(x, y);

			float oldOffsetX = region.offsetX;
			float oldOffsetY = region.offsetY;
			// Update x and y offsets.
			region.flip(x, y);

			// Update position with new offsets.
			translate(region.offsetX - oldOffsetX, region.offsetY - oldOffsetY);
		}

		public float getX () {
			return super.getX() - region.offsetX;
		}

		public float getY () {
			return super.getY() - region.offsetY;
		}

		public AtlasRegion getAtlasRegion () {
			return region;
		}
	}
}

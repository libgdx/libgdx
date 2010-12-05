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

package com.badlogic.gdx.graphics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.GdxRuntimeException;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.*;

/**
 * Loads images from texture atlases created by TexturePacker.<br>
 * <br>
 * A TextureAtlas must be disposed to free up the resources consumed by the backing textures.
 */
public class TextureAtlas {
	static private final String[] tuple = new String[2];

	private final ArrayList<Texture> textures = new ArrayList(4);
	private final AtlasRegion[] regions;

	public TextureAtlas (FileHandle imagesDir) {
		this(imagesDir.child("pack"), imagesDir);
	}

	public TextureAtlas (FileHandle imagesDir, boolean flip) {
		this(imagesDir.child("pack"), imagesDir, flip);
	}

	public TextureAtlas (FileHandle packFile, FileHandle imagesDir) {
		this(packFile, imagesDir, false);
	}

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

					pageImage = Gdx.graphics.newTexture(file, min, max, repeatX, repeatY);
					textures.add(pageImage);
				} else {
					boolean rotate = Boolean.valueOf(readValue(reader));

					readTuple(reader);
					int left = Integer.parseInt(tuple[0]);
					int top = Integer.parseInt(tuple[1]);

					readTuple(reader);
					int width = Integer.parseInt(tuple[0]);
					int height = Integer.parseInt(tuple[1]);

					if (flip) {
						if (rotate) {
							left += height;
							height = -height;
						} else {
							top += height;
							height = -height;
						}
					}

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
					if (region.index == -1) region.index = Integer.MAX_VALUE;

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
		regions = new AtlasRegion[n];
		for (int i = 0; i < n; i++)
			regions[i] = sortedRegions.poll();
	}

	/**
	 * Returns the first sprite found with the specified name.<br>
	 * <br>
	 * This method uses string comparison to find the sprite, so the result should be cached rather than calling this method every
	 * frame.
	 */
	public AtlasRegion getRegion (String name) {
		for (int i = 0, n = regions.length; i < n; i++)
			if (regions[i].name.equals(name)) return regions[i];
		return null;
	}

	/**
	 * Returns all sprites found with the specified name, ordered by smallest to largest {@link AtlasRegion#getIndex() index}.<br>
	 * <br>
	 * This method uses string comparison to find the sprite, so the result should be cached rather than calling this method every
	 * frame.
	 */
	public List<AtlasRegion> getRegions (String name) {
		ArrayList<AtlasRegion> matched = new ArrayList();
		for (int i = 0, n = regions.length; i < n; i++)
			if (regions[i].name.equals(name)) matched.add(regions[i]);
		return matched;
	}

	public Sprite getSprite (String name) {
		for (int i = 0, n = regions.length; i < n; i++)
			if (regions[i].name.equals(name)) return newSprite(regions[i]);
		return null;
	}

	public List<Sprite> getSprites (String name) {
		ArrayList<Sprite> matched = new ArrayList();
		for (int i = 0, n = regions.length; i < n; i++)
			if (regions[i].name.equals(name)) matched.add(newSprite(regions[i]));
		return matched;
	}

	private Sprite newSprite (AtlasRegion region) {
		if (region.packedWidth == region.originalWidth && region.packedHeight == region.originalHeight) return new Sprite(region);
		return new AtlasSprite(region);
	}

	/**
	 * Releases all resources associated with this TextureAtlas instance. This releases all the textures backing all TextureRegions
	 * and Sprites, which should no longer be used after calling dispose.
	 */
	public void dispose () {
		for (int i = 0, n = textures.size(); i < n; i++)
			textures.get(i).dispose();
		textures.clear();
	}

	static private final Comparator<AtlasRegion> indexComparator = new Comparator<AtlasRegion>() {
		public int compare (AtlasRegion region1, AtlasRegion region2) {
			return region1.index - region2.index;
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

	static public class AtlasRegion extends TextureRegion {
		int index;
		String name;
		float offsetX, offsetY;
		int packedWidth, packedHeight;
		int originalWidth, originalHeight;
		boolean rotate;

		AtlasRegion (Texture texture, int x, int y, int width, int height) {
			super(texture, x, y, width, height);
			packedWidth = width;
			packedHeight = height;
		}

		public void flip (boolean x, boolean y) {
			super.flip(x, y);
			if (x) offsetX = (int)(originalWidth - offsetX - packedWidth);
			if (y) offsetY = (int)(originalHeight - offsetY - packedHeight);
		}

		/**
		 * The name of the original image file, with any trailing numbers or special flags removed.
		 */
		public String getName () {
			return name;
		}

		/**
		 * The number at the end of the original image file name, or Integer.MAX_VALUE if none.<br>
		 * <br>
		 * When sprites are packed, if the original file name ends with a number, it is stored as the index and is not considered as
		 * part of the sprite's name. This is useful for keeping animation frames in order.
		 * @see TextureAtlas#getRegions(String)
		 */
		public int getIndex () {
			return index;
		}

		/**
		 * The width of the image, after whitespace was removed for packing.
		 */
		public int getPackedWidth () {
			return packedWidth;
		}

		/**
		 * The height of the image, after whitespace was removed for packing.
		 */
		public int getPackedHeight () {
			return packedHeight;
		}

		/**
		 * The width of the image, before whitespace was removed for packing.
		 */
		public int getOriginalWidth () {
			return originalWidth;
		}

		/**
		 * The height of the image, before whitespace was removed for packing.
		 */
		public int getOriginalHeight () {
			return originalHeight;
		}

		/**
		 * The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing.
		 */
		public float getOffsetX () {
			return offsetX;
		}

		/**
		 * The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
		 * packing.
		 */
		public float getOffsetY () {
			return offsetY;
		}
	}

	/**
	 * A sprite that provides additional information about the packed image it represents. An AtlasSprite's position is relative to
	 * the bottom left of the original image, before whitespace was removed for packing.
	 */
	static class AtlasSprite extends Sprite {
		final AtlasRegion region;
		final float widthScale, heightScale;

		AtlasSprite (AtlasRegion region) {
			this.region = region;
			getTextureRegion().set(region);
			if (region.rotate) rotate90(true);

			translate(region.offsetX, region.offsetY);

			super.setSize(region.getWidth(), region.getHeight());
			widthScale = region.packedWidth / region.originalWidth;
			heightScale = region.packedHeight / region.originalHeight;
		}

		public void setPosition (float x, float y) {
			super.setPosition(x + region.offsetX, y + region.offsetY);
		}

		public void setBounds (float x, float y, float width, float height) {
			AtlasRegion region = this.region;
			region.offsetX *= width / getWidth();
			region.offsetY *= height / getHeight();
			super.setBounds(x + region.offsetX, y + region.offsetY, width * widthScale, height * heightScale);
		}

		public void setSize (float width, float height) {
			region.offsetX *= width / getWidth();
			region.offsetY *= height / getHeight();
			super.setSize(width * widthScale, height * heightScale);
		}

		public void setOrigin (float originX, float originY) {
			super.setOrigin(originX + region.offsetX, originY + region.offsetY);
		}

		public void setScale (float scaleXY) {
			super.setScale(scaleXY);
			region.offsetX *= scaleXY;
			region.offsetY *= scaleXY;
		}

		public void setScale (float scaleX, float scaleY) {
			super.setScale(scaleX, scaleY);
			region.offsetX *= scaleX;
			region.offsetY *= scaleY;
		}

		public void scale (float amount) {
			float unscaledOffsetX = region.offsetX / getScaleX();
			float unscaledOffsetY = region.offsetY / getScaleY();
			super.scale(amount);
			region.offsetX = unscaledOffsetX * getScaleX();
			region.offsetY = unscaledOffsetY * getScaleY();
		}

		public float getWidth () {
			return super.getWidth() / widthScale;
		}

		public float getHeight () {
			return super.getHeight() / heightScale;
		}

		public float getX () {
			return super.getX() - region.offsetX;
		}

		public float getY () {
			return super.getY() - region.offsetY;
		}
	}
}

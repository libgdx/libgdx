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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.StreamUtils;

/** Loads images from texture atlases created by TexturePacker.<br>
 * <br>
 * A TextureAtlas must be disposed to free up the resources consumed by the backing textures.
 * @author Nathan Sweet */
public class TextureAtlas implements Disposable {
	private final ObjectSet<Texture> textures = new ObjectSet(4);
	private final Array<AtlasRegion> regions = new Array();

	/** Creates an empty atlas to which regions can be added. */
	public TextureAtlas () {
	}

	/** Loads the specified pack file using {@link FileType#Internal}, using the parent directory of the pack file to find the page
	 * images. */
	public TextureAtlas (String internalPackFile) {
		this(Gdx.files.internal(internalPackFile));
	}

	/** Loads the specified pack file, using the parent directory of the pack file to find the page images. */
	public TextureAtlas (FileHandle packFile) {
		this(packFile, packFile.parent());
	}

	/** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
	 * @see #TextureAtlas(FileHandle) */
	public TextureAtlas (FileHandle packFile, boolean flip) {
		this(packFile, packFile.parent(), flip);
	}

	public TextureAtlas (FileHandle packFile, FileHandle imagesDir) {
		this(packFile, imagesDir, false);
	}

	/** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner. */
	public TextureAtlas (FileHandle packFile, FileHandle imagesDir, boolean flip) {
		this(new TextureAtlasData(packFile, imagesDir, flip));
	}

	public TextureAtlas (TextureAtlasData data) {
		load(data);
	}

	/** Adds the textures and regions from the specified texture atlas data. */
	public void load (TextureAtlasData data) {
		textures.ensureCapacity(data.pages.size);
		for (Page page : data.pages) {
			if (page.texture == null) page.texture = new Texture(page.textureFile, page.format, page.useMipMaps);
			page.texture.setFilter(page.minFilter, page.magFilter);
			page.texture.setWrap(page.uWrap, page.vWrap);
			textures.add(page.texture);
		}

		regions.ensureCapacity(data.regions.size);
		for (Region region : data.regions) {
			AtlasRegion atlasRegion = new AtlasRegion(region.page.texture, region.left, region.top, //
				region.rotate ? region.height : region.width, //
				region.rotate ? region.width : region.height);
			atlasRegion.index = region.index;
			atlasRegion.name = region.name;
			atlasRegion.offsetX = region.offsetX;
			atlasRegion.offsetY = region.offsetY;
			atlasRegion.originalHeight = region.originalHeight;
			atlasRegion.originalWidth = region.originalWidth;
			atlasRegion.rotate = region.rotate;
			atlasRegion.degrees = region.degrees;
			atlasRegion.names = region.names;
			atlasRegion.values = region.values;
			if (region.flip) atlasRegion.flip(false, true);
			regions.add(atlasRegion);
		}
	}

	/** Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed. */
	public AtlasRegion addRegion (String name, Texture texture, int x, int y, int width, int height) {
		textures.add(texture);
		AtlasRegion region = new AtlasRegion(texture, x, y, width, height);
		region.name = name;
		regions.add(region);
		return region;
	}

	/** Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed. */
	public AtlasRegion addRegion (String name, TextureRegion textureRegion) {
		textures.add(textureRegion.texture);
		AtlasRegion region = new AtlasRegion(textureRegion);
		region.name = name;
		regions.add(region);
		return region;
	}

	/** Returns all regions in the atlas. */
	public Array<AtlasRegion> getRegions () {
		return regions;
	}

	/** Returns the first region found with the specified name. This method uses string comparison to find the region, so the
	 * result should be cached rather than calling this method multiple times. */
	public @Null AtlasRegion findRegion (String name) {
		for (int i = 0, n = regions.size; i < n; i++)
			if (regions.get(i).name.equals(name)) return regions.get(i);
		return null;
	}

	/** Returns the first region found with the specified name and index. This method uses string comparison to find the region, so
	 * the result should be cached rather than calling this method multiple times. */
	public @Null AtlasRegion findRegion (String name, int index) {
		for (int i = 0, n = regions.size; i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (!region.name.equals(name)) continue;
			if (region.index != index) continue;
			return region;
		}
		return null;
	}

	/** Returns all regions with the specified name, ordered by smallest to largest {@link AtlasRegion#index index}. This method
	 * uses string comparison to find the regions, so the result should be cached rather than calling this method multiple
	 * times. */
	public Array<AtlasRegion> findRegions (String name) {
		Array<AtlasRegion> matched = new Array(AtlasRegion.class);
		for (int i = 0, n = regions.size; i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (region.name.equals(name)) matched.add(new AtlasRegion(region));
		}
		return matched;
	}

	/** Returns all regions in the atlas as sprites. This method creates a new sprite for each region, so the result should be
	 * stored rather than calling this method multiple times.
	 * @see #createSprite(String) */
	public Array<Sprite> createSprites () {
		Array sprites = new Array(true, regions.size, Sprite.class);
		for (int i = 0, n = regions.size; i < n; i++)
			sprites.add(newSprite(regions.get(i)));
		return sprites;
	}

	/** Returns the first region found with the specified name as a sprite. If whitespace was stripped from the region when it was
	 * packed, the sprite is automatically positioned as if whitespace had not been stripped. This method uses string comparison to
	 * find the region and constructs a new sprite, so the result should be cached rather than calling this method multiple
	 * times. */
	public @Null Sprite createSprite (String name) {
		for (int i = 0, n = regions.size; i < n; i++)
			if (regions.get(i).name.equals(name)) return newSprite(regions.get(i));
		return null;
	}

	/** Returns the first region found with the specified name and index as a sprite. This method uses string comparison to find
	 * the region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
	 * @see #createSprite(String) */
	public @Null Sprite createSprite (String name, int index) {
		for (int i = 0, n = regions.size; i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (region.index != index) continue;
			if (!region.name.equals(name)) continue;
			return newSprite(regions.get(i));
		}
		return null;
	}

	/** Returns all regions with the specified name as sprites, ordered by smallest to largest {@link AtlasRegion#index index}.
	 * This method uses string comparison to find the regions and constructs new sprites, so the result should be cached rather
	 * than calling this method multiple times.
	 * @see #createSprite(String) */
	public Array<Sprite> createSprites (String name) {
		Array<Sprite> matched = new Array(Sprite.class);
		for (int i = 0, n = regions.size; i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (region.name.equals(name)) matched.add(newSprite(region));
		}
		return matched;
	}

	private Sprite newSprite (AtlasRegion region) {
		if (region.packedWidth == region.originalWidth && region.packedHeight == region.originalHeight) {
			if (region.rotate) {
				Sprite sprite = new Sprite(region);
				sprite.setBounds(0, 0, region.getRegionHeight(), region.getRegionWidth());
				sprite.rotate90(true);
				return sprite;
			}
			return new Sprite(region);
		}
		return new AtlasSprite(region);
	}

	/** Returns the first region found with the specified name as a {@link NinePatch}. The region must have been packed with
	 * ninepatch splits. This method uses string comparison to find the region and constructs a new ninepatch, so the result should
	 * be cached rather than calling this method multiple times. */
	public @Null NinePatch createPatch (String name) {
		for (int i = 0, n = regions.size; i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (region.name.equals(name)) {
				int[] splits = region.findValue("split");
				if (splits == null) throw new IllegalArgumentException("Region does not have ninepatch splits: " + name);
				NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
				int[] pads = region.findValue("pad");
				if (pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
				return patch;
			}
		}
		return null;
	}

	/** @return the textures of the pages, unordered */
	public ObjectSet<Texture> getTextures () {
		return textures;
	}

	/** Releases all resources associated with this TextureAtlas instance. This releases all the textures backing all
	 * TextureRegions and Sprites, which should no longer be used after calling dispose. */
	public void dispose () {
		for (Texture texture : textures)
			texture.dispose();
		textures.clear(0);
	}

	static public class TextureAtlasData {
		final Array<Page> pages = new Array();
		final Array<Region> regions = new Array();

		public TextureAtlasData () {
		}

		public TextureAtlasData (FileHandle packFile, FileHandle imagesDir, boolean flip) {
			load(packFile, imagesDir, flip);
		}

		public void load (FileHandle packFile, FileHandle imagesDir, boolean flip) {
			final String[] entry = new String[5];

			ObjectMap<String, Field<Page>> pageFields = new ObjectMap(15, 0.99f); // Size needed to avoid collisions.
			pageFields.put("size", new Field<Page>() {
				public void parse (Page page) {
					page.width = Integer.parseInt(entry[1]);
					page.height = Integer.parseInt(entry[2]);
				}
			});
			pageFields.put("format", new Field<Page>() {
				public void parse (Page page) {
					page.format = Format.valueOf(entry[1]);
				}
			});
			pageFields.put("filter", new Field<Page>() {
				public void parse (Page page) {
					page.minFilter = TextureFilter.valueOf(entry[1]);
					page.magFilter = TextureFilter.valueOf(entry[2]);
					page.useMipMaps = page.minFilter.isMipMap();
				}
			});
			pageFields.put("repeat", new Field<Page>() {
				public void parse (Page page) {
					if (entry[1].indexOf('x') != -1) page.uWrap = TextureWrap.Repeat;
					if (entry[1].indexOf('y') != -1) page.vWrap = TextureWrap.Repeat;
				}
			});
			pageFields.put("pma", new Field<Page>() {
				public void parse (Page page) {
					page.pma = entry[1].equals("true");
				}
			});

			final boolean[] hasIndexes = {false};
			ObjectMap<String, Field<Region>> regionFields = new ObjectMap(127, 0.99f); // Size needed to avoid collisions.
			regionFields.put("xy", new Field<Region>() { // Deprecated, use bounds.
				public void parse (Region region) {
					region.left = Integer.parseInt(entry[1]);
					region.top = Integer.parseInt(entry[2]);
				}
			});
			regionFields.put("size", new Field<Region>() { // Deprecated, use bounds.
				public void parse (Region region) {
					region.width = Integer.parseInt(entry[1]);
					region.height = Integer.parseInt(entry[2]);
				}
			});
			regionFields.put("bounds", new Field<Region>() {
				public void parse (Region region) {
					region.left = Integer.parseInt(entry[1]);
					region.top = Integer.parseInt(entry[2]);
					region.width = Integer.parseInt(entry[3]);
					region.height = Integer.parseInt(entry[4]);
				}
			});
			regionFields.put("offset", new Field<Region>() { // Deprecated, use offsets.
				public void parse (Region region) {
					region.offsetX = Integer.parseInt(entry[1]);
					region.offsetY = Integer.parseInt(entry[2]);
				}
			});
			regionFields.put("orig", new Field<Region>() { // Deprecated, use offsets.
				public void parse (Region region) {
					region.originalWidth = Integer.parseInt(entry[1]);
					region.originalHeight = Integer.parseInt(entry[2]);
				}
			});
			regionFields.put("offsets", new Field<Region>() {
				public void parse (Region region) {
					region.offsetX = Integer.parseInt(entry[1]);
					region.offsetY = Integer.parseInt(entry[2]);
					region.originalWidth = Integer.parseInt(entry[3]);
					region.originalHeight = Integer.parseInt(entry[4]);
				}
			});
			regionFields.put("rotate", new Field<Region>() {
				public void parse (Region region) {
					String value = entry[1];
					if (value.equals("true"))
						region.degrees = 90;
					else if (!value.equals("false")) //
						region.degrees = Integer.parseInt(value);
					region.rotate = region.degrees == 90;
				}
			});
			regionFields.put("index", new Field<Region>() {
				public void parse (Region region) {
					region.index = Integer.parseInt(entry[1]);
					if (region.index != -1) hasIndexes[0] = true;
				}
			});

			BufferedReader reader = new BufferedReader(new InputStreamReader(packFile.read()), 1024);
			try {
				String line = reader.readLine();
				// Ignore empty lines before first entry.
				while (line != null && line.trim().length() == 0)
					line = reader.readLine();
				// Header entries.
				while (true) {
					if (line == null || line.trim().length() == 0) break;
					if (readEntry(entry, line) == 0) break; // Silently ignore all header fields.
					line = reader.readLine();
				}
				// Page and region entries.
				Page page = null;
				Array<Object> names = null, values = null;
				while (true) {
					if (line == null) break;
					if (line.trim().length() == 0) {
						page = null;
						line = reader.readLine();
					} else if (page == null) {
						page = new Page();
						page.textureFile = imagesDir.child(line);
						while (true) {
							if (readEntry(entry, line = reader.readLine()) == 0) break;
							Field field = pageFields.get(entry[0]);
							if (field != null) field.parse(page); // Silently ignore unknown page fields.
						}
						pages.add(page);
					} else {
						Region region = new Region();
						region.page = page;
						region.name = line.trim();
						if (flip) region.flip = true;
						while (true) {
							int count = readEntry(entry, line = reader.readLine());
							if (count == 0) break;
							Field field = regionFields.get(entry[0]);
							if (field != null)
								field.parse(region);
							else {
								if (names == null) {
									names = new Array(8);
									values = new Array(8);
								}
								names.add(entry[0]);
								int[] entryValues = new int[count];
								for (int i = 0; i < count; i++) {
									try {
										entryValues[i] = Integer.parseInt(entry[i + 1]);
									} catch (NumberFormatException ignored) { // Silently ignore non-integer values.
									}
								}
								values.add(entryValues);
							}
						}
						if (region.originalWidth == 0 && region.originalHeight == 0) {
							region.originalWidth = region.width;
							region.originalHeight = region.height;
						}
						if (names != null && names.size > 0) {
							region.names = names.toArray(String.class);
							region.values = values.toArray(int[].class);
							names.clear();
							values.clear();
						}
						regions.add(region);
					}
				}
			} catch (Exception ex) {
				throw new GdxRuntimeException("Error reading texture atlas file: " + packFile, ex);
			} finally {
				StreamUtils.closeQuietly(reader);
			}

			if (hasIndexes[0]) {
				regions.sort(new Comparator<Region>() {
					public int compare (Region region1, Region region2) {
						int i1 = region1.index;
						if (i1 == -1) i1 = Integer.MAX_VALUE;
						int i2 = region2.index;
						if (i2 == -1) i2 = Integer.MAX_VALUE;
						return i1 - i2;
					}
				});
			}
		}

		public Array<Page> getPages () {
			return pages;
		}

		public Array<Region> getRegions () {
			return regions;
		}

		static private int readEntry (String[] entry, @Null String line) throws IOException {
			if (line == null) return 0;
			line = line.trim();
			if (line.length() == 0) return 0;
			int colon = line.indexOf(':');
			if (colon == -1) return 0;
			entry[0] = line.substring(0, colon).trim();
			for (int i = 1, lastMatch = colon + 1;; i++) {
				int comma = line.indexOf(',', lastMatch);
				if (comma == -1) {
					entry[i] = line.substring(lastMatch).trim();
					return i;
				}
				entry[i] = line.substring(lastMatch, comma).trim();
				lastMatch = comma + 1;
				if (i == 4) return 4;
			}
		}

		static private interface Field<T> {
			public void parse (T object);
		}

		static public class Page {
			/** May be null if this page isn't associated with a file. In that case, {@link #texture} must be set. */
			public @Null FileHandle textureFile;
			/** May be null if the texture is not yet loaded. */
			public @Null Texture texture;
			public float width, height;
			public boolean useMipMaps;
			public Format format = Format.RGBA8888;
			public TextureFilter minFilter = TextureFilter.Nearest, magFilter = TextureFilter.Nearest;
			public TextureWrap uWrap = TextureWrap.ClampToEdge, vWrap = TextureWrap.ClampToEdge;
			public boolean pma;
		}

		static public class Region {
			public Page page;
			public String name;
			public int left, top, width, height;
			public float offsetX, offsetY;
			public int originalWidth, originalHeight;
			public int degrees;
			public boolean rotate;
			public int index = -1;
			public @Null String[] names;
			public @Null int[][] values;
			public boolean flip;

			public @Null int[] findValue (String name) {
				if (names != null) {
					for (int i = 0, n = names.length; i < n; i++)
						if (name.equals(names[i])) return values[i];
				}
				return null;
			}
		}
	}

	/** Describes the region of a packed image and provides information about the original image before it was packed. */
	static public class AtlasRegion extends TextureRegion {
		/** The number at the end of the original image file name, or -1 if none.<br>
		 * <br>
		 * When sprites are packed, if the original file name ends with a number, it is stored as the index and is not considered as
		 * part of the sprite's name. This is useful for keeping animation frames in order.
		 * @see TextureAtlas#findRegions(String) */
		public int index = -1;

		/** The name of the original image file, without the file's extension.<br>
		 * If the name ends with an underscore followed by only numbers, that part is excluded: underscores denote special
		 * instructions to the texture packer. */
		public String name;

		/** The offset from the left of the original image to the left of the packed image, after whitespace was removed for
		 * packing. */
		public float offsetX;

		/** The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
		 * packing. */
		public float offsetY;

		/** The width of the image, after whitespace was removed for packing. */
		public int packedWidth;

		/** The height of the image, after whitespace was removed for packing. */
		public int packedHeight;

		/** The width of the image, before whitespace was removed and rotation was applied for packing. */
		public int originalWidth;

		/** The height of the image, before whitespace was removed for packing. */
		public int originalHeight;

		/** If true, the region has been rotated 90 degrees counter clockwise. */
		public boolean rotate;

		/** The degrees the region has been rotated, counter clockwise between 0 and 359. Most atlas region handling deals only with
		 * 0 or 90 degree rotation (enough to handle rectangles). More advanced texture packing may support other rotations (eg, for
		 * tightly packing polygons). */
		public int degrees;

		/** Names for name/value pairs other than the fields provided on this class, each entry corresponding to {@link #values}. */
		public @Null String[] names;

		/** Values for name/value pairs other than the fields provided on this class, each entry corresponding to {@link #names}. */
		public @Null int[][] values;

		public AtlasRegion (Texture texture, int x, int y, int width, int height) {
			super(texture, x, y, width, height);
			originalWidth = width;
			originalHeight = height;
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
			rotate = region.rotate;
			degrees = region.degrees;
			names = region.names;
			values = region.values;
		}

		public AtlasRegion (TextureRegion region) {
			setRegion(region);
			packedWidth = region.getRegionWidth();
			packedHeight = region.getRegionHeight();
			originalWidth = packedWidth;
			originalHeight = packedHeight;
		}

		@Override
		/** Flips the region, adjusting the offset so the image appears to be flipped as if no whitespace has been removed for
		 * packing. */
		public void flip (boolean x, boolean y) {
			super.flip(x, y);
			if (x) offsetX = originalWidth - offsetX - getRotatedPackedWidth();
			if (y) offsetY = originalHeight - offsetY - getRotatedPackedHeight();
		}

		/** Returns the packed width considering the {@link #rotate} value, if it is true then it returns the packedHeight,
		 * otherwise it returns the packedWidth. */
		public float getRotatedPackedWidth () {
			return rotate ? packedHeight : packedWidth;
		}

		/** Returns the packed height considering the {@link #rotate} value, if it is true then it returns the packedWidth,
		 * otherwise it returns the packedHeight. */
		public float getRotatedPackedHeight () {
			return rotate ? packedWidth : packedHeight;
		}

		public @Null int[] findValue (String name) {
			if (names != null) {
				for (int i = 0, n = names.length; i < n; i++)
					if (name.equals(names[i])) return values[i];
			}
			return null;
		}

		public String toString () {
			return name;
		}
	}

	/** A sprite that, if whitespace was stripped from the region when it was packed, is automatically positioned as if whitespace
	 * had not been stripped. */
	static public class AtlasSprite extends Sprite {
		final AtlasRegion region;
		float originalOffsetX, originalOffsetY;

		public AtlasSprite (AtlasRegion region) {
			this.region = new AtlasRegion(region);
			originalOffsetX = region.offsetX;
			originalOffsetY = region.offsetY;
			setRegion(region);
			setOrigin(region.originalWidth / 2f, region.originalHeight / 2f);
			int width = region.getRegionWidth();
			int height = region.getRegionHeight();
			if (region.rotate) {
				super.rotate90(true);
				super.setBounds(region.offsetX, region.offsetY, height, width);
			} else
				super.setBounds(region.offsetX, region.offsetY, width, height);
			setColor(1, 1, 1, 1);
		}

		public AtlasSprite (AtlasSprite sprite) {
			region = sprite.region;
			this.originalOffsetX = sprite.originalOffsetX;
			this.originalOffsetY = sprite.originalOffsetY;
			set(sprite);
		}

		@Override
		public void setPosition (float x, float y) {
			super.setPosition(x + region.offsetX, y + region.offsetY);
		}

		@Override
		public void setX (float x) {
			super.setX(x + region.offsetX);
		}

		@Override
		public void setY (float y) {
			super.setY(y + region.offsetY);
		}

		@Override
		public void setBounds (float x, float y, float width, float height) {
			float widthRatio = width / region.originalWidth;
			float heightRatio = height / region.originalHeight;
			region.offsetX = originalOffsetX * widthRatio;
			region.offsetY = originalOffsetY * heightRatio;
			int packedWidth = region.rotate ? region.packedHeight : region.packedWidth;
			int packedHeight = region.rotate ? region.packedWidth : region.packedHeight;
			super.setBounds(x + region.offsetX, y + region.offsetY, packedWidth * widthRatio, packedHeight * heightRatio);
		}

		@Override
		public void setSize (float width, float height) {
			setBounds(getX(), getY(), width, height);
		}

		@Override
		public void setOrigin (float originX, float originY) {
			super.setOrigin(originX - region.offsetX, originY - region.offsetY);
		}

		@Override
		public void setOriginCenter () {
			super.setOrigin(width / 2 - region.offsetX, height / 2 - region.offsetY);
		}

		@Override
		public void flip (boolean x, boolean y) {
			// Flip texture.
			if (region.rotate)
				super.flip(y, x);
			else
				super.flip(x, y);

			float oldOriginX = getOriginX();
			float oldOriginY = getOriginY();
			float oldOffsetX = region.offsetX;
			float oldOffsetY = region.offsetY;

			float widthRatio = getWidthRatio();
			float heightRatio = getHeightRatio();

			region.offsetX = originalOffsetX;
			region.offsetY = originalOffsetY;
			region.flip(x, y); // Updates x and y offsets.
			originalOffsetX = region.offsetX;
			originalOffsetY = region.offsetY;
			region.offsetX *= widthRatio;
			region.offsetY *= heightRatio;

			// Update position and origin with new offsets.
			translate(region.offsetX - oldOffsetX, region.offsetY - oldOffsetY);
			setOrigin(oldOriginX, oldOriginY);
		}

		@Override
		public void rotate90 (boolean clockwise) {
			// Rotate texture.
			super.rotate90(clockwise);

			float oldOriginX = getOriginX();
			float oldOriginY = getOriginY();
			float oldOffsetX = region.offsetX;
			float oldOffsetY = region.offsetY;

			float widthRatio = getWidthRatio();
			float heightRatio = getHeightRatio();

			if (clockwise) {
				region.offsetX = oldOffsetY;
				region.offsetY = region.originalHeight * heightRatio - oldOffsetX - region.packedWidth * widthRatio;
			} else {
				region.offsetX = region.originalWidth * widthRatio - oldOffsetY - region.packedHeight * heightRatio;
				region.offsetY = oldOffsetX;
			}

			// Update position and origin with new offsets.
			translate(region.offsetX - oldOffsetX, region.offsetY - oldOffsetY);
			setOrigin(oldOriginX, oldOriginY);
		}

		@Override
		public float getX () {
			return super.getX() - region.offsetX;
		}

		@Override
		public float getY () {
			return super.getY() - region.offsetY;
		}

		@Override
		public float getOriginX () {
			return super.getOriginX() + region.offsetX;
		}

		@Override
		public float getOriginY () {
			return super.getOriginY() + region.offsetY;
		}

		@Override
		public float getWidth () {
			return super.getWidth() / region.getRotatedPackedWidth() * region.originalWidth;
		}

		@Override
		public float getHeight () {
			return super.getHeight() / region.getRotatedPackedHeight() * region.originalHeight;
		}

		public float getWidthRatio () {
			return super.getWidth() / region.getRotatedPackedWidth();
		}

		public float getHeightRatio () {
			return super.getHeight() / region.getRotatedPackedHeight();
		}

		public AtlasRegion getAtlasRegion () {
			return region;
		}

		public String toString () {
			return region.toString();
		}
	}
}

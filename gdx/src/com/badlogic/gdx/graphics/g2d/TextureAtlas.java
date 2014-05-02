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

import static com.badlogic.gdx.graphics.Texture.TextureWrap.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.StreamUtils;

/** A newer implementation of a texture atlas loader, designed primarily to load from json files. Also capable of loading legacy
 * atlas files created by TexturePacker. <br>
 * A TextureAtlas must be disposed to free up the resources consumed by the backing textures.
 * @author Nicholas Hydock
 * @author Nathan Sweet */
public class TextureAtlas implements Disposable {

	/** Container class of the loaded texture atlas data */
	public static class TextureAtlasData implements Json.Serializable {

		protected FileHandle imgDir;
		protected Array<Page> pages;
		protected Array<Region> regions;

		// file loader/parser of json data
		private static Json loader = new Json();

		public TextureAtlasData () {
			pages = new Array<Page>();
			regions = new Array<Region>();
		}

		/** Generates TextureAtlasData from a file
		 * @param file
		 * @param imgDir
		 * @param flip */
		public static TextureAtlasData load (FileHandle file, FileHandle imgDir, boolean flip) {
			TextureAtlasData d;
			try {
				d = loader.fromJson(TextureAtlasData.class, file);
			}
			// if the file isn't a json, we fallback to old loading techniques
			catch (com.badlogic.gdx.utils.SerializationException e) {
				d = TextureAtlasData.LegacyParser.load(file, flip);
			}

			if (d != null) {
				d.imgDir = imgDir;
			}

			return d;
		}
		
		/** Force loads all the texture assets used by this atlas
		 * @param imgDir
		 * @return the input atlas with all resources loaded
		 */
		public static TextureAtlasData loadAssets(TextureAtlasData d, FileHandle imgDir)
		{
			for (Page p : d.pages) {
				FileHandle texFile = d.imgDir.child(p.texturePath);
				p.setTexture(texFile);
			}
			return d;			
		}

		@Override
		public void write (Json json) {
			for (Page p : pages) {
				json.writeValue(p.texturePath, p);
			}
		}

		@Override
		public void read (Json json, JsonValue jsonData) {
			pages = new Array<Page>();
			regions = new Array<Region>();

			for (int i = 0; i < jsonData.size; i++) {
				JsonValue pageData = jsonData.get(i);
				Page page = new Page();
				page.read(json, pageData);
				regions.addAll(page.regions);
				pages.add(page);
			}
		}

		public void addPage (TextureAtlasData.Page page) {
			regions.addAll(page.regions);
			pages.add(page);
		}

		public Array<Page> getPages () {
			return pages;
		}

		public Array<Region> getRegions () {
			return regions;
		}

		/** Parser utility used for reading in legacy atlas files */
		private static class LegacyParser {
			private static final String[] tuple = new String[4];

			protected static TextureAtlasData load (FileHandle packFile, boolean flip) {
				TextureAtlasData data = new TextureAtlasData();
				BufferedReader reader = new BufferedReader(new InputStreamReader(packFile.read()), 64);
				try {
					Page page = null;
					while (true) {
						String line = reader.readLine();
						if (line == null) break;
						if (line.trim().length() == 0)
							page = null;
						else if (page == null) {
							page = new Page();

							page.texturePath = line;
							page.format = Format.valueOf(readValue(reader));

							readTuple(reader);
							page.minFilter = TextureFilter.valueOf(tuple[0]);
							page.magFilter = TextureFilter.valueOf(tuple[1]);

							String direction = readValue(reader);
							if (direction.equals("x"))
								page.uWrap = Repeat;
							else if (direction.equals("y"))
								page.vWrap = Repeat;
							else if (direction.equals("xy")) {
								page.uWrap = Repeat;
								page.vWrap = Repeat;
							} else {
								page.uWrap = ClampToEdge;
								page.vWrap = ClampToEdge;
							}

							page.useMipMaps = page.minFilter.isMipMap();
							data.pages.add(page);
						} else {
							Region region = new Region();

							region.name = line;
							region.rotate = Boolean.valueOf(readValue(reader));

							readTuple(reader);
							// left
							region.left = Integer.parseInt(tuple[0]);
							// top
							region.top = Integer.parseInt(tuple[1]);

							readTuple(reader);
							// width
							region.width = Integer.parseInt(tuple[0]);
							// height
							region.height = Integer.parseInt(tuple[1]);

							if (readTuple(reader) == 4) { // split is optional
								region.splits = new int[] {Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
									Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

								if (readTuple(reader) == 4) { // pad is optional, but only present with splits
									region.pads = new int[] {Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
										Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

									readTuple(reader);
								}
							}

							// original width
							region.originalWidth = Integer.parseInt(tuple[0]);
							// original height
							region.originalHeight = Integer.parseInt(tuple[1]);

							readTuple(reader);
							// x offset
							region.offsetX = Integer.parseInt(tuple[0]);
							// y offset
							region.offsetY = Integer.parseInt(tuple[1]);

							region.index = Integer.parseInt(readValue(reader));

							if (flip) region.flip = true;

							page.regions.add(region);
							region.page = page;

							data.regions.add(region);
						}
					}
				} catch (Exception ex) {
					throw new GdxRuntimeException("Error reading pack file: " + packFile, ex);
				} finally {
					StreamUtils.closeQuietly(reader);
				}
				return data;
			}

			/** Returns the number of tuple values read (2 or 4). */
			private static int readTuple (BufferedReader reader) throws IOException {
				String line = reader.readLine();
				int colon = line.indexOf(':');
				if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
				int i = 0, lastMatch = colon + 1;
				for (i = 0; i < 3; i++) {
					int comma = line.indexOf(',', lastMatch);
					if (comma == -1) {
						if (i == 0) throw new GdxRuntimeException("Invalid line: " + line);
						break;
					}
					tuple[i] = line.substring(lastMatch, comma).trim();
					lastMatch = comma + 1;
				}
				tuple[i] = line.substring(lastMatch).trim();
				return i + 1;
			}

			/** Reads a standard value from a line. Scans the current line and advances the parser
			 * @return String - read in value
			 * @throws IOException */
			private static String readValue (BufferedReader reader) throws IOException {
				String line = reader.readLine();
				int colon = line.indexOf(':');
				if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
				return line.substring(colon + 1).trim();
			}
		}

		public static class Page implements Json.Serializable {

			protected Texture texture;
			protected String texturePath;
			protected FileHandle textureFile;
			protected boolean useMipMaps;
			protected Format format;
			protected TextureFilter minFilter;
			protected TextureFilter magFilter;
			protected TextureWrap uWrap;
			protected TextureWrap vWrap;
			protected Array<TextureAtlasData.Region> regions;

			public Page () {
				regions = new Array<TextureAtlasData.Region>();
			}

			/** Create a page by strictly knowing all of its values. */
			public Page (String textureFile, boolean useMipMaps, Format format, TextureFilter minFilter, TextureFilter magFilter,
				TextureWrap uWrap, TextureWrap vWrap, Array<Region> regions) {
				this.texturePath = textureFile;
				this.useMipMaps = useMipMaps;
				this.format = format;
				this.minFilter = minFilter;
				this.magFilter = magFilter;
				this.uWrap = uWrap;
				this.vWrap = vWrap;
				this.regions = regions;
			}

			public void addRegion (TextureAtlasData.Region r) {
				this.regions.add(r);
				r.page = this;
			}

			/** Assign a texture associated with this page and all regions attached to this page.
			 * @param texFile */
			public void setTexture (FileHandle texFile) {
				this.textureFile = texFile;
				this.texture = new Texture(texFile);
				this.texture.setFilter(minFilter, magFilter);
				this.texture.setWrap(uWrap, vWrap);
				for (TextureAtlasData.Region r : regions) {
					r.setTexture(texture);
				}
			}

			/** Generates a TextureParameter set for the page. Used for asset loaders */
			public TextureParameter getTextureParams () {
				TextureParameter params = new TextureParameter();
				params.format = format;
				params.genMipMaps = useMipMaps;
				params.minFilter = minFilter;
				params.magFilter = magFilter;
				return params;
			}
			
			public Texture getTexture() {
				return texture;
			}
			
			public FileHandle getTextureFile() {
				return textureFile;
			}

			@Override
			public void write (Json json) {
				json.writeValue("useMipMaps", this.useMipMaps);
				json.writeValue("format", this.format.name());

				json.writeObjectStart("filter");
				json.writeValue("min", this.minFilter.name());
				json.writeValue("mag", this.magFilter.name());
				json.writeObjectEnd();

				json.writeObjectStart("repeat");
				json.writeValue("x", this.uWrap.name());
				json.writeValue("y", this.vWrap.name());
				json.writeObjectEnd();

				json.writeValue("regions", regions);
			}

			@Override
			public void read (Json json, JsonValue jsonData) {
				this.texturePath = jsonData.name;

				this.useMipMaps = jsonData.getBoolean("useMipMaps", false);
				this.format = Format.valueOf(jsonData.getString("format", Format.RGBA8888.name()));

				// read in filters
				{
					JsonValue tmp = jsonData.get("filter");
					this.minFilter = TextureFilter.valueOf(tmp.getString("min", TextureFilter.Nearest.name()));
					this.magFilter = TextureFilter.valueOf(tmp.getString("mag", TextureFilter.Nearest.name()));
				}

				// read in wrapping
				{
					JsonValue tmp = jsonData.get("repeat");
					this.uWrap = TextureWrap.valueOf(tmp.getString("x", TextureWrap.ClampToEdge.name()));
					this.vWrap = TextureWrap.valueOf(tmp.getString("y", TextureWrap.ClampToEdge.name()));
				}

				// parse out the regions from the page
				JsonValue regionList = jsonData.get("regions");
				this.regions = new Array<TextureAtlasData.Region>();
				for (int n = 0; n < regionList.size; n++) {
					JsonValue regionData = regionList.get(n);
					TextureAtlasData.Region region = json.fromJson(TextureAtlasData.Region.class, regionData.toString());
					this.addRegion(region);
				}
			}

			public String getFileName () {
				if (this.textureFile != null) {
					return this.textureFile.parent().path() + "/" + this.texturePath;
				}
				else {
					return this.texturePath;	
				}
			}
		}

		/** Defines an area of a page which is recognized as a drawable bit of data
		 * @author nhydock */
		public static class Region implements Json.Serializable {
			protected Page page;
			protected int index;
			protected String name;

			protected int offsetX, offsetY;
			protected int originalWidth, originalHeight;
			protected int packedWidth, packedHeight;
			protected int left, top, width, height;
			protected int[] splits;
			protected int[] pads;
			protected boolean flip;
			protected boolean rotate;

			protected TextureRegion texRegion;

			public Region (String name, int index, int x, int y, int width, int height, int offsetX, int offsetY, int oWidth,
				int oHeight, int[] splits, int[] pads, boolean flip, boolean rotate) {
				this.name = name;
				this.index = index;
				this.offsetX = offsetX;
				this.offsetY = offsetY;
				this.originalWidth = width;
				this.originalHeight = height;
				this.packedWidth = oWidth;
				this.packedHeight = oHeight;
				this.left = x;
				this.top = y;
				this.width = width;
				this.height = height;
				this.splits = splits;
				this.pads = pads;
				this.flip = flip;
				this.rotate = rotate;
			}

			/** Clones a region
			 * @param r */
			protected Region (Region r) {
				this();
				this.offsetX = r.offsetX;
				this.offsetY = r.offsetY;
				this.originalWidth = r.originalWidth;
				this.originalHeight = r.originalHeight;

				this.left = r.left;
				this.top = r.top;
				this.width = r.width;
				this.height = r.height;
				
				if (r.splits != null) {
					this.splits = new int[r.splits.length];
					for (int i = 0; i < r.splits.length; i++)
						this.splits[i] = r.splits[i];

					if (r.pads != null) {
						this.pads = new int[r.pads.length];
						for (int i = 0; i < r.pads.length; i++)
							this.pads[i] = r.pads[i];

					}
				}

				this.flip = r.flip;
				this.rotate = r.rotate;
				this.index = r.index;
				this.name = "" + r.name;
				this.page = r.page;

				this.packedWidth = r.packedWidth;
				this.packedHeight = r.packedHeight;

				this.texRegion = new TextureRegion(r.texRegion);
			}

			protected Region () {
				flip = false;
				rotate = false;
			}

			protected void setTexture (Texture t) {
				texRegion = new TextureRegion(page.texture, left, top, width, height);
			}

			public String getName () {
				return name;
			}
			
			public Page getPage() {
				return page;
			}

			@Override
			public void write (Json json) {
				json.writeValue("name", this.name);
				json.writeValue("index", this.index);

				// write the offset
				json.writeValue("offsetX", this.offsetX);
				json.writeValue("offsetY", this.offsetY);

				// write the region box
				json.writeObjectStart("box");
				json.writeValue("left", this.left);
				json.writeValue("top", this.top);
				json.writeValue("width", this.width);
				json.writeValue("height", this.height);
				json.writeObjectEnd();
				
				json.writeValue("flip", this.flip);
				json.writeValue("rotate", this.rotate);

				// write split
				if (this.splits != null) {
					json.writeValue("split", this.splits);
					if (this.pads != null) {
						json.writeValue("padding", this.pads);
					}
				}

				// write original size
				json.writeObjectStart("original");
				json.writeValue("width", this.originalWidth);
				json.writeValue("height", this.originalHeight);
				json.writeObjectEnd();
			}

			@Override
			public void read (Json json, JsonValue jsonData) {
				this.name = jsonData.getString("name");

				// set index/related name
				this.index = jsonData.getInt("index", 0);

				// read in offset
				this.offsetX = jsonData.getInt("offsetX");
				this.offsetY = jsonData.getInt("offsetY");
			
				// read region cropping area
				{
					JsonValue tmp = jsonData.get("box");
					this.left = tmp.getInt("left");
					this.top = tmp.getInt("top");
					this.width = tmp.getInt("width");
					this.height = tmp.getInt("height");
				}

				flip = jsonData.getBoolean("flip", false);
				rotate = jsonData.getBoolean("rotate", false);

				// read split and padding
				if (jsonData.has("split")) {
					{
						JsonValue tmp = jsonData.get("split");
						this.splits = tmp.asIntArray();
					}
					// read padding only if split exists
					if (jsonData.has("padding")) {
						JsonValue tmp = jsonData.get("padding");
						this.pads = tmp.asIntArray();
					}
				}

				// read original size
				{
					JsonValue tmp = jsonData.get("original");
					this.originalWidth = this.packedWidth = tmp.getInt("width");
					this.originalHeight = this.packedHeight = tmp.getInt("height");
				}
			}

			public int getWidth () {
				return this.width;
			}
			
			public int getHeight() {
				return this.height;
			}
			
			public int getLeft() {
				return this.left;
			}
			
			public int getTop() {
				return this.top;
			}

			public boolean getRotate () {
				return this.rotate;
			}
			
			public int getIndex() {
				return this.index;
			}
		}
	}

	private ObjectSet<Texture> textures;
	private TextureAtlasData src;
	private Array<AtlasRegion> regions;

	public TextureAtlas () {
		this(new TextureAtlasData());
	}

	/** Loads the specified pack file using {@link FileType#Internal}, using the parent directory of the pack file to find the page
	 * images. */
	public TextureAtlas (String internalPackFile) {
		this(Gdx.files.internal(internalPackFile));
	}

	/** Loads the specified pack file, using the parent directory of the pack file to find the page images. */
	public TextureAtlas (FileHandle packFile) {
		this(packFile, packFile.parent(), false);
	}

	/** @param data May be null. */
	public TextureAtlas (TextureAtlasData data) {
		if (data != null) load(data);
	}

	/** Creates a new texture atlas when given a file handle */
	public TextureAtlas (FileHandle file, FileHandle imgDir, boolean flip) {
		TextureAtlasData d = TextureAtlasData.load(file, imgDir, flip);

		if (d != null) {
			TextureAtlasData.loadAssets(d, imgDir);
			load(d);
		}
	}

	/** Load all the data
	 * @param data */
	public void load (TextureAtlasData data) {
		this.src = data;
		this.textures = new ObjectSet<Texture>();
		this.regions = new Array<AtlasRegion>();

		for (TextureAtlasData.Page p : data.pages) {
			this.textures.add(p.texture);
			for (TextureAtlasData.Region r : p.regions) {
				this.regions.add(new AtlasRegion(r));
			}
		}

	}

	/** Returns all regions in the atlas. */
	public Array<AtlasRegion> getRegions () {
		return regions;
	}

	/** Returns the first region found with the specified name. This method uses string comparison to find the region, so the result
	 * should be cached rather than calling this method multiple times.
	 * @return The region, or null. */
	public AtlasRegion findRegion (String name) {
		for (int i = 0, n = regions.size; i < n; i++)
			if (regions.get(i).name.equals(name)) return regions.get(i);
		return null;
	}

	/** Returns the first region found with the specified name and index. This method uses string comparison to find the region, so
	 * the result should be cached rather than calling this method multiple times.
	 * @return The region, or null. */
	public AtlasRegion findRegion (String name, int index) {
		for (int i = 0, n = regions.size; i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (!region.name.equals(name)) continue;
			if (region.index != index) continue;
			return region;
		}
		return null;
	}

	/** Returns all regions with the specified name, ordered by smallest to largest {@link AtlasRegion#index index}. This method
	 * uses string comparison to find the regions, so the result should be cached rather than calling this method multiple times. */
	public Array<AtlasRegion> findRegions (String name) {
		Array<AtlasRegion> matched = new Array();
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
		Array sprites = new Array(regions.size);
		for (int i = 0, n = regions.size; i < n; i++)
			sprites.add(newSprite(regions.get(i)));
		return sprites;
	}

	/** Returns the first region found with the specified name as a sprite. If whitespace was stripped from the region when it was
	 * packed, the sprite is automatically positioned as if whitespace had not been stripped. This method uses string comparison to
	 * find the region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
	 * @return The sprite, or null. */
	public Sprite createSprite (String name) {
		for (int i = 0, n = regions.size; i < n; i++)
			if (regions.get(i).name.equals(name)) return newSprite(regions.get(i));
		return null;
	}

	/** Returns the first region found with the specified name and index as a sprite. This method uses string comparison to find the
	 * region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
	 * @return The sprite, or null.
	 * @see #createSprite(String) */
	public Sprite createSprite (String name, int index) {
		for (int i = 0, n = regions.size; i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (!region.name.equals(name)) continue;
			if (region.index != index) continue;
			return newSprite(regions.get(i));
		}
		return null;
	}

	/** Returns all regions with the specified name as sprites, ordered by smallest to largest {@link AtlasRegion#index index}. This
	 * method uses string comparison to find the regions and constructs new sprites, so the result should be cached rather than
	 * calling this method multiple times.
	 * @see #createSprite(String) */
	public Array<Sprite> createSprites (String name) {
		Array<Sprite> matched = new Array();
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
	 * be cached rather than calling this method multiple times.
	 * @return The ninepatch, or null. */
	public NinePatch createPatch (String name) {
		for (int i = 0, n = regions.size; i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (region.name.equals(name)) {
				int[] splits = region.splits;
				if (splits == null) throw new IllegalArgumentException("Region does not have ninepatch splits: " + name);
				NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
				if (region.pads != null) patch.setPadding(region.pads[0], region.pads[1], region.pads[2], region.pads[3]);
				return patch;
			}
		}
		return null;
	}

	/** @return the textures of the pages, unordered */
	public ObjectSet<Texture> getTextures () {
		return textures;
	}

	/** Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed. */
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

	/** Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed. */
	public AtlasRegion addRegion (String name, TextureRegion textureRegion) {
		return addRegion(name, textureRegion.texture, textureRegion.getRegionX(), textureRegion.getRegionY(),
			textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
	}

	/** Releases all resources associated with this TextureAtlas instance. This releases all the textures backing all TextureRegions
	 * and Sprites, which should no longer be used after calling dispose. */
	public void dispose () {
		for (Texture texture : textures)
			texture.dispose();
		textures.clear();
	}

	static final Comparator<Region> indexComparator = new Comparator<Region>() {
		public int compare (Region region1, Region region2) {
			int i1 = region1.index;
			if (i1 == -1) i1 = Integer.MAX_VALUE;
			int i2 = region2.index;
			if (i2 == -1) i2 = Integer.MAX_VALUE;
			return i1 - i2;
		}
	};

	/** Describes the region of a packed image and provides information about the original image before it was packed. */
	static public class AtlasRegion extends TextureRegion {
		/** The number at the end of the original image file name, or -1 if none.<br>
		 * <br>
		 * When sprites are packed, if the original file name ends with a number, it is stored as the index and is not considered as
		 * part of the sprite's name. This is useful for keeping animation frames in order.
		 * @see TextureAtlas#findRegions(String) */
		public int index;

		/** The name of the original image file, up to the first underscore. Underscores denote special instructions to the texture
		 * packer. */
		public String name;

		/** The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing. */
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

		/** The ninepatch splits, or null if not a ninepatch. Has 4 elements: left, right, top, bottom. */
		public int[] splits;

		/** The ninepatch pads, or null if not a ninepatch or the has no padding. Has 4 elements: left, right, top, bottom. */
		public int[] pads;

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
			splits = region.splits;
		}

		public AtlasRegion (TextureAtlasData.Region region) {
			setRegion(region.texRegion);
			index = region.index;
			name = region.name;
			offsetX = region.offsetX;
			offsetY = region.offsetY;
			packedWidth = region.packedWidth;
			packedHeight = region.packedHeight;
			originalWidth = region.originalWidth;
			originalHeight = region.originalHeight;
			rotate = region.rotate;
			splits = region.splits;
		}

		/** Flips the region, adjusting the offset so the image appears to be flip as if no whitespace has been removed for packing. */
		public void flip (boolean x, boolean y) {
			super.flip(x, y);
			if (x) offsetX = originalWidth - offsetX - getRotatedPackedWidth();
			if (y) offsetY = originalHeight - offsetY - getRotatedPackedHeight();
		}

		/** Returns the packed width considering the rotate value, if it is true then it returns the packedHeight, otherwise it
		 * returns the packedWidth. */
		public float getRotatedPackedWidth () {
			return rotate ? packedHeight : packedWidth;
		}

		/** Returns the packed height considering the rotate value, if it is true then it returns the packedWidth, otherwise it
		 * returns the packedHeight. */
		public float getRotatedPackedHeight () {
			return rotate ? packedWidth : packedHeight;
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

		public void setPosition (float x, float y) {
			super.setPosition(x + region.offsetX, y + region.offsetY);
		}

		public void setX (float x) {
			super.setX(x + region.offsetX);
		}

		public void setY (float y) {
			super.setY(y + region.offsetY);
		}

		public void setBounds (float x, float y, float width, float height) {
			float widthRatio = width / region.originalWidth;
			float heightRatio = height / region.originalHeight;
			region.offsetX = originalOffsetX * widthRatio;
			region.offsetY = originalOffsetY * heightRatio;
			int packedWidth = region.rotate ? region.packedHeight : region.packedWidth;
			int packedHeight = region.rotate ? region.packedWidth : region.packedHeight;
			super.setBounds(x + region.offsetX, y + region.offsetY, packedWidth * widthRatio, packedHeight * heightRatio);
		}

		public void setSize (float width, float height) {
			setBounds(getX(), getY(), width, height);
		}

		public void setOrigin (float originX, float originY) {
			super.setOrigin(originX - region.offsetX, originY - region.offsetY);
		}

		public void flip (boolean x, boolean y) {
			// Flip texture.
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

		public float getX () {
			return super.getX() - region.offsetX;
		}

		public float getY () {
			return super.getY() - region.offsetY;
		}

		public float getOriginX () {
			return super.getOriginX() + region.offsetX;
		}

		public float getOriginY () {
			return super.getOriginY() + region.offsetY;
		}

		public float getWidth () {
			return super.getWidth() / region.getRotatedPackedWidth() * region.originalWidth;
		}

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
	}
}

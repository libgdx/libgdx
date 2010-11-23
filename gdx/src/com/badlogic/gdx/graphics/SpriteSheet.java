
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

import static com.badlogic.gdx.graphics.Texture.TextureFilter.*;
import static com.badlogic.gdx.graphics.Texture.TextureWrap.*;

// BOZO - Load on the fly feature.

/**
 * Loads images from texture atlases created by SpriteSheetPacker.<br>
 * <br>
 * A SpriteSheet must be disposed to free up the resources consumed by the backing textures.
 */
public class SpriteSheet {
	static private final String[] tuple = new String[2];

	private final ArrayList<Texture> textures = new ArrayList(4);
	private final PackedSprite[] images;

	public SpriteSheet (FileHandle imagesDir) {
		this(imagesDir.child("pack"), imagesDir);
	}

	public SpriteSheet (FileHandle imagesDir, boolean flip) {
		this(imagesDir.child("pack"), imagesDir, flip);
	}

	public SpriteSheet (FileHandle packFile, FileHandle imagesDir) {
		this(packFile, imagesDir, false);
	}

	public SpriteSheet (FileHandle packFile, FileHandle imagesDir, boolean flip) {
		PriorityQueue<PackedSprite> sortedSprites = new PriorityQueue(16, indexComparator);

		BufferedReader reader = new BufferedReader(new InputStreamReader(packFile.read()), 64);
		try {
			Sprite pageImage = null;
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

					Texture texture = Gdx.graphics.newTexture(file, min, max, repeatX, repeatY);
					textures.add(texture);

					pageImage = new Sprite(texture);
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

					PackedSprite sprite;
					if (rotate) {
						sprite = new PackedSprite(pageImage, left, top, height, width);
						sprite.rotate90(true);
					} else
						sprite = new PackedSprite(pageImage, left, top, width, height);
					sprite.name = line;

					readTuple(reader);
					sprite.originalWidth = Integer.parseInt(tuple[0]);
					sprite.originalHeight = Integer.parseInt(tuple[1]);

					readTuple(reader);
					sprite.offsetX = Integer.parseInt(tuple[0]);
					sprite.offsetY = Integer.parseInt(tuple[1]);
					sprite.setPosition(sprite.offsetX, sprite.offsetY);

					sprite.index = Integer.parseInt(readValue(reader));
					if (sprite.index == -1) sprite.index = Integer.MAX_VALUE;

					sortedSprites.add(sprite);
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

		int n = sortedSprites.size();
		images = new PackedSprite[n];
		for (int i = 0; i < n; i++)
			images[i] = sortedSprites.poll();
	}

	/**
	 * Returns the first sprite found with the specified name.<br>
	 * <br>
	 * This method uses string comparison to find the sprite, so the result should be cached rather than calling this method every
	 * frame.
	 */
	public PackedSprite get (String name) {
		for (int i = 0, n = images.length; i < n; i++)
			if (images[i].name.equals(name)) return images[i];
		return null;
	}

	/**
	 * Returns all sprites found with the specified name, ordered by smallest to largest {@link PackedSprite#getIndex() index}.<br>
	 * <br>
	 * This method uses string comparison to find the sprite, so the result should be cached rather than calling this method every
	 * frame.
	 */
	public List<PackedSprite> getAll (String name) {
		ArrayList<PackedSprite> matched = new ArrayList();
		for (int i = 0, n = images.length; i < n; i++)
			if (images[i].name.equals(name)) matched.add(images[i]);
		return matched;
	}

	/**
	 * Releases all resources associated with this PackedSprite instance. This releases all the textures backing all the sprites,
	 * so the sprites should no longer be used after calling dispose.
	 */
	public void dispose () {
		for (int i = 0, n = textures.size(); i < n; i++)
			textures.get(i).dispose();
	}

	static private final Comparator<PackedSprite> indexComparator = new Comparator<PackedSprite>() {
		public int compare (PackedSprite image1, PackedSprite image2) {
			return image1.index - image2.index;
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
	 * A sprite that provides additional information about the packed image it represents. A PackedSprite's position is relative to
	 * the bottom left of the original image, before whitespace was removed for packing.
	 */
	static public class PackedSprite extends Sprite {
		int index;
		String name;
		int offsetX, offsetY;
		int originalWidth, originalHeight;

		PackedSprite (Sprite image, int textureLeft, int textureTop, int textureRight, int textureBottom) {
			super(image, textureLeft, textureTop, textureRight, textureBottom);
		}

		public void setPosition (float x, float y) {
			super.setPosition(x + offsetX, y + offsetY);
		}

		public void setBounds (float x, float y, float width, float height) {
			super.setBounds(x + offsetX, y + offsetY, width, height);
		}

		public void setOrigin (float originX, float originY) {
			super.setOrigin(originX + offsetX, originY + offsetY);
		}

		public float getX () {
			return super.getX() - offsetX;
		}

		public float getY () {
			return super.getY() - offsetY;
		}

		public void flip (boolean x, boolean y) {
			super.flip(x, y);
			if (x) {
				float xPosition = getX();
				offsetX = (int)(originalWidth - offsetX - getWidth());
				setPosition(xPosition, getY());
			}
			if (y) {
				float yPosition = getY();
				offsetY = (int)(originalHeight - offsetY - getHeight());
				setPosition(getX(), yPosition);
			}
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
		 * @see SpriteSheet#getAll(String)
		 */
		public int getIndex () {
			return index;
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
		public int getOffsetX () {
			return offsetX;
		}

		/**
		 * The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
		 * packing.
		 */
		public int getOffsetY () {
			return offsetY;
		}
	}
}

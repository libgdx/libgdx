
package com.badlogic.gdx.graphics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.*;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.*;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Loads images from texture atlases created by SpriteSheetPacker.<br>
 * <br>
 * A SpriteSheet must be disposed to free up the resources consumed by the backing textures.
 */
public class SpriteSheet {
	private final ArrayList<Texture> textures = new ArrayList(4);
	private final PackedSprite[] images;

	public SpriteSheet (FileHandle packFile, FileHandle imagesDir) {
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

					// BOZO - Get filter from file?

					String direction = reader.readLine();
					TextureWrap wrapX = ClampToEdge;
					TextureWrap wrapY = ClampToEdge;
					if (direction.equals("x"))
						wrapX = Repeat;
					else if (direction.equals("y"))
						wrapY = Repeat;
					else if (direction.equals("xy")) {
						wrapX = Repeat;
						wrapY = Repeat;
					}

					Texture texture = Gdx.graphics.newTexture(file, Linear, Linear, ClampToEdge, ClampToEdge);
					textures.add(texture);

					pageImage = new Sprite(texture);
				} else {
					int left = Integer.parseInt(reader.readLine());
					int top = Integer.parseInt(reader.readLine());
					int width = Integer.parseInt(reader.readLine());
					int height = Integer.parseInt(reader.readLine());
					int offsetX = Integer.parseInt(reader.readLine());
					int offsetY = Integer.parseInt(reader.readLine());
					int originalWidth = Integer.parseInt(reader.readLine());
					int originalHeight = Integer.parseInt(reader.readLine());
					PackedSprite image = new PackedSprite(pageImage, left, top, width, height);
					image.setPosition(offsetX, offsetY);
					image.name = line;
					image.offsetX = offsetX;
					image.offsetY = offsetY;
					image.originalWidth = originalWidth;
					image.originalHeight = originalHeight;
					image.index = Integer.parseInt(reader.readLine());
					if (image.index == -1) image.index = Integer.MAX_VALUE;
					sortedSprites.add(image);
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

		// BOZO - Test offset works and flip is handled.
		public void setPosition (float x, float y) {
			super.setPosition(x + offsetX, y + offsetY);
		}

		public void setBounds (float x, float y, float width, float height) {
			super.setBounds(x + offsetX, y + offsetY, width, height);
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

		public int getOriginalWidth () {
			return originalWidth;
		}

		public int getOriginalHeight () {
			return originalHeight;
		}

		/**
		 * The offset from the left of the original image to the left of the packed image, after whitespace has been removed.
		 */
		public int getOffsetX () {
			return offsetX;
		}

		/**
		 * The offset from the bottom of the original image to the bottom of the packed image, after whitespace has been removed.
		 */
		public int getOffsetY () {
			return offsetY;
		}
	}
}

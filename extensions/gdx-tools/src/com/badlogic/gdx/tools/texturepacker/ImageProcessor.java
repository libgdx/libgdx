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

package com.badlogic.gdx.tools.texturepacker;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.badlogic.gdx.tools.texturepacker.TexturePacker.Alias;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Resampling;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Rect;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.Array;

public class ImageProcessor {
	static private final BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
	static private Pattern indexPattern = Pattern.compile("(.+)_(\\d+)$");

	private final Settings settings;
	private final HashMap<String, Rect> crcs = new HashMap();
	private final Array<Rect> rects = new Array();
	private float scale = 1;
	private Resampling resampling = Resampling.bicubic;

	public ImageProcessor (Settings settings) {
		this.settings = settings;
	}

	/** The image won't be kept in-memory during packing if {@link Settings#limitMemory} is true.
	 * @param rootPath Used to strip the root directory prefix from image file names, can be null. */
	public void addImage (File file, String rootPath) {
		BufferedImage image;
		try {
			image = ImageIO.read(file);
		} catch (IOException ex) {
			throw new RuntimeException("Error reading image: " + file, ex);
		}
		if (image == null) throw new RuntimeException("Unable to read image: " + file);

		String name = file.getAbsolutePath().replace('\\', '/');

		// Strip root dir off front of image path.
		if (rootPath != null) {
			if (!name.startsWith(rootPath)) throw new RuntimeException("Path '" + name + "' does not start with root: " + rootPath);
			name = name.substring(rootPath.length());
		}

		// Strip extension.
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex != -1) name = name.substring(0, dotIndex);

		Rect rect = addImage(image, name);
		if (rect != null && settings.limitMemory) rect.unloadImage(file);
	}

	/** The image will be kept in-memory during packing.
	 * @see #addImage(File, String) */
	public Rect addImage (BufferedImage image, String name) {
		Rect rect = processImage(image, name);

		if (rect == null) {
			if (!settings.silent) System.out.println("Ignoring blank input image: " + name);
			return null;
		}

		if (settings.alias) {
			String crc = hash(rect.getImage(this));
			Rect existing = crcs.get(crc);
			if (existing != null) {
				if (!settings.silent) {
					String rectName = rect.name + (rect.index != -1 ? "_" + rect.index : "");
					String existingName = existing.name + (existing.index != -1 ? "_" + existing.index : "");
					System.out.println(rectName + " (alias of " + existingName + ")");
				}
				existing.aliases.add(new Alias(rect));
				return null;
			}
			crcs.put(crc, rect);
		}

		rects.add(rect);
		return rect;
	}

	public void setScale (float scale) {
		this.scale = scale;
	}

	public void setResampling (Resampling resampling) {
		this.resampling = resampling;
	}

	public Array<Rect> getImages () {
		return rects;
	}

	public void clear () {
		rects.clear();
		crcs.clear();
	}

	/** Returns a rect for the image describing the texture region to be packed, or null if the image should not be packed. */
	Rect processImage (BufferedImage image, String name) {
		if (scale <= 0) throw new IllegalArgumentException("scale cannot be <= 0: " + scale);

		int width = image.getWidth(), height = image.getHeight();

		if (image.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
			BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			newImage.getGraphics().drawImage(image, 0, 0, null);
			image = newImage;
		}

		boolean isPatch = name.endsWith(".9");
		int[] splits = null, pads = null;
		Rect rect = null;
		if (isPatch) {
			// Strip ".9" from file name, read ninepatch split pixels, and strip ninepatch split pixels.
			name = name.substring(0, name.length() - 2);
			splits = getSplits(image, name);
			pads = getPads(image, name, splits);
			// Strip split pixels.
			width -= 2;
			height -= 2;
			BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			newImage.getGraphics().drawImage(image, 0, 0, width, height, 1, 1, width + 1, height + 1, null);
			image = newImage;
		}

		// Scale image.
		if (scale != 1) {
			int originalWidth = width, originalHeight = height;
			width = Math.max(1, Math.round(width * scale));
			height = Math.max(1, Math.round(height * scale));
			BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			if (scale < 1) {
				newImage.getGraphics().drawImage(image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING), 0, 0, null);
			} else {
				Graphics2D g = (Graphics2D)newImage.getGraphics();
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, resampling.value);
				g.drawImage(image, 0, 0, width, height, null);
			}
			image = newImage;
		}

		if (isPatch) {
			// Ninepatches aren't rotated or whitespace stripped.
			rect = new Rect(image, 0, 0, width, height, true);
			rect.splits = splits;
			rect.pads = pads;
			rect.canRotate = false;
		} else {
			rect = stripWhitespace(image);
			if (rect == null) return null;
		}

		// Strip digits off end of name and use as index.
		int index = -1;
		if (settings.useIndexes) {
			Matcher matcher = indexPattern.matcher(name);
			if (matcher.matches()) {
				name = matcher.group(1);
				index = Integer.parseInt(matcher.group(2));
			}
		}

		rect.name = name;
		rect.index = index;
		return rect;
	}

	/** Strips whitespace and returns the rect, or null if the image should be ignored. */
	private Rect stripWhitespace (BufferedImage source) {
		WritableRaster alphaRaster = source.getAlphaRaster();
		if (alphaRaster == null || (!settings.stripWhitespaceX && !settings.stripWhitespaceY))
			return new Rect(source, 0, 0, source.getWidth(), source.getHeight(), false);
		final byte[] a = new byte[1];
		int top = 0;
		int bottom = source.getHeight();
		if (settings.stripWhitespaceY) {
			outer:
			for (int y = 0; y < source.getHeight(); y++) {
				for (int x = 0; x < source.getWidth(); x++) {
					alphaRaster.getDataElements(x, y, a);
					int alpha = a[0];
					if (alpha < 0) alpha += 256;
					if (alpha > settings.alphaThreshold) break outer;
				}
				top++;
			}
			outer:
			for (int y = source.getHeight(); --y >= top;) {
				for (int x = 0; x < source.getWidth(); x++) {
					alphaRaster.getDataElements(x, y, a);
					int alpha = a[0];
					if (alpha < 0) alpha += 256;
					if (alpha > settings.alphaThreshold) break outer;
				}
				bottom--;
			}
			// Leave 1px so nothing is copied into padding.
			if (settings.duplicatePadding) {
				if (top > 0) top--;
				if (bottom < source.getHeight()) bottom++;
			}
		}
		int left = 0;
		int right = source.getWidth();
		if (settings.stripWhitespaceX) {
			outer:
			for (int x = 0; x < source.getWidth(); x++) {
				for (int y = top; y < bottom; y++) {
					alphaRaster.getDataElements(x, y, a);
					int alpha = a[0];
					if (alpha < 0) alpha += 256;
					if (alpha > settings.alphaThreshold) break outer;
				}
				left++;
			}
			outer:
			for (int x = source.getWidth(); --x >= left;) {
				for (int y = top; y < bottom; y++) {
					alphaRaster.getDataElements(x, y, a);
					int alpha = a[0];
					if (alpha < 0) alpha += 256;
					if (alpha > settings.alphaThreshold) break outer;
				}
				right--;
			}
			// Leave 1px so nothing is copied into padding.
			if (settings.duplicatePadding) {
				if (left > 0) left--;
				if (right < source.getWidth()) right++;
			}
		}
		int newWidth = right - left;
		int newHeight = bottom - top;
		if (newWidth <= 0 || newHeight <= 0) {
			if (settings.ignoreBlankImages)
				return null;
			else
				return new Rect(emptyImage, 0, 0, 1, 1, false);
		}
		return new Rect(source, left, top, newWidth, newHeight, false);
	}

	static private String splitError (int x, int y, int[] rgba, String name) {
		throw new RuntimeException("Invalid " + name + " ninepatch split pixel at " + x + ", " + y + ", rgba: " + rgba[0] + ", "
			+ rgba[1] + ", " + rgba[2] + ", " + rgba[3]);
	}

	/** Returns the splits, or null if the image had no splits or the splits were only a single region. Splits are an int[4] that
	 * has left, right, top, bottom. */
	private int[] getSplits (BufferedImage image, String name) {
		WritableRaster raster = image.getRaster();

		int startX = getSplitPoint(raster, name, 1, 0, true, true);
		int endX = getSplitPoint(raster, name, startX, 0, false, true);
		int startY = getSplitPoint(raster, name, 0, 1, true, false);
		int endY = getSplitPoint(raster, name, 0, startY, false, false);

		// Ensure pixels after the end are not invalid.
		getSplitPoint(raster, name, endX + 1, 0, true, true);
		getSplitPoint(raster, name, 0, endY + 1, true, false);

		// No splits, or all splits.
		if (startX == 0 && endX == 0 && startY == 0 && endY == 0) return null;

		// Subtraction here is because the coordinates were computed before the 1px border was stripped.
		if (startX != 0) {
			startX--;
			endX = raster.getWidth() - 2 - (endX - 1);
		} else {
			// If no start point was ever found, we assume full stretch.
			endX = raster.getWidth() - 2;
		}
		if (startY != 0) {
			startY--;
			endY = raster.getHeight() - 2 - (endY - 1);
		} else {
			// If no start point was ever found, we assume full stretch.
			endY = raster.getHeight() - 2;
		}

		if (scale != 1) {
			startX = (int)Math.round(startX * scale);
			endX = (int)Math.round(endX * scale);
			startY = (int)Math.round(startY * scale);
			endY = (int)Math.round(endY * scale);
		}

		return new int[] {startX, endX, startY, endY};
	}

	/** Returns the pads, or null if the image had no pads or the pads match the splits. Pads are an int[4] that has left, right,
	 * top, bottom. */
	private int[] getPads (BufferedImage image, String name, int[] splits) {
		WritableRaster raster = image.getRaster();

		int bottom = raster.getHeight() - 1;
		int right = raster.getWidth() - 1;

		int startX = getSplitPoint(raster, name, 1, bottom, true, true);
		int startY = getSplitPoint(raster, name, right, 1, true, false);

		// No need to hunt for the end if a start was never found.
		int endX = 0;
		int endY = 0;
		if (startX != 0) endX = getSplitPoint(raster, name, startX + 1, bottom, false, true);
		if (startY != 0) endY = getSplitPoint(raster, name, right, startY + 1, false, false);

		// Ensure pixels after the end are not invalid.
		getSplitPoint(raster, name, endX + 1, bottom, true, true);
		getSplitPoint(raster, name, right, endY + 1, true, false);

		// No pads.
		if (startX == 0 && endX == 0 && startY == 0 && endY == 0) {
			return null;
		}

		// -2 here is because the coordinates were computed before the 1px border was stripped.
		if (startX == 0 && endX == 0) {
			startX = -1;
			endX = -1;
		} else {
			if (startX > 0) {
				startX--;
				endX = raster.getWidth() - 2 - (endX - 1);
			} else {
				// If no start point was ever found, we assume full stretch.
				endX = raster.getWidth() - 2;
			}
		}
		if (startY == 0 && endY == 0) {
			startY = -1;
			endY = -1;
		} else {
			if (startY > 0) {
				startY--;
				endY = raster.getHeight() - 2 - (endY - 1);
			} else {
				// If no start point was ever found, we assume full stretch.
				endY = raster.getHeight() - 2;
			}
		}

		if (scale != 1) {
			startX = (int)Math.round(startX * scale);
			endX = (int)Math.round(endX * scale);
			startY = (int)Math.round(startY * scale);
			endY = (int)Math.round(endY * scale);
		}

		int[] pads = new int[] {startX, endX, startY, endY};

		if (splits != null && Arrays.equals(pads, splits)) {
			return null;
		}

		return pads;
	}

	/** Hunts for the start or end of a sequence of split pixels. Begins searching at (startX, startY) then follows along the x or
	 * y axis (depending on value of xAxis) for the first non-transparent pixel if startPoint is true, or the first transparent
	 * pixel if startPoint is false. Returns 0 if none found, as 0 is considered an invalid split point being in the outer border
	 * which will be stripped. */
	static private int getSplitPoint (WritableRaster raster, String name, int startX, int startY, boolean startPoint,
		boolean xAxis) {
		int[] rgba = new int[4];

		int next = xAxis ? startX : startY;
		int end = xAxis ? raster.getWidth() : raster.getHeight();
		int breakA = startPoint ? 255 : 0;

		int x = startX;
		int y = startY;
		while (next != end) {
			if (xAxis)
				x = next;
			else
				y = next;

			raster.getPixel(x, y, rgba);
			if (rgba[3] == breakA) return next;

			if (!startPoint && (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0 || rgba[3] != 255)) splitError(x, y, rgba, name);

			next++;
		}

		return 0;
	}

	static private String hash (BufferedImage image) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");

			// Ensure image is the correct format.
			int width = image.getWidth();
			int height = image.getHeight();
			if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
				BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				newImage.getGraphics().drawImage(image, 0, 0, null);
				image = newImage;
			}

			WritableRaster raster = image.getRaster();
			int[] pixels = new int[width];
			for (int y = 0; y < height; y++) {
				raster.getDataElements(0, y, width, 1, pixels);
				for (int x = 0; x < width; x++)
					hash(digest, pixels[x]);
			}

			hash(digest, width);
			hash(digest, height);

			return new BigInteger(1, digest.digest()).toString(16);
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	static private void hash (MessageDigest digest, int value) {
		digest.update((byte)(value >> 24));
		digest.update((byte)(value >> 16));
		digest.update((byte)(value >> 8));
		digest.update((byte)value);
	}
}

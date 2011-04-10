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
package com.badlogic.gdx.imagepacker;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.utils.MathUtils;

public class TexturePacker {
	static Pattern indexPattern = Pattern.compile(".+_(\\d+)(_.*|$)");

	ArrayList<Image> images = new ArrayList();
	HashMap<String, Image> imageCrcs = new HashMap();
	FileWriter writer;
	int uncompressedSize, compressedSize;
	int xPadding, yPadding;
	final Filter filter;
	int minWidth, minHeight;
	int maxWidth, maxHeight;
	final Settings settings;

	public TexturePacker (Settings settings) {
		this.settings = settings;
		this.filter = new Filter(Direction.none, null, -1, -1, null, null);
	}

	public TexturePacker (Settings settings, File inputDir, Filter filter, File outputDir, File packFile) throws IOException {
		this.settings = settings;
		this.filter = filter;

		// Collect and squeeze images.
		File[] files = inputDir.listFiles(filter);
		if (files == null) return;
		for (File file : files) {
			if (file.isDirectory()) continue;
			String imageName = file.getAbsolutePath().substring(inputDir.getAbsolutePath().length()) + "\n";
			if (imageName.startsWith("/") || imageName.startsWith("\\")) imageName = imageName.substring(1);
			int dotIndex = imageName.lastIndexOf('.');
			if (dotIndex != -1) imageName = imageName.substring(0, dotIndex);
			addImage(ImageIO.read(file), imageName);
		}

		if (images.isEmpty()) return;

		System.out.println(inputDir);
		if (filter.format != null)
			System.out.println("Format: " + filter.format);
		else
			System.out.println("Format: " + settings.defaultFormat + " (default)");
		if (filter.minFilter != null && filter.magFilter != null)
			System.out.println("Filter: " + filter.minFilter + ", " + filter.magFilter);
		else
			System.out.println("Filter: " + settings.defaultFilterMin + ", " + settings.defaultFilterMag + " (default)");
		if (filter.direction != Direction.none) System.out.println("Repeat: " + filter.direction);

		process(outputDir, packFile, inputDir.getName());
	}

	public void addImage (BufferedImage image, String name) {
		Image squeezed = squeeze(image, name);
		if (squeezed != null) {
			if (settings.alias) {
				String crc = hash(squeezed);
				Image existing = imageCrcs.get(crc);
				if (existing != null) {
					existing.aliases.add(squeezed);
					return;
				}
				imageCrcs.put(crc, squeezed);
			}
			images.add(squeezed);
		}
	}

	public void process (File outputDir, File packFile, String prefix) throws IOException {
		if (images.isEmpty()) return;

		minWidth = filter.width != -1 ? filter.width : settings.minWidth;
		minHeight = filter.height != -1 ? filter.height : settings.minHeight;
		maxWidth = filter.width != -1 ? filter.width : settings.maxWidth;
		maxHeight = filter.height != -1 ? filter.height : settings.maxHeight;
		xPadding = images.size() > 1 && !filter.direction.isX() ? settings.padding : 0;
		yPadding = images.size() > 1 && !filter.direction.isY() ? settings.padding : 0;

		outputDir.mkdirs();
		writer = new FileWriter(packFile, true);
		try {
			while (!images.isEmpty())
				if (!writePage(prefix, outputDir)) break;
			if (writer != null) {
				System.out.println("Pixels eliminated: " + (1 - compressedSize / (float)uncompressedSize) * 100 + "%");
				System.out.println();
			}
		} finally {
			writer.close();
		}
	}

	private boolean writePage (String prefix, File outputDir) throws IOException {
		// Try reasonably hard to pack images into the smallest POT size.
		Comparator bestComparator = null;
		Comparator secondBestComparator = imageComparators.get(0);
		int bestWidth = 99999, bestHeight = 99999;
		int secondBestWidth = 99999, secondBestHeight = 99999;
		int bestUsedPixels = 0;
		int width = minWidth, height = minHeight;
		int grownPixels = 0, grownPixels2 = 0;
		int i = 0, ii = 0;
		while (true) {
			for (Comparator comparator : imageComparators) {
				// Pack as many images as possible, sorting the images different ways.
				Collections.sort(images, comparator);
				int usedPixels = insert(null, new ArrayList(images), width, height);
				// Store the best pack, in case not all images fit on the max texture size.
				if (usedPixels > bestUsedPixels) {
					secondBestComparator = comparator;
					secondBestWidth = width;
					secondBestHeight = height;
				}
				// If all images fit and this sort is the best so far, take note.
				if (usedPixels == -1) {
					if (width * height < bestWidth * bestHeight) {
						bestComparator = comparator;
						bestWidth = width;
						bestHeight = height;
					}
				}
			}
			if (width == maxWidth && height == maxHeight) break;
			if (bestComparator != null) break;
			if (settings.pot) {
				// 64,64 -> 128,64 -> 256,64 etc 64,128 -> 64,256 etc -> 128,128 -> 256,128 etc.
				if (i % 3 == 0) {
					grownPixels += MathUtils.nextPowerOfTwo(width + 1) - width;
					width = MathUtils.nextPowerOfTwo(width + 1);
					if (width > maxWidth) {
						i++;
						width -= grownPixels;
						grownPixels = 0;
					}
				} else if (i % 3 == 1) {
					grownPixels += MathUtils.nextPowerOfTwo(height + 1) - height;
					height = MathUtils.nextPowerOfTwo(height + 1);
					if (height > maxHeight) {
						i++;
						height -= grownPixels;
						grownPixels = 0;
					}
				} else {
					ii++;
					if (ii % 2 == 1)
						width = MathUtils.nextPowerOfTwo(width + 1);
					else
						height = MathUtils.nextPowerOfTwo(height + 1);
					i++;
				}
			} else {
				// 64-127,64 -> 64,64-127 -> 128-255,128 -> 128,128-255 etc.
				if (i % 3 == 0) {
					width++;
					grownPixels++;
					if (width == MathUtils.nextPowerOfTwo(width)) {
						width -= grownPixels;
						grownPixels = 0;
						i++;
					}
				} else if (i % 3 == 1) {
					height++;
					grownPixels++;
					if (height == MathUtils.nextPowerOfTwo(height)) {
						height -= grownPixels;
						grownPixels = 0;
						i++;
					}
				} else {
					if (width == MathUtils.nextPowerOfTwo(width) && height == MathUtils.nextPowerOfTwo(height)) ii++;
					if (ii % 2 == 1)
						width++;
					else
						height++;
					i++;
				}
			}
			width = Math.min(maxWidth, width);
			height = Math.min(maxHeight, height);
		}
		if (bestComparator != null) {
			Collections.sort(images, bestComparator);
		} else {
			Collections.sort(images, secondBestComparator);
			bestWidth = secondBestWidth;
			bestHeight = secondBestHeight;
		}
		width = bestWidth;
		height = bestHeight;
		if (settings.pot) {
			width = MathUtils.nextPowerOfTwo(width);
			height = MathUtils.nextPowerOfTwo(height);
		}
		if (width > maxWidth || height > maxHeight) {
			System.out.println("ERROR: Images do not fit on max size: " + maxWidth + "x" + maxHeight);
			return false;
		}

		int type;
		switch (filter.format != null ? filter.format : settings.defaultFormat) {
		case RGBA8888:
		case RGBA4444:
			type = BufferedImage.TYPE_INT_ARGB;
			break;
		case RGB565:
			type = BufferedImage.TYPE_INT_RGB;
			break;
		case Alpha:
			type = BufferedImage.TYPE_BYTE_GRAY;
			break;
		default:
			throw new RuntimeException();
		}

		int imageNumber = 1;
		File outputFile = new File(outputDir, prefix + imageNumber + ".png");
		while (outputFile.exists())
			outputFile = new File(outputDir, prefix + ++imageNumber + ".png");

		writer.write("\n" + outputFile.getName() + "\n");
		Format format;
		if (filter.format != null) {
			writer.write("format: " + filter.format + "\n");
			format = filter.format;
		} else {
			writer.write("format: " + settings.defaultFormat + "\n");
			format = settings.defaultFormat;
		}
		if (filter.minFilter == null || filter.magFilter == null)
			writer.write("filter: " + settings.defaultFilterMin + "," + settings.defaultFilterMag + "\n");
		else
			writer.write("filter: " + filter.minFilter + "," + filter.magFilter + "\n");
		writer.write("repeat: " + filter.direction + "\n");

		BufferedImage canvas = new BufferedImage(width, height, type);
		insert(canvas, images, bestWidth, bestHeight);
		System.out.println("Writing " + canvas.getWidth() + "x" + canvas.getHeight() + ": " + outputFile);
		ImageIO.write(canvas, "png", outputFile);
		compressedSize += canvas.getWidth() * canvas.getHeight();
		return true;
	}

	private int insert (BufferedImage canvas, ArrayList<Image> images, int width, int height) throws IOException {
		if (settings.debug && canvas != null) {
			Graphics g = canvas.getGraphics();
			g.setColor(Color.green);
			g.drawRect(0, 0, width - 1, height - 1);
		}
		// Pretend image is larger so padding on right and bottom edges is ignored.
		if (!filter.direction.isX()) width += xPadding;
		if (!filter.direction.isY()) height += yPadding;
		Node root = new Node(0, 0, width, height);
		int usedPixels = 0;
		for (int i = images.size() - 1; i >= 0; i--) {
			Image image = images.get(i);
			Node node = root.insert(image, false);
			if (node == null) {
				if (settings.rotate) node = root.insert(image, true);
				if (node == null) continue;
			}
			usedPixels += image.getWidth() * image.getHeight();
			images.remove(i);
			if (canvas != null) {
				node.writePackEntry();
				Graphics2D g = (Graphics2D)canvas.getGraphics();
				if (image.rotate) {
					g.translate(node.left, node.top);
					g.rotate(-90 * MathUtils.degreesToRadians);
					g.translate(-node.left, -node.top);
					g.translate(-image.getWidth(), 0);
				}
				if (settings.duplicatePadding) {
					int amount = settings.padding / 2;
					int imageWidth = image.getWidth();
					int imageHeight = image.getHeight();
					g.drawImage(image, node.left, node.top - amount, node.left + imageWidth, node.top, 0, 0, imageWidth, 1, null);
					g.drawImage(image, node.left, node.top + imageHeight, node.left + imageWidth, node.top + imageHeight + amount, 0,
						imageHeight - 1, imageWidth, imageHeight, null);
					g.drawImage(image, node.left - amount, node.top, node.left, node.top + imageHeight, 0, 0, 1, imageHeight, null);
					g.drawImage(image, node.left + imageWidth, node.top, node.left + imageWidth + amount, node.top + imageHeight,
						imageWidth - 1, 0, imageWidth, imageHeight, null);
				}
				g.drawImage(image, node.left, node.top, null);
				if (image.rotate) {
					g.translate(image.getWidth(), 0);
					g.translate(node.left, node.top);
					g.rotate(90 * MathUtils.degreesToRadians);
					g.translate(-node.left, -node.top);
				}
				if (settings.debug) {
					g.setColor(Color.magenta);
					int imageWidth = image.getWidth();
					int imageHeight = image.getHeight();
					if (image.rotate)
						g.drawRect(node.left, node.top, imageHeight - 1, imageWidth - 1);
					else
						g.drawRect(node.left, node.top, imageWidth - 1, imageHeight - 1);
				}
			}
		}
		return images.isEmpty() ? -1 : usedPixels;
	}

	private Image squeeze (BufferedImage source, String name) {
		if (source == null) return null;
		if (!filter.accept(source)) return null;
		uncompressedSize += source.getWidth() * source.getHeight();
		WritableRaster alphaRaster = source.getAlphaRaster();
		if (alphaRaster == null || !settings.stripWhitespace)
			return new Image(name, source, 0, 0, source.getWidth(), source.getHeight());
		final byte[] a = new byte[1];
		int top = 0;
		int bottom = source.getHeight();
		if (!filter.direction.isY()) {
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
		}
		int left = 0;
		int right = source.getWidth();
		if (!filter.direction.isX()) {
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
		}
		int newWidth = right - left;
		int newHeight = bottom - top;
		if (newWidth <= 0 || newHeight <= 0) {
			System.out.println("Ignoring blank input image: " + name);
			return null;
		}
		return new Image(name, source, left, top, newWidth, newHeight);
	}

	static private String hash (BufferedImage image) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			WritableRaster raster = image.getRaster();
			final byte[] pixel = new byte[4];
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					raster.getDataElements(x, y, pixel);
					digest.update(pixel);
				}
			}
			return new BigInteger(1, digest.digest()).toString(16);
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	private class Node {
		int left, top, width, height;
		Node child1, child2;
		Image image;

		public Node (int left, int top, int width, int height) {
			this.left = left;
			this.top = top;
			this.width = width;
			this.height = height;
		}

		/**
		 * Returns true if the image was inserted. If canvas != null, an entry is written to the pack file.
		 */
		public Node insert (Image image, boolean rotate) throws IOException {
			if (this.image != null) return null;
			if (child1 != null) {
				Node newNode = child1.insert(image, rotate);
				if (newNode != null) return newNode;
				return child2.insert(image, rotate);
			}
			int imageWidth = image.getWidth();
			int imageHeight = image.getHeight();
			if (rotate) {
				int temp = imageWidth;
				imageWidth = imageHeight;
				imageHeight = temp;
			}
			int neededWidth = imageWidth + xPadding;
			int neededHeight = imageHeight + yPadding;
			if (neededWidth > width || neededHeight > height) return null;
			if (neededWidth == width && neededHeight == height) {
				this.image = image;
				image.rotate = rotate;
				return this;
			}
			int dw = width - neededWidth;
			int dh = height - neededHeight;
			if (dw > dh) {
				child1 = new Node(left, top, neededWidth, height);
				child2 = new Node(left + neededWidth, top, width - neededWidth, height);
			} else {
				child1 = new Node(left, top, width, neededHeight);
				child2 = new Node(left, top + neededHeight, width, height - neededHeight);
			}
			return child1.insert(image, rotate);
		}

		void writePackEntry () throws IOException {
			writePackEntry(image);
			for (Image alias : image.aliases)
				writePackEntry(alias);
		}

		private void writePackEntry (Image image) throws IOException {
			String imageName = image.name;
			imageName = imageName.replace("\\", "/");

			System.out.println("Packing... " + imageName);

			Matcher matcher = indexPattern.matcher(imageName);
			int index = -1;
			if (matcher.matches()) index = Integer.parseInt(matcher.group(1));

			int underscoreIndex = imageName.indexOf('_');
			if (underscoreIndex != -1) imageName = imageName.substring(0, underscoreIndex);

			writer.write(imageName + "\n");
			writer.write("  rotate: " + image.rotate + "\n");
			writer.write("  xy: " + left + ", " + top + "\n");
			writer.write("  size: " + image.getWidth() + ", " + image.getHeight() + "\n");
			writer.write("  orig: " + image.originalWidth + ", " + image.originalHeight + "\n");
			writer.write("  offset: " + image.offsetX + ", " + image.offsetY + "\n");
			writer.write("  index: " + index + "\n");
		}
	}

	static private class Image extends BufferedImage {
		final String name;
		final int offsetX, offsetY;
		final int originalWidth, originalHeight;
		boolean rotate;
		ArrayList<Image> aliases = new ArrayList();

		public Image (String name, BufferedImage src, int left, int top, int newWidth, int newHeight) {
			super(src.getColorModel(), src.getRaster().createWritableChild(left, top, newWidth, newHeight, 0, 0, null), src
				.getColorModel().isAlphaPremultiplied(), null);
			this.name = name;
			offsetX = left;
			offsetY = top;
			originalWidth = src.getWidth();
			originalHeight = src.getHeight();
		}

		public String toString () {
			return name;
		}
	}

	static private ArrayList<Comparator> imageComparators = new ArrayList();
	static {
		imageComparators.add(new Comparator<Image>() {
			public int compare (Image image1, Image image2) {
				int diff = image1.getHeight() - image2.getHeight();
				if (diff != 0) return diff;
				return image1.getWidth() - image2.getWidth();
			}
		});
		imageComparators.add(new Comparator<Image>() {
			public int compare (Image image1, Image image2) {
				int diff = image1.getWidth() - image2.getWidth();
				if (diff != 0) return diff;
				return image1.getHeight() - image2.getHeight();
			}
		});
		imageComparators.add(new Comparator<Image>() {
			public int compare (Image image1, Image image2) {
				return image1.getWidth() * image1.getHeight() - image2.getWidth() * image2.getHeight();
			}
		});
	}

	static private class Filter implements FilenameFilter {
		Direction direction;
		Format format;
		TextureFilter minFilter;
		TextureFilter magFilter;
		int width = -1;
		int height = -1;
		Settings settings;

		public Filter (Direction direction, Format format, int width, int height, TextureFilter minFilter, TextureFilter magFilter) {
			this.direction = direction;
			this.format = format;
			this.width = width;
			this.height = height;
			this.minFilter = minFilter;
			this.magFilter = magFilter;
		}

		public boolean accept (File dir, String name) {
			switch (direction) {
			case none:
				if (name.contains("_x") || name.contains("_y")) return false;
				break;
			case x:
				if (!name.contains("_x") || name.contains("_xy")) return false;
				break;
			case y:
				if (!name.contains("_y") || name.contains("_xy")) return false;
				break;
			case xy:
				if (!name.contains("_xy")) return false;
				break;
			}

			if (format != null) {
				if (!name.contains("_" + formatToAbbrev.get(format))) return false;
			} else {
				// Return if name has a format.
				for (String f : formatToAbbrev.values())
					if (name.contains("_" + f)) return false;
			}

			if (minFilter != null && magFilter != null) {
				if (!name.contains("_" + filterToAbbrev.get(minFilter) + "," + filterToAbbrev.get(magFilter) + ".")
					&& !name.contains("_" + filterToAbbrev.get(minFilter) + "," + filterToAbbrev.get(magFilter) + "_")) return false;
			} else {
				// Return if the name has a filter.
				for (String f : filterToAbbrev.values()) {
					String tag = "_" + f + ",";
					int tagIndex = name.indexOf(tag);
					if (tagIndex != -1) {
						String rest = name.substring(tagIndex + tag.length());
						for (String f2 : filterToAbbrev.values())
							if (rest.startsWith(f2 + ".") || rest.startsWith(f2 + "_")) return false;
					}
				}
			}

			return true;
		}

		public boolean accept (BufferedImage image) {
			if (width != -1 && image.getWidth() != width) return false;
			if (height != -1 && image.getHeight() != height) return false;
			return true;
		}
	}

	static private enum Direction {
		x, y, xy, none;

		public boolean isX () {
			return this == x || this == xy;
		}

		public boolean isY () {
			return this == y || this == xy;
		}
	}

	static final HashMap<TextureFilter, String> filterToAbbrev = new HashMap();
	static {
		filterToAbbrev.put(TextureFilter.Linear, "l");
		filterToAbbrev.put(TextureFilter.Nearest, "n");
		filterToAbbrev.put(TextureFilter.MipMap, "m");
		filterToAbbrev.put(TextureFilter.MipMapLinearLinear, "mll");
		filterToAbbrev.put(TextureFilter.MipMapLinearNearest, "mln");
		filterToAbbrev.put(TextureFilter.MipMapNearestLinear, "mnl");
		filterToAbbrev.put(TextureFilter.MipMapNearestNearest, "mnn");
	}

	static final HashMap<Format, String> formatToAbbrev = new HashMap();
	static {
		formatToAbbrev.put(Format.RGBA8888, "rgba8");
		formatToAbbrev.put(Format.RGBA4444, "rgba4");
		formatToAbbrev.put(Format.RGB565, "rgb565");
		formatToAbbrev.put(Format.Alpha, "a");
	}

	static public class Settings {
		public Format defaultFormat = Format.RGBA8888;
		public TextureFilter defaultFilterMin = TextureFilter.Nearest;
		public TextureFilter defaultFilterMag = TextureFilter.Nearest;
		public int alphaThreshold = 0;
		public boolean pot = true;
		public int padding = 2;
		public boolean duplicatePadding = true;
		public boolean debug = false;
		public boolean rotate = false;
		public int minWidth = 16;
		public int minHeight = 16;
		public int maxWidth = 512;
		public int maxHeight = 512;
		public boolean stripWhitespace = true;
		public boolean incremental=true;
		public boolean alias;

		HashMap<String, Long> crcs = new HashMap();
		HashMap<String, String> packSections = new HashMap();
	}

	static private void process (Settings settings, File inputDir, File outputDir, File packFile) throws IOException {
		if (inputDir.getName().startsWith(".")) return;

		// Abort if nothing has changed.
		boolean skip = false;
		if (settings.incremental) {
			File[] files = inputDir.listFiles();
			if (files == null) return;
			boolean noneHaveChanged = true;
			int childCountNow = 0;
			for (File file : files) {
				if (file.isDirectory()) continue;
				String path = file.getAbsolutePath();
				Long crcOld = settings.crcs.get(path);
				long crcNow = crc(file);
				if (crcOld == null || crcOld != crcNow) noneHaveChanged = false;
				settings.crcs.put(path, crcNow);
				childCountNow++;
			}

			String path = inputDir.getAbsolutePath();
			Long childCountOld = settings.crcs.get(path);
			if (childCountOld == null || childCountNow != childCountOld) noneHaveChanged = false;
			settings.crcs.put(path, (long)childCountNow);

			if (outputDir.exists()) {
				boolean foundPage = false;
				String prefix = inputDir.getName();
				for (File file : outputDir.listFiles()) {
					if (file.getName().startsWith(prefix)) {
						foundPage = true;
						break;
					}
				}
				if (!foundPage) noneHaveChanged = false;
			}

			String section = settings.packSections.get(inputDir.getName());
			if (noneHaveChanged && section != null) {
				FileWriter writer = new FileWriter(packFile, true);
				writer.append(section);
				writer.close();

				System.out.println(inputDir);
				System.out.println("Skipping unchanged directory.");
				System.out.println();
				skip = true;
			}
		}

		if (!skip) {
			// Clean existing page images.
			if (outputDir.exists()) {
				String prefix = inputDir.getName();
				for (File file : outputDir.listFiles())
					if (file.getName().startsWith(prefix)) file.delete();
			}

			// Just check all combinations, because we are extremely lazy.
			ArrayList<TextureFilter> filters = new ArrayList();
			filters.add(null);
			filters.addAll(Arrays.asList(TextureFilter.values()));
			ArrayList<Format> formats = new ArrayList();
			formats.add(null);
			formats.addAll(Arrays.asList(Format.values()));
			for (int i = 0, n = formats.size(); i < n; i++) {
				Format format = formats.get(i);
				for (int ii = 0, nn = filters.size(); ii < nn; ii++) {
					TextureFilter min = filters.get(ii);
					for (int iii = ii; iii < nn; iii++) {
						TextureFilter mag = filters.get(iii);
						if ((min == null && mag != null) || (min != null && mag == null)) continue;

						Filter filter = new Filter(Direction.none, format, -1, -1, min, mag);
						new TexturePacker(settings, inputDir, filter, outputDir, packFile);

						for (int width = settings.minWidth; width <= settings.maxWidth; width <<= 1) {
							filter = new Filter(Direction.x, format, width, -1, min, mag);
							new TexturePacker(settings, inputDir, filter, outputDir, packFile);
						}

						for (int height = settings.minHeight; height <= settings.maxHeight; height <<= 1) {
							filter = new Filter(Direction.y, format, -1, height, min, mag);
							new TexturePacker(settings, inputDir, filter, outputDir, packFile);
						}

						for (int width = settings.minWidth; width <= settings.maxWidth; width <<= 1) {
							for (int height = settings.minHeight; height <= settings.maxHeight; height <<= 1) {
								filter = new Filter(Direction.xy, format, width, height, min, mag);
								new TexturePacker(settings, inputDir, filter, outputDir, packFile);
							}
						}
					}
				}
			}
		}

		// Process subdirectories.
		File[] files = inputDir.listFiles();
		if (files == null) return;
		for (File file : files)
			if (file.isDirectory()) process(settings, file, outputDir, packFile);
	}

	static public void process (Settings settings, String input, String output) throws IOException {
		File inputDir = new File(input);
		File outputDir = new File(output);

		if (!inputDir.isDirectory()) {
			System.out.println("Not a directory: " + inputDir);
			return;
		}

		File packFile = new File(outputDir, "pack");

		// Load incremental file.
		File incrmentalFile = null;
		if (settings.incremental && packFile.exists()) {
			settings.crcs.clear();
			incrmentalFile = new File(System.getProperty("user.home") + "/.texturepacker/" + hash(inputDir.getAbsolutePath()));
			if (incrmentalFile.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(incrmentalFile));
				while (true) {
					String path = reader.readLine();
					if (path == null) break;
					String crc = reader.readLine();
					if (crc == null) break;
					settings.crcs.put(path, Long.parseLong(crc));
				}
				reader.close();
			}

			// Store the pack file text for each section.
			BufferedReader reader = new BufferedReader(new FileReader(packFile));
			StringBuilder buffer = new StringBuilder(2048);
			while (true) {
				String imageName = reader.readLine();
				if (imageName == null) break;
				if (imageName.length() == 0) continue;

				String pageName = imageName.replaceAll("\\d+.png$", "");
				String section = settings.packSections.get(pageName);
				if (section != null) buffer.append(section);

				// buffer.append("****start\n");
				buffer.append('\n');
				buffer.append(imageName);
				buffer.append('\n');
				while (true) {
					String line = reader.readLine();
					if (line == null || line.length() == 0) break;
					buffer.append(line);
					buffer.append('\n');
				}
				settings.packSections.put(pageName, buffer.toString());
				buffer.setLength(0);
			}
			reader.close();
		}

		// Clean pack file.
		packFile.delete();

		process(settings, inputDir, outputDir, packFile);

		// Write incrmental file.
		if (incrmentalFile != null) {
			incrmentalFile.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(incrmentalFile);
			for (Entry<String, Long> entry : settings.crcs.entrySet()) {
				writer.write(entry.getKey() + "\n");
				writer.write(entry.getValue() + "\n");
			}
			writer.close();
		}
	}

	static private String hash (String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			digest.update(value.getBytes());
			return new BigInteger(1, digest.digest()).toString(16);
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	static private long crc (File file) {
		try {
			FileInputStream input = new FileInputStream(file);
			byte[] buffer = new byte[4096];
			CRC32 crc32 = new CRC32();
			while (true) {
				int length = input.read(buffer);
				if (length == -1) break;
				crc32.update(buffer, 0, length);
			}
			input.close();
			return crc32.getValue();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	static public void main (String[] args) throws Exception {
		String input, output;
		if (args.length != 2) {
			System.out.println("Usage: INPUTDIR OUTPUTDIR");
			return;
		}
		input = args[0];
		output = args[1];
		Settings settings = new Settings();
		settings.alias = true;
		process(settings, input, output);
	}
}

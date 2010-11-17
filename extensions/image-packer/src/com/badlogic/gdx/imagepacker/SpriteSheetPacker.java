
package com.badlogic.gdx.imagepacker;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.badlogic.gdx.utils.MathUtils;

public class SpriteSheetPacker {
	static Pattern numberedImagePattern = Pattern.compile(".*?(\\d+)");

	private ArrayList<Image> images = new ArrayList();
	FileWriter writer;
	final File inputDir;
	private int uncompressedSize, compressedSize;
	final Direction direction;
	int xPadding, yPadding;
	private final Filter filter;

	// User configurable settings:
	private int alphaThreshold = 11;
	private boolean pot = true;
	private int padding = 0;
	private boolean debug = false;

	public SpriteSheetPacker (File inputDir, Filter filter, Direction direction, File outputDir, File packFile) throws IOException {
		this.inputDir = inputDir;
		this.filter = filter;
		this.direction = direction;

		ArrayList<File> files = getFiles(inputDir, filter, direction);
		if (files == null) return;

		for (File file : files) {
			if (file.isDirectory()) continue;
			Image image = squeeze(file);
			if (image != null) images.add(image);
		}
		if (images.isEmpty()) return;

		System.out.println(inputDir);
		if (filter != rgba8888) System.out.println("Format: " + filter.name);
		if (direction != null) System.out.println("Direction: " + direction);
		for (Image image : images)
			System.out.println("Packing... " + image.file.getName());

		Collections.sort(images, new Comparator<Image>() {
			public int compare (Image image1, Image image2) {
				return image1.getWidth() * image1.getHeight() - image2.getWidth() * image2.getHeight();
			}
		});

		xPadding = images.size() > 1 && direction != Direction.x && direction != Direction.xy ? padding : 0;
		yPadding = images.size() > 1 && direction != Direction.y && direction != Direction.xy ? padding : 0;

		outputDir.mkdirs();
		String prefix = inputDir.getParentFile().getName();

		writer = new FileWriter(packFile, true);
		try {
			while (!images.isEmpty())
				writePage(prefix, outputDir);
			if (writer != null) {
				System.out.println("Pixels eliminated: " + (1 - compressedSize / (float)uncompressedSize) * 100 + "%");
				System.out.println();
			}
		} finally {
			writer.close();
		}
	}

	private void writePage (String prefix, File outputDir) throws IOException {
		int imageNumber = 1;
		File outputFile = new File(outputDir, prefix + imageNumber + ".png");
		while (outputFile.exists())
			outputFile = new File(outputDir, prefix + ++imageNumber + ".png");

		writer.write("\n" + prefix + imageNumber + ".png\n");
		writer.write(direction + "\n");

		// Try reasonably hard to find the smallest size that is also the smallest POT.
		Comparator bestComparator = null;
		Comparator secondBestComparator = imageComparators.get(0);
		int maxWidth = 1024, maxHeight = 1024;
		int bestWidth = 99999, bestHeight = 99999;
		int secondBestWidth = 99999, secondBestHeight = 99999;
		int bestUsedPixels = 0;
		int width = 64, height = 64;
		int grownPixels = 0, grownPixels2 = 0;
		int i = 0, ii = 0;
		while (true) {
			if (width > maxWidth && height > maxHeight) break;
			for (Comparator comparator : imageComparators) {
				Collections.sort(images, comparator);

				int usedPixels = insert(null, new ArrayList(images), width, height);
				if (usedPixels > bestUsedPixels) {
					secondBestComparator = comparator;
					secondBestWidth = width;
					secondBestHeight = height;
				}
				if (usedPixels == -1) {
					if (width * height < bestWidth * bestHeight) {
						bestComparator = comparator;
						bestWidth = width;
						bestHeight = height;
					}
				}
			}
			if (bestComparator != null) break;
			if (pot) {
				if (i % 3 == 0) {
					width *= 2;
					i++;
				} else if (i % 3 == 1) {
					width /= 2;
					height *= 2;
					i++;
				} else {
					width *= 2;
					i++;
				}
			} else {
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

		if (pot) {
			width = MathUtils.nextPowerOfTwo(width);
			height = MathUtils.nextPowerOfTwo(height);
		}

		BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		insert(canvas, images, bestWidth, bestHeight);
		System.out.println("Writing " + canvas.getWidth() + "x" + canvas.getHeight() + ": " + outputFile);
		ImageIO.write(canvas, "png", outputFile);
		compressedSize += canvas.getWidth() * canvas.getHeight();
	}

	private int insert (BufferedImage canvas, ArrayList<Image> images, int width, int height) throws IOException {
		if (debug && canvas != null) {
			Graphics g = canvas.getGraphics();
			g.setColor(Color.green);
			g.drawRect(0, 0, width - 1, height - 1);
		}
		// Pretend image is larger so padding on right and bottom edges is ignored.
		if (direction != Direction.x && direction != Direction.xy) width += xPadding;
		if (direction != Direction.y && direction != Direction.xy) height += yPadding;
		Node root = new Node(0, 0, width, height);
		int usedPixels = 0;
		for (int i = images.size() - 1; i >= 0; i--) {
			Image image = images.get(i);
			Node node = root.insert(image, canvas);
			if (node == null) continue;
			usedPixels += image.getWidth() * image.getHeight();
			images.remove(i);
			if (canvas != null) {
				Graphics g = canvas.getGraphics();
				g.drawImage(image, node.left, node.top, null);
				if (debug) {
					g.setColor(Color.magenta);
					g.drawRect(node.left, node.top, image.getWidth() - 1, image.getHeight() - 1);
				}
			}
		}
		return images.isEmpty() ? -1 : usedPixels;
	}

	private Image squeeze (File file) throws IOException {
		BufferedImage source = ImageIO.read(file);
		if (source == null) return null;
		uncompressedSize += source.getWidth() * source.getHeight();
		WritableRaster alphaRaster = source.getAlphaRaster();
		if (alphaRaster == null) return new Image(file, source, 0, 0, source.getWidth(), source.getHeight());
		final byte[] a = new byte[1];
		int top = 0;
		outer:
		for (int y = 0; y < source.getHeight(); y++) {
			for (int x = 0; x < source.getWidth(); x++) {
				alphaRaster.getDataElements(x, y, a);
				int alpha = a[0];
				if (alpha < 0) alpha += 256;
				if (alpha > alphaThreshold) break outer;
			}
			top++;
		}
		int bottom = source.getHeight() - 1;
		outer:
		for (int y = source.getHeight(); --y >= top;) {
			for (int x = 0; x < source.getWidth(); x++) {
				alphaRaster.getDataElements(x, y, a);
				int alpha = a[0];
				if (alpha < 0) alpha += 256;
				if (alpha > alphaThreshold) break outer;
			}
			bottom--;
		}
		int left = 0;
		outer:
		for (int x = 0; x < source.getWidth(); x++) {
			for (int y = top; y <= bottom; y++) {
				alphaRaster.getDataElements(x, y, a);
				int alpha = a[0];
				if (alpha < 0) alpha += 256;
				if (alpha > alphaThreshold) break outer;
			}
			left++;
		}
		int right = source.getWidth() - 1;
		outer:
		for (int x = source.getWidth(); --x >= left;) {
			for (int y = top; y <= bottom; y++) {
				alphaRaster.getDataElements(x, y, a);
				int alpha = a[0];
				if (alpha < 0) alpha += 256;
				if (alpha > alphaThreshold) break outer;
			}
			right--;
		}
		int newWidth = right - left;
		int newHeight = bottom - top;
		if (newWidth <= 0 || newHeight <= 0) {
			System.out.println("Ignoring blank input image: " + file.getAbsolutePath());
			return null;
		}
		return new Image(file, source, left, top, newWidth, newHeight);
	}

	private class Node {
		final int left, top, width, height;
		Node child1, child2;
		Image image;

		public Node (int left, int top, int width, int height) {
			this.left = left;
			this.top = top;
			this.width = width;
			this.height = height;
		}

		public Node insert (Image image, BufferedImage canvas) throws IOException {
			if (this.image != null) return null;
			if (child1 != null) {
				Node newNode = child1.insert(image, canvas);
				if (newNode != null) return newNode;
				return child2.insert(image, canvas);
			}
			int neededWidth = image.getWidth() + xPadding;
			int neededHeight = image.getHeight() + yPadding;
			if (neededWidth > width || neededHeight > height) return null;
			if (neededWidth == width && neededHeight == height) {
				this.image = image;
				write(canvas);
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
			return child1.insert(image, canvas);
		}

		private void write (BufferedImage canvas) throws IOException {
			if (canvas == null) return;

			String imageName = image.file.getAbsolutePath().substring(inputDir.getAbsolutePath().length()) + "\n";
			if (imageName.startsWith("/") || imageName.startsWith("\\")) imageName = imageName.substring(1);
			int dotIndex = imageName.lastIndexOf('.');
			if (dotIndex != -1) imageName = imageName.substring(0, dotIndex);
			if (imageName.endsWith("_4444")) imageName = imageName.substring(0, imageName.length() - 5);
			if (imageName.endsWith("_565")) imageName = imageName.substring(0, imageName.length() - 4);
			if (imageName.endsWith("_a")) imageName = imageName.substring(0, imageName.length() - 2);
			if (imageName.endsWith("_pre")) imageName = imageName.substring(0, imageName.length() - 2);

			writer.write(imageName.replace("\\", "/") + "\n");
			writer.write(left + "\n");
			writer.write(top + "\n");
			writer.write(image.getWidth() + "\n");
			writer.write(image.getHeight() + "\n");
			writer.write(image.offsetX + "\n");
			writer.write(image.offsetY + "\n");
			writer.write(image.originalWidth + "\n");
			writer.write(image.originalHeight + "\n");

			Matcher matcher = numberedImagePattern.matcher(imageName);
			if (matcher.matches())
				writer.write(Integer.parseInt(matcher.group(1)) + "\n");
			else
				writer.write("0\n");
		}
	}

	static private class Image extends BufferedImage {
		final File file;
		final int offsetX, offsetY;
		final int originalWidth, originalHeight;

		public Image (File file, BufferedImage src, int left, int top, int newWidth, int newHeight) {
			super(src.getColorModel(), src.getRaster().createWritableChild(left, top, newWidth, newHeight, 0, 0, null), src
				.getColorModel().isAlphaPremultiplied(), null);
			this.file = file;
			offsetX = left;
			offsetY = top;
			originalWidth = src.getWidth();
			originalHeight = src.getHeight();
		}

		public String toString () {
			return file.toString();
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

	static private Filter rgba8888 = new Filter("RGBA8888") {
		public boolean accept (File dir, String name) {
			return !name.endsWith("_4444") && !name.endsWith("_565") && !name.endsWith("_a");
		}
	};
	static private Filter rgba4444 = new Filter("RGBA4444") {
		public boolean accept (File dir, String name) {
			return name.endsWith("_4444");
		}
	};
	static private Filter rgb565 = new Filter("RGB565") {
		public boolean accept (File dir, String name) {
			return name.endsWith("_565");
		}
	};
	static private Filter alpha = new Filter("Alpha") {
		public boolean accept (File dir, String name) {
			return name.endsWith("_a");
		}
	};

	static abstract private class Filter implements FilenameFilter {
		String name;

		public Filter (String name) {
			this.name = name;
		}
	}

	static private enum Direction {
		x, y, xy, none
	}

	static private ArrayList<File> getFiles (File inputDir, Filter filter, Direction direction) {
		ArrayList<File> files = new ArrayList();
		files.addAll(Arrays.asList(inputDir.listFiles(filter)));
		for (Iterator<File> iter = files.iterator(); iter.hasNext();) {
			File file = iter.next();
			String name = file.getName();
			switch (direction) {
			case none:
				if (name.contains("_x") || name.contains("_y")) iter.remove();
				break;
			case x:
				if (!name.contains("_x") || name.contains("_xy")) iter.remove();
				break;
			case y:
				if (!name.contains("_y")) iter.remove();
				break;
			case xy:
				if (!name.contains("_xy")) iter.remove();
				break;
			}
		}
		return files;
	}

	static private void process (File inputDir, File outputDir, File packFile) throws Exception {
		if (outputDir.exists()) {
			String prefix = inputDir.getParentFile().getName();
			for (File file : outputDir.listFiles())
				if (file.getName().startsWith(prefix)) file.delete();
		}

		Direction[] directions = Direction.values();
		for (int i = 0; i < directions.length; i++) {
			Direction direction = directions[i];
			new SpriteSheetPacker(inputDir, rgba8888, direction, outputDir, packFile);
			new SpriteSheetPacker(inputDir, rgba4444, direction, outputDir, packFile);
			new SpriteSheetPacker(inputDir, rgb565, direction, outputDir, packFile);
			new SpriteSheetPacker(inputDir, alpha, direction, outputDir, packFile);
		}
		File[] files = inputDir.listFiles();
		if (files == null) return;
		for (File file : files)
			if (file.isDirectory()) process(file, new File(outputDir, file.getName()), packFile);
	}

	static public void process (String inputDir, String outputDir) throws Exception {
		File input = new File(inputDir);
		if (!input.isDirectory()) {
			System.out.println("Not a directory: " + input);
			return;
		}
		File packFile = new File(outputDir, "pack");
		packFile.delete();
		process(new File(inputDir), new File(outputDir), packFile);
	}

	public static void main (String[] args) throws Exception {
		process("C:/Dev/libgdx/tests/gdx-tests-lwjgl/data/New folder", "c:/temp/pack-out");
	}
}

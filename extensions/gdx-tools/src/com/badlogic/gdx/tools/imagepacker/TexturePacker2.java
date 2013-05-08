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

package com.badlogic.gdx.tools.imagepacker;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.SerializationException;

/** @author Nathan Sweet */
public class TexturePacker2 {
	private final Settings settings;
	private final MaxRectsPacker maxRectsPacker;
	private final ImageProcessor imageProcessor;

	/** @param rootDir Can be null. */
	public TexturePacker2 (File rootDir, Settings settings) {
		this.settings = settings;

		if (settings.pot) {
			if (settings.maxWidth != MathUtils.nextPowerOfTwo(settings.maxWidth))
				throw new RuntimeException("If pot is true, maxWidth must be a power of two: " + settings.maxWidth);
			if (settings.maxHeight != MathUtils.nextPowerOfTwo(settings.maxHeight))
				throw new RuntimeException("If pot is true, maxHeight must be a power of two: " + settings.maxHeight);
		}

		maxRectsPacker = new MaxRectsPacker(settings);
		imageProcessor = new ImageProcessor(rootDir, settings);
	}

	public TexturePacker2 (Settings settings) {
		this(null, settings);
	}

	public void addImage (File file) {
		imageProcessor.addImage(file);
	}

	public void addImage (BufferedImage image, String name) {
		imageProcessor.addImage(image, name);
	}

	public void pack (File outputDir, String packFileName) {
		outputDir.mkdirs();

		if (packFileName.indexOf('.') == -1) packFileName += ".atlas";

		Array<Page> pages = maxRectsPacker.pack(imageProcessor.getImages());
		writeImages(outputDir, pages, packFileName);
		try {
			writePackFile(outputDir, pages, packFileName);
		} catch (IOException ex) {
			throw new RuntimeException("Error writing pack file.", ex);
		}
	}

	private void writeImages (File outputDir, Array<Page> pages, String packFileName) {
		String imageName = packFileName;
		int dotIndex = imageName.lastIndexOf('.');
		if (dotIndex != -1) imageName = imageName.substring(0, dotIndex);

		int fileIndex = 0;
		for (Page page : pages) {
			int width = page.width, height = page.height;
			int paddingX = settings.paddingX;
			int paddingY = settings.paddingY;
			if (settings.duplicatePadding) {
				paddingX /= 2;
				paddingY /= 2;
			}
			width -= settings.paddingX;
			height -= settings.paddingY;
			if (settings.edgePadding) {
				page.x = paddingX;
				page.y = paddingY;
				width += paddingX * 2;
				height += paddingY * 2;
			}
			if (settings.pot) {
				width = MathUtils.nextPowerOfTwo(width);
				height = MathUtils.nextPowerOfTwo(height);
			}
			width = Math.max(settings.minWidth, width);
			height = Math.max(settings.minHeight, height);

			if (settings.forceSquareOutput) {
				if (width > height) {
					height = width;
				} else {
					width = height;
				}
			}

			File outputFile;
			while (true) {
				outputFile = new File(outputDir, imageName + (fileIndex++ == 0 ? "" : fileIndex) + "." + settings.outputFormat);
				if (!outputFile.exists()) break;
			}
			page.imageName = outputFile.getName();

			BufferedImage canvas = new BufferedImage(width, height, getBufferedImageType(settings.format));
			Graphics2D g = (Graphics2D)canvas.getGraphics();

			System.out.println("Writing " + canvas.getWidth() + "x" + canvas.getHeight() + ": " + outputFile);

			for (Rect rect : page.outputRects) {
				int rectX = page.x + rect.x, rectY = page.y + page.height - rect.y - rect.height;
				if (rect.rotated) {
					g.translate(rectX, rectY);
					g.rotate(-90 * MathUtils.degreesToRadians);
					g.translate(-rectX, -rectY);
					g.translate(-(rect.height - settings.paddingY), 0);
				}
				BufferedImage image = rect.image;
				if (settings.duplicatePadding) {
					int amountX = settings.paddingX / 2;
					int amountY = settings.paddingY / 2;
					int imageWidth = image.getWidth();
					int imageHeight = image.getHeight();
					// Copy corner pixels to fill corners of the padding.
					g.drawImage(image, rectX - amountX, rectY - amountY, rectX, rectY, 0, 0, 1, 1, null);
					g.drawImage(image, rectX + imageWidth, rectY - amountY, rectX + imageWidth + amountX, rectY, imageWidth - 1, 0,
						imageWidth, 1, null);
					g.drawImage(image, rectX - amountX, rectY + imageHeight, rectX, rectY + imageHeight + amountY, 0, imageHeight - 1,
						1, imageHeight, null);
					g.drawImage(image, rectX + imageWidth, rectY + imageHeight, rectX + imageWidth + amountX, rectY + imageHeight
						+ amountY, imageWidth - 1, imageHeight - 1, imageWidth, imageHeight, null);
					// Copy edge pixels into padding.
					g.drawImage(image, rectX, rectY - amountY, rectX + imageWidth, rectY, 0, 0, imageWidth, 1, null);
					g.drawImage(image, rectX, rectY + imageHeight, rectX + imageWidth, rectY + imageHeight + amountY, 0,
						imageHeight - 1, imageWidth, imageHeight, null);
					g.drawImage(image, rectX - amountX, rectY, rectX, rectY + imageHeight, 0, 0, 1, imageHeight, null);
					g.drawImage(image, rectX + imageWidth, rectY, rectX + imageWidth + amountX, rectY + imageHeight, imageWidth - 1,
						0, imageWidth, imageHeight, null);
				}
				g.drawImage(image, rectX, rectY, null);
				if (rect.rotated) {
					g.translate(rect.height - settings.paddingY, 0);
					g.translate(rectX, rectY);
					g.rotate(90 * MathUtils.degreesToRadians);
					g.translate(-rectX, -rectY);
				}
				if (settings.debug) {
					g.setColor(Color.magenta);
					g.drawRect(rectX, rectY, rect.width - settings.paddingX - 1, rect.height - settings.paddingY - 1);
				}
			}

			if (settings.debug) {
				g.setColor(Color.magenta);
				g.drawRect(0, 0, width - 1, height - 1);
			}

			try {
				if (settings.outputFormat.equalsIgnoreCase("jpg")) {
					Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
					ImageWriter writer = writers.next();
					ImageWriteParam param = writer.getDefaultWriteParam();
					param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					param.setCompressionQuality(settings.jpegQuality);
					ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
					writer.setOutput(ios);
					writer.write(null, new IIOImage(canvas, null, null), param);
				} else {
					if (settings.premultiplyAlpha) canvas.getColorModel().coerceData(canvas.getRaster(), true);
					ImageIO.write(canvas, "png", outputFile);
				}
			} catch (IOException ex) {
				throw new RuntimeException("Error writing file: " + outputFile, ex);
			}
		}
	}

	private void writePackFile (File outputDir, Array<Page> pages, String packFileName) throws IOException {
		File packFile = new File(outputDir, packFileName);

		if (packFile.exists()) {
			// Make sure there aren't duplicate names.
			TextureAtlasData textureAtlasData = new TextureAtlasData(new FileHandle(packFile), new FileHandle(packFile), false);
			for (Page page : pages) {
				for (Rect rect : page.outputRects) {
					String rectName = Rect.getAtlasName(rect.name, settings.flattenPaths);
					System.out.println(rectName);
					for (Region region : textureAtlasData.getRegions()) {
						if (region.name.equals(rectName)) {
							throw new GdxRuntimeException("A region with the name \"" + rectName + "\" has already been packed: "
								+ rect.name);
						}
					}
				}
			}
		}

		FileWriter writer = new FileWriter(packFile, true);
// if (settings.jsonOutput) {
// } else {
		for (Page page : pages) {
			writer.write("\n" + page.imageName + "\n");
			writer.write("format: " + settings.format + "\n");
			writer.write("filter: " + settings.filterMin + "," + settings.filterMag + "\n");
			writer.write("repeat: " + getRepeatValue() + "\n");

			for (Rect rect : page.outputRects) {
				writeRect(writer, page, rect, rect.name);
				for (String alias : rect.aliases)
					writeRect(writer, page, rect, alias);
			}
		}
// }
		writer.close();
	}

	private void writeRect (FileWriter writer, Page page, Rect rect, String name) throws IOException {
		writer.write(Rect.getAtlasName(name, settings.flattenPaths) + "\n");
		writer.write("  rotate: " + rect.rotated + "\n");
		writer.write("  xy: " + (page.x + rect.x) + ", " + (page.y + page.height - rect.height - rect.y) + "\n");
		writer.write("  size: " + rect.image.getWidth() + ", " + rect.image.getHeight() + "\n");
		if (rect.splits != null) {
			writer
				.write("  split: " + rect.splits[0] + ", " + rect.splits[1] + ", " + rect.splits[2] + ", " + rect.splits[3] + "\n");
		}
		if (rect.pads != null) {
			if (rect.splits == null) writer.write("  split: 0, 0, 0, 0\n");
			writer.write("  pad: " + rect.pads[0] + ", " + rect.pads[1] + ", " + rect.pads[2] + ", " + rect.pads[3] + "\n");
		}
		writer.write("  orig: " + rect.originalWidth + ", " + rect.originalHeight + "\n");
		writer.write("  offset: " + rect.offsetX + ", " + (rect.originalHeight - rect.image.getHeight() - rect.offsetY) + "\n");
		writer.write("  index: " + rect.index + "\n");
	}

	private String getRepeatValue () {
		if (settings.wrapX == TextureWrap.Repeat && settings.wrapY == TextureWrap.Repeat) return "xy";
		if (settings.wrapX == TextureWrap.Repeat && settings.wrapY == TextureWrap.ClampToEdge) return "x";
		if (settings.wrapX == TextureWrap.ClampToEdge && settings.wrapY == TextureWrap.Repeat) return "y";
		return "none";
	}

	private int getBufferedImageType (Format format) {
		switch (settings.format) {
		case RGBA8888:
		case RGBA4444:
			return BufferedImage.TYPE_INT_ARGB;
		case RGB565:
		case RGB888:
			return BufferedImage.TYPE_INT_RGB;
		case Alpha:
			return BufferedImage.TYPE_BYTE_GRAY;
		default:
			throw new RuntimeException("Unsupported format: " + settings.format);
		}
	}

	/** @author Nathan Sweet */
	static class Page {
		public String imageName;
		public Array<Rect> outputRects, remainingRects;
		public float occupancy;
		public int x, y, width, height;
	}

	/** @author Nathan Sweet */
	static class Rect {
		public String name;
		public BufferedImage image;
		public int offsetX, offsetY, originalWidth, originalHeight;
		public int x, y, width, height;
		public int index;
		public boolean rotated;
		public ArrayList<String> aliases = new ArrayList();
		public int[] splits;
		public int[] pads;
		public boolean canRotate = true;

		int score1, score2;

		Rect (BufferedImage source, int left, int top, int newWidth, int newHeight) {
			image = new BufferedImage(source.getColorModel(), source.getRaster().createWritableChild(left, top, newWidth, newHeight,
				0, 0, null), source.getColorModel().isAlphaPremultiplied(), null);
			offsetX = left;
			offsetY = top;
			originalWidth = source.getWidth();
			originalHeight = source.getHeight();
			width = newWidth;
			height = newHeight;
		}

		Rect () {
		}

		Rect (Rect rect) {
			setSize(rect);
		}

		void setSize (Rect rect) {
			x = rect.x;
			y = rect.y;
			width = rect.width;
			height = rect.height;
		}

		void set (Rect rect) {
			name = rect.name;
			image = rect.image;
			offsetX = rect.offsetX;
			offsetY = rect.offsetY;
			originalWidth = rect.originalWidth;
			originalHeight = rect.originalHeight;
			x = rect.x;
			y = rect.y;
			width = rect.width;
			height = rect.height;
			index = rect.index;
			rotated = rect.rotated;
			aliases = rect.aliases;
			splits = rect.splits;
			pads = rect.pads;
			canRotate = rect.canRotate;
			score1 = rect.score1;
			score2 = rect.score2;
		}

		@Override
		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Rect other = (Rect)obj;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			return true;
		}

		@Override
		public String toString () {
			return name + "[" + x + "," + y + " " + width + "x" + height + "]";
		}

		static public String getAtlasName (String name, boolean flattenPaths) {
			return flattenPaths ? new FileHandle(name).name() : name;
		}
	}

	/** @author Nathan Sweet */
	static public class Settings {
		public boolean pot = true;
		public int paddingX = 2, paddingY = 2;
		public boolean edgePadding = true;
		public boolean duplicatePadding = true;
		public boolean rotation;
		public int minWidth = 16, minHeight = 16;
		public int maxWidth = 1024, maxHeight = 1024;
		public boolean forceSquareOutput = false;
		public boolean stripWhitespaceX, stripWhitespaceY;
		public int alphaThreshold;
		public TextureFilter filterMin = TextureFilter.Nearest, filterMag = TextureFilter.Nearest;
		public TextureWrap wrapX = TextureWrap.ClampToEdge, wrapY = TextureWrap.ClampToEdge;
		public Format format = Format.RGBA8888;
		public boolean alias = true;
		public String outputFormat = "png";
		public float jpegQuality = 0.9f;
		public boolean ignoreBlankImages = true;
		public boolean fast;
		public boolean debug;
		public boolean combineSubdirectories;
		public boolean flattenPaths;
		public boolean premultiplyAlpha;
		public boolean useIndexes = true;

		public Settings () {
		}

		public Settings (Settings settings) {
			fast = settings.fast;
			rotation = settings.rotation;
			pot = settings.pot;
			minWidth = settings.minWidth;
			minHeight = settings.minHeight;
			maxWidth = settings.maxWidth;
			maxHeight = settings.maxHeight;
			paddingX = settings.paddingX;
			paddingY = settings.paddingY;
			edgePadding = settings.edgePadding;
			alphaThreshold = settings.alphaThreshold;
			ignoreBlankImages = settings.ignoreBlankImages;
			stripWhitespaceX = settings.stripWhitespaceX;
			stripWhitespaceY = settings.stripWhitespaceY;
			alias = settings.alias;
			format = settings.format;
			jpegQuality = settings.jpegQuality;
			outputFormat = settings.outputFormat;
			filterMin = settings.filterMin;
			filterMag = settings.filterMag;
			wrapX = settings.wrapX;
			wrapY = settings.wrapY;
			duplicatePadding = settings.duplicatePadding;
			debug = settings.debug;
			combineSubdirectories = settings.combineSubdirectories;
			flattenPaths = settings.flattenPaths;
			premultiplyAlpha = settings.premultiplyAlpha;
		}
	}

	static public void process (String input, String output, String packFileName) {
		try {
			new TexturePackerFileProcessor(new Settings(), packFileName).process(new File(input), new File(output));
		} catch (Exception ex) {
			throw new RuntimeException("Error packing files.", ex);
		}
	}

	static public void process (Settings settings, String input, String output, String packFileName) {
		try {
			new TexturePackerFileProcessor(settings, packFileName).process(new File(input), new File(output));
		} catch (Exception ex) {
			throw new RuntimeException("Error packing files.", ex);
		}
	}

	/** @return true if the output file does not yet exist or its last modification date is before the last modification date of the
	 *         input file */
	static public boolean isModified (String input, String output, String packFileName) {
		String packFullFileName = output;
		if (!packFullFileName.endsWith("/")) packFullFileName += "/";
		packFullFileName += packFileName;
		File outputFile = new File(packFullFileName);
		if (!outputFile.exists()) return true;

		File inputFile = new File(input);
		if (!inputFile.exists()) throw new IllegalArgumentException("Input file does not exist: " + inputFile.getAbsolutePath());
		return inputFile.lastModified() > outputFile.lastModified();
	}

	static public void processIfModified (String input, String output, String packFileName) {
		if (isModified(input, output, packFileName)) process(input, output, packFileName);
	}

	static public void processIfModified (Settings settings, String input, String output, String packFileName) {
		if (isModified(input, output, packFileName)) process(settings, input, output, packFileName);
	}

	public static void main (String[] args) throws Exception {
		String input = null, output = null, packFileName = "pack.atlas";

		switch (args.length) {
		case 3:
			packFileName = args[2];
		case 2:
			output = args[1];
		case 1:
			input = args[0];
			break;
		default:
			System.out.println("Usage: inputDir [outputDir] [packFileName]");
			System.exit(0);
		}

		if (output == null) {
			File inputFile = new File(input);
			output = new File(inputFile.getParentFile(), inputFile.getName() + "-packed").getAbsolutePath();
		}

		process(input, output, packFileName);
	}
}

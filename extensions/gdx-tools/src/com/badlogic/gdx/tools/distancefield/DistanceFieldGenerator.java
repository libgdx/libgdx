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

package com.badlogic.gdx.tools.distancefield;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Generates a signed distance field image from a binary (black/white) source image.
 * 
 * <p> Signed distance fields are used in Team Fortress 2 by Valve to enable
 * sharp rendering of bitmap fonts even at high magnifications,
 * using nothing but alpha testing so at no extra runtime cost.
 * 
 * <p> The technique is described in the SIGGRAPH 2007 paper
 * "Improved Alpha-Tested Magniﬁcation for Vector Textures and Special Effects" by Chris Green:
 * <a href="http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf">
 * http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf
 * </a>
 * 
 * @author Thomas ten Cate
 */
public class DistanceFieldGenerator {
	
	private Color color = Color.white;
	private int downscale = 1;
	private float spread = 1;
	
	/** @see #setColor(Color) */
	public Color getColor() {
		return color;
	}
	
	/**
	 * Sets the color to be used for the output image. Its alpha component is ignored.
	 * Defaults to white, which is convenient for multiplying by a color value at runtime.
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	/** @see #setDownscale(int) */
	public int getDownscale() {
		return downscale;
	}
	
	/**
	 * Sets the factor by which to downscale the image during processing.
	 * The output image will be smaller than the input image by this factor, rounded downwards.
	 * 
	 * <p> For greater accuracy, images to be used as input for a distance field are often
	 * generated at higher resolution.
	 * 
	 * @param downscale a positive integer
	 * @throws IllegalArgumentException if downscale is not positive
	 */
	public void setDownscale(int downscale) {
		if (downscale <= 0)
			throw new IllegalArgumentException("downscale must be positive");
		this.downscale = downscale;
	}
	
	/** @see #setSpread(float) */
	public float getSpread() {
		return spread;
	}
	
	/**
	 * Sets the spread of the distance field. The spread is the maximum distance in pixels
	 * that we'll scan while for a nearby edge. The resulting distance is also normalized
	 * by the spread.
	 * 
	 * @param spread a positive number
	 * @throws IllegalArgumentException if spread is not positive
	 */
	public void setSpread(float spread) {
		if (spread <= 0)
			throw new IllegalArgumentException("spread must be positive");
		this.spread = spread;
	}
	
	/**
	 * Caclulate the squared distance between two points
	 * 
	 * @param x1 The x coordinate of the first point
	 * @param y1 The y coordiante of the first point
 	 * @param x2 The x coordinate of the second point
	 * @param y2 The y coordinate of the second point
	 * @return The squared distance between the two points
	 */
	private static int squareDist(final int x1, final int y1, final int x2, final int y2)
	{
		final int dx = x1 - x2;
		final int dy = y1 - y2;
		return dx*dx + dy*dy;
	}
	
	/**
	 * Process the image into a distance field.
	 * 
	 * The input image should be binary (black/white), but if not, see {@link #isInside(int)}.
	 *  
	 * The returned image is a factor of {@code upscale} smaller than {@code inImage}.
	 * Opaque pixels more than {@link #spread} away in the output image from white remain opaque;
	 * transparent pixels more than {@link #spread} away in the output image from black remain transparent.
	 * In between, we get a smooth transition from opaque to transparent, with an alpha value of 0.5
	 * when we are exactly on the edge.
	 * 
	 * @param inImage the image to process. 
	 * @return the distance field image
	 */
	public BufferedImage generateDistanceField(BufferedImage inImage)
	{
		final int inWidth = inImage.getWidth();
		final int inHeight = inImage.getHeight();
		final int outWidth = inWidth / downscale;
		final int outHeight = inHeight / downscale;
		final BufferedImage outImage = new BufferedImage(outWidth, outHeight, BufferedImage.TYPE_4BYTE_ABGR);
		
		// Note: coordinates reversed to mimic storage of BufferedImage, for memory locality
		final boolean[][] bitmap = new boolean[inHeight][inWidth];
		for (int y = 0; y < inHeight; ++y) {
			for (int x = 0; x < inWidth; ++x) {
				bitmap[y][x] = isInside(inImage.getRGB(x, y));
			}
		}
		
		for (int y = 0; y < outHeight; ++y)
		{
			for (int x = 0; x < outWidth; ++x)
			{
				int centerX = (x * downscale) + (downscale / 2);
				int centerY = (y * downscale) + (downscale / 2);
				float signedDistance = findSignedDistance(centerX, centerY, bitmap);
				outImage.setRGB(x, y, distanceToRGB(signedDistance));
			}
		}
		
		return outImage;
	}
	
	/**
	 * Returns {@code true} if the color is considered as the "inside" of the image,
	 * {@code false} if considered "outside".
	 * 
	 * <p> Any color with one of its color channels at least 128
	 * <em>and</em> its alpha channel at least 128 is considered "inside".
	 */
	private boolean isInside(int rgb) {
		return (rgb & 0x808080) != 0 && (rgb & 0x80000000) != 0;
	}
	
	/**
	 * For a distance as returned by {@link #findSignedDistance}, returns the corresponding "RGB" (really ARGB) color value.
	 *  
	 * @param signedDistance the signed distance of a pixel
	 * @return an ARGB color value suitable for {@link BufferedImage#setRGB}.
	 */
	private int distanceToRGB(float signedDistance) {
		float alpha = 0.5f + 0.5f * (signedDistance / spread);
		alpha = Math.min(1, Math.max(0, alpha)); // compensate for rounding errors
		int alphaByte = (int) (alpha * 0xFF); // no unsigned byte in Java :(
		return (alphaByte << 24) | (color.getRGB() & 0xFFFFFF);
	}
	
	/**
	 * Returns the signed distance for a given point.
	 * 
	 * For points "inside", this is the distance to the closest "outside" pixel.
	 * For points "outside", this is the <em>negative</em> distance to the closest "inside" pixel.
	 * If no pixel of different color is found within a radius of {@code spread}, returns
	 * the {@code -spread} or {@code spread}, respectively.
	 * 
	 * @param centerX the x coordinate of the center point 
	 * @param centerY the y coordinate of the center point
	 * @param bitmap the array representation of an image, {@code true} representing "inside"
	 * @return the signed distance 
	 */
	private float findSignedDistance(final int centerX, final int centerY, boolean[][] bitmap)
	{
		final int width = bitmap[0].length;
		final int height = bitmap.length;
		final boolean base = bitmap[centerY][centerX];
		
		final int delta = (int) Math.ceil(spread);
		final int startX = Math.max(0, centerX - delta);
		final int endX  = Math.min(width - 1, centerX + delta);
		final int startY = Math.max(0, centerY - delta);
		final int endY = Math.min(height - 1, centerY + delta);

		int closestSquareDist = delta * delta;
		
		for (int y = startY; y <= endY; ++y)
		{
			for (int x = startX; x <= endX; ++x)
			{
				if (base != bitmap[y][x])
				{
					final int squareDist = squareDist(centerX, centerY, x, y);
					if (squareDist < closestSquareDist)
					{
						closestSquareDist = squareDist;
					}
				}
			}
		}
		
		float closestDist = (float) Math.sqrt(closestSquareDist);
		return (base ? 1 : -1) * Math.min(closestDist, spread);
	}
	
	/** Prints usage information to standard output. */
	private static void usage() {
		System.out.println(
			"Generates a distance field image from a black and white input image.\n" +
		   "The distance field image contains a solid color and stores the distance\n" +
			"in the alpha channel.\n" +
		   "\n" +
		   "The output file format is inferred from the file name.\n" +
		   "\n" +
			"Command line arguments: INFILE OUTFILE [OPTION...]\n" +
		   "\n" +
			"Possible options:\n" +
			"  --color rrggbb    color of output image (default: ffffff)\n" +
			"  --downscale n     downscale by factor of n (default: 1)\n" +
			"  --spread n        edge scan distance (default: 1)\n");
	}
	
	/** Thrown when the command line contained nonsense. */
	private static class CommandLineArgumentException extends IllegalArgumentException {
		public CommandLineArgumentException(String message) {
			super(message);
		}
	}
	
	/**
	 * Main function to run the generator as a standalone program.
	 * Run without arguments for usage instructions (or see {@link #usage()}).
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		try {
			run(args);
		} catch (CommandLineArgumentException e) {
			System.err.println("Error: " + e.getMessage() + "\n");
			usage();
			System.exit(1);
		}
	}
	
	/**
	 * Runs the program.
	 * @param args command line arguments
	 * @throws CommandLineArgumentException if the command line contains an error
	 */
	private static void run(String[] args) {
		DistanceFieldGenerator generator = new DistanceFieldGenerator();
		String inputFile = null;
		String outputFile = null;
		
		int i = 0;
		try {
			for (; i < args.length; ++i) {
				String arg = args[i];
				if (arg.startsWith("-")) {
					if ("--help".equals(arg)) {
						usage();
						System.exit(0);
					} else if ("--color".equals(arg)) {
						++i;
						generator.setColor(new Color(Integer.parseInt(args[i], 16)));
					} else if ("--downscale".equals(arg)) {
						++i;
						generator.setDownscale(Integer.parseInt(args[i]));
					} else if ("--spread".equals(arg)) {
						++i;
						generator.setSpread(Float.parseFloat(args[i]));
					} else {
						throw new CommandLineArgumentException("unknown option " + arg);
					}
				} else {
					if (inputFile == null) {
						inputFile = arg;
					} else if (outputFile == null) {
						outputFile = arg;
					} else {
						throw new CommandLineArgumentException("exactly two file names are expected");
					}
				}
			}
		} catch (IndexOutOfBoundsException e) {
			throw new CommandLineArgumentException("option " + args[args.length - 1] + " requires an argument");
		} catch (NumberFormatException e) {
			throw new CommandLineArgumentException(args[i] + " is not a number");
		}
		if (inputFile == null) {
			throw new CommandLineArgumentException("no input file specified");
		}
		if (outputFile == null) {
			throw new CommandLineArgumentException("no output file specified");
		}
		
		String outputFormat = outputFile.substring(outputFile.lastIndexOf('.') + 1);
		boolean exists;
		if (!ImageIO.getImageWritersByFormatName(outputFormat).hasNext()) {
			throw new RuntimeException("No image writers found that can handle the format '" + outputFormat + "'");
		}
		
		BufferedImage input = null;
		try {
			input = ImageIO.read(new File(inputFile));
		} catch (IOException e) {
			System.err.println("Failed to load image: " + e.getMessage());
		}
		
		BufferedImage output = generator.generateDistanceField(input);
		
		try {
			ImageIO.write(output, outputFormat, new File(outputFile));
		} catch (IOException e) {
			System.err.println("Failed to write output image: " + e.getMessage());
		}
	}
}
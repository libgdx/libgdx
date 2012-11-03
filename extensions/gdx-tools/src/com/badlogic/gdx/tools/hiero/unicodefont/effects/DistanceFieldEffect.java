package com.badlogic.gdx.tools.hiero.unicodefont.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.tools.hiero.unicodefont.Glyph;
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont;

/**
 * A filter to create a distance field from a source image.
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
 * @author Orangy
 * @author ttencate
 */
public class DistanceFieldEffect implements ConfigurableEffect
{
	// See getValues() for descriptions of these
	private Color color = Color.white;
	private int spread = 4;
	private int upscale = 8;
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public int getSpread() {
		return spread;
	}
	
	public void setSpread(int spread) {
		this.spread = Math.max(1, spread);
	}
	
	public int getUpscale() {
		return upscale;
	}
	
	public void setUpscale(int upscale) {
		this.upscale = Math.max(1, upscale);
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
	 * The input image should be binary (black/white), but if not, any pixel with a value of over 128
	 * in any of its color channels is considered opaque, and transparent otherwise.
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
	public BufferedImage computeDistanceField(BufferedImage inImage)
	{
		final int inWidth = inImage.getWidth();
		final int inHeight = inImage.getHeight();
		final int outWidth = inWidth / upscale;
		final int outHeight = inHeight / upscale;
		final BufferedImage outImage = new BufferedImage(outWidth, outHeight, BufferedImage.TYPE_4BYTE_ABGR);
		
		// Note: coordinates reversed to mimic storage of BufferedImage, for memory locality
		final boolean[][] bitmap = new boolean[inHeight][inWidth];
		for (int y = 0; y < inHeight; ++y) {
			for (int x = 0; x < inWidth; ++x) {
				// Any colour with one of its channels greater than 128 is considered "inside"
				bitmap[y][x] = (inImage.getRGB(x, y) & 0x808080) != 0;
			}
		}
		
		for (int y = 0; y < outHeight; ++y)
		{
			for (int x = 0; x < outWidth; ++x)
			{
				float signedDistance = findSignedDistance(
						(x * upscale) + (upscale / 2),
						(y * upscale) + (upscale / 2),
						bitmap);
				outImage.setRGB(x, y, distanceToRGB(signedDistance));
			}
		}
		
		return outImage;
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
	 * @param pointX The x coordinate of the point 
	 * @param pointY The y coordinate of the point
	 * @param bitmap The upscaled binary glyph image
	 * @return The signed distance, in units of pixels in the <em>output</em> image 
	 */
	private float findSignedDistance(final int pointX, final int pointY, boolean[][] bitmap)
	{
		final int width = bitmap[0].length;
		final int height = bitmap.length;
		final boolean base = bitmap[pointY][pointX];
		
		int maxDist = upscale * spread;
		final int startX = Math.max(0, pointX - maxDist);
		final int endX  = Math.min(width - 1, pointX + maxDist);
		final int startY = Math.max(0, pointY - maxDist);
		final int endY = Math.min(height - 1, pointY + maxDist);

		int closestSquareDist = maxDist * maxDist;
		
		for (int y = startY; y <= endY; ++y)
		{
			for (int x = startX; x <= endX; ++x)
			{
				if (base != bitmap[y][x])
				{
					final int squareDist = squareDist(pointX, pointY, x, y);
					if (squareDist < closestSquareDist)
					{
						closestSquareDist = squareDist;
					}
				}
			}
		}
		
		float closestDist = (float) Math.sqrt(closestSquareDist);
		return (base ? 1 : -1) * closestDist / upscale;
	}

	/**
	 * Draws the glyph to the given image, upscaled by a factor of {@link #upscale}.
	 * 
	 * @param image the image to draw to
	 * @param glyph the glyph to draw
	 */
	private void drawGlyph(BufferedImage image, Glyph glyph) {
		Graphics2D inputG = (Graphics2D) image.getGraphics();
		inputG.setTransform(AffineTransform.getScaleInstance(upscale, upscale));
		// We don't really want anti-aliasing (we'll discard it anyway),
		// but accurate positioning might improve the result slightly
		inputG.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		inputG.setColor(Color.WHITE);
		inputG.fill(glyph.getShape());
	}

	@Override
	public void draw(BufferedImage image, Graphics2D g, UnicodeFont unicodeFont, Glyph glyph) {
		BufferedImage input = new BufferedImage(
				upscale * glyph.getWidth(),
				upscale * glyph.getHeight(),
				BufferedImage.TYPE_BYTE_BINARY);
		drawGlyph(input, glyph);
		
		BufferedImage distanceField = computeDistanceField(input);
		
		g.drawImage(distanceField, new AffineTransform(), null);
	}
	
	@Override
	public String toString() {
		return "Distance field";
	}

	@Override
	public List getValues() {
		List values = new ArrayList();
		values.add(EffectUtil.colorValue("Color", getColor()));
		values.add(EffectUtil.intValue("Upscale", getUpscale(), "The distance field is computed from an image upscaled by this factor. Set this to a higher value for more accuracy, but slower font generation."));
		values.add(EffectUtil.intValue("Spread", getSpread(), "The maximum distance from edges where the effect of the distance field is seen. Set this to about half the width of lines in your font."));
		return values;
	}

	@Override
	public void setValues(List values) {
		for (Iterator iter = values.iterator(); iter.hasNext();) {
			Value value = (Value)iter.next();
			if (value.getName().equals("Color")) {
				setColor((Color)value.getObject());
			} else if (value.getName().equals("Upscale")) {
				setUpscale((Integer)value.getObject());
			} else if (value.getName().equals("Spread")) {
				setSpread((Integer)value.getObject());
			}
		}
		
	}
}
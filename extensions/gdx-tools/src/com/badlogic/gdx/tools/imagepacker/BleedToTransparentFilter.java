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

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/** Set color of transparent pixels (below the given alpha threshold) to the color of the nearest pixel. This is needed to perform
 * correct linear filtering on non alpha premutiplied images.
 * @author Vincent Bousquet */
public class BleedToTransparentFilter implements BufferedImageOp {

	public BleedToTransparentFilter () {
		alphaThreshold = 32;
	}

	public void setAlphaThreshold (int alphaThreshold) {
		this.alphaThreshold = alphaThreshold;
	}

	public int getAlphaThreshold () {
		return alphaThreshold;
	}

	public boolean isDebug () {
		return debug;
	}

	public void setDebug (boolean debug) {
		this.debug = debug;
	}

	@Override
	public BufferedImage filter (BufferedImage src, BufferedImage dest) {
		if (dest == null) dest = createCompatibleDestImage(src, null);
		int w = src.getWidth();
		int h = src.getHeight();
		double dists[] = new double[w * h];
		Arrays.fill(dists, Double.MAX_VALUE);
		final int dFilter = Math.max(w, h);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int rgb = src.getRGB(i, j);
				if (a(rgb) >= alphaThreshold)
					dists[i + j * w] = 0;
				else
					dists[i + j * w] = Double.MAX_VALUE;
				dest.setRGB(i, j, rgb);
			}
		}
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int rgb = dest.getRGB(i, j);
				if (a(rgb) < alphaThreshold) {
					double nearest = Double.MAX_VALUE;
					for (int d = 1; d < dFilter && d * d <= nearest; d++) {
						for (int px = i - d; px <= i + d; px++) {
							double dist = (i - px) * (i - px) + d * d;
							if (dist >= nearest) continue;
							if (in(px, 0, w, j - d, 0, h)) {
								int pos = px + (j - d) * w;
								if (dists[pos] != Double.MAX_VALUE && dist + dists[pos] < nearest) {
									rgb = dest.getRGB(px, j - d);
									nearest = dist + dists[pos];
								}
							}
							if (in(px, 0, w, j + d, 0, h)) {
								int pos = px + (j + d) * w;
								if (dists[pos] != Double.MAX_VALUE && dist + dists[pos] < nearest) {
									rgb = dest.getRGB(px, j + d);
									nearest = dist + dists[pos];
								}
							}
						}
						for (int py = j - (d - 1); py <= j + (d - 1); py++) {
							double dist = (j - py) * (j - py) + d * d;
							if (dist >= nearest) continue;
							if (in(i - d, 0, w, py, 0, h)) {
								int pos = (i - d) + py * w;
								if (dists[pos] != Double.MAX_VALUE && dist + dists[pos] < nearest) {
									rgb = dest.getRGB(i - d, py);
									nearest = dist + dists[pos];
								}
							}
							if (in(i + d, 0, w, py, 0, h)) {
								int pos = (i + d) + py * w;
								if (dists[pos] != Double.MAX_VALUE && dist + dists[pos] < nearest) {
									rgb = dest.getRGB(i + d, py);
									nearest = dist + dists[pos];
								}
							}
						}
					}
					rgb = rgb & 0x00FFFFFF;
					dists[i + j * w] = nearest;
				}
				if (debug) rgb |= 0xFF000000;
				dest.setRGB(i, j, rgb);
			}
		}
		return dest;
	}

	private static boolean in (int x, int x0, int w, int y, int y0, int h) {
		return x0 <= x && x < w && y0 <= y && y < h;
	}

	private static int a (int argb) {
		return (argb >> 24) & 0x0FF;
	}

	@Override
	public Rectangle2D getBounds2D (BufferedImage src) {
		return src.getRaster().getBounds();
	}

	@Override
	public BufferedImage createCompatibleDestImage (BufferedImage src, ColorModel destCM) {
		BufferedImage image;
		int w = src.getWidth();
		int h = src.getHeight();
		WritableRaster wr = null;
		if (destCM == null) {
			destCM = src.getColorModel();
			if (destCM instanceof IndexColorModel) {
				destCM = ColorModel.getRGBdefault();
			} else {
				wr = src.getData().createCompatibleWritableRaster(w, h);
			}
		}
		if (wr == null) wr = destCM.createCompatibleWritableRaster(w, h);
		image = new BufferedImage(destCM, wr, destCM.isAlphaPremultiplied(), null);
		return image;
	}

	@Override
	public Point2D getPoint2D (Point2D srcPt, Point2D dstPt) {
		if (dstPt == null) dstPt = new Point2D.Float();
		dstPt.setLocation(srcPt.getX(), srcPt.getY());
		return dstPt;
	}

	@Override
	public RenderingHints getRenderingHints () {
		return null;
	}

	private int alphaThreshold;

	private boolean debug;

}

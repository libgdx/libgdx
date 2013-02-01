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
package com.badlogic.gdx.tools.hiero.unicodefont.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.tools.distancefield.DistanceFieldGenerator;
import com.badlogic.gdx.tools.hiero.unicodefont.Glyph;
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont;

/**
 * A filter to create a distance field. The resulting font can be rendered
 * with a simple custom shader to draw bitmap fonts that remain crisp
 * even under high magnification.
 * 
 * <p> An example of the use of such a font is included in the libgdx test suite
 * under the name {@code BitmapFontDistanceFieldTest}.
 * 
 * @see DistanceFieldGenerator
 * 
 * @author Thomas ten Cate
 */
public class DistanceFieldEffect implements ConfigurableEffect
{
	private Color color = Color.WHITE;
	private int scale = 1;
	private float spread = 1;

	/**
	 * Draws the glyph to the given image, upscaled by a factor of {@link #scale}.
	 * 
	 * @param image the image to draw to
	 * @param glyph the glyph to draw
	 */
	private void drawGlyph(BufferedImage image, Glyph glyph) {
		Graphics2D inputG = (Graphics2D) image.getGraphics();
		inputG.setTransform(AffineTransform.getScaleInstance(scale, scale));
		// We don't really want anti-aliasing (we'll discard it anyway),
		// but accurate positioning might improve the result slightly
		inputG.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		inputG.setColor(Color.WHITE);
		inputG.fill(glyph.getShape());
	}

	@Override
	public void draw(BufferedImage image, Graphics2D g, UnicodeFont unicodeFont, Glyph glyph) {
		BufferedImage input = new BufferedImage(
			scale * glyph.getWidth(),
			scale * glyph.getHeight(),
			BufferedImage.TYPE_BYTE_BINARY);
		drawGlyph(input, glyph);
		
		DistanceFieldGenerator generator = new DistanceFieldGenerator();
		generator.setColor(color);
		generator.setDownscale(scale);
		// We multiply spread by the scale, so that changing scale will only affect accuracy
		// and not spread in the output image.
		generator.setSpread(scale * spread);
		BufferedImage distanceField = generator.generateDistanceField(input);
		
		g.drawImage(distanceField, new AffineTransform(), null);
	}
	
	@Override
	public String toString() {
		return "Distance field";
	}

	@Override
	public List getValues() {
		List values = new ArrayList();
		values.add(EffectUtil.colorValue("Color", color));
		values.add(EffectUtil.intValue("Scale", scale, "The distance field is computed from an image larger than the output glyph by this factor. Set this to a higher value for more accuracy, but slower font generation."));
		values.add(EffectUtil.floatValue("Spread", spread, 1.0f, Float.MAX_VALUE, "The maximum distance from edges where the effect of the distance field is seen. Set this to about half the width of lines in your output font."));
		return values;
	}

	@Override
	public void setValues(List values) {
		for (Iterator iter = values.iterator(); iter.hasNext();) {
			Value value = (Value)iter.next();
			if ("Color".equals(value.getName())) {
				color = (Color)value.getObject();
			} else if ("Scale".equals(value.getName())) {
				scale = Math.max(1, (Integer)value.getObject());
			} else if ("Spread".equals(value.getName())) {
				spread = Math.max(0, (Float)value.getObject());
			}
		}
		
	}
}
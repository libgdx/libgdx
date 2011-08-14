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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.tools.hiero.unicodefont.Glyph;
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont;

/** Paints glyphs with a gradient fill.
 * @author Nathan Sweet */
public class GradientEffect implements ConfigurableEffect {
	private Color topColor = Color.cyan, bottomColor = Color.blue;
	private int offset = 0;
	private float scale = 1;
	private boolean cyclic;

	public GradientEffect () {
	}

	public GradientEffect (Color topColor, Color bottomColor, float scale) {
		this.topColor = topColor;
		this.bottomColor = bottomColor;
		this.scale = scale;
	}

	public void draw (BufferedImage image, Graphics2D g, UnicodeFont unicodeFont, Glyph glyph) {
		int ascent = unicodeFont.getAscent();
		float height = (ascent) * scale;
		float top = -glyph.getYOffset() + unicodeFont.getDescent() + offset + ascent / 2 - height / 2;
		g.setPaint(new GradientPaint(0, top, topColor, 0, top + height, bottomColor, cyclic));
		g.fill(glyph.getShape());
	}

	public Color getTopColor () {
		return topColor;
	}

	public void setTopColor (Color topColor) {
		this.topColor = topColor;
	}

	public Color getBottomColor () {
		return bottomColor;
	}

	public void setBottomColor (Color bottomColor) {
		this.bottomColor = bottomColor;
	}

	public int getOffset () {
		return offset;
	}

	/** Sets the pixel offset to move the gradient up or down. The gradient is normally centered on the glyph. */
	public void setOffset (int offset) {
		this.offset = offset;
	}

	public float getScale () {
		return scale;
	}

	/** Changes the height of the gradient by a percentage. The gradient is normally the height of most glyphs in the font. */
	public void setScale (float scale) {
		this.scale = scale;
	}

	public boolean isCyclic () {
		return cyclic;
	}

	/** If set to true, the gradient will repeat. */
	public void setCyclic (boolean cyclic) {
		this.cyclic = cyclic;
	}

	public String toString () {
		return "Gradient";
	}

	public List getValues () {
		List values = new ArrayList();
		values.add(EffectUtil.colorValue("Top color", topColor));
		values.add(EffectUtil.colorValue("Bottom color", bottomColor));
		values.add(EffectUtil.intValue("Offset", offset,
			"This setting allows you to move the gradient up or down. The gradient is normally centered on the glyph."));
		values.add(EffectUtil.floatValue("Scale", scale, 0, 10, "This setting allows you to change the height of the gradient by a"
			+ "percentage. The gradient is normally the height of most glyphs in the font."));
		values.add(EffectUtil.booleanValue("Cyclic", cyclic, "If this setting is checked, the gradient will repeat."));
		return values;
	}

	public void setValues (List values) {
		for (Iterator iter = values.iterator(); iter.hasNext();) {
			Value value = (Value)iter.next();
			if (value.getName().equals("Top color")) {
				topColor = (Color)value.getObject();
			} else if (value.getName().equals("Bottom color")) {
				bottomColor = (Color)value.getObject();
			} else if (value.getName().equals("Offset")) {
				offset = ((Integer)value.getObject()).intValue();
			} else if (value.getName().equals("Scale")) {
				scale = ((Float)value.getObject()).floatValue();
			} else if (value.getName().equals("Cyclic")) {
				cyclic = ((Boolean)value.getObject()).booleanValue();
			}
		}
	}
}

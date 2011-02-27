
package com.badlogic.gdx.hiero.unicodefont.effects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.hiero.unicodefont.Glyph;
import com.badlogic.gdx.hiero.unicodefont.UnicodeFont;

/**
 * Strokes glyphs with an outline.
 * @author Nathan Sweet
 */
public class OutlineEffect implements ConfigurableEffect {
	private float width = 2;
	private Color color = Color.black;
	private int join = BasicStroke.JOIN_BEVEL;
	private Stroke stroke;

	public OutlineEffect () {
	}

	public OutlineEffect (int width, Color color) {
		this.width = width;
		this.color = color;
	}

	public void draw (BufferedImage image, Graphics2D g, UnicodeFont unicodeFont, Glyph glyph) {
		g = (Graphics2D)g.create();
		if (stroke != null)
			g.setStroke(stroke);
		else
			g.setStroke(getStroke());
		g.setColor(color);
		g.draw(glyph.getShape());
		g.dispose();
	}

	public float getWidth () {
		return width;
	}

	/**
	 * Sets the width of the outline. The glyphs will need padding so the outline doesn't get clipped.
	 */
	public void setWidth (int width) {
		this.width = width;
	}

	public Color getColor () {
		return color;
	}

	public void setColor (Color color) {
		this.color = color;
	}

	public int getJoin () {
		return join;
	}

	public Stroke getStroke () {
		if (stroke == null) return new BasicStroke(width, BasicStroke.CAP_SQUARE, join);
		return stroke;
	}

	/**
	 * Sets the stroke to use for the outline. If this is set, the other outline settings are ignored.
	 */
	public void setStroke (Stroke stroke) {
		this.stroke = stroke;
	}

	/**
	 * Sets how the corners of the outline are drawn. This is usually only noticeable at large outline widths.
	 * @param join One of: {@link BasicStroke#JOIN_BEVEL}, {@link BasicStroke#JOIN_MITER}, {@link BasicStroke#JOIN_ROUND}
	 */
	public void setJoin (int join) {
		this.join = join;
	}

	public String toString () {
		return "Outline";
	}

	public List getValues () {
		List values = new ArrayList();
		values.add(EffectUtil.colorValue("Color", color));
		values.add(EffectUtil.floatValue("Width", width, 0.1f, 999, "This setting controls the width of the outline. "
			+ "The glyphs will need padding so the outline doesn't get clipped."));
		values.add(EffectUtil.optionValue("Join", String.valueOf(join), new String[][] { {"Bevel", BasicStroke.JOIN_BEVEL + ""},
			{"Miter", BasicStroke.JOIN_MITER + ""}, {"Round", BasicStroke.JOIN_ROUND + ""}},
			"This setting defines how the corners of the outline are drawn. "
				+ "This is usually only noticeable at large outline widths."));
		return values;
	}

	public void setValues (List values) {
		for (Iterator iter = values.iterator(); iter.hasNext();) {
			Value value = (Value)iter.next();
			if (value.getName().equals("Color")) {
				color = (Color)value.getObject();
			} else if (value.getName().equals("Width")) {
				width = ((Float)value.getObject()).floatValue();
			} else if (value.getName().equals("Join")) {
				join = Integer.parseInt((String)value.getObject());
			}
		}
	}
}

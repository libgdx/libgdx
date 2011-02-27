
package com.badlogic.gdx.hiero.unicodefont.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.badlogic.gdx.hiero.unicodefont.Glyph;
import com.badlogic.gdx.hiero.unicodefont.UnicodeFont;

/**
 * A graphical effect that is applied to glyphs in a {@link UnicodeFont}.
 * @author Nathan Sweet
 */
public interface Effect {
	/**
	 * Called to draw the effect.
	 */
	public void draw (BufferedImage image, Graphics2D g, UnicodeFont unicodeFont, Glyph glyph);
}

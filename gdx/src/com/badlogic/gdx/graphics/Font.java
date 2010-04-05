/*
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with libgdx.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.graphics;

import java.util.HashMap;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Rectangle;

/**
 * A Font creates glyph bitmaps and stores them in a texture cache. 
 * It has methods to retrieve text metrics to calculate bounds. To
 * use a Font for text output one has to create a {@link Text} instance
 * via the {@link Font.newText()} method. Once the Font is no longer
 * needed it has to be disposed via the {@link Font.dispose()} method
 * to free all resources.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public abstract class Font 
{	
	/**
	 * The font style
	 * @author mzechner
	 *
	 */
	public enum FontStyle
	{
		Plain,
		Bold,
		Italic,
		BoldItalic
	}
	
	/** the glyph texture **/
	private final Texture texture;
	
	/** glyph hashmap **/
	// FIXME potentially wastefull, for the time being we keep it as a hashmap would be even worse.
	private final Glyph[] glyphs = new Glyph[Character.MAX_VALUE];
	
	/** current position in glyph texture to write the next glyph to **/
	private int glyphX = 0;
	private int glyphY = 0;
	
	/** the graphics instance **/
	private Graphics graphics;
	
	/** whether the Font is managed or not **/
	private boolean isManaged;
	
	protected Font( Graphics graphics, boolean managed )
	{
		this.texture = graphics.newTexture( 512, 512, Pixmap.Format.RGBA4444, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, managed );
		this.graphics = graphics;
		this.isManaged = managed;
	}
	
	/**
	 * Disposes the font and all associated 
	 * resources. Does not dispose the {@link TextRun}s
	 * created via this Font.
	 */
	public void dispose( )
	{
		texture.dispose();
	}
	
	/**
	 * @return The height of a line in pixels
	 */
	public abstract int getLineHeight( );
	
	/**
	 * @return The gap in pixels between two lines
	 */
	public abstract int getLineGap( );
	
	/**
	 * Returns a bitmap containing the glyph for the
	 * given character. The bitmap height equals the
	 * value returned by getLineHeight()
	 * 
	 * @param character The character to get the glyph for
	 * @return A {@link Pixmap} containing the glyph
	 */
	protected abstract Pixmap getGlyphBitmap( char character );
	
	/**
	 * @param character The character to get the advance for
	 * @return The advance in pixels
	 */
	public abstract int getGlyphAdvance( char character );	
	
	/**
	 * Returns the pixel bounds of a glyph
	 * @param character The character
	 * @param Rect structure to be filled with the bounds
	 */
	public abstract void getGlyphBounds( char character, Rectangle rect );
	
	/**
	 * Returns the width of the string in pixels. Note that this
	 * ignores newlines.
	 * 
	 * @param text The text to get the width for
	 * @return The width of the text in pixels
	 */
	public abstract int getStringWidth( String text );
	
	/**
	 * @return The glyph texture
	 */
	protected Texture getTexture( )
	{
		return texture;
	}
	
	/**
	 * Returns the glyph for the given character
	 * 
	 * @param character The character
	 * @return The glyph of the character
	 */
	protected Glyph getGlyph( char character )
	{
		Glyph glyph = glyphs[character];
		if( glyph == null )
		{
			glyph = createGlyph(character);
			glyphs[character] = glyph;
		}
		return glyph;
	}
	
	private Glyph createGlyph( char character )
	{
		Pixmap bitmap = getGlyphBitmap( character );
		Rectangle rect = new Rectangle( );
		getGlyphBounds( character, rect );

		if( glyphX + rect.getWidth() >= 512)
		{
			glyphX = 0;
			glyphY += getLineGap() + getLineHeight();
		}
		
		texture.draw( bitmap, glyphX, glyphY );		
						
		Glyph glyph = new Glyph( getGlyphAdvance( character ), (int)rect.getWidth(), (int)rect.getHeight(), glyphX / 512.0f, glyphY / 512.0f, rect.getWidth() / 512.0f, rect.getHeight() / 512.0f );
		glyphX += rect.getWidth();
		return glyph;	
	}
	
	/** 
	 * Package private helper class to store
	 * glyph information.
	 * 
	 * @author mzechner
	 *
	 */
	protected class Glyph
	{
		public int advance;
		public int width;		
		public int height;
		public float u;
		public float v;
		public float uWidth;
		public float vHeight;
		
		public Glyph( int advance, int width, int height, float u, float v, float uWidth, float vHeight )
		{
			this.advance = advance;
			this.width = width;
			this.height = height;
			this.u = u;
			this.v = v;
			this.uWidth = uWidth;
			this.vHeight = vHeight;
		}
	}

	/**
	 * @return whether the font is managed or not
	 */
	public boolean isManaged() 
	{	
		return isManaged;
	}	
}

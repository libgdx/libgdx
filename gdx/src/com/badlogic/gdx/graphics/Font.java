/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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
package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Rectangle;

/**
 * <p>
 * A Font creates glyph bitmaps and stores them in a texture cache. 
 * It has methods to retrieve text metrics to calculate bounds. To
 * render text with a specific Font one has to use a {@link SpriteBatch}
 * instance and it's {@link SpriteBatch.drawText()} method. Once the Font is no longer
 * needed it has to be disposed via the {@link Font.dispose()} method
 * to free all resources.
 * </p>
 * 
 * <p>
 * Fonts must be created by the {@link Graphics.newFont()} methods.
 * </p>
 * 
 * <p>
 * A Font can be managed. In case the OpenGL context is lost all OpenGL resources
 * such as textures get invalidated and have to be reloaded. A Font is basically a
 * texture and is thus also invalidated by a context loss. A context loss happens
 * when a user presses switches to another application or receives an incoming call. This
 * only happens on Android, there are not context losses on the desktop. A managed Font
 * will be reloaded automatically after a context loss so you don't have to do that
 * manually. The drawback: it uses twice the memory for storing the original bitmap
 * from which the texture cache was created. Use this only if you know that you have
 * enough memory available. Future versions will use a different mechanism to get
 * rid of this additional memory overhead.
 * </p>
 * 
 * @author badlogicgames@gmail.com
 *
 * FIXME this class could be improved quiet a lot. Dynamical texture resizes, better
 * subregion allocation and so on. 
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
	
	private final static int TEXTURE_WIDTH = 512;
	private final static int TEXTURE_HEIGHT = 512;
	
	/** the glyph texture **/
	private final Texture texture;
	
	/** glyph hashmap **/
	// FIXME potentially wasteful, for the time being we keep it as a hashmap would be even worse.
	private final Glyph[] glyphs = new Glyph[Character.MAX_VALUE];
	
	/** current position in glyph texture to write the next glyph to **/
	private int glyphX = 0;
	private int glyphY = 0;
	
	
	/** whether the Font is managed or not **/
	private boolean isManaged;
	
	protected Font( Graphics graphics, boolean managed )
	{
		this.texture = graphics.newTexture( 512, 512, Pixmap.Format.RGBA8888, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, managed );		
		this.isManaged = managed;
	}
	
	/**
	 * Disposes the font and all associated 
	 * resources.
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

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
	private final HashMap<Character, Glyph> glyphs = new HashMap<Character, Glyph>( );
	
	/** current position in glyph texture to write the next glyph to **/
	private int glyphX = 0;
	private int glyphY = 0;
	
	/** the graphics instance **/
	private Graphics graphics;
	
	/** whether the Font is managed or not **/
	private boolean isManaged;
	
	protected Font( Graphics graphics, boolean managed )
	{
		this.texture = graphics.newTexture( 256, 256, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, managed );
		this.graphics = graphics;
		this.isManaged = managed;
	}
	
	/**
	 * Creates a new {@link Text}.
	 * 
	 * @return The new Text.
	 */
	public Text newText( )
	{
		return new Text(graphics, this);
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
		Glyph glyph = glyphs.get(character);
		if( glyph == null )
		{
			glyph = createGlyph(character);
			glyphs.put( character, glyph );
		}
		return glyph;
	}
	
	private Glyph createGlyph( char character )
	{
		Pixmap bitmap = getGlyphBitmap( character );
		Rectangle rect = new Rectangle( );
		getGlyphBounds( character, rect );

		if( glyphX + rect.getWidth() >= 256)
		{
			glyphX = 0;
			glyphY += getLineGap() + getLineHeight();
		}
		
		texture.draw( bitmap, glyphX, glyphY );		
						
		Glyph glyph = new Glyph( getGlyphAdvance( character ), (int)rect.getWidth(), (int)rect.getHeight(), glyphX / 256.0f, glyphY / 256.0f, rect.getWidth() / 256.0f, rect.getHeight() / 256.0f );
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
	class Glyph
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

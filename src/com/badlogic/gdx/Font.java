package com.badlogic.gdx;

import java.util.HashMap;

import com.badlogic.gdx.Application.TextureFilter;
import com.badlogic.gdx.Application.TextureWrap;
import com.badlogic.gdx.Mesh.PrimitiveType;
import com.badlogic.gdx.math.Rectangle;

/**
 * A font returns glyph bitmaps for characters as well
 * as some metrics. Does only work for left to right
 * languages without surrogates.
 * 
 * @author mzechner
 *
 */
public abstract class Font 
{
	private final Texture texture;
	
	private class Glyph
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
	 * A textrun is a mesh that holds the glyphs
	 * of the given string formated to fit the
	 * rectangle and alignment.
	 * 
	 * @author mzechner
	 *
	 */
	public class TextRun
	{
		private Application app;			
		private Mesh mesh;
		private String text = "";
		private int width;
		private int height;
		private HorizontalAlign hAlign;
		private VerticalAlign vAlign;
		private boolean wordWrap = false;
		private String[] lines;
		private int[] widths;
		private float posX, posY;
		
		protected TextRun( Application app )
		{			
			this.app = app;			
		}
		
		public void setTextArea( int width, int height )
		{
			this.width = width;
			this.height = height;
		}
		
		public void setHorizontalAlign( HorizontalAlign hAlign )
		{
			this.hAlign = hAlign;
		}
		
		public void setVerticalAlign( VerticalAlign vAlign )
		{
			this.vAlign = vAlign;
		}
		
		public void setText( String text )
		{					
			if( this.text.equals( text ) )
				return;
			
			if( text == null )
				text = "";
			
			this.text = text;
			this.lines = text.split( "\n" );
			this.widths = new int[lines.length];
			for( int i = 0; i < lines.length; i++ )
				widths[i] = getStringWidth( lines[i] );
			rebuild( );					
		}
		
		public void setPosition( float x, float y )
		{
			posX = x;
			posY = y;
			rebuild( );
		}
		
		private void rebuild( )
		{					
			if( mesh == null )			
				mesh = app.newMesh( 6 * text.length(), false, false, true, false, 0, true );
			
			if( mesh.getMaximumVertices() / 6 < text.length() )
			{
				mesh.dispose();
				mesh = app.newMesh( 6 * text.length(), false, false, true, false, 0, true );
			}
						
			mesh.reset();
			int lineHeight = getLineHeight();
			for( int i = 0; i < lines.length; i++ )
			{
				String line = lines[i];
				int x = 0;
				int y = height;
				
				if( hAlign == HorizontalAlign.Left )
					x = 0;
				if( hAlign == HorizontalAlign.Center )
					x = width / 2 - widths[i] / 2;
				if( hAlign == HorizontalAlign.Right )
					x = width - widths[i];
				
				if( vAlign == VerticalAlign.Top )
					y = height;
				if( vAlign == VerticalAlign.Center )
					y = height / 2 + lines.length * (getLineHeight() + getLineGap()) / 2;				
				if( vAlign == VerticalAlign.Bottom )
					y = lines.length * (getLineHeight() + getLineGap());
				
				y -= i * (getLineHeight() + getLineGap());
				
				for( int j = 0; j < line.length(); j++ )
				{
					Glyph glyph = getGlyph( line.charAt(j) );
					mesh.texCoord( glyph.u, glyph.v );
					mesh.vertex( posX + x, posY + y, 0 );
					mesh.texCoord( glyph.u + glyph.uWidth, glyph.v );
					mesh.vertex( posX + x + glyph.width, posY + y, 0 );
					mesh.texCoord( glyph.u + glyph.uWidth, glyph.v + glyph.vHeight );
					mesh.vertex( posX + x + glyph.width, posY + y - lineHeight, 0 );
					mesh.texCoord( glyph.u + glyph.uWidth, glyph.v + glyph.vHeight );
					mesh.vertex( posX + x + glyph.width, posY + y - lineHeight, 0 );
					mesh.texCoord( glyph.u, glyph.v + glyph.vHeight );
					mesh.vertex( posX + x, posY + y - lineHeight, 0 );
					mesh.texCoord( glyph.u, glyph.v );
					mesh.vertex( posX + x, y, 0 );
					x += glyph.advance;
				}
			}
		}
		
		public void render( )
		{
			if( mesh == null )
				return;
						
			texture.bind();
			mesh.render(PrimitiveType.Triangles);
		}
		
		public void dispose( )
		{
			if( mesh != null )
				mesh.dispose();
		}
	}
	
	/**
	 * Horizontal text alignement
	 * @author mzechner
	 *
	 */
	public enum HorizontalAlign
	{
		Left,
		Center, 
		Right
	}
	
	/**
	 * Vertical text alignement
	 * @author mzechner	 
	 */
	public enum VerticalAlign
	{
		Top,
		Center,
		Bottom
	}
	
	/** glyph hashmap **/
	private final HashMap<Character, Glyph> glyphs = new HashMap<Character, Glyph>( );
	/** current position in glyph texture to write the next glyph to **/
	private int glyphX = 0;
	private int glyphY = 0;
	/** the application **/
	private Application app;	
	protected Font( Application app )
	{
		this.texture = app.newTexture( 256, 256, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
		this.app = app;
	}
	
	/**
	 * Creates a new {@link TextRun}.
	 * 
	 * @return The new TextRun.
	 */
	public TextRun newTextRun( )
	{
		return new TextRun(app);
	}
	
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
		Object bitmap = getGlyphBitmap( character );
		Rectangle rect = new Rectangle( );
		getGlyphBounds( character, rect );

		if( glyphX + rect.width >= 256)
		{
			glyphX = 0;
			glyphY += getLineGap() + getLineHeight();
		}
		
		texture.draw( bitmap, glyphX, glyphY );		
						
		Glyph glyph = new Glyph( getGlyphAdvance( character ), (int)rect.width, (int)rect.height, glyphX / 256.0f, glyphY / 256.0f, rect.width / 256.0f, rect.height / 256.0f );
		glyphX += rect.width;
		return glyph;
		
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
	 * @return A bitmap containing the glyph
	 */
	protected abstract Object getGlyphBitmap( char character );
	
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
		
}

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Font.Glyph;

/**
 * A textrun is a mesh that holds the glyphs
 * of the given string formated to fit the
 * rectangle and alignment.
 * 
 * @author mzechner
 *
 */
public class Text
{
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
	
	private Graphics graphics;
	private Font font;
	private MeshRenderer mesh;
	private String text = "";
	private int width;
	private int height;
	private HorizontalAlign hAlign;
	private VerticalAlign vAlign;
	private boolean wordWrap = false;
	private String[] lines;
	private int[] widths;
	
	protected Text( Graphics graphics, Font font )
	{			
		this.graphics = graphics;
		this.font = font;
	}
	
	public void setTextArea( int width, int height )
	{
		this.width = width;
		this.height = height;
		rebuild();
	}
	
	public void setHorizontalAlign( HorizontalAlign hAlign )
	{
		this.hAlign = hAlign;
		rebuild();
	}
	
	public void setVerticalAlign( VerticalAlign vAlign )
	{
		this.vAlign = vAlign;
		rebuild();
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
			widths[i] = font.getStringWidth( lines[i] );
		rebuild( );					
	}	
	
	private void rebuild( )
	{					
		if( mesh == null )		
		{
			FloatMesh m = new FloatMesh( 6 * text.length(), 3, false, false, true, 1, 2, false, 0 );
			mesh = new MeshRenderer( graphics.getGL10(), m, false, font.isManaged() );
		}
		
		if( mesh.getMaximumVertices() / 6 < text.length() )
		{
			mesh.dispose();
			FloatMesh m = new FloatMesh( 6 * text.length(), 3, false, false, true, 1, 2, false, 0 );
			mesh = new MeshRenderer( graphics.getGL10(), m, false, font.isManaged() );
		}
					
		float[] vertices = ((FloatMesh)mesh.getMesh()).getVerticesArray();
		int vertIdx = 0;
		
		int lineHeight = font.getLineHeight();
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
				y = height / 2 + lines.length * (font.getLineHeight() + font.getLineGap()) / 2;				
			if( vAlign == VerticalAlign.Bottom )
				y = lines.length * (font.getLineHeight() + font.getLineGap());
			
			y -= i * (font.getLineHeight() + font.getLineGap());
			
			for( int j = 0; j < line.length(); j++ )
			{
				Glyph glyph = font.getGlyph( line.charAt(j) );
				vertices[vertIdx++] = x; vertices[vertIdx++] = y; vertices[vertIdx++] = 0; 
				vertices[vertIdx++] = glyph.u; vertices[vertIdx++] = glyph.v;
				vertices[vertIdx++] = x + glyph.width; vertices[vertIdx++] = y; vertices[vertIdx++] = 0; 
				vertices[vertIdx++] = glyph.u + glyph.uWidth; vertices[vertIdx++] = glyph.v;
				vertices[vertIdx++] = x + glyph.width; vertices[vertIdx++] = y - lineHeight; vertices[vertIdx++] = 0; 
				vertices[vertIdx++] = glyph.u + glyph.uWidth; vertices[vertIdx++] = glyph.v + glyph.vHeight;
				vertices[vertIdx++] = x + glyph.width; vertices[vertIdx++] = y - lineHeight; vertices[vertIdx++] = 0; 
				vertices[vertIdx++] = glyph.u + glyph.uWidth; vertices[vertIdx++] = glyph.v + glyph.vHeight;				
				vertices[vertIdx++] = x; vertices[vertIdx++] = y - lineHeight; vertices[vertIdx++] = 0; 
				vertices[vertIdx++] = glyph.u; vertices[vertIdx++] = glyph.v + glyph.vHeight;
				vertices[vertIdx++] = x; vertices[vertIdx++] = y; vertices[vertIdx++] = 0; 
				vertices[vertIdx++] = glyph.u; vertices[vertIdx++] = glyph.v;							
				x += glyph.advance;
			}
		}
		
		((FloatMesh)mesh.getMesh()).updateVertexBufferFromArray( vertIdx / 5 );
		mesh.update();
	}
	
	public void render( )
	{
		font.getTexture().bind();
		mesh.render( GL10.GL_TRIANGLES, 0, mesh.getMesh().getNumVertices() );
	}
}
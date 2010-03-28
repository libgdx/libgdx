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

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Font.Glyph;

/**
 * A Text is created by a {@link Font} and allows you to display formated
 * strings. A text creates a mesh internally which holds all the glyphs of
 * the string. Newlines get interpreted. A text can be aligned horizontally
 * either to the left, the center or the right. The glyphs of the mesh are
 * constructed on the x/z plane in the positive quadrant. The bounds of the
 * Text are [0,0]-[Text.getWidth(), Text.getHeight()]. Once the text is no
 * longer used it must be disposed via the {@link Text.dispose()} method.
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
	
	private Graphics graphics;
	private Font font;
	private MeshRenderer mesh;
	private String text = "";	
	private HorizontalAlign hAlign = HorizontalAlign.Left;	
	private String[] lines;
	private int[] widths;
	private int width;	
	private boolean dirty = true;
	
	protected Text( Graphics graphics, Font font )
	{			
		this.graphics = graphics;
		this.font = font;
	}	
	
	/**
	 * @return the width of the text's bounding box
	 */
	public int getWidth( )
	{
		return width;
	}
	
	/**
	 * @return the height of the text's bounding box
	 */ 
	public int getHeight( )
	{
		return lines.length * font.getLineHeight();
	}
	
	/**
	 * Sets the horizontal alignment of this text.
	 * 
	 * @param hAlign the {@link HorizontalAlign}
	 */
	public void setHorizontalAlign( HorizontalAlign hAlign )
	{
		this.hAlign = hAlign;
		dirty = true;		
	}	
	
	/**
	 * Sets the text of this Text. If the text contains
	 * new lines they get interpreted.
	 * 
	 * @param text the text 
	 */
	public void setText( String text )
	{					
		if( text == null )
			throw new IllegalArgumentException( "text must not be null" );
		if( this.text.equals( text ) )
			return;
		
		if( text == null )
			text = "";
		
		this.text = text;
		this.lines = text.split( "\n" );
		this.widths = new int[lines.length];
		width = 0;
		for( int i = 0; i < lines.length; i++ )
		{
			widths[i] = font.getStringWidth( lines[i] );
			if( width < widths[i] )
				width = widths[i];
		}
		dirty = true;			
	}	
	
	/**
	 * Rebuilds the Text from the last set text and {@link HorizontalAlign}.
	 * This rebuilds he internal mesh and recalculates the bounds of the text. 
	 */
	private void rebuild( )
	{					
		if( mesh == null )		
		{
			FloatMesh m = new FloatMesh( 6 * text.length(), 3, false, false, true, 1, 2, false, 0 );
			mesh = new MeshRenderer( graphics.getGL10(), m, false, font.isManaged() );
		}
		
		if( mesh.getMesh().getMaximumVertices() / 6 <= text.length() )
		{
			mesh.dispose();
			FloatMesh m = new FloatMesh( 6 * text.length(), 3, false, false, true, 1, 2, false, 0 );
			mesh = new MeshRenderer( graphics.getGL10(), m, false, font.isManaged() );
		}
					
		float[] vertices = ((FloatMesh)mesh.getMesh()).getVerticesArray();
		int vertIdx = 0;
		
		int lineHeight = font.getLineHeight();
		int height = lines.length * lineHeight;
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
		dirty = false;
	}
	
	/**
	 * Renders the Text. Note that this will bind the glyph texture
	 * of the {@link Font} being used by this text. The glyph texture
	 * will stay bound. You will have to have blending and texturing enabled for this 
	 * to work. 
	 */
	public void render( )	
	{
		if( dirty )
			rebuild( );
		font.getTexture().bind();
		mesh.render( GL10.GL_TRIANGLES, 0, mesh.getMesh().getNumVertices() );
	}
}
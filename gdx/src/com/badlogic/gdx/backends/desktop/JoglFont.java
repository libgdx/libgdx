/**
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
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.backends.desktop;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;


/**
 * An implementation of {@link Font} based on the java graphics framework.
 * 
 * @author badlogicgames@gmail.com
 *
 */
final class JoglFont extends Font 
{
	private final BufferedImage tmpBitmap = new BufferedImage( 1, 1, BufferedImage.TYPE_4BYTE_ABGR );
	private java.awt.Font font; 
	private FontMetrics metrics;
	
	JoglFont( com.badlogic.gdx.Graphics graphics, String fontName, int size, FontStyle style, boolean managed )
	{	
		super( graphics, managed );
		font = new java.awt.Font( fontName, getJavaFontStyle(style), size );
		Graphics g = tmpBitmap.getGraphics();
		g.setFont( font );
		metrics = g.getFontMetrics();
		g.dispose();
	}
	
	JoglFont( com.badlogic.gdx.Graphics graphics, InputStream in, int size, FontStyle style, boolean managed )
	{
		super( graphics, managed );
		
		try
		{
			java.awt.Font baseFont = java.awt.Font.createFont( java.awt.Font.PLAIN, in);
			font = baseFont.deriveFont( getJavaFontStyle(style), size );
		}
		catch( Exception ex )
		{
			font = new java.awt.Font( "Arial", getJavaFontStyle(style), size );
		}
		Graphics g = tmpBitmap.getGraphics();
		g.setFont( font );
		metrics = g.getFontMetrics();
		g.dispose();
	}
	
	private int getJavaFontStyle( FontStyle style )
	{
		if( style == FontStyle.Plain )
			return java.awt.Font.PLAIN;
		if( style == FontStyle.Bold )
			return java.awt.Font.BOLD;
		if( style == FontStyle.Italic )
			return java.awt.Font.ITALIC;
		if( style == FontStyle.BoldItalic )
			return java.awt.Font.BOLD | java.awt.Font.ITALIC;
		
		return java.awt.Font.PLAIN;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getGlyphAdvance(char character) 
	{	
		return metrics.charWidth(character);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pixmap getGlyphBitmap(char character) {
		Graphics2D g = (Graphics2D)tmpBitmap.getGraphics();
		g.setFont( font );
		Rectangle2D bounds = metrics.getStringBounds( "" + character, g);
		g.dispose( );
		
		BufferedImage bitmap = new BufferedImage( (int)Math.ceil(bounds.getWidth()), getLineHeight(), BufferedImage.TYPE_4BYTE_ABGR );
		g = (Graphics2D)bitmap.getGraphics();
		
		g.setFont(font);		
		g.setColor( new java.awt.Color( 0x0000000, true ) );
		g.fillRect( 0, 0, bitmap.getWidth(), bitmap.getHeight() );
		g.setColor( new java.awt.Color( 0xffffffff, true ) );
		g.drawString( "" + character, 0, metrics.getAscent());
		g.dispose();
		
		return new JoglPixmap( bitmap );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLineGap() {
		return metrics.getLeading();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLineHeight() {
		return metrics.getAscent() + metrics.getDescent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStringWidth(String text) {					
		Graphics g = tmpBitmap.getGraphics();
		g.setFont( font );
		int width = (int)Math.ceil(metrics.getStringBounds(text, g).getWidth());
		g.dispose();
		return width;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void getGlyphBounds(char character, Rectangle rect) {
		Graphics g = tmpBitmap.getGraphics();
		g.setFont( font );
		Rectangle2D bounds = metrics.getStringBounds( "" + character, g);
		g.dispose();
		rect.setWidth( (int)Math.ceil(bounds.getWidth()) );
		rect.setHeight( getLineHeight() );
	}

}

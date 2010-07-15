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
package com.badlogic.gdx.backends.applet;

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
final class AppletFont extends Font 
{
	private final BufferedImage tmpBitmap = new BufferedImage( 1, 1, BufferedImage.TYPE_4BYTE_ABGR );
	private java.awt.Font font; 
	private FontMetrics metrics;
	
	AppletFont( com.badlogic.gdx.Graphics graphics, String fontName, int size, FontStyle style, boolean managed )
	{	
		super( graphics, managed );
		font = new java.awt.Font( fontName, getJavaFontStyle(style), size );
		Graphics g = tmpBitmap.getGraphics();
		g.setFont( font );
		metrics = g.getFontMetrics();
		g.dispose();
	}
	
	AppletFont( com.badlogic.gdx.Graphics graphics, InputStream in, int size, FontStyle style, boolean managed )
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
	    g.addRenderingHints( new RenderingHints( RenderingHints.KEY_ANTIALIASING,
	                                             RenderingHints.VALUE_ANTIALIAS_ON ));	    
	    g.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
	                          RenderingHints.VALUE_TEXT_ANTIALIAS_ON );

		g.setColor( new java.awt.Color( 0x0000000, true ) );
		g.fillRect( 0, 0, bitmap.getWidth(), bitmap.getHeight() );
		g.setColor( new java.awt.Color( 0xffffffff, true ) );
		g.drawString( "" + character, 0, metrics.getAscent());
		g.dispose();
		
		return new AppletPixmap( bitmap );
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

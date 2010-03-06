package com.badlogic.gdx.backends.jogl;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Font;
import com.badlogic.gdx.Application.FontStyle;
import com.badlogic.gdx.math.Rectangle;

public class JoglFont extends Font 
{
	private final BufferedImage tmpBitmap = new BufferedImage( 1, 1, BufferedImage.TYPE_4BYTE_ABGR );
	private java.awt.Font font; 
	private FontMetrics metrics;
	
	public JoglFont( Application app, String fontName, int size, FontStyle style )
	{	
		super( app );
		font = new java.awt.Font( fontName, getJavaFontStyle(style), size );
		Graphics g = tmpBitmap.getGraphics();
		g.setFont( font );
		metrics = g.getFontMetrics();
		g.dispose();
	}
	
	public JoglFont( Application app, InputStream in, int size, FontStyle style )
	{
		super( app );
		int fontStyle = getJavaFontStyle( style );
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
	
	@Override
	public int getGlyphAdvance(char character) 
	{	
		return metrics.charWidth(character);
	}

	@Override
	public Object getGlyphBitmap(char character) {
		Graphics g = tmpBitmap.getGraphics();
		g.setFont( font );
		Rectangle2D bounds = metrics.getStringBounds( "" + character, g);
		g.dispose( );
		
		BufferedImage bitmap = new BufferedImage( (int)Math.ceil(bounds.getWidth()), getLineHeight(), BufferedImage.TYPE_4BYTE_ABGR );
		g = bitmap.getGraphics();
		g.setFont(font);
		g.setColor( new java.awt.Color( 0x0000000, true ) );
		g.fillRect( 0, 0, bitmap.getWidth(), bitmap.getHeight() );
		g.setColor( new java.awt.Color( 0xffffffff, true ) );
		g.drawString( "" + character, 0, metrics.getAscent());
		g.dispose();
		
		return bitmap;
	}

	@Override
	public int getLineGap() {
		return metrics.getLeading();
	}

	@Override
	public int getLineHeight() {
		return metrics.getAscent() + metrics.getDescent();
	}

	@Override
	public int getStringWidth(String text) {					
		Graphics g = tmpBitmap.getGraphics();
		g.setFont( font );
		int width = (int)Math.ceil(metrics.getStringBounds(text, g).getWidth());
		g.dispose();
		return width;
	}

	@Override
	public void getGlyphBounds(char character, Rectangle rect) {
		Graphics g = tmpBitmap.getGraphics();
		g.setFont( font );
		Rectangle2D bounds = metrics.getStringBounds( "" + character, g);
		g.dispose();
		rect.width = (int)Math.ceil(bounds.getWidth());
		rect.height = getLineHeight();
	}

}

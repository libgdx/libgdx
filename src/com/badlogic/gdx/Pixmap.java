package com.badlogic.gdx;

public interface Pixmap 
{
	public enum Format
	{
		Alpha,
		RGBA4444,
		RGBA8888
	}
	
	public void setColor( float r, float g, float b, float a );
	
	public void fill( );
	
	public void setStrokeWidth( int width );
	
	public void drawLine( int x, int y, int x2, int y2 );
	
	public void drawRectangle( int x, int y, int width, int height );
	
	public void drawPixmap( Pixmap pixmap, int x, int y, int srcx, int srcy, int width, int height );
	
	public void fillRectangle( int x, int y, int width, int height );
	
	public void drawCircle( int x, int y, int radius );
	
	public void fillCircle( int x, int y, int radius );
	
	public int getPixel( int x, int y );	
	
	public void getPixelRow( int[] pixels, int y );
	
	public Object getNativePixmap( );

	public int getWidth();

	public int getHeight( );
	
	public void dispose( );
}

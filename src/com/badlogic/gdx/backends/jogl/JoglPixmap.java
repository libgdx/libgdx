package com.badlogic.gdx.backends.jogl;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.badlogic.gdx.Pixmap;

public class JoglPixmap implements Pixmap
{
	BufferedImage pixmap;
	Composite composite;
	Color color = new Color( 0 );
	int strokeWidth = 1;

	public JoglPixmap( int width, int height, Pixmap.Format format )
	{
		int internalformat = getInternalFormat( format );
		pixmap = new BufferedImage(width, height, internalformat);
		composite = AlphaComposite.Src;
	}
	
	public JoglPixmap(BufferedImage image) 
	{
		pixmap = image;
	}

	private int getInternalFormat( Pixmap.Format format )
	{
		if( format == Pixmap.Format.RGBA4444 || format == Pixmap.Format.RGBA8888 )
			return BufferedImage.TYPE_4BYTE_ABGR;
		else
			return BufferedImage.TYPE_BYTE_GRAY;
	}
	
	@Override
	public void drawCircle(int x, int y, int radius) {
		Graphics2D g = (Graphics2D)pixmap.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setComposite( composite );
		g.setColor( color );
		g.setStroke( new BasicStroke( strokeWidth ) );
		g.drawRect(x, y, radius * 2, radius * 2);		
		g.dispose();
		
	}

	@Override
	public void drawLine(int x, int y, int x2, int y2) {
		Graphics2D g = (Graphics2D)pixmap.getGraphics();	
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setComposite( composite );
		g.setColor( color );
		g.setStroke( new BasicStroke( strokeWidth ) );
		g.drawLine(x, y, x2, y2);		
		g.dispose();
		
	}

	@Override
	public void drawRectangle(int x, int y, int width, int height) {
		Graphics2D g = (Graphics2D)pixmap.getGraphics();		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setComposite( composite );
		g.setColor( color );
		g.setStroke( new BasicStroke( strokeWidth ) );
		g.drawRect(x, y, width, height);		
		g.dispose();
	}

	@Override
	public void fill() 
	{	
		Graphics2D g = (Graphics2D)pixmap.getGraphics();	
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setComposite( composite );
		g.setColor( color );
		g.fillRect( 0, 0, pixmap.getWidth(), pixmap.getHeight() );
		g.dispose();
	}

	@Override
	public void fillCircle(int x, int y, int radius) 
	{	
		Graphics2D g = (Graphics2D)pixmap.getGraphics();	
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setComposite( composite );
		g.setColor( color );		
		g.fillOval( x, y, radius * 2, radius * 2);		
		g.dispose();
	}

	@Override
	public void fillRectangle(int x, int y, int width, int height) 
	{	
		Graphics2D g = (Graphics2D)pixmap.getGraphics();	
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setComposite( composite );
		g.setColor( color );
		g.fillRect( x, y, width, height );
		g.dispose();
	}

	@Override
	public Object getNativePixmap() 
	{	
		return pixmap;
	}

	@Override
	public void setColor(float r, float g, float b, float a) 
	{	
		color = new Color( r, g, b, a );
	}

	@Override
	public void setStrokeWidth(int width) 
	{	
		strokeWidth = width;
	}

	@Override
	public int getPixel(int x, int y) 
	{	
		if( x < 0 || x >= pixmap.getWidth() )
			return 0;
		if( y < 0 || y >= pixmap.getHeight() )
			return 0;
		return pixmap.getRGB(x, y);
	}

	@Override
	public int getHeight() {
		return pixmap.getHeight();
	}

	@Override
	public int getWidth() {
		return pixmap.getWidth();
	}

	@Override
	public void drawPixmap(Pixmap pixmap, int x, int y, int srcx, int srcy, int width, int height) 
	{
		BufferedImage image = (BufferedImage)pixmap.getNativePixmap();
				
		Graphics2D g = (Graphics2D)this.pixmap.getGraphics();
		g.setComposite( composite );
		g.drawImage(image, x, y, x + width, y + height, 
					 srcx, srcy, srcx + width, srcy + height, null);
		g.dispose();
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getPixelRow(int[] pixels, int y) 
	{		
		for( int x = 0; x < pixmap.getWidth(); x++ )
		{
			pixels[x] = pixmap.getRGB(x, y);
		}
		
	}
	
}

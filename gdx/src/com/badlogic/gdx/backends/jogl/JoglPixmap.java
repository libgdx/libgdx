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
package com.badlogic.gdx.backends.jogl;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.badlogic.gdx.graphics.Pixmap;

/**
 * An implementation of Pixmap based on the java graphics framework.
 * 
 * @author badlogicgames@gmail.com
 *
 */
final class JoglPixmap implements Pixmap
{
	BufferedImage pixmap;
	Composite composite;
	Color color = new Color( 0 );
	int strokeWidth = 1;

	JoglPixmap( int width, int height, Pixmap.Format format )
	{
		int internalformat = getInternalFormat( format );
		pixmap = new BufferedImage(width, height, internalformat);
		composite = AlphaComposite.Src;
	}
	
	JoglPixmap(BufferedImage image) 
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
	
	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getNativePixmap() 
	{	
		return pixmap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setColor(float r, float g, float b, float a) 
	{	
		color = new Color( r, g, b, a );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStrokeWidth(int width) 
	{	
		strokeWidth = width;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPixel(int x, int y) 
	{	
		if( x < 0 || x >= pixmap.getWidth() )
			return 0;
		if( y < 0 || y >= pixmap.getHeight() )
			return 0;
		return pixmap.getRGB(x, y);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHeight() {
		return pixmap.getHeight();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWidth() {
		return pixmap.getWidth();
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void getPixelRow(int[] pixels, int y) 
	{		
		for( int x = 0; x < pixmap.getWidth(); x++ )
		{
			pixels[x] = pixmap.getRGB(x, y);
		}
		
	}
	
}

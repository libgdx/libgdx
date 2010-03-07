package com.badlogic.gdx.backends.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;

import com.badlogic.gdx.Pixmap;

public class AndroidPixmap implements Pixmap
{
	Canvas canvas;
	Bitmap pixmap;
	Paint paint;
	int r, g, b, a;

	public AndroidPixmap( int width, int height, Pixmap.Format format )
	{
		Bitmap.Config config = getInternalFormat( format );
		pixmap = Bitmap.createBitmap( width, height, config );
		canvas = new Canvas( pixmap );		
		paint = new Paint( );
		paint.setStyle( Style.FILL );
		
		paint.setAntiAlias(true);
		paint.setXfermode( new PorterDuffXfermode( Mode.SRC ) );
	}
	
	public AndroidPixmap(Bitmap bitmap) 
	{
		pixmap = bitmap;
		
		if( pixmap.isMutable() )
			canvas = new Canvas( pixmap );		
		paint = new Paint( );
		paint.setStyle( Style.FILL );
		paint.setAntiAlias(true);
		paint.setXfermode( new PorterDuffXfermode( Mode.SRC ) );
	}

	protected static Bitmap.Config getInternalFormat( Pixmap.Format format )
	{
		if( format == Pixmap.Format.Alpha )
			return Bitmap.Config.ALPHA_8;
		if( format == Pixmap.Format.RGBA4444 )
			return Bitmap.Config.ARGB_4444;
		return Bitmap.Config.ARGB_8888;
	}
	
	@Override
	public void drawCircle(int x, int y, int radius) 
	{
		if( canvas == null )
			return;
		paint.setStyle(Style.STROKE);
		canvas.drawCircle( x + radius, y + radius, radius, paint );
		paint.setStyle(Style.FILL);
	}

	@Override
	public void drawLine(int x, int y, int x2, int y2) 
	{	
		if( canvas == null )
			return;
		canvas.drawLine( x, y, x2, y2, paint );	
	}

	@Override
	public void drawRectangle(int x, int y, int width, int height) 
	{
		if( canvas == null )
			return;
		paint.setStyle(Style.STROKE);
		canvas.drawRect( x, y, x + width, y + height, paint );
		paint.setStyle(Style.FILL);
	}

	@Override
	public void fill() 
	{	
		if( canvas == null )
			return;
		fillRectangle(0, 0, pixmap.getWidth(), pixmap.getHeight());
	}

	@Override
	public void fillCircle(int x, int y, int radius) 
	{
		if( canvas == null )
			return;
		canvas.drawCircle( x + radius, y + radius, radius, paint );
	}

	@Override
	public void fillRectangle(int x, int y, int width, int height) 
	{
		if( canvas == null )
			return;
		canvas.drawRect( x, y, x + width, y + height, paint );
	}

	@Override
	public Object getNativePixmap() 
	{	
		return pixmap;
	}

	@Override
	public void setColor(float r, float g, float b, float a) 
	{
		this.r = (int)(255 * r);
		this.g = (int)(255 * g);
		this.b = (int)(255 * b);
		this.a = (int)(255 * a);
		
		paint.setColor( this.a << 24 | this.r << 16 | this.g << 8 | this.b );
	}

	@Override
	public void setStrokeWidth(int width) 
	{
		paint.setStrokeWidth( width );		
	}

	@Override
	public int getPixel(int x, int y) 
	{	
		if( x < 0 || x >= pixmap.getWidth() )
			return 0;
		if( y < 0 || y >= pixmap.getHeight() )
			return 0;
		return pixmap.getPixel(x, y);
	}

	@Override
	public int getHeight() 
	{
		return pixmap.getHeight();
	}

	@Override
	public int getWidth() {
		return pixmap.getWidth();
	}

	Rect src = new Rect( );
	RectF dst = new RectF( );
	@Override
	public void drawPixmap(Pixmap pixmap, int x, int y, int srcx, int srcy,
			int width, int height) 
	{
		if( canvas == null )
			return;
		
		dst.set( x, y, x + width, y + height );
		src.set( srcx, srcy, srcx + width, srcy + height );
		
		canvas.drawBitmap((Bitmap)pixmap.getNativePixmap(), src, dst, null);
		
	}

	@Override
	public void dispose() {
		pixmap.recycle();
		pixmap = null;
	}

	@Override
	public void getPixelRow(int[] pixels, int y) 
	{	
		pixmap.getPixels( pixels, 0, pixmap.getWidth(), 0, y, pixmap.getWidth(), 1 );		
	}

}

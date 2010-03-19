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
package com.badlogic.gdx.backends.android;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;



/**
 * An implementation of {@link Font} for Android. 
 * 
 * @author badlogicgames@gmail.com
 *
 */
final class AndroidFont extends Font
{
	Typeface font;
	Paint paint;
	FontMetrics metrics;

	AndroidFont(Graphics graphics, String fontName, int size, FontStyle style, boolean managed) 
	{
		super( graphics, managed );		
		font = Typeface.create( fontName, getFontStyle( style ) );
		paint = new Paint( );
		paint.setTypeface(font);
		paint.setTextSize(size);
		paint.setAntiAlias(false);
		metrics = paint.getFontMetrics();		
	}

	AndroidFont(Graphics graphics, AssetManager assets, String file, int size,	FontStyle style, boolean managed) 
	{	
		super( graphics, managed );				
		font = Typeface.createFromAsset( assets, file );		
		paint = new Paint( );
		paint.setTypeface(font);
		paint.setTextSize(size);	
		paint.setAntiAlias(false);
		metrics = paint.getFontMetrics();
	}

	private int getFontStyle( FontStyle style )
	{
		if( style == FontStyle.Bold )
			return Typeface.BOLD;
		if( style == FontStyle.BoldItalic )
			return Typeface.BOLD_ITALIC;
		if( style == FontStyle.Italic )
			return Typeface.ITALIC;
		if( style == FontStyle.Plain )
			return Typeface.NORMAL;
		
		return Typeface.NORMAL;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getGlyphAdvance(char character) {
		float[] width = new float[1];
		paint.getTextWidths( "" + character, width );
		return (int)(Math.ceil(width[0]));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pixmap getGlyphBitmap(char character) {
		Rect rect = new Rect();		
		paint.getTextBounds( "" + character, 0, 1, rect );
		Bitmap bitmap = Bitmap.createBitmap( rect.width()==0?1:rect.width() + 5, getLineHeight(), Bitmap.Config.ARGB_8888 );
		Canvas g = new Canvas( bitmap );		
		paint.setColor(0x00000000);
		paint.setStyle(Style.FILL);
		g.drawRect( new Rect( 0, 0, rect.width() + 5, getLineHeight()), paint);
		paint.setColor(0xFFFFFFFF);		
		g.drawText( "" + character, 0, -metrics.ascent, paint );		
		return new AndroidPixmap( bitmap );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLineGap() {	
		return (int)(Math.ceil(metrics.leading));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLineHeight() {	
		return (int)Math.ceil(Math.abs(metrics.ascent) + Math.abs(metrics.descent));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStringWidth(String text) 
	{		
		Rect rect = new Rect();
		paint.getTextBounds(text, 0, text.length(), rect);
		return rect.width();
	}

	Rect tmpRect = new Rect();
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void getGlyphBounds(char character, Rectangle rect) {		
		paint.getTextBounds( "" + character, 0, 1, tmpRect );
		rect.setWidth(tmpRect.width() + 5);
		rect.setHeight(getLineHeight());
	}
}

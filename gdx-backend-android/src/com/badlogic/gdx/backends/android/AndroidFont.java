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
		paint.setAntiAlias(true);
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
	Rect tmpRect = new Rect();
	@Override	
	public int getStringWidth(String text) 
	{				
		paint.getTextBounds(text, 0, text.length(), tmpRect);
		return tmpRect.width();
	}

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

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

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.opengl.GLUtils;
import android.util.Log;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * An implementation of {@link Texture} for Android
 * 
 * @author badlogicgames@gmail.com
 *
 */
final class AndroidTexture implements Texture
{	
	/** list of currently active textures used to invalidate them in case the surface was lost **/
	private static final ArrayList<AndroidTexture> textures = new ArrayList<AndroidTexture>( );
	/** the texture handle **/
	private int textureHandle;
	/** handle to gl wrapper **/
	private GL10 gl10;
	/** handle to the gl 2 wrapper **/
	private GL20 gl20;
	/** height of original image in pixels **/
	private int height;    
	/** width of original image in pixels **/
	private int width;        
	/** height in pixels of texture **/
	private int texHeight;
	/** width in pixels of texture **/
	private int texWidth;	
	/** whether this texture is managed **/
	private final boolean isManaged;
	/** the managed pixmap **/
	private Bitmap bitmap;
	/** whether this texture is mip mapped **/
	private final boolean isMipMap;
	/** the min filter **/
	private final TextureFilter minFilter;
	/** the mag filter **/
	private final TextureFilter magFilter;
	/** the u wrap **/
	private final TextureWrap uWrap;
	/** the v wrap **/
	private final TextureWrap vWrap;		
	/** invalidate flag **/
	private boolean invalidated = false;
	/** the format of this texture **/
	private Bitmap.Config format;
			
	/**
	 * Creates a new texture based on the given image
	 * 
	 * @param gl
	 * @param bitmap
	 */
	AndroidTexture( AndroidGraphics graphics, GL10 gl, Bitmap image, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap, boolean managed )
	{		
		this.isManaged = managed;
		if( isManaged )
			this.bitmap = image;
		else
			this.bitmap = null;
		this.minFilter = minFilter;
		this.magFilter = maxFilter;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.texWidth = image.getWidth();
		this.texHeight = image.getHeight();	
		this.gl10 = gl;
		this.format = image.getConfig();
			
		if( minFilter == TextureFilter.MipMap )
			isMipMap = true;
		else
			isMipMap = false;
		
		createTexture( gl );
		buildMipmap( gl, image);					
		gl.glBindTexture( GL10.GL_TEXTURE_2D, 0 );
		
		if( isManaged )
			textures.add( this );
	}
	
	AndroidTexture( AndroidGraphics graphics, GL20 gl, Bitmap image, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap, boolean managed )
	{		
		this.isManaged = managed;
		if( isManaged )
			this.bitmap = image;
		else
			this.bitmap = null;
		this.minFilter = minFilter;
		this.magFilter = maxFilter;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.texWidth = image.getWidth();
		this.texHeight = image.getHeight();	
		this.gl20 = gl;
		this.format = image.getConfig();
	
		if( minFilter == TextureFilter.MipMap )
			isMipMap = true;
		else
			isMipMap = false;
		
		createTexture( gl );        
		buildMipmap( gl, image);					
		gl.glBindTexture( GL20.GL_TEXTURE_2D, 0 );
		textures.add( this );
	}		
	
	private void rebuild( )
	{
		if( gl10 != null )
		{
			createTexture( gl10 );
			buildMipmap( gl10, bitmap );
		}
		else
		{
			createTexture( gl20 );
			buildMipmap( gl20, bitmap );
		}
		invalidated = false;
	}
	
	private void createTexture( GL10 gl )
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(4);
		buffer.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = buffer.asIntBuffer();
		gl.glGenTextures(1, intBuffer);
		textureHandle = intBuffer.get(0);		
		
		gl.glBindTexture( GL10.GL_TEXTURE_2D, textureHandle );
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, getTextureFilter( minFilter ) );
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, getTextureFilter( magFilter ) );
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, getTextureWrap( uWrap ) );
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, getTextureWrap( vWrap ) );	
	}
	
	private void createTexture( GL20 gl )
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(4);
		buffer.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = buffer.asIntBuffer();
		gl.glGenTextures(1, intBuffer);
		textureHandle = intBuffer.get(0);	
				
		gl.glBindTexture( GL20.GL_TEXTURE_2D, textureHandle );
        gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, getTextureFilter( minFilter ) );
        gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, getTextureFilter( magFilter ) );
        gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, getTextureWrap( uWrap ) );
        gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, getTextureWrap( vWrap ) );
	}	
	
	private int getTextureFilter( TextureFilter filter )
	{
		if( filter == TextureFilter.Linear )
			return GL10.GL_LINEAR;
		else
		if( filter == TextureFilter.Nearest )
			return GL10.GL_NEAREST;
		else
			return GL10.GL_LINEAR_MIPMAP_NEAREST;
	}
	
	private int getTextureWrap( TextureWrap wrap )
	{
		if( wrap == TextureWrap.ClampToEdge )
			return GL10.GL_CLAMP_TO_EDGE;
		else
			return GL10.GL_REPEAT;
	}

	private void buildMipmap(GL10 gl, Bitmap bitmap ) 
	{

		int level = 0;
		int height = bitmap.getHeight();
		int width = bitmap.getWidth();	      	       
		Log.d( "texture", "creating texture mipmaps: " + bitmap.getWidth() + ", " + bitmap.getHeight() );

		while(height >= 1 || width >= 1 && level < 4 ) {
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bitmap, 0);			
			if(height == 1 || width == 1 || isMipMap == false ) 
			{
				break;
			}

			level++;
			if( height > 1 )
				height /= 2;
			if( width > 1 )
				width /= 2;

			Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width, height, true);
			if( level > 1 )
				bitmap.recycle();
			bitmap = bitmap2;
		}		
	}

	private void buildMipmap(GL20 gl, Bitmap bitmap ) 
	{

		int level = 0;
		int height = bitmap.getHeight();
		int width = bitmap.getWidth();	      	       
		Log.d( "texture", "creating texture mipmaps: " + bitmap.getWidth() + ", " + bitmap.getHeight() );

		while(height >= 1 || width >= 1 && level < 4 ) {
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bitmap, 0);			
			if(height == 1 || width == 1 || isMipMap == false ) 
			{
				break;
			}

			level++;
			if( height > 1 )
				height /= 2;
			if( width > 1 )
				width /= 2;

			Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width, height, true);
			if( level > 1 )
				bitmap.recycle();
			bitmap = bitmap2;
		}		
	}

	public boolean isManaged( )
	{
		return isManaged;
	}

	/**
	 * {@inheritDoc}
	 */
	public void draw( Pixmap bmp, int x, int y )
	{
		if( isManaged && invalidated )
			rebuild( );		
		
		if( gl10 != null )
			gl10.glBindTexture( GL10.GL_TEXTURE_2D, textureHandle );
		else
			gl20.glBindTexture( GL10.GL_TEXTURE_2D, textureHandle );
		Bitmap bitmap = (Bitmap)bmp.getNativePixmap();
		if( bitmap.getConfig() != format )
			throw new IllegalArgumentException( "can't draw bitmap with different format: " + bitmap.getConfig() + " != " + format );
		
		if( isManaged )
		{
			Canvas canvas = new Canvas( this.bitmap );
			Rect src = new Rect( );
			RectF dst = new RectF( );
			
			dst.set( x, y, x + bitmap.getWidth(), y + bitmap.getHeight() );
			src.set( 0, 0, bitmap.getWidth(), bitmap.getHeight() );
			
			canvas.drawBitmap(bitmap, src, dst, null);	
			
			try
			{
				FileOutputStream out = new FileOutputStream( "/sdcard/texture.png" );
				bitmap.compress(CompressFormat.PNG, 100, out );
				out.close();
			}
			catch( Exception ex )
			{
				System.out.println( "no" );
			}
		}
		int level = 0;
		int height = bitmap.getHeight();
		int width = bitmap.getWidth();	      	       					
		
		while(height >= 1 || width >= 1 && level < 4 ) {
			GLUtils.texSubImage2D( GL10.GL_TEXTURE_2D, level, x, y, bitmap );			
			
			if(height == 1 || width == 1 || isMipMap == false ) 
			{
				break;
			}

			level++;
			if( height > 1 )
				height /= 2;
			if( width > 1 )
				width /= 2;

			Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width, height, true);
			if( level > 1 )
				bitmap.recycle();
			bitmap = bitmap2;
		}	
	}
	
	static Texture lastTexture = null;
	
	/**
	 * {@inheritDoc}
	 */
	public void bind(  )
	{				
		if( isManaged && invalidated )
		{
			rebuild( );
			lastTexture = null;
		}
		
		if( lastTexture != this )
		{
			lastTexture = this;
			if( gl10 != null )
				gl10.glBindTexture( GL10.GL_TEXTURE_2D, textureHandle );
			else
				gl20.glBindTexture( GL10.GL_TEXTURE_2D, textureHandle );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose( )
	{
		if( gl10 != null )
		{
			if( gl10 instanceof GL11 )
			{
				GL11 gl11 = (GL11)gl10;
				if( gl11.glIsTexture( textureHandle ) )
				{
					int[] textures = { textureHandle };
					gl10.glDeleteTextures( 1, textures, 0 );
				}
			}
			else
			{
				int[] textures = { textureHandle };
				gl10.glDeleteTextures( 1, textures, 0 );
			}
		}
		else
		{
			if( gl20.glIsTexture( textureHandle ) )
			{
				ByteBuffer buffer = ByteBuffer.allocateDirect(4);
				buffer.order(ByteOrder.nativeOrder());
				IntBuffer intBuffer = buffer.asIntBuffer();
				intBuffer.put(textureHandle);
				intBuffer.position(0);
				gl20.glDeleteTextures( 1, intBuffer);
			}
		}		
		
		textureHandle = 0;				
		if( bitmap != null )
		{
			bitmap.recycle();
			bitmap = null;
		}
		textures.remove( this );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHeight() {
		return texHeight;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getImageHeight() {
		return height;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getImageWidth() {
		return width;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWidth() {
		return texWidth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getTextureObjectHandle() 
	{
		return textureHandle;
	}
	
	public static void invalidateAllTextures( )
	{
		for( int i = 0; i < textures.size(); i++ )
		{			
			if( textures.get(i).isManaged )
			{
				AndroidTexture texture = textures.get(i);
				texture.invalidated = true;
				texture.rebuild( );				
			}
		}
		lastTexture = null;
	}
}

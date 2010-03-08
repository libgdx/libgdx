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

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

import com.badlogic.gdx.Pixmap;
import com.badlogic.gdx.Texture;
import com.badlogic.gdx.Application.TextureFilter;
import com.badlogic.gdx.Application.TextureWrap;

/**
 * An implementation of {@link Texture} for Android
 * 
 * @author mzechner
 *
 */
final class AndroidTexture implements Texture
{
	/** the texture handle **/
	private int textureHandle;
	/** handle to gl wrapper **/
	private GL10 gl;
	/** height of original image in pixels **/
	private int height;    
	/** width of original image in pixels **/
	private int width;        
	/** height in pixels of texture **/
	private int texHeight;
	/** width in pixels of texture **/
	private int texWidth;	
	
	public static int textures = 0;
	
	public boolean isMipMap = false;
	/**
	 * Creates a new texture based on the given image
	 * 
	 * @param gl
	 * @param bitmap
	 */
	AndroidTexture( GL10 gl, Bitmap image, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap )
	{
		this.gl = gl;
	
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		textureHandle = textures[0];
		
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.texWidth = image.getWidth();
		this.texHeight = image.getHeight();			
		
		gl.glBindTexture( GL10.GL_TEXTURE_2D, textureHandle );
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, getTextureFilter( minFilter ) );
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, getTextureFilter( maxFilter ) );
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, getTextureWrap( uWrap ) );
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, getTextureWrap( vWrap ) );
		
        gl.glMatrixMode( GL10.GL_TEXTURE );
        gl.glLoadIdentity();
        
		buildMipmap( gl, image);

		AndroidTexture.textures++;
		
		if( minFilter == TextureFilter.MipMap )
			isMipMap = true;
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
			if(height == 1 || width == 1 ) // || isMipMap == false ) 
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


	/**
	 * {@inheritDoc}
	 */
	public void draw( Pixmap bmp, int x, int y )
	{
		gl.glBindTexture( GL10.GL_TEXTURE_2D, textureHandle );		
		Bitmap bitmap = (Bitmap)bmp.getNativePixmap();
		int level = 0;
		int height = bitmap.getHeight();
		int width = bitmap.getWidth();	      	       		

		while(height >= 1 || width >= 1 && level < 4 ) {
			GLUtils.texSubImage2D( GL10.GL_TEXTURE_2D, level, x, y, (Bitmap)bitmap );
			
			if(height == 1 || width == 1 ) //|| isMipMap == false ) 
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
		if( lastTexture != this )
		{
			lastTexture = this;
			gl.glBindTexture( GL10.GL_TEXTURE_2D, textureHandle );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose( )
	{
		int[] textures = { textureHandle };
		gl.glDeleteTextures( 1, textures, 0 );
		textureHandle = 0;
		AndroidTexture.textures--;
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
}

package com.badlogic.gdx.backends.android;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

import com.badlogic.gdx.Texture;
import com.badlogic.gdx.Application.TextureFilter;
import com.badlogic.gdx.Application.TextureWrap;

public class AndroidTexture implements Texture
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
	protected AndroidTexture( GL10 gl, Bitmap image, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap )
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

		this.textures++;
		
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
	 * Draws the given image to the texture
	 * @param gl
	 * @param bitmap
	 * @param x
	 * @param y
	 */
	public void draw( Object bmp, int x, int y )
	{
		gl.glBindTexture( GL10.GL_TEXTURE_2D, textureHandle );		
		Bitmap bitmap = (Bitmap)bmp;
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

	/**
	 * Binds the texture
	 * @param gl
	 */
	static Texture lastTexture = null;
	public void bind(  )
	{				
		if( lastTexture != this )
		{
			lastTexture = this;
			gl.glBindTexture( GL10.GL_TEXTURE_2D, textureHandle );
		}
	}

	/**
	 * Disposes the texture and frees the associated resourcess
	 * @param gl
	 */
	public void dispose( )
	{
		int[] textures = { textureHandle };
		gl.glDeleteTextures( 1, textures, 0 );
		textureHandle = 0;
		this.textures--;
	}

	@Override
	public int getHeight() {
		return texHeight;
	}

	@Override
	public int getImageHeight() {
		return height;
	}

	@Override
	public int getImageWidth() {
		return width;
	}

	@Override
	public int getWidth() {
		return texWidth;
	}
}

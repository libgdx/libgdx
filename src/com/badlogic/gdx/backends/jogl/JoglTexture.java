package com.badlogic.gdx.backends.jogl;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.badlogic.gdx.Texture;
import com.badlogic.gdx.Application.TextureFilter;
import com.badlogic.gdx.Application.TextureWrap;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class JoglTexture implements Texture
{				   
	/** height of original image in pixels **/
	private int height;    
	/** width of original image in pixels **/
	private int width;        
	/** height in pixels of texture **/
	private int texHeight;
	/** width in pixels of texture **/
	private int texWidth;	   

	/** texture wrapper **/
	com.sun.opengl.util.texture.Texture texture;
	
	public static int textures = 0;
	
	/**
	 * Create a new texture
	 *
	 * @param textureID The GL texture ID
	 */
	protected JoglTexture(InputStream in, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap ) 
	{        
		try
		{
			BufferedImage image = ImageIO.read(in);			
			this.width = image.getWidth();
			this.height = image.getHeight();
			texture = com.sun.opengl.util.texture.TextureIO.newTexture( image, minFilter == TextureFilter.MipMap?true:false );
		}
		catch( Exception ex )
		{			
			throw new RuntimeException( ex );
		}
		GL gl = GLContext.getCurrent().getGL();
        gl.glEnable( GL.GL_TEXTURE_2D );
        bind(); 
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, getTextureFilter( minFilter ) );
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, getTextureFilter( maxFilter ) );
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, getTextureWrap( uWrap ) );
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, getTextureWrap( vWrap ) );
		this.texWidth = texture.getWidth();
		this.texHeight = texture.getHeight();
		textures++;
	}	    	
	
	protected JoglTexture(BufferedImage image, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap ) 
	{        
				
		this.width = image.getWidth();
		this.height = image.getHeight();
		texture = com.sun.opengl.util.texture.TextureIO.newTexture( image, minFilter == TextureFilter.MipMap?true:false );		
		GL gl = GLContext.getCurrent().getGL();
        gl.glEnable( GL.GL_TEXTURE_2D );
        bind(); 
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, getTextureFilter( minFilter ) );
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, getTextureFilter( maxFilter ) );
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, getTextureWrap( uWrap ) );
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, getTextureWrap( vWrap ) );
		this.texWidth = texture.getWidth();
		this.texHeight = texture.getHeight();
		textures++;
	}	
	
	/**
	 * Create a new texture
	 *
	 * @param textureID The GL texture ID
	 */
	protected JoglTexture(int width, int height, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap ) 
	{        		
		BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_4BYTE_ABGR );			
		this.width = image.getWidth();
		this.height = image.getHeight();
		texture = com.sun.opengl.util.texture.TextureIO.newTexture( image, minFilter == TextureFilter.MipMap?true:false );
		
		GL gl = GLContext.getCurrent().getGL();
        gl.glEnable( GL.GL_TEXTURE_2D );
        bind(); 
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, getTextureFilter( minFilter ) );
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, getTextureFilter( maxFilter ) );
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, getTextureWrap( uWrap ) );
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, getTextureWrap( vWrap ) );
		this.texWidth = texture.getWidth();
		this.texHeight = texture.getHeight();
		textures++;
	}

	private int getTextureFilter( TextureFilter filter )
	{
		if( filter == TextureFilter.Linear )
			return GL.GL_LINEAR;
		else
		if( filter == TextureFilter.Nearest )
			return GL.GL_NEAREST;
		else
			return GL.GL_LINEAR_MIPMAP_LINEAR;
	}
	
	private int getTextureWrap( TextureWrap wrap )
	{
		if( wrap == TextureWrap.ClampToEdge )
			return GL.GL_CLAMP_TO_EDGE;
		else
			return GL.GL_REPEAT;
	}
	
	@Override
	public void bind() 
	{				
		texture.bind();
	}

	@Override
	public void dispose() {
		texture.dispose();		    
		textures--;		    
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

	/**
	 * Draws the given bitmap to the texture at position x, y
	 * @param bitmap The bitmap
	 * @param x The x coordinate in pixels
	 * @param y The y coordinate in pixels
	 */
	public void draw( Object bitmap, int x, int y )
	{
		TextureData data = TextureIO.newTextureData((BufferedImage)bitmap, true);
		texture.bind();
		texture.updateSubImage( data, 0, x, y );		
	}
}

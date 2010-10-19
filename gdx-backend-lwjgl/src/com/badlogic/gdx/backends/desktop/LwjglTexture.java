/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.desktop;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * An implementation of {@link Texture} based on Jogl
 * 
 * @author badlogicgames@gmail.com
 * 
 */
final class LwjglTexture implements Texture {
	/** height in pixels of texture **/
	private int texHeight;
	/** width in pixels of texture **/
	private int texWidth;
	/** whether this textures i managed or not **/
	private final boolean isManaged;
	private int textureID;
	private final boolean isMipMapped;

	/** global number of textures **/
	public static int textures = 0;

	static private ByteBuffer imageBuffer;
	static private final BitmapDecoder bitmapDecoder = new BitmapDecoder();
	
	/**
	 * Create a new texture
	 */
	LwjglTexture (FileHandle file, InputStream in, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap,
		TextureWrap vWrap, boolean managed) {
		this.isManaged = managed;
		this.isMipMapped = minFilter == TextureFilter.MipMap;
		BufferedImage image = (BufferedImage)Gdx.graphics.newPixmap( file ).getNativePixmap();
		loadMipMap( image );
		bind();
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, getTextureFilter(maxFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));
		textures++;
	}

	LwjglTexture (BufferedImage image, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap,
		boolean managed) {
		this.isManaged = managed;
		this.isMipMapped = minFilter == TextureFilter.MipMap;
		loadMipMap(image);
		bind();
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, getTextureFilter(maxFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));
		textures++;
	}

	/**
	 * Create a new texture
	 */
	LwjglTexture (int width, int height, int format, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap,
		TextureWrap vWrap, boolean managed) {
		this.isManaged = managed;
		this.isMipMapped = minFilter == TextureFilter.MipMap;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		loadMipMap(image);
		bind();
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, getTextureFilter(maxFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));
		textures++;
	}

	private ByteBuffer toByteBuffer( BufferedImage image )
	{
		try {
			imageBuffer = bitmapDecoder.decode(image, imageBuffer);
			return imageBuffer;
		} catch (IOException e) {
			throw new GdxRuntimeException( "couldn't decode image" );
		}
	}
	
	private BufferedImage scaleDown( BufferedImage image )
	{
		BufferedImage scaled = new BufferedImage( image.getWidth() / 2, image.getHeight() / 2, BufferedImage.TYPE_4BYTE_ABGR );
		Graphics2D g = scaled.createGraphics();
		g.drawImage( image, 0, 0, scaled.getWidth(), scaled.getHeight(), null ); //FIXME replace with something that looks actually like a scaled image...
		g.dispose();
		return scaled;
	}
	
	private void loadMipMap( BufferedImage image )
	{
		int level = 0;
		int height = image.getHeight();
		int width = image.getWidth();
		texWidth = width;
		texHeight = height;
		textureID = GL11.glGenTextures();
		GL11.glBindTexture( GL11.GL_TEXTURE_2D, textureID );

		while(height >= 1 || width >= 1 && level < 4 ) {
			ByteBuffer imageBuffer = toByteBuffer( image );
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, GL11.GL_RGBA8, bitmapDecoder.width, bitmapDecoder.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageBuffer);			
			if(height == 1 || width == 1 || isMipMapped == false ) 
			{
				break;
			}

			level++;
			if( height > 1 )
				height /= 2;
			if( width > 1 )
				width /= 2;

			image = scaleDown( image );	
		}		
	}
	
	private int getTextureFilter (TextureFilter filter) {
		if (filter == TextureFilter.Linear)
			return GL11.GL_LINEAR;
		else if (filter == TextureFilter.Nearest)
			return GL11.GL_NEAREST;
		else
			return GL11.GL_LINEAR_MIPMAP_LINEAR;
	}

	private int getTextureWrap (TextureWrap wrap) {
		if (wrap == TextureWrap.ClampToEdge)
			return GL11.GL_CLAMP;
		else
			return GL11.GL_REPEAT;
	}

	public boolean isManaged () {
		return isManaged;
	}

	public void bind () {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	}

	public void dispose () {
		GL11.glDeleteTextures(textureID);
		textures--;
	}

	public int getHeight () {
		return texHeight;
	}

	public int getWidth () {
		return texWidth;
	}

	public void draw (Pixmap pixmap, int x, int y) {
		if (isManaged) throw new GdxRuntimeException("Can't draw to a managed texture");
		BufferedImage image = (BufferedImage)pixmap.getNativePixmap();
		
		int level = 0;
		int height = image.getHeight();
		int width = image.getWidth();
		bind();
		while(height >= 1 || width >= 1 && level < 4 ) {
			ByteBuffer imageBuffer = toByteBuffer( image );
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, x, y, bitmapDecoder.width, bitmapDecoder.height, GL11.GL_RGBA,
					GL11.GL_UNSIGNED_BYTE, imageBuffer);			
			if(height == 1 || width == 1 || isMipMapped == false ) 
			{
				break;
			}

			level++;
			if( height > 1 )
				height /= 2;
			if( width > 1 )
				width /= 2;

			image = scaleDown( image );	
		}	
		

	}

	public int getTextureObjectHandle () {
		return textureID;
	}
}

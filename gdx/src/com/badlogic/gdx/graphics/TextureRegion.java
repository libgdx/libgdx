package com.badlogic.gdx.graphics;

/**
 * A TextureRegion defines a rectangular area in a texture
 * given in pixels.
 * 
 * @author mzechner
 *
 */
public class TextureRegion 
{
	public int x, y;
	public int width, height;
	public Texture texture;
	
	public TextureRegion( Texture texture, int x, int y, int width, int height )
	{
		set( texture, x, y, width, height );
	}

	public void set(Texture texture, int x, int y, int width, int height) 
	{
		this.texture = texture;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}

package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;

/** @brief Layer for a TiledMap that's just a single image */
public class TiledMapImageLayer extends MapLayer
{
	
	private float x, y;
	
	private TextureRegion image;
	
	public TiledMapImageLayer(float x, float y, TextureRegion tex)
	{
		this.x = x;
		this.y = y;
		this.image = tex;
	}
	
	public TextureRegion getImage()
	{
		return this.image;
	}
	
	public float getX()
	{
		return x;
	}
	
	public float getY()
	{
		return y;
	}
	
	public int getWidth()
	{
		return image.getRegionWidth();
	}
	
	public int getHeight()
	{
		return image.getRegionHeight();
	}

}

package com.badlogic.gdx.graphics;

import java.util.HashMap;
import java.util.Map;

/**
 * A TextureAtlas is composed of a single {@link Texture} and
 * named {@link TextureRegion}s. You can add new regions at any
 * time either programmatically or by reading them in from a file.
 * 
 * @author mzechner
 *
 */
public class TextureAtlas 
{
	private final Map<String, TextureRegion> regions = new HashMap<String, TextureRegion>( );
	private final Texture texture;
	
	/**
	 * Constructs a new texture atlas with the given texture.
	 * Use {@link #addRegion()} or one of the {@link #load()} methods
	 * to add {@link TextureRegions}
	 * 
	 * @param texture the texture
	 */
	public TextureAtlas( Texture texture )
	{
		this.texture = texture;
	}
	
	/**
	 * Returns the {@link TextureRegion} with name regionName or null
	 * in case the region is undefined.
	 * 
	 * @param regionName the region name
	 * @return the region or null.
	 */
	public TextureRegion getRegion( String regionName )
	{
		return regions.get(regionName);
	}
	
	/**
	 * Adds the {@link TextureRegion} with name regionName
	 * to this atlas. If a region with the given name was
	 * present already it will be overwritten.
	 * 
	 * @param regionName the region name
	 * @param region the region
	 */
	public void addRegion( String regionName, TextureRegion region )
	{
		regions.put( regionName, region );
	}
	
	/**
	 * Adds a new {@link TextureRegion} with name regionName to this
	 * atlas. If a region with the given name was already present it
	 * is overwritten.
	 * 
	 * @param regionName the region name
	 * @param x the x coordinate of the region in pixels 
	 * @param y the y coordinate of the region in pixels
	 * @param width the width of the region in pixels
	 * @param height the height of the region in pixels
	 */
	public void addRegion( String regionName, int x, int y, int width, int height )
	{
		regions.put( regionName, new TextureRegion( texture, x, y, width, height ) );
	}
	
	/**
	 * @return the {@link Texture} of this atlas
	 */
	public Texture getTexture( )
	{
		return texture;
	}
}

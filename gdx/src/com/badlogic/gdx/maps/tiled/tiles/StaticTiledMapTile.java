package com.badlogic.gdx.maps.tiled.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;

public class StaticTiledMapTile implements TiledMapTile {

	private MapProperties properties;
	
	private TextureRegion textureRegion;	

	@Override
	public MapProperties getProperties() {
		return properties;
	}
	
	@Override
	public TextureRegion getTextureRegion() {
		return textureRegion;
	}
	
	public StaticTiledMapTile(TextureRegion textureRegion) {
		this.textureRegion = textureRegion;
	}
	
	public StaticTiledMapTile(StaticTiledMapTile copy) {
		this.properties.putAll(copy.properties);
		this.textureRegion = copy.textureRegion;
	}
	
}

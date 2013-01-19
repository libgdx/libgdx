package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;

public interface TiledMapTile {

	public TextureRegion getTextureRegion();

	public MapProperties getProperties();
	
}

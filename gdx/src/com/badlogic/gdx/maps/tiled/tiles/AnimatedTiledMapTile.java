package com.badlogic.gdx.maps.tiled.tiles;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.utils.Array;

public class AnimatedTiledMapTile implements TiledMapTile {

	private Array<StaticTiledMapTile> frameTiles;
	
	private float animationTime;
	
	@Override
	public BlendMode getBlendMode () {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBlendMode (BlendMode blendMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TextureRegion getTextureRegion () {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapProperties getProperties () {
		// TODO Auto-generated method stub
		return null;
	}

	public AnimatedTiledMapTile(float interval, Array<StaticTiledMapTile> frameTiles) {

	}
	
	public void setAnimationTime(float animationTime) {
		this.animationTime = animationTime;
	}
	
}

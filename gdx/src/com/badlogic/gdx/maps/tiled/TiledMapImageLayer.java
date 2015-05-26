package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;

public class TiledMapImageLayer extends MapLayer {
	private int x;
	private int y;
	private TextureRegion region;
	
	public TiledMapImageLayer(TextureRegion region, int x, int y) {
		this.region = region;
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setX(int x){
		this.x=x
	}
	
	public void setY(int y){
		this.y=y
	}
	
	public TextureRegion getTextureRegion () {
		return region;
	}
}

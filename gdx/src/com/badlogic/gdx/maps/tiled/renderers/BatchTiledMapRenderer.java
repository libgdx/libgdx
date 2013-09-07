package com.badlogic.gdx.maps.tiled.renderers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

public abstract class BatchTiledMapRenderer implements TiledMapRenderer, Disposable {
	
	protected TiledMap map;

	protected float unitScale;
	
	protected SpriteBatch spriteBatch;
	
	protected Rectangle viewBounds; 

	protected boolean ownsSpriteBatch;
	
	public TiledMap getMap() {
		return map;			
	}
	
	public void setMap(TiledMap map) {
		this.map = map;
	}
	
	public float getUnitScale() {
		return unitScale;
	}
	
	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}

	public Rectangle getViewBounds() {
		return viewBounds;
	}
	
	public BatchTiledMapRenderer(TiledMap map) {
		this(map, 1.0f);
	}
	
	public BatchTiledMapRenderer(TiledMap map, float unitScale) {
		this.map = map;
		this.unitScale = unitScale;
		this.viewBounds = new Rectangle();
		this.spriteBatch = new SpriteBatch();
		this.ownsSpriteBatch = true;
	}
	
	public BatchTiledMapRenderer(TiledMap map, SpriteBatch spriteBatch) {
		this(map, 1.0f, spriteBatch);		
	}
	
	public BatchTiledMapRenderer(TiledMap map, float unitScale, SpriteBatch spriteBatch) {
		this.map = map;
		this.unitScale = unitScale;
		this.viewBounds = new Rectangle();
		this.spriteBatch = spriteBatch;
		this.ownsSpriteBatch = false;
	}
	
	@Override
	public void setView(OrthographicCamera camera) {
		spriteBatch.setProjectionMatrix(camera.combined);
		float width = camera.viewportWidth * camera.zoom;
		float height = camera.viewportHeight * camera.zoom;
		viewBounds.set(camera.position.x - width / 2, camera.position.y - height / 2, width, height);
	}
	
	@Override
	public void setView (Matrix4 projection, float x, float y, float width, float height) {
		spriteBatch.setProjectionMatrix(projection);
		viewBounds.set(x, y, width, height);
	}
	
	@Override
	public void render () {
		AnimatedTiledMapTile.updateAnimationBaseTime();
		spriteBatch.begin();
		for (MapLayer layer : map.getLayers()) {
			if (layer.isVisible()) {
				if (layer instanceof TiledMapTileLayer) {
					renderTileLayer((TiledMapTileLayer) layer);
				} else {
					for (MapObject object : layer.getObjects()) {
						renderObject(object);
					}
				}					
			}				
		}
		spriteBatch.end();
	}
	
	@Override
	public void render (int[] layers) {
		AnimatedTiledMapTile.updateAnimationBaseTime();
		spriteBatch.begin();
		for (int layerIdx : layers) {
			MapLayer layer = map.getLayers().get(layerIdx);
			if (layer.isVisible()) {
				if (layer instanceof TiledMapTileLayer) {
					renderTileLayer((TiledMapTileLayer) layer);
				} else {
					for (MapObject object : layer.getObjects()) {
						renderObject(object);
					}
				}					
			}				
		}		
		spriteBatch.end();
	}

	@Override
	public void dispose () {
		if (ownsSpriteBatch) {
			spriteBatch.dispose();	
		}		
	}
	
}
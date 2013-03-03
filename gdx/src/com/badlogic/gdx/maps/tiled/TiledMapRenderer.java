package com.badlogic.gdx.maps.tiled;

import static com.badlogic.gdx.graphics.g2d.SpriteBatch.*;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.math.Polygon;

public interface TiledMapRenderer extends MapRenderer {
	public void renderObject(MapObject object);
	public void renderTileLayer(TiledMapTileLayer layer);
}
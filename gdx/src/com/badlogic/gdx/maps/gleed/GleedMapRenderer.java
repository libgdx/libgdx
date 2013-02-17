/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.maps.gleed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author David Saltares Márquez
 * 
 * @brief Renderer for GLEED maps
 */
public class GleedMapRenderer implements MapRenderer, Disposable {
	private Map map;
	private SpriteBatch batch;
	private boolean ownSpriteBatch;
	private float units;
	
	private Rectangle box = new Rectangle();
	private Rectangle viewBounds = new Rectangle();
	private Vector2 a = new Vector2();
	private Vector2 b = new Vector2();
	private Vector2 c = new Vector2();
	private Vector2 d = new Vector2();
	
	/**
	 * @param map map data that will be used to render
	 * 
	 * Uses its own SpriteBatch
	 */
	public GleedMapRenderer(Map map) {
		this(map, new SpriteBatch(), 1.0f);
		ownSpriteBatch = true;
	}
	
	/**
	 * @param map map data that will be used to render
	 * @param batch sprite batch to render the map textures
	 * @param units metres per pixel (used to scale textures, defaults to 1.0f)
	 */
	public GleedMapRenderer(Map map, SpriteBatch batch, float units) {
		this.map = map;
		this.units = units;
		if (batch != null) {
			this.batch = batch;
			ownSpriteBatch = false;
		}
		else
		{
			batch = new SpriteBatch();
			ownSpriteBatch = true;
		}
	}
	
	@Override
	public void setView(OrthographicCamera camera) {
		batch.setProjectionMatrix(camera.combined);
		float width = camera.viewportWidth * camera.zoom;
		float height = camera.viewportHeight * camera.zoom;
		viewBounds.set(camera.position.x - width / 2, camera.position.y - height / 2, width, height);
	}
	
	@Override
	public void setView (Matrix4 projection, float x, float y, float width, float height) {
		batch.setProjectionMatrix(projection);
		viewBounds.set(x, y, width, height);
	}
	
	@Override
	public void render () {
		batch.begin();
		batch.enableBlending();
		MapLayers layers = map.getLayers();
		for (MapLayer layer : layers) {
			renderLayer(layer);
		}
		batch.end();
	}

	@Override
	public void render (int[] layers) {
		batch.begin();
		batch.enableBlending();
		MapLayers mapLayers = map.getLayers();
		
		for (int i = 0; i < layers.length; i++) {
			renderLayer(mapLayers.getLayer(i));
		}
		batch.end();
	}
	
	@Override
	public void dispose () {
		if (ownSpriteBatch) {
			batch.dispose();
		}
	}
	
	private void renderLayer(MapLayer layer) {
		
		if (!layer.getVisible()) {
			return;
		}
		
		MapObjects objects = layer.getObjects();
		int numObjects = objects.getNumObjects();
		
		for (int j = 0; j < numObjects; ++j) {
			MapObject mapObject = objects.getObject(j);
			
			if (mapObject == null || !mapObject.getVisible()) {
				continue;
			}
			
			if (!(mapObject instanceof TextureMapObject)) {
				continue;
			}
			
			TextureMapObject texture = (TextureMapObject)mapObject;
			
			setBounds(texture);

			// If the image is in the frustum, draw it (culling)
			if (viewBounds.overlaps(box) ||
				 viewBounds.contains(box) ||
				 box.contains(viewBounds)) {
			
				// Skip complex rendering if there is no scaling nor rotation
				if (texture.getRotation() == 0 &&
					 texture.getScaleX() == 1.0f &&
					 texture.getScaleY() == 1.0f) {
					
					batch.draw(texture.getTextureRegion(),
									 texture.getX() * units - texture.getOriginX(),
									 texture.getY() * units - texture.getOriginY());
				}
				else {
					batch.draw(texture.getTextureRegion(),
									 texture.getX() * units - texture.getOriginX(),
									 texture.getY() * units - texture.getOriginY(),
									 texture.getOriginX(),
									 texture.getOriginY(),
									 texture.getTextureRegion().getRegionWidth(),
									 texture.getTextureRegion().getRegionHeight(),
									 texture.getScaleX() * units,
									 texture.getScaleY() * units,
									 -MathUtils.radiansToDegrees * texture.getRotation());
				}
			}
		}
	}

	private void setBounds(TextureMapObject texture) {
		TextureRegion region = texture.getTextureRegion();
		
		float x1 = -region.getRegionWidth() * 0.5f * units * texture.getScaleX();
		float x2 = -x1;
		float y1 = -region.getRegionHeight() * 0.5f * units * texture.getScaleY();
		float y2 = -y1;
		
		float rotation = texture.getRotation();
		
		// Skip sin and cos calculations if we don't have rotation
		if (rotation == 0.0f) {
			a.x = x1;
			a.y = y1;
			
			b.x = x2;
			b.y = y1;
			
			c.x = x2;
			c.y = y2;
			
			d.x = x1;
			d.y = y2;
		}
		else {
			float sin = (float)Math.sin(rotation);
			float cos = (float)Math.cos(rotation);
			
			a.x = x1 * cos - y1 * sin;
			a.y = x1 * sin - y1 * cos;
			
			b.x = x2 * cos - y1 * sin;
			b.y = x2 * sin - y1 * cos;
			
			c.x = x2 * cos - y2 * sin;
			c.y = x2 * sin - y2 * cos;
			
			d.x = x1 * cos - y2 * sin;
			d.y = x1 * sin - y2 * cos;
		}
		
		
		float posX = texture.getX();
		float posY = texture.getY();
		
		a.x += posX * units;
		a.y += posY * units;
		b.x += posX * units;
		b.y += posY * units;
		c.x += posX * units;
		c.y += posY * units;
		d.x += posX * units;
		d.y += posY * units;
		
		float minX = Math.min(Math.min(Math.min(a.x, b.x), c.x), d.x);
		float minY = Math.min(Math.min(Math.min(a.y, b.y), c.y), d.y);
		float maxX = Math.max(Math.max(Math.max(a.x, b.x), c.x), d.x);
		float maxY = Math.max(Math.max(Math.max(a.y, b.y), c.y), d.y);
		
		box.x = minX;
		box.y = minY;
		box.width = maxX - minX;
		box.height = maxY - minY;
	}
}

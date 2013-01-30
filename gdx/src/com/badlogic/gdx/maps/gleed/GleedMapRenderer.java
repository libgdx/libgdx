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

	// Renderer data
	private GleedMap m_map;
	private SpriteBatch m_batch;
	private boolean m_ownSpriteBatch;
	private float m_units;
	
	// Aux data for fustrum culling
	private Rectangle m_box = new Rectangle();
	private Rectangle m_cameraRectangle = new Rectangle();
	private Vector2 m_a = new Vector2();
	private Vector2 m_b = new Vector2();
	private Vector2 m_c = new Vector2();
	private Vector2 m_d = new Vector2();
	
	/**
	 * @param map map data that will be used to render
	 */
	public GleedMapRenderer(GleedMap map) {
		this(map, new SpriteBatch(), 1.0f);
		m_ownSpriteBatch = true;
	}
	
	/**
	 * @param map map data that will be used to render
	 * @param batch sprite batch to render the map textures
	 * @param units metres per pixel (used to scale textures, defaults to 1.0f)
	 */
	public GleedMapRenderer(GleedMap map, SpriteBatch batch, float units) {
		m_map = map;
		m_units = units;
		if (batch != null) {
			m_batch = batch;
			m_ownSpriteBatch = false;
		}
		else
		{
			m_batch = new SpriteBatch();
			m_ownSpriteBatch = true;
		}
	}
	
	/**
	 * @param projectionMatrix sets the projection matrix to the map's batch
	 */
	@Override
	public void setProjectionMatrix (Matrix4 projectionMatrix) {
		m_batch.setProjectionMatrix(projectionMatrix);
	}

	/**
	 * Gets the rendering process ready
	 */
	@Override
	public void begin () {
		m_batch.begin();
		m_batch.enableBlending();
	}

	/**
	 * Finishes the rendering process for that frame
	 */
	@Override
	public void end () {
		m_batch.end();
	}
	
	/**
	 * Renders all the map layers
	 * 
	 * @param camera 2D camera used to render
	 */
	public void render(OrthographicCamera camera) {
		m_cameraRectangle.x = camera.position.x - camera.viewportWidth * 0.5f * camera.zoom;
		m_cameraRectangle.y = camera.position.y - camera.viewportHeight * 0.5f * camera.zoom;
		m_cameraRectangle.width = camera.viewportWidth *  camera.zoom;
		m_cameraRectangle.height = camera.viewportHeight *  camera.zoom;
		
		setProjectionMatrix(camera.combined);
		
		MapLayers layers = m_map.getLayers();
		
		for (MapLayer layer : layers) {
			renderLayer(layer);
		}
	}
	
	/**
	 * @param camera camera 2D camera used to render
	 * @param layers layers indices to be rendered
	 */
	public void render(OrthographicCamera camera, int[] layers) {
		m_cameraRectangle.x = camera.position.x - camera.viewportWidth * 0.5f * camera.zoom;
		m_cameraRectangle.y = camera.position.y - camera.viewportHeight * 0.5f * camera.zoom;
		m_cameraRectangle.width = camera.viewportWidth *  camera.zoom;
		m_cameraRectangle.height = camera.viewportHeight *  camera.zoom;

		setProjectionMatrix(camera.combined);
		
		MapLayers mapLayers = m_map.getLayers();
		
		for (int i = 0; i < layers.length; i++) {
			renderLayer(mapLayers.getLayer(i));
		}
	}
	
	/**
	 * Renders all the layers
	 * 
	 * @param viewboundsX bottom left x coordinate of the frustum
	 * @param viewboundsY bottom left y coordinate of the frustrum
	 * @param viewboundsWidth frustum width 
	 * @param viewboundsHeight frustum height
	 */
	@Override
	public void render (float viewboundsX, float viewboundsY, float viewboundsWidth, float viewboundsHeight) {
		m_cameraRectangle.x = viewboundsX;
		m_cameraRectangle.y = viewboundsY;
		m_cameraRectangle.width = viewboundsWidth;
		m_cameraRectangle.height = viewboundsHeight;
		
		MapLayers layers = m_map.getLayers();
		
		for (MapLayer layer : layers) {
			renderLayer(layer);
		}
	}

	/**
	 * @param viewboundsX bottom left x coordinate of the frustum
	 * @param viewboundsY bottom left y coordinate of the frustrum
	 * @param viewboundsWidth frustum width 
	 * @param viewboundsHeight frustum height
	 * @param layers layers indices to be rendered
	 */
	@Override
	public void render (float viewboundsX, float viewboundsY, float viewboundsWidth, float viewboundsHeight, int[] layers) {
		m_cameraRectangle.x = viewboundsX;
		m_cameraRectangle.y = viewboundsY;
		m_cameraRectangle.width = viewboundsWidth;
		m_cameraRectangle.height = viewboundsHeight;
		
		MapLayers mapLayers = m_map.getLayers();
		
		for (int i = 0; i < layers.length; i++) {
			renderLayer(mapLayers.getLayer(i));
		}
	}
	
	/**
	 * Disposes the sprite batch in case the renderer owns it
	 */
	@Override
	public void dispose () {
		if (m_ownSpriteBatch) {
			m_batch.dispose();
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
			if (m_cameraRectangle.overlaps(m_box) ||
				 m_cameraRectangle.contains(m_box) ||
				 m_box.contains(m_cameraRectangle)) {
			
				// Skip complex rendering if there is no scaling nor rotation
				if (texture.getRotation() == 0 &&
					 texture.getScaleX() == 1.0f &&
					 texture.getScaleY() == 1.0f) {
					
					m_batch.draw(texture.getTextureRegion(),
									 texture.getX() * m_units - texture.getOriginX(),
									 texture.getY() * m_units - texture.getOriginY());
				}
				else {
					m_batch.draw(texture.getTextureRegion(),
									 texture.getX() * m_units - texture.getOriginX(),
									 texture.getY() * m_units - texture.getOriginY(),
									 texture.getOriginX(),
									 texture.getOriginY(),
									 texture.getTextureRegion().getRegionWidth(),
									 texture.getTextureRegion().getRegionHeight(),
									 texture.getScaleX() * m_units,
									 texture.getScaleY() * m_units,
									 -MathUtils.radiansToDegrees * texture.getRotation());
				}
			}
		}
	}

	private void setBounds(TextureMapObject texture) {
		TextureRegion region = texture.getTextureRegion();
		
		float x1 = -region.getRegionWidth() * 0.5f * m_units * texture.getScaleX();
		float x2 = -x1;
		float y1 = -region.getRegionHeight() * 0.5f * m_units * texture.getScaleY();
		float y2 = -y1;
		
		float rotation = texture.getRotation();
		
		// Skip sin and cos calculations if we don't have rotation
		if (rotation == 0.0f) {
			m_a.x = x1;
			m_a.y = y1;
			
			m_b.x = x2;
			m_b.y = y1;
			
			m_c.x = x2;
			m_c.y = y2;
			
			m_d.x = x1;
			m_d.y = y2;
		}
		else {
			float sin = (float)Math.sin(rotation);
			float cos = (float)Math.cos(rotation);
			
			m_a.x = x1 * cos - y1 * sin;
			m_a.y = x1 * sin - y1 * cos;
			
			m_b.x = x2 * cos - y1 * sin;
			m_b.y = x2 * sin - y1 * cos;
			
			m_c.x = x2 * cos - y2 * sin;
			m_c.y = x2 * sin - y2 * cos;
			
			m_d.x = x1 * cos - y2 * sin;
			m_d.y = x1 * sin - y2 * cos;
		}
		
		
		float posX = texture.getX();
		float posY = texture.getY();
		
		m_a.x += posX * m_units;
		m_a.y += posY * m_units;
		m_b.x += posX * m_units;
		m_b.y += posY * m_units;
		m_c.x += posX * m_units;
		m_c.y += posY * m_units;
		m_d.x += posX * m_units;
		m_d.y += posY * m_units;
		
		float minX = Math.min(Math.min(Math.min(m_a.x, m_b.x), m_c.x), m_d.x);
		float minY = Math.min(Math.min(Math.min(m_a.y, m_b.y), m_c.y), m_d.y);
		float maxX = Math.max(Math.max(Math.max(m_a.x, m_b.x), m_c.x), m_d.x);
		float maxY = Math.max(Math.max(Math.max(m_a.y, m_b.y), m_c.y), m_d.y);
		
		m_box.x = minX;
		m_box.y = minY;
		m_box.width = maxX - minX;
		m_box.height = maxY - minY;
	}
}

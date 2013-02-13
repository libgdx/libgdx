package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * @brief Logic for rendering TiledMap objects
 * 
 * Includes several optimisations such as SpriteCache usage, fustrum culling etc.
 */
public class TiledMapRenderer implements MapRenderer, Disposable {

	private Map map;
	
	private SpriteBatch spriteBatch;
	
	private float unitScale = 1;
	
	private boolean ownsSpriteBatch = false;
	
	/**
	 * @return map currently being used for rendering
	 */
	public Map getMap() {
		return map;
	}
	
	/**
	 * @return batch used for rendering
	 */
	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}
	
	/**
	 * @return world units per pixel used for rendering
	 */
	public float getUnitScale() {
		return unitScale;
	}
	
	/**
	 * Creates a renderer from a map. Will use own spritebatch and world units of 1.0 pixels
	 * 
	 * @param map will use this map for rendering 
	 */
	public TiledMapRenderer(TiledMap map) {
		this(map, new SpriteBatch());
		ownsSpriteBatch = true;
	}
	
	/**
	 * Creates a renderer from a map using the given world units. Will use a batch from its own.
	 * 
	 * @param map will use this map for rendering
	 * @param unitScale world units per pixel
	 */
	public TiledMapRenderer(TiledMap map, float unitScale) {
		this(map, new SpriteBatch(), unitScale);
		ownsSpriteBatch = true;
	}
	
	/**
	 * @param map will use this map for rendering
	 * @param spriteBatch batch that will be used for rendering
	 */
	public TiledMapRenderer(TiledMap map, SpriteBatch spriteBatch) {
		this.map = map;
		this.spriteBatch = spriteBatch;
		this.ownsSpriteBatch = false;
	}
	
	/**
	 * @param map will use this map for rendering
	 * @param spriteBatch batch that will be used for rendering
	 * @param unitScale world units per pixel
	 */
	public TiledMapRenderer(TiledMap map, SpriteBatch spriteBatch, float unitScale) {
		this.map = map;
		this.spriteBatch = spriteBatch;
		this.unitScale = unitScale;
		this.ownsSpriteBatch = false;
	}

	/**
	 * @param projection projection matrix that will be used for rendering the map
	 */
	@Override
	public void setProjectionMatrix (Matrix4 projection) {
		spriteBatch.setProjectionMatrix(projection);
	}

	/** Sets up the SpriteBatch for drawing.
	 * 
	 * @see com.badlogic.gdx.maps.MapRenderer#begin()
	 */
	public void begin() {
		spriteBatch.begin();
	}
	
	public void end() {
		spriteBatch.end();
	}
	
	/**Convenience method. Calculates the view bounds from the provided OrthographicCamera.
	 * 
	 * @param camera The OrthographicCamera from which to calculate the view bounds.
	 */
	public void render(OrthographicCamera camera) {
		
		final float camTop = camera.position.y + camera.viewportHeight / 2 * camera.zoom;
		final float camLeft = camera.position.x - camera.viewportWidth / 2 * camera.zoom;
		final float camRight = camera.position.x + camera.viewportWidth / 2 * camera.zoom;
		final float camBottom = camera.position.y - camera.viewportHeight / 2 * camera.zoom;

		for (MapLayer layer : map.getLayers()) {
			renderLayer(camLeft, camTop, camRight, camBottom, layer);
		}
	}

	/**Convenience method. Calculates the view bounds from the provided OrthographicCamera.
	 * 
	 * @param camera The OrthographicCamera from which to calculate the view bounds.
	 * @param layers The indices of the layers of the map to draw.
	 */
	public void render(OrthographicCamera camera, int[] layers) {
		final float camTop = camera.position.y + camera.viewportHeight / 2 * camera.zoom;
		final float camLeft = camera.position.x - camera.viewportWidth / 2 * camera.zoom;
		final float camRight = camera.position.x + camera.viewportWidth / 2 * camera.zoom;
		final float camBottom = camera.position.y - camera.viewportHeight / 2 * camera.zoom;

		MapLayers mapLayers = map.getLayers();
		for (int i = 0; i < layers.length; i++) {
			renderLayer(camLeft, camTop, camRight, camBottom, mapLayers.getLayer(i));
		}
	}
	
	/**
	 * Renders all the layers using the projection matrix and the given bounds for fustrum culling
	 * 
	 * @param viewboundsX
	 * @param viewboundsY
	 * @param viewboundsWidth
	 * @param viewboundsHeight
	 */
	@Override
	public void render (float viewboundsX, float viewboundsY, float viewboundsWidth, float viewboundsHeight) {
		MapLayers mapLayers = map.getLayers();
		for (MapLayer layer : mapLayers) {
			renderLayer(viewboundsX, viewboundsY, viewboundsX + viewboundsHeight, viewboundsY + viewboundsHeight, layer);
		}
	}
	
	/**
	 * Renders the given layers using the projection matrix and the given bounds for fustrum culling
	 * 
	 * @param viewboundsX
	 * @param viewboundsY
	 * @param viewboundsWidth
	 * @param viewboundsHeight
	 * @param layers
	 */
	@Override
	public void render (float viewboundsX, float viewboundsY, float viewboundsWidth, float viewboundsHeight, int[] layers) {
		MapLayers mapLayers = map.getLayers();
		for (int i = 0; i < layers.length; i++) {
			renderLayer(viewboundsX, viewboundsY, viewboundsX + viewboundsHeight, viewboundsY + viewboundsHeight, mapLayers.getLayer(i));
		}
	}
	
	protected void renderLayer(float camLeft, float camTop, float camRight, float camBottom, MapLayer layer) {
		if (layer.getVisible()) {
			spriteBatch.setColor(1, 1, 1, layer.getOpacity());
			
			if (layer instanceof TiledMapTileLayer) {
				final TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
				
				final int layerWidth = tileLayer.getWidth();
				final int layerHeight = tileLayer.getHeight();
				
				final float layerTileWidth = tileLayer.getTileWidth() * unitScale;
				final float layerTileHeight = tileLayer.getTileHeight() * unitScale;
				
				final int x1 = Math.max(0, (int) (camLeft / layerTileWidth));
				final int x2 = Math.min(layerWidth, (int) ((camRight + layerTileWidth) / layerTileWidth));

				final int y1 = Math.max(0, (int) (camBottom / layerTileHeight));
				final int y2 = Math.min(layerHeight, (int) ((camTop + layerTileHeight) / layerTileHeight));				
				
				for (int x = x1; x < x2; x++) {
					for (int y = y1; y < y2; y++) {
						final TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
						final TiledMapTile tile = cell.getTile();
						if (tile != null) {
							
							final boolean flipX = cell.getFlipHorizontally();
							final boolean flipY = cell.getFlipVertically();
							final float rotate = cell.getRotation();
							
							TextureRegion region = tile.getTextureRegion();
							float drawX = x * layerTileWidth;
							float drawY = y * layerTileHeight;
							float width = region.getRegionWidth() * unitScale;
							float height = region.getRegionHeight() * unitScale;
							float originX = width * 0.5f;
							float originY = height * 0.5f;
							float scaleX = 1;
							float scaleY = 1;
							float rotation = 0;
							int sourceX = region.getRegionX();
							int sourceY = region.getRegionY();
							int sourceWidth = region.getRegionWidth();
							int sourceHeight = region.getRegionHeight();

							if (flipX) {
								sourceX += sourceWidth;
								sourceWidth = -sourceWidth;
							}
							if (flipY) {
								sourceY += sourceHeight;
								sourceHeight = -sourceHeight;
							}
							if (rotation == 0) {
								spriteBatch.draw(
									region.getTexture(),
									drawX,
									drawY,
									width,
									height,
									sourceX,
									sourceY,
									sourceWidth,
									sourceHeight,
									false,
									false
								);
							} else {
								spriteBatch.draw(
									region.getTexture(),
									drawX,
									drawY,
									originX,
									originY,
									width,
									height,
									scaleX,
									scaleY,
									rotate,
									sourceX,
									sourceY,
									sourceWidth,
									sourceHeight,
									false,
									false
								);
							}
							
							
						}
					}
				}				
			}
		}		
	}

	@Override
	public void dispose() {
		if (ownsSpriteBatch) {
			spriteBatch.dispose();	
		}
	}
	
}

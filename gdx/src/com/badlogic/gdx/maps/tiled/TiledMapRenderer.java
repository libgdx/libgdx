package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

import static com.badlogic.gdx.graphics.g2d.SpriteBatch.*;

public class TiledMapRenderer implements MapRenderer, Disposable {

	private Map map;
	
	private SpriteBatch spriteBatch;
	
	private float unitScale = 1;
	
	private boolean ownsSpriteBatch = false;
	
	public Map getMap() {
		return map;
	}
	
	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}
	
	public float getUnitScale() {
		return unitScale;
	}
	
	public TiledMapRenderer(TiledMap map) {
		this(map, new SpriteBatch());
		ownsSpriteBatch = true;
	}
	
	public TiledMapRenderer(TiledMap map, float unitScale) {
		this(map, new SpriteBatch(), unitScale);
		ownsSpriteBatch = true;
	}
	
	public TiledMapRenderer(TiledMap map, SpriteBatch spriteBatch) {
		this.map = map;
		this.spriteBatch = spriteBatch;
		this.ownsSpriteBatch = false;
	}
	
	public TiledMapRenderer(TiledMap map, SpriteBatch spriteBatch, float unitScale) {
		this.map = map;
		this.spriteBatch = spriteBatch;
		this.unitScale = unitScale;
		this.ownsSpriteBatch = false;
	}

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
	
	/* (non-Javadoc)
	 * @see com.badlogic.gdx.maps.MapRenderer2#render(float, float, float, float, int[])
	 */
	@Override
	public void render (float viewboundsX, float viewboundsY, float viewboundsWidth, float viewboundsHeight) {
		MapLayers mapLayers = map.getLayers();
		for (MapLayer layer : mapLayers) {
			renderLayer(viewboundsX, viewboundsY, viewboundsX + viewboundsHeight, viewboundsY + viewboundsHeight, layer);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.badlogic.gdx.maps.MapRenderer2#render(float, float, float, float, int[])
	 */
	@Override
	public void render (float viewboundsX, float viewboundsY, float viewboundsWidth, float viewboundsHeight, int[] layers) {
		MapLayers mapLayers = map.getLayers();
		for (int i = 0; i < layers.length; i++) {
			renderLayer(viewboundsX, viewboundsY, viewboundsX + viewboundsHeight, viewboundsY + viewboundsHeight, mapLayers.getLayer(i));
		}
	}

	float[] vertices = new float[20];
	protected void renderLayer(float camLeft, float camTop, float camRight, float camBottom, MapLayer layer) {
		if (layer.getVisible()) {
			
			float color = Color.toFloatBits(1, 1, 1, layer.getOpacity());
			
			if (layer instanceof TiledMapTileLayer) {
				final TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
				
				final int layerWidth = tileLayer.getWidth();
				final int layerHeight = tileLayer.getHeight();
				
				final float layerTileWidth = tileLayer.getTileWidth() * unitScale;
				final float layerTileHeight = tileLayer.getTileHeight() * unitScale;
				
				final int col1 = Math.max(0, (int) (camLeft / layerTileWidth));
				final int col2 = Math.min(layerWidth, (int) ((camRight + layerTileWidth) / layerTileWidth));

				final int row1 = Math.max(0, (int) (camBottom / layerTileHeight));
				final int row2 = Math.min(layerHeight, (int) ((camTop + layerTileHeight) / layerTileHeight));				
				
				for (int row = row1; row < row2; row++) {
					for (int col = col1; col < col2; col++) {
						final TiledMapTileLayer.Cell cell = tileLayer.getCell(col, row);
						final TiledMapTile tile = cell.getTile();
						if (tile != null) {
							
							final boolean flipX = cell.getFlipHorizontally();
							final boolean flipY = cell.getFlipVertically();
							final int rotations = cell.getRotation();
							
							TextureRegion region = tile.getTextureRegion();
							
							float x1 = col * layerTileWidth;
							float y1 = row * layerTileHeight;
							float x2 = x1 + region.getRegionWidth() * unitScale;
							float y2 = y1 + region.getRegionHeight() * unitScale;
							
							float u1 = region.getU();
							float v1 = region.getV2();
							float u2 = region.getU2();
							float v2 = region.getV();
							
							vertices[X1] = x1;
							vertices[Y1] = y1;
							vertices[C1] = color;
							vertices[U1] = u1;
							vertices[V1] = v1;
							
							vertices[X2] = x1;
							vertices[Y2] = y2;
							vertices[C2] = color;
							vertices[U2] = u1;
							vertices[V2] = v2;
							
							vertices[X3] = x2;
							vertices[Y3] = y2;
							vertices[C3] = color;
							vertices[U3] = u2;
							vertices[V3] = v2;
							
							vertices[X4] = x2;
							vertices[Y4] = y1;
							vertices[C4] = color;
							vertices[U4] = u2;
							vertices[V4] = v1;							
							
							if (flipX) {
								float temp = vertices[U1];
								vertices[U1] = vertices[U3];
								vertices[U3] = temp;
								temp = vertices[U2];
								vertices[U2] = vertices[U4];
								vertices[U4] = temp;
							}
							if (flipY) {
								float temp = vertices[V1];
								vertices[V1] = vertices[V3];
								vertices[V3] = temp;
								temp = vertices[V2];
								vertices[V2] = vertices[V4];
								vertices[V4] = temp;
							}
							if (rotations != 0) {
								switch (rotations) {
									case Cell.ROTATE_90: {
										float tempV = vertices[V1];
										vertices[V1] = vertices[V2];
										vertices[V2] = vertices[V3];
										vertices[V3] = vertices[V4];
										vertices[V4] = tempV;
	
										float tempU = vertices[U1];
										vertices[U1] = vertices[U2];
										vertices[U2] = vertices[U3];
										vertices[U3] = vertices[U4];
										vertices[U4] = tempU;									
										break;
									}
									case Cell.ROTATE_180: {
										float tempU = vertices[U1];
										vertices[U1] = vertices[U3];
										vertices[U3] = tempU;
										tempU = vertices[U2];
										vertices[U2] = vertices[U4];
										vertices[U4] = tempU;									
										float tempV = vertices[V1];
										vertices[V1] = vertices[V3];
										vertices[V3] = tempV;
										tempV = vertices[V2];
										vertices[V2] = vertices[V4];
										vertices[V4] = tempV;
										break;
									}
									case Cell.ROTATE_270: {
										float tempV = vertices[V1];
										vertices[V1] = vertices[V4];
										vertices[V4] = vertices[V3];
										vertices[V3] = vertices[V2];
										vertices[V2] = tempV;
	
										float tempU = vertices[U1];
										vertices[U1] = vertices[U4];
										vertices[U4] = vertices[U3];
										vertices[U3] = vertices[U2];
										vertices[U2] = tempU;									
										break;
									}
								}								
							}
							spriteBatch.draw(region.getTexture(), vertices, 0, 20);
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

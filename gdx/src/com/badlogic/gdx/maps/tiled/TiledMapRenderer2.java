package com.badlogic.gdx.maps.tiled;

import static com.badlogic.gdx.graphics.g2d.SpriteBatch.C1;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.C2;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.C3;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.C4;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.U1;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.U2;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.U3;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.U4;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.V1;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.V2;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.V3;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.V4;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.X1;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.X2;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.X3;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.X4;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.Y1;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.Y2;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.Y3;
import static com.badlogic.gdx.graphics.g2d.SpriteBatch.Y4;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

public interface TiledMapRenderer2 {
	
	public void setViewBounds(float x, float y, float width, float height);
	
	public void setProjectionMatrix(Matrix4 projection);
	
	public void begin();
	public void end();
	
	public void render();
	public void renderObject(MapObject object);
	public void renderTileLayer(TiledMapTileLayer layer);
	
	public class BaseTiledMapRenderer implements TiledMapRenderer2 {
		
		protected TiledMap map;

		protected float unitScale;
		
		protected SpriteBatch spriteBatch;
		
		protected Rectangle viewBounds; 

		public TiledMap getMap() {
			return map;			
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
		
		public BaseTiledMapRenderer(TiledMap map) {
			this.map = map;
			this.unitScale = 1;
			this.viewBounds = new Rectangle();
		}
		
		public BaseTiledMapRenderer(TiledMap map, float unitScale) {
			this.map = map;
			this.unitScale = unitScale;
			this.viewBounds = new Rectangle();
			this.spriteBatch = new SpriteBatch();
		}
		
		@Override
		public void setViewBounds (float x, float y, float width, float height) {
			viewBounds.set(x, y, width, height);
		}
		
		@Override
		public void setProjectionMatrix (Matrix4 projection) {
			spriteBatch.setProjectionMatrix(projection);
		}
		
		@Override
		public void begin () {
			spriteBatch.begin();
		}

		@Override
		public void end () {
			spriteBatch.end();
		}
		
		@Override
		public void render () {
			for (MapLayer layer : map.getLayers()) {
				if (layer.getVisible()) {
					if (layer instanceof TiledMapTileLayer) {
						renderTileLayer((TiledMapTileLayer) layer);
					} else {
						for (MapObject object : layer.getObjects()) {
							renderObject(object);
						}
					}					
				}				
			}			
		}

		@Override
		public void renderObject (MapObject object) {
			// Do nothing
		}

		@Override
		public void renderTileLayer (TiledMapTileLayer layer) {
			// Do nothing			
		}

	}
	
	public class IsometricTiledMapRenderer extends  BaseTiledMapRenderer {

		private TiledMap map;
		
		private float[] vertices = new float[20];
		
		ShapeRenderer shapeRenderer;
		
		public IsometricTiledMapRenderer(TiledMap map) {
			super(map);
			shapeRenderer = new ShapeRenderer();
		}
		
		public IsometricTiledMapRenderer(TiledMap map, float unitScale) {
			super(map, unitScale);
			shapeRenderer = new ShapeRenderer();
		}	
		
		@Override
		public void renderObject (MapObject object) {
			
		}

		@Override
		public void setProjectionMatrix (Matrix4 projection) {
			super.setProjectionMatrix(projection);
			shapeRenderer.setProjectionMatrix(projection);
		}
		
		@Override
		public void renderTileLayer (TiledMapTileLayer layer) {

			final float color = Color.toFloatBits(1, 1, 1, layer.getOpacity());
			
			int col1 = 0;
			int col2 = layer.getWidth() - 1;
			
			int row1 = 0;
			int row2 = layer.getHeight() - 1;
			
			float tileWidth = layer.getTileWidth() * unitScale;
			float tileHeight = layer.getTileHeight() * unitScale;
			float halfTileWidth = tileWidth * 0.5f;
			float halfTileHeight = tileHeight * 0.5f;
			
			//shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
			
			for (int row = row2; row >= row1; row--) {
				boolean firstCol = true;
				for (int col = col1; col <= col2; col++) {
					float x = (col * halfTileWidth) + (row * halfTileWidth);
					float y = (row * halfTileHeight) - (col * halfTileHeight);
//					
//					shapeRenderer.line(x, y + halfTileHeight, x + halfTileWidth, y + tileHeight);
//					shapeRenderer.line(x + halfTileWidth, y + tileHeight, x + tileWidth, y + halfTileHeight);
//					shapeRenderer.line(x + tileWidth, y + halfTileHeight, x + halfTileWidth, y);
//					shapeRenderer.line(x + halfTileWidth, y, x, y + halfTileHeight);
					
					final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
					final TiledMapTile tile = cell.getTile();
					if (tile != null) {
						
						final boolean flipX = cell.getFlipHorizontally();
						final boolean flipY = cell.getFlipVertically();
						final int rotations = cell.getRotation();
						
						TextureRegion region = tile.getTextureRegion();
						
						float x1 = x;
						float y1 = y;
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
			
//			shapeRenderer.end();
		}
		
	}
	
	public class OrthogonalTiledMapRenderer extends BaseTiledMapRenderer {
		
		private float[] vertices = new float[20];
		
		public OrthogonalTiledMapRenderer(TiledMap map) {
			super(map);
		}

		public OrthogonalTiledMapRenderer(TiledMap map, float unitScale) {
			super(map, unitScale);
		}		
		
		@Override
		public void renderObject (MapObject object) {
			
		}

		@Override
		public void renderTileLayer (TiledMapTileLayer layer) {
			
			final float color = Color.toFloatBits(1, 1, 1, layer.getOpacity());
			
			final int layerWidth = layer.getWidth();
			final int layerHeight = layer.getHeight();
			
			final float layerTileWidth = layer.getTileWidth() * unitScale;
			final float layerTileHeight = layer.getTileHeight() * unitScale;
			
			final int col1 = Math.max(0, (int) (viewBounds.x / layerTileWidth));
			final int col2 = Math.min(layerWidth, (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth));

			final int row1 = Math.max(0, (int) (viewBounds.y / layerTileHeight));
			final int row2 = Math.min(layerHeight, (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight));				
			
			for (int row = row1; row < row2; row++) {
				for (int col = col1; col < col2; col++) {
					final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
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

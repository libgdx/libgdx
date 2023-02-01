/*******************************************************************************
 * Copyright 2013 See AUTHORS file.
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

package com.badlogic.gdx.maps.tiled.renderers;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

public abstract class BatchTiledMapRenderer implements TiledMapRenderer, Disposable {
	static protected final int NUM_VERTICES = 20;

	protected TiledMap map;

	protected float unitScale;

	protected Batch batch;

	protected Rectangle viewBounds;
	protected Rectangle imageBounds = new Rectangle();
	protected Rectangle repeatedImageBounds = new Rectangle();

	protected boolean ownsBatch;

	protected float vertices[] = new float[NUM_VERTICES];

	public TiledMap getMap () {
		return map;
	}

	public void setMap (TiledMap map) {
		this.map = map;
	}

	public float getUnitScale () {
		return unitScale;
	}

	public Batch getBatch () {
		return batch;
	}

	public Rectangle getViewBounds () {
		return viewBounds;
	}

	public BatchTiledMapRenderer (TiledMap map) {
		this(map, 1.0f);
	}

	public BatchTiledMapRenderer (TiledMap map, float unitScale) {
		this.map = map;
		this.unitScale = unitScale;
		this.viewBounds = new Rectangle();
		this.batch = new SpriteBatch();
		this.ownsBatch = true;
	}

	public BatchTiledMapRenderer (TiledMap map, Batch batch) {
		this(map, 1.0f, batch);
	}

	public BatchTiledMapRenderer (TiledMap map, float unitScale, Batch batch) {
		this.map = map;
		this.unitScale = unitScale;
		this.viewBounds = new Rectangle();
		this.batch = batch;
		this.ownsBatch = false;
	}

	@Override
	public void setView (OrthographicCamera camera) {
		batch.setProjectionMatrix(camera.combined);
		float width = camera.viewportWidth * camera.zoom;
		float height = camera.viewportHeight * camera.zoom;
		float w = width * Math.abs(camera.up.y) + height * Math.abs(camera.up.x);
		float h = height * Math.abs(camera.up.y) + width * Math.abs(camera.up.x);
		viewBounds.set(camera.position.x - w / 2, camera.position.y - h / 2, w, h);
	}

	@Override
	public void setView (Matrix4 projection, float x, float y, float width, float height) {
		batch.setProjectionMatrix(projection);
		viewBounds.set(x, y, width, height);
	}

	@Override
	public void render () {
		beginRender();
		for (MapLayer layer : map.getLayers()) {
			renderMapLayer(layer);
		}
		endRender();
	}

	@Override
	public void render (int[] layers) {
		beginRender();
		for (int layerIdx : layers) {
			MapLayer layer = map.getLayers().get(layerIdx);
			renderMapLayer(layer);
		}
		endRender();
	}

	protected void renderMapLayer (MapLayer layer) {
		if (!layer.isVisible()) return;
		if (layer instanceof MapGroupLayer) {
			MapLayers childLayers = ((MapGroupLayer)layer).getLayers();
			for (int i = 0; i < childLayers.size(); i++) {
				MapLayer childLayer = childLayers.get(i);
				if (!childLayer.isVisible()) continue;
				renderMapLayer(childLayer);
			}
		} else {
			if (layer instanceof TiledMapTileLayer) {
				renderTileLayer((TiledMapTileLayer)layer);
			} else if (layer instanceof TiledMapImageLayer) {
				renderImageLayer((TiledMapImageLayer)layer);
			} else {
				renderObjects(layer);
			}
		}
	}

	@Override
	public void renderObjects (MapLayer layer) {
		for (MapObject object : layer.getObjects()) {
			renderObject(object);
		}
	}

	@Override
	public void renderObject (MapObject object) {

	}

	@Override
	public void renderImageLayer (TiledMapImageLayer layer) {
		final Color batchColor = batch.getColor();
		final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

		final float[] vertices = this.vertices;

		TextureRegion region = layer.getTextureRegion();

		if (region == null) {
			return;
		}

		final float x = layer.getX();
		final float y = layer.getY();
		final float x1 = x * unitScale;
		final float y1 = y * unitScale;
		final float x2 = x1 + region.getRegionWidth() * unitScale;
		final float y2 = y1 + region.getRegionHeight() * unitScale;

		imageBounds.set(x1, y1, x2 - x1, y2 - y1);

		if (!layer.isRepeatX() && !layer.isRepeatY()) {
			if (viewBounds.contains(imageBounds) || viewBounds.overlaps(imageBounds)) {
				final float u1 = region.getU();
				final float v1 = region.getV2();
				final float u2 = region.getU2();
				final float v2 = region.getV();

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

				batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
			}
		} else {

			float repeatX = layer.isRepeatX() ? (viewBounds.width / imageBounds.width) + 3 : 0;
			float repeatY = layer.isRepeatY() ? (viewBounds.height / imageBounds.height) + 3 : 0;

			for (int i = 0; i <= repeatX; i++) {
				for (int j = 0; j <= repeatY; j++) {
					float rx1 = x1;
					float ry1 = y1;
					float rx2 = x2;
					float ry2 = y2;

					/** What's going on here? Explaining for the x position, same reasoning is used for Y. We need to tile this layer
					 * infinitely on the x, y, or both axis, based on its origin, so it matches up with Tiled's rendering. rx1 = (x1 +
					 * i * imageBounds.width); ^--this would tile the image layer from it's x,y/offset position, but what we want is
					 * the texture be tiled from left to right, within the camera's bounds, while keeping its offset rx1 =
					 * (viewBounds.x-(viewBounds.x% imageBounds.width)) ^-- Here we can ignore the x1 starting position and remove
					 * that, as it no longer matters, we want the starting position flush to camera's x position, Taking camera's x
					 * position we then need to subtract that by the camera modulus of the image width to get the remainder so we know
					 * how far to move the image to get flush with the camera, But of course we are not done yet, if the image starts
					 * off with an x/y position that is not zero, (an offset) we must now negate this. rx1 =
					 * ((viewBounds.x)-(viewBounds.x% imageBounds.width)) + (x1% imageBounds.width); ^---We offset by (x1 %
					 * imageBounds.width) which is a simple way to get the remainder of how many textures would fit between its
					 * starting position and 0. rx1 = ((viewBounds.x)-(viewBounds.x% imageBounds.width))+(i -2)*imageBounds.width+(x1%
					 * imageBounds.width); ^--- Finally this needs to be tiled, this is where our loops comes in, we use the repeatX =
					 * (viewBounds.width / imageBounds.width) +3 to find out how many textures we can fit in the camera + 3 more(so we
					 * can expand beyond the borders ) so we dont have pop in and out around the edges of the camera, we use + (i -2)
					 * to start off placing them to the left of the camera, and we fill it out going right multiplying by our image
					 * width. until we go through entire loop. *note for rx2 position we have to add the image width, for ry2 we add
					 * height */

					if (layer.isRepeatX()) {
						rx1 = (viewBounds.x - (viewBounds.x % imageBounds.width)) + ((i - 2) * imageBounds.width)
							+ (x1 % imageBounds.width);
						rx2 = ((imageBounds.width + viewBounds.x) - (viewBounds.x % imageBounds.width)) + ((i - 2) * imageBounds.width)
							+ (x1 % imageBounds.width);
					}

					if (layer.isRepeatY()) {
						ry1 = (viewBounds.y - (viewBounds.y % imageBounds.height)) + ((j - 2) * imageBounds.height)
							+ (y1 % imageBounds.height);
						ry2 = ((imageBounds.height + viewBounds.y) - (viewBounds.y % imageBounds.height))
							+ ((j - 2) * imageBounds.height) + (y1 % imageBounds.height);
					}

					repeatedImageBounds.set(rx1, ry1, rx2 - rx1, ry2 - ry1);

					if (viewBounds.contains(repeatedImageBounds) || viewBounds.overlaps(repeatedImageBounds)) {
						final float ru1 = region.getU();
						final float rv1 = region.getV2();
						final float ru2 = region.getU2();
						final float rv2 = region.getV();

						vertices[X1] = rx1;
						vertices[Y1] = ry1;
						vertices[C1] = color;
						vertices[U1] = ru1;
						vertices[V1] = rv1;

						vertices[X2] = rx1;
						vertices[Y2] = ry2;
						vertices[C2] = color;
						vertices[U2] = ru1;
						vertices[V2] = rv2;

						vertices[X3] = rx2;
						vertices[Y3] = ry2;
						vertices[C3] = color;
						vertices[U3] = ru2;
						vertices[V3] = rv2;

						vertices[X4] = rx2;
						vertices[Y4] = ry1;
						vertices[C4] = color;
						vertices[U4] = ru2;
						vertices[V4] = rv1;

						batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
					}
				}
			}
		}
	}

	/** Called before the rendering of all layers starts. */
	protected void beginRender () {
		AnimatedTiledMapTile.updateAnimationBaseTime();
		batch.begin();
	}

	/** Called after the rendering of all layers ended. */
	protected void endRender () {
		batch.end();
	}

	@Override
	public void dispose () {
		if (ownsBatch) {
			batch.dispose();
		}
	}

}

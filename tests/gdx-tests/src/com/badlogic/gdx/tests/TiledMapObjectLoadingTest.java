/**
 * *****************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ****************************************************************************
 */

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;

public class TiledMapObjectLoadingTest extends GdxTest {

	private TiledMap map;
	private ShapeRenderer shapeRenderer;
	private OrthographicCamera camera;
	private OrthoCamController cameraController;
	private BitmapFont font;
	private SpriteBatch batch;
	private String loadingStatus;

	@Override
	public void create () {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * 512, 512);
		camera.zoom = .5f;
		camera.update();

		cameraController = new OrthoCamController(camera);
		Gdx.input.setInputProcessor(cameraController);

		font = new BitmapFont();
		batch = new SpriteBatch();
		map = new TmxMapLoader().load("data/maps/tiled-objects/test-load-mapobjects.tmx");
		MapProperties properties = map.getProperties();
		shapeRenderer = new ShapeRenderer();
		
		// Test get objects by type (adding circle manually because it doesn't exists in Tiledmap editor)
		
		loadingStatus = "loading status:\n";
		MapLayer layer = map.getLayers().get("Objects");
		MapObjects mapObjects = layer.getObjects();
		
		mapObjects.add(new CircleMapObject(280, 400, 50));

		loadingStatus += "- MapObject : " + mapObjects.getByType(MapObject.class).size + "\n";
		loadingStatus += "- CircleMapObject : " + mapObjects.getByType(CircleMapObject.class).size + "\n";
		loadingStatus += "- EllipseMapObject : " + mapObjects.getByType(EllipseMapObject.class).size + "\n";
		loadingStatus += "- PolygonMapObject : " + mapObjects.getByType(PolygonMapObject.class).size + "\n";
		loadingStatus += "- PolylineMapObject : " + mapObjects.getByType(PolylineMapObject.class).size + "\n";
		loadingStatus += "- RectangleMapObject : " + mapObjects.getByType(RectangleMapObject.class).size + "\n";
		loadingStatus += "- TextureMapObject : " + mapObjects.getByType(TextureMapObject.class).size + "\n";
		loadingStatus += "- TiledMapTileMapObject : " + mapObjects.getByType(TiledMapTileMapObject.class).size + "\n";
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.55f, 0.55f, 0.55f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		shapeRenderer.setProjectionMatrix(camera.combined);
		batch.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(Color.BLUE);
		Gdx.gl20.glLineWidth(2);
		MapLayer layer = map.getLayers().get("Objects");
		AnimatedTiledMapTile.updateAnimationBaseTime();
		for (MapObject mapObject : layer.getObjects()) {
			if (!mapObject.isVisible()) continue;
			if (mapObject instanceof TiledMapTileMapObject) {
				batch.begin();
				TiledMapTileMapObject tmtObject = (TiledMapTileMapObject)mapObject;
				TextureRegion textureRegion = tmtObject.getTile().getTextureRegion();
				// TilEd rotation is clockwise, we need counter-clockwise.
				float rotation = -tmtObject.getRotation();
				float scaleX = tmtObject.getScaleX();
				float scaleY = tmtObject.getScaleY();
				float xPos = tmtObject.getX();
				float yPos = tmtObject.getY();
				textureRegion.flip(tmtObject.isFlipHorizontally(), tmtObject.isFlipVertically());
				batch.draw(textureRegion, xPos, yPos, tmtObject.getOriginX() * scaleX, tmtObject.getOriginY() * scaleY,
					textureRegion.getRegionWidth() * scaleX, textureRegion.getRegionHeight() * scaleY, 1f, 1f, rotation);
				// We flip back to the original state.
				textureRegion.flip(tmtObject.isFlipHorizontally(), tmtObject.isFlipVertically());
				batch.end();
			} else if (mapObject instanceof EllipseMapObject) {
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				Ellipse ellipse = ((EllipseMapObject)mapObject).getEllipse();
				shapeRenderer.ellipse(ellipse.x, ellipse.y, ellipse.width, ellipse.height);
				shapeRenderer.end();
			} else if (mapObject instanceof CircleMapObject) {
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				Circle circle = ((CircleMapObject)mapObject).getCircle();
				shapeRenderer.circle(circle.x, circle.y, circle.radius);
				shapeRenderer.end();
			} else if (mapObject instanceof RectangleMapObject) {
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				Rectangle rectangle = ((RectangleMapObject)mapObject).getRectangle();
				shapeRenderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
				shapeRenderer.end();
			} else if (mapObject instanceof PolygonMapObject) {
				shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
				Polygon polygon = ((PolygonMapObject)mapObject).getPolygon();
				shapeRenderer.polygon(polygon.getTransformedVertices());
				shapeRenderer.end();
			} else if (mapObject instanceof PolylineMapObject) {
				shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
				Polyline polyline = ((PolylineMapObject)mapObject).getPolyline();
				shapeRenderer.polyline(polyline.getTransformedVertices());
				shapeRenderer.end();
			}
		}
		batch.begin();
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond() + "\n" + loadingStatus, 20, 500);
		batch.end();
	}

	@Override
	public void dispose () {
		map.dispose();
		shapeRenderer.dispose();
	}
}

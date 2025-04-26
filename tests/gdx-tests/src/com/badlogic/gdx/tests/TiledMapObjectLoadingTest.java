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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class TiledMapObjectLoadingTest extends GdxTest {

	private TiledMap map;
	private ShapeRenderer shapeRenderer;
	private OrthographicCamera camera;
	private OrthoCamController cameraController;
	private BitmapFont font;
	private BitmapFont textMapObjectFont;
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
		textMapObjectFont = new BitmapFont();
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
		loadingStatus += "- PointMapObject : " + mapObjects.getByType(PointMapObject.class).size + "\n";
		loadingStatus += "- TiledMapTileMapObject : " + mapObjects.getByType(TiledMapTileMapObject.class).size + "\n";
		loadingStatus += "- TextMapObject : " + mapObjects.getByType(TextMapObject.class).size + "\n";
	}

	@Override
	public void render () {
		ScreenUtils.clear(0.55f, 0.55f, 0.55f, 1f);
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
				// Tiled rotation is clockwise, we need counter-clockwise.
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
			} else if (mapObject instanceof PointMapObject) {
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				Vector2 point = ((PointMapObject)mapObject).getPoint();
				// drawing circle, because shapeRenderer.point is barely visible, if visible at all
				shapeRenderer.circle(point.x, point.y, 1f);
				shapeRenderer.end();
			} else if (mapObject instanceof TextMapObject) {
				batch.begin();
				TextMapObject textMapObject = (TextMapObject)mapObject;

				int alignment;
				String hAlign = textMapObject.getHorizontalAlign();

				switch (hAlign.toLowerCase()) {
				case "center":
					alignment = Align.center;
					break;
				case "right":
					alignment = Align.right;
					break;
				case "left":
				default:
					// Default is 'left alignment, also there is no Align 'justify' like equivalent
					alignment = Align.left;
					break;
				}

				textMapObjectFont.setColor(textMapObject.getColor());
				// The text rendering starts from the baseline, causing the text to appear below the specified Y-coordinate.
				// To align the text with the top of the bounding box (as it appears in Tiled), we add textMapObject.getHeight() to
				// the Y position.
				textMapObjectFont.draw(batch, textMapObject.getText(), textMapObject.getX(),
					textMapObject.getY() + textMapObject.getHeight(), textMapObject.getWidth(), alignment, textMapObject.isWrap());
				batch.end();

				shapeRenderer.setColor(Color.DARK_GRAY);
				// Draw display bounding box of TextMapObject
				shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
				shapeRenderer.rect(textMapObject.getX(), textMapObject.getY(), textMapObject.getWidth(), textMapObject.getHeight());
				shapeRenderer.end();

				// reset back to blue
				shapeRenderer.setColor(Color.BLUE);
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
		font.dispose();
		textMapObjectFont.dispose();
	}
}

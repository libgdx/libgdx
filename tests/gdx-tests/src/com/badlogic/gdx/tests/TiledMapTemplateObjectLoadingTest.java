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
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.maps.tiled.TmjMapLoader;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class TiledMapTemplateObjectLoadingTest extends GdxTest {

	private TiledMap map;
	private ShapeRenderer shapeRenderer;
	private OrthographicCamera camera;
	private OrthoCamController cameraController;
	private BitmapFont font;
	private SpriteBatch batch;
	private String loadingStatus;
	private StringBuilder stringBuilder;
	private String defaultTemplateInfo;
	private String properties;
	private TmxMapLoader tmxMapLoader;
	private TmjMapLoader tmjMapLoader;
	private BitmapFont textMapObjectFont;
	private int mapType = 0;

	private final static String TMJ_TESTMAP = "data/maps/tiled-objects/test-load-maptemplateobjects.tmj";
	private final static String TMX_TESTMAP = "data/maps/tiled-objects/test-load-maptemplateobjects.tmx";

	@Override
	public void create () {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * 520, 520);
		camera.zoom = 1f;
		camera.position.set(w / 2, 1050, 0);
		camera.update();

		cameraController = new OrthoCamController(camera);
		Gdx.input.setInputProcessor(cameraController);

		stringBuilder = new StringBuilder();
		font = new BitmapFont();
		textMapObjectFont = new BitmapFont();
		batch = new SpriteBatch();
		tmxMapLoader = new TmxMapLoader();
		tmjMapLoader = new TmjMapLoader();
		map = tmxMapLoader.load(TMX_TESTMAP);

		MapProperties properties = map.getProperties();
		shapeRenderer = new ShapeRenderer();

		// Test get objects by type (adding circle manually because it doesn't exists in Tiledmap editor)
		loadingStatus = "All Objects are template Objects. You should see 2 of each type.\n";
		loadingStatus += "Top Object contains default template property values.\n";
		loadingStatus += "Object Underneath, may contain default values or overridden values.\n";
		loadingStatus += "Objects Marked as Overridden can be compared against the map file.\n";
		loadingStatus += "Toggle between loaders with keys 1 and 2. BOTH should have identical objects and properties.\n";
		loadingStatus += "Except for the TEXT object which has some different values based on map. \n";
		loadingStatus += "If not there may be a parsing issue. Compare files since Maps should be identical.";

		MapLayer layer = map.getLayers().get("Objects");
		MapObjects mapObjects = layer.getObjects();

	}

	@Override
	public void render () {
		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			if (mapType != 0) {
				mapType = 0;
				map = tmxMapLoader.load(TMX_TESTMAP);
			}
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
			if (mapType != 1) {
				mapType = 1;
				map = tmjMapLoader.load(TMJ_TESTMAP);
			}
		}

		ScreenUtils.clear(0.55f, 0.55f, 0.55f, 1f);
		camera.update();
		shapeRenderer.setProjectionMatrix(camera.combined);
		batch.setProjectionMatrix(camera.combined);
		if (mapType == 0) {
			shapeRenderer.setColor(Color.BLUE);
		} else {
			shapeRenderer.setColor(Color.RED);
		}

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
				shapeRenderer.circle(point.x, point.y, 2f);
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

				// Draw display bounding box of TextMapObject
				shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
				shapeRenderer.rect(textMapObject.getX(), textMapObject.getY(), textMapObject.getWidth(), textMapObject.getHeight());
				shapeRenderer.end();

			}
		}

		defaultTemplateInfo = "Hard Coded Default Reference:\n" + "BoolProp: true\n" + "ColorProp: #ff0000ff\n" + "IntProp: 86\n"
			+ "StringProp: Template Default";

		batch.begin();
		if (mapType == 0) {
			font.setColor(Color.BLUE);
			font.draw(batch, "Current Loader: TMX\n Press KEY 2\n to switch to TMJ LOADER", 20, 1315);
			font.setColor(Color.WHITE);
		} else if (mapType == 1) {
			font.setColor(Color.RED);
			font.draw(batch, "Current Loader: TMJ:\n Press KEY 1\n to switch to TMX LOADER\"", 20, 1315);
			font.setColor(Color.WHITE);
		}

		font.draw(batch, defaultTemplateInfo, 220, 1315);
		// Display Properties under the unaltered template objects
		for (MapObject mapObject : layer.getObjects()) {
			if (!mapObject.isVisible()) continue;
			if (mapObject instanceof TiledMapTileMapObject) {
				// check for default object without any alterations
				if (!mapObject.getProperties().containsKey("Overridden")) {
					// All properties should match the default hard coded ones
					font.draw(batch, readDefaultProps(mapObject), (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - (float)mapObject.getProperties().get("height") / 3);
				} else {
					/** Note, object named Texture that is overridden will be flipped on the Y axis since we overrode the Flipping
					 * Vertical property to true, that's why it appears upside down * */
					properties = "Overridden:\n" + "BoolProp: " + mapObject.getProperties().get("BoolProp") + "\n" + "ColorProp: "
						+ mapObject.getProperties().get("ColorProp") + "\n" + "IntProp: " + mapObject.getProperties().get("IntProp")
						+ "\n" + "StringProp: " + mapObject.getProperties().get("StringProp") + "\n" + "Overridden: "
						+ mapObject.getProperties().get("Overridden");
					font.draw(batch, properties, (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - (float)mapObject.getProperties().get("height") / 3);
				}

			} else if (mapObject instanceof EllipseMapObject) {
				// check for default object without any alterations
				if (!mapObject.getProperties().containsKey("Overridden")) {
					// All properties should match the default hard coded ones
					font.draw(batch, readDefaultProps(mapObject), (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - (float)mapObject.getProperties().get("height") / 3);
				} else {
					properties = "Overridden:\n" + "BoolProp: " + mapObject.getProperties().get("BoolProp") + "\n" + "ColorProp: "
						+ mapObject.getProperties().get("ColorProp") + "\n" + "IntProp: " + mapObject.getProperties().get("IntProp")
						+ "\n" + "StringProp: " + mapObject.getProperties().get("StringProp") + "\n" + "Overridden: "
						+ mapObject.getProperties().get("Overridden");
					font.draw(batch, properties, (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - (float)mapObject.getProperties().get("height") / 3);
				}
			} else if (mapObject instanceof RectangleMapObject) {

				// check for default object without any alterations
				if (!mapObject.getProperties().containsKey("Overridden")) {
					// All properties should match the default hard coded ones
					font.draw(batch, readDefaultProps(mapObject), (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - (float)mapObject.getProperties().get("height") / 3);
				} else {
					properties = "Overridden + Extra prop:\n" + "BoolProp: " + mapObject.getProperties().get("BoolProp") + "\n"
						+ "ColorProp: " + mapObject.getProperties().get("ColorProp") + "\n" + "IntProp: "
						+ mapObject.getProperties().get("IntProp") + "\n" + "StringProp: " + mapObject.getProperties().get("StringProp")
						+ "\n" + "NonTemplateProperty: " + mapObject.getProperties().get("NonTemplateProperty") + "\n" + "Overridden: "
						+ mapObject.getProperties().get("Overridden");
					font.draw(batch, properties, (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - (float)mapObject.getProperties().get("height") / 3);
				}

			} else if (mapObject instanceof PolygonMapObject) {
				// check for default object without any alterations
				if (!mapObject.getProperties().containsKey("Overridden")) {
					// All properties should match the default hard coded ones
					font.draw(batch, readDefaultProps(mapObject), (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - 75);
				} else {
					properties = "Overridden:\n" + "BoolProp: " + mapObject.getProperties().get("BoolProp") + "\n" + "ColorProp: "
						+ mapObject.getProperties().get("ColorProp") + "\n" + "IntProp: " + mapObject.getProperties().get("IntProp")
						+ "\n" + "StringProp: " + mapObject.getProperties().get("StringProp") + "\n" + "Overridden: "
						+ mapObject.getProperties().get("Overridden");
					font.draw(batch, properties, (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - 75);
				}

			} else if (mapObject instanceof PolylineMapObject) {
				// check for default object without any alterations
				if (!mapObject.getProperties().containsKey("Overridden")) {
					// All properties should match the default hard coded ones
					font.draw(batch, readDefaultProps(mapObject), (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - (float)mapObject.getProperties().get("height") / 3 - 10);
				} else {
					properties = "Overridden:\n" + "BoolProp: " + mapObject.getProperties().get("BoolProp") + "\n" + "ColorProp: "
						+ mapObject.getProperties().get("ColorProp") + "\n" + "IntProp: " + mapObject.getProperties().get("IntProp")
						+ "\n" + "StringProp: " + mapObject.getProperties().get("StringProp") + "\n" + "Overridden: "
						+ mapObject.getProperties().get("Overridden");
					font.draw(batch, properties, (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - (float)mapObject.getProperties().get("height") / 3 - 10);
				}
			} else if (mapObject instanceof PointMapObject) {
				// check for default object without any alterations
				if (!mapObject.getProperties().containsKey("Overridden")) {
					// All properties should match the default hard coded ones
					font.draw(batch, readDefaultProps(mapObject), (float)mapObject.getProperties().get("x") - 60,
						(float)mapObject.getProperties().get("y") - 20);
				} else {
					properties = "Overridden:\n" + "BoolProp: " + mapObject.getProperties().get("BoolProp") + "\n" + "ColorProp: "
						+ mapObject.getProperties().get("ColorProp") + "\n" + "IntProp: " + mapObject.getProperties().get("IntProp")
						+ "\n" + "StringProp: " + mapObject.getProperties().get("StringProp") + "\n" + "Overridden: "
						+ mapObject.getProperties().get("Overridden");
					font.draw(batch, properties, (float)mapObject.getProperties().get("x") - 60,
						(float)mapObject.getProperties().get("y") - 20);

				}

			} else if (mapObject instanceof TextMapObject) {
				// check for default object without any alterations
				if (!mapObject.getProperties().containsKey("Overridden")) {
					// All properties should match the default hard coded ones
					font.draw(batch, readDefaultProps(mapObject), (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - 40);
				} else {
					properties = "Overridden:\n" + "BoolProp: " + mapObject.getProperties().get("BoolProp") + "\n" + "ColorProp: "
						+ mapObject.getProperties().get("ColorProp") + "\n" + "IntProp: " + mapObject.getProperties().get("IntProp")
						+ "\n" + "StringProp: " + mapObject.getProperties().get("StringProp") + "\n" + "Overridden: "
						+ mapObject.getProperties().get("Overridden");
					font.draw(batch, properties, (float)mapObject.getProperties().get("x"),
						(float)mapObject.getProperties().get("y") - 40);

				}

			}
		}

		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond() + "\n" + loadingStatus, 20, 1470);
		batch.end();
	}

	private String readDefaultProps (MapObject mapObject) {
		stringBuilder.setLength(0);
		stringBuilder.append("Template Default:\n");
		stringBuilder.append("BoolProp: ").append(mapObject.getProperties().get("BoolProp")).append("\n");
		stringBuilder.append("ColorProp: ").append(mapObject.getProperties().get("ColorProp")).append("\n");
		stringBuilder.append("IntProp: ").append(mapObject.getProperties().get("IntProp")).append("\n");
		stringBuilder.append("StringProp: ").append(mapObject.getProperties().get("StringProp"));
		return stringBuilder.toString();
	}

	@Override
	public void dispose () {
		map.dispose();
		shapeRenderer.dispose();
	}
}

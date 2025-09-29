
package com.badlogic.gdx.tests;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.ScreenUtils;

public class TiledMapObjectPropertyTest extends GdxTest {

	private TiledMap map;
	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	private OrthographicCamera camera;
	private TiledMapRenderer mapRenderer;
	private Array<MapObject> objects;

	private boolean error;

	@Override
	public void create () {
		try {
			TmxMapLoader loader = new TmxMapLoader();
			// run multiple times to ensure reloading map works correctly
			for (int i = 0; i < 3; i++) {
				Gdx.app.log("-------------------------------------", "Running test " + (i + 1) + "/3\n");

				StringBuilder builder = new StringBuilder();
				builder.append("Expected results:\n").append("- Object with id 1 should have \"object\" props:\n")
					.append("\t- Points_To_ID_1 = id: 1\n").append("\t- Points_To_ID_2 = id: 2\n")
					.append("\t- Points_To_ID_5 = id: 5\n").append("- Object with id 2 should have \"object\" props:\n")
					.append("\t- Points_To_ID_3 = id: 3\n").append("\t- Points_To_ID_4 = id: 4\n")
					.append("- Object with id 3 should have \"object\" props:\n").append("\t- Points_To_ID_2 = id: 2\n")
					.append("- Object with id 4 should have \"object\" props:\n").append("\t- Points_To_ID_1 = id: 1\n")
					.append("- Objects with id's 5 and 6 should have \"object\" props:\n").append("\t- Placeholder = 0\n");
				Gdx.app.log("TiledMapObjectPropertyTest", builder.toString());

				float w = Gdx.graphics.getWidth();
				float h = Gdx.graphics.getHeight();

				camera = new OrthographicCamera();
				camera.setToOrtho(false, (w / h) * 512, 512);
				camera.zoom = .5f;
				camera.update();

				OrthoCamController cameraController = new OrthoCamController(camera);
				Gdx.input.setInputProcessor(cameraController);

				map = loader.load("data/maps/tiled-objects/test-object-properties.tmx");

				batch = new SpriteBatch();
				shapeRenderer = new ShapeRenderer();
				mapRenderer = new OrthogonalTiledMapRenderer(map);

				MapObjects objects1 = map.getLayers().get("Objects 1").getObjects();
				MapObjects objects2 = map.getLayers().get("Objects 2").getObjects();
				objects = new Array<>();
				for (MapObject object : objects1) {
					objects.add(object);
				}
				for (MapObject object : objects2) {
					objects.add(object);
				}

				IntMap<MapObject> idToObject = loader.getIdToObject();

				builder.setLength(0);
				builder.append("\nidToObject: {");
				for (IntMap.Entry<MapObject> entry : idToObject) {
					builder.append("\n\t").append(entry.key).append(" -> ").append(entry.value);
				}
				builder.append("\n}\n");
				Gdx.app.log("TiledMapObjectPropertyTest", builder.toString());

				for (MapObject object1 : objects) {
					int id = object1.getProperties().get("id", Integer.class);
					MapObject object2 = idToObject.get(id);
					if (object1 != object2) {
						throw new RuntimeException(
							"Error! Object with id " + id + " " + "is not the same object as the one in the idToObject map!");
					}

					MapProperties props = object1.getProperties();
					switch (id) {
					case 1:
						test(props, 2, idToObject);
						test(props, 5, idToObject);
						test(props, 1, idToObject);
						break;
					case 2:
						test(props, 3, idToObject);
						test(props, 4, idToObject);
						break;
					case 3:
						test(props, 2, idToObject);
						break;
					case 4:
						test(props, 1, idToObject);
						break;
					case 5:
					case 6:
						Iterator<String> propKeysIterator = props.getKeys();
						ObjectSet<String> propKeys = new ObjectSet<>();
						while (propKeysIterator.hasNext()) {
							propKeys.add(propKeysIterator.next());
						}
						if (propKeys.size != 6) {
							throw new RuntimeException("Object with id " + id + " should " + "have six keys " + "but has " + propKeys);
						}
					}
				}

				builder.setLength(0);
				builder.append("Actual results:\n");
				for (IntMap.Entry<MapObject> entry : idToObject.entries()) {
					int id = entry.key;
					MapProperties props = entry.value.getProperties();

					builder.append("- Object with id ").append(id).append(" has \"object\" props:\n");

					Iterator<String> propKeysIterator = props.getKeys();
					Iterator<Object> propValuesIterator = props.getValues();

					while (propKeysIterator.hasNext() && propValuesIterator.hasNext()) {
						Object value = propValuesIterator.next();
						String key = propKeysIterator.next();
						if (!key.contains("Points_To_ID_") && !key.contains("Placeholder")) {
							continue;
						}

						if (value instanceof MapObject) {
							MapObject object = (MapObject)value;
							int objectId = object.getProperties().get("id", Integer.class);
							value = "id: " + objectId + ", object: " + object;
						}

						builder.append("\t\t").append(key).append(" -> ").append(value).append("\n");
					}
				}
				Gdx.app.log("TiledMapObjectPropertyTest", builder.toString());
			}
		} catch (Exception e) {
			Gdx.app.error("TiledMapObjectPropertyTest", "Failed to run test!", e);
			e.printStackTrace();
			error = true;
		}
	}

	@Override
	public void render () {
		if (error) {
			Gdx.app.error("TiledMapObjectPropertyTest", "Failed to run test!");
			Gdx.app.exit();
		}

		ScreenUtils.clear(0.55f, 0.55f, 0.55f, 1f);
		camera.update();
		mapRenderer.setView(camera);
		mapRenderer.render();

		shapeRenderer.setProjectionMatrix(camera.combined);
		batch.setProjectionMatrix(camera.combined);

		shapeRenderer.setColor(Color.BLUE);
		Gdx.gl20.glLineWidth(2);
		for (MapObject object : objects) {
			if (!object.isVisible()) continue;
			if (object instanceof RectangleMapObject) {
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				Rectangle rectangle = ((RectangleMapObject)object).getRectangle();
				shapeRenderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
				shapeRenderer.end();
			}
		}
	}

	@Override
	public void dispose () {
		map.dispose();
		shapeRenderer.dispose();
	}

	private void test (MapProperties props, int idToObjProp1, IntMap<MapObject> idToObjectMap) {
		String key = "Points_To_ID_" + idToObjProp1;
		if (!props.containsKey(key)) {
			throw new GdxRuntimeException("Missing property: " + key);
		}

		MapObject other1 = idToObjectMap.get(idToObjProp1);
		MapObject other2 = props.get(key, MapObject.class);

		if (other1 != other2) {
			throw new GdxRuntimeException("Property " + key + " does not point to the correct object");
		}
	}
}

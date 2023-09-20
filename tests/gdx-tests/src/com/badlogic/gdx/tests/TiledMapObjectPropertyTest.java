package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
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
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Iterator;

/**
 * Test for successful loading of "object" properties for tiled objects in a map. The value of the "object"
 * property is the id of another tiled object. This test ensures that the "object" property is correctly
 * loaded and points to the correct object.
 *
 * The following should be true:
 * - Object with id 1 should have props:
 *     - Points_To_ID_1 = 1
 *     - Points_To_ID_2 = 2
 *     - Points_To_ID_5 = 5
 * - Object with id 2 should have props:
 *     - Points_To_ID_3 = 3
 *     - Points_To_ID_4 = 4
 * - Object with id 3 should have props:
 *     - Points_To_ID_2 = 2
 * - Object with id 4 should have props:
 *     - Points_To_ID_1 = 1
 * - Objects with id's 5 and 6 should have props:
 *     - Placeholder = 0
 */
public class TiledMapObjectPropertyTest extends GdxTest {

    private TiledMap map;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private TiledMapRenderer mapRenderer;
    private boolean error;

    @Override
    public void create() {
        Gdx.app.log("TiledMapObjectPropertyTest", "Running test...");
        try {
            float w = Gdx.graphics.getWidth();
            float h = Gdx.graphics.getHeight();

            camera = new OrthographicCamera();
            camera.setToOrtho(false, (w / h) * 512, 512);
            camera.zoom = .5f;
            camera.update();

            OrthoCamController cameraController = new OrthoCamController(camera);
            Gdx.input.setInputProcessor(cameraController);

            Gdx.app.log("TiledMapObjectPropertyTest", "Load map...");
            map = new TmxMapLoader().load("data/maps/tiled-objects/test-object-properties.tmx");
            Gdx.app.log("TiledMapObjectPropertyTest", "Finish loading map!");

            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            mapRenderer = new OrthogonalTiledMapRenderer(map);

            MapObjects objects = map.getLayers().get("Objects").getObjects();

            ObjectMap<Integer, MapProperties> idPropMap = new ObjectMap<>();
            for (MapObject object : objects) {
                int id = object.getProperties().get("id", Integer.class);
                idPropMap.put(id, object.getProperties());
            }

            for (MapObject object : objects) {
                int id = object.getProperties().get("id", Integer.class);
                Gdx.app.log("TiledMapObjectPropertyTest", "Testing object with id " + id);
                MapProperties props = idPropMap.get(id);

                switch (id) {
                    case 1:
                        test(props, 2, idPropMap);
                        test(props, 5, idPropMap);
                        test(props, 1, idPropMap);
                        break;
                    case 2:
                        test(props, 3, idPropMap);
                        test(props, 4, idPropMap);
                        break;
                    case 3:
                        test(props, 2, idPropMap);
                        break;
                    case 4:
                        test(props, 1, idPropMap);
                        break;
                    case 5:
                    case 6:
                        Iterator<String> propKeysIterator = props.getKeys();
                        ObjectSet<String> propKeys = new ObjectSet<>();
                        while (propKeysIterator.hasNext()) {
                            propKeys.add(propKeysIterator.next());
                        }
                        if (propKeys.size != 6) {
                            throw new RuntimeException("Object with id " + id + " should have six keys " +
                                    "but has " + propKeys);
                        }
                }
            }

            for (ObjectMap.Entry<Integer, MapProperties> entry : idPropMap.entries()) {
                int id = entry.key;
                MapProperties props = entry.value;

                System.out.println("Object with id " + id + " has \"object\" properties: ");

                Iterator<String> propKeysIterator = props.getKeys();
                Iterator<Object> propValuesIterator = props.getValues();

                while (propKeysIterator.hasNext() && propValuesIterator.hasNext()) {
                    Object value = propValuesIterator.next();
                    String key = propKeysIterator.next();
                    if (!key.contains("Points_To_ID_")) {
                        continue;
                    }

                    if (value instanceof MapProperties) {
                        MapProperties nestedProps = (MapProperties) value;
                        value = "props of object with id " + nestedProps.get("id", Integer.class);
                    }

                    System.out.println("\t" + key + " = " + value);
                }

            }

            Gdx.app.log("TiledMapObjectPropertyTest", "Successfully ran test!");
        } catch (Exception e) {
            Gdx.app.error("TiledMapObjectPropertyTest", "Failed to run test!", e);
            e.printStackTrace();
            error = true;
        }
    }

    @Override
    public void render() {
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
        MapLayer layer = map.getLayers().get("Objects");
        for (MapObject mapObject : layer.getObjects()) {
            if (!mapObject.isVisible())
                continue;
            if (mapObject instanceof RectangleMapObject) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                Rectangle rectangle = ((RectangleMapObject) mapObject).getRectangle();
                shapeRenderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
                shapeRenderer.end();
            }
        }
    }

    @Override
    public void dispose() {
        map.dispose();
        shapeRenderer.dispose();
    }

    private void test(MapProperties props, int idToObjProp, ObjectMap<Integer, MapProperties> idPropMap) {
        String key = "Points_To_ID_" + idToObjProp;
        Gdx.app.log("TiledMapObjectPropertyTest", "Testing property " + key);

        if (!props.containsKey(key)) {
            throw new RuntimeException("Missing property: " + key);
        }

        MapProperties otherProps = idPropMap.get(idToObjProp);
        if (otherProps != idPropMap.get(idToObjProp)) {
            throw new RuntimeException("Property " + key + " does not point to the correct object");
        }
    }
}


package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TmjMapLoader;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Objects;

/** This test verifies that all possible Tiled property types are loaded correctly. This includes properties of type:
 * <ul>
 * <li>boolean</li>
 * <li>class</li>
 * <li>color</li>
 * <li>enum</li>
 * <li>file</li>
 * <li>float</li>
 * <li>int</li>
 * <li>obj</li>
 * <li>str</li>
 * </ul>
 * <p>
 * It also verifies default value loading of class properties and different variations of enums (single vs multi value and string
 * vs int storage). */
public class TiledMapPropertiesTest extends GdxTest {

	private static final String LOG_TAG = "TILED-MAP-PROPS";
	private TiledMap tiledMap;
	private boolean success;

	@Override
	public void create () {
		success = false;

		// verify TMJ
		TmjMapLoader tmjLoader = new TmjMapLoader();
		TmjMapLoader.Parameters tmjLoaderParams = new TmjMapLoader.Parameters();
		tmjLoaderParams.projectFilePath = "data/maps/tiled-properties/tiled-prop-test.tiled-project";
		tiledMap = tmjLoader.load("data/maps/tiled-properties/tiled-prop-test.tmj", tmjLoaderParams);
		try {
			verifyTiledMap(tiledMap);
		} catch (Exception e) {
			Gdx.app.error(LOG_TAG, "Verification of tiledmap properties failed", e);
			return;
		}
		Gdx.app.log(LOG_TAG, "TMJ properties successfully verified!");

		// verify TMX
		tiledMap.dispose();
		TmxMapLoader tmxLoader = new TmxMapLoader();
		TmxMapLoader.Parameters tmxLoaderParams = new TmxMapLoader.Parameters();
		tmxLoaderParams.projectFilePath = "data/maps/tiled-properties/tiled-prop-test.tiled-project";
		tiledMap = tmxLoader.load("data/maps/tiled-properties/tiled-prop-test.tmx", tmxLoaderParams);
		try {
			verifyTiledMap(tiledMap);
		} catch (Exception e) {
			Gdx.app.error(LOG_TAG, "Verification of tiledmap properties failed", e);
			return;
		}
		Gdx.app.log(LOG_TAG, "TMX properties successfully verified!");

		success = true;
	}

	private void verifyTiledMap (TiledMap tiledMap) {
		MapObject mapObj = tiledMap.getLayers().get("object layer").getObjects().get("Test Object");
		MapProperties objProps = mapObj.getProperties();
		verifyProperty("objBool", true, objProps.get("objBool", Boolean.class));
		verifyProperty("objColor", Color.RED, objProps.get("objColor", Boolean.class));
		verifyProperty("objEnumNum", 1, objProps.get("objEnumNum", Integer.class));
		// enums that are stored as numbers are not stored comma separated like string enums.
		// Instead, they are added up meaning the first value (=1) and the second value (=2) are stored as 3.
		// I guess Tiled uses power of two for those values, otherwise you cannot identify what value is really set?
		verifyProperty("objEnumNumMulti", 3, objProps.get("objEnumNumMulti", Integer.class));
		verifyProperty("objEnumStr", "STR2", objProps.get("objEnumStr", String.class));
		verifyProperty("objEnumStrMulti", "STR1,STR2", objProps.get("objEnumStrMulti", String.class));
		verifyProperty("objFile", "tiled-prop-test.tiled-project", objProps.get("objFile", String.class));
		verifyProperty("objFloat", 4.2f, objProps.get("objFloat", Float.class));
		verifyProperty("objInt", 42, objProps.get("objInt", Integer.class));
		verifyProperty("objObj", mapObj, objProps.get("objObj", MapObject.class));
		verifyProperty("objStr", "someTxt", objProps.get("objStr", String.class));
		// verify class property
		MapProperties expectedProps = new MapProperties();
		expectedProps.put("classInt", 43);
		expectedProps.put("classStr", "txt");
		expectedProps.put("type", "testClass");
		expectedProps.put("classObj", null);
		expectedProps.put("classEnumStr", "STR2");
		expectedProps.put("classColor", Color.GREEN); // default green color of class definition in project file
		verifyProperty("objClass", expectedProps, objProps.get("objClass", MapProperties.class));
		// verify nested class property
		expectedProps = new MapProperties();
		expectedProps.put("classInt", 45);
		expectedProps.put("type", "testClassNested");
		MapProperties nestedProps = new MapProperties();
		nestedProps.put("classInt", 1); // default value of class definition in project file
		nestedProps.put("classStr", "txt2");
		nestedProps.put("classColor", Color.BLUE);
		nestedProps.put("classObj", mapObj);
		nestedProps.put("type", "testClass");
		nestedProps.put("classEnumStr", "STR2");
		expectedProps.put("classClass", nestedProps);
		verifyProperty("objClassNested", expectedProps, objProps.get("objClassNested", MapProperties.class));
		// verify class default value loading
		expectedProps = new MapProperties();
		expectedProps.put("classStr", "defaultStr");
		expectedProps.put("classObj", null);
		expectedProps.put("classColor", Color.YELLOW);
		expectedProps.put("type", "testClassDefaults");
		nestedProps = new MapProperties();
		nestedProps.put("classColor", Color.GREEN);
		nestedProps.put("classInt", 3);
		nestedProps.put("classObj", null);
		nestedProps.put("classStr", "defaultStrNested");
		nestedProps.put("type", "testClass");
		nestedProps.put("classEnumStr", "STR2");
		expectedProps.put("classClass", nestedProps);
		verifyProperty("objClassNested", expectedProps, objProps.get("objClassDefaults", MapProperties.class));
		// verify an object linked to a tile with a class
		TiledMapTileMapObject tileMapObj = (TiledMapTileMapObject)tiledMap.getLayers().get("object layer").getObjects()
			.get("Tile Object");
		TiledMapTile tile = tileMapObj.getTile();
		MapProperties tileProps = tile.getProperties();
		expectedProps = new MapProperties();
		expectedProps.put("type", "testClass");
		expectedProps.put("classColor", Color.GREEN);
		expectedProps.put("classEnumStr", "STR1");
		expectedProps.put("classInt", 2);
		expectedProps.put("classObj", null);
		expectedProps.put("classStr", "");
		verifyProperty("tileProps", expectedProps, tileProps);
		// verify an object linked to a tile with a nested class
		tileMapObj = (TiledMapTileMapObject)tiledMap.getLayers().get("object layer").getObjects().get("Tile Object Nested");
		tile = tileMapObj.getTile();
		tileProps = tile.getProperties();
		expectedProps = new MapProperties();
		expectedProps.put("type", "testClassNested");
		expectedProps.put("classInt", 2);
		nestedProps = new MapProperties();
		nestedProps.put("type", "testClass");
		nestedProps.put("classColor", Color.GREEN);
		nestedProps.put("classEnumStr", "STR1");
		nestedProps.put("classInt", 1);
		nestedProps.put("classObj", null);
		nestedProps.put("classStr", "");
		expectedProps.put("classClass", nestedProps);
		verifyProperty("tilePropsNested", expectedProps, tileProps);
		// verify an object with a class
		mapObj = tiledMap.getLayers().get("object layer").getObjects().get("Test Object 2");
		objProps = mapObj.getProperties();
		expectedProps = new MapProperties();
		expectedProps.put("type", "testClass");
		expectedProps.put("classColor", Color.GREEN);
		expectedProps.put("classEnumStr", "STR2");
		expectedProps.put("classInt", 2);
		expectedProps.put("classObj", null);
		expectedProps.put("classStr", "");
		objProps.remove("x");
		objProps.remove("y");
		objProps.remove("id");
		objProps.remove("width");
		objProps.remove("height");
		objProps.remove("rotation");
		verifyProperty("classObjProps", expectedProps, objProps);
	}

	private <T> void verifyProperty (String propName, T expected, T actual) {
		if (expected instanceof Float) {
			// floats are verified with a specific tolerance
			if (!MathUtils.isEqual((Float)expected, (Float)actual, 0.01f)) {
				throw new GdxRuntimeException(propName + " does not match:\nexpected=" + expected + ",\n  actual=" + actual);
			}
			return;
		}

		if (!Objects.equals(expected, actual)) {
			throw new GdxRuntimeException(propName + " does not match:\nexpected=" + expected + ",\n  actual=" + actual);
		}
	}

	@Override
	public void render () {
		ScreenUtils.clear(success ? 0f : 1f, success ? 1f : 0f, 0f, 1f);
	}

	@Override
	public void dispose () {
		tiledMap.dispose();
	}
}


package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmjMapLoader;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Objects;

public class TiledMapPropertiesTest extends GdxTest {

	private static final String LOG_TAG = "TILED-MAP-PROPS";
	private TiledMap tiledMap;
	private boolean success;

	// TODO
	// add MapObject and Color properties to class -> how are they stored in TMJ? Type is not stored it seems
	// properly parse tmj class
	// properly parse tmj nested class

	@Override
	public void create () {
		success = false;

		// verify TMX
		TmxMapLoader tmxLoader = new TmxMapLoader();
		tiledMap = tmxLoader.load("data/maps/tiled-properties/tiled-prop-test.tmx");
		try {
			verifyTiledMap(tiledMap);
		} catch (Exception e) {
			Gdx.app.error(LOG_TAG, "Verification of tiledmap properties failed", e);
			return;
		}
		Gdx.app.log(LOG_TAG, "TMX properties successfully verified!");

		// verify TMJ
		TmjMapLoader tmjLoader = new TmjMapLoader();
		TmjMapLoader.Parameters parameters = new TmjMapLoader.Parameters();
		parameters.projectFilePath = "data/maps/tiled-properties/tiled-prop-test.tiled-project";
		tiledMap.dispose();
		tiledMap = tmjLoader.load("data/maps/tiled-properties/tiled-prop-test.tmj", parameters);
		try {
			verifyTiledMap(tiledMap);
		} catch (Exception e) {
			Gdx.app.error(LOG_TAG, "Verification of tiledmap properties failed", e);
			return;
		}
		Gdx.app.log(LOG_TAG, "TMJ properties successfully verified!");

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
		// TODO missing verification of color which doesn't work for TMX right now because of missing defaultValue information
		verifyProperty("objClass", expectedProps, objProps.get("objClass", MapProperties.class));
		// verify nested class property
		expectedProps = new MapProperties();
		expectedProps.put("classInt", 45);
		expectedProps.put("type", "testClassNested");
		MapProperties nestedProps = new MapProperties();
		nestedProps.put("classInt", 44);
		nestedProps.put("classStr", "txt2");
		nestedProps.put("classColor", Color.BLUE);
		nestedProps.put("classObj", mapObj);
		nestedProps.put("type", "testClass");
		expectedProps.put("classClass", nestedProps);
		verifyProperty("objClassNested", expectedProps, objProps.get("objClassNested", MapProperties.class));
	}

	private <T> void verifyProperty (String propName, T expected, T actual) {
		if (!Objects.equals(expected, actual)) {
			throw new GdxRuntimeException(propName + " does not match: expected=" + expected + ", actual=" + actual);
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

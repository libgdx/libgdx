package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.loaders.json.JsonModelLoader;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.tests.utils.GdxTest;

public class JsonModelLoaderTest extends GdxTest {

	@Override
	public void create () {
		JsonModelLoader loader = new JsonModelLoader();
		loader.load(Gdx.files.internal("data/g3d/test.g3dj"), null);
	}

	@Override
	public void render () {
		
	}
}

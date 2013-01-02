package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.loaders.json.JsonModelLoader;
import com.badlogic.gdx.tests.utils.GdxTest;

public class JsonModelLoaderTest extends GdxTest {
	@Override
	public void create () {
		new JsonModelLoader().load(Gdx.files.internal("data/g3d/simple.json"), null);
	}

	@Override
	public void render () {
	}
}

package com.badlogic.gdx.tests;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ShapeRendererTest extends GdxTest {

	@Override
	public boolean needsGL20 () {
		return false;
	}

	ShapeRenderer renderer;
	
	public void create() {
		renderer = new ShapeRenderer();
	}
	
	public void render() {
		
	}
}

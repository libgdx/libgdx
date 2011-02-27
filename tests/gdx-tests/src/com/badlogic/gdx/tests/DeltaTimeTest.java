package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.utils.GdxTest;

public class DeltaTimeTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	long lastFrameTime;
	
	@Override public void create() {
		lastFrameTime = System.nanoTime();
	}
	
	@Override public void render() {
		long frameTime = System.nanoTime();
		float deltaTime = (frameTime - lastFrameTime) / 1000000000.0f;		
		lastFrameTime = frameTime;
		
		Gdx.app.log("DeltaTimeTest", "delta: " + deltaTime + ", gdx delta: " + Gdx.graphics.getDeltaTime());
	}
}

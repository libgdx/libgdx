package com.badlogic.gdx.tests.utils;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

public class OrthoCamController extends InputAdapter {
	final OrthographicCamera camera;
	final Vector2 curr = new Vector2();
	final Vector2 last = new Vector2(-1, -1);	
	final Vector2 delta = new Vector2();
	
	public OrthoCamController(OrthographicCamera camera) {
		this.camera = camera;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		camera.getScreenToWorld(x, y, curr);
		if(!(last.x == -1 && last.y == -1)) {
			camera.getScreenToWorld(last.x, last.y, delta);
			delta.sub(curr);
			camera.getPosition().add(delta.x, delta.y, 0);
		}
		last.set(x, y);
		return false;
	}
	
	@Override public boolean touchUp(int x, int y, int pointer) {
		last.set(-1, -1);
		return false;
	}
}

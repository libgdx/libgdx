/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.tests.utils.GdxTest;

public class IsoCamTest extends GdxTest {	
	private static final int TARGET_WIDTH = 480;
	private static final float UNIT_TO_PIXEL = TARGET_WIDTH * 0.15f;
	Texture texture;
	OrthographicCamera cam;
	SpriteBatch batch;	
	final Sprite[][] sprites = new Sprite[10][10];
	final Matrix4 matrix = new Matrix4();
	
	@Override public void create() {
		texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		float unitsOnX = (float)Math.sqrt(2) * TARGET_WIDTH / (UNIT_TO_PIXEL);
		float pixelsOnX = Gdx.graphics.getWidth() / unitsOnX;
		float unitsOnY = Gdx.graphics.getHeight() / pixelsOnX;
		cam = new OrthographicCamera(unitsOnX, unitsOnY, 25);
		cam.position.mul(30);
		cam.near = 1;
		cam.far = 1000;		
		matrix.setToRotation(new Vector3(1, 0, 0), 90);
		
		for(int z = 0; z < 10; z++) {
			for(int x = 0; x < 10; x++) {
				sprites[x][z] = new Sprite(texture);
				sprites[x][z].setPosition(x,z);
				sprites[x][z].setSize(1, 1);
			}
		}
		
		batch = new SpriteBatch();
		
		Gdx.input.setInputProcessor(new IsoCamController(cam));
	}
	
	@Override public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		cam.update();		
		
		batch.setProjectionMatrix(cam.combined);
		batch.setTransformMatrix(matrix);
		batch.begin();
		for(int z = 0; z < 10; z++) {
			for(int x = 0; x < 10; x++) {
				sprites[x][z].draw(batch);
			}
		}
		batch.end();
		
		checkTileTouched();
	}
		
	final Plane xzPlane = new Plane(new Vector3(0, 1, 0), 0);
	final Vector3 intersection = new Vector3();
	Sprite lastSelectedTile = null;
	
	private void checkTileTouched() {
		if(Gdx.input.justTouched()) {
			Ray pickRay = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
			Intersector.intersectRayPlane(pickRay, xzPlane, intersection);
			System.out.println(intersection);
			int x = (int)intersection.x;
			int z = (int)intersection.z;
			if(x >= 0 && x < 10 && z >= 0 && z < 10) {
				if(lastSelectedTile != null) lastSelectedTile.setColor(1, 1, 1, 1);
				Sprite sprite = sprites[x][z];
				sprite.setColor(1, 0, 0, 1);
				lastSelectedTile = sprite;
			}
		}
	}
	
	public class IsoCamController extends InputAdapter {
		final Plane xzPlane = new Plane(new Vector3(0, 1, 0), 0);
		final Vector3 intersection = new Vector3();
		final Vector3 curr = new Vector3();
		final Vector3 last = new Vector3(-1, -1, -1);
		final Vector3 delta = new Vector3();
		final Camera camera;
		
		public IsoCamController(Camera camera) {
			this.camera = camera;
		}
		
		@Override public boolean touchDragged (int x, int y, int pointer) {
			Ray pickRay = camera.getPickRay(x, y);
			Intersector.intersectRayPlane(pickRay, xzPlane, curr);
			
			if(!(last.x == -1 && last.y == -1 && last.z == -1)) {
				pickRay = camera.getPickRay(last.x, last.y);
				Intersector.intersectRayPlane(pickRay, xzPlane, delta);			
				delta.sub(curr);
				camera.position.add(delta.x, 0, delta.z);
			}
			last.set(x, y, 0);
			return false;
		}
		
		@Override public boolean touchUp(int x, int y, int pointer, int button) {
			last.set(-1, -1, -1);
			return false;
		}
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}

}

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

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.tests.utils.GdxTest;

public class PickingTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	static final int BORDER = 20;
	static final int VP_X = BORDER;
	static final int VP_Y = BORDER * 2;
	static int VP_WIDTH;
	static int VP_HEIGHT;
	Mesh sphere;
	Camera cam;	
	Vector3[] positions = new Vector3[100];
	ImmediateModeRenderer10 renderer;
	SpriteBatch batch;
	Texture logo;
	
	@Override public void create() {
		VP_WIDTH = Gdx.graphics.getWidth() - 4 * BORDER;
		VP_HEIGHT = Gdx.graphics.getHeight() - 4 * BORDER;
		sphere = ObjLoader.loadObj(Gdx.files.internal("data/sphere.obj").read());
		cam = new PerspectiveCamera(45, VP_WIDTH, VP_HEIGHT);
//		cam = new OrthographicCamera(10, 10);
		cam.far = 200;
		batch = new SpriteBatch();
		logo = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		Random rand = new Random(10);
		for(int i = 0; i < positions.length; i++) {
			positions[i] = new Vector3(rand.nextFloat() * 100 - rand.nextFloat() * 100, 
												rand.nextFloat() * 100 - rand.nextFloat() * 100, 
												rand.nextFloat() * 100 - rand.nextFloat() * 100);
		}		
		positions[0].set(0, 0, -10);
		renderer = new ImmediateModeRenderer10();
	}

	Vector3 intersection = new Vector3();
	@Override public void render() {
		GL10 gl = Gdx.gl10;
				
		gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
		cam.update();
		cam.apply(gl);
		gl.glViewport(VP_X, VP_Y, VP_WIDTH, VP_HEIGHT);
		
		Ray pickRay = null;
		if(Gdx.input.isTouched()) {
			pickRay = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY(), VP_X, VP_Y, VP_WIDTH, VP_HEIGHT);
//			Gdx.app.log("PickingTest", "ray: " + pickRay);
		}
		
		boolean intersected = false;
		for(int i = 0; i < positions.length; i++) {
			if(pickRay != null && Intersector.intersectRaySphere(pickRay, positions[i], 1, intersection)) {
				gl.glColor4f(1, 0, 0, 1);
				intersected = true;
			} else {
				gl.glColor4f(1, 1, 1, 1);
			}
			gl.glPushMatrix();
			gl.glTranslatef(positions[i].x, positions[i].y, positions[i].z);
			sphere.render(GL10.GL_TRIANGLES);
			gl.glPopMatrix();
		}
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		if(intersected) {			
			cam.project(intersection, VP_X, VP_Y, VP_WIDTH, VP_HEIGHT);
			batch.draw(logo, intersection.x, intersection.y);			
		}
		batch.end();
		
		renderer.begin(GL10.GL_LINE_LOOP);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(VP_X, VP_Y, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(VP_X + VP_WIDTH, VP_Y, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(VP_X + VP_WIDTH, VP_Y + VP_HEIGHT, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(VP_X, VP_Y + VP_HEIGHT, 0);
		renderer.end();
		
		if(Gdx.input.isKeyPressed(Keys.A))
			cam.rotate(20 * Gdx.graphics.getDeltaTime(), 0, 1, 0);
		if(Gdx.input.isKeyPressed(Keys.D))	
			cam.rotate(-20 * Gdx.graphics.getDeltaTime(), 0, 1, 0);					
	}	
}

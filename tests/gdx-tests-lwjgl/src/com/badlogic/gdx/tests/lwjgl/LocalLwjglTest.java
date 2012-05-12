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
package com.badlogic.gdx.tests.lwjgl;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;

public class LocalLwjglTest extends ApplicationAdapter {
	ShapeRenderer renderer;
	OrthographicCamera camera;
	float[] coords = {-2.0f, 0.0f, -2.0f, 0.5f, 0.0f, 1.0f, 0.5f, 2.875f, 1.0f,0.5f, 1.5f,1.0f, 2.0f,1.0f, 2.0f,0.0f};
	private List<Vector2> triangles;
	
	@Override
	public void create () {
		renderer = new ShapeRenderer();
		camera = new OrthographicCamera(10, 10);
		camera.position.set(0, 0, 0);
		camera.update();
		
		List<Vector2> poly = new ArrayList<Vector2>();
		for(int i = 0; i < coords.length; i+=2) {
			poly.add(new Vector2(coords[i], coords[i+1]));
		}
		triangles = new EarClippingTriangulator().computeTriangles(poly);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl10.glColor4f(1, 1, 1, 1);
		
		renderer.setProjectionMatrix(camera.combined);
		renderer.setColor(1, 1, 1, 1);
		renderer.begin(ShapeType.Line);
		for(int j = 0; j < coords.length - 2; j+=2) {
			renderer.line(coords[j], coords[j+1], coords[j+2], coords[j+3]);			
		}
		renderer.line(coords[0], coords[1], coords[coords.length - 2], coords[coords.length - 1]);
		renderer.end();
		
		renderer.setColor(1, 0, 0, 1);
		renderer.translate(0, -4, 0);
		renderer.begin(ShapeType.Triangle);
		for(int i = 0; i < triangles.size(); i+=3) {
			Vector2 v1 = triangles.get(i);
			Vector2 v2 = triangles.get(i+1);
			Vector2 v3 = triangles.get(i+2);
			renderer.triangle(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
		}
		renderer.end();
		renderer.identity();
	}

	public static void main (String[] argv) {
		new LwjglApplication(new LocalLwjglTest(), "test", 480, 320, false);
	}
}
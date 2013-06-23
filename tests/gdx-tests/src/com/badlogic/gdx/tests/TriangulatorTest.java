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

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.EarClippingTriangulator.CollinearTrackingLevel;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TriangulatorTest extends GdxTest {

   private ShapeRenderer renderer;
	private List<Vector2> vertices;
	private Vector3 mouse = new Vector3();
	private EarClippingTriangulator triangulator;
	private OrthographicCamera cam;
	private boolean triangulated = false;

	/** left mouse button for add a new vertex
   * middle mouse button for remove last vertex
   * right mouse button for change view ( triangles / standart view ) */

	@Override
	public void create () {
		renderer = new ShapeRenderer();
		vertices = new ArrayList<Vector2>(5) {
			{
				// put your vertices for test here
				/*add(new Vector2(3.914329f, 1.9008259f));
				add(new Vector2(4.414321f, 1.903619f));
				add(new Vector2(4.8973203f, 1.9063174f));
				add(new Vector2(5.4979978f, 1.9096732f));*/
			}
		};
		triangulator = new EarClippingTriangulator();
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		cam.apply(Gdx.gl10);
		cam.update();
		
		renderer.setProjectionMatrix(cam.combined);
		renderer.begin(ShapeType.Line);

		if (!triangulated) {
			if (vertices.size() < 1) {
				renderer.end();
				return;
			}
			for (int i = 0; i < vertices.size() - 1; i++)
				renderer.line(vertices.get(i).x, vertices.get(i).y, vertices.get(i + 1).x, vertices.get(i + 1).y);
			renderer.line(vertices.get(0).x, vertices.get(0).y, vertices.get(vertices.size() - 1).x,
				vertices.get(vertices.size() - 1).y);
		} else {
			List<Vector2> v = triangulator.getTriangulatedPolygon();
			for (int i = 0; i < v.size(); i += 3)
				renderer.triangle(v.get(i).x, v.get(i).y, v.get(i + 1).x, v.get(i + 1).y, v.get(i + 2).x, v.get(i + 2).y);
		}
		renderer.end();
		
		if (triangulated) return;
		
		renderer.begin(ShapeType.Filled);

		 for (int i = 0; i < vertices.size(); i++)
			renderer.circle(vertices.get(i).x, vertices.get(i).y, 5f);

		renderer.end();

	}

	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (pointer > 0) return false;

		switch (button) {
		case (Buttons.MIDDLE): {
			if (vertices.size() >= 1) vertices.remove(vertices.size() - 1);
			break;
		}
		case (Buttons.RIGHT): {
			if (triangulated)
				triangulated = false;
			else {
				triangulated = true;
				try {
					triangulator.computeTriangles(vertices, CollinearTrackingLevel.STRONG);
					System.out.println("Created with "+triangulator.getTriangulatedPolygon().size()/3+" triangles");
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
			break;
		}
		case (Buttons.LEFT): {
			triangulated = false;
			mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			cam.unproject(mouse);
			vertices.add(new Vector2(mouse.x, mouse.y));
			break;
		}
		}

		return true;
	}

	@Override
	public void resize (int width, int height) {
		cam = new OrthographicCamera(width, height);
	}
	
	@Override
	public boolean scrolled (int amount) {
		cam.zoom -= amount*0.01f;
		return false;
	}
	
	public boolean keyDown (int keycode) {
		float speed = 5;
		if(keycode == Keys.UP)
			cam.translate(0, speed);
		if(keycode == Keys.LEFT)
			cam.translate(-speed, 0);
		if(keycode == Keys.RIGHT)
			cam.translate(speed, 0);
		if(keycode == Keys.DOWN)
			cam.translate(0, -speed);
		return false;
	}
	
	@Override
	public boolean needsGL20 () {
		return false;
	}

}
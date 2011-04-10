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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MyFirstTriangle extends GdxTest {
	private Mesh mesh;

	@Override public void create () {
		if (mesh == null) {
			mesh = new Mesh(true, 3, 3, new VertexAttribute(Usage.Position, 3, "a_position"));

			mesh.setVertices(new float[] {-0.5f, -0.5f, 0, 0.5f, -0.5f, 0, 0, 0.5f, 0});
			mesh.setIndices(new short[] {0, 1, 2});
		}
	}

	@Override public void dispose () {
	}

	@Override public void pause () {
	}

	int renderCount = 0;
	@Override public void render () {
		renderCount++;
		Gdx.app.log("RenderCountTest", String.valueOf(renderCount));
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
		mesh.render(GL10.GL_TRIANGLES, 0, 3);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Gdx.app.log("RenderCountTest", e.toString());
		}	
	}

	@Override public void resize (int width, int height) {
	}

	@Override public void resume () {
	}

	@Override public boolean needsGL20 () {
		return false;
	}
}

/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.ModelLoader;

public class PerformanceTest implements RenderListener {
	boolean fixed = false;

	Mesh fpMesh;
	Mesh flMesh;

	long startTime = System.nanoTime();
	int frames = 0;

	@Override public void dispose () {

	}

	@Override public void render () {
		Gdx.graphics.getGL10().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Mesh m = null;
		if (fixed)
			m = fpMesh;
		else
			m = flMesh;

		for (int i = 0; i < 10; i++) {
			m.render(GL10.GL_TRIANGLES);
		}

		if ((System.nanoTime() - startTime) > 1000000000) {
			Gdx.app.log("Performance", frames + " fps, " + (m.getNumVertices() / 3) * frames * 10 + " tris/s");
			frames = 0;
			startTime = System.nanoTime();
		}
		frames++;
	}

	@Override public void surfaceChanged (int width, int height) {

	}

	@Override public void surfaceCreated () {
		if (fpMesh == null) {
			fpMesh = ModelLoader.loadObj(Gdx.files.readFile("data/heavysphere.obj", FileType.Internal), false);
			flMesh = ModelLoader.loadObj(Gdx.files.readFile("data/heavysphere.obj", FileType.Internal), false);
		}
	}

}

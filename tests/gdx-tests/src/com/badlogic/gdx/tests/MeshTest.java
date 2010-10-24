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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class MeshTest implements RenderListener {
	Mesh mesh;
	Texture texture;

	@Override public void dispose () {
		// TODO Auto-generated method stub

	}

	@Override public void render () {
		Gdx.graphics.getGL10().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.graphics.getGL10().glEnable(GL10.GL_TEXTURE_2D);
		texture.bind();
		mesh.render(GL10.GL_TRIANGLES, 0, 3);
	}

	@Override public void surfaceChanged (int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override public void surfaceCreated () {
		if (mesh == null) {
			mesh = new Mesh(true, false, 3, 3, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(
				Usage.ColorPacked, 4, "a_color"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoords"));

			mesh.setVertices(new float[] {-0.5f, -0.5f, 0, Color.toFloatBits(255, 0, 0, 255), 0, 0, 0.5f, -0.5f, 0,
				Color.toFloatBits(0, 255, 0, 255), 1, 0, 0, 0.5f, 0, Color.toFloatBits(0, 0, 255, 255), 0.5f, 1});
			mesh.setIndices(new short[] {0, 1, 2});

			texture = Gdx.graphics.newTexture(Gdx.files.getFileHandle("data/badlogic.jpg", FileType.Internal), TextureFilter.MipMap,
				TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		}
	}

}

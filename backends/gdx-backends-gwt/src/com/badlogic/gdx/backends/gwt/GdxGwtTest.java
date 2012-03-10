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

package com.badlogic.gdx.backends.gwt;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Label;

public class GdxGwtTest extends GwtApplication implements ApplicationListener {
	ShaderProgram shader;
	Mesh mesh;
	Matrix4 matrix = new Matrix4();
	SpriteBatch batch;
	Texture texture;
	List<Vector2> positions;
	private Label label;
	Sprite sprite;

	@Override
	public GwtApplicationConfiguration getConfig () {
		return new GwtApplicationConfiguration(640, 640);
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return this;
	}

	@Override
	public void create () {
		label = new Label("fps:");
		Document.get().getBody().appendChild(label.getElement());

		String vertexShader = "attribute vec4 a_position;\n" + "attribute vec2 a_texCoord0;\n" + "uniform mat4 u_projView;\n"
			+ "varying vec4 v_color;\n" + "varying vec2 v_texCoord;\n" + "void main() {\n" + "v_color = vec4(1, 1, 1, 1);\n"
			+ "v_texCoord = a_texCoord0;\n" + "gl_Position = u_projView * a_position;\n" + "}\n";
		String fragmentShader = "#ifdef GL_ES\n" + "precision mediump float;\n" + "#endif\n" + "uniform sampler2D u_texture;\n"
			+ "varying vec2 v_texCoord;\n" + "varying vec4 v_color;\n" + "void main() {\n"
			+ "gl_FragColor = v_color * texture2D(u_texture, v_texCoord);\n" + "}\n";
		shader = new ShaderProgram(vertexShader, fragmentShader);
		if (!shader.isCompiled()) throw new GdxRuntimeException(shader.getLog());
		mesh = new Mesh(VertexDataType.VertexBufferObject, true, 6, 0, VertexAttribute.Position(), VertexAttribute.TexCoords(0));
		mesh.setVertices(new float[] {-0.5f, -0.5f, 0, 0, 1, 0.5f, -0.5f, 0, 1, 1, 0.5f, 0.5f, 0, 1, 0, 0.5f, 0.5f, 0, 1, 0, -0.5f,
			0.5f, 0, 0, 0, -0.5f, -0.5f, 0, 0, 1});

		Pixmap pixmap = new Pixmap(32, 32, Format.RGBA8888);
		pixmap.setColor(0, 0, 1, 1);
		pixmap.fill();
		pixmap.setColor(1, 0, 0, 1);
		pixmap.drawLine(0, 0, 32, 32);
		pixmap.drawLine(32, 0, 0, 32);
		texture = new Texture(pixmap);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		pixmap.dispose();

		final ImageElement img = createImage();
		img.setSrc(GWT.getHostPageBaseURL() + "badlogic.jpg");
		hookOnLoad(img, new EventHandler() {
			@Override
			public void onEvent (NativeEvent e) {
				// Load image data into the texture object once it's loaded.
				texture.dispose();
				Pixmap pixmap = new Pixmap(img);
				texture = new Texture(pixmap);
				texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
				pixmap.dispose();
				sprite = new Sprite(texture);
				sprite.setSize(64, 64);
				sprite.setOrigin(32, 32);
			}
		});

		batch = new SpriteBatch();
		positions = new ArrayList<Vector2>();
		for (int i = 0; i < 100; i++) {
			positions.add(new Vector2(MathUtils.random() * Gdx.graphics.getWidth(), MathUtils.random() * Gdx.graphics.getHeight()));
		}
	}
	
	public static interface EventHandler {
		public void onEvent(NativeEvent e);
	}
	
	private static native ImageElement createImage () /*-{
		return new Image();
	}-*/;
	
	private static native void hookOnLoad(ImageElement img, EventHandler h) /*-{
   img.addEventListener('load', function(e) {
     h.@com.badlogic.gdx.backends.gwt.GdxGwtTest.EventHandler::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);
   }, false);
 	}-*/;

	@Override
	public void resume () {
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);
		texture.bind(0);
		shader.begin();
		shader.setUniformMatrix("u_projView", matrix);
		shader.setUniformi("u_texture", 0);
		mesh.render(shader, GL20.GL_TRIANGLES);
		shader.end();

		if(sprite != null) {
			batch.begin();
			sprite.rotate(Gdx.graphics.getDeltaTime() * 45);
			for(Vector2 position: positions) {
				sprite.setPosition(position.x, position.y);
				sprite.draw(batch);
			}
			batch.end();
		}
		label.setText("fps:" + Gdx.graphics.getFramesPerSecond() + ", delta: " + Gdx.graphics.getDeltaTime());
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void pause () {
	}

	@Override
	public void dispose () {
	}
}

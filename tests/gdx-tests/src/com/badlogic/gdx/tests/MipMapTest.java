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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.MipMapGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.PerspectiveCamController;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class MipMapTest extends GdxTest {
	PerspectiveCamera camera;
	PerspectiveCamController controller;
	Mesh mesh;
	Texture textureHW;
	Texture textureSW;
	Texture currTexture;
	ShaderProgram shader;
	Stage ui;
	Skin skin;
	InputMultiplexer multiplexer;
	SelectBox<String> minFilter;
	SelectBox<String> magFilter;
	CheckBox hwMipMap;

	@Override
	public void create () {
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0, 1.5f, 1.5f);
		camera.lookAt(0, 0, 0);
		camera.update();
		controller = new PerspectiveCamController(camera);

		mesh = new Mesh(true, 4, 4, new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(
			Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
		mesh.setVertices(new float[] {-1, 0, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, -1, 1, 0, -1, 0, -1, 0, 0,});
		mesh.setIndices(new short[] {0, 1, 2, 3});

		shader = new ShaderProgram(Gdx.files.internal("data/shaders/flattex-vert.glsl").readString(), Gdx.files.internal(
			"data/shaders/flattex-frag.glsl").readString());
		if (!shader.isCompiled()) throw new GdxRuntimeException("shader error: " + shader.getLog());

		textureHW = new Texture(Gdx.files.internal("data/badlogic.jpg"), Format.RGB565, true);
		MipMapGenerator.setUseHardwareMipMap(false);
		textureSW = new Texture(Gdx.files.internal("data/badlogic.jpg"), Format.RGB565, true);
		currTexture = textureHW;

		createUI();

		multiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(multiplexer);
		multiplexer.addProcessor(ui);
		multiplexer.addProcessor(controller);
	}

	private void createUI () {
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		ui = new Stage();

		String[] filters = new String[TextureFilter.values().length];
		int idx = 0;
		for (TextureFilter filter : TextureFilter.values()) {
			filters[idx++] = filter.toString();
		}
		hwMipMap = new CheckBox("Hardware Mips", skin);
		minFilter = new SelectBox(skin);
		minFilter.setItems(filters);
		magFilter = new SelectBox(skin.get(SelectBoxStyle.class));
		magFilter.setItems("Nearest", "Linear");

		Table table = new Table();
		table.setSize(ui.getWidth(), 30);
		table.setY(ui.getHeight() - 30);
		table.add(hwMipMap).spaceRight(5);
		table.add(new Label("Min Filter", skin)).spaceRight(5);
		table.add(minFilter).spaceRight(5);
		table.add(new Label("Mag Filter", skin)).spaceRight(5);
		table.add(magFilter);

		ui.addActor(table);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);

		camera.update();

		currTexture = hwMipMap.isChecked() ? textureHW : textureSW;
		currTexture.bind();
		currTexture.setFilter(TextureFilter.valueOf(minFilter.getSelected()), TextureFilter.valueOf(magFilter.getSelected()));

		shader.bind();
		shader.setUniformMatrix("u_projTrans", camera.combined);
		shader.setUniformi("s_texture", 0);
		mesh.render(shader, GL20.GL_TRIANGLE_FAN);

		ui.act();
		ui.draw();
	}

	@Override
	public void dispose () {
		shader.dispose();
		textureHW.dispose();
		textureSW.dispose();
		mesh.dispose();
		ui.dispose();
		skin.dispose();
	}
}

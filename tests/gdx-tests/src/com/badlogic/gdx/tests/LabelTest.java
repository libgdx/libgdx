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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LabelTest extends GdxTest {
	Skin skin;
	Stage stage;
	SpriteBatch batch;
	Actor root;
	ShapeRenderer renderer;

	@Override
	public void create () {
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		skin.getAtlas().getTextures().iterator().next().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		skin.getFont("default-font").getData().markupEnabled = true;
		float scale = 1;
		skin.getFont("default-font").getData().setScale(scale);
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		Table table = new Table();
		stage.addActor(table);
		table.setPosition(200, 65);

		table.debug();
		table.add(new Label("This is regular text.", skin));
		table.row();
		table.add(new Label("This is regular text\nwith a newline.", skin));
		table.row();
		Label label3 = new Label("This is [RED]regular text\n\nwith newlines,\naligned bottom, right.", skin);
		label3.setColor(Color.GREEN);
		label3.setAlignment(Align.bottom | Align.right);
		table.add(label3).minWidth(200 * scale).minHeight(110 * scale).fill();
		table.row();
		Label label4 = new Label("This is regular text with NO newlines, wrap enabled and aligned bottom, right.", skin);
		label4.setWrap(true);
		label4.setAlignment(Align.bottom | Align.right);
		table.add(label4).minWidth(200 * scale).minHeight(110 * scale).fill();
		table.row();
		Label label5 = new Label("This is regular text with\n\nnewlines, wrap\nenabled and aligned bottom, right.", skin);
		label5.setWrap(true);
		label5.setAlignment(Align.bottom | Align.right);
		table.add(label5).minWidth(200 * scale).minHeight(110 * scale).fill();

		table.pack();
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();

		float x = 40, y = 40;

		BitmapFont font = skin.getFont("default-font");
		batch.begin();
		font.draw(batch, "The quick brown fox jumped over the lazy cow.", x, y);
		batch.end();

		drawLine(x, y - font.getDescent(), x + 1000, y - font.getDescent());
		drawLine(x, y - font.getCapHeight() + font.getDescent(), x + 1000, y - font.getCapHeight() + font.getDescent());
	}

	public void drawLine (float x1, float y1, float x2, float y2) {
		renderer.setProjectionMatrix(batch.getProjectionMatrix());
		renderer.begin(ShapeType.Line);
		renderer.line(x1, y1, x2, y2);
		renderer.end();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}
}

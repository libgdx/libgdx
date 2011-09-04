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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class LabelTest extends GdxTest {
	Skin skin;
	Stage stage;
	SpriteBatch batch;
	Actor root;
	ImmediateModeRenderer10 renderer;

	@Override
	public void create () {
		batch = new SpriteBatch();
		renderer = new ImmediateModeRenderer10();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"), Gdx.files.internal("data/uiskin.png"));
		skin.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		stage = new Stage(0, 0, false);
		Gdx.input.setInputProcessor(stage);

		Gdx.gl10.glColor4f(1, 0, 0, 0);

		Table table = new Table();
		stage.addActor(table);
		table.x = table.y = 100;

		table.debug();
		table.add(new Label(null, "This is regular text.", skin.getStyle("default", LabelStyle.class)));
		table.row();
		table.add(new Label(null, "This is regular text\nwith a newline.", skin.getStyle("default", LabelStyle.class)));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
		Table.drawDebug(stage);

		float x = 40, y = 40;

		BitmapFont font = skin.getResource("default-font", BitmapFont.class);
		batch.begin();
		font.draw(batch, "The quick brown fox jumped over the lazy cow.", x, y);
		batch.end();

		drawLine(x, y- font.getDescent(), x + 1000, y- font.getDescent());
		drawLine(x, y - font.getCapHeight() + font.getDescent(), x + 1000, y - font.getCapHeight() + font.getDescent());
	}

	public void drawLine (float x1, float y1, float x2, float y2) {
		renderer.begin(batch.getProjectionMatrix(), GL10.GL_LINES);
		renderer.vertex(x1, y1, 0);
		renderer.vertex(x2, y2, 0);
		renderer.end();
	}

	@Override
	public void resize (int width, int height) {
		stage.setViewport(width, height, false);
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}
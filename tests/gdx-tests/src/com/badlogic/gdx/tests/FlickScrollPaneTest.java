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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.FlickScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class FlickScrollPaneTest extends GdxTest {
	private Stage stage;
	private BitmapFont font;
	private Table container;

	public void create () {
		stage = new Stage(0, 0, false);
		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
		Gdx.input.setInputProcessor(stage);

		Gdx.graphics.setVSync(false);

		container = new Table();
		stage.addActor(container);
		container.getTableLayout().debug();

		Table table = new Table();

		FlickScrollPane scroll = new FlickScrollPane(table);
		container.add(scroll).expand().fill();

		table.parse("pad:10 * expand:x space:4");
		for (int i = 0; i < 100; i++) {
			table.row();
			table.add(new Label(i + "uno", new LabelStyle(font, Color.RED))).expandX().fillX();
			table.add(new Label(i + "dos", new LabelStyle(font, Color.RED)));
			table.add(new Label(i + "tres long0 long1 long2 long3 long4 long5 long6 long7 long8 long9 long10 long11 long12",
				new LabelStyle(font, Color.RED)));
		}

		container.getTableLayout().row();
		container.getTableLayout().add(new Label("stuff at bottom!", new LabelStyle(font, Color.WHITE))).pad(20, 20, 20, 20);
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		Table.drawDebug(stage);
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, false);
		container.width = width;
		container.height = height;
	}

	public void dispose () {
		stage.dispose();
		font.dispose();
	}

	public boolean needsGL20 () {
		return false;
	}
}

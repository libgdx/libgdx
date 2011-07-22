
package com.badlogic.gdx.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.FlickScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class FlickScrollPaneTest extends GdxTest {
	private Stage stage;
	private BitmapFont font;
	private Container container;

	public void create () {
		stage = new Stage(0, 0, false);
		font = new BitmapFont();
		Gdx.input.setInputProcessor(stage);

		container = new Container(null, 0, 0);
		stage.addActor(container);
		// container.layout.debug = "all";

		Container table = new Container(null, 0, 0);

		FlickScrollPane scroll = new FlickScrollPane(null, stage, table, 0, 0);
		container.add(scroll).expand(true, true).fill(true, true);

		table.layout.parse("pad:10 * expand:x space:4");
		for (int i = 0; i < 100; i++) {
			table.row();
			table.add(new Label(null, i + "uno", new LabelStyle(font, Color.RED)));
			table.add(new Label(null, i + "dos", new LabelStyle(font, Color.RED)));
			table.add(new Label(null, i + "tres long0 long1 long2 long3 long4 long5 long6 long7 long8 long9", new LabelStyle(font,
				Color.RED)));
		}

		container.row();
		container.add(new Label(null, "stuff at bottom!", new LabelStyle(font, Color.WHITE))).pad(20, 20, 20, 20);
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.draw();
		Table.drawDebug(stage);
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, false);
		container.setPrefSize(width, height);
	}

	public void pause () {
	}

	public void resume () {
	}

	public void dispose () {
	}

	public boolean needsGL20 () {
		return false;
	}
}

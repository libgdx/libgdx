
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Scaling;

public class ImageTest extends GdxTest {
	Skin skin;
	Stage ui;
	Table root;
	TextureRegion image2;

	@Override
	public void create () {
		skin = new Skin(Gdx.files.internal("data/uiskin.json"), Gdx.files.internal("data/uiskin.png"));
		image2 = new TextureRegion(new Texture(Gdx.files.internal("data/badlogic.jpg")));
		ui = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		Gdx.input.setInputProcessor(ui);

		root = new Table();
		ui.addActor(root);
		root.debug();

		Image image = new Image(image2, Scaling.fill);
		root.add(image).width(160).height(100);
	}

	@Override
	public void dispose () {
		ui.dispose();
		skin.dispose();
		image2.getTexture().dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		ui.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		ui.draw();
		Table.drawDebug(ui);
	}

	@Override
	public void resize (int width, int height) {
		ui.setViewport(width, height, false);
		root.width = width;
		root.height = height;
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}

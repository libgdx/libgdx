
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GroupCullingTest extends GdxTest {
	static private final int count = 100;

	private Stage stage;
	private Table root;
	private Label drawnLabel;
	int drawn;

	public void create () {
		stage = new Stage(0, 0, true);
		Gdx.input.setInputProcessor(stage);

		root = new Table();
		stage.addActor(root);

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"), Gdx.files.internal("data/uiskin.png"));

		Table labels = new Table();
		root.add(new ScrollPane(labels, skin)).expand().fill();
		root.row();
		root.add(drawnLabel = new Label("", skin));

		for (int i = 0; i < count; i++) {
			labels.add(new Label("Label: " + i, skin) {
				public void draw (SpriteBatch batch, float parentAlpha) {
					super.draw(batch, parentAlpha);
					drawn++;
				}
			});
			labels.row();
		}
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, true);
		root.width = width;
		root.height = height;
		root.invalidate();
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		drawn = 0;
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		drawnLabel.setText("Drawn: " + drawn + "/" + count);
		drawnLabel.invalidateHierarchy();
	}

	public boolean needsGL20 () {
		return false;
	}
}

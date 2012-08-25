
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TreeTest extends GdxTest {
	Stage stage;

	public void create () {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		Table table = new Table();
		table.setFillParent(true);
		stage.addActor(table);

		final Tree tree = new Tree(skin);

		final Node moo1 = new Node(new TextButton("moo1", skin));
		final Node moo2 = new Node(new TextButton("moo2", skin));
		final Node moo3 = new Node(new TextButton("moo3", skin));
		final Node moo4 = new Node(new TextButton("moo4", skin));
		final Node moo5 = new Node(new TextButton("moo5", skin));
		tree.add(moo1);
		tree.add(moo2);
		moo2.add(moo3);
		moo3.add(moo4);
		tree.add(moo5);

		moo5.getActor().addListener(new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				tree.remove(moo4);
			}
		});

		table.add(tree).fill().expand();
	}

	public void render () {
		// System.out.println(meow.getValue());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		Table.drawDebug(stage);
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, true);
	}

	public boolean needsGL20 () {
		return true;
	}

	public void dispose () {
		stage.dispose();
	}
}

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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;

public class TreeTest extends GdxTest {
	Stage stage;
	Skin skin;
	Tree<Node, String> tree;
	private Label label;

	class Node extends Tree.Node<Node, String, TextButton> {
		public Node (String text) {
			super(new TextButton(text, skin));
			setValue(text);
		}
	}

	public void create () {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		label = new Label("", skin);
		stage.addActor(label);

		Table table = new Table();
		table.setFillParent(true);
		stage.addActor(table);

		tree = new Tree(skin);
		tree.setPadding(10);
		tree.setIndentSpacing(25);
		tree.setIconSpacing(5, 0);
		final Node moo1 = new Node("moo1 (add to moo2)");
		final Node moo2 = new Node("moo2 (moo3 to bottom)");
		final Node moo3 = new Node("moo3");
		final Node moo4 = new Node("moo4");
		final Node moo5 = new Node("moo5 (remove moo4)");
		tree.add(moo1);
		tree.add(moo2);
		moo2.add(moo3);
		moo3.add(moo4);
		tree.add(moo5);

		moo1.getActor().addListener(new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				System.out.println(moo1.getActor().getText() + ", " + moo1.getValue() + ", " + moo1.getValue().length());
				Node node = new Node("added " + moo2.getChildren().size);
				node.add(new Node("1"));
				node.add(new Node("2"));
				node.setExpanded(MathUtils.randomBoolean());
				moo2.insert(MathUtils.randomBoolean() ? moo2.getChildren().size : MathUtils.random(0, moo2.getChildren().size), node);
			}
		});
		moo2.getActor().addListener(new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				moo2.getChildren().removeValue(moo3, true);
				moo2.getChildren().add(moo3);
				moo2.updateChildren();
			}
		});
		moo5.getActor().addListener(new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				Node node = tree.findNode("moo4");
				if (node != null) node.remove();
			}
		});

		table.add(tree).fill().expand();
	}

	public void render () {
		// System.out.println(meow.getValue());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

		label.setText(tree.toString());
		label.pack();
		label.setPosition(0, 0, Align.bottomLeft);
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	public void dispose () {
		stage.dispose();
	}
}

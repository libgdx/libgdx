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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.SplitViewport;
import com.badlogic.gdx.utils.viewport.SplitViewport.SizeInformation;
import com.badlogic.gdx.utils.viewport.SplitViewport.SizeType;
import com.badlogic.gdx.utils.viewport.SplitViewport.SubView;
import com.badlogic.gdx.utils.viewport.Viewport;

/** This test is supposed to show the capabilities of the {@link SplitViewport}. It is not exemplary for its usage in a standard
 * scenario. Usually, there is no need to switch the viewport in this automated way.
 * @author Daniel Holderbaum */
public class SplitViewportTest extends GdxTest {

	private class SubViewInformation {
		public Stage stage;
		public Array<Viewport> viewports;
		public SubView subView;

		public SubViewInformation (Stage stage, Array<Viewport> viewports, SubView subView) {
			this.stage = stage;
			this.viewports = viewports;
			this.subView = subView;
		}
	}

	SplitViewport splitViewport;
	Array<Viewport> rootViewports;

	Array<String> names;

	Array<Array<SubViewInformation>> subViews = new Array<Array<SubViewInformation>>();

	public void create () {
		rootViewports = ViewportTest1.getViewports(new OrthographicCamera());
		names = ViewportTest1.getViewportNames();

		splitViewport = new SplitViewport(rootViewports.first());

		// first row
		splitViewport.row(new SizeInformation(SizeType.REST, 0f));
		subViews.add(new Array<SubViewInformation>());

		SubViewInformation subView00 = createSubView(new SizeInformation(SizeType.ABSOLUTE, 300f));
		subViews.get(0).add(subView00);
		splitViewport.add(subView00.subView);

		SubViewInformation subView01 = createSubView(new SizeInformation(SizeType.REST, 0));
		subViews.get(0).add(subView01);
		splitViewport.add(subView01.subView);

		SubViewInformation subView02 = createSubView(new SizeInformation(SizeType.RELATIVE, 0.25f));
		subViews.get(0).add(subView02);
		splitViewport.add(subView02.subView);

		// second row
		splitViewport.row(new SizeInformation(SizeType.ABSOLUTE, 200f));
		subViews.add(new Array<SubViewInformation>());

		SubViewInformation subView10 = createSubView(new SizeInformation(SizeType.REST, 0));
		subViews.get(1).add(subView10);
		splitViewport.add(subView10.subView);

		InputMultiplexer inputMultiplexer = new InputMultiplexer();

		// create an input processor to switch the root viewport
		InputProcessor rootViewportSwitcher = new InputAdapter() {
			public boolean keyDown (int keycode) {
				if (keycode == Input.Keys.SPACE) {
					int index = (rootViewports.indexOf(splitViewport.getRootViewport(), true) + 1) % rootViewports.size;
					Viewport viewport = rootViewports.get(index);
					splitViewport.setRootViewport(viewport);
					System.out.println("Root viewport: " + names.get(index));
					resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				}
				return true;
			}
		};
		inputMultiplexer.addProcessor(rootViewportSwitcher);

		// add all stages to the multiplexer
		for (int row = 0; row < subViews.size; row++) {
			for (int col = 0; col < subViews.get(row).size; col++) {
				SubViewInformation subViewInformation = subViews.get(row).get(col);
				inputMultiplexer.addProcessor(subViewInformation.stage);
			}
		}

		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	private SubViewInformation createSubView (SizeInformation sizeInformation) {
		final Stage stage = new Stage();
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		final Array<Viewport> viewports = ViewportTest1.getViewports(stage.getCamera());
		int viewportStartIndex = 5;
		Viewport viewport = viewports.get(viewportStartIndex);
		stage.setViewport(viewport);
		final Label label = new Label(names.get(viewportStartIndex), skin);
		final SubView subView = new SubView(sizeInformation, viewport);

		final Table root = new Table(skin);
		root.setFillParent(true);
		root.setBackground(skin.getDrawable("default-pane"));
		root.debug().defaults().space(6);

		TextButton smallerButton = new TextButton("-5 World Size", skin);

		smallerButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				subView.viewport.setWorldSize(subView.viewport.getWorldWidth() - 5, subView.viewport.getWorldHeight() - 5);
			}
		});

		TextButton biggerButton = new TextButton("+5 World Size", skin);
		biggerButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				subView.viewport.setWorldSize(subView.viewport.getWorldWidth() + 5, subView.viewport.getWorldHeight() + 5);
			}
		});

		root.add(smallerButton);
		root.add(biggerButton).row();

		TextButton changeViewportButton = new TextButton("Press to change the viewport", skin);
		changeViewportButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				int index = (viewports.indexOf(stage.getViewport(), true) + 1) % viewports.size;
				label.setText(names.get(index));
				Viewport viewport = viewports.get(index);
				subView.viewport = viewport;
				stage.setViewport(viewport);
			}
		});
		root.add(changeViewportButton).colspan(2).row();

		root.add(label).colspan(2);
		stage.addActor(root);

		return new SubViewInformation(stage, viewports, subView);
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		for (int row = 0; row < subViews.size; row++) {
			for (int col = 0; col < subViews.get(row).size; col++) {
				SubViewInformation subViewInformation = subViews.get(row).get(col);
				splitViewport.activateSubViewport(row, col, true);
				subViewInformation.stage.act();
				subViewInformation.stage.draw();
			}
		}
	}

	public void resize (int width, int height) {
		splitViewport.update(width, height);
	}

	public void dispose () {
		for (int row = 0; row < subViews.size; row++) {
			for (int col = 0; col < subViews.get(row).size; col++) {
				SubViewInformation subViewInformation = subViews.get(row).get(col);
				subViewInformation.stage.dispose();
			}
		}
	}

}

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

package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

public abstract class BaseG3dHudTest extends BaseG3dTest {
	public final static int PREF_HUDWIDTH = 640;
	public final static int PREF_HUDHEIGHT = 480;
	public final static float rotationSpeed = 0.02f * 360f; // degrees per second
	public final static float moveSpeed = 0.25f; // cycles per second

	protected Stage hud;
	protected float hudWidth, hudHeight;
	protected Skin skin;
	protected Label fpsLabel;
	protected CollapsableWindow modelsWindow;
	protected CheckBox gridCheckBox, rotateCheckBox, moveCheckBox;
	protected final StringBuilder stringBuilder = new StringBuilder();
	protected final Matrix4 transform = new Matrix4();
	protected float moveRadius = 2f;

	protected String models[] = new String[] {"car.obj", "cube.obj", "scene.obj", "scene2.obj", "wheel.obj", "g3d/invaders.g3dj",
		"g3d/head.g3db", "g3d/knight.g3dj", "g3d/knight.g3db", "g3d/monkey.g3db", "g3d/ship.obj", "g3d/shapes/cube_1.0x1.0.g3dj",
		"g3d/shapes/cube_1.5x1.5.g3dj", "g3d/shapes/sphere.g3dj", "g3d/shapes/teapot.g3dj", "g3d/shapes/torus.g3dj"};

	@Override
	public void create () {
		super.create();

		createHUD();

		Gdx.input.setInputProcessor(new InputMultiplexer(hud, this, inputController));
	}

	protected void createHUD () {
		hud = new Stage(new ScalingViewport(Scaling.fit, PREF_HUDWIDTH, PREF_HUDHEIGHT));
		hudWidth = hud.getWidth();
		hudHeight = hud.getHeight();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		final List<String> modelsList = new List(skin);
		modelsList.setItems(models);
		modelsList.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!modelsWindow.isCollapsed() && getTapCount() == 2) {
					onModelClicked(modelsList.getSelected());
					modelsWindow.collapse();
				}
			}
		});
		modelsWindow = addListWindow("Models", modelsList, 0, -1);

		fpsLabel = new Label("FPS: 999", skin);
		hud.addActor(fpsLabel);
		gridCheckBox = new CheckBox("Show grid", skin);
		gridCheckBox.setChecked(showAxes);
		gridCheckBox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				showAxes = gridCheckBox.isChecked();
			}
		});
		gridCheckBox.setPosition(hudWidth - gridCheckBox.getWidth(), 0);
		hud.addActor(gridCheckBox);

		rotateCheckBox = new CheckBox("Rotate", skin);
		rotateCheckBox.setChecked(true);
		rotateCheckBox.setPosition(hudWidth - rotateCheckBox.getWidth(), gridCheckBox.getHeight());
		hud.addActor(rotateCheckBox);

		moveCheckBox = new CheckBox("Move", skin);
		moveCheckBox.setChecked(false);
		moveCheckBox.setPosition(hudWidth - moveCheckBox.getWidth(), rotateCheckBox.getTop());
		hud.addActor(moveCheckBox);
	}

	protected CollapsableWindow addListWindow (String title, List list, float x, float y) {
		CollapsableWindow window = new CollapsableWindow(title, skin);
		window.row();
		ScrollPane pane = new ScrollPane(list, skin);
		pane.setFadeScrollBars(false);
		window.add(pane);
		window.pack();
		window.pack();
		if (window.getHeight() > hudHeight) {
			window.setHeight(hudHeight);
		}
		window.setX(x < 0 ? hudWidth - (window.getWidth() - (x + 1)) : x);
		window.setY(y < 0 ? hudHeight - (window.getHeight() - (y + 1)) : y);
		window.layout();
		window.collapse();
		hud.addActor(window);
		pane.setScrollX(0);
		pane.setScrollY(0);
		return window;
	}

	protected abstract void onModelClicked (final String name);

	protected void getStatus (final StringBuilder stringBuilder) {
		stringBuilder.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
		if (loading) stringBuilder.append(" loading...");
	}

	protected float rotation, movement;

	@Override
	public void render () {
		transform.idt();
		if (rotateCheckBox.isChecked())
			transform.rotate(Vector3.Y, rotation = (rotation + rotationSpeed * Gdx.graphics.getRawDeltaTime()) % 360);
		if (moveCheckBox.isChecked()) {
			movement = (movement + moveSpeed * Gdx.graphics.getRawDeltaTime()) % 1f;
			final float sm = MathUtils.sin(movement * MathUtils.PI2);
			final float cm = MathUtils.cos(movement * MathUtils.PI2);
			transform.trn(0, moveRadius * cm, moveRadius * sm);
		}

		super.render();

		stringBuilder.setLength(0);
		getStatus(stringBuilder);
		fpsLabel.setText(stringBuilder);
		hud.act(Gdx.graphics.getDeltaTime());
		hud.draw();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		hud.getViewport().update(width, height, true);
		hudWidth = hud.getWidth();
		hudHeight = hud.getHeight();
	}

	@Override
	public void dispose () {
		super.dispose();
		skin.dispose();
		skin = null;
	}

	/** Double click title to expand/collapse */
	public static class CollapsableWindow extends Window {
		private boolean collapsed;
		private float collapseHeight = 20f;
		private float expandHeight;

		public CollapsableWindow (String title, Skin skin) {
			super(title, skin);
			addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					if (getTapCount() == 2 && getHeight() - y <= getPadTop() && y < getHeight() && x > 0 && x < getWidth())
						toggleCollapsed();
				}
			});
		}

		public void expand () {
			if (!collapsed) return;
			setHeight(expandHeight);
			setY(getY() - expandHeight + collapseHeight);
			collapsed = false;
		}

		public void collapse () {
			if (collapsed) return;
			expandHeight = getHeight();
			setHeight(collapseHeight);
			setY(getY() + expandHeight - collapseHeight);
			collapsed = true;
			if (getStage() != null) getStage().setScrollFocus(null);
		}

		public void toggleCollapsed () {
			if (collapsed)
				expand();
			else
				collapse();
		}

		public boolean isCollapsed () {
			return collapsed;
		}
	}
}

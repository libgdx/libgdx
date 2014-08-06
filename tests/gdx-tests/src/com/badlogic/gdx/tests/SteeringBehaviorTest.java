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

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.AIUtils;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.ai.steer.SteeringTest;
import com.badlogic.gdx.tests.ai.steer.tests.ArriveTest;
import com.badlogic.gdx.tests.ai.steer.tests.CollisionAvoidanceTest;
import com.badlogic.gdx.tests.ai.steer.tests.FaceTest;
import com.badlogic.gdx.tests.ai.steer.tests.FlockingTest;
import com.badlogic.gdx.tests.ai.steer.tests.FollowPathTest;
import com.badlogic.gdx.tests.ai.steer.tests.HideTest;
import com.badlogic.gdx.tests.ai.steer.tests.InterposeTest;
import com.badlogic.gdx.tests.ai.steer.tests.LookWhereYouAreGoingTest;
import com.badlogic.gdx.tests.ai.steer.tests.PursueTest;
import com.badlogic.gdx.tests.ai.steer.tests.RaycastObstacleAvoidanceTest;
import com.badlogic.gdx.tests.ai.steer.tests.SeekTest;
import com.badlogic.gdx.tests.ai.steer.tests.WanderTest;
import com.badlogic.gdx.tests.g3d.BaseG3dHudTest.CollapsableWindow;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.TimeUtils;

public class SteeringBehaviorTest extends GdxTest {
	
	private static final boolean DEBUG_STAGE = false;
	
	public CollapsableWindow behaviorsWindow;
	Label fpsLabel;
	StringBuilder fpsStringBuilder;
	
	SteeringTest[] behaviors = {
		new ArriveTest(this),
		new CollisionAvoidanceTest(this),
		new FaceTest(this),
		new FlockingTest(this),
		new FollowPathTest(this),
		new HideTest(this),
		new InterposeTest(this),
		new LookWhereYouAreGoingTest(this),
		new PursueTest(this),
		new RaycastObstacleAvoidanceTest(this),
		new SeekTest(this),
		new WanderTest(this)
	};
	
	Table behaviorTable;
	SteeringTest currentBehavior;

	public Stage stage;
	public float stageWidth;
	public float stageHeight;
	public Skin skin;
	String behaviorNames[];

	public TextureRegion greenFish;
	public TextureRegion cloud;
	public TextureRegion badlogicSmall;
	public TextureRegion target;

	private int previousFrameId;
	private long lastConflictTime;

	@Override
	public void create () {
		Gdx.gl.glClearColor(.3f, .3f, .3f, 1);

		previousFrameId = AIUtils.getFrameId();
		lastConflictTime = TimeUtils.nanoTime();

		fpsStringBuilder = new StringBuilder();

		greenFish = new TextureRegion(new Texture("data/green_fish.png"));
		cloud = new TextureRegion(new Texture("data/particle-cloud.png"));
		badlogicSmall = new TextureRegion(new Texture("data/badlogicsmall.jpg"));
		target = new TextureRegion(new Texture("data/target.png"));

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		stage = new Stage();
		stage.setDebugAll(DEBUG_STAGE);
		stageWidth = stage.getWidth();
		stageHeight = stage.getHeight();

		Gdx.input.setInputProcessor(new InputMultiplexer(stage));

		Stack stack = new Stack();
		stage.addActor(stack);
		stack.setSize(stageWidth, stageHeight);
		behaviorTable = new Table();
		stack.add(behaviorTable);
		
		// Create behavior names
		behaviorNames = new String[behaviors.length];
		for (int i = 0; i < behaviors.length; i++) {
			behaviorNames[i] = behaviors[i].name;
		}
		Arrays.sort(behaviorNames);
		
		final List<String> behaviorsList = new List(skin);
		behaviorsList.setItems(behaviorNames);
		behaviorsList.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!behaviorsWindow.isCollapsed() && getTapCount() == 2) {
					changeBehavior(behaviorsList.getSelectedIndex());
					behaviorsWindow.collapse();
				}
			}
		});
		behaviorsWindow = addListWindow("Behaviors", behaviorsList, 0, -1);
		
		changeBehavior(0);
		
		fpsLabel = new Label("FPS: 999", skin);
		stage.addActor(fpsLabel);
	}
	
	@Override
	public void render () {
		if (AIUtils.getFrameId() == previousFrameId) {
			long now = TimeUtils.nanoTime();
			float elapsed = (now - lastConflictTime) / 1000000000f;
			Gdx.app.log("SteeringBehaviorTest", "FrameId conflict after " + elapsed + " seconds.");
			lastConflictTime = now;
		}
		else {
			previousFrameId = AIUtils.getFrameId();
		}
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		fpsStringBuilder.setLength(0);
		getStatus(fpsStringBuilder);
		fpsLabel.setText(fpsStringBuilder);

		if (currentBehavior != null)
			currentBehavior.render();
		
		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		stage.getViewport().update(width, height, true);
		stageWidth = stage.getWidth();
		stageHeight = stage.getHeight();
	}

	@Override
	public void dispose () {
		if (currentBehavior != null)
			currentBehavior.dispose();
		
		stage.dispose();
		skin.dispose();

		// Dispose textures
		greenFish.getTexture().dispose();
		cloud.getTexture().dispose();
		badlogicSmall.getTexture().dispose();
		target.getTexture().dispose();
	}

	protected void getStatus (final StringBuilder stringBuilder) {
		stringBuilder.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
	}

	protected CollapsableWindow addListWindow (String title, List list, float x, float y) {
		CollapsableWindow window = new CollapsableWindow(title, skin);
		window.row();
		ScrollPane pane = new ScrollPane(list, skin);
		pane.setFadeScrollBars(false);
		window.add(pane);
		window.pack();
		window.pack();
		if (window.getHeight() > stage.getHeight()) {
			window.setHeight(stage.getHeight());
		}
		window.setX(x < 0 ? stage.getWidth() - (window.getWidth() - (x + 1)) : x);
		window.setY(y < 0 ? stage.getHeight() - (window.getHeight() - (y + 1)) : y);
		window.layout();
		window.collapse();
		stage.addActor(window);
		pane.setScrollX(0);
		pane.setScrollY(0);
		return window;
	}
	
	void changeBehavior (int selectedIndex) {
		// Remove the old behavior and its window
		behaviorTable.clear();
		if (currentBehavior != null) {
			if (currentBehavior.getDetailWindow() != null)
				currentBehavior.getDetailWindow().remove();
			currentBehavior.dispose();
		}

		// Add the new behavior and its window
		currentBehavior = behaviors[selectedIndex];
		currentBehavior.create(behaviorTable);
		InputMultiplexer im = (InputMultiplexer)Gdx.input.getInputProcessor();
		if (im.size() > 1) im.removeProcessor(1);
		if (currentBehavior.getInputProcessor() != null) im.addProcessor(currentBehavior.getInputProcessor());
		if (currentBehavior.getDetailWindow() != null)
			stage.addActor(currentBehavior.getDetailWindow());
	}

}

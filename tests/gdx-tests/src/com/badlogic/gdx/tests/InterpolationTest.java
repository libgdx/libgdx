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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class InterpolationTest extends GdxTest {
	Stage stage;
	private Skin skin;
	private Table table;
	List<String> list;
	String interpolationNames[], selectedInterpolation;
	private ShapeRenderer renderer;
	float graphSize, steps, time = 0, duration = 2.5f;
	Vector2 startPosition = new Vector2(), targetPosition = new Vector2(), position = new Vector2();

	/** resets {@link #startPosition} and {@link #targetPosition} */
	void resetPositions () {
		startPosition.set(stage.getWidth() - stage.getWidth() / 5f, stage.getHeight() - stage.getHeight() / 5f);
		targetPosition.set(startPosition.x, stage.getHeight() / 5f);
	}

	/** @return the {@link #position} with the {@link #selectedInterpolation interpolation} applied */
	Vector2 getPosition (float time) {
		position.set(targetPosition);
		position.sub(startPosition);
		position.scl(getInterpolation(selectedInterpolation).apply(time / duration));
		position.add(startPosition);
		return position;
	}

	/** @return the {@link #selectedInterpolation selected} interpolation */
	private Interpolation getInterpolation (String name) {
		try {
			return (Interpolation)ClassReflection.getField(Interpolation.class, name).get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void create () {
		Gdx.gl.glClearColor(.3f, .3f, .3f, 1);
		renderer = new ShapeRenderer();

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		stage = new Stage(new ScreenViewport());
		resetPositions();

		Field[] interpolationFields = ClassReflection.getFields(Interpolation.class);

		// see how many fields are actually interpolations (for safety; other fields may be added with future)
		int interpolationMembers = 0;
		for (int i = 0; i < interpolationFields.length; i++)
			if (ClassReflection.isAssignableFrom(Interpolation.class, interpolationFields[i].getDeclaringClass()))
				interpolationMembers++;

		// get interpolation names
		interpolationNames = new String[interpolationMembers];
		for (int i = 0; i < interpolationFields.length; i++)
			if (ClassReflection.isAssignableFrom(Interpolation.class, interpolationFields[i].getDeclaringClass()))
				interpolationNames[i] = interpolationFields[i].getName();
		selectedInterpolation = interpolationNames[0];

		list = new List(skin);
		list.setItems(interpolationNames);
		list.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				selectedInterpolation = list.getSelected();
				time = 0;
				resetPositions();
			}
		});

		ScrollPane scroll = new ScrollPane(list, skin);
		scroll.setFadeScrollBars(false);
		scroll.setScrollingDisabled(true, false);

		table = new Table();
		table.setFillParent(true);
		table.add(scroll).expandX().left().width(100);
		stage.addActor(table);

		Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {
			public boolean scrolled (int amount) {
				if (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) return false;
				duration -= amount / 15f;
				duration = MathUtils.clamp(duration, 0, Float.POSITIVE_INFINITY);
				return true;
			}

		}, stage, new InputAdapter() {
			public boolean touchDown (int screenX, int screenY, int pointer, int button) {
				if (!Float.isNaN(time)) // if "walking" was interrupted by this touch down event
					startPosition.set(getPosition(time)); // set startPosition to the current position
				targetPosition.set(stage.screenToStageCoordinates(targetPosition.set(screenX, screenY)));
				time = 0;
				return true;
			}

		}));
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float bottomLeftX = Gdx.graphics.getWidth() / 2 - graphSize / 2, bottomLeftY = Gdx.graphics.getHeight() / 2 - graphSize / 2;

		// only show up to two decimals
		String text = String.valueOf(duration);
		if (text.length() > 4) text = text.substring(0, text.lastIndexOf('.') + 3);
		text = "duration: " + text + " s (ctrl + scroll to change)";
		stage.getBatch().begin();
		list.getStyle().font.draw(stage.getBatch(), text, bottomLeftX + graphSize / 2, bottomLeftY + graphSize
			+ list.getStyle().font.getLineHeight(), 0, Align.center, false);
		stage.getBatch().end();

		renderer.begin(ShapeType.Line);
		renderer.rect(bottomLeftX, bottomLeftY, graphSize, graphSize); // graph bounds
		float lastX = bottomLeftX, lastY = bottomLeftY;
		for (float step = 0; step <= steps; step++) {
			Interpolation interpolation = getInterpolation(selectedInterpolation);
			float percent = step / steps;
			float x = bottomLeftX + graphSize * percent, y = bottomLeftY + graphSize * interpolation.apply(percent);
			renderer.line(lastX, lastY, x, y);
			lastX = x;
			lastY = y;
		}
		time += Gdx.graphics.getDeltaTime();
		if (time > duration) {
			time = Float.NaN; // stop "walking"
			startPosition.set(targetPosition); // set startPosition to targetPosition for next click
		}
		// draw time marker
		renderer.line(bottomLeftX + graphSize * time / duration, bottomLeftY, bottomLeftX + graphSize * time / duration,
			bottomLeftY + graphSize);
		// draw path
		renderer.setColor(Color.GRAY);
		renderer.line(startPosition, targetPosition);
		renderer.setColor(Color.WHITE);
		renderer.end();

		// draw the position
		renderer.begin(ShapeType.Filled);
		if (!Float.isNaN(time)) // don't mess up position if time is NaN
			getPosition(time);
		renderer.circle(position.x, position.y, 7);
		renderer.end();

		stage.act();
		stage.draw();
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
		table.invalidateHierarchy();

		renderer.setProjectionMatrix(stage.getViewport().getCamera().combined);

		graphSize = 0.75f * Math.min(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
		steps = graphSize * 0.5f;
	}

	public void dispose () {
		stage.dispose();
		skin.dispose();
	}
}

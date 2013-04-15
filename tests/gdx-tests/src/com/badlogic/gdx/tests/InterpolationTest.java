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
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class InterpolationTest extends GdxTest {
	static private final String[] interpolators = new String[] {"bounce", "bounceIn", "bounceOut", "circle", "circleIn",
		"circleOut", "elastic", "elasticIn", "elasticOut", "exp10", "exp10In", "exp10Out", "exp5", "exp5In", "exp5Out", "fade",
		"linear", "pow2", "pow2In", "pow2Out", "pow3", "pow3In", "pow3Out", "pow4", "pow4In", "pow4Out", "pow5", "pow5In",
		"pow5Out", "sine", "sineIn", "sineOut", "swing", "swingIn", "swingOut"};

	private Stage stage;
	private Table root;
	private List list;
	private ShapeRenderer renderer;
	Vector2 position = new Vector2(300, 20);
	Vector2 targetPosition = new Vector2(position);
	Vector2 temp = new Vector2();
	float timer;

	public void create () {
		renderer = new ShapeRenderer();

		stage = new Stage(0, 0, true);
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputAdapter() {
			public boolean touchDown (int x, int y, int pointer, int button) {
				Vector2 current = getCurrentPosition();
				position.set(current);
				targetPosition.set(x - 10, Gdx.graphics.getHeight() - y - 10);
				timer = 0;
				return true;
			}
		}));

		root = new Table();
		stage.addActor(root);
		root.pad(10).top().left();

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		list = new List(interpolators, skin);
		ScrollPane scrollPane = new ScrollPane(list, skin);
		scrollPane.setOverscroll(false, false);
		scrollPane.setFadeScrollBars(false);
		root.add(scrollPane).expandY().fillY().prefWidth(110);
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, true);
		root.setSize(width, height);
		root.invalidate();
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		int steps = 100;
		int size = 200;
		int x = Gdx.graphics.getWidth() / 2 - size / 2;
		int y = Gdx.graphics.getHeight() / 2 - size / 2;

		renderer.setProjectionMatrix(stage.getCamera().combined);

		renderer.begin(ShapeType.Line);
		renderer.box(x, y, 0, size, size, 0);
		renderer.end();

		Interpolation interpolation = getInterpolation();
		float lastX = x, lastY = y;
		renderer.begin(ShapeType.Line);
		for (int i = 0; i <= steps; i++) {
			float alpha = i / (float)steps;
			float lineX = x + size * alpha;
			float lineY = y + size * interpolation.apply(alpha);
			renderer.line(lastX, lastY, lineX, lineY);
			lastX = lineX;
			lastY = lineY;
		}
		renderer.end();

		timer += Gdx.graphics.getDeltaTime();
		Vector2 current = getCurrentPosition();
		renderer.begin(ShapeType.Filled);
		renderer.rect(current.x, current.y, 20, 20);
		renderer.end();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	Vector2 getCurrentPosition () {
		temp.set(targetPosition);
		temp.sub(position);
		temp.scl(getInterpolation().apply(Math.min(1, timer / 1f)));
		temp.add(position);
		return temp;
	}

	private Interpolation getInterpolation () {
		try {
			return (Interpolation)Interpolation.class.getField(list.getSelection()).get(null);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public boolean needsGL20 () {
		return false;
	}
}

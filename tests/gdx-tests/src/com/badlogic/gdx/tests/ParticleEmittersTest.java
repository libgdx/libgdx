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
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class ParticleEmittersTest extends GdxTest {
	private SpriteBatch spriteBatch;
	ParticleEffect effect;
	ParticleEffectPool effectPool;
	Array<PooledEffect> effects = new Array();
	PooledEffect latestEffect;
	float fpsCounter;
	Stage ui;
	CheckBox skipCleanup;
	Button clearEmitters;
	Label logLabel;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();

		effect = new ParticleEffect();
		effect.load(Gdx.files.internal("data/singleTextureAllAdditive.p"), Gdx.files.internal("data"));
		effect.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		effectPool = new ParticleEffectPool(effect, 20, 20);

		setupUI();

		InputProcessor inputProcessor = new InputAdapter() {

			public boolean touchDragged (int x, int y, int pointer) {
				if (latestEffect != null) latestEffect.setPosition(x, Gdx.graphics.getHeight() - y);
				return false;
			}

			public boolean touchDown (int x, int y, int pointer, int newParam) {
				latestEffect = effectPool.obtain();
				latestEffect.setEmittersCleanUpBlendFunction(!skipCleanup.isChecked());
				latestEffect.setPosition(x, Gdx.graphics.getHeight() - y);
				effects.add(latestEffect);

				return false;
			}

		};

		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(ui);
		multiplexer.addProcessor(inputProcessor);

		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		effect.dispose();
	}

	@Override
	public void resize (int width, int height) {
		ui.getViewport().update(width, height);
	}

	public void render () {
		ui.act();
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		float delta = Gdx.graphics.getDeltaTime();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		for (ParticleEffect e : effects)
			e.draw(spriteBatch, delta);
		spriteBatch.end();
		fpsCounter += delta;
		if (fpsCounter > 3) {
			fpsCounter = 0;
			String log = effects.size + " particle effects, FPS: " + Gdx.graphics.getFramesPerSecond() + ", Render calls: "
				+ spriteBatch.renderCalls;
			Gdx.app.log("libgdx", log);
			logLabel.setText(log);
		}
		ui.draw();
	}

	public boolean needsGL20 () {
		return false;
	}

	private void setupUI () {
		ui = new Stage(new ExtendViewport(640, 480));
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		skipCleanup = new CheckBox("Skip blend function clean-up", skin);
		skipCleanup.setTransform(false);
		skipCleanup.addListener(listener);
		logLabel = new Label("", skin.get(LabelStyle.class));
		clearEmitters = new TextButton("Clear screen", skin);
		clearEmitters.setTransform(false);
		clearEmitters.addListener(listener);
		Table table = new Table();
		table.setTransform(false);
		table.setFillParent(true);
		table.defaults().padTop(5).left();
		table.top().left().padLeft(5);
		table.add(skipCleanup).row();
		table.add(clearEmitters).row();
		table.add(logLabel);
		ui.addActor(table);
	}

	void updateSkipCleanupState () {
		for (ParticleEffect eff : effects) {
			for (ParticleEmitter e : eff.getEmitters())
				e.setCleansUpBlendFunction(!skipCleanup.isChecked());
		}
	}

	ChangeListener listener = new ChangeListener() {

		@Override
		public void changed (ChangeEvent event, Actor actor) {
			if (actor == skipCleanup) {
				updateSkipCleanupState();
			} else if (actor == clearEmitters) {
				for (PooledEffect e : effects)
					e.free();
				effects.clear();
			}
		}
	};
}

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
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class StageTest extends GdxTest implements InputProcessor {
	private static final int NUM_GROUPS = 4;
	private static final int NUM_SPRITES = (int)Math.sqrt(150 / NUM_GROUPS);
	private static final float SPACING = 5;
	ShapeRenderer renderer;
	Stage stage;
	Stage ui;
	Texture texture;
	Texture uiTexture;
	BitmapFont font;

	boolean rotateSprites = false;
	boolean scaleSprites = false;
	float angle;
	Array<Actor> sprites = new Array();
	float scale = 1;
	float vScale = 1;
	Label fps;

	@Override
	public void create () {
		texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);

		stage = new Stage(new ScreenViewport());

		float loc = (NUM_SPRITES * (32 + SPACING) - SPACING) / 2;
		for (int i = 0; i < NUM_GROUPS; i++) {
			Group group = new Group();
			group.setX((float)Math.random() * (stage.getWidth() - NUM_SPRITES * (32 + SPACING)));
			group.setY((float)Math.random() * (stage.getHeight() - NUM_SPRITES * (32 + SPACING)));
			group.setOrigin(loc, loc);

			fillGroup(group, texture);
			stage.addActor(group);
		}

		uiTexture = new Texture(Gdx.files.internal("data/ui.png"));
		uiTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		ui = new Stage(new ScreenViewport());

		Image blend = new Image(new TextureRegion(uiTexture, 0, 0, 64, 32));
		blend.setAlign(Align.center);
		blend.setScaling(Scaling.none);
		blend.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (stage.getBatch().isBlendingEnabled())
					stage.getBatch().disableBlending();
				else
					stage.getBatch().enableBlending();
				return true;
			}
		});
		blend.setY(ui.getHeight() - 64);

		Image rotate = new Image(new TextureRegion(uiTexture, 64, 0, 64, 32));
		rotate.setAlign(Align.center);
		rotate.setScaling(Scaling.none);
		rotate.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				rotateSprites = !rotateSprites;
				return true;
			}
		});
		rotate.setPosition(64, blend.getY());

		Image scale = new Image(new TextureRegion(uiTexture, 64, 32, 64, 32));
		scale.setAlign(Align.center);
		scale.setScaling(Scaling.none);
		scale.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				scaleSprites = !scaleSprites;
				return true;
			}
		});
		scale.setPosition(128, blend.getY());

		{
			Actor shapeActor = new Actor() {
				public void drawDebug (ShapeRenderer shapes) {
					shapes.set(ShapeType.Filled);
					shapes.setColor(getColor());
					shapes.rect(getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(),
						getRotation());
				}
			};
			shapeActor.setBounds(0, 0, 100, 150);
			shapeActor.setOrigin(50, 75);
			shapeActor.debug();
			sprites.add(shapeActor);

			Group shapeGroup = new Group();
			shapeGroup.setBounds(300, 300, 300, 300);
			shapeGroup.setOrigin(50, 75);
			shapeGroup.setTouchable(Touchable.childrenOnly);
			shapeGroup.addActor(shapeActor);
			stage.addActor(shapeGroup);
		}

		ui.addActor(blend);
		ui.addActor(rotate);
		ui.addActor(scale);

		fps = new Label("fps: 0", new Label.LabelStyle(font, Color.WHITE));
		fps.setPosition(10, 30);
		fps.setColor(0, 1, 0, 1);
		ui.addActor(fps);

		renderer = new ShapeRenderer();
		Gdx.input.setInputProcessor(this);
	}

	private void fillGroup (Group group, Texture texture) {
		float advance = 32 + SPACING;
		for (int y = 0; y < NUM_SPRITES * advance; y += advance)
			for (int x = 0; x < NUM_SPRITES * advance; x += advance) {
				Image img = new Image(new TextureRegion(texture));
				img.setAlign(Align.center);
				img.setScaling(Scaling.none);
				img.setBounds(x, y, 32, 32);
				img.setOrigin(16, 16);
				group.addActor(img);
				sprites.add(img);
			}
	}

	private final Vector2 stageCoords = new Vector2();

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (Gdx.input.isTouched()) {
			stage.screenToStageCoordinates(stageCoords.set(Gdx.input.getX(), Gdx.input.getY()));
			Actor actor = stage.hit(stageCoords.x, stageCoords.y, true);
			if (actor != null)
				actor.setColor((float)Math.random(), (float)Math.random(), (float)Math.random(), 0.5f + 0.5f * (float)Math.random());
		}

		Array<Actor> actors = stage.getActors();
		int len = actors.size;
		if (rotateSprites) {
			for (int i = 0; i < len; i++)
				actors.get(i).rotateBy(Gdx.graphics.getDeltaTime() * 10);
		}

		scale += vScale * Gdx.graphics.getDeltaTime();
		if (scale > 1) {
			scale = 1;
			vScale = -vScale;
		}
		if (scale < 0.5f) {
			scale = 0.5f;
			vScale = -vScale;
		}

		len = sprites.size;
		for (int i = 0; i < len; i++) {
			Actor sprite = sprites.get(i);
			if (rotateSprites)
				sprite.rotateBy(-40 * Gdx.graphics.getDeltaTime());
			else
				sprite.setRotation(0);

			if (scaleSprites) {
				sprite.setScale(scale);
			} else {
				sprite.setScale(1);
			}
		}

		stage.draw();

		renderer.begin(ShapeType.Point);
		renderer.setColor(1, 0, 0, 1);
		len = actors.size;
		for (int i = 0; i < len; i++) {
			Group group = (Group)actors.get(i);
			renderer.point(group.getX() + group.getOriginX(), group.getY() + group.getOriginY(), 0);
		}
		renderer.end();

		fps.setText("fps: " + Gdx.graphics.getFramesPerSecond() + ", actors " + sprites.size + ", groups " + sprites.size);
		ui.draw();
	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		return ui.touchDown(x, y, pointer, button);
	}

	public void resize (int width, int height) {
		ui.getViewport().update(width, height, true);
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		ui.dispose();
		renderer.dispose();
		texture.dispose();
		uiTexture.dispose();
		font.dispose();
	}
}

/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.scenes.scene2d.actors.Label;
import com.badlogic.gdx.tests.utils.GdxTest;

public class StageTest extends GdxTest implements InputProcessor {
	private static final int NUM_GROUPS = 5;
	private static final int NUM_SPRITES = (int)Math.sqrt(400 / NUM_GROUPS);
	private static final float SPACING = 5;
	ImmediateModeRenderer renderer;
	Stage stage;
	Stage ui;
	Texture texture;
	Texture uiTexture;
	BitmapFont font;

	boolean rotateSprites = false;
	boolean scaleSprites = false;
	float angle;
	Vector2 point = new Vector2();
	List<Image> images = new ArrayList<Image>();
	float scale = 1;
	float vScale = 1;

	@Override public void create () {
		texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont();

		stage = new Stage(480, 320, true);

		float loc = (NUM_SPRITES * (32 + SPACING) - SPACING) / 2;
		for (int i = 0; i < NUM_GROUPS; i++) {
			Group group = new Group("group" + i);
			group.x = (float)Math.random() * (stage.width() - NUM_SPRITES * (32 + SPACING));
			group.y = (float)Math.random() * (stage.height() - NUM_SPRITES * (32 + SPACING));
			group.originX = loc;
			group.originY = loc;

			fillGroup(group, texture);
			stage.addActor(group);
		}

		uiTexture = new Texture(Gdx.files.internal("data/ui.png"));
		uiTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);		
		ui = new Stage(480, 320, false);
		Image blend = new Image("blend button", new TextureRegion(uiTexture, 0, 0, 64, 32));
		blend.y = ui.height() - 32;
		Image rotate = new Image("rotate button", new TextureRegion(uiTexture, 64, 0, 64, 32));
		rotate.y = blend.y;
		rotate.x = 64;
		Image scale = new Image("scale button", new TextureRegion(uiTexture, 64, 32, 64, 32));
		scale.y = blend.y;
		scale.x = 128;

		ui.addActor(blend);
		ui.addActor(rotate);
		ui.addActor(scale);

		 Label fps = new Label("fps", font, "fps: 0");
		 fps.x = 10; fps.y = 30;
		 fps.color.set(0, 1, 0, 1);
		 ui.addActor(fps);

		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		renderer = new ImmediateModeRenderer();
		Gdx.input.setInputProcessor(this);
	}

	private void fillGroup (Group group, Texture texture) {
		float advance = 32 + SPACING;
		for (int y = 0; y < NUM_SPRITES * advance; y += advance)
			for (int x = 0; x < NUM_SPRITES * advance; x += advance) {
				Image img = new Image(group.name + "-sprite" + x * y, texture);
				img.x = x;
				img.y = y;
				img.width = 32;
				img.height = 32;
				group.addActor(img);
				images.add(img);
			}
	}

	@Override public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		if (Gdx.input.isTouched()) {
			stage.toStageCoordinates(Gdx.input.getX(), Gdx.input.getY(), point);
			Actor actor = stage.hit(point.x, point.y);

			if (actor != null)
				if (actor instanceof Image)
					((Image)actor).color.set((float)Math.random(), (float)Math.random(), (float)Math.random(),
						0.5f + 0.5f * (float)Math.random());
		}

		int len = stage.getGroups().size();
		for (int i = 0; i < len; i++)
			if (rotateSprites)
				stage.getGroups().get(i).rotation += Gdx.graphics.getDeltaTime();
			else
				stage.getGroups().get(i).rotation = 0;

		scale += vScale * Gdx.graphics.getDeltaTime();
		if (scale > 1) {
			scale = 1;
			vScale = -vScale;
		}
		if (scale < 0.5f) {
			scale = 0.5f;
			vScale = -vScale;
		}

		len = images.size();
		for (int i = 0; i < len; i++) {
			Image img = images.get(i);
			if (rotateSprites)
				img.rotation -= 40 * Gdx.graphics.getDeltaTime();
			else
				img.rotation = 0;

			if (scaleSprites) {
				img.scaleX = scale;
				img.scaleY = scale;
			} else {
				img.scaleX = 1;
				img.scaleY = 1;
			}
		}

		stage.draw();

		Gdx.graphics.getGL10().glPointSize(4);
		renderer.begin(GL10.GL_POINTS);
		len = stage.getRoot().getGroups().size();
		for (int i = 0; i < len; i++) {
			renderer.color(1, 0, 0, 1);
			Group group = stage.getRoot().getGroups().get(i);
			renderer.vertex(group.x + group.originX, group.y + group.originY, 0);
		}
		renderer.end();
		Gdx.graphics.getGL10().glPointSize(4);

		 ((Label)ui.findActor("fps")).setText("fps: " +
			 Gdx.graphics.getFramesPerSecond() + ", actors " + images.size() +
			 ", groups "
			 + stage.getGroups().size());
		ui.draw();
	}

	@Override public boolean touchDown (int x, int y, int pointer, int button) {
		boolean touched = ui.touchDown(x, y, pointer, button);
		if (touched) {
			Actor hitActor = ui.getLastTouchedChild();
			if (hitActor == null) return touched;
			if (hitActor.name.startsWith("blend")) if (stage.getSpriteBatch().isBlendingEnabled())
				stage.getSpriteBatch().disableBlending();
			else
				stage.getSpriteBatch().enableBlending();
			if (hitActor.name.startsWith("rotate")) rotateSprites = !rotateSprites;
			if (hitActor.name.startsWith("scale")) scaleSprites = !scaleSprites;
		}
		return touched;
	}

	@Override public boolean touchUp (int x, int y, int pointer, int button) {
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		return false;
	}

	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean needsGL20 () {
		return false;
	}

	@Override public boolean touchMoved (int x, int y) {
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}
}

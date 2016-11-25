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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** This tests both {@link Actor#parentToLocalCoordinates(Vector2)} and {@link Actor#localToParentCoordinates(Vector2)}. */
public class GroupTest extends GdxTest {
	Stage stage;
	SpriteBatch batch;
	BitmapFont font;
	ShapeRenderer renderer;
	TextureRegion region;
	TestGroup group1;
	TestGroup group2;
	HorizontalGroup horiz, horizWrap;
	VerticalGroup vert, vertWrap;

	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont();
		renderer = new ShapeRenderer();

		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		region = new TextureRegion(new Texture(Gdx.files.internal("data/group-debug.png")));

		group2 = new TestGroup("group2");
		group2.setTransform(true);
		stage.addActor(group2);

		group1 = new TestGroup("group1");
		group1.setTransform(true);
		group2.addActor(group1);

		LabelStyle style = new LabelStyle();
		style.font = new BitmapFont();

		Texture texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));

		horiz = new HorizontalGroup().pad(10, 20, 30, 40).top().space(5).reverse();
		for (int i = 1; i <= 15; i++) {
			horiz.addActor(new Label(i + ",", style));
			if (i == 7) horiz.addActor(new Container(new Image(texture)).size(10));
		}
		horiz.addActor(new Container(new Image(texture)).fill().prefSize(30));
		horiz.debug();
		horiz.setPosition(10, 10);
		horiz.pack();
		stage.addActor(horiz);

		horizWrap = new HorizontalGroup().wrap().pad(10, 20, 30, 40).right().rowBottom().space(5).wrapSpace(15).reverse();
		for (int i = 1; i <= 15; i++) {
			horizWrap.addActor(new Label(i + ",", style));
			if (i == 7) horizWrap.addActor(new Container(new Image(texture)).prefSize(10).fill());
		}
		horizWrap.addActor(new Container(new Image(texture)).prefSize(30));
		horizWrap.debug();
		horizWrap.setBounds(10, 85, 150, 40);
		stage.addActor(horizWrap);

		vert = new VerticalGroup().pad(10, 20, 30, 40).top().space(5).reverse();
		for (int i = 1; i <= 8; i++) {
			vert.addActor(new Label(i + ",", style));
			if (i == 4) vert.addActor(new Container(new Image(texture)).size(10));
		}
		vert.addActor(new Container(new Image(texture)).size(30));
		vert.debug();
		vert.setPosition(515, 10);
		vert.pack();
		stage.addActor(vert);

		vertWrap = new VerticalGroup().wrap().pad(10, 20, 30, 40).bottom().columnRight().space(5).wrapSpace(15).reverse();
		for (int i = 1; i <= 8; i++) {
			vertWrap.addActor(new Label(i + ",", style));
			if (i == 4) vertWrap.addActor(new Container(new Image(texture)).prefSize(10).fill());
		}
		vertWrap.addActor(new Container(new Image(texture)).prefSize(30));
		vertWrap.debug();
		vertWrap.setBounds(610, 10, 150, 40);
		stage.addActor(vertWrap);
	}

	public void render () {

		horiz.setVisible(true);
		horiz.setWidth(Gdx.input.getX() - horiz.getX());
		// horiz.setWidth(200);
		horiz.setHeight(100);
		horiz.fill();
		horiz.expand();
		horiz.invalidate();
		
		horizWrap.setVisible(true);
		horizWrap.fill();
		horizWrap.expand();
		horizWrap.setWidth(Gdx.input.getX() - horizWrap.getX());
		// horizWrap.setHeight(horizWrap.getPrefHeight());
		horizWrap.setHeight(200);
		
		vert.setHeight(Gdx.graphics.getHeight() - Gdx.input.getY() - vert.getY());
		// vert.setWidth(200);
		vertWrap.setHeight(Gdx.graphics.getHeight() - Gdx.input.getY() - vertWrap.getY());
// vertWrap.setWidth(vertWrap.getPrefWidth());
		vertWrap.setWidth(200);
		

		// Vary the transforms to exercise the different code paths.
		group2.setBounds(150, 150, 150, 150);
		group2.setRotation(45);
		group2.setOrigin(150, 150);
		group2.setScale(1.25f);

		group1.setBounds(150, 150, 50, 50);
		group1.setRotation(45);
		group1.setOrigin(25, 25);
		group1.setScale(1.3f);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();

		renderer.setProjectionMatrix(batch.getProjectionMatrix());
		renderer.begin(ShapeType.Filled);
		if (MathUtils.randomBoolean()) { // So we see when they are drawn on top of each other (which should be always).
			renderer.setColor(Color.GREEN);
			renderer.circle(group1.toScreenCoordinates.x, Gdx.graphics.getHeight() - group1.toScreenCoordinates.y, 5);
			renderer.setColor(Color.RED);
			renderer.circle(group1.localToParentCoordinates.x, Gdx.graphics.getHeight() - group1.localToParentCoordinates.y, 5);
		} else {
			renderer.setColor(Color.RED);
			renderer.circle(group1.localToParentCoordinates.x, Gdx.graphics.getHeight() - group1.localToParentCoordinates.y, 5);
			renderer.setColor(Color.GREEN);
			renderer.circle(group1.toScreenCoordinates.x, Gdx.graphics.getHeight() - group1.toScreenCoordinates.y, 5);
		}
		renderer.end();
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	public boolean needsGL20 () {
		return false;
	}

	class TestGroup extends Group {
		private String name;
		Vector2 toScreenCoordinates = new Vector2();
		Vector2 localToParentCoordinates = new Vector2();
		float testX = 25;
		float testY = 25;

		public TestGroup (String name) {
			this.name = name;

			addListener(new InputListener() {
				public boolean mouseMoved (InputEvent event, float x, float y) {
					// These come from Actor#parentToLocalCoordinates.
					testX = x;
					testY = y;
					return true;
				}
			});
		}

		public void draw (Batch batch, float parentAlpha) {
			// Use Stage#toScreenCoordinates, which we know is correct.
			toScreenCoordinates.set(testX, testY).sub(getOriginX(), getOriginY()).scl(getScaleX(), getScaleY()).rotate(getRotation())
				.add(getOriginX(), getOriginY()).add(getX(), getY());
			getStage().toScreenCoordinates(toScreenCoordinates, batch.getTransformMatrix());

			// Do the same as toScreenCoordinates via Actor#localToParentCoordinates.
			localToAscendantCoordinates(null, localToParentCoordinates.set(testX, testY));
			getStage().stageToScreenCoordinates(localToParentCoordinates);

			// System.out.println(name + " " + toScreenCoordinates + " " + localToParentCoordinates);

			batch.setColor(getColor());
			batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(),
				getRotation());
			super.draw(batch, parentAlpha);
		}
	}
}

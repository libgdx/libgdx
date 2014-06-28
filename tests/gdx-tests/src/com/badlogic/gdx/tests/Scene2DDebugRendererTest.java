/* ######################################
 * Copyright 2014 (c) Pixel Scientists
 * All rights reserved.
 * Unauthorized copying of this file, via
 * any medium is strictly prohibited.
 * Proprietary and confidential.
 * ###################################### */

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Scene2DDebugRenderer;
import com.badlogic.gdx.tests.utils.GdxTest;

/** @author Daniel Holderbaum */
public class Scene2DDebugRendererTest extends GdxTest {

	static TextureRegion textureRegion;

	private Stage stage;
	private Stage stage1;
	private Stage stage2;

	private Scene2DDebugRenderer debugRenderer;

	class DebugActor extends Actor {

		@Override
		public void draw (Batch batch, float parentAlpha) {
			batch.draw(textureRegion, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(),
				getRotation());
		}
	}

	@Override
	public void create () {
		textureRegion = new TextureRegion(new Texture("data/badlogic.jpg"));

		Gdx.input.setInputProcessor(this);

		stage1 = new Stage();
		stage1.getCamera().position.set(100, 100, 0);

		Group group = new Group();
//		 group.setBounds(0, 0, 10, 10);
		// group.setOrigin(25, 50);
		group.setRotation(10);
		group.setScale(1.2f);
		stage1.addActor(group);

		DebugActor actor = new DebugActor();
		actor.setBounds(400, 240, 50, 100);
		actor.setOrigin(25, 50);
		actor.setRotation(-45);
		actor.setScale(2f);
		group.addActor(actor);

		stage2 = new Stage();
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		Table root = new Table(skin);
		root.setFillParent(true);
		root.setBackground(skin.getDrawable("default-pane"));
		root.defaults().space(6);
		root.debug();
		TextButton shortButton = new TextButton("Button short", skin);
		root.add(shortButton).pad(5);
		shortButton.debug();
		TextButton longButton = new TextButton("Button loooooooooong", skin);
		longButton.debug();
		root.add(longButton).row();
		root.add("Colspan").colspan(2).row();
		root.setTransform(true);
		root.rotateBy(10);
		stage2.addActor(root);

		switchStage();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		stage.draw();
		debugRenderer.render();
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		switchStage();
		return false;
	}

	@Override
	public void resize (int width, int height) {
		stage1.getViewport().update(width, height, true);
		stage2.getViewport().update(width, height, true);
	}

	private void switchStage () {
		if (stage != stage1) {
			stage = stage1;
		} else {
			stage = stage2;
		}
		debugRenderer = new Scene2DDebugRenderer(stage);
	}

}

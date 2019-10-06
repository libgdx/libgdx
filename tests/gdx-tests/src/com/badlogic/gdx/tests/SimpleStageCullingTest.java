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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

/** This is a simple demonstration of how to perform VERY basic culling on hierarchies of stage actors that do not scale or rotate.
 * It is not a general solution as it assumes that actors and groups are only translated (moved, change their x/y coordinates).
 * NOTE: This has been obsoleted by {@link Cullable}.
 * 
 * @author mzechner */
public class SimpleStageCullingTest extends GdxTest {

	/** We need to extend a base actor class so we can add the culling in the render method. We also add a method to get the stage
	 * coordinates of the actor so we can cull it against the camera's view volume.
	 * 
	 * @author mzechner */
	private class CullableActor extends Image {
		/** the camera to test against **/
		final OrthographicCamera camera;
		/** whether we are visible or not, used for counting visible actors **/
		boolean visible = false;

		public CullableActor (String name, Texture texture, OrthographicCamera camera) {
			super(new TextureRegion(texture));
			setAlign(Align.center);
			setScaling(Scaling.none);
			this.camera = camera;
		}

		public void draw (Batch batch, float parentAlpha) {
			// if this actor is not within the view of the camera we don't draw it.
			if (isCulled()) return;

			// otherwise we draw via the super class method
			super.draw(batch, parentAlpha);
		}

		/** static helper Rectangles **/
		Rectangle actorRect = new Rectangle();
		Rectangle camRect = new Rectangle();

		private boolean isCulled () {
			// we start by setting the stage coordinates to this
			// actors coordinates which are relative to its parent
			// Group.
			float stageX = getX();
			float stageY = getY();

			// now we go up the hierarchy and add all the parents'
			// coordinates to this actors coordinates. Note that
			// this assumes that neither this actor nor any of its
			// parents are rotated or scaled!
			Actor parent = this.getParent();
			while (parent != null) {
				stageX += parent.getX();
				stageY += parent.getY();
				parent = parent.getParent();
			}

			// now we check if the rectangle of this actor in screen
			// coordinates is in the rectangle spanned by the camera's
			// view. This assumes that the camera has no zoom and is
			// not rotated!
			actorRect.set(stageX, stageY, getWidth(), getHeight());
			camRect.set(camera.position.x - camera.viewportWidth / 2.0f, camera.position.y - camera.viewportHeight / 2.0f,
				camera.viewportWidth, camera.viewportHeight);
			visible = camRect.overlaps(actorRect);
			return !visible;
		}
	}

	OrthoCamController camController;
	Stage stage;
	Texture texture;
	SpriteBatch batch;
	BitmapFont font;

	@Override
	public void create () {
		// create a stage and a camera controller so we can pan the view.
		stage = new Stage();;
		camController = new OrthoCamController((OrthographicCamera)stage.getCamera()); // we know it's an ortho cam at this point!
		Gdx.input.setInputProcessor(camController);

		// load a dummy texture
		texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));

		// populate the stage with some actors and groups.
		for (int i = 0; i < 5000; i++) {
			Actor img = new CullableActor("img" + i, texture, (OrthographicCamera)stage.getCamera());
			img.setX((float)Math.random() * 480 * 10);
			img.setY((float)Math.random() * 320 * 10);
			stage.addActor(img);
		}

		// we also want to output the number of visible actors, so we need a SpriteBatch and a BitmapFont
		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();

		// check how many actors are visible.
		Array<Actor> actors = stage.getActors();
		int numVisible = 0;
		for (int i = 0; i < actors.size; i++) {
			numVisible += ((CullableActor)actors.get(i)).visible ? 1 : 0;
		}

		batch.begin();
		font.draw(batch, "Visible: " + numVisible + ", fps: " + Gdx.graphics.getFramesPerSecond(), 20, 30);
		batch.end();
	}

	@Override
	public void dispose () {
		stage.dispose();
		texture.dispose();
		batch.dispose();
		font.dispose();
	}
}

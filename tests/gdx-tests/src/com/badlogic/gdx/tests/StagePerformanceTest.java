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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import java.util.Random;

public class StagePerformanceTest extends GdxTest {

	Texture texture;
	TextureRegion[] regions;
	Stage stage;
	SpriteBatch batch;
	BitmapFont font;
	Sprite[] sprites;
	boolean useStage = true;

	@Override
	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont();
		stage = new Stage(new ScalingViewport(Scaling.fit, 24, 12));
		regions = new TextureRegion[8 * 8];
		sprites = new Sprite[24 * 12];

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				regions[x + y * 8] = new TextureRegion(texture, x * 32, y * 32, 32, 32);
			}
		}

		Random rand = new Random();
		for (int y = 0, i = 0; y < 12; y++) {
			for (int x = 0; x < 24; x++) {
				Image img = new Image(regions[rand.nextInt(8 * 8)]);
				img.setBounds(x, y, 1, 1);
				stage.addActor(img);
				sprites[i] = new Sprite(regions[rand.nextInt(8 * 8)]);
				sprites[i].setPosition(x, y);
				sprites[i].setSize(1, 1);
				i++;
			}
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (useStage) {
			stage.act(Gdx.graphics.getDeltaTime());
			stage.getBatch().disableBlending();
			Group root = stage.getRoot();
			Array<Actor> actors = root.getChildren();
// for(int i = 0; i < actors.size(); i++) {
// actors.get(i).rotation += 45 * Gdx.graphics.getDeltaTime();
// }
			stage.draw();
		} else {
			batch.getProjectionMatrix().setToOrtho2D(0, 0, 24, 12);
			batch.getTransformMatrix().idt();
			batch.disableBlending();
			batch.begin();
			for (int i = 0; i < sprites.length; i++) {
// sprites[i].rotate(45 * Gdx.graphics.getDeltaTime());
				sprites[i].draw(batch);
			}
			batch.end();
		}

		batch.getProjectionMatrix().setToOrtho2D(0, 0, 480, 320);
		batch.enableBlending();
		batch.begin();
		font.setColor(0, 0, 1, 1);
		font.getData().setScale(2);
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond() + (useStage ? ", stage" : "sprite"), 10, 40);
		batch.end();

		if (Gdx.input.justTouched()) {
			useStage = !useStage;
		}
	}

	@Override
	public void dispose () {
		stage.dispose();
		batch.dispose();
		font.dispose();
		texture.dispose();
	}
}

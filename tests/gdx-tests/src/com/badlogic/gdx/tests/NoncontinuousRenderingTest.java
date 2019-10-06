/*******************************************************************************
 * Copyright 2016 See AUTHORS file.
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
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class NoncontinuousRenderingTest extends GdxTest {
	SpriteBatch batch;
	Texture texture;
	TextureRegion region;
	Stage stage;
	Skin skin;
	BitmapFont font;
	float elapsed;
	int colorCycle;

	@Override
	public void create () {
		batch = new SpriteBatch();
		texture = new Texture("data/badlogic.jpg");
		region = new TextureRegion(texture);
		stage = new Stage(new ScreenViewport(), batch);
		Gdx.input.setInputProcessor(stage);

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		skin.add("default", font = new BitmapFont(Gdx.files.internal("data/arial-32.fnt"), false));

		populateTable();
		
		Gdx.graphics.setContinuousRendering(false);
		Gdx.graphics.requestRendering();
	}
	
	void nextColor(){
		synchronized (this){
			colorCycle = (colorCycle + 1) % 3;
		}
	}

	@Override
	public void render () {
		float delta = Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f);
		elapsed += delta;
		float value = elapsed % 1f;
		value = value < 0.5f ? 
			Interpolation.fade.apply(2 * value) : 1 - Interpolation.fade.apply(2 * value - 1);
		value = 0.2f + value * 0.8f; //avoid black
		
		synchronized (this){
			switch (colorCycle){
			case 0: 
				Gdx.gl.glClearColor(value, 0, 0, 1); 
				break;
			case 1: 
				Gdx.gl.glClearColor(0, value, 0, 1); 
				break;
			case 2: 
				Gdx.gl.glClearColor(0, 0, value, 1); 
				break;
			}
		}
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Camera cam = stage.getCamera();
		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		batch.draw(region, cam.position.x - texture.getWidth() / 2, cam.position.y - texture.getHeight() / 2, 
			texture.getWidth() / 2f, texture.getHeight() / 2f, (float)texture.getWidth(), (float)texture.getHeight(), 1f, 1f, -((elapsed / 2f) % 1f) * 360f);
		batch.end();
		
		stage.act(delta);
		stage.draw();
	}
	
	private void populateTable (){
		Table root = new Table();
		stage.addActor(root);
		root.setFillParent(true);
		root.pad(5);
		root.defaults().left().space(5);
		
		Button button0 = new TextButton("Toggle continuous rendering", skin, "toggle");
		button0.addListener(new ChangeListener(){
			public void changed (ChangeEvent event, Actor actor) {
				boolean continuous = Gdx.graphics.isContinuousRendering();
				Gdx.graphics.setContinuousRendering(!continuous);
			}
		});
		root.add(button0).row();
		
		final String str1 = "2s sleep -> Application.postRunnable()";
		Button button1 = new TextButton(str1, skin);
		button1.addListener(new ChangeListener(){
			public void changed (ChangeEvent event, Actor actor) {
				new Thread(new Runnable(){
					public void run () {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException ignored){}
						nextColor();
						Gdx.app.postRunnable(new Runnable(){
							public void run () {
								Gdx.app.log(str1, "Posted runnable to Gdx.app");
							}
						});
					}}).start();
				
			}});
		root.add(button1).row();

		final String str2 = "2s sleep -> Graphics.requestRendering()";
		Button button2 = new TextButton(str2, skin);
		button2.addListener(new ChangeListener(){
			public void changed (ChangeEvent event, Actor actor) {
				final Graphics graphics = Gdx.graphics; // caching necessary to ensure call on this window
				new Thread(new Runnable(){
					public void run () {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException ignored){}
						nextColor();
						graphics.requestRendering();
						Gdx.app.log(str2, "Called Gdx.graphics.requestRendering()");
					}}).start();
				
			}});
		root.add(button2).row();
		
		final String str3 = "2s Timer -> Application.postRunnable()";
		Button button3 = new TextButton(str3, skin);
		button3.addListener(new ChangeListener(){
			public void changed (ChangeEvent event, Actor actor) {
				Timer.schedule(new Task(){
					public void run () {
						nextColor();
						Gdx.app.postRunnable(new Runnable(){
							public void run () {
								Gdx.app.log(str3, "Posted runnable to Gdx.app");
							}
						});
					}}, 2f);
			}});
		root.add(button3).row();
		
		final String str4 = "2s DelayAction";
		Button button4 = new TextButton(str4, skin);
		button4.addListener(new ChangeListener(){
			public void changed (ChangeEvent event, Actor actor) {
				stage.addAction(Actions.sequence(Actions.delay(2), Actions.run(new Runnable(){
					public void run () {
						nextColor();
						Gdx.app.log(str4, "RunnableAction executed");
					}
				})));
			}});
		root.add(button4).row();
		
		final String str5 = "(2s sleep -> toggle continuous) 2X";
		Button button5 = new TextButton(str5, skin);
		button5.addListener(new ChangeListener(){
			public void changed (ChangeEvent event, Actor actor) {
				final Graphics graphics = Gdx.graphics; // caching necessary to ensure call on this window
				new Thread(new Runnable(){
					public void run () {
						for (int i=0; i<2; i++){
							try {
								Thread.sleep(2000);
							} catch (InterruptedException ignored){}
							nextColor();
							boolean continuous = graphics.isContinuousRendering();
							graphics.setContinuousRendering(!continuous);
							Gdx.app.log(str5, "Toggled continuous");
						}
					}}).start();
				
			}});
		root.add(button5).row();
		
		final CheckBox actionsRequestRendering = new CheckBox("ActionsRequestRendering", skin);
		actionsRequestRendering.setChecked(true);
		actionsRequestRendering.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				stage.setActionsRequestRendering(actionsRequestRendering.isChecked());
			}
		});
		root.add(actionsRequestRendering).row();
		
		Drawable knobDown = skin.newDrawable("default-slider-knob", Color.GRAY);
		SliderStyle sliderStyle = skin.get("default-horizontal", SliderStyle.class);
		sliderStyle.knobDown = knobDown;
		Slider slider = new Slider (0, 100, 1, false, sliderStyle);
		root.add(slider).row();
		
		SelectBox<Pixmap.Format> selectBox = new SelectBox(skin);
		selectBox.setItems(Pixmap.Format.values());
		root.add(selectBox).row();
		
		root.add();
		root.add().grow();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}
	
	@Override
	public void dispose (){
		batch.dispose();
		texture.dispose();
		stage.dispose();
		font.dispose();
	}
}

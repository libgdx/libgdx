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

package com.badlogic.gdx.tests.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;

public class NetAPITest extends GdxTest implements HttpResponseListener {

	SpriteBatch batch;
	Skin skin;
	Stage stage;
	TextButton textButton;
	Label statusLabel;
	Texture texture;

	public boolean needsGL20 () {
		// just because the non pot, we could change the image instead...
		return true;
	}

	@Override
	public void dispose () {
		batch.dispose();
		stage.dispose();
		skin.dispose();
		if (texture != null) texture.dispose();
	}

	@Override
	public void create () {
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		Gdx.input.setInputProcessor(stage);

		{
			statusLabel = new Label("", skin);
			statusLabel.setWrap(true);
			statusLabel.setWidth(Gdx.graphics.getWidth() * 0.96f);
			statusLabel.setAlignment(Align.center);
			statusLabel.setPosition(Gdx.graphics.getWidth() * 0.5f - statusLabel.getWidth() * 0.5f, 30f);
			stage.addActor(statusLabel);
		}

		{
			ClickListener clickListener = new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					super.clicked(event, x, y);
					textButton.setDisabled(true);
					textButton.setTouchable(Touchable.disabled);

					if (texture != null) {
						texture.dispose();
						texture = null;
					}

					HttpRequest httpRequest = new HttpRequest(Net.HttpMethods.GET);
					httpRequest.setUrl("http://i.imgur.com/vxomF.jpg");

					Gdx.net.sendHttpRequest(httpRequest, NetAPITest.this);

					statusLabel.setText("Downloading image from " + httpRequest.getUrl());
				}
			};

			textButton = new TextButton("Download image", skin);
			textButton.setPosition(Gdx.graphics.getWidth() * 0.5f - textButton.getWidth() * 0.5f, 60f);
			textButton.addListener(clickListener);
			stage.addActor(textButton);
		}

	}

	@Override
	public void handleHttpResponse (HttpResponse httpResponse) {

		final int statusCode = httpResponse.getStatus().getStatusCode();
		// We are not in main thread right now so we need to post to main thread for ui updates
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
				statusLabel.setText("HTTP Request status: " + statusCode);
				textButton.setDisabled(false);
				textButton.setTouchable(Touchable.enabled);
			}
		});

		if (statusCode != 200) {
			Gdx.app.log("NetAPITest", "An error ocurred since statusCode is not OK");
			return;
		}

		final byte[] rawImageBytes = httpResponse.getResult();
		Gdx.app.postRunnable(new Runnable() {
			public void run () {
				Texture.setEnforcePotImages(false);
				Pixmap pixmap = new Pixmap(rawImageBytes, 0, rawImageBytes.length);
				texture = new Texture(pixmap);
				Texture.setEnforcePotImages(true);
			}
		});
	}

	@Override
	public void failed (Throwable t) {
		textButton.setDisabled(false);
		textButton.setTouchable(Touchable.enabled);
		statusLabel.setText("Failed to perform the HTTP Request: " + t.getMessage());
		t.printStackTrace();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		if (texture != null) {
			batch.begin();
			batch.draw(texture, Gdx.graphics.getWidth() * 0.5f - texture.getWidth() * 0.5f, 100f);
			batch.end();
		}

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.setViewport(width, height, false);
	}

}

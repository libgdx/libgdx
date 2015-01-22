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
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * Demonstrates how to perform a simple HTTP request. Need to add internet permission to AndroidManifest.xml.
 *
 * @author badlogic
 */
public class HttpRequestExample extends GdxTest {

    SpriteBatch batch;

    Texture texture;

    @Override
    public void create() {
        Gdx.graphics.setContinuousRendering(false);
        Gdx.graphics.requestRendering();

        batch = new SpriteBatch();

        HttpRequest request = new HttpRequest(HttpMethods.GET);
        request.setUrl("http://libgdx.badlogicgames.com/img/logo.png");
        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                Gdx.app.log("HttpRequestExample", "response: " + httpResponse.getResultAsString());
                final byte[] rawImageBytes = httpResponse.getResult();
                Gdx.app.postRunnable(new Runnable() {
                    public void run() {
                        Pixmap pixmap = new Pixmap(rawImageBytes, 0, rawImageBytes.length);
                        texture = new Texture(pixmap);
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("HttpRequestExample", "something went wrong", t);
            }

            @Override
            public void cancelled() {
                Gdx.app.log("HttpRequestExample", "cancelled");
            }
        });
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (texture != null) {
            batch.begin();
            batch.draw(texture, Gdx.graphics.getWidth() * 0.5f - texture.getWidth() * 0.5f, Gdx.graphics
                    .getHeight() * 0.5f - texture.getHeight() * 0.5f);
            batch.end();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (null != texture)
            texture.dispose();
    }
}

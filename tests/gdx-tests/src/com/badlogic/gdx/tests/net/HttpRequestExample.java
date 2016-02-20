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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.tests.utils.GdxTest;

/** Demonstrates how to perform a simple HTTP request. Need to add internet permission to AndroidManifest.xml.
 * @author badlogic */
public class HttpRequestExample extends GdxTest {
	@Override
	public void create () {
		HttpRequest request = new HttpRequest(HttpMethods.GET);
		request.setUrl("http://libgdx.badlogicgames.com/nightlies/dist/AUTHORS");
		Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
			@Override
			public void handleHttpResponse (HttpResponse httpResponse) {
				Gdx.app.log("HttpRequestExample", "response: " + httpResponse.getResultAsString());
			}

			@Override
			public void failed (Throwable t) {
				Gdx.app.error("HttpRequestExample", "something went wrong", t);
			}

			@Override
			public void cancelled () {
				Gdx.app.log("HttpRequestExample", "cancelled");
			}
		});
	}
}

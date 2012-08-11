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

package com.badlogic.gdx.backends.gwt.preloader;

import javax.xml.ws.Response;

public class TextLoader {
	private final LoaderCallback<String> callback;

	public TextLoader (String url, LoaderCallback<String> callback) {
		this.callback = callback;
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
		try {
			builder.sendRequest(null, new RequestCallback() {
				@Override
				public void onResponseReceived (Request request, Response response) {
					TextLoader.this.callback.success(response.getText());
				}

				@Override
				public void onError (Request request, Throwable exception) {
					TextLoader.this.callback.error();
				}
			});
		} catch (RequestException e) {
			callback.error();
		}
	}
}

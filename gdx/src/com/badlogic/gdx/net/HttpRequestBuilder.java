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

package com.badlogic.gdx.net;

import java.io.InputStream;
import java.util.Map;

import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Pools;

/** A builder for {@link HttpRequest}s.
 * 
 * Make sure to call {@link #newRequest()} first, then set the request up and obtain it via {@link #build()} when you are done.
 * 
 * It also offers a few utility methods to deal with content encoding and HTTP headers.
 * 
 * @author Daniel Holderbaum */
public class HttpRequestBuilder {

	/** Will be added as a prefix to each URL when {@link #url(String)} is called. Empty by default. */
	public static String baseUrl = "";

	/** Will be set for each new HttpRequest. By default set to {@code 1000}. Can be overwritten via {@link #timeout(int)}. */
	public static int defaultTimeout = 1000;

	/** Will be used for the object serialization in case {@link #jsonContent(Object)} is called. */
	public static Json json = new Json();

	private HttpRequest httpRequest;

	/** Initializes the builder and sets it up to build a new {@link HttpRequest} . */
	public HttpRequestBuilder newRequest () {
		if (httpRequest != null) {
			throw new IllegalStateException("A new request has already been started. Call HttpRequestBuilder.build() first.");
		}

		httpRequest = Pools.obtain(HttpRequest.class);
		httpRequest.setTimeOut(defaultTimeout);
		return this;
	}

	/** @see HttpRequest#setMethod(String) */
	public HttpRequestBuilder method (String httpMethod) {
		validate();
		httpRequest.setMethod(httpMethod);
		return this;
	}

	/** The {@link #baseUrl} will automatically be added as a prefix to the given URL.
	 * 
	 * @see HttpRequest#setUrl(String) */
	public HttpRequestBuilder url (String url) {
		validate();
		httpRequest.setUrl(baseUrl + url);
		return this;
	}

	/** If this method is not called, the {@link #defaultTimeout} will be used.
	 * 
	 * @see HttpRequest#setTimeOut(int) */
	public HttpRequestBuilder timeout (int timeOut) {
		validate();
		httpRequest.setTimeOut(timeOut);
		return this;
	}

	/** @see HttpRequest#setFollowRedirects(boolean) */
	public HttpRequestBuilder followRedirects (boolean followRedirects) {
		validate();
		httpRequest.setFollowRedirects(followRedirects);
		return this;
	}
	
	/** @see HttpRequest#setIncludeCredentials(boolean) */
	public HttpRequestBuilder includeCredentials (boolean includeCredentials) {
		validate();
		httpRequest.setIncludeCredentials(includeCredentials);
		return this;
	}

	/** @see HttpRequest#setHeader(String, String) */
	public HttpRequestBuilder header (String name, String value) {
		validate();
		httpRequest.setHeader(name, value);
		return this;
	}

	/** @see HttpRequest#setContent(String) */
	public HttpRequestBuilder content (String content) {
		validate();
		httpRequest.setContent(content);
		return this;
	}

	/** @see HttpRequest#setContent(java.io.InputStream, long) */
	public HttpRequestBuilder content (InputStream contentStream, long contentLength) {
		validate();
		httpRequest.setContent(contentStream, contentLength);
		return this;
	}

	/** Sets the correct {@code ContentType} and encodes the given parameter map, then sets it as the content. */
	public HttpRequestBuilder formEncodedContent (Map<String, String> content) {
		validate();
		httpRequest.setHeader(HttpRequestHeader.ContentType, "application/x-www-form-urlencoded");
		String formEncodedContent = HttpParametersUtils.convertHttpParameters(content);
		httpRequest.setContent(formEncodedContent);
		return this;
	}

	/** Sets the correct {@code ContentType} and encodes the given content object via {@link #json}, then sets it as the content. */
	public HttpRequestBuilder jsonContent (Object content) {
		validate();
		httpRequest.setHeader(HttpRequestHeader.ContentType, "application/json");
		String jsonContent = json.toJson(content);
		httpRequest.setContent(jsonContent);
		return this;
	}

	/** Sets the {@code Authorization} header via the Base64 encoded username and password. */
	public HttpRequestBuilder basicAuthentication (String username, String password) {
		validate();
		httpRequest.setHeader(HttpRequestHeader.Authorization, "Basic " + Base64Coder.encodeString(username + ":" + password));
		return this;
	}

	/** Returns the {@link HttpRequest} that has been setup by this builder so far. After using the request, it should be returned
	 * to the pool via {@code Pools.free(request)}. */
	public HttpRequest build () {
		validate();
		HttpRequest request = httpRequest;
		httpRequest = null;
		return request;
	}

	private void validate () {
		if (httpRequest == null) {
			throw new IllegalStateException("A new request has not been started yet. Call HttpRequestBuilder.newRequest() first.");
		}
	}

}

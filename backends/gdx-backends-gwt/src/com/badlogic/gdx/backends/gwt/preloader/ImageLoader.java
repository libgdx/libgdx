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

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;

/**
 * Fugly but does the job.
 * @author mzechner
 *
 */
public class ImageLoader {
	private final LoaderCallback<ImageElement> callback;
	private final ImageElement image;
	
	public ImageLoader(String url, LoaderCallback<ImageElement> callback) {
		this.callback = callback;
		this.image = createImage();
		this.image.setSrc(url);
		hookOnLoad(image, this);
	}
	
	private void onEvent(NativeEvent event) {
		if(event.getType().equals("error")) callback.error();
		else callback.success(image);
	}

	private static native ImageElement createImage() /*-{
		return new Image();
	}-*/;

	private static native void hookOnLoad(ImageElement img, ImageLoader h) /*-{
		img.addEventListener('load',function(e) {h.@com.badlogic.gdx.backends.gwt.preloader.ImageLoader::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);}, false);
		img.addEventListener('error',function(e) {h.@com.badlogic.gdx.backends.gwt.preloader.ImageLoader::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);}, false);
	}-*/;
}
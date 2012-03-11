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

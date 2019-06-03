package com.badlogic.gdx.backends.gwt;

import com.google.gwt.core.client.JavaScriptObject;

public class GwtSensor extends JavaScriptObject {

	protected GwtSensor() {
	}

	final native boolean activated() /*-{
		return this.activated;
	}-*/;

	final native boolean hasReading() /*-{
		return this.hasReading;
	}-*/;

	final native double timestamp() /*-{
		return this.timestamp;
	}-*/;

	final native void start() /*-{
		this.start();
	}-*/;

	final native void stop() /*-{
		this.stop();
	}-*/;

	final native void setReadingListener(SensorRead listener) /*-{
		this.onreading = listener.@com.badlogic.gdx.backends.gwt.GwtSensor.SensorRead::onReading()();
	}-*/;

	final native void setErrorListener(SensorError listener) /*-{
		this.onerror = listener.@com.badlogic.gdx.backends.gwt.GwtSensor.SensorError::onError()();
	}-*/;

	final native void setActivateListener(SensorActivate listener) /*-{
		this.onactivate = listener.@com.badlogic.gdx.backends.gwt.GwtSensor.SensorActivate::onActivate()();
	}-*/;

	interface SensorRead {
		void onReading();
	}

	interface SensorError {
		void onError();
	}

	interface SensorActivate {
		void onActivate();
	}
}


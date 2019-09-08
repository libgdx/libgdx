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

package com.badlogic.gdx.backends.gwt;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Implementation of the <a href="https://www.w3.org/TR/generic-sensor/#the-sensor-interface">Sensor Interface</a>
 * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Sensor#Browser_compatibility">Compatibility</a>
 */
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


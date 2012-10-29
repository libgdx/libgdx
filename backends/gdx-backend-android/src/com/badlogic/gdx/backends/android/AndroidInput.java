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

package com.badlogic.gdx.backends.android;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;

/** An implementation of the {@link Input} interface for Android.
 * 
 * @author mzechner */
/** @author jshapcot */
public final class AndroidInput extends AndroidInputBase implements Input, OnKeyListener, OnTouchListener {

	private String text = null;
	private TextInputListener textListener = null;
	private Handler handle;
	
	private final AndroidTouchHandler touchHandler;

	private final AndroidOnscreenKeyboard onscreenKeyboard;

	private SensorEventListener accelerometerListener;
	private SensorEventListener compassListener;

	public AndroidInput (AndroidApplication activity, View view, AndroidApplicationConfiguration config) {
		view.setOnKeyListener(this);
		view.setOnTouchListener(this);
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.requestFocusFromTouch();
		this.config = config;
		this.onscreenKeyboard = new AndroidOnscreenKeyboard(activity, new Handler(), this);

		for (int i = 0; i < realId.length; i++)
			realId[i] = -1;
		handle = new Handler();
		this.app = activity;
		this.sleepTime = config.touchSleepTime;
		int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
		if (sdkVersion >= 5)
			touchHandler = new AndroidMultiTouchHandler();
		else
			touchHandler = new AndroidSingleTouchHandler();
		hasMultitouch = touchHandler.supportsMultitouch((AndroidApplication)app);

		vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);

		int rotation = getRotation();
		DisplayMode mode = app.graphics.getDesktopDisplayMode();
		if (((rotation == 0 || rotation == 180) && (mode.width >= mode.height))
			|| ((rotation == 90 || rotation == 270) && (mode.width <= mode.height))) {
			nativeOrientation = Orientation.Landscape;
		} else {
			nativeOrientation = Orientation.Portrait;
		}
	}

	@Override
	public float getAccelerometerX () {
		return accelerometerValues[0];
	}

	@Override
	public float getAccelerometerY () {
		return accelerometerValues[1];
	}

	@Override
	public float getAccelerometerZ () {
		return accelerometerValues[2];
	}

	@Override
	public void getTextInput (final TextInputListener listener, final String title, final String text) {
		handle.post(new Runnable() {
			public void run () {
				AlertDialog.Builder alert = new AlertDialog.Builder(AndroidInput.this.app);
				alert.setTitle(title);
				final EditText input = new EditText(AndroidInput.this.app);
				input.setText(text);
				input.setSingleLine();
				alert.setView(input);
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick (DialogInterface dialog, int whichButton) {
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run () {
								listener.input(input.getText().toString());
							}
						});
					}
				});
				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick (DialogInterface dialog, int whichButton) {
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run () {
								listener.canceled();
							}
						});
					}
				});
				alert.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel (DialogInterface arg0) {
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run () {
								listener.canceled();
							}
						});
					}
				});
				alert.show();
			}
		});
	}

	public void getPlaceholderTextInput (final TextInputListener listener, final String title, final String placeholder) {
		handle.post(new Runnable() {
			public void run () {
				AlertDialog.Builder alert = new AlertDialog.Builder(AndroidInput.this.app);
				alert.setTitle(title);
				final EditText input = new EditText(AndroidInput.this.app);
				input.setHint(placeholder);
				input.setSingleLine();
				alert.setView(input);
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick (DialogInterface dialog, int whichButton) {
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run () {
								listener.input(input.getText().toString());
							}
						});
					}
				});
				alert.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel (DialogInterface arg0) {
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run () {
								listener.canceled();
							}
						});
					}
				});
				alert.show();
			}
		});
	}

	protected void processEvents () {
		synchronized (this) {
			justTouched = false;

			if (processor != null) {
				final InputProcessor processor = this.processor;

				int len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					KeyEvent e = keyEvents.get(i);
					currentEventTimeStamp = e.timeStamp;
					switch (e.type) {
					case KeyEvent.KEY_DOWN:
						processor.keyDown(e.keyCode);
						break;
					case KeyEvent.KEY_UP:
						processor.keyUp(e.keyCode);
						break;
					case KeyEvent.KEY_TYPED:
						processor.keyTyped(e.keyChar);
					}
					usedKeyEvents.free(e);
				}

				len = touchEvents.size();
				for (int i = 0; i < len; i++) {
					TouchEvent e = touchEvents.get(i);
					currentEventTimeStamp = e.timeStamp;
					switch (e.type) {
					case TouchEvent.TOUCH_DOWN:
						processor.touchDown(e.x, e.y, e.pointer, Buttons.LEFT);
						justTouched = true;
						break;
					case TouchEvent.TOUCH_UP:
						processor.touchUp(e.x, e.y, e.pointer, Buttons.LEFT);
						break;
					case TouchEvent.TOUCH_DRAGGED:
						processor.touchDragged(e.x, e.y, e.pointer);
					}
					usedTouchEvents.free(e);
				}
			} else {
				int len = touchEvents.size();
				for (int i = 0; i < len; i++) {
					TouchEvent e = touchEvents.get(i);
					if (e.type == TouchEvent.TOUCH_DOWN) justTouched = true;
					usedTouchEvents.free(e);
				}

				len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					usedKeyEvents.free(keyEvents.get(i));
				}
			}

			if (touchEvents.size() == 0) {
				for (int i = 0; i < deltaX.length; i++) {
					deltaX[0] = 0;
					deltaY[0] = 0;
				}
			}

			keyEvents.clear();
			touchEvents.clear();
		}
	}

	boolean requestFocus = true;

	@Override
	public boolean onTouch (View view, MotionEvent event) {
		if (requestFocus) {
			view.requestFocus();
			view.requestFocusFromTouch();
			requestFocus = false;
		}

		// synchronized in handler.postTouchEvent()
		touchHandler.onTouch(event, this);

		if (sleepTime != 0) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
			}
		}
		return true;
	}

	@Override
	public void setOnscreenKeyboardVisible (final boolean visible) {
// onscreenKeyboard.setVisible(visible);
		handle.post(new Runnable() {
			public void run () {
				InputMethodManager manager = (InputMethodManager)app.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (visible) {
					View view = ((AndroidGraphics)app.getGraphics()).getView();
					view.setFocusable(true);
					view.setFocusableInTouchMode(true);
					manager.showSoftInput(((AndroidGraphics)app.getGraphics()).getView(), 0);
				} else {
					manager.hideSoftInputFromWindow(((AndroidGraphics)app.getGraphics()).getView().getWindowToken(), 0);
				}
			}
		});
	}

	protected void registerSensorListeners () {
		if (config.useAccelerometer) {
			manager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
			if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
				accelerometerAvailable = false;
			} else {
				Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
				accelerometerListener = new SensorListener(this.nativeOrientation, this.accelerometerValues, this.magneticFieldValues);
				accelerometerAvailable = manager.registerListener(accelerometerListener, accelerometer,
					SensorManager.SENSOR_DELAY_GAME);
			}
		} else
			accelerometerAvailable = false;

		if (config.useCompass) {
			if (manager == null) manager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
			Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			if (sensor != null) {
				compassAvailable = accelerometerAvailable;
				if (compassAvailable) {
					compassListener = new SensorListener(this.nativeOrientation, this.accelerometerValues, this.magneticFieldValues);
					compassAvailable = manager.registerListener(compassListener, sensor, SensorManager.SENSOR_DELAY_GAME);
				}
			} else {
				compassAvailable = false;
			}
		} else
			compassAvailable = false;
		Gdx.app.log("AndroidInput", "sensor listener setup");
	}

	protected void unregisterSensorListeners () {
		if (manager != null) {
			if (accelerometerListener != null) {
				manager.unregisterListener(accelerometerListener);
				accelerometerListener = null;
			}
			if (compassListener != null) {
				manager.unregisterListener(compassListener);
				compassListener = null;
			}
			manager = null;
		}
		Gdx.app.log("AndroidInput", "sensor listener tear down");
	}

	@Override
	public int getRotation () {
		int orientation = app.getWindowManager().getDefaultDisplay().getOrientation();
		switch (orientation) {
		case Surface.ROTATION_0:
			return 0;
		case Surface.ROTATION_90:
			return 90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return 270;
		default:
			return 0;
		}
	}

	/** Our implementation of SensorEventListener. Because Android doesn't like it when we register more than one Sensor to a single
	 * SensorEventListener, we add one of these for each Sensor. Could use an anonymous class, but I don't see any harm in
	 * explicitly defining it here. Correct me if I am wrong. */
	private class SensorListener implements SensorEventListener {
		final float[] accelerometerValues;
		final float[] magneticFieldValues;
		final Orientation nativeOrientation;

		SensorListener (Orientation nativeOrientation, float[] accelerometerValues, float[] magneticFieldValues) {
			this.accelerometerValues = accelerometerValues;
			this.magneticFieldValues = magneticFieldValues;
			this.nativeOrientation = nativeOrientation;
		}

		@Override
		public void onAccuracyChanged (Sensor arg0, int arg1) {

		}

		@Override
		public void onSensorChanged (SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				if (nativeOrientation == Orientation.Portrait) {
					System.arraycopy(event.values, 0, accelerometerValues, 0, accelerometerValues.length);
				} else {
					accelerometerValues[0] = event.values[1];
					accelerometerValues[1] = -event.values[0];
					accelerometerValues[2] = event.values[2];
				}
			}
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				System.arraycopy(event.values, 0, magneticFieldValues, 0, magneticFieldValues.length);
			}
		}
	}
}

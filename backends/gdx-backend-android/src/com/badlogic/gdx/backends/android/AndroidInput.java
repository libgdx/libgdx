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
import java.util.HashSet;

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
public final class AndroidInput implements Input, OnKeyListener, OnTouchListener {
	class KeyEvent {
		static final int KEY_DOWN = 0;
		static final int KEY_UP = 1;
		static final int KEY_TYPED = 2;

		long timeStamp;
		int type;
		int keyCode;
		char keyChar;
	}

	class TouchEvent {
		static final int TOUCH_DOWN = 0;
		static final int TOUCH_UP = 1;
		static final int TOUCH_DRAGGED = 2;

		long timeStamp;
		int type;
		int x;
		int y;
		int pointer;
	}

	Pool<KeyEvent> usedKeyEvents = new Pool<KeyEvent>(16, 1000) {
		protected KeyEvent newObject () {
			return new KeyEvent();
		}
	};

	Pool<TouchEvent> usedTouchEvents = new Pool<TouchEvent>(16, 1000) {
		protected TouchEvent newObject () {
			return new TouchEvent();
		}
	};

	ArrayList<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
	ArrayList<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	int[] touchX = new int[20];
	int[] touchY = new int[20];
	int[] deltaX = new int[20];
	int[] deltaY = new int[20];
	boolean[] touched = new boolean[20];
	int[] realId = new int[10];
	final boolean hasMultitouch;
	private IntMap<Object> keys = new IntMap<Object>();
	private SensorManager manager;
	public boolean accelerometerAvailable = false;
	private final float[] accelerometerValues = new float[3];
	private String text = null;
	private TextInputListener textListener = null;
	private Handler handle;
	final AndroidApplication app;
	private final AndroidTouchHandler touchHandler;
	private int sleepTime = 0;
	private boolean catchBack = false;
	private boolean catchMenu = false;
	private final Vibrator vibrator;
	private boolean compassAvailable = false;
	boolean keyboardAvailable;
	private final float[] magneticFieldValues = new float[3];
	private float azimuth = 0;
	private float pitch = 0;
	private float roll = 0;
	private float inclination = 0;
	private boolean justTouched = false;
	private InputProcessor processor;
	private final AndroidApplicationConfiguration config;
	private final Orientation nativeOrientation;
	private long currentEventTimeStamp = System.nanoTime();
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
		hasMultitouch = touchHandler.supportsMultitouch(app);

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
						listener.input(input.getText().toString());
					}
				});
				alert.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel (DialogInterface arg0) {
						listener.canceled();
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
						listener.input(input.getText().toString());
					}
				});
				alert.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel (DialogInterface arg0) {
						listener.canceled();
					}
				});
				alert.show();
			}
		});
	}

	@Override
	public int getX () {
		synchronized (this) {
			return touchX[0];
		}
	}

	@Override
	public int getY () {
		synchronized (this) {
			return touchY[0];
		}
	}

	@Override
	public int getX (int pointer) {
		synchronized (this) {
			return touchX[pointer];
		}
	}

	@Override
	public int getY (int pointer) {
		synchronized (this) {
			return touchY[pointer];
		}
	}

	public boolean isTouched (int pointer) {
		synchronized (this) {
			return touched[pointer];
		}
	}

	@Override
	public boolean isKeyPressed (int key) {
		synchronized (this) {
			if (key == Input.Keys.ANY_KEY)
				return keys.size > 0;
			else
				return keys.containsKey(key);
		}
	}

	@Override
	public boolean isTouched () {
		synchronized (this) {
			return touched[0];
		}
	}

	public void setInputProcessor (InputProcessor processor) {
		synchronized (this) {
			this.processor = processor;
		}
	}

	void processEvents () {
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
	public boolean onKey (View v, int keyCode, android.view.KeyEvent e) {
		synchronized (this) {
			char character = (char)e.getUnicodeChar();
			// Android doesn't report a unicode char for back space. hrm...
			if (keyCode == 67) character = '\b';

			KeyEvent event = null;
			switch (e.getAction()) {
			case android.view.KeyEvent.ACTION_DOWN:
				event = usedKeyEvents.obtain();
				event.keyChar = 0;
				event.keyCode = e.getKeyCode();
				event.type = KeyEvent.KEY_DOWN;

				// Xperia hack for circle key. gah...
				if (keyCode == android.view.KeyEvent.KEYCODE_BACK && e.isAltPressed()) {
					keyCode = Keys.BUTTON_CIRCLE;
					event.keyCode = keyCode;
				}

				keyEvents.add(event);
				keys.put(event.keyCode, null);
				break;
			case android.view.KeyEvent.ACTION_UP:
				event = usedKeyEvents.obtain();
				event.keyChar = 0;
				event.keyCode = e.getKeyCode();
				event.type = KeyEvent.KEY_UP;
				// Xperia hack for circle key. gah...
				if (keyCode == android.view.KeyEvent.KEYCODE_BACK && e.isAltPressed()) {
					keyCode = Keys.BUTTON_CIRCLE;
					event.keyCode = keyCode;
				}
				keyEvents.add(event);

				event = usedKeyEvents.obtain();
				event.keyChar = character;
				event.keyCode = 0;
				event.type = KeyEvent.KEY_TYPED;
				keyEvents.add(event);

				if (keyCode == Keys.BUTTON_CIRCLE)
					keys.remove(Keys.BUTTON_CIRCLE);
				else
					keys.remove(e.getKeyCode());
			}
			app.getGraphics().requestRendering();
		}

		// circle button on Xperia Play shouldn't need catchBack == true
		if (keyCode == Keys.BUTTON_CIRCLE) return true;
		if (catchBack && keyCode == android.view.KeyEvent.KEYCODE_BACK) return true;
		if (catchMenu && keyCode == android.view.KeyEvent.KEYCODE_MENU) return true;
		return false;
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

	@Override
	public void setCatchBackKey (boolean catchBack) {
		this.catchBack = catchBack;
	}

	@Override
	public void setCatchMenuKey (boolean catchMenu) {
		this.catchMenu = catchMenu;
	}

	@Override
	public void vibrate (int milliseconds) {
		vibrator.vibrate(milliseconds);
	}

	@Override
	public void vibrate (long[] pattern, int repeat) {
		vibrator.vibrate(pattern, repeat);
	}

	@Override
	public void cancelVibrate () {
		vibrator.cancel();
	}

	@Override
	public boolean justTouched () {
		return justTouched;
	}

	@Override
	public boolean isButtonPressed (int button) {
		if (button == Buttons.LEFT)
			return isTouched();
		else
			return false;
	}

	final float[] R = new float[9];
	final float[] orientation = new float[3];

	private void updateOrientation () {
		if (SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues)) {
			SensorManager.getOrientation(R, orientation);
			azimuth = (float)Math.toDegrees(orientation[0]);
			pitch = (float)Math.toDegrees(orientation[1]);
			roll = (float)Math.toDegrees(orientation[2]);
		}
	}

	/** Returns the rotation matrix describing the devices rotation as per <a href=
	 * "http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])"
	 * >SensorManager#getRotationMatrix(float[], float[], float[], float[])</a>. Does not manipulate the matrix if the platform
	 * does not have an accelerometer.
	 * @param matrix */
	public void getRotationMatrix (float[] matrix) {
		SensorManager.getRotationMatrix(matrix, null, accelerometerValues, magneticFieldValues);
	}

	@Override
	public float getAzimuth () {
		if (!compassAvailable) return 0;

		updateOrientation();
		return azimuth;
	}

	@Override
	public float getPitch () {
		if (!compassAvailable) return 0;

		updateOrientation();
		return pitch;
	}

	@Override
	public float getRoll () {
		if (!compassAvailable) return 0;

		updateOrientation();
		return roll;
	}

	void registerSensorListeners () {
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

	void unregisterSensorListeners () {
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
	public InputProcessor getInputProcessor () {
		return this.processor;
	}

	@Override
	public boolean isPeripheralAvailable (Peripheral peripheral) {
		if (peripheral == Peripheral.Accelerometer) return accelerometerAvailable;
		if (peripheral == Peripheral.Compass) return compassAvailable;
		if (peripheral == Peripheral.HardwareKeyboard) return keyboardAvailable;
		if (peripheral == Peripheral.OnscreenKeyboard) return true;
		if (peripheral == Peripheral.Vibrator) return vibrator != null;
		if (peripheral == Peripheral.MultitouchScreen) return hasMultitouch;
		return false;
	}

	public int getFreePointerIndex () {
		int len = realId.length;
		for (int i = 0; i < len; i++) {
			if (realId[i] == -1) return i;
		}

		int[] tmp = new int[realId.length + 1];
		System.arraycopy(realId, 0, tmp, 0, realId.length);
		realId = tmp;
		return tmp.length - 1;
	}

	public int lookUpPointerIndex (int pointerId) {
		int len = realId.length;
		for (int i = 0; i < len; i++) {
			if (realId[i] == pointerId) return i;
		}

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < len; i++) {
			buf.append(i + ":" + realId[i] + " ");
		}
		Gdx.app.log("AndroidInput", "Pointer ID lookup failed: " + pointerId + ", " + buf.toString());
		return -1;
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

	@Override
	public Orientation getNativeOrientation () {
		return nativeOrientation;
	}

	@Override
	public void setCursorCatched (boolean catched) {
	}

	@Override
	public boolean isCursorCatched () {
		return false;
	}

	@Override
	public int getDeltaX () {
		return deltaX[0];
	}

	@Override
	public int getDeltaX (int pointer) {
		return deltaX[pointer];
	}

	@Override
	public int getDeltaY () {
		return deltaY[0];
	}

	@Override
	public int getDeltaY (int pointer) {
		return deltaY[pointer];
	}

	@Override
	public void setCursorPosition (int x, int y) {
	}

	@Override
	public long getCurrentEventTime () {
		return currentEventTimeStamp;
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

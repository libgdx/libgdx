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
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Pool;

/**
 * An implementation of the {@link Input} interface for Android.
 * 
 * @author mzechner
 * 
 */
public final class AndroidInput implements Input, OnKeyListener, OnTouchListener, SensorEventListener {
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
	int[] touchX = new int[40];
	int[] touchY = new int[40];
	boolean[] touched = new boolean[40];
	final boolean hasMultitouch;
	private HashSet<Integer> keys = new HashSet<Integer>();
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

	public AndroidInput (AndroidApplication activity, View view, AndroidApplicationConfiguration config) {
		view.setOnKeyListener(this);
		view.setOnTouchListener(this);
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.requestFocusFromTouch();
		this.config = config;
		
		handle = new Handler();
		this.app = activity;
		this.sleepTime = config.touchSleepTime;
		int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
		if (sdkVersion >= 5)
			touchHandler = new AndroidMultiTouchHandler();
		else
			touchHandler = new AndroidSingleTouchHandler();
		hasMultitouch = touchHandler instanceof AndroidMultiTouchHandler
			&& ((AndroidMultiTouchHandler)touchHandler).supportsMultitouch(activity);

		vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);					
	}

	@Override public float getAccelerometerX () {
		return accelerometerValues[0];
	}

	@Override public float getAccelerometerY () {
		return accelerometerValues[1];
	}

	@Override public float getAccelerometerZ () {
		return accelerometerValues[2];
	}

	@Override public void getTextInput (final TextInputListener listener, final String title, final String text) {
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
					public void onCancel(DialogInterface arg0) {
						listener.cancled();
					}
				});
				alert.show();
			}
		});
	}

	@Override public int getX () {
		return touchX[0];
	}

	@Override public int getY () {
		return touchY[0];
	}

	@Override public int getX (int pointer) {
		return touchX[pointer];
	}

	@Override public int getY (int pointer) {
		return touchY[pointer];
	}

	public boolean isTouched (int pointer) {
		return touched[pointer];
	}

	@Override public boolean isKeyPressed (int key) {
		synchronized (this) {
			if (key == Input.Keys.ANY_KEY)
				return keys.size() > 0;
			else
				return keys.contains(key);
		}
	}

	@Override public boolean isTouched () {
		return touched[0];
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

			keyEvents.clear();
			touchEvents.clear();
		}
	}

	boolean requestFocus = true;

	@Override public boolean onTouch (View view, MotionEvent event) {
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

	@Override public boolean onKey (View v, int keyCode, android.view.KeyEvent e) {
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
				keyEvents.add(event);
				keys.add(event.keyCode);
				break;
			case android.view.KeyEvent.ACTION_UP:
				event = usedKeyEvents.obtain();
				event.keyChar = 0;
				event.keyCode = e.getKeyCode();
				event.type = KeyEvent.KEY_UP;
				keyEvents.add(event);

				event = usedKeyEvents.obtain();
				event.keyChar = character;
				event.keyCode = 0;
				event.type = KeyEvent.KEY_TYPED;
				keyEvents.add(event);

				keys.remove(e.getKeyCode());
			}
		}

		if (catchBack && keyCode == android.view.KeyEvent.KEYCODE_BACK) return true;
		return false;
	}

	@Override public void onAccuracyChanged (Sensor arg0, int arg1) {

	}

	@Override public void onSensorChanged (SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			System.arraycopy(event.values, 0, accelerometerValues, 0, accelerometerValues.length);
		}
		if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			System.arraycopy(event.values, 0, magneticFieldValues, 0, magneticFieldValues.length);
		}
	}

	@Override public void setOnscreenKeyboardVisible (boolean visible) {
		InputMethodManager manager = (InputMethodManager)app.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (visible) {
			manager.showSoftInput(((AndroidGraphics)app.getGraphics()).getView(), 0);
		} else {
			manager.hideSoftInputFromWindow(((AndroidGraphics)app.getGraphics()).getView().getWindowToken(), 0);
		}
	}	

	@Override public void setCatchBackKey (boolean catchBack) {
		this.catchBack = catchBack;
	}

	@Override public void vibrate (int milliseconds) {
		vibrator.vibrate(milliseconds);
	}
	
	@Override public void vibrate (long[] pattern, int repeat) {
		vibrator.vibrate(pattern, repeat);
	}

	@Override public void cancelVibrate () {
		vibrator.cancel();
	}

	@Override public boolean justTouched () {
		return justTouched;
	}

	@Override public boolean isButtonPressed (int button) {
		if (button == Buttons.LEFT)
			return isTouched();
		else
			return false;
	}

	final float[] R = new float[9];	
	final float[] orientation = new float[3];			
	private void updateOrientation() {
		if(SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues)) {
			SensorManager.getOrientation(R, orientation);
			azimuth = (float)Math.toDegrees(orientation[0]);
			pitch = (float)Math.toDegrees(orientation[1]);
			roll = (float)Math.toDegrees(orientation[2]);			
		}
	}
	
	@Override public float getAzimuth () {
		if(!compassAvailable)
			return 0;
		
		updateOrientation();
		return azimuth;
	}

	@Override public float getPitch () {
		if(!compassAvailable)
			return 0;
		
		updateOrientation();
		return pitch;
	}

	@Override public float getRoll () {
		if(!compassAvailable)
			return 0;
		
		updateOrientation();
		return roll;
	}
	
	void registerSensorListeners() {		
		if(config.useAccelerometer) {
			manager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
			if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
				accelerometerAvailable = false;
			} else {
				Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
				accelerometerAvailable = manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);				
			}
		} else accelerometerAvailable = false;
		
		if(config.useCompass) {
			if(manager == null) manager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
			Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			if(sensor != null) {
				compassAvailable = accelerometerAvailable;
				if(compassAvailable) {
					compassAvailable = manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
				}
			} else {
				compassAvailable = false;
			}
		} else compassAvailable = false;
		Gdx.app.log("AndroidInput", "sensor listener setup");
	}
	
	void unregisterSensorListeners() {	
		if(manager != null) {
			manager.unregisterListener(this);
			manager = null;
		}
		Gdx.app.log("AndroidInput", "sensor listener tear down");
	}
	
	@Override public InputProcessor getInputProcessor() {
		return this.processor;
	}

	@Override public boolean isPeripheralAvailable (Peripheral peripheral) {
		if(peripheral == Peripheral.Accelerometer) return accelerometerAvailable;
		if(peripheral == Peripheral.Compass) return compassAvailable;
		if(peripheral == Peripheral.HardwareKeyboard) return keyboardAvailable;
		if(peripheral == Peripheral.OnscreenKeyboard) return true;
		if(peripheral == Peripheral.Vibrator) return vibrator != null;
		if(peripheral == Peripheral.MultitouchScreen) return hasMultitouch;
		return false;
	}
}

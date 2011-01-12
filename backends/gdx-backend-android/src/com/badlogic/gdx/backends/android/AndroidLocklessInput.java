/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.backends.android;

import java.util.HashSet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.AtomicQueue;

/**
 * An implementation of the {@link Input} interface for Android.
 * 
 * @author mzechner
 * 
 */
public final class AndroidLocklessInput implements Input, OnKeyListener, OnTouchListener, SensorEventListener {
	static class KeyEvent {
		static final int KEY_DOWN = 0;
		static final int KEY_UP = 1;
		static final int KEY_TYPED = 2;

		long timeStamp;
		int type;
		int keyCode;
		char keyChar;
	}

	static class TouchEvent {
		static final int TOUCH_DOWN = 0;
		static final int TOUCH_UP = 1;
		static final int TOUCH_DRAGGED = 2;

		long timeStamp;
		int type;
		int x;
		int y;
		int pointer;
	}

	AtomicQueue<KeyEvent> keyEvents = new AtomicQueue<KeyEvent>(64);
	AtomicQueue<KeyEvent> freeKeyEvents = new AtomicQueue<KeyEvent>(64);

	AtomicQueue<TouchEvent> touchEvents = new AtomicQueue<TouchEvent>(64);
	AtomicQueue<TouchEvent> freeTouchEvents = new AtomicQueue<TouchEvent>(64);

	int[] touchX = new int[10];
	int[] touchY = new int[10];
	boolean[] touched = new boolean[10];
	final boolean hasMultitouch;
	private HashSet<Integer> keys = new HashSet<Integer>();
	private SensorManager manager;
	public boolean accelerometerAvailable = false;
	private final float[] accelerometerValues = new float[3];
	private String text = null;
	private TextInputListener textListener = null;
	private Handler handle;
	final AndroidApplication app;
	private final AndroidLocklessTouchHandler touchHandler;
	private int sleepTime = 0;
	private boolean catchBack = false;
	private Vibrator vibrator;
	private boolean justTouched = false;

	private InputProcessor processor;

	public AndroidLocklessInput (AndroidApplication activity, View view, int sleepTime) {
		view.setOnKeyListener(this);
		view.setOnTouchListener(this);
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.requestFocusFromTouch();

		manager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
		if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
			accelerometerAvailable = false;
		} else {
			Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
			if (!manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME))
				accelerometerAvailable = false;
			else
				accelerometerAvailable = true;
		}

		handle = new Handler();
		this.app = activity;
		this.sleepTime = sleepTime;
		int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
		if (sdkVersion >= 5)
			touchHandler = new AndroidLocklessMultiTouchHandler();
		else
			touchHandler = new AndroidLocklessSingleTouchHandler();
		hasMultitouch = touchHandler instanceof AndroidLocklessMultiTouchHandler
			&& ((AndroidLocklessMultiTouchHandler)touchHandler).supportsMultitouch(activity);

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
				AlertDialog.Builder alert = new AlertDialog.Builder(AndroidLocklessInput.this.app);
				alert.setTitle(title);
				final EditText input = new EditText(AndroidLocklessInput.this.app);
				input.setText(text);
				input.setSingleLine();
				alert.setView(input);
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick (DialogInterface dialog, int whichButton) {
						listener.input(input.getText().toString());
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

	@Override public boolean isAccelerometerAvailable () {
		return accelerometerAvailable;
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
		InputProcessor processor;
		synchronized (this) {
			processor = this.processor;
		}

		justTouched = false;

		if (processor != null) {
			KeyEvent e;
			while ((e = keyEvents.poll()) != null) {
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
				freeKeyEvents.put(e);
			}

			TouchEvent te = null;
			while ((te = touchEvents.poll()) != null) {
				switch (te.type) {
				case TouchEvent.TOUCH_DOWN:
					processor.touchDown(te.x, te.y, te.pointer, Buttons.LEFT);
					justTouched = true;
					break;
				case TouchEvent.TOUCH_UP:
					processor.touchUp(te.x, te.y, te.pointer, Buttons.LEFT);
					break;
				case TouchEvent.TOUCH_DRAGGED:
					processor.touchDragged(te.x, te.y, te.pointer);
				}
				freeTouchEvents.put(te);
			}
		} else {
			TouchEvent e = null;
			while ((e = touchEvents.poll()) != null) {
				if (e.type == TouchEvent.TOUCH_DOWN) justTouched = true;
				freeTouchEvents.put(e);
			}

			KeyEvent ke = null;
			while ((ke = keyEvents.poll()) != null) {
				freeKeyEvents.put(ke);
			}
		}
	}

	boolean requestFocus = true;

	@Override public boolean onTouch (View view, MotionEvent event) {
		if (requestFocus) {
			view.requestFocus();
			view.requestFocusFromTouch();
			requestFocus = false;
		}

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
		char character = (char)e.getUnicodeChar();
		// Android doesn't report a unicode char for back space. hrm...
		if (keyCode == 67) character = '\b';

		KeyEvent event = null;
		switch (e.getAction()) {
		case android.view.KeyEvent.ACTION_DOWN:
			event = freeKeyEvents.poll();
			if (event == null) event = new KeyEvent();
			event.keyChar = 0;
			event.keyCode = e.getKeyCode();
			event.type = KeyEvent.KEY_DOWN;
			keyEvents.put(event);
			synchronized (this) {
				keys.add(event.keyCode);
			}
			break;
		case android.view.KeyEvent.ACTION_UP:
			event = freeKeyEvents.poll();
			if (event == null) event = new KeyEvent();
			event.keyChar = 0;
			event.keyCode = e.getKeyCode();
			event.type = KeyEvent.KEY_UP;
			keyEvents.put(event);

			event = freeKeyEvents.poll();
			if (event == null) event = new KeyEvent();
			event.keyChar = character;
			event.keyCode = 0;
			event.type = KeyEvent.KEY_TYPED;
			keyEvents.put(event);

			synchronized (this) {
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
	}

	@Override public boolean supportsMultitouch () {
		return hasMultitouch;
	}

	@Override public void setOnscreenKeyboardVisible (boolean visible) {
		InputMethodManager manager = (InputMethodManager)app.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (visible) {
			manager.showSoftInput(((AndroidGraphics)app.getGraphics()).getView(), 0);
		} else {
			manager.hideSoftInputFromWindow(((AndroidGraphics)app.getGraphics()).getView().getWindowToken(), 0);
		}
	}

	@Override public boolean supportsOnscreenKeyboard () {
		return true;
	}

	@Override public void setCatchBackKey (boolean catchBack) {
		this.catchBack = catchBack;
	}

	@Override public void vibrate (int milliseconds) {
		vibrator.vibrate(milliseconds);
	}

	@Override public boolean supportsVibrator () {
		return true;
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
}

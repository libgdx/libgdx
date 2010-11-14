/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * An implementation of the {@link Input} interface for Android.
 * 
 * @author mzechner
 * 
 */
public final class AndroidInput implements Input, OnKeyListener, OnTouchListener,
		SensorEventListener {
	class KeyEvent {
		static final int KEY_DOWN = 0;
		static final int KEY_UP = 1;
		static final int KEY_TYPED = 2;

		int type;
		int keyCode;
		char keyChar;
	}

	class TouchEvent {
		static final int TOUCH_DOWN = 0;
		static final int TOUCH_UP = 1;
		static final int TOUCH_DRAGGED = 2;

		int type;
		int x;
		int y;
		int pointer;
	}

	Pool<KeyEvent> freeKeyEvents = new Pool<KeyEvent>(
			new PoolObjectFactory<KeyEvent>() {

				@Override
				public KeyEvent createObject() {
					return new KeyEvent();
				}
			}, 1000);

	Pool<TouchEvent> freeTouchEvents = new Pool<TouchEvent>(
			new PoolObjectFactory<TouchEvent>() {

				@Override
				public TouchEvent createObject() {
					return new TouchEvent();
				}
			}, 1000);

	List<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
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
	private final AndroidTouchHandler touchHandler;
	private int sleepTime = 0;
	private boolean catchBack = false;

	public AndroidInput(AndroidApplication activity, View view, int sleepTime) {
		view.setOnKeyListener(this);
		view.setOnTouchListener(this);
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.requestFocusFromTouch();

		manager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
			accelerometerAvailable = false;
		}
		else {
			Sensor accelerometer = manager.getSensorList(
					Sensor.TYPE_ACCELEROMETER).get(0);
			if (!manager.registerListener(this, accelerometer,
					SensorManager.SENSOR_DELAY_GAME))
				accelerometerAvailable = false;
			else
				accelerometerAvailable = true;
		}

		handle = new Handler();
		this.app = activity;
		this.sleepTime = sleepTime;
		int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
		if (sdkVersion >= 5)
			touchHandler = new AndroidMultiTouchHandler();
		else
			touchHandler = new AndroidSingleTouchHandler();
		hasMultitouch = touchHandler instanceof AndroidMultiTouchHandler
				&& ((AndroidMultiTouchHandler)touchHandler)
						.supportsMultitouch(activity);
	}

	@Override
	public float getAccelerometerX() {
		return accelerometerValues[0];
	}

	@Override
	public float getAccelerometerY() {
		return accelerometerValues[1];
	}

	@Override
	public float getAccelerometerZ() {
		return accelerometerValues[2];
	}

	@Override
	public void getTextInput(final TextInputListener listener,
			final String title, final String text) {
		handle.post(new Runnable() {
			public void run() {
				AlertDialog.Builder alert = new AlertDialog.Builder(
						AndroidInput.this.app);
				alert.setTitle(title);
				final EditText input = new EditText(AndroidInput.this.app);
				input.setText(text);
				input.setSingleLine();
				alert.setView(input);
				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								listener.input(input.getText().toString());
							}
						});
				alert.show();
			}
		});
	}

	@Override
	public int getX() {
		return touchX[0];
	}

	@Override
	public int getY() {
		return touchY[0];
	}

	@Override
	public int getX(int pointer) {
		return touchX[pointer];
	}

	@Override
	public int getY(int pointer) {
		return touchY[pointer];
	}

	public boolean isTouched(int pointer) {
		return touched[pointer];
	}

	@Override
	public boolean isAccelerometerAvailable() {
		return accelerometerAvailable;
	}

	@Override
	public boolean isKeyPressed(int key) {
		synchronized (this) {
			if (key == Input.Keys.ANY_KEY)
				return keys.size() > 0;
			else
				return keys.contains(key);
		}
	}

	@Override
	public boolean isTouched() {
		return touched[0];
	}
	
	@Override
	public void processEvents(InputProcessor listener) {
		synchronized(this) {
			if(listener!=null) {		
				int len = keyEvents.size();
				for(int i=0; i < len; i++) {
					KeyEvent e = keyEvents.get(i);
					switch(e.type) {
					case KeyEvent.KEY_DOWN:
						listener.keyDown(e.keyCode);
						break;
					case KeyEvent.KEY_UP:
						listener.keyUp(e.keyCode);
						break;
					case KeyEvent.KEY_TYPED:
						listener.keyTyped(e.keyChar);
					}
					freeKeyEvents.free(e);
				}					
				
				len = touchEvents.size();
				for(int i=0; i < len; i++) {
					TouchEvent e = touchEvents.get(i);
					switch(e.type) {
					case TouchEvent.TOUCH_DOWN:
						listener.touchDown(e.x, e.y, e.pointer);
						break;
					case TouchEvent.TOUCH_UP:
						listener.touchUp(e.x, e.y, e.pointer);
						break;
					case TouchEvent.TOUCH_DRAGGED:
						listener.touchDragged(e.x, e.y, e.pointer);
					}
					freeTouchEvents.free(e);
				}
			}
			
			keyEvents.clear();
			touchEvents.clear();
		}
	}

	boolean requestFocus = true;
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (requestFocus) {
			view.requestFocus();
			view.requestFocusFromTouch();
			requestFocus = false;
		}
		synchronized (this) {
			touchHandler.onTouch(event, this);
		}
		if (sleepTime != 0)
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
			}
		return true;
	}

	@Override
	public boolean onKey(View v, int keyCode, android.view.KeyEvent e) {
		synchronized (this) {
			char character = (char) e.getUnicodeChar();
			// Android doesn't report a unicode char for back space. hrm...
			if (keyCode == 67)
				character = '\b';

			KeyEvent event = null;
			switch (e.getAction()) {
			case android.view.KeyEvent.ACTION_DOWN:
				event = freeKeyEvents.newObject();
				event.keyChar = 0;
				event.keyCode = e.getKeyCode();
				event.type = KeyEvent.KEY_DOWN;
				keyEvents.add(event);
				keys.add(event.keyCode);
				break;
			case android.view.KeyEvent.ACTION_UP:
				event = freeKeyEvents.newObject();
				event.keyChar = 0;
				event.keyCode = e.getKeyCode();
				event.type = KeyEvent.KEY_UP;
				keyEvents.add(event);				

				event = freeKeyEvents.newObject();
				event.keyChar = character;
				event.keyCode = 0;
				event.type = KeyEvent.KEY_TYPED;
				keyEvents.add(event);
				
				keys.remove(e.getKeyCode());
			}
		}

		if (catchBack && keyCode == android.view.KeyEvent.KEYCODE_BACK)
			return true;
		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			System.arraycopy(event.values, 0, accelerometerValues, 0,
					accelerometerValues.length);
		}
	}

	@Override
	public boolean supportsMultitouch() {
		return hasMultitouch;
	}

	@Override
	public void setOnscreenKeyboardVisible(boolean visible) {
		InputMethodManager manager = (InputMethodManager) app
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (visible) {
			manager.showSoftInput(((AndroidGraphics) app.getGraphics())
					.getView(), 0);
		} else {
			manager.hideSoftInputFromWindow(((AndroidGraphics) app
					.getGraphics()).getView().getWindowToken(), 0);
		}
	}

	@Override
	public boolean supportsOnscreenKeyboard() {
		return true;
	}

	@Override
	public void setCatchBackKey(boolean catchBack) {
		this.catchBack = catchBack;
	}
}

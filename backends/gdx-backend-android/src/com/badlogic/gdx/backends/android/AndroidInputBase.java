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
public abstract class AndroidInputBase implements Input, OnKeyListener, OnTouchListener {
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
		public static final int TOUCH_DOWN = 0;
		public static final int TOUCH_UP = 1;
		public static final int TOUCH_DRAGGED = 2;

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
	protected boolean hasMultitouch;
	private IntMap<Object> keys = new IntMap<Object>();
	protected SensorManager manager;
	public boolean accelerometerAvailable = false;
	protected final float[] accelerometerValues = new float[3];
//	private String text = null;
//	private TextInputListener textListener = null;
//	private Handler handle;
	protected AndroidApplicationBase app;
//	private final AndroidTouchHandler touchHandler;
	protected int sleepTime = 0;
	private boolean catchBack = false;
	private boolean catchMenu = false;
	protected Vibrator vibrator;
	protected boolean compassAvailable = false;
	boolean keyboardAvailable;
	protected final float[] magneticFieldValues = new float[3];
	private float azimuth = 0;
	private float pitch = 0;
	private float roll = 0;
	private float inclination = 0;
	protected boolean justTouched = false;
	protected InputProcessor processor;
	protected AndroidApplicationConfiguration config;
	protected Orientation nativeOrientation;
	protected long currentEventTimeStamp = System.nanoTime();
//	private final AndroidOnscreenKeyboard onscreenKeyboard;

//	private SensorEventListener accelerometerListener;
//	private SensorEventListener compassListener;

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
	public abstract void getTextInput (final TextInputListener listener, final String title, final String text);
	
	public abstract void getPlaceholderTextInput (final TextInputListener listener, final String title, final String placeholder);

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

	abstract protected void processEvents ();

	boolean requestFocus = true;

	@Override
	abstract public boolean onTouch (View view, MotionEvent event);
	
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
	abstract public void setOnscreenKeyboardVisible (final boolean visible);

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

	abstract protected void registerSensorListeners ();
	
	abstract protected void unregisterSensorListeners ();

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

}

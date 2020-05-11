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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnGenericMotionListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** An implementation of the {@link Input} interface for Android.
 * 
 * @author mzechner */
/** @author jshapcot */
public class AndroidInputImpl implements AndroidInput {

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
		static final int TOUCH_SCROLLED = 3;
		static final int TOUCH_MOVED = 4;

		long timeStamp;
		int type;
		int x;
		int y;
		int scrollAmount;
		int button;
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

	public static final int NUM_TOUCHES = 20;
	public static final int SUPPORTED_KEYS = 260;
	
	ArrayList<OnKeyListener> keyListeners = new ArrayList();
	ArrayList<KeyEvent> keyEvents = new ArrayList();
	ArrayList<TouchEvent> touchEvents = new ArrayList();
	int[] touchX = new int[NUM_TOUCHES];
	int[] touchY = new int[NUM_TOUCHES];
	int[] deltaX = new int[NUM_TOUCHES];
	int[] deltaY = new int[NUM_TOUCHES];
	boolean[] touched = new boolean[NUM_TOUCHES];
	int[] button = new int[NUM_TOUCHES];
	int[] realId = new int[NUM_TOUCHES];
	float[] pressure = new float[NUM_TOUCHES];
	final boolean hasMultitouch;
	private int keyCount = 0;
	private boolean[] keys = new boolean[SUPPORTED_KEYS];
	private boolean keyJustPressed = false;
	private boolean[] justPressedKeys = new boolean[SUPPORTED_KEYS];
	private boolean[] justPressedButtons = new boolean[NUM_TOUCHES];
	private SensorManager manager;
	public boolean accelerometerAvailable = false;
	protected final float[] accelerometerValues = new float[3];
	public boolean gyroscopeAvailable = false;
	protected final float[] gyroscopeValues = new float[3];
	private Handler handle;
	final Application app;
	final Context context;
	protected final AndroidTouchHandler touchHandler;
	private int sleepTime = 0;
	private IntSet keysToCatch = new IntSet();
	protected final Vibrator vibrator;
	private boolean compassAvailable = false;
	private boolean rotationVectorAvailable = false;
	boolean keyboardAvailable;
	protected final float[] magneticFieldValues = new float[3];
	protected final float[] rotationVectorValues = new float[3];
	private float azimuth = 0;
	private float pitch = 0;
	private float roll = 0;
	private boolean justTouched = false;
	private InputProcessor processor;
	private final AndroidApplicationConfiguration config;
	protected final Orientation nativeOrientation;
	private long currentEventTimeStamp = 0;

	private SensorEventListener accelerometerListener;
	private SensorEventListener gyroscopeListener;
	private SensorEventListener compassListener;
	private SensorEventListener rotationVectorListener;

	private final ArrayList<OnGenericMotionListener> genericMotionListeners = new ArrayList();
	private final AndroidMouseHandler mouseHandler;

	public AndroidInputImpl (Application activity, Context context, Object view, AndroidApplicationConfiguration config) {
		// we hook into View, for LWPs we call onTouch below directly from
		// within the AndroidLivewallpaperEngine#onTouchEvent() method.
		if (view instanceof View) {
			View v = (View)view;
			v.setOnKeyListener(this);
			v.setOnTouchListener(this);
			v.setFocusable(true);
			v.setFocusableInTouchMode(true);
			v.requestFocus();
			v.setOnGenericMotionListener(this);
		}
		this.config = config;
		this.mouseHandler = new AndroidMouseHandler();

		for (int i = 0; i < realId.length; i++)
			realId[i] = -1;
		handle = new Handler();
		this.app = activity;
		this.context = context;
		this.sleepTime = config.touchSleepTime;
		touchHandler = new AndroidTouchHandler();
		hasMultitouch = touchHandler.supportsMultitouch(context);

		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

		int rotation = getRotation();
		DisplayMode mode = app.getGraphics().getDisplayMode();
		if (((rotation == 0 || rotation == 180) && (mode.width >= mode.height))
			|| ((rotation == 90 || rotation == 270) && (mode.width <= mode.height))) {
			nativeOrientation = Orientation.Landscape;
		} else {
			nativeOrientation = Orientation.Portrait;
		}

		// this is for backward compatibility: libGDX always caught the circle button, original comment:
		// circle button on Xperia Play shouldn't need catchBack == true
		keysToCatch.add(Keys.BUTTON_CIRCLE);
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
	public float getGyroscopeX () {
		return gyroscopeValues[0];
	}

	@Override
	public float getGyroscopeY () {
		return gyroscopeValues[1];
	}

	@Override
	public float getGyroscopeZ () {
		return gyroscopeValues[2];
	}

	@Override
	public void getTextInput (final TextInputListener listener, final String title, final String text, final String hint) {
		handle.post(new Runnable() {
			public void run () {
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
				alert.setTitle(title);
				final EditText input = new EditText(context);
				input.setHint(hint);
				input.setText(text);				
				input.setSingleLine();
				alert.setView(input);
				alert.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick (DialogInterface dialog, int whichButton) {
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run () {
								listener.input(input.getText().toString());
							}
						});
					}
				});
				alert.setNegativeButton(context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
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

	@Override
	public int getMaxPointers () {
		return NUM_TOUCHES;
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
	public float getPressure () {
		return getPressure(0);
	}

	@Override
	public float getPressure (int pointer) {
		return pressure[pointer];
	}

	@Override
	public void setKeyboardAvailable (boolean available) {
		this.keyboardAvailable = available;
	}

	@Override
	public synchronized boolean isKeyPressed (int key) {
		if (key == Input.Keys.ANY_KEY) {
			return keyCount > 0;
		}
		if (key < 0 || key >= SUPPORTED_KEYS) {
			return false;
		}
		return keys[key];
	}

	@Override
	public synchronized boolean isKeyJustPressed (int key) {
		if (key == Input.Keys.ANY_KEY) {
			return keyJustPressed;
		}
		if (key < 0 || key >= SUPPORTED_KEYS) {
			return false;
		}
		return justPressedKeys[key];
	}

	@Override
	public boolean isTouched () {
		synchronized (this) {
			if (hasMultitouch) {
				for (int pointer = 0; pointer < NUM_TOUCHES; pointer++) {
					if (touched[pointer]) {
						return true;
					}
				}
			}
			return touched[0];
		}
	}

	public void setInputProcessor (InputProcessor processor) {
		synchronized (this) {
			this.processor = processor;
		}
	}

	@Override
	public void processEvents () {
		synchronized (this) {
			if (justTouched) {
				justTouched = false;
				for (int i = 0; i < justPressedButtons.length; i++) {
					justPressedButtons[i] = false;
				}
			}
			if (keyJustPressed) {
				keyJustPressed = false;
				for (int i = 0; i < justPressedKeys.length; i++) {
					justPressedKeys[i] = false;
				}
			}

			if (processor != null) {
				final InputProcessor processor = this.processor;

				int len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					KeyEvent e = keyEvents.get(i);
					currentEventTimeStamp = e.timeStamp;
					switch (e.type) {
					case KeyEvent.KEY_DOWN:
						processor.keyDown(e.keyCode);
						keyJustPressed = true;
						justPressedKeys[e.keyCode] = true;
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
						processor.touchDown(e.x, e.y, e.pointer, e.button);
						justTouched = true;
						justPressedButtons[e.button] = true;
						break;
					case TouchEvent.TOUCH_UP:
						processor.touchUp(e.x, e.y, e.pointer, e.button);
						break;
					case TouchEvent.TOUCH_DRAGGED:
						processor.touchDragged(e.x, e.y, e.pointer);
						break;
					case TouchEvent.TOUCH_MOVED:
						processor.mouseMoved(e.x, e.y);
						break;
					case TouchEvent.TOUCH_SCROLLED:
						processor.scrolled(e.scrollAmount);
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

			if (touchEvents.isEmpty()) {
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
		if (requestFocus && view != null) {
			view.setFocusableInTouchMode(true);
			view.requestFocus();
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

// TODO Seems unused. Delete when confirmed.
//	/** Called in {@link AndroidLiveWallpaperService} on tap
//	 * @param x
//	 * @param y */
//	public void onTap (int x, int y) {
//		postTap(x, y);
//	}
//
//	/** Called in {@link AndroidLiveWallpaperService} on drop
//	 * @param x
//	 * @param y */
//	public void onDrop (int x, int y) {
//		postTap(x, y);
//	}
//
//	protected void postTap (int x, int y) {
//		synchronized (this) {
//			TouchEvent event = usedTouchEvents.obtain();
//			event.timeStamp = System.nanoTime();
//			event.pointer = 0;
//			event.x = x;
//			event.y = y;
//			event.type = TouchEvent.TOUCH_DOWN;
//			touchEvents.add(event);
//
//			event = usedTouchEvents.obtain();
//			event.timeStamp = System.nanoTime();
//			event.pointer = 0;
//			event.x = x;
//			event.y = y;
//			event.type = TouchEvent.TOUCH_UP;
//			touchEvents.add(event);
//		}
//		Gdx.app.getGraphics().requestRendering();
//	}

	@Override
	public boolean onKey (View v, int keyCode, android.view.KeyEvent e) {
		for (int i = 0, n = keyListeners.size(); i < n; i++)
			if (keyListeners.get(i).onKey(v, keyCode, e)) return true;

		// If the key is held sufficiently long that it repeats, then the initial down is followed
		// additional key events with ACTION_DOWN and a non-zero value for getRepeatCount().
		// We are only interested in the first key down event here and must ignore all others
		if (e.getAction() == android.view.KeyEvent.ACTION_DOWN && e.getRepeatCount() > 0)
			return keysToCatch.contains(keyCode);

		synchronized (this) {
			KeyEvent event = null;

			if (e.getKeyCode() == android.view.KeyEvent.KEYCODE_UNKNOWN && e.getAction() == android.view.KeyEvent.ACTION_MULTIPLE) {
				String chars = e.getCharacters();
				for (int i = 0; i < chars.length(); i++) {
					event = usedKeyEvents.obtain();
					event.timeStamp = System.nanoTime();
					event.keyCode = 0;
					event.keyChar = chars.charAt(i);
					event.type = KeyEvent.KEY_TYPED;
					keyEvents.add(event);
				}
				return false;
			}

			char character = (char)e.getUnicodeChar();
			// Android doesn't report a unicode char for back space. hrm...
			if (keyCode == 67) character = '\b';
			if (e.getKeyCode() < 0 || e.getKeyCode() >= SUPPORTED_KEYS) {
				return false;
			}
			
			switch (e.getAction()) {
			case android.view.KeyEvent.ACTION_DOWN:
				event = usedKeyEvents.obtain();
				event.timeStamp = System.nanoTime();
				event.keyChar = 0;
				event.keyCode = e.getKeyCode();
				event.type = KeyEvent.KEY_DOWN;

				// Xperia hack for circle key. gah...
				if (keyCode == android.view.KeyEvent.KEYCODE_BACK && e.isAltPressed()) {
					keyCode = Keys.BUTTON_CIRCLE;
					event.keyCode = keyCode;
				}

				keyEvents.add(event);
				if (!keys[event.keyCode]) {
					keyCount++;
					keys[event.keyCode] = true;
				}
				break;
			case android.view.KeyEvent.ACTION_UP:
				long timeStamp = System.nanoTime();
				event = usedKeyEvents.obtain();
				event.timeStamp = timeStamp;
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
				event.timeStamp = timeStamp;
				event.keyChar = character;
				event.keyCode = 0;
				event.type = KeyEvent.KEY_TYPED;
				keyEvents.add(event);

				if (keyCode == Keys.BUTTON_CIRCLE) {
					if (keys[Keys.BUTTON_CIRCLE]) {
						keyCount--;
						keys[Keys.BUTTON_CIRCLE] = false;
					}
				} else {
					if (keys[e.getKeyCode()]) {
						keyCount--;
						keys[e.getKeyCode()] = false;
					}
				}
			}
			app.getGraphics().requestRendering();
		}

		return keysToCatch.contains(keyCode);
	}

	@Override
	public void setOnscreenKeyboardVisible (final boolean visible) {
		handle.post(new Runnable() {
			public void run () {
				InputMethodManager manager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
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
		setCatchKey(Keys.BACK, catchBack);
	}

	@Override
	public boolean isCatchBackKey() {
		return keysToCatch.contains(Keys.BACK);
	}

	@Override
	public void setCatchMenuKey (boolean catchMenu) {
		setCatchKey(Keys.MENU, catchMenu);
	}

	@Override
	public boolean isCatchMenuKey () {
		return keysToCatch.contains(Keys.MENU);
	}

	@Override
	public void setCatchKey (int keycode, boolean catchKey) {
		if (!catchKey) {
			keysToCatch.remove(keycode);
		} else if (catchKey) {
			keysToCatch.add(keycode);
		}
	}

	@Override
	public boolean isCatchKey (int keycode) {
		return keysToCatch.contains(keyCount);
	}

	@Override
	public void vibrate (int milliseconds) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
		else
			vibrator.vibrate(milliseconds);
	}

	@Override
	public void vibrate (long[] pattern, int repeat) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat));
		else
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
		synchronized (this) {
			if (hasMultitouch) {
				for (int pointer = 0; pointer < NUM_TOUCHES; pointer++) {
					if (touched[pointer] && (this.button[pointer] == button)) {
						return true;
					}
				}
			}
			return (touched[0] && (this.button[0] == button));
		}
	}

	@Override
	public boolean isButtonJustPressed(int button) {
		if(button < 0 || button > NUM_TOUCHES) return false;
		return justPressedButtons[button];
	}

	final float[] R = new float[9];
	final float[] orientation = new float[3];

	private void updateOrientation () {
		if (rotationVectorAvailable){
			SensorManager.getRotationMatrixFromVector(R, rotationVectorValues);
		} else if (!SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues)) {
				return; // compass + accelerometer in free fall
		}
		SensorManager.getOrientation(R, orientation);
		azimuth = (float)Math.toDegrees(orientation[0]);
		pitch = (float)Math.toDegrees(orientation[1]);
		roll = (float)Math.toDegrees(orientation[2]);
	}

	/** Returns the rotation matrix describing the devices rotation as per <a href=
	 * "http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])"
	 * >SensorManager#getRotationMatrix(float[], float[], float[], float[])</a>. Does not manipulate the matrix if the platform
	 * does not have an accelerometer and compass, or a rotation vector sensor.
	 * @param matrix */
	public void getRotationMatrix (float[] matrix) {
		if (rotationVectorAvailable)
			SensorManager.getRotationMatrixFromVector(matrix, rotationVectorValues);
		else // compass + accelerometer
			SensorManager.getRotationMatrix(matrix, null, accelerometerValues, magneticFieldValues);
	}

	@Override
	public float getAzimuth () {
		if (!compassAvailable && !rotationVectorAvailable) return 0;

		updateOrientation();
		return azimuth;
	}

	@Override
	public float getPitch () {
		if (!compassAvailable && !rotationVectorAvailable) return 0;

		updateOrientation();
		return pitch;
	}

	@Override
	public float getRoll () {
		if (!compassAvailable && !rotationVectorAvailable) return 0;

		updateOrientation();
		return roll;
	}

	void registerSensorListeners () {
		if (config.useAccelerometer) {
			manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
			if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).isEmpty()) {
				accelerometerAvailable = false;
			} else {
				Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
				accelerometerListener = new SensorListener();
				accelerometerAvailable = manager.registerListener(accelerometerListener, accelerometer,
					config.sensorDelay);
			}
		} else
			accelerometerAvailable = false;
		
		if (config.useGyroscope) {
			manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
			if (manager.getSensorList(Sensor.TYPE_GYROSCOPE).isEmpty()) {
				gyroscopeAvailable = false;
			} else {
				Sensor gyroscope = manager.getSensorList(Sensor.TYPE_GYROSCOPE).get(0);
				gyroscopeListener = new SensorListener();
				gyroscopeAvailable = manager.registerListener(gyroscopeListener, gyroscope,
					config.sensorDelay);
			}
		} else
			gyroscopeAvailable = false;

		rotationVectorAvailable = false;
		if (config.useRotationVectorSensor){
			if (manager == null) manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
			List<Sensor> rotationVectorSensors = manager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
			if (!rotationVectorSensors.isEmpty()){
				rotationVectorListener = new SensorListener();
				for (Sensor sensor : rotationVectorSensors){ // favor AOSP sensor
					if (sensor.getVendor().equals("Google Inc.") && sensor.getVersion() == 3){
						rotationVectorAvailable = manager.registerListener(rotationVectorListener, sensor,
							config.sensorDelay);
						break;
					}
				}
				if (!rotationVectorAvailable)
					rotationVectorAvailable = manager.registerListener(rotationVectorListener, rotationVectorSensors.get(0),
						config.sensorDelay);
			}
		}
		
		if (config.useCompass && !rotationVectorAvailable) {
			if (manager == null) manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
			Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			if (sensor != null) {
				compassAvailable = accelerometerAvailable;
				if (compassAvailable) {
					compassListener = new SensorListener();
					compassAvailable = manager.registerListener(compassListener, sensor, config.sensorDelay);
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
			if (gyroscopeListener != null) {
				manager.unregisterListener(gyroscopeListener);
				gyroscopeListener = null;
			}
			if (rotationVectorListener != null) {
				manager.unregisterListener(rotationVectorListener);
				rotationVectorListener = null;
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
		if (peripheral == Peripheral.Gyroscope) return gyroscopeAvailable;
		if (peripheral == Peripheral.Compass) return compassAvailable;
		if (peripheral == Peripheral.HardwareKeyboard) return keyboardAvailable;
		if (peripheral == Peripheral.OnscreenKeyboard) return true;
		if (peripheral == Peripheral.Vibrator) return vibrator != null && vibrator.hasVibrator();
		if (peripheral == Peripheral.MultitouchScreen) return hasMultitouch;
		if (peripheral == Peripheral.RotationVector) return rotationVectorAvailable;
		if (peripheral == Peripheral.Pressure) return true;
		return false;
	}

	public int getFreePointerIndex () {
		int len = realId.length;
		for (int i = 0; i < len; i++) {
			if (realId[i] == -1) return i;
		}

		pressure = resize(pressure);
		realId = resize(realId);
		touchX = resize(touchX);
		touchY = resize(touchY);
		deltaX = resize(deltaX);
		deltaY = resize(deltaY);
		touched = resize(touched);
		button = resize(button);

		return len;
	}

	private int[] resize (int[] orig) {
		int[] tmp = new int[orig.length + 2];
		System.arraycopy(orig, 0, tmp, 0, orig.length);
		return tmp;
	}

	private boolean[] resize (boolean[] orig) {
		boolean[] tmp = new boolean[orig.length + 2];
		System.arraycopy(orig, 0, tmp, 0, orig.length);
		return tmp;
	}

	private float[] resize (float[] orig) {
		float[] tmp = new float[orig.length + 2];
		System.arraycopy(orig, 0, tmp, 0, orig.length);
		return tmp;
	}

	public int lookUpPointerIndex (int pointerId) {
		int len = realId.length;
		for (int i = 0; i < len; i++) {
			if (realId[i] == pointerId) return i;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			sb.append(i + ":" + realId[i] + " ");
		}
		Gdx.app.log("AndroidInput", "Pointer ID lookup failed: " + pointerId + ", " + sb.toString());
		return -1;
	}

	@Override
	public int getRotation () {
		int orientation = 0;

		if (context instanceof Activity) {
			orientation = ((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
		} else {
			orientation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		}

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

	@Override
	public void addKeyListener (OnKeyListener listener) {
		keyListeners.add(listener);
	}

	@Override
	public boolean onGenericMotion (View view, MotionEvent event) {
		if (mouseHandler.onGenericMotion(event, this)) return true;
		for (int i = 0, n = genericMotionListeners.size(); i < n; i++)
			if (genericMotionListeners.get(i).onGenericMotion(view, event)) return true;
		return false;
	}

	@Override
	public void addGenericMotionListener (OnGenericMotionListener listener) {
		genericMotionListeners.add(listener);
	}

	@Override
	public void onPause () {
		unregisterSensorListeners();

		// erase pointer ids. this sucks donkeyballs...
		Arrays.fill(realId, -1);

		// erase touched state. this also sucks donkeyballs...
		Arrays.fill(touched, false);
	}

	@Override
	public void onResume () {
		registerSensorListeners();
	}

	@Override
	public void onDreamingStarted () {
		registerSensorListeners();
	}

	@Override
	public void onDreamingStopped () {
		unregisterSensorListeners();
		// erase pointer ids. this sucks donkeyballs...
		Arrays.fill(realId, -1);
		// erase touched state. this also sucks donkeyballs...
		Arrays.fill(touched, false);
	}

	/** Our implementation of SensorEventListener. Because Android doesn't like it when we register more than one Sensor to a single
	 * SensorEventListener, we add one of these for each Sensor. Could use an anonymous class, but I don't see any harm in
	 * explicitly defining it here. Correct me if I am wrong. */
	private class SensorListener implements SensorEventListener {
		
		public SensorListener (){
			
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
			if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				if (nativeOrientation == Orientation.Portrait) {
					System.arraycopy(event.values, 0, gyroscopeValues, 0, gyroscopeValues.length);
				} else {
					gyroscopeValues[0] = event.values[1];
					gyroscopeValues[1] = -event.values[0];
					gyroscopeValues[2] = event.values[2];
				}
			}
			if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
				if (nativeOrientation == Orientation.Portrait) {
					System.arraycopy(event.values, 0, rotationVectorValues, 0, rotationVectorValues.length);
				} else {
					rotationVectorValues[0] = event.values[1];
					rotationVectorValues[1] = -event.values[0];
					rotationVectorValues[2] = event.values[2];
				}
			}
		}
	}
}

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

import tv.ouya.console.api.OuyaController;

import android.app.Activity;
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
import android.service.wallpaper.WallpaperService.Engine;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService.AndroidWallpaperEngine;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;

/** An implementation of the {@link Input} interface for Android.
 * 
 * @author mzechner */
/** @author jshapcot */
public class OuyaInput extends AndroidInput implements Input, OnKeyListener, OnTouchListener , OnGenericMotionListener{
	static class KeyEvent {
		static final int KEY_DOWN = 0;
		static final int KEY_UP = 1;
		static final int KEY_TYPED = 2;

		long timeStamp;
		int type;
		int keyCode;
		char keyChar;
		int numPlayer;
	}
	
	static class ControllerEvent {
		float amount;
		int axis;
		int numPlayer;
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

	Pool<KeyEvent> usedKeyEvents = new Pool<KeyEvent>(16, 1000) {
		protected KeyEvent newObject () {
			return new KeyEvent();
		}
	};
	Pool<ControllerEvent> usedControllerEvents = new Pool<ControllerEvent>(16, 1000) {
		protected ControllerEvent newObject () {
			return new ControllerEvent();
		}
	};
	Pool<TouchEvent> usedTouchEvents = new Pool<TouchEvent>(16, 1000) {
		protected TouchEvent newObject () {
			return new TouchEvent();
		}
	};

	ArrayList<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
	ArrayList<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	ArrayList<ControllerEvent> controllerEvents = new ArrayList<ControllerEvent>();
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
	
	private String text = null;
	private TextInputListener textListener = null;
	private Handler handle;
	
	private int sleepTime = 0;
	private boolean catchBack = false;
	private boolean catchMenu = false;
	
	private boolean compassAvailable = false;
	boolean keyboardAvailable;
	
	private float azimuth = 0;
	private float pitch = 0;
	private float roll = 0;
	private float inclination = 0;
	private boolean justTouched = false;
	private InputProcessor processor;
	private long currentEventTimeStamp = System.nanoTime();
	

	private SensorEventListener accelerometerListener;
	private SensorEventListener compassListener;

	public OuyaInput (Application activity, Context context, Object view, AndroidApplicationConfiguration config) {
		super(activity,context,view,config);
		if(view instanceof View) {
			View v = (View)view;
			v.setOnGenericMotionListener(this);
		}
		this.hasMultitouch=false;
	}

	

	@Override
	public void getTextInput (final TextInputListener listener, final String title, final String text) {
		handle.post(new Runnable() {
			public void run () {
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
				alert.setTitle(title);
				final EditText input = new EditText(context);
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
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
				alert.setTitle(title);
				final EditText input = new EditText(context);
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
				
				int len = controllerEvents.size();
				for (int i = 0; i < len; i++) {
					ControllerEvent e = controllerEvents.get(i);
					processor.axisMove(e.numPlayer, e.axis, e.amount);
					usedControllerEvents.free(e);
				}
				len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					KeyEvent e = keyEvents.get(i);
					currentEventTimeStamp = e.timeStamp;
					switch (e.type) {
					case KeyEvent.KEY_DOWN:
						processor.buttonDown(e.numPlayer, e.keyCode);
						break;
					case KeyEvent.KEY_UP:
						processor.buttonUp(e.numPlayer, e.keyCode);
						break;
					case KeyEvent.KEY_TYPED:
						processor.buttonTyped(e.numPlayer, e.keyCode);
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
				int len = controllerEvents.size();
				for (int i = 0; i < len; i++) {
					ControllerEvent e = controllerEvents.get(i);
					usedControllerEvents.free(e);
				}
				len = touchEvents.size();
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
			controllerEvents.clear();
			keyEvents.clear();
			touchEvents.clear();
		}
	}

	boolean requestFocus = true;

	
	
	/**
	 * Called in {@link AndroidLiveWallpaperService} on tap
	 * @param x
	 * @param y
	 */
	public void onTap(int x, int y) {
		postTap(x, y);
	}
	
	/**
	 * Called in {@link AndroidLiveWallpaperService} on drop
	 * @param x
	 * @param y
	 */
	public void onDrop(int x, int y) {
		postTap(x, y);
	}
	
	protected void postTap(int x, int y) {
		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.timeStamp = System.nanoTime();
			event.pointer = 0;
			event.x = x;
			event.y = y;
			event.type = TouchEvent.TOUCH_DOWN;
			touchEvents.add(event);
			
			event = usedTouchEvents.obtain();
			event.timeStamp = System.nanoTime();
			event.pointer = 0;
			event.x = x;
			event.y = y;
			event.type = TouchEvent.TOUCH_UP;
			touchEvents.add(event);
		}
		Gdx.app.getGraphics().requestRendering();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onKey (View v, int keyCode, android.view.KeyEvent e) {
		synchronized (this) {
			char character = (char)e.getUnicodeChar();
			// Android doesn't report a unicode char for back space. hrm...
			if (keyCode == 67) character = '\b';

			KeyEvent event = null;
			switch (e.getAction()) {
			case android.view.KeyEvent.ACTION_DOWN:
				OuyaController.onKeyDown(keyCode, e);
				for(int i =0;i<OuyaController.MAX_CONTROLLERS;i++){
					int button = OuyaController.interpret(keyCode);
					OuyaController control = OuyaController.getControllerByPlayer(i);
					if(control != null && control.getButton(button)){
						event = usedKeyEvents.obtain();
						event.keyChar = 0;
						event.keyCode = normalizeInput(OuyaController.interpret(keyCode));
						event.type = KeyEvent.KEY_DOWN;
						event.numPlayer=i;
						keyEvents.add(event);
						keys.put(event.keyCode, null);
						break;
					}
					
				}
				break;
			case android.view.KeyEvent.ACTION_UP:
				OuyaController.onKeyUp(keyCode, e);
				for(int i =0;i<OuyaController.MAX_CONTROLLERS;i++){
					int button = OuyaController.interpret(keyCode);
					OuyaController control = OuyaController.getControllerByPlayer(i);
					if(control != null && control.getButton(button)){
						event = usedKeyEvents.obtain();
						event.keyChar = 0;
						event.keyCode =normalizeInput(button);
						event.type = KeyEvent.KEY_UP;
						keyEvents.add(event);

						event = usedKeyEvents.obtain();
						event.keyChar = character;
						event.keyCode = 0;
						event.type = KeyEvent.KEY_TYPED;
						keyEvents.add(event);
						break;
				}
			}
				
				keys.remove(e.getKeyCode());
			}
			app.getGraphics().requestRendering();
		}
		return false;
	}

	@Override
	public void setOnscreenKeyboardVisible (final boolean visible) {
// onscreenKeyboard.setVisible(visible);
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

	

	/** Returns the rotation matrix describing the devices rotation as per <a href=
	 * "http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])"
	 * >SensorManager#getRotationMatrix(float[], float[], float[], float[])</a>. Does not manipulate the matrix if the platform
	 * does not have an accelerometer.
	 * @param matrix */
	

	

	



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
		Gdx.app.log("OuyaInput", "Pointer ID lookup failed: " + pointerId + ", " + buf.toString());
		return -1;
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
	

	@Override
	public boolean onGenericMotion(View view, MotionEvent Motionevent) {
		boolean handled = OuyaController.onGenericMotionEvent(Motionevent);
		synchronized(this){
		if(handled){
			
			for(int i = 0;i<OuyaController.MAX_CONTROLLERS;i++){
				OuyaController control = OuyaController.getControllerByPlayer(i);
				if(control!=null){
					for(Integer axi : OuyaController.AVAILABLE_AXES){
					ControllerEvent event = usedControllerEvents.obtain();
					event.axis=normalizeInput(axi);
					event.amount=control.getAxisValue(axi);
					event.numPlayer=control.getPlayerNum();
					controllerEvents.add(event);
					}
				}
			}
		}
		app.getGraphics().requestRendering();
		}
		return handled;
	}
	/*
	 * Ouya keys mapping in gdx keys
	 */
	private int normalizeInput(int ouyaNumber){
		switch(ouyaNumber){
		case OuyaController.BUTTON_O:
			return Keys.BUTTON_O;
		case OuyaController.BUTTON_U:
			return Keys.BUTTON_U;
		case OuyaController.BUTTON_Y:
			return Keys.BUTTON_Y;
		case OuyaController.BUTTON_A:
			return Keys.BUTTON_A;
		case OuyaController.BUTTON_L1:
			return Keys.BUTTON_L1;
		case OuyaController.BUTTON_L2:
			return Keys.BUTTON_L2;
		case OuyaController.BUTTON_L3:
			return Keys.BUTTON_L3;
		case OuyaController.BUTTON_R1:
			return Keys.BUTTON_R1;
		case OuyaController.BUTTON_R2:
			return Keys.BUTTON_R2;
		case OuyaController.BUTTON_R3:
			return Keys.BUTTON_R3;
		case OuyaController.AXIS_L2:
			return Keys.AXIS_L2;
		case OuyaController.AXIS_R2:
			return Keys.AXIS_R2;
		case OuyaController.AXIS_LS_X:
			return Keys.AXIS_LS_X;
		case OuyaController.AXIS_LS_Y:
			return Keys.AXIS_LS_Y;
		case OuyaController.AXIS_RS_Y:
			return Keys.AXIS_RS_Y;
		case OuyaController.AXIS_RS_X:
			return Keys.AXIS_RS_X;
		default :
			return ouyaNumber;
		
		}
		
	}
}
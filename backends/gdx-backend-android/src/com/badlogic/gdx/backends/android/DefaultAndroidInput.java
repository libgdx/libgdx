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

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.text.*;
import android.text.InputFilter.LengthFilter;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import com.badlogic.gdx.AbstractInput;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.android.keyboardheight.KeyboardHeightObserver;
import com.badlogic.gdx.backends.android.keyboardheight.KeyboardHeightProvider;
import com.badlogic.gdx.backends.android.keyboardheight.StandardKeyboardHeightProvider;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.input.TextInputWrapper;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;
import java.util.List;

/** An implementation of the {@link Input} interface for Android.
 *
 * @author mzechner
 * @author jshapcot */
public class DefaultAndroidInput extends AbstractInput implements AndroidInput, KeyboardHeightObserver {

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
		static final int TOUCH_CANCELLED = 5;

		long timeStamp;
		int type;
		int x;
		int y;
		int scrollAmountX;
		int scrollAmountY;
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
	protected final AndroidHaptics haptics;
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
	private PredictiveBackHandler predictiveBackHandler;

	private SensorEventListener accelerometerListener;
	private SensorEventListener gyroscopeListener;
	private SensorEventListener compassListener;
	private SensorEventListener rotationVectorListener;

	private final ArrayList<OnGenericMotionListener> genericMotionListeners = new ArrayList();
	private final AndroidMouseHandler mouseHandler;

	public DefaultAndroidInput (Application activity, Context context, Object view, AndroidApplicationConfiguration config) {
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

		haptics = new AndroidHaptics(context);

		if (Build.VERSION.SDK_INT >= 33 && context instanceof Activity) {
			this.predictiveBackHandler = new PredictiveBackHandler();
		}

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
		setCatchKey(Keys.BUTTON_CIRCLE, true);
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
	public void getTextInput (TextInputListener listener, String title, String text, String hint) {
		getTextInput(listener, title, text, hint, OnscreenKeyboardType.Default);
	}

	@Override
	public void getTextInput (final TextInputListener listener, final String title, final String text, final String hint,
		final OnscreenKeyboardType keyboardType) {
		handle.post(new Runnable() {
			public void run () {
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
				alert.setTitle(title);
				final EditText input = new EditText(context);
				if (keyboardType != OnscreenKeyboardType.Default) {
					input.setInputType(getAndroidInputType(keyboardType, false));
				}
				input.setHint(hint);
				input.setText(text);
				input.setSingleLine();
				if (keyboardType == OnscreenKeyboardType.Password) {
					input.setTransformationMethod(new PasswordTransformationMethod());
				}
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

	public static int getAndroidInputType (OnscreenKeyboardType type, boolean defaultDisableAutocorrection) {
		int inputType;
		switch (type) {
		case NumberPad:
			inputType = InputType.TYPE_CLASS_NUMBER;
			break;
		case PhonePad:
			inputType = InputType.TYPE_CLASS_PHONE;
			break;
		case Email:
			inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
			break;
		case Password:
			inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
			break;
		case URI:
			inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI;
			break;
		default:
			if (defaultDisableAutocorrection) {
				inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
					| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
			} else {
				inputType = InputType.TYPE_CLASS_TEXT;
			}
			break;
		}
		return inputType;
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
					case TouchEvent.TOUCH_CANCELLED:
						processor.touchCancelled(e.x, e.y, e.pointer, e.button);
						break;
					case TouchEvent.TOUCH_MOVED:
						processor.mouseMoved(e.x, e.y);
						break;
					case TouchEvent.TOUCH_SCROLLED:
						processor.scrolled(e.scrollAmountX, e.scrollAmountY);
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
// /** Called in {@link AndroidLiveWallpaperService} on tap
// * @param x
// * @param y */
// public void onTap (int x, int y) {
// postTap(x, y);
// }
//
// /** Called in {@link AndroidLiveWallpaperService} on drop
// * @param x
// * @param y */
// public void onDrop (int x, int y) {
// postTap(x, y);
// }
//
// protected void postTap (int x, int y) {
// synchronized (this) {
// TouchEvent event = usedTouchEvents.obtain();
// event.timeStamp = System.nanoTime();
// event.pointer = 0;
// event.x = x;
// event.y = y;
// event.type = TouchEvent.TOUCH_DOWN;
// touchEvents.add(event);
//
// event = usedTouchEvents.obtain();
// event.timeStamp = System.nanoTime();
// event.pointer = 0;
// event.x = x;
// event.y = y;
// event.type = TouchEvent.TOUCH_UP;
// touchEvents.add(event);
// }
// Gdx.app.getGraphics().requestRendering();
// }

	@Override
	public boolean onKey (View v, int keyCode, android.view.KeyEvent e) {
		for (int i = 0, n = keyListeners.size(); i < n; i++)
			if (keyListeners.get(i).onKey(v, keyCode, e)) return true;

		// If the key is held sufficiently long that it repeats, then the initial down is followed
		// additional key events with ACTION_DOWN and a non-zero value for getRepeatCount().
		// We are only interested in the first key down event here and must ignore all others
		if (e.getAction() == android.view.KeyEvent.ACTION_DOWN && e.getRepeatCount() > 0) return isCatchKey(keyCode);

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
			if (e.getKeyCode() < 0 || e.getKeyCode() > Keys.MAX_KEYCODE) {
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
				if (!pressedKeys[event.keyCode]) {
					pressedKeyCount++;
					pressedKeys[event.keyCode] = true;
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
					if (pressedKeys[Keys.BUTTON_CIRCLE]) {
						pressedKeyCount--;
						pressedKeys[Keys.BUTTON_CIRCLE] = false;
					}
				} else {
					if (pressedKeys[e.getKeyCode()]) {
						pressedKeyCount--;
						pressedKeys[e.getKeyCode()] = false;
					}
				}
			}
			app.getGraphics().requestRendering();
		}

		return isCatchKey(keyCode);
	}

	@Override
	public void setOnscreenKeyboardVisible (final boolean visible) {
		setOnscreenKeyboardVisible(visible, OnscreenKeyboardType.Default);
	}

	private boolean onscreenVisible = false;

	@Override
	public void setOnscreenKeyboardVisible (final boolean visible, final OnscreenKeyboardType type) {
		if (isNativeInputOpen()) throw new GdxRuntimeException("Can't open keyboard if already open");
		onscreenVisible = visible;
		handle.post(new Runnable() {
			public void run () {
				InputMethodManager manager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (visible) {
					View view = ((AndroidGraphics)app.getGraphics()).getView();
					OnscreenKeyboardType tmp = type == null ? OnscreenKeyboardType.Default : type;
					if (((GLSurfaceView20)view).onscreenKeyboardType != tmp) {
						((GLSurfaceView20)view).onscreenKeyboardType = tmp;
						manager.restartInput(view);
					}

					view.setFocusable(true);
					view.setFocusableInTouchMode(true);
					manager.showSoftInput(((AndroidGraphics)app.getGraphics()).getView(), 0);
				} else {
					manager.hideSoftInputFromWindow(((AndroidGraphics)app.getGraphics()).getView().getWindowToken(), 0);
				}
			}
		});
	}

	private RelativeLayout relativeLayoutField = null;
	private TextInputWrapper textInputWrapper;

	private int getSoftButtonsBarHeight () {
		AndroidApplication androidApplication = (AndroidApplication)Gdx.app;

		DisplayMetrics metrics = new DisplayMetrics();
		androidApplication.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int usableHeight = metrics.heightPixels;
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if (sdkVersion < 17) return usableHeight;

		androidApplication.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
		int realHeight = metrics.heightPixels;

		if (realHeight > usableHeight) {
			return realHeight - usableHeight;
		}

		return 0;
	}

	@Override
	public void onKeyboardHeightChanged (int height, int leftInset, int rightInset, int orientation) {
		KeyboardHeightProvider keyboardHeightProvider = ((AndroidApplication)app).getKeyboardHeightProvider();
		boolean isStandardHeightProvider = keyboardHeightProvider instanceof StandardKeyboardHeightProvider;
		if (config.useImmersiveMode && isStandardHeightProvider) {
			height += getSoftButtonsBarHeight();
		}

		if (!isNativeInputOpen()) {
			if (observer != null) observer.onKeyboardHeightChanged(height);
			return;
		}

		if (height == 0) {
			// Don't close keyboard on floating keyboards
			if (!isStandardHeightProvider && (keyboardHeightProvider.getKeyboardLandscapeHeight() != 0
				|| keyboardHeightProvider.getKeyboardPortraitHeight() != 0)) {
				closeTextInputField(false);
			}
			// What should I say at this point, everything is busted on android
			if (isStandardHeightProvider && getEditTextForNativeInput().isPopupShowing()) {
				return;
			}
			if (observer != null) observer.onKeyboardHeightChanged(0);
			relativeLayoutField.setY(0);
			return;
		}
		if (observer != null) observer.onKeyboardHeightChanged(height + getEditTextForNativeInput().getHeight());
		// This is weird, if I don't do that there is a weird scaling/position error after rotating the 2. time
		relativeLayoutField.setX(0);
		relativeLayoutField.setScaleX(1);
		relativeLayoutField.setY(0);
		// @off
		if ((((Activity)context).getWindow().getAttributes().softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST) != WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING) {
			height = 0;
		}
		relativeLayoutField.animate()
				.y(-height)
				.scaleX(((float) Gdx.graphics.getWidth() - rightInset - leftInset) / Gdx.graphics.getWidth())
				.x((float) (leftInset - rightInset) / 2)
				.setDuration(100)
				.setListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationCancel(Animator animation) {}

					@Override
					public void onAnimationRepeat(Animator animation) {}

					@Override
					public void onAnimationStart(Animator animation) {}

					@Override
					public void onAnimationEnd(Animator animation) {
						if (getEditTextForNativeInput().isPopupShowing()) {
							// In case it gets reopened
							getEditTextForNativeInput().showDropDown();
						}
					}
				});

		// @on
	}

	private void createDefaultEditText () {
		// TODO: 07.10.2024 This should probably just get the content/root view instead
		View view = ((AndroidGraphics)app.getGraphics()).getView();
		ViewGroup frameLayout = (ViewGroup)view.getParent();
		final RelativeLayout relativeLayout = new RelativeLayout(context);
		relativeLayout.setGravity(Gravity.BOTTOM);
		// Why? Why isn't it working without?
		relativeLayout.setBackgroundColor(Color.TRANSPARENT);

		final AutoCompleteTextView editText = new AutoCompleteTextView(context) {

			private int count = 0;

			@Override
			public void onFilterComplete (int count) {
				this.count = count;
				super.onFilterComplete(count);
			}

			@Override
			public void showDropDown () {
				int size = 165 * count;
				if (size > relativeLayout.getHeight() + relativeLayout.getY() - getHeight())
					size = (int)(relativeLayout.getHeight() + relativeLayout.getY() - getHeight());
				if (size > 0) setDropDownHeight(size);
				setDropDownVerticalOffset(-getDropDownHeight() - getHeight());
				setDropDownWidth((int)(getWidth() * relativeLayout.getScaleX()));
				super.showDropDown();
			}

			@Override
			public boolean onKeyPreIme (int keyCode, android.view.KeyEvent event) {
				if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK) {
					Gdx.input.closeTextInputField(false);
				}
				return super.onKeyPreIme(keyCode, event);
			}

			@Override
			public InputConnection onCreateInputConnection (EditorInfo outAttrs) {
				return new InputConnectionWrapper(super.onCreateInputConnection(outAttrs), true) {

					// Why? Is this correct handling? I mean, this can't be right! Why shouldn't it work out of the box?
					// This is needed for multiline delete
					@Override
					public boolean sendKeyEvent (android.view.KeyEvent event) {
						if (multiline && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
							if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_DEL) {
								super.deleteSurroundingText(1, 0);
								return true;
							} else if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER) {
								commitText("\n", 0);
								return true;
							}
						}

						return super.sendKeyEvent(event);
					}
				};
			}
		};

		RelativeLayout.LayoutParams editTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
			RelativeLayout.LayoutParams.WRAP_CONTENT);

		editText.setLayoutParams(editTextParams);

		relativeLayout.setVisibility(View.INVISIBLE);
		relativeLayout.addView(editText);
		relativeLayout.requestLayout();

		frameLayout.addView(relativeLayout);
		relativeLayoutField = relativeLayout;
	}

	private boolean isNativeInputOpen () {
		return relativeLayoutField != null && relativeLayoutField.getVisibility() == View.VISIBLE;
	}

	private AutoCompleteTextView getEditTextForNativeInput () {
		return (AutoCompleteTextView)relativeLayoutField.getChildAt(0);
	}

	private boolean multiline;

	@Override
	public void openTextInputField (final NativeInputConfiguration configuration) {
		configuration.validate();
		if (isNativeInputOpen()) {
			if (closeTriggered) {
				while (closeTriggered) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						return;
					}
				}
			} else {
				throw new GdxRuntimeException("Can't open keyboard if already open with openTextInputField");
			}
		}
		if (onscreenVisible) throw new GdxRuntimeException("Can't open keyboard if already open with setOnscreenKeyboardVisible");

		textInputWrapper = configuration.getTextInputWrapper();
		multiline = configuration.isMultiLine();
		handle.post(new Runnable() {
			public void run () {
				if (relativeLayoutField == null) createDefaultEditText();
				final AutoCompleteTextView editText = getEditTextForNativeInput();
				if (isNativeInputOpen()) return;

				InputMethodManager manager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);

				// Potential cleanup
				if (relativeLayoutField.getChildCount() > 1)
					relativeLayoutField.removeViews(1, relativeLayoutField.getChildCount() - 1);

				editText.setOnEditorActionListener(new OnEditorActionListener() {
					@Override
					public boolean onEditorAction (TextView textView, int actionId, android.view.KeyEvent keyEvent) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							Gdx.input.closeTextInputField(true);
							return true;
						}
						return true;
					}
				});

				// Needs to be done first, for some reason...
				if (configuration.getType() != OnscreenKeyboardType.Password) {
					editText.setTransformationMethod(null);
				}

				editText.setInputType(getAndroidInputType(configuration.getType(), false));

				if (configuration.isPreventCorrection()) {
					editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					editText.setInputType(editText.getInputType() & ~InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
				} else {
					editText.setInputType(
						editText.getInputType() | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
				}

				editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
				if (configuration.isMultiLine()) {
					editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
					editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_FLAG_NO_FULLSCREEN);
					// For cursor control support
					editText.setWidth(Gdx.graphics.getWidth());
					editText.setLines(3);
				} else {
					editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_FLAG_NO_FULLSCREEN);
					editText.setSingleLine();
				}
				// Reset filters to not run into a issue, where the max length filter messes with setText
				// But, we can't set the correct filters here, because that leads to problems for some apparent reason nobody will
				// ever understand
				editText.setFilters(new InputFilter[] {});
				editText.setText(textInputWrapper.getText());
				editText.setHint(configuration.getPlaceholder());

				InputFilter filter = new InputFilter() {
					@Override
					public CharSequence filter (CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
						boolean keepOriginal = true;
						StringBuilder sb = new StringBuilder(end - start);
						for (int i = start; i < end; i++) {
							char c = source.charAt(i);
							// TODO: 02.08.2022 There is a backend incosistenty between iOS and android. On Autocomplete
							// iOS would delete whole words, while android only deletes characters. We should make it
							// consistent. However that seems not that trivial and it first needs to be decided, which
							// behavior the correct one is.
							if (configuration.getValidator() == null || configuration.getValidator().validate(c + ""))
								sb.append(c);
							else
								keepOriginal = false;
						}
						if (keepOriginal)
							return null;
						else {
							if (source instanceof Spanned) {
								SpannableString sp = new SpannableString(sb);
								TextUtils.copySpansFrom((Spanned)source, start, sb.length(), null, sp, 0);
								return sp;
							} else {
								return sb;
							}
						}
					}
				};
				InputFilter[] filters = new InputFilter[] {filter};
				if (configuration.getMaxLength() != null) {
					filters = new InputFilter[] {filter, new LengthFilter(configuration.getMaxLength())};
				}

				editText.setFilters(filters);

				if (configuration.getAutoComplete() != null) {
					ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line,
						configuration.getAutoComplete());
					editText.setAdapter(adapter);
				} else {
					editText.setAdapter(null);
				}

				editText.setBackgroundColor(Color.WHITE);

				if (configuration.getType() == OnscreenKeyboardType.Password) {
					// For some reason this needs to be done last, otherwise it won't work
					editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
					if (configuration.isShowPasswordButton()) {
						final ImageView imageView = new ImageView(context);

						imageView.setImageResource(com.badlogic.gdx.backends.android.R.drawable.design_ic_visibility);
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
						params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
						params.rightMargin = 10;
						params.height = editText.getHeight();
						params.width = editText.getHeight();

						imageView.setLayoutParams(params);
						imageView.setOnClickListener(new View.OnClickListener() {
							private boolean isHidding = true;

							@Override
							public void onClick (View v) {
								int start = editText.getSelectionStart();
								int end = editText.getSelectionStart();
								isHidding = !isHidding;
								if (isHidding) {
									editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
									imageView.setImageResource(com.badlogic.gdx.backends.android.R.drawable.design_ic_visibility);
								} else {
									editText.setTransformationMethod(null);
									imageView.setImageResource(com.badlogic.gdx.backends.android.R.drawable.design_ic_visibility_off);
								}
								// Seems to get reset by "setTransformationMethod"
								editText.setSelection(start, end);
							}
						});
						imageView.setAlpha(0.5f);
						imageView.setPadding(5, 5, 5, 5);
						relativeLayoutField.addView(imageView);
					}
				}

				// One wonders why here? I don't know!
				editText.setSelection(textInputWrapper.getSelectionStart(), textInputWrapper.getSelectionEnd());

				relativeLayoutField.setVisibility(View.VISIBLE);

				editText.requestFocus();
				manager.showSoftInput(editText, 0);
			}
		});
	}

	// Due to the lots of threads in android, we need to use this as a lock to wait with openTextInputField until close has
	// finished
	boolean closeTriggered = false;

	@Override
	public void closeTextInputField (final boolean sendReturn) {
		if (closeTriggered) return;
		if (!isNativeInputOpen()) return;
		closeTriggered = true;
		handle.post(new Runnable() {
			@Override
			public void run () {
				if (!isNativeInputOpen()) {
					closeTriggered = false;
					return;
				}
				final View view = ((AndroidGraphics)app.getGraphics()).getView();
				view.requestFocus();
				EditText editText = getEditTextForNativeInput();
				final String text = editText.getText().toString();
				final int selection = editText.getSelectionStart();
				Gdx.app.postRunnable(new Runnable() {
					TextInputWrapper wrapper = textInputWrapper;

					@Override
					public void run () {
						wrapper.setText(text);
						wrapper.setPosition(selection);
						if (sendReturn) {
							getInputProcessor().keyDown(Keys.ENTER);
							getInputProcessor().keyTyped((char)13);
						}

						// This is getting ridiculous...
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run () {
								if (wrapper.shouldClose()) {
									handle.post(new Runnable() {
										@Override
										public void run () {
											InputMethodManager manager = (InputMethodManager)context
												.getSystemService(Context.INPUT_METHOD_SERVICE);
											manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
										}
									});
								}
							}

						});
					}
				});
				if (relativeLayoutField.getChildCount() > 1)
					relativeLayoutField.removeViews(1, relativeLayoutField.getChildCount() - 1);
				relativeLayoutField.setVisibility(View.INVISIBLE);
				closeTriggered = false;
			}
		});
	}

	private KeyboardHeightObserver observer;

	@Override
	public void setKeyboardHeightObserver (KeyboardHeightObserver observer) {
		this.observer = observer;
	}

	@Override
	public void vibrate (int milliseconds) {
		haptics.vibrate(milliseconds);
	}

	@Override
	public void vibrate (int milliseconds, boolean fallback) {
		haptics.vibrate(milliseconds);
	}

	@Override
	public void vibrate (int milliseconds, int amplitude, boolean fallback) {
		haptics.vibrate(milliseconds, amplitude, fallback);
	}

	@Override
	public void vibrate (VibrationType vibrationType) {
		haptics.vibrate(vibrationType);
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
	public boolean isButtonJustPressed (int button) {
		if (button < 0 || button > NUM_TOUCHES) return false;
		return justPressedButtons[button];
	}

	final float[] R = new float[9];
	final float[] orientation = new float[3];

	private void updateOrientation () {
		if (rotationVectorAvailable) {
			SensorManager.getRotationMatrixFromVector(R, rotationVectorValues);
		} else if (!SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues)) {
			return; // compass + accelerometer in free fall
		}
		SensorManager.getOrientation(R, orientation);
		azimuth = (float)Math.toDegrees(orientation[0]);
		pitch = (float)Math.toDegrees(orientation[1]);
		roll = (float)Math.toDegrees(orientation[2]);
	}

	/** Returns the rotation matrix describing the devices rotation as per
	 * <a href= "http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[],
	 * float[], float[])" >SensorManager#getRotationMatrix(float[], float[], float[], float[])</a>. Does not manipulate the matrix
	 * if the platform does not have an accelerometer and compass, or a rotation vector sensor.
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
				accelerometerAvailable = manager.registerListener(accelerometerListener, accelerometer, config.sensorDelay);
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
				gyroscopeAvailable = manager.registerListener(gyroscopeListener, gyroscope, config.sensorDelay);
			}
		} else
			gyroscopeAvailable = false;

		rotationVectorAvailable = false;
		if (config.useRotationVectorSensor) {
			if (manager == null) manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
			List<Sensor> rotationVectorSensors = manager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
			if (!rotationVectorSensors.isEmpty()) {
				rotationVectorListener = new SensorListener();
				for (Sensor sensor : rotationVectorSensors) { // favor AOSP sensor
					if (sensor.getVendor().equals("Google Inc.") && sensor.getVersion() == 3) {
						rotationVectorAvailable = manager.registerListener(rotationVectorListener, sensor, config.sensorDelay);
						break;
					}
				}
				if (!rotationVectorAvailable) rotationVectorAvailable = manager.registerListener(rotationVectorListener,
					rotationVectorSensors.get(0), config.sensorDelay);
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
		if (peripheral == Peripheral.Vibrator) return haptics.hasVibratorAvailable();
		if (peripheral == Peripheral.HapticFeedback) return haptics.hasHapticsSupport();
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
	}

	@Override
	public void setCatchKey (int keycode, boolean catchKey) {
		super.setCatchKey(keycode, catchKey);
		if (keycode == Keys.BACK && predictiveBackHandler != null) {
			if (catchKey)
				predictiveBackHandler.register();
			else
				predictiveBackHandler.unregister();
		}
	}

	/** Handle predictive back gestures on Android 13 and newer, replacing the <code>BACK</code> key event for exiting the
	 * activity.
	 * @see <a href="https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture">Add support for the
	 *      predictive back gesture - Android Developers</a> */
	@TargetApi(33)
	private class PredictiveBackHandler {

		private final OnBackInvokedDispatcher dispatcher = ((Activity)context).getOnBackInvokedDispatcher();
		private final OnBackInvokedCallback callback = new OnBackInvokedCallback() {
			@Override
			public void onBackInvoked () {
				if (processor != null) {
					processor.keyDown(Keys.BACK);
				}
			}
		};

		private void register () {
			dispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback);
		}

		private void unregister () {
			dispatcher.unregisterOnBackInvokedCallback(callback);
		}

	}

	/** Our implementation of SensorEventListener. Because Android doesn't like it when we register more than one Sensor to a
	 * single SensorEventListener, we add one of these for each Sensor. Could use an anonymous class, but I don't see any harm in
	 * explicitly defining it here. Correct me if I am wrong. */
	private class SensorListener implements SensorEventListener {

		public SensorListener () {

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

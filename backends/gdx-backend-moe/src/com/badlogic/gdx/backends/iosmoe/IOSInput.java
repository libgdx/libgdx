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

package com.badlogic.gdx.backends.iosmoe;

import org.moe.natj.general.ann.NInt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

import apple.audiotoolbox.c.AudioToolbox;
import apple.coregraphics.struct.CGPoint;
import apple.coregraphics.struct.CGRect;
import apple.coregraphics.struct.CGSize;
import apple.coremotion.CMAccelerometerData;
import apple.coremotion.CMMagnetometerData;
import apple.coremotion.CMMotionManager;
import apple.foundation.NSError;
import apple.foundation.NSOperationQueue;
import apple.foundation.NSSet;
import apple.foundation.struct.NSRange;
import apple.uikit.UIAlertView;
import apple.uikit.UIDevice;
import apple.uikit.UITextField;
import apple.uikit.UITouch;
import apple.uikit.enums.UIAlertViewStyle;
import apple.uikit.enums.UIDeviceOrientation;
import apple.uikit.enums.UIKeyboardType;
import apple.uikit.enums.UIReturnKeyType;
import apple.uikit.enums.UITextAutocapitalizationType;
import apple.uikit.enums.UITextAutocorrectionType;
import apple.uikit.enums.UITextSpellCheckingType;
import apple.uikit.enums.UITouchPhase;
import apple.uikit.protocol.UIAlertViewDelegate;
import apple.uikit.protocol.UITextFieldDelegate;

public class IOSInput implements Input {
	static final int MAX_TOUCHES = 20;

	IOSApplication app;
	IOSApplicationConfiguration config;
	int[] deltaX = new int[MAX_TOUCHES];
	int[] deltaY = new int[MAX_TOUCHES];
	int[] touchX = new int[MAX_TOUCHES];
	int[] touchY = new int[MAX_TOUCHES];
	// we store the pointer to the UITouch struct here, or 0
	UITouch[] touchDown = new UITouch[MAX_TOUCHES];
	int numTouched = 0;
	boolean justTouched = false;
	Pool<TouchEvent> touchEventPool = new Pool<TouchEvent>() {
		@Override
		protected TouchEvent newObject () {
			return new TouchEvent();
		}
	};
	Array<TouchEvent> touchEvents = new Array<TouchEvent>();
	TouchEvent currentEvent = null;
	float[] acceleration = new float[3];
	float[] rotation = new float[3];
	float[] R = new float[9];
	InputProcessor inputProcessor = null;

	boolean hasVibrator;
	protected CMMotionManager motionManager;
	boolean compassSupported;
	boolean keyboardCloseOnReturn;

	IOSGLKView view;

	public IOSInput (IOSApplication app) {
		this.app = app;
		this.config = app.config;
		this.keyboardCloseOnReturn = app.config.keyboardCloseOnReturn;
	}

	void setupPeripherals () {
		motionManager = CMMotionManager.alloc().init();
		setupAccelerometer();
		setupCompass();
		UIDevice device = UIDevice.currentDevice();
		if (device.model().equalsIgnoreCase("iphone")) hasVibrator = true;
	}

	private void setupCompass () {
		if (config.useCompass) {
			setupMagnetometer();
		}
	}

	protected void setupAccelerometer () {
		if (config.useAccelerometer) {
			motionManager.setAccelerometerUpdateInterval(config.accelerometerUpdate);
			CMMotionManager.Block_startAccelerometerUpdatesToQueueWithHandler handler = new CMMotionManager.Block_startAccelerometerUpdatesToQueueWithHandler() {
				@Override
				public void call_startAccelerometerUpdatesToQueueWithHandler (CMAccelerometerData cmAccelerometerData,
					NSError nsError) {
					updateAccelerometer(cmAccelerometerData);
				}
			};
			motionManager.startAccelerometerUpdatesToQueueWithHandler(NSOperationQueue.alloc().init(), handler);
		}
	}

	protected void setupMagnetometer () {
		if (motionManager.isMagnetometerAvailable() && config.useCompass)
			compassSupported = true;
		else
			return;
		motionManager.setMagnetometerUpdateInterval(config.magnetometerUpdate);

		CMMotionManager.Block_startMagnetometerUpdatesToQueueWithHandler handler = new CMMotionManager.Block_startMagnetometerUpdatesToQueueWithHandler() {
			@Override
			public void call_startMagnetometerUpdatesToQueueWithHandler (CMMagnetometerData cmMagnetometerData, NSError nsError) {
				updateRotation(cmMagnetometerData);
			}
		};

		motionManager.startMagnetometerUpdatesToQueueWithHandler(NSOperationQueue.alloc().init(), handler);
	}

	private void updateAccelerometer (CMAccelerometerData data) {
		float x = (float)data.acceleration().x() * 10f;
		float y = (float)data.acceleration().y() * 10f;
		float z = (float)data.acceleration().z() * 10f;
		acceleration[0] = -x;
		acceleration[1] = -y;
		acceleration[2] = -z;
	}

	private void updateRotation (CMMagnetometerData data) {
		final float eX = (float)data.magneticField().x();
		final float eY = (float)data.magneticField().y();
		final float eZ = (float)data.magneticField().z();

		float gX = acceleration[0];
		float gY = acceleration[1];
		float gZ = acceleration[2];

		float cX = eY * gZ - eZ * gY;
		float cY = eZ * gX - eX * gZ;
		float cZ = eX * gY - eY * gX;

		final float normal = (float)Math.sqrt(cX * cX + cY * cY + cZ * cZ);
		final float invertC = 1.0f / normal;
		cX *= invertC;
		cY *= invertC;
		cZ *= invertC;
		final float invertG = 1.0f / (float)Math.sqrt(gX * gX + gY * gY + gZ * gZ);
		gX *= invertG;
		gY *= invertG;
		gZ *= invertG;
		final float mX = gY * cZ - gZ * cY;
		final float mY = gZ * cX - gX * cZ;
		final float mZ = gX * cY - gY * cX;

		R[0] = cX;
		R[1] = cY;
		R[2] = cZ;
		R[3] = mX;
		R[4] = mY;
		R[5] = mZ;
		R[6] = gX;
		R[7] = gY;
		R[8] = gZ;

		rotation[0] = (float)Math.atan2(R[1], R[4]) * MathUtils.radDeg;
		rotation[1] = (float)Math.asin(-R[7]) * MathUtils.radDeg;
		rotation[2] = (float)Math.atan2(-R[6], R[8]) * MathUtils.radDeg;
	}

	@Override
	public float getAccelerometerX () {
		return acceleration[0];
	}

	@Override
	public float getAccelerometerY () {
		return acceleration[1];
	}

	@Override
	public float getAccelerometerZ () {
		return acceleration[2];
	}

	@Override
	public float getAzimuth () {
		if (!compassSupported) return 0;
		return rotation[0];
	}

	@Override
	public float getPitch () {
		if (!compassSupported) return 0;
		return rotation[1];
	}

	@Override
	public float getRoll () {
		if (!compassSupported) return 0;
		return rotation[2];
	}

	@Override
	public void getRotationMatrix (float[] matrix) {
		if (matrix.length != 9) return;
		// TODO implement when azimuth is fixed
	}

	@Override
	public int getX () {
		return touchX[0];
	}

	@Override
	public int getX (int pointer) {
		return touchX[pointer];
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
	public int getY () {
		return touchY[0];
	}

	@Override
	public int getY (int pointer) {
		return touchY[pointer];
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
	public boolean isTouched () {
		for (int pointer = 0; pointer < MAX_TOUCHES; pointer++) {
			if (touchDown[pointer] != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean justTouched () {
		return justTouched;
	}

	@Override
	public boolean isTouched (int pointer) {
		return touchDown[pointer] != null;
	}

	@Override
	public boolean isButtonPressed (int button) {
		return button == Buttons.LEFT && numTouched > 0;
	}

	@Override
	public boolean isKeyPressed (int key) {
		return false;
	}

	@Override
	public boolean isKeyJustPressed (int key) {
		return false;
	}

	@Override
	public void getTextInput (TextInputListener listener, String title, String text, String hint) {
		buildUIAlertView(listener, title, text, hint).show();
	}

	private UITextField textfield = null;
	private final UITextFieldDelegate textDelegate = new UITextFieldDelegate() {
		@Override
		public boolean textFieldShouldChangeCharactersInRangeReplacementString (UITextField textField, NSRange range,
			String string) {
			for (int i = 0; i < range.length(); i++) {
				app.input.inputProcessor.keyTyped((char)8);
			}

			if (string.isEmpty()) {
				if (range.length() > 0) Gdx.graphics.requestRendering();
				return false;
			}

			char[] chars = new char[string.length()];
			string.getChars(0, string.length(), chars, 0);

			for (int i = 0; i < chars.length; i++) {
				app.input.inputProcessor.keyTyped(chars[i]);
			}
			Gdx.graphics.requestRendering();

			return true;
		}

		@Override
		public boolean textFieldShouldEndEditing (UITextField textField) {
			// Text field needs to have at least one symbol - so we can use backspace
			textField.setText("x");
			Gdx.graphics.requestRendering();

			return true;
		}

		@Override
		public boolean textFieldShouldReturn (UITextField textField) {
			if (keyboardCloseOnReturn) setOnscreenKeyboardVisible(false);
			app.input.inputProcessor.keyDown(Keys.ENTER);
			app.input.inputProcessor.keyTyped((char)13);
			Gdx.graphics.requestRendering();
			return false;
		}
	};

	@Override
	public void setOnscreenKeyboardVisible (boolean visible) {
		if (textfield == null) createDefaultTextField();
		if (visible) {
			textfield.becomeFirstResponder();
			textfield.setDelegate(textDelegate);
		} else {
			textfield.resignFirstResponder();
		}
	}

	/** Set the keyboard to close when the UITextField return key is pressed
	 * @param shouldClose Whether or not the keyboard should clsoe on return key press */
	public void setKeyboardCloseOnReturnKey (boolean shouldClose) {
		keyboardCloseOnReturn = shouldClose;
	}

	public UITextField getKeyboardTextField () {
		if (textfield == null) createDefaultTextField();
		return textfield;
	}

	private void createDefaultTextField () {
		textfield = UITextField.alloc();
		textfield.initWithFrame(new CGRect(new CGPoint(10, 10), new CGSize(100, 50)));
		// Parameters
		// Setting parameters
		textfield.setKeyboardType(UIKeyboardType.Default);
		textfield.setReturnKeyType(UIReturnKeyType.Done);
		textfield.setAutocapitalizationType(UITextAutocapitalizationType.None);
		textfield.setAutocorrectionType(UITextAutocorrectionType.No);
		textfield.setSpellCheckingType(UITextSpellCheckingType.No);
		textfield.setHidden(true);
		// Text field needs to have at least one symbol - so we can use backspace
		textfield.setText("x");
		app.getUIViewController().view().addSubview(textfield);
	}

	// Issue 773 indicates this may solve a premature GC issue

	/** Builds an {@link UIAlertView} with an added {@link UITextField} for inputting text.
	 * @param listener Text input listener
	 * @param title Dialog title
	 * @param text Text for text field
	 * @return UiAlertView */
	private UIAlertView buildUIAlertView (final TextInputListener listener, String title, String text, String placeholder) {
		UIAlertViewDelegate delegate = new UIAlertViewDelegate() {
			@Override
			public void alertViewClickedButtonAtIndex (UIAlertView alertView, @NInt long buttonIndex) {
				if (buttonIndex == 0) {
					// user clicked "Cancel" button
					listener.canceled();
				} else if (buttonIndex == 1) {
					// user clicked "Ok" button
					UITextField textField = alertView.textFieldAtIndex(0);
					listener.input(textField.text());
				}
			}

			@Override
			public void alertViewCancel (UIAlertView alertView) {
				listener.canceled();
			}
		};

		// build the view
		final UIAlertView uiAlertView = UIAlertView.alloc().init();
		uiAlertView.setTitle(title);
		uiAlertView.addButtonWithTitle("Cancel");
		uiAlertView.addButtonWithTitle("Ok");
		uiAlertView.setAlertViewStyle(UIAlertViewStyle.PlainTextInput);
		uiAlertView.setDelegate(delegate);

		UITextField textField = uiAlertView.textFieldAtIndex(0);
		textField.setPlaceholder(placeholder);
		textField.setText(text);

		return uiAlertView;
	}

	@Override
	public void vibrate (int milliseconds) {
		AudioToolbox.AudioServicesPlaySystemSound(4095);
	}

	@Override
	public void vibrate (long[] pattern, int repeat) {
		// FIXME implement this
	}

	@Override
	public void cancelVibrate () {
		// FIXME implement this
	}

	@Override
	public long getCurrentEventTime () {
		return currentEvent.timestamp;
	}

	@Override
	public void setCatchBackKey (boolean catchBack) {
	}

	@Override
	public boolean isCatchBackKey () {
		return false;
	}

	@Override
	public void setCatchMenuKey (boolean catchMenu) {
	}

	@Override
	public boolean isCatchMenuKey () {
		return false;
	}

	@Override
	public void setInputProcessor (InputProcessor processor) {
		this.inputProcessor = processor;
	}

	@Override
	public InputProcessor getInputProcessor () {
		return inputProcessor;
	}

	@Override
	public boolean isPeripheralAvailable (Peripheral peripheral) {
		if (peripheral == Peripheral.Accelerometer && config.useAccelerometer) return true;
		if (peripheral == Peripheral.MultitouchScreen) return true;
		if (peripheral == Peripheral.Vibrator) return hasVibrator;
		if (peripheral == Peripheral.Compass) return compassSupported;
		// if(peripheral == Peripheral.OnscreenKeyboard) return true;
		return false;
	}

	@Override
	public int getRotation () {
		// we measure orientation counter clockwise, just like on Android
		if (app.uiApp.statusBarOrientation() == UIDeviceOrientation.LandscapeLeft) return 270;
		if (app.uiApp.statusBarOrientation() == UIDeviceOrientation.PortraitUpsideDown) return 180;
		if (app.uiApp.statusBarOrientation() == UIDeviceOrientation.LandscapeRight) return 90;
		return 0;
	}

	@Override
	public Orientation getNativeOrientation () {
		if (app.uiApp.statusBarOrientation() == UIDeviceOrientation.LandscapeLeft
			|| app.uiApp.statusBarOrientation() == UIDeviceOrientation.LandscapeRight) {
			return Orientation.Landscape;
		} else {
			return Orientation.Portrait;
		}
	}

	@Override
	public void setCursorCatched (boolean catched) {
	}

	@Override
	public boolean isCursorCatched () {
		return false;
	}

	@Override
	public void setCursorPosition (int x, int y) {
	}

	protected void onTouch (NSSet<? extends UITouch> touches) {
		toTouchEvents(touches);
		Gdx.graphics.requestRendering();
	}

	void processEvents () {
		synchronized (touchEvents) {
			justTouched = false;
			for (TouchEvent event : touchEvents) {
				currentEvent = event;
				if (event.phase == UITouchPhase.Began) {
					if (inputProcessor != null) inputProcessor.touchDown(event.x, event.y, event.pointer, Buttons.LEFT);
					if (numTouched == 1) justTouched = true;
				} else if (event.phase == UITouchPhase.Cancelled || event.phase == UITouchPhase.Ended) {
					if (inputProcessor != null) inputProcessor.touchUp(event.x, event.y, event.pointer, Buttons.LEFT);
				} else if (event.phase == UITouchPhase.Moved || event.phase == UITouchPhase.Stationary) {
					if (inputProcessor != null) inputProcessor.touchDragged(event.x, event.y, event.pointer);
				}
			}
			touchEventPool.freeAll(touchEvents);
			touchEvents.clear();
		}
	}

	private int getFreePointer () {
		for (int i = 0; i < touchDown.length; i++) {
			if (touchDown[i] == null) return i;
		}
		throw new GdxRuntimeException("Couldn't find free pointer id!");
	}

	private int findPointer (UITouch touch) {
		long ptr = 0;
		for (int i = 0; i < touchDown.length; i++) {
			if (touchDown[i] == touch) return i;
		}
		throw new GdxRuntimeException("Couldn't find pointer id for touch event!");
	}

	private void toTouchEvents (NSSet<? extends UITouch> touches) {
		for (UITouch touch : touches.allObjects()) {
			final int locX, locY;
			// Get and map the location to our drawing space
			{
				CGPoint loc = view == null ? loc = touch.locationInView(touch.window()) : touch.locationInView(view);
				final CGRect bounds = app.getCachedBounds();
				locX = (int)(loc.x() * app.displayScaleFactor - bounds.origin().x());
				locY = (int)(loc.y() * app.displayScaleFactor - bounds.origin().y());
				// app.debug("IOSInput","pos= "+loc+" bounds= "+bounds+" x= "+locX+" locY= "+locY);
			}

			synchronized (touchEvents) {
				long phase = touch.phase();
				TouchEvent event = touchEventPool.obtain();
				event.x = locX;
				event.y = locY;
				event.phase = phase;
				event.timestamp = (long)(touch.timestamp() * 1000000000);
				touchEvents.add(event);

				if (phase == UITouchPhase.Began) {
					event.pointer = getFreePointer();
					touchDown[event.pointer] = touch;
					touchX[event.pointer] = event.x;
					touchY[event.pointer] = event.y;
					deltaX[event.pointer] = 0;
					deltaY[event.pointer] = 0;
					numTouched++;
				}

				if (phase == UITouchPhase.Moved || phase == UITouchPhase.Stationary) {
					event.pointer = findPointer(touch);
					deltaX[event.pointer] = event.x - touchX[event.pointer];
					deltaY[event.pointer] = event.y - touchY[event.pointer];
					touchX[event.pointer] = event.x;
					touchY[event.pointer] = event.y;
				}

				if (phase == UITouchPhase.Cancelled || phase == UITouchPhase.Ended) {
					event.pointer = findPointer(touch);
					touchDown[event.pointer] = null;
					touchX[event.pointer] = event.x;
					touchY[event.pointer] = event.y;
					deltaX[event.pointer] = 0;
					deltaY[event.pointer] = 0;
					numTouched--;
				}
			}
		}
	}

	static class TouchEvent {
		long phase;
		long timestamp;
		int x, y;
		int pointer;
	}

	@Override
	public float getGyroscopeX () {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getGyroscopeY () {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getGyroscopeZ () {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setView (IOSGLKView view) {
		this.view = view;
	}

}

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

import apple.coregraphics.struct.CGSize;
import apple.coremotion.CMAccelerometerData;
import apple.coremotion.CMMagnetometerData;
import apple.coremotion.CMMotionManager;
import apple.foundation.NSError;
import apple.foundation.NSOperationQueue;
import apple.foundation.NSSet;
import apple.foundation.struct.NSRange;
import apple.uikit.UIAlertAction.Block_actionWithTitleStyleHandler;
import apple.uikit.UIAlertController.Block_addTextFieldWithConfigurationHandler;
import com.badlogic.gdx.AbstractInput;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import apple.coregraphics.struct.CGPoint;
import apple.coregraphics.struct.CGRect;
import apple.gamecontroller.GCKeyboard;
import apple.uikit.UIAlertAction;
import apple.uikit.enums.UIAlertActionStyle;
import apple.uikit.UIAlertController;
import apple.uikit.enums.UIAlertControllerStyle;
import apple.uikit.enums.UIForceTouchCapability;
import apple.uikit.UIKey;
import apple.uikit.enums.UIKeyboardType;
import apple.uikit.enums.UIReturnKeyType;
import apple.uikit.UIScreen;
import apple.uikit.enums.UITextAutocapitalizationType;
import apple.uikit.enums.UITextAutocorrectionType;
import apple.uikit.UITextField;
import apple.uikit.protocol.UITextFieldDelegate;
import apple.uikit.enums.UITextSpellCheckingType;
import apple.uikit.UITouch;
import apple.uikit.enums.UITouchPhase;

import static apple.uikit.enums.UITouchPhase.*;
import static apple.uikit.enums.UIDeviceOrientation.*;
import static apple.uikit.enums.UIKeyboardHIDUsage.*;

public class DefaultIOSInput extends AbstractInput implements IOSInput {
	static final int MAX_TOUCHES = 20;
	private static final int POINTER_NOT_FOUND = -1;

	IOSApplication app;
	IOSApplicationConfiguration config;
	int[] deltaX = new int[MAX_TOUCHES];
	int[] deltaY = new int[MAX_TOUCHES];
	int[] touchX = new int[MAX_TOUCHES];
	int[] touchY = new int[MAX_TOUCHES];
	float[] pressures = new float[MAX_TOUCHES];
	boolean pressureSupported;
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
	private final Pool<KeyEvent> keyEventPool = new Pool<KeyEvent>(16, 1000) {
		protected KeyEvent newObject () {
			return new KeyEvent();
		}
	};
	private final Array<KeyEvent> keyEvents = new Array();
	private long currentEventTimeStamp = 0;
	float[] acceleration = new float[3];
	float[] rotation = new float[3];
	float[] R = new float[9];
	InputProcessor inputProcessor = null;

	private IOSHaptics haptics;
	// CMMotionManager motionManager;
	protected CMMotionManager motionManager;
	boolean compassSupported;
	boolean keyboardCloseOnReturn;
	boolean softkeyboardActive = false;

	private boolean hadHardwareKeyEvent = false;

	public DefaultIOSInput (IOSApplication app) {
		this.app = app;
		this.config = app.config;
		this.keyboardCloseOnReturn = app.config.keyboardCloseOnReturn;
	}

	@Override
	public void setupPeripherals () {
		motionManager = CMMotionManager.alloc().init();
		setupAccelerometer();
		setupCompass();
		setupHaptics();
		setupPressure();
	}

	protected void setupCompass () {
		if (config.useCompass) {
			setupMagnetometer();
		}
	}

	protected void setupAccelerometer () {
		if (config.useAccelerometer) {
			motionManager.setAccelerometerUpdateInterval(config.accelerometerUpdate);
			CMMotionManager.Block_startAccelerometerUpdatesToQueueWithHandler handler = new CMMotionManager.Block_startAccelerometerUpdatesToQueueWithHandler() {
				@Override
				public void call_startAccelerometerUpdatesToQueueWithHandler (CMAccelerometerData data, NSError nsError) {
					float x = (float)data.acceleration().x() * 10f;
					float y = (float)data.acceleration().y() * 10f;
					float z = (float)data.acceleration().z() * 10f;
					acceleration[0] = -x;
					acceleration[1] = -y;
					acceleration[2] = -z;
				}
			};
			motionManager.startAccelerometerUpdatesToQueueWithHandler(NSOperationQueue.alloc().init(), handler);
		}
	}

	protected void setupHaptics () {
		haptics = new IOSHaptics(config.useHaptics);
	}

	protected void setupPressure () {
		if (app.getVersion() >= 9) {
			long forceTouchCapability = UIScreen.mainScreen().traitCollection().forceTouchCapability();
			pressureSupported = forceTouchCapability == UIForceTouchCapability.Available;
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
	public int getMaxPointers () {
		return MAX_TOUCHES;
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
	public float getPressure () {
		return pressures[0];
	}

	@Override
	public float getPressure (int pointer) {
		return pressures[pointer];
	}

	@Override
	public boolean isButtonPressed (int button) {
		return button == Buttons.LEFT && numTouched > 0;
	}

	@Override
	public boolean isButtonJustPressed (int button) {
		return button == Buttons.LEFT && justTouched;
	}

	@Override
	public void getTextInput (TextInputListener listener, String title, String text, String hint) {
		getTextInput(listener, title, text, hint, OnscreenKeyboardType.Default);
	}

	@Override
	public void getTextInput (TextInputListener listener, String title, String text, String hint, OnscreenKeyboardType type) {
		UIAlertController uiAlertController = buildUIAlertController(listener, title, text, hint, type);
		app.getUIViewController().presentViewControllerAnimatedCompletion(uiAlertController, true, null);
	}

	// hack for software keyboard support
	// uses a hidden textfield to capture input
	// see: https://web.archive.org/web/20171016192705/http://www.badlogicgames.com/forum/viewtopic.php?f=17&t=11788

	private UITextField textfield = null;
	private final UITextFieldDelegate textDelegate = new UITextFieldDelegate() {

		@Override
		public boolean textFieldShouldChangeCharactersInRangeReplacementString (UITextField textField, NSRange range,
			String string) {
			for (int i = 0; i < range.length(); i++) {
				inputProcessor.keyTyped((char)8);
			}

			if (string.isEmpty()) {
				if (range.length() > 0) Gdx.graphics.requestRendering();
				return false;
			}

			char[] chars = new char[string.length()];
			string.getChars(0, string.length(), chars, 0);

			for (int i = 0; i < chars.length; i++) {
				inputProcessor.keyTyped(chars[i]);
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
			inputProcessor.keyDown(Keys.ENTER);
			inputProcessor.keyTyped((char)13);
			Gdx.graphics.requestRendering();
			return false;
		}
	};

	@Override
	public void setOnscreenKeyboardVisible (boolean visible) {
		setOnscreenKeyboardVisible(visible, OnscreenKeyboardType.Default);
	}

	@Override
	public void setOnscreenKeyboardVisible (boolean visible, OnscreenKeyboardType type) {
		if (textfield == null) createDefaultTextField();
		softkeyboardActive = visible;
		if (visible) {
			if (type == null) type = OnscreenKeyboardType.Default;
			textfield.setKeyboardType(getIosInputType(type));
			textfield.reloadInputViews();
			textfield.becomeFirstResponder();
			textfield.setDelegate(textDelegate);
		} else {
			textfield.resignFirstResponder();
		}
	}

	protected long getIosInputType (OnscreenKeyboardType type) {
		long preferredInputType;
		switch (type) {
		case NumberPad:
			preferredInputType = UIKeyboardType.NumberPad;
			break;
		case PhonePad:
			preferredInputType = UIKeyboardType.PhonePad;
			break;
		case Email:
			preferredInputType = UIKeyboardType.EmailAddress;
			break;
		case URI:
			preferredInputType = UIKeyboardType.URL;
			break;
		case Password: // no equivalent in UIKeyboardType?
		default:
			preferredInputType = UIKeyboardType.Default;
			break;
		}
		return preferredInputType;
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
		CGRect rect = new CGRect();
		rect.setOrigin(new CGPoint(10, 10));
		rect.setSize(new CGSize(100, 50));
		textfield = UITextField.alloc().initWithFrame(rect);
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

	/** Builds an {@link UIAlertController} with an added {@link UITextField} for inputting text.
	 * @param listener Text input listener
	 * @param title Dialog title
	 * @param text Text for text field
	 * @param type
	 * @return UIAlertController */
	private UIAlertController buildUIAlertController (final TextInputListener listener, String title, final String text,
		final String placeholder, final OnscreenKeyboardType type) {
		final UIAlertController uiAlertController = UIAlertController.alertControllerWithTitleMessagePreferredStyle(title, text,
			UIAlertControllerStyle.Alert);
		uiAlertController.addTextFieldWithConfigurationHandler(new Block_addTextFieldWithConfigurationHandler() {
			@Override
			public void call_addTextFieldWithConfigurationHandler (UITextField uiTextField) {
				uiTextField.setPlaceholder(placeholder);
				uiTextField.setText(text);
				uiTextField.setKeyboardType(getIosInputType(type));
				if (type == OnscreenKeyboardType.Password) {
					uiTextField.setSecureTextEntry(true);
				}
				uiTextField.reloadInputViews();
			}
		});
		uiAlertController.addAction(
			UIAlertAction.actionWithTitleStyleHandler("Ok", UIAlertActionStyle.Default, new Block_actionWithTitleStyleHandler() {

				@Override
				public void call_actionWithTitleStyleHandler (UIAlertAction uiAlertAction) {
					// user clicked "Ok" button
					UITextField textField = uiAlertController.textFields().get(0);
					listener.input(textField.text());
				}
			}));
		uiAlertController.addAction(
			UIAlertAction.actionWithTitleStyleHandler("Cancel", UIAlertActionStyle.Cancel, new Block_actionWithTitleStyleHandler() {
				@Override
				public void call_actionWithTitleStyleHandler (UIAlertAction uiAlertAction) {
					// user clicked "Cancel" button
					listener.canceled();
				}
			}));
		return uiAlertController;
	}

	@Override
	public void vibrate (int milliseconds) {
		haptics.vibrate(milliseconds, true);
	}

	@Override
	public void vibrate (int milliseconds, boolean fallback) {
		haptics.vibrate(milliseconds, fallback);
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
	public long getCurrentEventTime () {
		return currentEventTimeStamp;
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
		if (peripheral == Peripheral.Vibrator) return haptics.isVibratorSupported();
		if (peripheral == Peripheral.HapticFeedback) return haptics.isHapticsSupported();
		if (peripheral == Peripheral.Compass) return compassSupported;
		if (peripheral == Peripheral.OnscreenKeyboard) return true;
		if (peripheral == Peripheral.Pressure) return pressureSupported;
		if (peripheral == Peripheral.HardwareKeyboard)
			return Foundation.getMajorSystemVersion() >= 14 ? GCKeyboard.coalescedKeyboard() != null : hadHardwareKeyEvent;
		return false;
	}

	@Override
	public int getRotation () {
		// we measure orientation counter clockwise, just like on Android
		switch ((int)app.uiApp.statusBarOrientation()) {
		case (int)LandscapeLeft:
			return 270;
		case (int)PortraitUpsideDown:
			return 180;
		case (int)LandscapeRight:
			return 90;
		case (int)Portrait:
		default:
			return 0;
		}
	}

	@Override
	public Orientation getNativeOrientation () {
		return Orientation.Portrait;
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

	@Override
	public void onTouch (NSSet<? extends UITouch> touches) {
		toTouchEvents(touches);
		Gdx.graphics.requestRendering();
	}

	@Override
	public boolean onKey (UIKey key, boolean down) {
		if (key == null) {
			return false;
		}

		int keyCode = getGdxKeyCode(key);

		if (keyCode != Keys.UNKNOWN) synchronized (keyEvents) {
			hadHardwareKeyEvent = true;

			KeyEvent event = keyEventPool.obtain();
			long timeStamp = System.nanoTime();
			event.timeStamp = timeStamp;
			event.keyChar = 0;
			event.keyCode = keyCode;
			event.type = down ? KeyEvent.KEY_DOWN : KeyEvent.KEY_UP;
			keyEvents.add(event);

			if (!down) {
				char character;

				switch (keyCode) {
				case (int)Keys.DEL:
					character = 8;
					break;
				case (int)Keys.FORWARD_DEL:
					character = 127;
					break;
				case (int)Keys.ENTER:
					character = 13;
					break;
				default:
					String characters = key.characters();
					// special keys return constants like "UIKeyInputF5", so we check for length 1
					character = (characters != null && characters.length() == 1) ? characters.charAt(0) : 0;
				}

				if (character >= 0) {
					event = keyEventPool.obtain();
					event.timeStamp = timeStamp;
					event.type = KeyEvent.KEY_TYPED;
					event.keyCode = keyCode;
					event.keyChar = character;
					keyEvents.add(event);
				}

				if (pressedKeys[keyCode]) {
					pressedKeyCount--;
					pressedKeys[keyCode] = false;
				}
			} else {
				if (!pressedKeys[event.keyCode]) {
					pressedKeyCount++;
					pressedKeys[event.keyCode] = true;
				}
			}

		}

		return isCatchKey(keyCode);
	}

	@Override
	public void processEvents () {
		synchronized (touchEvents) {
			justTouched = false;
			for (TouchEvent event : touchEvents) {
				currentEventTimeStamp = event.timestamp;
				switch ((int)event.phase) {
				case (int)Began:
					if (inputProcessor != null) inputProcessor.touchDown(event.x, event.y, event.pointer, Buttons.LEFT);
					if (numTouched >= 1) justTouched = true;
					break;
				case (int)Cancelled:
				case (int)Ended:
					if (inputProcessor != null) inputProcessor.touchUp(event.x, event.y, event.pointer, Buttons.LEFT);
					break;
				case (int)Moved:
				case (int)Stationary:
					if (inputProcessor != null) inputProcessor.touchDragged(event.x, event.y, event.pointer);
					break;
				}
			}
			touchEventPool.freeAll(touchEvents);
			touchEvents.clear();
		}

		synchronized (keyEvents) {
			if (keyJustPressed) {
				keyJustPressed = false;
				for (int i = 0; i < justPressedKeys.length; i++) {
					justPressedKeys[i] = false;
				}
			}

			for (KeyEvent e : keyEvents) {
				currentEventTimeStamp = e.timeStamp;
				switch (e.type) {
				case (int)KeyEvent.KEY_DOWN:
					if (inputProcessor != null) inputProcessor.keyDown(e.keyCode);
					keyJustPressed = true;
					justPressedKeys[e.keyCode] = true;
					break;
				case (int)KeyEvent.KEY_UP:
					if (inputProcessor != null) inputProcessor.keyUp(e.keyCode);
					break;
				case (int)KeyEvent.KEY_TYPED:
					// don't process key typed events if soft keyboard is active
					// the soft keyboard hook already catches the changes
					if (!softkeyboardActive && inputProcessor != null) inputProcessor.keyTyped(e.keyChar);
				}

			}
			keyEventPool.freeAll(keyEvents);
			keyEvents.clear();
		}
	}

	private int getFreePointer () {
		for (int i = 0; i < touchDown.length; i++) {
			if (touchDown[i] == null) return i;
		}
		throw new GdxRuntimeException("Couldn't find free pointer id!");
	}

	private int findPointer (UITouch touch) {
		for (int i = 0; i < touchDown.length; i++) {
			if (touchDown[i] == touch) return i;
		}
		// If pointer is not found
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < touchDown.length; i++) {
			sb.append(i + ":" + touchDown[i] + " ");
		}
		Gdx.app.error("IOSInput", "Pointer ID lookup failed: " + touch + ", " + sb.toString());
		return POINTER_NOT_FOUND;
	}

	private void toTouchEvents (NSSet<? extends UITouch> touches) {
		final IOSScreenBounds screenBounds = app.getScreenBounds();
		for (UITouch touch : touches.allObjects()) {
			int locX, locY;
			// Get and map the location to our drawing space
			{
				CGPoint loc = touch.locationInView(app.graphics.view);
				locX = (int)(loc.x() - screenBounds.x);
				locY = (int)(loc.y() - screenBounds.y);
				if (config.hdpiMode == HdpiMode.Pixels) {
					locX *= app.pixelsPerPoint;
					locY *= app.pixelsPerPoint;
				}
				// app.debug("IOSInput","pos= "+loc+" bounds= "+bounds+" x= "+locX+" locY= "+locY);
			}

			// if its not supported, we will simply use 1.0f when touch is present
			float pressure = 1.0f;
			if (pressureSupported) {
				pressure = (float)touch.force();
			}

			synchronized (touchEvents) {
				long phase = touch.phase();
				TouchEvent event = touchEventPool.obtain();
				event.x = locX;
				event.y = locY;
				event.phase = phase;
				event.timestamp = (long)(touch.timestamp() * 1000000000);

				if (phase == UITouchPhase.Began) {
					event.pointer = getFreePointer();
					touchDown[event.pointer] = touch;
					touchX[event.pointer] = event.x;
					touchY[event.pointer] = event.y;
					deltaX[event.pointer] = 0;
					deltaY[event.pointer] = 0;
					pressures[event.pointer] = pressure;
					numTouched++;
				}

				else if (phase == UITouchPhase.Moved || phase == UITouchPhase.Stationary) {
					event.pointer = findPointer(touch);
					if (event.pointer != POINTER_NOT_FOUND) {
						deltaX[event.pointer] = event.x - touchX[event.pointer];
						deltaY[event.pointer] = event.y - touchY[event.pointer];
						touchX[event.pointer] = event.x;
						touchY[event.pointer] = event.y;
						pressures[event.pointer] = pressure;
					}
				}

				else if (phase == UITouchPhase.Cancelled || phase == UITouchPhase.Ended) {
					event.pointer = findPointer(touch);
					if (event.pointer != POINTER_NOT_FOUND) {
						touchDown[event.pointer] = null;
						touchX[event.pointer] = event.x;
						touchY[event.pointer] = event.y;
						deltaX[event.pointer] = 0;
						deltaY[event.pointer] = 0;
						pressures[event.pointer] = 0;
						numTouched--;
					}
				}

				if (event.pointer != POINTER_NOT_FOUND) {
					touchEvents.add(event);
				} else {
					touchEventPool.free(event);
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

	static class KeyEvent {
		static final int KEY_DOWN = 0;
		static final int KEY_UP = 1;
		static final int KEY_TYPED = 2;

		long timeStamp;
		int type;
		int keyCode;
		char keyChar;
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

	protected int getGdxKeyCode (UIKey key) {
		long keyCode;
		try {
			keyCode = key.keyCode();
		} catch (IllegalArgumentException e) {
			return Keys.UNKNOWN;
		}

		switch ((int)keyCode) {
		case (int)KeyboardA:
			return Keys.A;
		case (int)KeyboardB:
			return Keys.B;
		case (int)KeyboardC:
			return Keys.C;
		case (int)KeyboardD:
			return Keys.D;
		case (int)KeyboardE:
			return Keys.E;
		case (int)KeyboardF:
			return Keys.F;
		case (int)KeyboardG:
			return Keys.G;
		case (int)KeyboardH:
			return Keys.H;
		case (int)KeyboardI:
			return Keys.I;
		case (int)KeyboardJ:
			return Keys.J;
		case (int)KeyboardK:
			return Keys.K;
		case (int)KeyboardL:
			return Keys.L;
		case (int)KeyboardM:
			return Keys.M;
		case (int)KeyboardN:
			return Keys.N;
		case (int)KeyboardO:
			return Keys.O;
		case (int)KeyboardP:
			return Keys.P;
		case (int)KeyboardQ:
			return Keys.Q;
		case (int)KeyboardR:
			return Keys.R;
		case (int)KeyboardS:
			return Keys.S;
		case (int)KeyboardT:
			return Keys.T;
		case (int)KeyboardU:
			return Keys.U;
		case (int)KeyboardV:
			return Keys.V;
		case (int)KeyboardW:
			return Keys.W;
		case (int)KeyboardX:
			return Keys.X;
		case (int)KeyboardY:
			return Keys.Y;
		case (int)KeyboardZ:
			return Keys.Z;
		case (int)Keyboard1:
			return Keys.NUM_1;
		case (int)Keyboard2:
			return Keys.NUM_2;
		case (int)Keyboard3:
			return Keys.NUM_3;
		case (int)Keyboard4:
			return Keys.NUM_4;
		case (int)Keyboard5:
			return Keys.NUM_5;
		case (int)Keyboard6:
			return Keys.NUM_6;
		case (int)Keyboard7:
			return Keys.NUM_7;
		case (int)Keyboard8:
			return Keys.NUM_8;
		case (int)Keyboard9:
			return Keys.NUM_9;
		case (int)Keyboard0:
			return Keys.NUM_0;
		case (int)KeyboardReturnOrEnter:
			return Keys.ENTER;
		case (int)KeyboardEscape:
			return Keys.ESCAPE;
		case (int)KeyboardDeleteOrBackspace:
			return Keys.BACKSPACE;
		case (int)KeyboardTab:
			return Keys.TAB;
		case (int)KeyboardSpacebar:
			return Keys.SPACE;
		case (int)KeyboardHyphen:
			return Keys.MINUS;
		case (int)KeyboardEqualSign:
			return Keys.EQUALS;
		case (int)KeyboardOpenBracket:
			return Keys.LEFT_BRACKET;
		case (int)KeyboardCloseBracket:
			return Keys.RIGHT_BRACKET;
		case (int)KeyboardBackslash:
			return Keys.BACKSLASH;
		case (int)KeyboardNonUSPound:
			return Keys.POUND;
		case (int)KeyboardSemicolon:
			return Keys.SEMICOLON;
		case (int)KeyboardQuote:
			return Keys.APOSTROPHE;
		case (int)KeyboardGraveAccentAndTilde:
			return Keys.GRAVE;
		case (int)KeyboardComma:
			return Keys.COMMA;
		case (int)KeyboardPeriod:
			return Keys.PERIOD;
		case (int)KeyboardSlash:
			return Keys.SLASH;
		case (int)KeyboardF1:
			return Keys.F1;
		case (int)KeyboardF2:
			return Keys.F2;
		case (int)KeyboardF3:
			return Keys.F3;
		case (int)KeyboardF4:
			return Keys.F4;
		case (int)KeyboardF5:
			return Keys.F5;
		case (int)KeyboardF6:
			return Keys.F6;
		case (int)KeyboardF7:
			return Keys.F7;
		case (int)KeyboardF8:
			return Keys.F8;
		case (int)KeyboardF9:
			return Keys.F9;
		case (int)KeyboardF10:
			return Keys.F10;
		case (int)KeyboardF11:
			return Keys.F11;
		case (int)KeyboardF12:
			return Keys.F12;
		case (int)KeyboardF13:
			return Keys.F13;
		case (int)KeyboardF14:
			return Keys.F14;
		case (int)KeyboardF15:
			return Keys.F15;
		case (int)KeyboardF16:
			return Keys.F16;
		case (int)KeyboardF17:
			return Keys.F17;
		case (int)KeyboardF18:
			return Keys.F18;
		case (int)KeyboardF19:
			return Keys.F19;
		case (int)KeyboardF20:
			return Keys.F20;
		case (int)KeyboardF21:
			return Keys.F21;
		case (int)KeyboardF22:
			return Keys.F22;
		case (int)KeyboardF23:
			return Keys.F23;
		case (int)KeyboardF24:
			return Keys.F24;
		case (int)KeyboardPause:
			return Keys.PAUSE;
		case (int)KeyboardInsert:
			return Keys.INSERT;
		case (int)KeyboardHome:
			return Keys.HOME;
		case (int)KeyboardPageUp:
			return Keys.PAGE_UP;
		case (int)KeyboardDeleteForward:
			return Keys.FORWARD_DEL;
		case (int)KeyboardEnd:
			return Keys.END;
		case (int)KeyboardPageDown:
			return Keys.PAGE_DOWN;
		case (int)KeyboardRightArrow:
			return Keys.RIGHT;
		case (int)KeyboardLeftArrow:
			return Keys.LEFT;
		case (int)KeyboardDownArrow:
			return Keys.DOWN;
		case (int)KeyboardUpArrow:
			return Keys.UP;
		case (int)KeypadNumLock:
			return Keys.NUM_LOCK;
		case (int)KeypadSlash:
			return Keys.NUMPAD_DIVIDE;
		case (int)KeypadAsterisk:
			return Keys.NUMPAD_MULTIPLY;
		case (int)KeypadHyphen:
			return Keys.NUMPAD_SUBTRACT;
		case (int)KeypadPlus:
			return Keys.NUMPAD_ADD;
		case (int)KeypadEnter:
			return Keys.NUMPAD_ENTER;
		case (int)Keypad1:
			return Keys.NUM_1;
		case (int)Keypad2:
			return Keys.NUM_2;
		case (int)Keypad3:
			return Keys.NUM_3;
		case (int)Keypad4:
			return Keys.NUM_4;
		case (int)Keypad5:
			return Keys.NUM_5;
		case (int)Keypad6:
			return Keys.NUM_6;
		case (int)Keypad7:
			return Keys.NUM_7;
		case (int)Keypad8:
			return Keys.NUM_8;
		case (int)Keypad9:
			return Keys.NUM_9;
		case (int)Keypad0:
			return Keys.NUM_0;
		case (int)KeypadPeriod:
			return Keys.NUMPAD_DOT;
		case (int)KeyboardNonUSBackslash:
			return Keys.BACKSLASH;
		case (int)KeyboardApplication:
			return Keys.MENU;
		case (int)KeyboardPower:
			return Keys.POWER;
		case (int)KeypadEqualSign:
		case (int)KeypadEqualSignAS400:
			return Keys.NUMPAD_EQUALS;
		case (int)KeyboardHelp:
			return Keys.F1;
		case (int)KeyboardMenu:
			return Keys.MENU;
		case (int)KeyboardSelect:
			return Keys.BUTTON_SELECT;
		case (int)KeyboardStop:
			return Keys.MEDIA_STOP;
		case (int)KeyboardFind:
			return Keys.SEARCH;
		case (int)KeyboardMute:
			return Keys.MUTE;
		case (int)KeyboardVolumeUp:
			return Keys.VOLUME_UP;
		case (int)KeyboardVolumeDown:
			return Keys.VOLUME_DOWN;
		case (int)KeypadComma:
			return Keys.NUMPAD_COMMA;
		case (int)KeyboardAlternateErase:
			return Keys.DEL;
		case (int)KeyboardCancel:
			return Keys.ESCAPE;
		case (int)KeyboardClear:
			return Keys.CLEAR;
		case (int)KeyboardReturn:
			return Keys.ENTER;
		case (int)KeyboardLeftControl:
			return Keys.CONTROL_LEFT;
		case (int)KeyboardLeftShift:
			return Keys.SHIFT_LEFT;
		case (int)KeyboardLeftAlt:
			return Keys.ALT_LEFT;
		case (int)KeyboardRightControl:
			return Keys.CONTROL_RIGHT;
		case (int)KeyboardRightShift:
			return Keys.SHIFT_RIGHT;
		case (int)KeyboardRightAlt:
			return Keys.ALT_RIGHT;
		case (int)KeyboardCapsLock:
			return Keys.CAPS_LOCK;
		case (int)KeyboardPrintScreen:
			return Keys.PRINT_SCREEN;
		case (int)KeyboardScrollLock:
			return Keys.SCROLL_LOCK;
		default:
			return Keys.UNKNOWN;
		}
	}
}

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

package com.badlogic.gdx.backends.iosrobovm;

import org.robovm.apple.audiotoolbox.AudioServices;
import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.*;
import org.robovm.objc.annotation.Method;
import org.robovm.rt.VM;
import org.robovm.rt.bro.NativeObject;
import org.robovm.rt.bro.annotation.MachineSizedUInt;
import org.robovm.rt.bro.annotation.Pointer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.iosrobovm.custom.UIAcceleration;
import com.badlogic.gdx.backends.iosrobovm.custom.UIAccelerometer;
import com.badlogic.gdx.backends.iosrobovm.custom.UIAccelerometerDelegate;
import com.badlogic.gdx.backends.iosrobovm.custom.UIAccelerometerDelegateAdapter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

public class IOSInput implements Input {
	static final int MAX_TOUCHES = 20;
	private static final int POINTER_NOT_FOUND = -1;

	private static class NSObjectWrapper<T extends NSObject> {
		private static final long HANDLE_OFFSET;
		static {
			try {
				HANDLE_OFFSET = VM.getInstanceFieldOffset(VM.getFieldAddress(NativeObject.class.getDeclaredField("handle")));
			} catch (Throwable t) {
				throw new Error(t);
			}
		}

		private final T instance;

		public NSObjectWrapper (Class<T> cls) {
			instance = VM.allocateObject(cls);
		}

		public T wrap (long handle) {
			VM.setLong(VM.getObjectAddress(instance) + HANDLE_OFFSET, handle);
			return instance;
		}
	}

	private static final NSObjectWrapper<UITouch> UI_TOUCH_WRAPPER = new NSObjectWrapper<UITouch>(UITouch.class);
	static final NSObjectWrapper<UIAcceleration> UI_ACCELERATION_WRAPPER = new NSObjectWrapper<UIAcceleration>(UIAcceleration.class);
	
	IOSApplication app;
	IOSApplicationConfiguration config;
	int[] deltaX = new int[MAX_TOUCHES];
	int[] deltaY = new int[MAX_TOUCHES];
	int[] touchX = new int[MAX_TOUCHES];
	int[] touchY = new int[MAX_TOUCHES];
	float[] pressures = new float[MAX_TOUCHES];
	boolean pressureSupported;
	// we store the pointer to the UITouch struct here, or 0
	long[] touchDown = new long[MAX_TOUCHES];
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
	//CMMotionManager motionManager;
	protected UIAccelerometerDelegate accelerometerDelegate;
	boolean compassSupported;
	boolean keyboardCloseOnReturn;

	public IOSInput (IOSApplication app) {
		this.app = app;
		this.config = app.config;
		this.keyboardCloseOnReturn = app.config.keyboardCloseOnReturn;
	}

	void setupPeripherals () {
		//motionManager = new CMMotionManager();
		setupAccelerometer();
		setupCompass();
		UIDevice device = UIDevice.getCurrentDevice();
		if (device.getModel().equalsIgnoreCase("iphone")) hasVibrator = true;

		if (app.getIosVersion() >= 9){
			UIForceTouchCapability forceTouchCapability = UIScreen.getMainScreen().getTraitCollection().getForceTouchCapability();
			pressureSupported = forceTouchCapability == UIForceTouchCapability.Available;
		}
	}

	protected void setupCompass () {
		if (config.useCompass) {
			//setupMagnetometer();
		}
	}
	
	protected void setupAccelerometer () {
		if (config.useAccelerometer) {
			accelerometerDelegate = new UIAccelerometerDelegateAdapter() {

				@Method(selector = "accelerometer:didAccelerate:")
				public void didAccelerate (UIAccelerometer accelerometer, @Pointer long valuesPtr) {
					UIAcceleration values = UI_ACCELERATION_WRAPPER.wrap(valuesPtr);
					float x = (float)values.getX() * 10;
					float y = (float)values.getY() * 10;
					float z = (float)values.getZ() * 10;

					acceleration[0] = -x;
					acceleration[1] = -y;
					acceleration[2] = -z;
				}
			};
			UIAccelerometer.getSharedAccelerometer().setDelegate(accelerometerDelegate);
			UIAccelerometer.getSharedAccelerometer().setUpdateInterval(config.accelerometerUpdate);
		}
	}

	// need to retain a reference so GC doesn't get right of the
	// object passed to the native thread
//	VoidBlock2<CMAccelerometerData, NSError> accelVoid = null;	
//	private void setupAccelerometer () {
//		if (config.useAccelerometer) {
//			motionManager.setAccelerometerUpdateInterval(config.accelerometerUpdate);			
//			accelVoid = new VoidBlock2<CMAccelerometerData, NSError>() {
//				@Override
//				public void invoke(CMAccelerometerData accelData, NSError error) {
//					updateAccelerometer(accelData);					
//				}
//			};
//			motionManager.startAccelerometerUpdates(new NSOperationQueue(), accelVoid);
//		}
//	}
	
	// need to retain a reference so GC doesn't get right of the
	// object passed to the native thread
//	VoidBlock2<CMMagnetometerData, NSError> magnetVoid = null;
//	private void setupMagnetometer () {
//		if (motionManager.isMagnetometerAvailable() && config.useCompass) compassSupported = true;
//		else return;
//		motionManager.setMagnetometerUpdateInterval(config.magnetometerUpdate);
//		magnetVoid = new VoidBlock2<CMMagnetometerData, NSError>() {
//			@Override
//			public void invoke(CMMagnetometerData magnetData, NSError error) {
//				updateRotation(magnetData);
//			}
//		};
//		motionManager.startMagnetometerUpdates(new NSOperationQueue(), magnetVoid);
//	}
	
//	private void updateAccelerometer (CMAccelerometerData data) {
//		float x = (float) data.getAcceleration().x() * 10f;
//		float y = (float) data.getAcceleration().y() * 10f;
//		float z = (float) data.getAcceleration().z() * 10f;
//		acceleration[0] = -x;
//		acceleration[1] = -y;
//		acceleration[2] = -z;
//	}
//	
//	private void updateRotation (CMMagnetometerData data) {
//		final float eX = (float) data.getMagneticField().x();
//		final float eY = (float) data.getMagneticField().y();
//		final float eZ = (float) data.getMagneticField().z();
//				
//		float gX = acceleration[0];
//		float gY = acceleration[1];
//		float gZ = acceleration[2];
//		
//		float cX = eY * gZ - eZ * gY;
//		float cY = eZ * gX - eX * gZ;
//		float cZ = eX * gY - eY * gX;
//		
//		final float normal = (float) Math.sqrt(cX * cX + cY * cY + cZ * cZ);
//		final float invertC = 1.0f / normal;
//		cX *= invertC;
//		cY *= invertC;
//		cZ *= invertC;
//		final float invertG = 1.0f / (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);
//		gX *= invertG;
//		gY *= invertG;
//		gZ *= invertG;
//		final float mX = gY * cZ - gZ * cY;
//		final float mY = gZ * cX - gX * cZ;
//		final float mZ = gX * cY - gY * cX;
//		
//		R[0] = cX;	R[1] = cY;	R[2] = cZ;
//		R[3] = mX;	R[4] = mY;	R[5] = mZ;
//		R[6] = gX;	R[7] = gY;	R[8] = gZ;
//		
//		rotation[0] = (float) Math.atan2(R[1], R[4]) * MathUtils.radDeg;
//		rotation[1] = (float) Math.asin(-R[7]) * MathUtils.radDeg;
//		rotation[2] = (float) Math.atan2(-R[6], R[8]) * MathUtils.radDeg;
//	}

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
		//TODO implement when azimuth is fixed
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
			if (touchDown[pointer] != 0) {
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
		return touchDown[pointer] != 0;
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
	public boolean isKeyPressed (int key) {
		return false;
	}

	@Override
	public boolean isKeyJustPressed (int key) {
		return false;
	}

	@Override
	public void getTextInput(TextInputListener listener, String title, String text, String hint) {
		buildUIAlertView(listener, title, text, hint).show();
	}	

	// hack for software keyboard support
	// uses a hidden textfield to capture input
	// see: http://www.badlogicgames.com/forum/viewtopic.php?f=17&t=11788

	private class HiddenTextField extends UITextField {
		public HiddenTextField (CGRect frame) {
			super(frame);

			setKeyboardType(UIKeyboardType.Default);
			setReturnKeyType(UIReturnKeyType.Done);
			setAutocapitalizationType(UITextAutocapitalizationType.None);
			setAutocorrectionType(UITextAutocorrectionType.No);
			setSpellCheckingType(UITextSpellCheckingType.No);
			setHidden(true);
		}

		@Override
		public void deleteBackward () {
			app.input.inputProcessor.keyTyped((char)8);
			super.deleteBackward();
			Gdx.graphics.requestRendering();
		}
	}

	private UITextField textfield = null;
	private final UITextFieldDelegate textDelegate = new UITextFieldDelegateAdapter() {
		@Override
		public boolean shouldChangeCharacters (UITextField textField, NSRange range, String string) {
			for (int i = 0; i < range.getLength(); i++) {
				app.input.inputProcessor.keyTyped((char)8);
			}

			if (string.isEmpty()) {
				if (range.getLength() > 0) Gdx.graphics.requestRendering();
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
		public boolean shouldEndEditing (UITextField textField) {
			// Text field needs to have at least one symbol - so we can use backspace
			textField.setText("x");
			Gdx.graphics.requestRendering();

			return true;
		}

		@Override
		public boolean shouldReturn (UITextField textField) {
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
	
	/**
	 * Set the keyboard to close when the UITextField return key is pressed
	 * @param shouldClose Whether or not the keyboard should clsoe on return key press
	 */
	public void setKeyboardCloseOnReturnKey (boolean shouldClose) {
		keyboardCloseOnReturn = shouldClose;
	}
	
	public UITextField getKeyboardTextField () {
		if (textfield == null) createDefaultTextField();
		return textfield;
	}
	
	private void createDefaultTextField () {
		textfield = new UITextField(new CGRect(10, 10, 100, 50));
		//Parameters
		// Setting parameters
		textfield.setKeyboardType(UIKeyboardType.Default);
		textfield.setReturnKeyType(UIReturnKeyType.Done);
		textfield.setAutocapitalizationType(UITextAutocapitalizationType.None);
		textfield.setAutocorrectionType(UITextAutocorrectionType.No);
		textfield.setSpellCheckingType(UITextSpellCheckingType.No);
		textfield.setHidden(true);
		// Text field needs to have at least one symbol - so we can use backspace
		textfield.setText("x");
		app.getUIViewController().getView().addSubview(textfield);
	}
	
	// Issue 773 indicates this may solve a premature GC issue
	UIAlertViewDelegate delegate;

	/** Builds an {@link UIAlertView} with an added {@link UITextField} for inputting text.
	 * @param listener Text input listener
	 * @param title Dialog title
	 * @param text Text for text field
	 * @return UiAlertView */
	private UIAlertView buildUIAlertView (final TextInputListener listener, String title, String text, String placeholder) {
		delegate = new UIAlertViewDelegateAdapter() {
			@Override
			public void clicked (UIAlertView view, long clicked) {
				if (clicked == 0) {
					// user clicked "Cancel" button
					listener.canceled();
				} else if (clicked == 1) {
					// user clicked "Ok" button
					UITextField textField = view.getTextField(0);
					listener.input(textField.getText());
				}
				delegate = null;
			}

			@Override
			public void cancel (UIAlertView view) {
				listener.canceled();
				delegate = null;
			}
		};

		// build the view
		final UIAlertView uiAlertView = new UIAlertView();
		uiAlertView.setTitle(title);
		uiAlertView.addButton("Cancel");
		uiAlertView.addButton("Ok");
		uiAlertView.setAlertViewStyle(UIAlertViewStyle.PlainTextInput);
		uiAlertView.setDelegate(delegate);

		UITextField textField = uiAlertView.getTextField(0);
		textField.setPlaceholder(placeholder);
		textField.setText(text);

		return uiAlertView;
	}

	@Override
	public void vibrate (int milliseconds) {
		AudioServices.playSystemSound(4095);
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
	public boolean isCatchMenuKey() {
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
		if (peripheral == Peripheral.OnscreenKeyboard) return true;
		if (peripheral == Peripheral.Pressure) return pressureSupported;
		return false;
	}

	@Override
	public int getRotation () {
		// we measure orientation counter clockwise, just like on Android
		switch (app.uiApp.getStatusBarOrientation()) {
		case LandscapeLeft:
			return 270;
		case PortraitUpsideDown:
			return 180;
		case LandscapeRight:
			return 90;
		case Portrait:
		default:
			return 0;
		}
	}

	@Override
	public Orientation getNativeOrientation () {
		switch (app.uiApp.getStatusBarOrientation()) {
		case LandscapeLeft:
		case LandscapeRight:
			return Orientation.Landscape;
		default:
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

	protected void onTouch (long touches) {
		toTouchEvents(touches);
		Gdx.graphics.requestRendering();
	}

	void processEvents () {
		synchronized (touchEvents) {
			justTouched = false;
			for (TouchEvent event : touchEvents) {
				currentEvent = event;
				switch (event.phase) {
				case Began:
					if (inputProcessor != null) inputProcessor.touchDown(event.x, event.y, event.pointer, Buttons.LEFT);
					if (numTouched >= 1) justTouched = true;
					break;
				case Cancelled:
				case Ended:
					if (inputProcessor != null) inputProcessor.touchUp(event.x, event.y, event.pointer, Buttons.LEFT);
					break;
				case Moved:
				case Stationary:
					if (inputProcessor != null) inputProcessor.touchDragged(event.x, event.y, event.pointer);
					break;
				}
			}
			touchEventPool.freeAll(touchEvents);
			touchEvents.clear();
		}
	}

	private int getFreePointer () {
		for (int i = 0; i < touchDown.length; i++) {
			if (touchDown[i] == 0) return i;
		}
		throw new GdxRuntimeException("Couldn't find free pointer id!");
	}

	private int findPointer (UITouch touch) {
		long ptr = touch.getHandle();
		for (int i = 0; i < touchDown.length; i++) {
			if (touchDown[i] == ptr) return i;
		}
		// If pointer is not found
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < touchDown.length; i++) {
			sb.append(i + ":" + touchDown[i] + " ");
		}
		Gdx.app.error("IOSInput", "Pointer ID lookup failed: " + ptr + ", " + sb.toString());
		return POINTER_NOT_FOUND;
	}

	private static class NSSetExtensions extends NSExtensions {
		@Method(selector = "allObjects")
		public static native @Pointer long allObjects (@Pointer long thiz);
	}

	private static class NSArrayExtensions extends NSExtensions {
		@Method(selector = "objectAtIndex:")
		public static native @Pointer long objectAtIndex$ (@Pointer long thiz, @MachineSizedUInt long index);

		@Method(selector = "count")
		public static native @MachineSizedUInt long count (@Pointer long thiz);
	}

	private void toTouchEvents (long touches) {
		long array = NSSetExtensions.allObjects(touches);
		int length = (int)NSArrayExtensions.count(array);
		for (int i = 0; i < length; i++) {
			long touchHandle = NSArrayExtensions.objectAtIndex$(array, i);
			UITouch touch = UI_TOUCH_WRAPPER.wrap(touchHandle);
			final int locX, locY;
			// Get and map the location to our drawing space
			{
				CGPoint loc = touch.getLocationInView(touch.getWindow());
				final CGRect bounds = app.getCachedBounds();
				locX = (int)(loc.getX() * app.displayScaleFactor - bounds.getMinX());
				locY = (int)(loc.getY() * app.displayScaleFactor - bounds.getMinY());
				// app.debug("IOSInput","pos= "+loc+"  bounds= "+bounds+" x= "+locX+" locY= "+locY);
			}

			// if its not supported, we will simply use 1.0f when touch is present
			float pressure = 1.0f;
			if (pressureSupported) {
				pressure = (float)touch.getForce();
			}

			synchronized (touchEvents) {
				UITouchPhase phase = touch.getPhase();
				TouchEvent event = touchEventPool.obtain();
				event.x = locX;
				event.y = locY;
				event.phase = phase;
				event.timestamp = (long)(touch.getTimestamp() * 1000000000);

				if (phase == UITouchPhase.Began) {
					event.pointer = getFreePointer();
					touchDown[event.pointer] = touch.getHandle();
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
						touchDown[event.pointer] = 0;
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
		UITouchPhase phase;
		long timestamp;
		int x, y;
		int pointer;
	}

	@Override
	public float getGyroscopeX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getGyroscopeY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getGyroscopeZ() {
		// TODO Auto-generated method stub
		return 0;
	}


}

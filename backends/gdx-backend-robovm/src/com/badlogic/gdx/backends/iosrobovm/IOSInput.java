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

import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSExtensions;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSRange;
import org.robovm.apple.uikit.UIAcceleration;
import org.robovm.apple.uikit.UIAccelerometer;
import org.robovm.apple.uikit.UIAccelerometerDelegate;
import org.robovm.apple.uikit.UIAccelerometerDelegateAdapter;
import org.robovm.apple.uikit.UIAlertView;
import org.robovm.apple.uikit.UIAlertViewDelegate;
import org.robovm.apple.uikit.UIAlertViewDelegateAdapter;
import org.robovm.apple.uikit.UIAlertViewStyle;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIEvent;
import org.robovm.apple.uikit.UIInterfaceOrientation;
import org.robovm.apple.uikit.UIKeyboardType;
import org.robovm.apple.uikit.UIReturnKeyType;
import org.robovm.apple.uikit.UITextAutocapitalizationType;
import org.robovm.apple.uikit.UITextAutocorrectionType;
import org.robovm.apple.uikit.UITextField;
import org.robovm.apple.uikit.UITextFieldDelegate;
import org.robovm.apple.uikit.UITextFieldDelegateAdapter;
import org.robovm.apple.uikit.UITextSpellCheckingType;
import org.robovm.apple.uikit.UITouch;
import org.robovm.apple.uikit.UITouchPhase;
import org.robovm.objc.annotation.Method;
import org.robovm.rt.VM;
import org.robovm.rt.bro.NativeObject;
import org.robovm.rt.bro.annotation.MachineSizedUInt;
import org.robovm.rt.bro.annotation.Pointer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

public class IOSInput implements Input {
	static final int MAX_TOUCHES = 20;

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
	static final NSObjectWrapper<UIAcceleration> UI_ACCELERATION_WRAPPER = new NSObjectWrapper<UIAcceleration>(
		UIAcceleration.class);

	IOSApplication app;
	IOSApplicationConfiguration config;
	int[] deltaX = new int[MAX_TOUCHES];
	int[] deltaY = new int[MAX_TOUCHES];
	int[] touchX = new int[MAX_TOUCHES];
	int[] touchY = new int[MAX_TOUCHES];
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
	InputProcessor inputProcessor = null;
	// We need to hold on to the reference to this delegate or else its
	// ObjC peer will get released when the Java peer is GCed.
	UIAccelerometerDelegate accelerometerDelegate;

	public IOSInput (IOSApplication app) {
		this.app = app;
		this.config = app.config;
	}

	void setupPeripherals () {
		setupAccelerometer();
		setupCompass();
	}

	private void setupCompass () {
		if (config.useCompass) {
			// FIXME implement compass
		}
	}

	private void setupAccelerometer () {
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
		// FIXME implement this
		return 0;
	}

	@Override
	public float getPitch () {
		// FIXME implement this
		return 0;
	}

	@Override
	public float getRoll () {
		// FIXME implement this
		return 0;
	}

	@Override
	public void getRotationMatrix (float[] matrix) {
		// FIXME implement this
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
	public void getTextInput (TextInputListener listener, String title, String text) {
		final UIAlertView uiAlertView = buildUIAlertView(listener, title, text, null);
		uiAlertView.show();
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
			for (int i = 0; i < range.length(); i++) {
				app.input.inputProcessor.keyTyped((char)8);
			}

			if (string.isEmpty()) {
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
			textField.resignFirstResponder();
			Gdx.graphics.requestRendering();
			return false;
		}
	};

	@Override
	public void setOnscreenKeyboardVisible (boolean visible) {
		if (textfield == null) {
			// Making simple textField
			textfield = new UITextField(new CGRect(10, 10, 100, 50));
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
		if (visible) {
			textfield.becomeFirstResponder();
			textfield.setDelegate(textDelegate);
		} else {
			textfield.resignFirstResponder();
		}
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
	public void getPlaceholderTextInput (TextInputListener listener, String title, String placeholder) {
		final UIAlertView uiAlertView = buildUIAlertView(listener, title, null, placeholder);
		uiAlertView.show();
	}

	@Override
	public void vibrate (int milliseconds) {
		// FIXME implement this
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
		// FIXME implement this (not sure if possible)
// if(peripheral == Peripheral.OnscreenKeyboard) return true;
		// FIXME implement this
// if(peripheral == Peripheral.Compass) return true;

		return false;
	}

	@Override
	public int getRotation () {
		UIInterfaceOrientation orientation = app.graphics.viewController != null ? app.graphics.viewController
			.getInterfaceOrientation() : UIApplication.getSharedApplication().getStatusBarOrientation();
		// we measure orientation counter clockwise, just like on Android
		if (orientation == UIInterfaceOrientation.Portrait) return 0;
		if (orientation == UIInterfaceOrientation.LandscapeLeft) return 270;
		if (orientation == UIInterfaceOrientation.PortraitUpsideDown) return 180;
		if (orientation == UIInterfaceOrientation.LandscapeRight) return 90;
		return 0;
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
	public void setCursorImage (Pixmap pixmap, int xHotspot, int yHotspot) {
	}

	public void touchDown (long touches, UIEvent event) {
		toTouchEvents(touches, event);
		Gdx.graphics.requestRendering();
	}

	public void touchUp (long touches, UIEvent event) {
		toTouchEvents(touches, event);
		Gdx.graphics.requestRendering();
	}

	public void touchMoved (long touches, UIEvent event) {
		toTouchEvents(touches, event);
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
					if (numTouched == 1) justTouched = true;
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
		throw new GdxRuntimeException("Couldn't find pointer id for touch event!");
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

	private void toTouchEvents (long touches, UIEvent uiEvent) {
		long array = NSSetExtensions.allObjects(touches);
		int length = (int)NSArrayExtensions.count(array);
		for (int i = 0; i < length; i++) {
			long touchHandle = NSArrayExtensions.objectAtIndex$(array, i);
			UITouch touch = UI_TOUCH_WRAPPER.wrap(touchHandle);
			CGPoint loc = touch.getLocation(touch.getView());
			synchronized (touchEvents) {
				UITouchPhase phase = touch.getPhase();
				TouchEvent event = touchEventPool.obtain();
				event.x = (int)(loc.x() * app.displayScaleFactor);
				event.y = (int)(loc.y() * app.displayScaleFactor);
				event.phase = phase;
				event.timestamp = (long)(touch.getTimestamp() * 1000000000);
				touchEvents.add(event);

				if (phase == UITouchPhase.Began) {
					event.pointer = getFreePointer();
					touchDown[event.pointer] = (int)touch.getHandle();
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
					touchDown[event.pointer] = 0;
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
		UITouchPhase phase;
		long timestamp;
		int x, y;
		int pointer;
	}
}

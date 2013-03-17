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
package com.badlogic.gdx.backends.ios;

import cli.MonoTouch.Foundation.NSObject;
import cli.MonoTouch.Foundation.NSSet;
import cli.MonoTouch.Foundation.NSSetEnumerator;
import cli.MonoTouch.UIKit.UIAcceleration;
import cli.MonoTouch.UIKit.UIAccelerometer;
import cli.MonoTouch.UIKit.UIAccelerometerDelegate;
import cli.MonoTouch.UIKit.UIAlertView;
import cli.MonoTouch.UIKit.UIAlertViewDelegate;
import cli.MonoTouch.UIKit.UIAlertViewStyle;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UITextField;
import cli.MonoTouch.UIKit.UITouch;
import cli.MonoTouch.UIKit.UITouchPhase;
import cli.MonoTouch.UIKit.UIView;
import cli.System.Drawing.PointF;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

public class IOSInput implements Input {
	static final int MAX_TOUCHES = 20;

	IOSApplication app;
	IOSApplicationConfiguration config;
	int[] deltaX = new int[MAX_TOUCHES];
	int[] deltaY = new int[MAX_TOUCHES];
	int[] touchX = new int[MAX_TOUCHES];
	int[] touchY = new int[MAX_TOUCHES];
	// we store the pointer to the UITouch struct here, or 0
	int[] touchDown = new int[MAX_TOUCHES];
	int numTouched = 0;
	boolean justTouched = false;
	Pool<TouchEvent> touchEventPool = new Pool<TouchEvent>() {
		@Override
		protected TouchEvent newObject() {
			return new TouchEvent();
		}
	};
	Array<TouchEvent> touchEvents = new Array<TouchEvent>();
	TouchEvent currentEvent = null;
	float[] acceleration = new float[3];
	InputProcessor inputProcessor = null;
	
	public IOSInput(IOSApplication app) {
		this.app = app;
		this.config = app.config;
	}
	
	void setupPeripherals() {
		setupAccelerometer();
	}

	private void setupAccelerometer() {
		if(config.useAccelerometer) {
			UIAccelerometer.get_SharedAccelerometer().set_Delegate(new UIAccelerometerDelegate() {

				@Override
				public void DidAccelerate(UIAccelerometer accelerometer, UIAcceleration values) {
					//super.DidAccelerate(accelerometer, values);
					// FIXME take orientation into account, these values here get flipped by iOS...
					acceleration[0] = (float)values.get_X() * 10;
					acceleration[1] = (float)values.get_Y() * 10;
					acceleration[2] = (float)values.get_Z() * 10;
				}
			});
			UIAccelerometer.get_SharedAccelerometer().set_UpdateInterval(config.accelerometerUpdate);
		}
	}

	@Override
	public float getAccelerometerX() {
		return acceleration[0];
	}

	@Override
	public float getAccelerometerY() {
		return acceleration[1];
	}

	@Override
	public float getAccelerometerZ() {
		return acceleration[2];
	}

	@Override
	public float getAzimuth() {
		// FIXME implement this
		return 0;
	}

	@Override
	public float getPitch() {
		// FIXME implement this
		return 0;
	}

	@Override
	public float getRoll() {
		// FIXME implement this
		return 0;
	}

	@Override
	public void getRotationMatrix(float[] matrix) {
		// FIXME implement this
	}

	@Override
	public int getX() {
		return touchX[0];
	}

	@Override
	public int getX(int pointer) {
		return touchX[pointer];
	}

	@Override
	public int getDeltaX() {
		return deltaX[0];
	}

	@Override
	public int getDeltaX(int pointer) {
		return deltaX[pointer];
	}

	@Override
	public int getY() {
		return touchY[0];
	}

	@Override
	public int getY(int pointer) {
		return touchY[pointer];
	}

	@Override
	public int getDeltaY() {
		return deltaY[0];
	}

	@Override
	public int getDeltaY(int pointer) {
		return deltaY[pointer];
	}

	@Override
	public boolean isTouched() {
		return touchDown[0] != 0;
	}

	@Override
	public boolean justTouched() {
		return justTouched;
	}

	@Override
	public boolean isTouched(int pointer) {
		return touchDown[pointer] != 0;
	}

	@Override
	public boolean isButtonPressed(int button) {
		return button == Buttons.LEFT && numTouched > 0;
	}

	@Override
	public boolean isKeyPressed(int key) {
		return false;
	}

	@Override
	public void getTextInput(TextInputListener listener, String title, String text) {
		final UIAlertView uiAlertView = buildUIAlertView(listener, title, text);
		app.uiViewController.Add(uiAlertView);
		uiAlertView.Show();
	}
	
	/** Builds an {@link UIAlertView} with an added {@link UITextField} for inputting text.
	 * @param listener Text input listener
	 * @param title Dialog title
	 * @param text Text for text field
	 * @return UiAlertView */
	private UIAlertView buildUIAlertView (final TextInputListener listener, String title, String text) {
		UIAlertViewDelegate delegate = new UIAlertViewDelegate() {
			@Override
			public void Clicked (UIAlertView view, int clicked) {
				if (clicked == 0) {
					// user clicked "Cancel" button
					listener.canceled();
				} else if (clicked == 1) {
					// user clicked "Ok" button
					UIView[] views = view.get_Subviews();
					for (UIView uiView : views) {
						// find text field from sub views
						if (uiView != null && uiView instanceof UITextField) {
							UITextField tf = (UITextField)uiView;
							listener.input(tf.get_Text());
						}
					}
				}
				view.Dispose(); // is this necessary?
			}

			@Override
			public void Canceled (UIAlertView view) {
				listener.canceled();
				view.Dispose(); // is this necessary?
			}
		};

		// build the view
		final UIAlertView uiAlertView = new UIAlertView();
		uiAlertView.set_Title(title);
		uiAlertView.AddButton("Cancel");
		uiAlertView.AddButton("Ok");
		uiAlertView.set_AlertViewStyle(UIAlertViewStyle.wrap(UIAlertViewStyle.PlainTextInput));
		uiAlertView.set_Delegate(delegate);

		for (UIView uiView : uiAlertView.get_Subviews()) {
			// find text field from sub views and add default text
			if (uiView != null && uiView instanceof UITextField) {
				UITextField tf = (UITextField)uiView;
				tf.set_Text(text);
			}
		}

		return uiAlertView;
	}

	@Override
	public void getPlaceholderTextInput(TextInputListener listener, String title, String placeholder) {
		// FIXME implement this
	}

	@Override
	public void setOnscreenKeyboardVisible(boolean visible) {
	}

	@Override
	public void vibrate(int milliseconds) {
		// FIXME implement this
	}

	@Override
	public void vibrate(long[] pattern, int repeat) {
		// FIXME implement this
	}

	@Override
	public void cancelVibrate() {
		// FIXME implement this
	}

	@Override
	public long getCurrentEventTime() {
		return currentEvent.timestamp;
	}

	@Override
	public void setCatchBackKey(boolean catchBack) {
	}

	@Override
	public void setCatchMenuKey(boolean catchMenu) {
	}

	@Override
	public void setInputProcessor(InputProcessor processor) {
		this.inputProcessor = processor;
	}

	@Override
	public InputProcessor getInputProcessor() {
		return inputProcessor;
	}

	@Override
	public boolean isPeripheralAvailable(Peripheral peripheral) {
		if(peripheral == Peripheral.Accelerometer && config.useAccelerometer) return true;
		if(peripheral == Peripheral.MultitouchScreen) return true;
		// FIXME implement this (not sure if possible)
//		if(peripheral == Peripheral.OnscreenKeyboard) return true;
		// FIXME implement this
//		if(peripheral == Peripheral.Compass) return true;
		
		return false;
	}

	@Override
	public int getRotation() {
		// FIXME implement this
		return 0;
	}

	@Override
	public Orientation getNativeOrientation() {
		// FIXME implement this
		return null;
	}

	@Override
	public void setCursorCatched(boolean catched) {
	}

	@Override
	public boolean isCursorCatched() {
		return false;
	}

	@Override
	public void setCursorPosition(int x, int y) {
	}

	public void touchDown(NSSet touches, UIEvent event) {
		toTouchEvents(touches, event);
	}

	public void touchUp(NSSet touches, UIEvent event) {
		toTouchEvents(touches, event);
	}

	public void touchMoved(NSSet touches, UIEvent event) {
		toTouchEvents(touches, event);
	}
	
	void processEvents() {
		synchronized(touchEvents) {
			justTouched = false;
			for(TouchEvent event: touchEvents) {
				currentEvent = event;
				switch(event.phase) {
				case UITouchPhase.Began:
					if(inputProcessor != null) inputProcessor.touchDown(event.x, event.y, event.pointer, Buttons.LEFT);
					if(numTouched == 1)
						justTouched = true;
					break;
				case UITouchPhase.Cancelled:
				case UITouchPhase.Ended:
					if(inputProcessor != null) inputProcessor.touchUp(event.x, event.y, event.pointer, Buttons.LEFT);
					break;
				case UITouchPhase.Moved:
				case UITouchPhase.Stationary:
					if(inputProcessor != null) inputProcessor.touchDragged(event.x, event.y, event.pointer);
					break;
				}
			}
			touchEventPool.freeAll(touchEvents);
			touchEvents.clear();
		}
	}
	
	NSSetEnumerator touchEnumerator = new NSSetEnumerator(new NSSetEnumerator.Method() {
		public void Invoke(NSObject obj, boolean[] stop) {
			UITouch touch = (UITouch) obj;
			PointF loc = touch.LocationInView(touch.get_View());
			synchronized(touchEvents) {
				TouchEvent event = touchEventPool.obtain();
				event.x = (int)(loc.get_X() * app.displayScaleFactor);
				event.y = (int)(loc.get_Y() * app.displayScaleFactor);
				event.phase = touch.get_Phase().Value;
				event.timestamp = (long)(touch.get_Timestamp() * 1000000000);
				touchEvents.add(event);
				
				if(touch.get_Phase().Value == UITouchPhase.Began) {
					event.pointer = getFreePointer();
					touchDown[event.pointer] = touch.get_Handle().ToInt32();
					touchX[event.pointer] = event.x;
					touchY[event.pointer] = event.y;
					deltaX[event.pointer] = 0;
					deltaY[event.pointer] = 0; 
					numTouched++;
				}
				
				if(touch.get_Phase().Value == UITouchPhase.Moved ||
					touch.get_Phase().Value == UITouchPhase.Stationary) {
					event.pointer = findPointer(touch);
					deltaX[event.pointer] = event.x - touchX[event.pointer];
					deltaY[event.pointer] = event.y - touchY[event.pointer]; 
					touchX[event.pointer] = event.x;
					touchY[event.pointer] = event.y;
				}
				
				if(touch.get_Phase().Value == UITouchPhase.Cancelled ||
					touch.get_Phase().Value == UITouchPhase.Ended) {
					event.pointer = findPointer(touch);
					touchDown[event.pointer] = 0; 
					touchX[event.pointer] = event.x;
					touchY[event.pointer] = event.y;
					deltaX[event.pointer] = 0;
					deltaY[event.pointer] = 0;
					numTouched--;
				}
			}
			stop[0] = false;
		}
	});
	
	int getFreePointer() {
		for(int i = 0; i < touchDown.length; i++) {
			if(touchDown[i] == 0) return i;
		}
		throw new GdxRuntimeException("Couldn't find free pointer id!");
	}
	
	int findPointer(UITouch touch) {
		int ptr = touch.get_Handle().ToInt32();
		for(int i = 0; i < touchDown.length; i++) {
			if(touchDown[i] == ptr) return i;
		}
		throw new GdxRuntimeException("Couldn't find pointer id for touch event!");
	}

	private void toTouchEvents(NSSet touches, UIEvent event) {
		touches.Enumerate(touchEnumerator);
	}
	
	static class TouchEvent {
		int phase;
		long timestamp;
		int x, y;
		int pointer;
	}
}
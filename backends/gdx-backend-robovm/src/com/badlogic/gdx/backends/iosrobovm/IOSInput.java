package com.badlogic.gdx.backends.iosrobovm;

import org.robovm.cocoatouch.coregraphics.CGPoint;
import org.robovm.cocoatouch.foundation.NSArray;
import org.robovm.cocoatouch.foundation.NSSet;
import org.robovm.cocoatouch.uikit.UIAcceleration;
import org.robovm.cocoatouch.uikit.UIAccelerometer;
import org.robovm.cocoatouch.uikit.UIAccelerometerDelegate;
import org.robovm.cocoatouch.uikit.UIAlertView;
import org.robovm.cocoatouch.uikit.UIAlertViewDelegate;
import org.robovm.cocoatouch.uikit.UIAlertViewStyle;
import org.robovm.cocoatouch.uikit.UIApplication;
import org.robovm.cocoatouch.uikit.UIEvent;
import org.robovm.cocoatouch.uikit.UIInterfaceOrientation;
import org.robovm.cocoatouch.uikit.UITextField;
import org.robovm.cocoatouch.uikit.UITouch;
import org.robovm.cocoatouch.uikit.UITouchPhase;
import org.robovm.cocoatouch.uikit.UIView;
import org.robovm.objc.ObjCClass;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

public class IOSInput implements Input {
	static final int MAX_TOUCHES = 20;

	static {
		// HACK: Make sure UITouch has been registered
		ObjCClass.getByType(UITouch.class);
	}
	
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
	// We need to hold on to the reference to this delegate or else its
	// ObjC peer will get released when the Java peer is GCed.
	UIAccelerometerDelegate accelerometerDelegate;
	
	public IOSInput(IOSApplication app) {
		this.app = app;
		this.config = app.config;
	}
	
	void setupPeripherals() {
		setupAccelerometer();
	}

	private void setupAccelerometer() {
		if(config.useAccelerometer) {
			accelerometerDelegate = new UIAccelerometerDelegate.Adapter() {

				@Override
				public void didAccelerate(UIAccelerometer accelerometer, UIAcceleration values) {
					//super.DidAccelerate(accelerometer, values);
					float x = (float)values.getX() * 10;
					float y = (float)values.getY() * 10;
					float z = (float)values.getZ() * 10;

					UIInterfaceOrientation orientation = app.graphics.viewController != null 
							? app.graphics.viewController.getInterfaceOrientation() 
									: UIApplication.getSharedApplication().getStatusBarOrientation();

					if (orientation == UIInterfaceOrientation.LandscapeLeft || orientation == UIInterfaceOrientation.LandscapeRight) {
						float t = x;
						x = y;
						y = t;
					}
					if (orientation == UIInterfaceOrientation.LandscapeLeft || orientation == UIInterfaceOrientation.Portrait) {
						x = -x;
					}
					
					acceleration[0] = x;
					acceleration[1] = y;
					acceleration[2] = z;
				}
			};
			UIAccelerometer.getSharedAccelerometer().setDelegate(accelerometerDelegate);
			UIAccelerometer.getSharedAccelerometer().setUpdateInterval(config.accelerometerUpdate);
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
		uiAlertView.show();
	}
	
	/** Builds an {@link UIAlertView} with an added {@link UITextField} for inputting text.
	 * @param listener Text input listener
	 * @param title Dialog title
	 * @param text Text for text field
	 * @return UiAlertView */
	private UIAlertView buildUIAlertView (final TextInputListener listener, String title, String text) {
		UIAlertViewDelegate delegate = new UIAlertViewDelegate.Adapter() {
			@Override
			public void clicked (UIAlertView view, int clicked) {
				if (clicked == 0) {
					// user clicked "Cancel" button
					listener.canceled();
				} else if (clicked == 1) {
					// user clicked "Ok" button
					NSArray<UIView> views = view.getSubviews();
					for (UIView uiView : views) {
						// find text field from sub views
						if (uiView != null && uiView instanceof UITextField) {
							UITextField tf = (UITextField)uiView;
							listener.input(tf.getText());
						}
					}
				}
			}

			@Override
			public void cancel (UIAlertView view) {
				listener.canceled();
			}
		};

		// build the view
		final UIAlertView uiAlertView = new UIAlertView();
		uiAlertView.setTitle(title);
		uiAlertView.addButton("Cancel");
		uiAlertView.addButton("Ok");
		uiAlertView.setAlertViewStyle(UIAlertViewStyle.PlainTextInput);
		uiAlertView.setDelegate(delegate);

		for (UIView uiView : (NSArray<UIView>) uiAlertView.getSubviews()) {
			// find text field from sub views and add default text
			if (uiView != null && uiView instanceof UITextField) {
				UITextField tf = (UITextField)uiView;
				tf.setText(text);
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
				case Began:
					if(inputProcessor != null) inputProcessor.touchDown(event.x, event.y, event.pointer, Buttons.LEFT);
					if(numTouched == 1)
						justTouched = true;
					break;
				case Cancelled:
				case Ended:
					if(inputProcessor != null) inputProcessor.touchUp(event.x, event.y, event.pointer, Buttons.LEFT);
					break;
				case Moved:
				case Stationary:
					if(inputProcessor != null) inputProcessor.touchDragged(event.x, event.y, event.pointer);
					break;
				}
			}
			touchEventPool.freeAll(touchEvents);
			touchEvents.clear();
		}
	}
	
	private int getFreePointer() {
		for(int i = 0; i < touchDown.length; i++) {
			if(touchDown[i] == 0) return i;
		}
		throw new GdxRuntimeException("Couldn't find free pointer id!");
	}
	
	private int findPointer(UITouch touch) {
		int ptr = (int) touch.getHandle();
		for(int i = 0; i < touchDown.length; i++) {
			if(touchDown[i] == ptr) return i;
		}
		throw new GdxRuntimeException("Couldn't find pointer id for touch event!");
	}

	private void toTouchEvents(NSSet touches, UIEvent uiEvent) {
		for (UITouch touch : (NSSet<UITouch>) touches) {
			CGPoint loc = touch.getLocation(touch.getView());
			synchronized(touchEvents) {
				TouchEvent event = touchEventPool.obtain();
				event.x = (int)(loc.x() * app.displayScaleFactor);
				event.y = (int)(loc.y() * app.displayScaleFactor);
				event.phase = touch.getPhase();
				event.timestamp = (long)(touch.getTimestamp() * 1000000000);
				touchEvents.add(event);
				
				if(touch.getPhase() == UITouchPhase.Began) {
					event.pointer = getFreePointer();
					touchDown[event.pointer] = (int) touch.getHandle();
					touchX[event.pointer] = event.x;
					touchY[event.pointer] = event.y;
					deltaX[event.pointer] = 0;
					deltaY[event.pointer] = 0; 
					numTouched++;
				}
				
				if(touch.getPhase() == UITouchPhase.Moved ||
					touch.getPhase() == UITouchPhase.Stationary) {
					event.pointer = findPointer(touch);
					deltaX[event.pointer] = event.x - touchX[event.pointer];
					deltaY[event.pointer] = event.y - touchY[event.pointer]; 
					touchX[event.pointer] = event.x;
					touchY[event.pointer] = event.y;
				}
				
				if(touch.getPhase() == UITouchPhase.Cancelled ||
					touch.getPhase() == UITouchPhase.Ended) {
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
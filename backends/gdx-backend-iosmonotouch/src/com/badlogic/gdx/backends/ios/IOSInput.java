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
import cli.MonoTouch.Foundation.NSSetEnumerator.Method;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UITouch;
import cli.System.Drawing.PointF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class IOSInput implements Input {
	static final int MAX_TOUCHES = 20;

	int[] deltaX = new int[MAX_TOUCHES];
	int[] deltaY = new int[MAX_TOUCHES];
	int[] touchX = new int[MAX_TOUCHES];
	int[] touchY = new int[MAX_TOUCHES];
	boolean[] touchDown = new boolean[MAX_TOUCHES];
	boolean justTouched = false;
	Pool<TouchEvent> touchEventPool = new Pool<TouchEvent>() {
		@Override
		protected TouchEvent newObject() {
			return new TouchEvent();
		}
	};
	Array<TouchEvent> touchEvents = new Array<TouchEvent>();

	@Override
	public float getAccelerometerX() {
		return 0;
	}

	@Override
	public float getAccelerometerY() {
		return 0;
	}

	@Override
	public float getAccelerometerZ() {
		return 0;
	}

	@Override
	public float getAzimuth() {
		return 0;
	}

	@Override
	public float getPitch() {
		return 0;
	}

	@Override
	public float getRoll() {
		return 0;
	}

	@Override
	public void getRotationMatrix(float[] matrix) {
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
		return touchDown[0];
	}

	@Override
	public boolean justTouched() {
		return justTouched;
	}

	@Override
	public boolean isTouched(int pointer) {
		return touchDown[pointer];
	}

	@Override
	public boolean isButtonPressed(int button) {
		return false;
	}

	@Override
	public boolean isKeyPressed(int key) {
		return false;
	}

	@Override
	public void getTextInput(TextInputListener listener, String title,
			String text) {
	}

	@Override
	public void getPlaceholderTextInput(TextInputListener listener,
			String title, String placeholder) {
	}

	@Override
	public void setOnscreenKeyboardVisible(boolean visible) {
	}

	@Override
	public void vibrate(int milliseconds) {
	}

	@Override
	public void vibrate(long[] pattern, int repeat) {
	}

	@Override
	public void cancelVibrate() {
	}

	@Override
	public long getCurrentEventTime() {
		return 0;
	}

	@Override
	public void setCatchBackKey(boolean catchBack) {
	}

	@Override
	public void setCatchMenuKey(boolean catchMenu) {
	}

	@Override
	public void setInputProcessor(InputProcessor processor) {
	}

	@Override
	public InputProcessor getInputProcessor() {
		return null;
	}

	@Override
	public boolean isPeripheralAvailable(Peripheral peripheral) {
		return false;
	}

	@Override
	public int getRotation() {
		return 0;
	}

	@Override
	public Orientation getNativeOrientation() {
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

	private void toTouchEvents(NSSet touches, UIEvent event) {
		Gdx.app.log("IOSInput", "got " + touches.get_Count() + " touches" );
		touches.Enumerate(new NSSetEnumerator(new NSSetEnumerator.Method() {
			public void Invoke(NSObject obj, boolean[] stop) {
				UITouch touch = (UITouch) obj;
				PointF loc = touch.LocationInView(touch.get_View());
				Gdx.app.log("IOSInput", "" + touch.get_Phase() + ", " + loc.get_X() + ", " + loc.get_Y());
				stop[0] = false;
			}
		}));
	}
	
	static class TouchEvent {
		long timestamp;
		int x, y;
		int pointer;
	}
}
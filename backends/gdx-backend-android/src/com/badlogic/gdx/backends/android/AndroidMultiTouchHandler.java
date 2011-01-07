/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.backends.android;

import android.view.MotionEvent;

import com.badlogic.gdx.backends.android.AndroidInput.TouchEvent;

/**
 * Multitouch handler for devices running Android >= 2.0. If device is capable of (fake) multitouch this will report additional
 * pointers.
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public class AndroidMultiTouchHandler implements AndroidTouchHandler {
	public void onTouch (MotionEvent event, AndroidInput input) {
		final int action = event.getAction() & MotionEvent.ACTION_MASK;
		int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		int pointerId = event.getPointerId(pointerIndex);

		int x = 0, y = 0;

		switch (action) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			x = (int)event.getX(pointerIndex);
			y = (int)event.getY(pointerIndex);
			postTouchEvent(input, TouchEvent.TOUCH_DOWN, x, y, pointerId);
			input.touchX[pointerId] = x;
			input.touchY[pointerId] = y;
			input.touched[pointerId] = true;
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_OUTSIDE:
		case MotionEvent.ACTION_CANCEL:
			x = (int)event.getX(pointerIndex);
			y = (int)event.getY(pointerIndex);
			postTouchEvent(input, TouchEvent.TOUCH_UP, x, y, pointerId);
			input.touchX[pointerId] = x;
			input.touchY[pointerId] = y;
			input.touched[pointerId] = false;
			break;

		case MotionEvent.ACTION_MOVE:
			int pointerCount = event.getPointerCount();
			for (int i = 0; i < pointerCount; i++) {
				pointerIndex = i;
				pointerId = event.getPointerId(pointerIndex);
				x = (int)event.getX(pointerIndex);
				y = (int)event.getY(pointerIndex);
				postTouchEvent(input, TouchEvent.TOUCH_DRAGGED, x, y, pointerId);
				input.touchX[pointerId] = x;
				input.touchY[pointerId] = y;
			}
			break;
		}
	}

	private void postTouchEvent (AndroidInput input, int type, int x, int y, int pointer) {
		long timeStamp = System.nanoTime();
		synchronized (input) {
			TouchEvent event = input.usedTouchEvents.obtain();
			event.timeStamp = timeStamp;
			event.pointer = pointer;
			event.x = x;
			event.y = y;
			event.type = type;
			input.touchEvents.add(event);
		}
	}

	public boolean supportsMultitouch (AndroidApplication activity) {
		return activity.getPackageManager().hasSystemFeature("android.hardware.touchscreen.multitouch");
	}
}

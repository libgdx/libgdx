/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Modified by Elijah Cornell
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

import com.badlogic.gdx.backends.android.AndroidInputBase.TouchEvent;

/**
 * Single touch handler for devices running <= 1.6
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public class AndroidSingleTouchHandlerLW implements AndroidTouchHandlerLW {

	@Override
	public void onTap(int pX, int pY, AndroidInputLW input) {
		
		input.touchX[0] = pX;
		input.touchY[0] = pY;

		postTouchEvent(input, TouchEvent.TOUCH_DOWN, pX, pY, 0);
		input.touched[0] = true;

	}
	
	@Override
	public void onDrop(int pX, int pY, AndroidInputLW input) {
		
		input.touchX[0] = pX;
		input.touchY[0] = pY;

		postTouchEvent(input, TouchEvent.TOUCH_DRAGGED, pX, pY, 0);
		input.touched[0] = true;

	}

	private void postTouchEvent(AndroidInputLW input, int type, int x, int y,
			int pointer) {
		long timeStamp = System.nanoTime();
		synchronized (input) {
			TouchEvent event = input.usedTouchEvents.obtain();
			event.timeStamp = timeStamp;
			event.pointer = 0;
			event.x = x;
			event.y = y;
			event.type = type;
			input.touchEvents.add(event);
		}
	}

}

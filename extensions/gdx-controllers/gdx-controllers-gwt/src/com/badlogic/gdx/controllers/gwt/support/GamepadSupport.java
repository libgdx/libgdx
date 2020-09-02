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

package com.badlogic.gdx.controllers.gwt.support;

import com.badlogic.gdx.utils.IntMap;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class GamepadSupport {
	
	private static Ticker ticker = new Ticker();

	private static GamepadSupportListener listener;

	private static IntMap<Gamepad> gamepads = new IntMap<Gamepad>();
	private static IntMap<Gamepad> gamepadsTemp = new IntMap<Gamepad>();
	
	public static void init(GamepadSupportListener listener) {
		GamepadSupport.listener = listener;
		nativeInit();
	}
	
	public static void startPolling() {
        consoleLog("startPolling");
		ticker.start();
	}
	
	public static void stopPolling() {
		ticker.stop();
	}
	
	public static void pollGamepads() {
		JsArray<Gamepad> currentGamepads = nativePollGamepads();
		if (currentGamepads != null) {
			gamepadsTemp.clear();
			gamepadsTemp.putAll(gamepads);
			for (int i = 0, j = currentGamepads.length(); i < j; i++) {
				Gamepad gamepad = currentGamepads.get(i);
				if (gamepad != null) {
					if (!gamepadsTemp.containsKey(gamepad.getIndex())) {
						onGamepadConnect(gamepad);
					} else {
						gamepads.put(gamepad.getIndex(), gamepad);
					}
					gamepadsTemp.remove(gamepad.getIndex());
				}				
			}
			for (Gamepad gamepad : gamepadsTemp.values()) {
				onGamepadDisconnect(gamepad);
			}
		}
	}
	
	public static void pollGamepadsStatus() {
		for (Gamepad gamepad : gamepads.values()) {
			if (gamepad.getPreviousTimestamp() != gamepad.getTimestamp()) {
				fireGamepadUpdated(gamepad.getIndex());
			}
			gamepad.setPreviousTimestamp(gamepad.getTimestamp());
		}
	}
	
	public static Gamepad getGamepad(int index) {
		return gamepads.get(index);
	}

	private static void onGamepadConnect(Gamepad gamepad) {
		consoleLog("onGamepadConnect: " + gamepad.getId());
		gamepads.put(gamepad.getIndex(), gamepad);
		fireGamepadConnected(gamepad.getIndex());
	}

	private static void onGamepadDisconnect(Gamepad gamepad) {
		consoleLog("onGamepadDisconnect: " + gamepad.getId());
		gamepads.remove(gamepad.getIndex());
		fireGamepadDisconnected(gamepad.getIndex());
	}

	private static void fireGamepadConnected(int index) {
		if (listener != null) {
			listener.onGamepadConnected(index);
		}
	}
	
	private static void fireGamepadDisconnected(int index) {
		if (listener != null) {
			listener.onGamepadDisconnected(index);
		}
	}
	
	private static void fireGamepadUpdated(int index) {
		if (listener != null) {
			listener.onGamepadUpdated(index);
		}
	}
	
	private static void handleGamepadConnect(GamepadEvent event) {
		onGamepadConnect(event.getGamepad());
	}
	private static void handleGamepadDisconnect(GamepadEvent event) {
		onGamepadDisconnect(event.getGamepad());
	}
	
	private static native void nativeInit() /*-{
        var gamepadSupportAvailable = !! navigator.getGamepads;
        if (gamepadSupportAvailable) {
            $wnd.addEventListener('gamepadconnected', @com.badlogic.gdx.controllers.gwt.support.GamepadSupport::handleGamepadConnect(Lcom/badlogic/gdx/controllers/gwt/support/GamepadSupport$GamepadEvent;), false);
            $wnd.addEventListener('gamepaddisconnected', @com.badlogic.gdx.controllers.gwt.support.GamepadSupport::handleGamepadDisconnect(Lcom/badlogic/gdx/controllers/gwt/support/GamepadSupport$GamepadEvent;), false);
            if ( !! navigator.getGamepads) {
                @com.badlogic.gdx.controllers.gwt.support.GamepadSupport::startPolling()();
            }
        }
	}-*/;
	
	private static native JsArray<Gamepad> nativePollGamepads() /*-{
		return navigator.getGamepads();
	}-*/;
	
	public static native void consoleLog(String message) /*-{
		$wnd.console.log(message);
	}-*/;
	
	private static class Ticker implements AnimationScheduler.AnimationCallback {
		
		private boolean ticking = false;
		
		public void start() {
			if (!ticking) {
				ticking = true;
				AnimationScheduler.get().requestAnimationFrame(this);			
			}
		}
		
		public void stop() {
			ticking = false;
		}
		
		@Override
		public void execute(double timestamp) {
			if (ticking) {
				GamepadSupport.pollGamepads();
				GamepadSupport.pollGamepadsStatus();
				AnimationScheduler.get().requestAnimationFrame(this);
			}
		}
	}

	private static final class GamepadEvent extends JavaScriptObject {
		protected GamepadEvent() {
			// Required by GWT
		}
		
		public native Gamepad getGamepad() /*-{
			return this.gamepad;
		}-*/;
	}
}
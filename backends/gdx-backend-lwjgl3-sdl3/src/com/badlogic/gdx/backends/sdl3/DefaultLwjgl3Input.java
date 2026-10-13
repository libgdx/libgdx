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

package com.badlogic.gdx.backends.sdl3;

import com.badlogic.gdx.input.NativeInputConfiguration;

import com.badlogic.gdx.AbstractInput;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputEventQueue;
import com.badlogic.gdx.InputProcessor;
import org.lwjgl.sdl.SDLKeycode;
import org.lwjgl.sdl.SDLMouse;

import java.util.Arrays;

public class DefaultLwjgl3Input extends AbstractInput implements Lwjgl3Input {
	final Lwjgl3Window window;
	private InputProcessor inputProcessor;
	final InputEventQueue eventQueue = new InputEventQueue();

	int mouseX, mouseY;
	int mousePressed;
	int deltaX, deltaY;
	boolean justTouched;
	final boolean[] justPressedButtons = new boolean[5];
	final boolean[] pressedButtons = new boolean[5];
	char lastCharacter;

	@Override
	public void charCallback (long window, int codepoint) {
		if ((codepoint & 0xff00) == 0xf700) return;
		lastCharacter = (char)codepoint;
		DefaultLwjgl3Input.this.window.getGraphics().requestRendering();
		eventQueue.keyTyped((char)codepoint, System.nanoTime());
	}

	@Override
	public void scrollCallback (long window, double scrollX, double scrollY) {
		DefaultLwjgl3Input.this.window.getGraphics().requestRendering();
		eventQueue.scrolled(-(float)scrollX, -(float)scrollY, System.nanoTime());
	}

	private int cursorPosCallbackLogicalMouseY;
	private int cursorPosCallbackLogicalMouseX;

	@Override
	public void cursorPosCallback (long windowHandle, double x, double y) {
		deltaX = (int)x - cursorPosCallbackLogicalMouseX;
		deltaY = (int)y - cursorPosCallbackLogicalMouseY;
		mouseX = cursorPosCallbackLogicalMouseX = (int)x;
		mouseY = cursorPosCallbackLogicalMouseY = (int)y;

		if (window.getConfig().hdpiMode == HdpiMode.Pixels) {
			float xScale = window.getGraphics().getBackBufferWidth() / (float)window.getGraphics().getLogicalWidth();
			float yScale = window.getGraphics().getBackBufferHeight() / (float)window.getGraphics().getLogicalHeight();
			deltaX = (int)(deltaX * xScale);
			deltaY = (int)(deltaY * yScale);
			mouseX = (int)(mouseX * xScale);
			mouseY = (int)(mouseY * yScale);
		}

		DefaultLwjgl3Input.this.window.getGraphics().requestRendering();
		long time = System.nanoTime();
		if (mousePressed > 0) {
			eventQueue.touchDragged(mouseX, mouseY, 0, time);
		} else {
			eventQueue.mouseMoved(mouseX, mouseY, time);
		}
	}

	@Override
	public void mouseButtonCallback (long window, int button, boolean down) {
		int gdxButton = toGdxButton(button);
		if (button != -1 && gdxButton == -1) return;

		long time = System.nanoTime();
		if (down) {
			mousePressed++;
			justTouched = true;
			pressedButtons[gdxButton] = true;
			justPressedButtons[gdxButton] = true;
			DefaultLwjgl3Input.this.window.getGraphics().requestRendering();
			eventQueue.touchDown(mouseX, mouseY, 0, gdxButton, time);
		} else {
			pressedButtons[gdxButton] = false;
			mousePressed = Math.max(0, mousePressed - 1);
			DefaultLwjgl3Input.this.window.getGraphics().requestRendering();
			eventQueue.touchUp(mouseX, mouseY, 0, gdxButton, time);
		}
	}

	private int toGdxButton (int button) {
		if (button == SDLMouse.SDL_BUTTON_LEFT) return Buttons.LEFT;
		if (button == SDLMouse.SDL_BUTTON_RIGHT) return Buttons.RIGHT;
		if (button == SDLMouse.SDL_BUTTON_MIDDLE) return Buttons.MIDDLE;
		if (button == SDLMouse.SDL_BUTTON_X1) return Buttons.BACK;
		if (button == SDLMouse.SDL_BUTTON_X2) return Buttons.FORWARD;
		return -1;
	}

	public DefaultLwjgl3Input (Lwjgl3Window window) {
		this.window = window;
		windowHandleChanged(window.getWindowHandle());
	}

	@Override
	public void keyCallback (long window, int key, int scancode, int mods, boolean repeat, boolean down) {
		if (repeat) {
			if (lastCharacter != 0) {
				DefaultLwjgl3Input.this.window.getGraphics().requestRendering();
				eventQueue.keyTyped(lastCharacter, System.nanoTime());
			}
		} else if (down) {
			key = getGdxKeyCode(key);
			eventQueue.keyDown(key, System.nanoTime());
			pressedKeyCount++;
			keyJustPressed = true;
			pressedKeys[key] = true;
			justPressedKeys[key] = true;
			DefaultLwjgl3Input.this.window.getGraphics().requestRendering();
			lastCharacter = 0;
			char character = characterForKeyCode(key);
			if (character != 0) charCallback(window, character);
		} else {
			key = getGdxKeyCode(key);
			pressedKeyCount--;
			pressedKeys[key] = false;
			DefaultLwjgl3Input.this.window.getGraphics().requestRendering();
			eventQueue.keyUp(key, System.nanoTime());
		}
	}

	@Override
	public void resetPollingStates () {
		justTouched = false;
		keyJustPressed = false;
		Arrays.fill(justPressedKeys, false);
		Arrays.fill(justPressedButtons, false);
		eventQueue.drain(null);
	}

	@Override
	public void windowHandleChanged (long windowHandle) {
		resetPollingStates();
	}

	@Override
	public void update () {
		eventQueue.drain(inputProcessor);
	}

	@Override
	public void prepareNext () {
		if (justTouched) {
			justTouched = false;
			Arrays.fill(justPressedButtons, false);
		}

		if (keyJustPressed) {
			keyJustPressed = false;
			Arrays.fill(justPressedKeys, false);
		}
		deltaX = 0;
		deltaY = 0;
	}

	@Override
	public int getMaxPointers () {
		return 1;
	}

	@Override
	public int getX () {
		return mouseX;
	}

	@Override
	public int getX (int pointer) {
		return pointer == 0 ? mouseX : 0;
	}

	@Override
	public int getDeltaX () {
		return deltaX;
	}

	@Override
	public int getDeltaX (int pointer) {
		return pointer == 0 ? deltaX : 0;
	}

	@Override
	public int getY () {
		return mouseY;
	}

	@Override
	public int getY (int pointer) {
		return pointer == 0 ? mouseY : 0;
	}

	@Override
	public int getDeltaY () {
		return deltaY;
	}

	@Override
	public int getDeltaY (int pointer) {
		return pointer == 0 ? deltaY : 0;
	}

	@Override
	public boolean isTouched () {
		return pressedButtons[Buttons.LEFT] || pressedButtons[Buttons.RIGHT] || pressedButtons[Buttons.MIDDLE]
			|| pressedButtons[Buttons.BACK] || pressedButtons[Buttons.FORWARD];
	}

	@Override
	public boolean justTouched () {
		return justTouched;
	}

	@Override
	public boolean isTouched (int pointer) {
		return pointer == 0 ? isTouched() : false;
	}

	@Override
	public float getPressure () {
		return getPressure(0);
	}

	@Override
	public float getPressure (int pointer) {
		return isTouched(pointer) ? 1 : 0;
	}

	@Override
	public boolean isButtonPressed (int button) {
		if (button < 0 || button >= pressedButtons.length) {
			return false;
		}
		return pressedButtons[button];
	}

	@Override
	public boolean isButtonJustPressed (int button) {
		if (button < 0 || button >= justPressedButtons.length) {
			return false;
		}
		return justPressedButtons[button];
	}

	@Override
	public void getTextInput (TextInputListener listener, String title, String text, String hint) {
		getTextInput(listener, title, text, hint, OnscreenKeyboardType.Default);
	}

	@Override
	public void getTextInput (TextInputListener listener, String title, String text, String hint, OnscreenKeyboardType type) {
		// FIXME getTextInput does nothing
		listener.canceled();
	}

	@Override
	public long getCurrentEventTime () {
		// queue sets its event time for each event dequeued/processed
		return eventQueue.getCurrentEventTime();
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
	public void setCursorCatched (boolean catched) {
		if (!catched) {
			SDLMouse.SDL_ShowCursor();
		}
		SDLMouse.SDL_SetWindowRelativeMouseMode(window.getWindowHandle(), catched);
	}

	@Override
	public boolean isCursorCatched () {
		return SDLMouse.SDL_GetWindowRelativeMouseMode(window.getWindowHandle());
	}

	@Override
	public void setCursorPosition (int x, int y) {
		if (window.getConfig().hdpiMode == HdpiMode.Pixels) {
			float xScale = window.getGraphics().getLogicalWidth() / (float)window.getGraphics().getBackBufferWidth();
			float yScale = window.getGraphics().getLogicalHeight() / (float)window.getGraphics().getBackBufferHeight();
			x = (int)(x * xScale);
			y = (int)(y * yScale);
		}
		SDLMouse.SDL_WarpMouseInWindow(window.getWindowHandle(), x, y);
		cursorPosCallback(window.getWindowHandle(), x, y);
	}

	protected char characterForKeyCode (int key) {
		// Map certain key codes to character codes.
		switch (key) {
		case Keys.BACKSPACE:
			return 8;
		case Keys.TAB:
			return '\t';
		case Keys.FORWARD_DEL:
			return 127;
		case Keys.NUMPAD_ENTER:
		case Keys.ENTER:
			return '\n';
		}
		return 0;
	}

	public int getGdxKeyCode (int lwjglKeyCode) {
		switch (lwjglKeyCode) {
		case SDLKeycode.SDLK_SPACE:
			return Input.Keys.SPACE;
		case SDLKeycode.SDLK_APOSTROPHE:
			return Input.Keys.APOSTROPHE;
		case SDLKeycode.SDLK_COMMA:
			return Input.Keys.COMMA;
		case SDLKeycode.SDLK_MINUS:
			return Input.Keys.MINUS;
		case SDLKeycode.SDLK_PERIOD:
			return Input.Keys.PERIOD;
		case SDLKeycode.SDLK_SLASH:
			return Input.Keys.SLASH;
		case SDLKeycode.SDLK_0:
			return Input.Keys.NUM_0;
		case SDLKeycode.SDLK_1:
			return Input.Keys.NUM_1;
		case SDLKeycode.SDLK_2:
			return Input.Keys.NUM_2;
		case SDLKeycode.SDLK_3:
			return Input.Keys.NUM_3;
		case SDLKeycode.SDLK_4:
			return Input.Keys.NUM_4;
		case SDLKeycode.SDLK_5:
			return Input.Keys.NUM_5;
		case SDLKeycode.SDLK_6:
			return Input.Keys.NUM_6;
		case SDLKeycode.SDLK_7:
			return Input.Keys.NUM_7;
		case SDLKeycode.SDLK_8:
			return Input.Keys.NUM_8;
		case SDLKeycode.SDLK_9:
			return Input.Keys.NUM_9;
		case SDLKeycode.SDLK_SEMICOLON:
			return Input.Keys.SEMICOLON;
		case SDLKeycode.SDLK_EQUALS:
			return Input.Keys.EQUALS;
		case SDLKeycode.SDLK_A:
			return Input.Keys.A;
		case SDLKeycode.SDLK_B:
			return Input.Keys.B;
		case SDLKeycode.SDLK_C:
			return Input.Keys.C;
		case SDLKeycode.SDLK_D:
			return Input.Keys.D;
		case SDLKeycode.SDLK_E:
			return Input.Keys.E;
		case SDLKeycode.SDLK_F:
			return Input.Keys.F;
		case SDLKeycode.SDLK_G:
			return Input.Keys.G;
		case SDLKeycode.SDLK_H:
			return Input.Keys.H;
		case SDLKeycode.SDLK_I:
			return Input.Keys.I;
		case SDLKeycode.SDLK_J:
			return Input.Keys.J;
		case SDLKeycode.SDLK_K:
			return Input.Keys.K;
		case SDLKeycode.SDLK_L:
			return Input.Keys.L;
		case SDLKeycode.SDLK_M:
			return Input.Keys.M;
		case SDLKeycode.SDLK_N:
			return Input.Keys.N;
		case SDLKeycode.SDLK_O:
			return Input.Keys.O;
		case SDLKeycode.SDLK_P:
			return Input.Keys.P;
		case SDLKeycode.SDLK_Q:
			return Input.Keys.Q;
		case SDLKeycode.SDLK_R:
			return Input.Keys.R;
		case SDLKeycode.SDLK_S:
			return Input.Keys.S;
		case SDLKeycode.SDLK_T:
			return Input.Keys.T;
		case SDLKeycode.SDLK_U:
			return Input.Keys.U;
		case SDLKeycode.SDLK_V:
			return Input.Keys.V;
		case SDLKeycode.SDLK_W:
			return Input.Keys.W;
		case SDLKeycode.SDLK_X:
			return Input.Keys.X;
		case SDLKeycode.SDLK_Y:
			return Input.Keys.Y;
		case SDLKeycode.SDLK_Z:
			return Input.Keys.Z;
		case SDLKeycode.SDLK_LEFTBRACKET:
			return Input.Keys.LEFT_BRACKET;
		case SDLKeycode.SDLK_BACKSLASH:
			return Input.Keys.BACKSLASH;
		case SDLKeycode.SDLK_RIGHTBRACKET:
			return Input.Keys.RIGHT_BRACKET;
		case SDLKeycode.SDLK_GRAVE:
			return Input.Keys.GRAVE;
// case SDLKeycode.SDLK_WORLD_1: FIXME Scancode SDL_SCANCODE_NONUSBACKSLASH
// return Input.Keys.WORLD_1;
// case SDLKeycode.SDLK_WORLD_2:
// return Input.Keys.WORLD_2;
		case SDLKeycode.SDLK_ESCAPE:
			return Input.Keys.ESCAPE;
		case SDLKeycode.SDLK_RETURN:
			return Input.Keys.ENTER;
		case SDLKeycode.SDLK_TAB:
			return Input.Keys.TAB;
		case SDLKeycode.SDLK_BACKSPACE:
			return Input.Keys.BACKSPACE;
		case SDLKeycode.SDLK_INSERT:
			return Input.Keys.INSERT;
		case SDLKeycode.SDLK_DELETE:
			return Input.Keys.FORWARD_DEL;
		case SDLKeycode.SDLK_RIGHT:
			return Input.Keys.RIGHT;
		case SDLKeycode.SDLK_LEFT:
			return Input.Keys.LEFT;
		case SDLKeycode.SDLK_DOWN:
			return Input.Keys.DOWN;
		case SDLKeycode.SDLK_UP:
			return Input.Keys.UP;
		case SDLKeycode.SDLK_PAGEUP:
			return Input.Keys.PAGE_UP;
		case SDLKeycode.SDLK_PAGEDOWN:
			return Input.Keys.PAGE_DOWN;
		case SDLKeycode.SDLK_HOME:
			return Input.Keys.HOME;
		case SDLKeycode.SDLK_END:
			return Input.Keys.END;
		case SDLKeycode.SDLK_CAPSLOCK:
			return Keys.CAPS_LOCK;
		case SDLKeycode.SDLK_SCROLLLOCK:
			return Keys.SCROLL_LOCK;
		case SDLKeycode.SDLK_PRINTSCREEN:
			return Keys.PRINT_SCREEN;
		case SDLKeycode.SDLK_PAUSE:
			return Keys.PAUSE;
		case SDLKeycode.SDLK_F1:
			return Input.Keys.F1;
		case SDLKeycode.SDLK_F2:
			return Input.Keys.F2;
		case SDLKeycode.SDLK_F3:
			return Input.Keys.F3;
		case SDLKeycode.SDLK_F4:
			return Input.Keys.F4;
		case SDLKeycode.SDLK_F5:
			return Input.Keys.F5;
		case SDLKeycode.SDLK_F6:
			return Input.Keys.F6;
		case SDLKeycode.SDLK_F7:
			return Input.Keys.F7;
		case SDLKeycode.SDLK_F8:
			return Input.Keys.F8;
		case SDLKeycode.SDLK_F9:
			return Input.Keys.F9;
		case SDLKeycode.SDLK_F10:
			return Input.Keys.F10;
		case SDLKeycode.SDLK_F11:
			return Input.Keys.F11;
		case SDLKeycode.SDLK_F12:
			return Input.Keys.F12;
		case SDLKeycode.SDLK_F13:
			return Input.Keys.F13;
		case SDLKeycode.SDLK_F14:
			return Input.Keys.F14;
		case SDLKeycode.SDLK_F15:
			return Input.Keys.F15;
		case SDLKeycode.SDLK_F16:
			return Input.Keys.F16;
		case SDLKeycode.SDLK_F17:
			return Input.Keys.F17;
		case SDLKeycode.SDLK_F18:
			return Input.Keys.F18;
		case SDLKeycode.SDLK_F19:
			return Input.Keys.F19;
		case SDLKeycode.SDLK_F20:
			return Input.Keys.F20;
		case SDLKeycode.SDLK_F21:
			return Input.Keys.F21;
		case SDLKeycode.SDLK_F22:
			return Input.Keys.F22;
		case SDLKeycode.SDLK_F23:
			return Input.Keys.F23;
		case SDLKeycode.SDLK_F24:
			return Input.Keys.F24;
		case SDLKeycode.SDLK_NUMLOCKCLEAR:
			return Keys.NUM_LOCK;
		case SDLKeycode.SDLK_KP_0:
			return Input.Keys.NUMPAD_0;
		case SDLKeycode.SDLK_KP_1:
			return Input.Keys.NUMPAD_1;
		case SDLKeycode.SDLK_KP_2:
			return Input.Keys.NUMPAD_2;
		case SDLKeycode.SDLK_KP_3:
			return Input.Keys.NUMPAD_3;
		case SDLKeycode.SDLK_KP_4:
			return Input.Keys.NUMPAD_4;
		case SDLKeycode.SDLK_KP_5:
			return Input.Keys.NUMPAD_5;
		case SDLKeycode.SDLK_KP_6:
			return Input.Keys.NUMPAD_6;
		case SDLKeycode.SDLK_KP_7:
			return Input.Keys.NUMPAD_7;
		case SDLKeycode.SDLK_KP_8:
			return Input.Keys.NUMPAD_8;
		case SDLKeycode.SDLK_KP_9:
			return Input.Keys.NUMPAD_9;
		case SDLKeycode.SDLK_KP_DECIMAL:
			return Keys.NUMPAD_DOT;
		case SDLKeycode.SDLK_KP_DIVIDE:
			return Keys.NUMPAD_DIVIDE;
		case SDLKeycode.SDLK_KP_MULTIPLY:
			return Keys.NUMPAD_MULTIPLY;
		case SDLKeycode.SDLK_KP_MINUS:
			return Keys.NUMPAD_SUBTRACT;
		case SDLKeycode.SDLK_KP_PLUS:
			return Keys.NUMPAD_ADD;
		case SDLKeycode.SDLK_KP_ENTER:
			return Keys.NUMPAD_ENTER;
		case SDLKeycode.SDLK_KP_EQUALS:
			return Keys.NUMPAD_EQUALS;
		case SDLKeycode.SDLK_LSHIFT:
			return Input.Keys.SHIFT_LEFT;
		case SDLKeycode.SDLK_LCTRL:
			return Input.Keys.CONTROL_LEFT;
		case SDLKeycode.SDLK_LALT:
			return Input.Keys.ALT_LEFT;
		case SDLKeycode.SDLK_LGUI:
			return Input.Keys.SYM;
		case SDLKeycode.SDLK_RSHIFT:
			return Input.Keys.SHIFT_RIGHT;
		case SDLKeycode.SDLK_RCTRL:
			return Input.Keys.CONTROL_RIGHT;
		case SDLKeycode.SDLK_RALT:
			return Input.Keys.ALT_RIGHT;
		case SDLKeycode.SDLK_RGUI:
			return Input.Keys.SYM;
		case SDLKeycode.SDLK_MENU:
			return Input.Keys.MENU;
		default:
			return Input.Keys.UNKNOWN;
		}
	}

	@Override
	public void dispose () {
	}

	// --------------------------------------------------------------------------
	// -------------------------- Nothing to see below this line except for stubs
	// --------------------------------------------------------------------------

	@Override
	public float getAccelerometerX () {
		return 0;
	}

	@Override
	public float getAccelerometerY () {
		return 0;
	}

	@Override
	public float getAccelerometerZ () {
		return 0;
	}

	@Override
	public boolean isPeripheralAvailable (Peripheral peripheral) {
		return peripheral == Peripheral.HardwareKeyboard;
	}

	@Override
	public int getRotation () {
		return 0;
	}

	@Override
	public Orientation getNativeOrientation () {
		return Orientation.Landscape;
	}

	@Override
	public void setOnscreenKeyboardVisible (boolean visible) {
	}

	@Override
	public void setOnscreenKeyboardVisible (boolean visible, OnscreenKeyboardType type) {
	}

	@Override
	public void openTextInputField (NativeInputConfiguration configuration) {

	}

	@Override
	public void closeTextInputField (boolean sendReturn) {

	}

	@Override
	public void setKeyboardHeightObserver (KeyboardHeightObserver observer) {

	}

	@Override
	public void vibrate (int milliseconds) {
	}

	@Override
	public void vibrate (int milliseconds, boolean fallback) {
	}

	@Override
	public void vibrate (int milliseconds, int amplitude, boolean fallback) {
	}

	@Override
	public void vibrate (VibrationType vibrationType) {
	}

	@Override
	public float getAzimuth () {
		return 0;
	}

	@Override
	public float getPitch () {
		return 0;
	}

	@Override
	public float getRoll () {
		return 0;
	}

	@Override
	public void getRotationMatrix (float[] matrix) {
	}

	@Override
	public float getGyroscopeX () {
		return 0;
	}

	@Override
	public float getGyroscopeY () {
		return 0;
	}

	@Override
	public float getGyroscopeZ () {
		return 0;
	}
}

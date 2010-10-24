/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.desktop;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;

/**
 * An implementation of the {@link Input} interface hooking a Jogl panel for input.
 * 
 * @author mzechner
 * 
 */
final class LwjglInput implements Input, RenderListener {
	String text;
	TextInputListener textListener;
	@SuppressWarnings("unchecked") private final ArrayList<InputListener> listeners = new ArrayList();

	public void addInputListener (InputListener listener) {
		listeners.add(listener);
	}

	public float getAccelerometerX () {
		return 0;
	}

	public float getAccelerometerY () {
		return 0;
	}

	public float getAccelerometerZ () {
		return 0;
	}

	public void getTextInput (final TextInputListener listener, final String title, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run () {
				LwjglInput.this.text = JOptionPane.showInputDialog(null, title, text);
				if (LwjglInput.this.text != null) textListener = listener;
			}
		});
	}

	public int getX () {
		return Mouse.getX();
	}

	public int getY () {
		return Gdx.graphics.getHeight() - Mouse.getY();
	}

	public boolean isAccelerometerAvailable () {
		return false;
	}

	public boolean isKeyPressed (int key) {
		return Keyboard.isKeyDown(getKeyCodeReverse(key));
	}

	public boolean isTouched () {
		boolean button = Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2);
		if (button) System.out.println("button!!");
		return button;
	}

	public void removeInputListener (InputListener listener) {
		listeners.remove(listener);
	}

	public void dispose () {
		listeners.clear();
	}

	public void render () {
		if (textListener != null) {
			textListener.input(text);
			textListener = null;
		}
	}

	public void surfaceCreated () {
		// TODO Auto-generated method stub

	}

	public void surfaceChanged (int width, int height) {
		// TODO Auto-generated method stub

	}

	public int getX (int pointer) {
		if (pointer > 0)
			return 0;
		else
			return getX();
	}

	public int getY (int pointer) {
		if (pointer > 0)
			return 0;
		else
			return getY();
	}

	public boolean isTouched (int pointer) {
		if (pointer > 0)
			return false;
		else
			return isTouched();
	}

	public boolean supportsMultitouch () {
		return false;
	}

	public void fireKeyDown (int keycode) {
		for (int i = 0; i < listeners.size(); i++)
			if (listeners.get(i).keyDown(keycode)) return;
	}

	public void fireKeyUp (int keycode) {
		for (int i = 0; i < listeners.size(); i++)
			if (listeners.get(i).keyUp(keycode)) return;
	}

	public void fireKeyTyped (char character) {
		for (int i = 0; i < listeners.size(); i++)
			if (listeners.get(i).keyTyped(character)) return;
	}

	public void fireTouchDown (int x, int y, int pointer) {
		for (int i = 0; i < listeners.size(); i++)
			if (listeners.get(i).touchDown(x, y, pointer)) return;
	}

	public void fireTouchUp (int x, int y, int pointer) {
		for (int i = 0; i < listeners.size(); i++)
			if (listeners.get(i).touchUp(x, y, pointer)) return;
	}

	public void fireTouchDragged (int x, int y, int pointer) {
		for (int i = 0; i < listeners.size(); i++)
			if (listeners.get(i).touchDragged(x, y, pointer)) return;
	}

	public static int getKeyCode (int keyCode) {
		if (keyCode == Keyboard.KEY_0) return Input.Keys.KEYCODE_0;
		if (keyCode == Keyboard.KEY_1) return Input.Keys.KEYCODE_1;
		if (keyCode == Keyboard.KEY_2) return Input.Keys.KEYCODE_2;
		if (keyCode == Keyboard.KEY_3) return Input.Keys.KEYCODE_3;
		if (keyCode == Keyboard.KEY_4) return Input.Keys.KEYCODE_4;
		if (keyCode == Keyboard.KEY_5) return Input.Keys.KEYCODE_5;
		if (keyCode == Keyboard.KEY_6) return Input.Keys.KEYCODE_6;
		if (keyCode == Keyboard.KEY_7) return Input.Keys.KEYCODE_7;
		if (keyCode == Keyboard.KEY_8) return Input.Keys.KEYCODE_8;
		if (keyCode == Keyboard.KEY_9) return Input.Keys.KEYCODE_9;
		if (keyCode == Keyboard.KEY_A) return Input.Keys.KEYCODE_A;
		if (keyCode == Keyboard.KEY_B) return Input.Keys.KEYCODE_B;
		if (keyCode == Keyboard.KEY_C) return Input.Keys.KEYCODE_C;
		if (keyCode == Keyboard.KEY_D) return Input.Keys.KEYCODE_D;
		if (keyCode == Keyboard.KEY_E) return Input.Keys.KEYCODE_E;
		if (keyCode == Keyboard.KEY_F) return Input.Keys.KEYCODE_F;
		if (keyCode == Keyboard.KEY_G) return Input.Keys.KEYCODE_G;
		if (keyCode == Keyboard.KEY_H) return Input.Keys.KEYCODE_H;
		if (keyCode == Keyboard.KEY_I) return Input.Keys.KEYCODE_I;
		if (keyCode == Keyboard.KEY_J) return Input.Keys.KEYCODE_J;
		if (keyCode == Keyboard.KEY_K) return Input.Keys.KEYCODE_K;
		if (keyCode == Keyboard.KEY_L) return Input.Keys.KEYCODE_L;
		if (keyCode == Keyboard.KEY_M) return Input.Keys.KEYCODE_M;
		if (keyCode == Keyboard.KEY_N) return Input.Keys.KEYCODE_N;
		if (keyCode == Keyboard.KEY_O) return Input.Keys.KEYCODE_O;
		if (keyCode == Keyboard.KEY_P) return Input.Keys.KEYCODE_P;
		if (keyCode == Keyboard.KEY_Q) return Input.Keys.KEYCODE_Q;
		if (keyCode == Keyboard.KEY_R) return Input.Keys.KEYCODE_R;
		if (keyCode == Keyboard.KEY_S) return Input.Keys.KEYCODE_S;
		if (keyCode == Keyboard.KEY_T) return Input.Keys.KEYCODE_T;
		if (keyCode == Keyboard.KEY_U) return Input.Keys.KEYCODE_U;
		if (keyCode == Keyboard.KEY_V) return Input.Keys.KEYCODE_V;
		if (keyCode == Keyboard.KEY_W) return Input.Keys.KEYCODE_W;
		if (keyCode == Keyboard.KEY_X) return Input.Keys.KEYCODE_X;
		if (keyCode == Keyboard.KEY_Y) return Input.Keys.KEYCODE_Y;
		if (keyCode == Keyboard.KEY_Z) return Input.Keys.KEYCODE_Z;
		if (keyCode == Keyboard.KEY_LMETA) return Input.Keys.KEYCODE_ALT_LEFT;
		if (keyCode == Keyboard.KEY_RMETA) return Input.Keys.KEYCODE_ALT_RIGHT;
		if (keyCode == Keyboard.KEY_BACKSLASH) return Input.Keys.KEYCODE_BACKSLASH;
		if (keyCode == Keyboard.KEY_COMMA) return Input.Keys.KEYCODE_COMMA;
		if (keyCode == Keyboard.KEY_DELETE) return Input.Keys.KEYCODE_DEL;
		if (keyCode == Keyboard.KEY_LEFT) return Input.Keys.KEYCODE_DPAD_LEFT;
		if (keyCode == Keyboard.KEY_RIGHT) return Input.Keys.KEYCODE_DPAD_RIGHT;
		if (keyCode == Keyboard.KEY_UP) return Input.Keys.KEYCODE_DPAD_UP;
		if (keyCode == Keyboard.KEY_DOWN) return Input.Keys.KEYCODE_DPAD_DOWN;
		if (keyCode == Keyboard.KEY_RETURN) return Input.Keys.KEYCODE_ENTER;
		if (keyCode == Keyboard.KEY_HOME) return Input.Keys.KEYCODE_HOME;
		if (keyCode == Keyboard.KEY_MINUS) return Input.Keys.KEYCODE_MINUS;
		if (keyCode == Keyboard.KEY_PERIOD) return Input.Keys.KEYCODE_PERIOD;
		if (keyCode == Keyboard.KEY_ADD) return Input.Keys.KEYCODE_PLUS;
		if (keyCode == Keyboard.KEY_SEMICOLON) return Input.Keys.KEYCODE_SEMICOLON;
		if (keyCode == Keyboard.KEY_LSHIFT) return Input.Keys.KEYCODE_SHIFT_LEFT;
		if (keyCode == Keyboard.KEY_RSHIFT) return Input.Keys.KEYCODE_SHIFT_RIGHT;
		if (keyCode == Keyboard.KEY_SLASH) return Input.Keys.KEYCODE_SLASH;
		if (keyCode == Keyboard.KEY_SPACE) return Input.Keys.KEYCODE_SPACE;
		if (keyCode == Keyboard.KEY_TAB) return Input.Keys.KEYCODE_TAB;
		return Input.Keys.KEYCODE_UNKNOWN;
	}

	public static int getKeyCodeReverse (int keyCode) {
		if (keyCode == Input.Keys.KEYCODE_0) return Keyboard.KEY_0;
		if (keyCode == Input.Keys.KEYCODE_1) return Keyboard.KEY_1;
		if (keyCode == Input.Keys.KEYCODE_2) return Keyboard.KEY_2;
		if (keyCode == Input.Keys.KEYCODE_3) return Keyboard.KEY_3;
		if (keyCode == Input.Keys.KEYCODE_4) return Keyboard.KEY_4;
		if (keyCode == Input.Keys.KEYCODE_5) return Keyboard.KEY_5;
		if (keyCode == Input.Keys.KEYCODE_6) return Keyboard.KEY_6;
		if (keyCode == Input.Keys.KEYCODE_7) return Keyboard.KEY_7;
		if (keyCode == Input.Keys.KEYCODE_8) return Keyboard.KEY_8;
		if (keyCode == Input.Keys.KEYCODE_9) return Keyboard.KEY_9;
		if (keyCode == Input.Keys.KEYCODE_A) return Keyboard.KEY_A;
		if (keyCode == Input.Keys.KEYCODE_B) return Keyboard.KEY_B;
		if (keyCode == Input.Keys.KEYCODE_C) return Keyboard.KEY_C;
		if (keyCode == Input.Keys.KEYCODE_D) return Keyboard.KEY_D;
		if (keyCode == Input.Keys.KEYCODE_E) return Keyboard.KEY_E;
		if (keyCode == Input.Keys.KEYCODE_F) return Keyboard.KEY_F;
		if (keyCode == Input.Keys.KEYCODE_G) return Keyboard.KEY_G;
		if (keyCode == Input.Keys.KEYCODE_H) return Keyboard.KEY_H;
		if (keyCode == Input.Keys.KEYCODE_I) return Keyboard.KEY_I;
		if (keyCode == Input.Keys.KEYCODE_J) return Keyboard.KEY_J;
		if (keyCode == Input.Keys.KEYCODE_K) return Keyboard.KEY_K;
		if (keyCode == Input.Keys.KEYCODE_L) return Keyboard.KEY_L;
		if (keyCode == Input.Keys.KEYCODE_M) return Keyboard.KEY_M;
		if (keyCode == Input.Keys.KEYCODE_N) return Keyboard.KEY_N;
		if (keyCode == Input.Keys.KEYCODE_O) return Keyboard.KEY_O;
		if (keyCode == Input.Keys.KEYCODE_P) return Keyboard.KEY_P;
		if (keyCode == Input.Keys.KEYCODE_Q) return Keyboard.KEY_Q;
		if (keyCode == Input.Keys.KEYCODE_R) return Keyboard.KEY_R;
		if (keyCode == Input.Keys.KEYCODE_S) return Keyboard.KEY_S;
		if (keyCode == Input.Keys.KEYCODE_T) return Keyboard.KEY_T;
		if (keyCode == Input.Keys.KEYCODE_U) return Keyboard.KEY_U;
		if (keyCode == Input.Keys.KEYCODE_V) return Keyboard.KEY_V;
		if (keyCode == Input.Keys.KEYCODE_W) return Keyboard.KEY_W;
		if (keyCode == Input.Keys.KEYCODE_X) return Keyboard.KEY_X;
		if (keyCode == Input.Keys.KEYCODE_Y) return Keyboard.KEY_Y;
		if (keyCode == Input.Keys.KEYCODE_Z) return Keyboard.KEY_Z;
		if (keyCode == Input.Keys.KEYCODE_ALT_LEFT) return Keyboard.KEY_LMETA;
		if (keyCode == Input.Keys.KEYCODE_ALT_RIGHT) return Keyboard.KEY_RMETA;
		if (keyCode == Input.Keys.KEYCODE_BACKSLASH) return Keyboard.KEY_BACKSLASH;
		if (keyCode == Input.Keys.KEYCODE_COMMA) return Keyboard.KEY_COMMA;
		if (keyCode == Input.Keys.KEYCODE_DEL) return Keyboard.KEY_DELETE;
		if (keyCode == Input.Keys.KEYCODE_DPAD_LEFT) return Keyboard.KEY_LEFT;
		if (keyCode == Input.Keys.KEYCODE_DPAD_RIGHT) return Keyboard.KEY_RIGHT;
		if (keyCode == Input.Keys.KEYCODE_DPAD_UP) return Keyboard.KEY_UP;
		if (keyCode == Input.Keys.KEYCODE_DPAD_DOWN) return Keyboard.KEY_DOWN;
		if (keyCode == Input.Keys.KEYCODE_ENTER) return Keyboard.KEY_RETURN;
		if (keyCode == Input.Keys.KEYCODE_HOME) return Keyboard.KEY_HOME;
		if (keyCode == Input.Keys.KEYCODE_MINUS) return Keyboard.KEY_MINUS;
		if (keyCode == Input.Keys.KEYCODE_PERIOD) return Keyboard.KEY_PERIOD;
		if (keyCode == Input.Keys.KEYCODE_PLUS) return Keyboard.KEY_ADD;
		if (keyCode == Input.Keys.KEYCODE_SEMICOLON) return Keyboard.KEY_SEMICOLON;
		if (keyCode == Input.Keys.KEYCODE_SHIFT_LEFT) return Keyboard.KEY_LSHIFT;
		if (keyCode == Input.Keys.KEYCODE_SHIFT_RIGHT) return Keyboard.KEY_RSHIFT;
		if (keyCode == Input.Keys.KEYCODE_SLASH) return Keyboard.KEY_SLASH;
		if (keyCode == Input.Keys.KEYCODE_SPACE) return Keyboard.KEY_SPACE;
		if (keyCode == Input.Keys.KEYCODE_TAB) return Keyboard.KEY_TAB;
		return Keyboard.KEY_NONE;
	}
}

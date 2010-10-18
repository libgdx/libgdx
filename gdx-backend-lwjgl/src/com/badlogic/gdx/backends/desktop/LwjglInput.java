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

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

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
	private final ArrayList<InputListener> listeners = new ArrayList();

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
		return Mouse.getY();
	}

	public boolean isAccelerometerAvailable () {
		return false;
	}

	public boolean isKeyPressed (int key) {
		return Keyboard.isKeyDown(key);
	}

	public boolean isTouched () {
		return Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2);
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

	static public int translateKeyCode (int keyCode) {
		if (keyCode == KeyEvent.VK_0) return Input.Keys.KEYCODE_0;
		if (keyCode == KeyEvent.VK_1) return Input.Keys.KEYCODE_1;
		if (keyCode == KeyEvent.VK_2) return Input.Keys.KEYCODE_2;
		if (keyCode == KeyEvent.VK_3) return Input.Keys.KEYCODE_3;
		if (keyCode == KeyEvent.VK_4) return Input.Keys.KEYCODE_4;
		if (keyCode == KeyEvent.VK_5) return Input.Keys.KEYCODE_5;
		if (keyCode == KeyEvent.VK_6) return Input.Keys.KEYCODE_6;
		if (keyCode == KeyEvent.VK_7) return Input.Keys.KEYCODE_7;
		if (keyCode == KeyEvent.VK_8) return Input.Keys.KEYCODE_8;
		if (keyCode == KeyEvent.VK_9) return Input.Keys.KEYCODE_9;
		if (keyCode == KeyEvent.VK_A) return Input.Keys.KEYCODE_A;
		if (keyCode == KeyEvent.VK_B) return Input.Keys.KEYCODE_B;
		if (keyCode == KeyEvent.VK_C) return Input.Keys.KEYCODE_C;
		if (keyCode == KeyEvent.VK_D) return Input.Keys.KEYCODE_D;
		if (keyCode == KeyEvent.VK_E) return Input.Keys.KEYCODE_E;
		if (keyCode == KeyEvent.VK_F) return Input.Keys.KEYCODE_F;
		if (keyCode == KeyEvent.VK_G) return Input.Keys.KEYCODE_G;
		if (keyCode == KeyEvent.VK_H) return Input.Keys.KEYCODE_H;
		if (keyCode == KeyEvent.VK_I) return Input.Keys.KEYCODE_I;
		if (keyCode == KeyEvent.VK_J) return Input.Keys.KEYCODE_J;
		if (keyCode == KeyEvent.VK_K) return Input.Keys.KEYCODE_K;
		if (keyCode == KeyEvent.VK_L) return Input.Keys.KEYCODE_L;
		if (keyCode == KeyEvent.VK_M) return Input.Keys.KEYCODE_M;
		if (keyCode == KeyEvent.VK_N) return Input.Keys.KEYCODE_N;
		if (keyCode == KeyEvent.VK_O) return Input.Keys.KEYCODE_O;
		if (keyCode == KeyEvent.VK_P) return Input.Keys.KEYCODE_P;
		if (keyCode == KeyEvent.VK_Q) return Input.Keys.KEYCODE_Q;
		if (keyCode == KeyEvent.VK_R) return Input.Keys.KEYCODE_R;
		if (keyCode == KeyEvent.VK_S) return Input.Keys.KEYCODE_S;
		if (keyCode == KeyEvent.VK_T) return Input.Keys.KEYCODE_T;
		if (keyCode == KeyEvent.VK_U) return Input.Keys.KEYCODE_U;
		if (keyCode == KeyEvent.VK_V) return Input.Keys.KEYCODE_V;
		if (keyCode == KeyEvent.VK_W) return Input.Keys.KEYCODE_W;
		if (keyCode == KeyEvent.VK_X) return Input.Keys.KEYCODE_X;
		if (keyCode == KeyEvent.VK_Y) return Input.Keys.KEYCODE_Y;
		if (keyCode == KeyEvent.VK_Z) return Input.Keys.KEYCODE_Z;
		if (keyCode == KeyEvent.VK_ALT) return Input.Keys.KEYCODE_ALT_LEFT;
		if (keyCode == KeyEvent.VK_ALT_GRAPH) return Input.Keys.KEYCODE_ALT_RIGHT;
		if (keyCode == KeyEvent.VK_BACK_SLASH) return Input.Keys.KEYCODE_BACKSLASH;
		if (keyCode == KeyEvent.VK_COMMA) return Input.Keys.KEYCODE_COMMA;
		if (keyCode == KeyEvent.VK_DELETE) return Input.Keys.KEYCODE_DEL;
		if (keyCode == KeyEvent.VK_LEFT) return Input.Keys.KEYCODE_DPAD_LEFT;
		if (keyCode == KeyEvent.VK_RIGHT) return Input.Keys.KEYCODE_DPAD_RIGHT;
		if (keyCode == KeyEvent.VK_UP) return Input.Keys.KEYCODE_DPAD_UP;
		if (keyCode == KeyEvent.VK_DOWN) return Input.Keys.KEYCODE_DPAD_DOWN;
		if (keyCode == KeyEvent.VK_ENTER) return Input.Keys.KEYCODE_ENTER;
		if (keyCode == KeyEvent.VK_HOME) return Input.Keys.KEYCODE_HOME;
		if (keyCode == KeyEvent.VK_MINUS) return Input.Keys.KEYCODE_MINUS;
		if (keyCode == KeyEvent.VK_PERIOD) return Input.Keys.KEYCODE_PERIOD;
		if (keyCode == KeyEvent.VK_PLUS) return Input.Keys.KEYCODE_PLUS;
		if (keyCode == KeyEvent.VK_SEMICOLON) return Input.Keys.KEYCODE_SEMICOLON;
		if (keyCode == KeyEvent.VK_SHIFT) return Input.Keys.KEYCODE_SHIFT_LEFT;
		if (keyCode == KeyEvent.VK_SLASH) return Input.Keys.KEYCODE_SLASH;
		if (keyCode == KeyEvent.VK_SPACE) return Input.Keys.KEYCODE_SPACE;
		if (keyCode == KeyEvent.VK_TAB) return Input.Keys.KEYCODE_TAB;
		return Input.Keys.KEYCODE_UNKNOWN;
	}
}

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

import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;

/**
 * An implementation of the {@link Input} interface hooking a Jogl panel for input.
 * 
 * @author mzechner
 * 
 */
final class JoglInput implements Input, RenderListener {
	/** the multiplexer **/
	private final JoglInputMultiplexer multiplexer;

	/** the graphics panel **/
	private final JoglPanel panel;

	/** user input **/
	private String text;

	/** user input listener **/
	private TextInputListener textListener;

	JoglInput (JoglPanel panel) {
		multiplexer = new JoglInputMultiplexer(panel.getCanvas());
		this.panel = panel;
		this.panel.addGraphicListener(this);
	}

	@Override public void addInputListener (InputListener listener) {
		multiplexer.addListener(listener);
	}

	@Override public float getAccelerometerX () {
		return 0;
	}

	@Override public float getAccelerometerY () {
		return 0;
	}

	@Override public float getAccelerometerZ () {
		return 0;
	}

	@Override public void getTextInput (final TextInputListener listener, final String title, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run () {
				JoglInput.this.text = JOptionPane.showInputDialog(null, title, text);
				if (JoglInput.this.text != null) textListener = listener;
			}
		});
	}

	@Override public int getX () {
		return panel.getMouseX();
	}

	@Override public int getY () {
		return panel.getMouseY();
	}

	@Override public boolean isAccelerometerAvailable () {
		return false;
	}

	@Override public boolean isKeyPressed (int key) {
		return panel.isKeyDown(key);
	}

	@Override public boolean isTouched () {
		return panel.isButtonDown(MouseEvent.BUTTON1) || panel.isButtonDown(MouseEvent.BUTTON2)
			|| panel.isButtonDown(MouseEvent.BUTTON3);
	}

	@Override public void removeInputListener (InputListener listener) {
		multiplexer.removeListener(listener);
	}

	@Override public void dispose () {
		// TODO Auto-generated method stub

	}

	@Override public void render () {
		multiplexer.processEvents();

		if (textListener != null) {
			textListener.input(text);
			textListener = null;
		}
	}

	@Override public void surfaceCreated () {
		// TODO Auto-generated method stub

	}

	@Override public void surfaceChanged (int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override public int getX (int pointer) {
		if (pointer > 0)
			return 0;
		else
			return getX();
	}

	@Override public int getY (int pointer) {
		if (pointer > 0)
			return 0;
		else
			return getY();
	}

	@Override public boolean isTouched (int pointer) {
		if (pointer > 0)
			return false;
		else
			return isTouched();
	}

	@Override public boolean supportsMultitouch () {
		return false;
	}

	@Override public void setOnscreenKeyboardVisible (boolean visible) {
		
	}

	@Override public boolean supportsOnscreenKeyboard () {
		return false;
	}
}

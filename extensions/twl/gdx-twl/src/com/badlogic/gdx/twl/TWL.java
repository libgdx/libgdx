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
package com.badlogic.gdx.twl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.twl.renderer.GdxRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.theme.ThemeManager;

/**
 * Convenience class for using TWL. This provides all the basics sufficient for most UIs. TWL can be used without this class if
 * more complex configurations are required (eg, multiple GUI instances).<br>
 * <br>
 * This class provides a single {@link GUI} instance with a root pane set to a widget that takes up the whole screen.
 * {@link #setWidget(Widget)} puts a widget into the root pane, making it easy to layout your widgets using the whole screen.<br>
 * <br>
 * This class is relatively heavyweight because it loads a TWL theme. Generally only one instance should be created for an entire
 * application. Use {@link #setWidget(Widget)} and {@link #clear()} to change the widgets displayed on various application
 * screens.<br>
 * <br>
 * This class implements {@link InputProcessor} and the input methods return true if TWL handled an event. Generally you will want
 * to use {@link InputMultiplexer} to avoid dispatching events that TWL handled to your application.<br>
 * <br>
 * If an instance of this call will no longer be used, {@link #dispose()} must be called to release resources.
 * @author Nathan Sweet
 */
public class TWL implements InputProcessor {
	private final GdxRenderer renderer;
	private final GUI gui;

	private boolean mouseDown, ignoreMouse, lastPressConsumed;
	public Widget root;

	/**
	 * Creates a new TWL instance with the specified theme file. The specified widget is added to the root pane.
	 */
	public TWL (SpriteBatch batch, String themeFile, FileType fileType, Widget widget) {
		this(batch, themeFile, fileType);
		setWidget(widget);
	}

	/**
	 * Creates a new TWL instance with the specified theme file.
	 */
	public TWL (SpriteBatch batch, String themeFile, FileType fileType) {
		renderer = new GdxRenderer(batch);

		root = new Widget() {
			protected void layout () {
				int width = getInnerWidth();
				int height = getInnerHeight();
				for (int i = 0, n = getNumChildren(); i < n; i++)
					getChild(i).setSize(width, height);
			}
		};
		root.setTheme("");

		gui = new GUI(root, renderer, null);
		try {
			gui.applyTheme(ThemeManager.createThemeManager(getThemeURL(themeFile, fileType), renderer));
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error loading theme: " + themeFile + " (" + fileType + ")", ex);
		}
	}

	/**
	 * Returns the GUI instance, which is the root of the TWL UI hierachy and manages timing, inputs, etc.
	 */
	public GUI getGUI () {
		return gui;
	}

	/**
	 * Sets the widget in the GUI's root pane. By default the root pane takes up the whole screen.
	 * @param widget If null, this method is equivalent to {@link #clear()}.
	 */
	public void setWidget (Widget widget) {
		Widget root = gui.getRootPane();
		root.removeAllChildren();
		if (widget != null) root.add(widget);
	}

	/**
	 * Removes all widgets from the GUI's root pane. This effectively means that no TWL UI will be drawn.
	 */
	public void clear () {
		gui.getRootPane().removeAllChildren();
	}

	/**
	 * Draws the TWL UI.
	 */
	public void render () {
		GUI gui = this.gui;
		int viewWidth = Gdx.graphics.getWidth();
		int viewHeight = Gdx.graphics.getHeight();
		if (renderer.getWidth() != viewWidth || renderer.getHeight() != viewHeight) {
			renderer.setSize(viewWidth, viewHeight);
			gui.setSize(viewWidth, viewHeight);
		}
		gui.updateTime();
		gui.handleKeyRepeat();
		gui.handleTooltips();
		gui.updateTimers();
		gui.invokeRunables();
		gui.validateLayout();
		gui.draw();
	}

	public boolean keyDown (int keycode) {
		keycode = getTwlKeyCode(keycode);
		return gui.handleKey(keycode, (char)0, true);
	}

	public boolean keyUp (int keycode) {
		keycode = getTwlKeyCode(keycode);
		return gui.handleKey(keycode, (char)0, false);
	}

	public boolean keyTyped (char character) {
		return gui.handleKey(0, character, true);
	}

	public boolean touchDown (int x, int y, int pointer, int button) {
		if (!mouseDown) lastPressConsumed = false; // Only the first button down counts.
		mouseDown = true;
		if (ignoreMouse) return false;
		boolean handled = gui.handleMouse(x, y, button, true);
		if (handled) lastPressConsumed = true;
		return handled;
	}

	public boolean touchUp (int x, int y, int pointer, int button) {
		mouseDown = false;
		if (ignoreMouse) {
			ignoreMouse = false;
			return false;
		}
		boolean handled = gui.handleMouse(x, y, button, false);
		if (Gdx.app.getType() == ApplicationType.Android) {
			// Move mouse away since there is no mouse cursor on android.
			gui.handleMouse(-9999, -9999, -1, false);
		}
		return handled;
	}

	public boolean touchDragged (int x, int y, int pointer) {
		if (mouseDown && !lastPressConsumed) {
			ignoreMouse = true;
			gui.clearMouseState();
			return false;
		}
		return gui.handleMouse(x, y, -1, true);
	}

	public boolean touchMoved (int x, int y) {
		return gui.handleMouse(x, y, -1, true);
	}

	public boolean scrolled (int amount) {
		return gui.handleMouseWheel(amount);
	}

	public void dispose () {
		gui.destroy();
		renderer.dispose();
	}

	static public int getTwlKeyCode (int gdxKeyCode) {
		switch (gdxKeyCode) {
		case Keys.NUM_0:
			return Event.KEY_0;
		case Keys.NUM_1:
			return Event.KEY_1;
		case Keys.NUM_2:
			return Event.KEY_2;
		case Keys.NUM_3:
			return Event.KEY_3;
		case Keys.NUM_4:
			return Event.KEY_4;
		case Keys.NUM_5:
			return Event.KEY_5;
		case Keys.NUM_6:
			return Event.KEY_6;
		case Keys.NUM_7:
			return Event.KEY_7;
		case Keys.NUM_8:
			return Event.KEY_8;
		case Keys.NUM_9:
			return Event.KEY_9;
		case Keys.A:
			return Event.KEY_A;
		case Keys.B:
			return Event.KEY_B;
		case Keys.C:
			return Event.KEY_C;
		case Keys.D:
			return Event.KEY_D;
		case Keys.E:
			return Event.KEY_E;
		case Keys.F:
			return Event.KEY_F;
		case Keys.G:
			return Event.KEY_G;
		case Keys.H:
			return Event.KEY_H;
		case Keys.I:
			return Event.KEY_I;
		case Keys.J:
			return Event.KEY_J;
		case Keys.K:
			return Event.KEY_K;
		case Keys.L:
			return Event.KEY_L;
		case Keys.M:
			return Event.KEY_M;
		case Keys.N:
			return Event.KEY_N;
		case Keys.O:
			return Event.KEY_O;
		case Keys.P:
			return Event.KEY_P;
		case Keys.Q:
			return Event.KEY_Q;
		case Keys.R:
			return Event.KEY_R;
		case Keys.S:
			return Event.KEY_S;
		case Keys.T:
			return Event.KEY_T;
		case Keys.U:
			return Event.KEY_U;
		case Keys.V:
			return Event.KEY_V;
		case Keys.W:
			return Event.KEY_W;
		case Keys.X:
			return Event.KEY_X;
		case Keys.Y:
			return Event.KEY_Y;
		case Keys.Z:
			return Event.KEY_Z;
		case Keys.ALT_LEFT:
			return Event.KEY_LMETA;
		case Keys.ALT_RIGHT:
			return Event.KEY_RMETA;
		case Keys.BACKSLASH:
			return Event.KEY_BACKSLASH;
		case Keys.COMMA:
			return Event.KEY_COMMA;
		case Keys.DEL:
			return Event.KEY_BACK;
		case Keys.FORWARD_DEL:
			return Event.KEY_DELETE;
		case Keys.DPAD_LEFT:
			return Event.KEY_LEFT;
		case Keys.DPAD_RIGHT:
			return Event.KEY_RIGHT;
		case Keys.DPAD_UP:
			return Event.KEY_UP;
		case Keys.DPAD_DOWN:
			return Event.KEY_DOWN;
		case Keys.ENTER:
			return Event.KEY_RETURN;
		case Keys.HOME:
			return Event.KEY_HOME;
		case Keys.MINUS:
			return Event.KEY_MINUS;
		case Keys.PERIOD:
			return Event.KEY_PERIOD;
		case Keys.PLUS:
			return Event.KEY_ADD;
		case Keys.SEMICOLON:
			return Event.KEY_SEMICOLON;
		case Keys.SHIFT_LEFT:
			return Event.KEY_LSHIFT;
		case Keys.SHIFT_RIGHT:
			return Event.KEY_RSHIFT;
		case Keys.SLASH:
			return Event.KEY_SLASH;
		case Keys.SPACE:
			return Event.KEY_SPACE;
		case Keys.TAB:
			return Event.KEY_TAB;
		}
		return Event.KEY_NONE;
	}

	/**
	 * Returns a URL to a theme file, which can be used with
	 * {@link ThemeManager#createThemeManager(URL, de.matthiasmann.twl.renderer.Renderer) ThemeManager} to create a theme for
	 * {@link GUI#applyTheme(ThemeManager)}. This is only needed if not using the {@link TWL} class to make use of TWL.
	 */
	static public URL getThemeURL (String themeFile, final FileType fileType) throws MalformedURLException {
		File file = new File(themeFile);
		final File themeRoot = file.getParentFile();
		return new URL("gdx-twl", "local", 80, file.getName(), new URLStreamHandler() {
			protected URLConnection openConnection (URL url) throws IOException {
				final String path = new File(themeRoot, url.getPath()).getPath();
				final FileHandle fileHandle = Gdx.files.getFileHandle(path, fileType);
				return new URLConnection(url) {
					public void connect () {
					}

					public Object getContent () {
						return fileHandle;
					}

					public InputStream getInputStream () {
						if (!path.endsWith(".xml")) return null; // Only theme files are loaded through the URL.
						return fileHandle.read();
					}
				};
			}
		});
	}
}

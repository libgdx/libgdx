
package com.badlogic.gdx.twl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.twl.renderer.GdxRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.theme.ThemeManager;

public class TWL implements InputProcessor {
	private final GdxRenderer renderer = new GdxRenderer();
	private final GUI gui;

	public TWL (String themeFile, FileType fileType, Widget widget) {
		this(themeFile, fileType);
		setWidget(widget);
	}

	public TWL (String themeFile, FileType fileType) {
		Widget root = new Widget() {
			protected void layout () {
				layoutChildrenFullInnerArea();
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

	public GdxRenderer getRenderer () {
		return renderer;
	}

	public GUI getGUI () {
		return gui;
	}

	public void setWidget (Widget widget) {
		Widget root = gui.getRootPane();
		root.removeAllChildren();
		root.add(widget);
	}

	public void clear () {
		gui.getRootPane().removeAllChildren();
	}

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
		boolean handled = gui.handleKey(0, character, true);
		return gui.handleKey(0, character, false) || handled;
	}

	public boolean touchDown (int x, int y, int pointer) {
		return gui.handleMouse(x, y, pointer, true);
	}

	public boolean touchUp (int x, int y, int pointer) {
		return gui.handleMouse(x, y, pointer, false);
	}

	public boolean touchDragged (int x, int y, int pointer) {
		return gui.handleMouse(x, y, -1, true);
	}

	public void dispose () {
		gui.destroy();
		renderer.dispose();
	}

	static public int getTwlKeyCode (int gdxKeyCode) {
		if (gdxKeyCode == Input.Keys.KEYCODE_0) return Event.KEY_0;
		if (gdxKeyCode == Input.Keys.KEYCODE_1) return Event.KEY_1;
		if (gdxKeyCode == Input.Keys.KEYCODE_2) return Event.KEY_2;
		if (gdxKeyCode == Input.Keys.KEYCODE_3) return Event.KEY_3;
		if (gdxKeyCode == Input.Keys.KEYCODE_4) return Event.KEY_4;
		if (gdxKeyCode == Input.Keys.KEYCODE_5) return Event.KEY_5;
		if (gdxKeyCode == Input.Keys.KEYCODE_6) return Event.KEY_6;
		if (gdxKeyCode == Input.Keys.KEYCODE_7) return Event.KEY_7;
		if (gdxKeyCode == Input.Keys.KEYCODE_8) return Event.KEY_8;
		if (gdxKeyCode == Input.Keys.KEYCODE_9) return Event.KEY_9;
		if (gdxKeyCode == Input.Keys.KEYCODE_A) return Event.KEY_A;
		if (gdxKeyCode == Input.Keys.KEYCODE_B) return Event.KEY_B;
		if (gdxKeyCode == Input.Keys.KEYCODE_C) return Event.KEY_C;
		if (gdxKeyCode == Input.Keys.KEYCODE_D) return Event.KEY_D;
		if (gdxKeyCode == Input.Keys.KEYCODE_E) return Event.KEY_E;
		if (gdxKeyCode == Input.Keys.KEYCODE_F) return Event.KEY_F;
		if (gdxKeyCode == Input.Keys.KEYCODE_G) return Event.KEY_G;
		if (gdxKeyCode == Input.Keys.KEYCODE_H) return Event.KEY_H;
		if (gdxKeyCode == Input.Keys.KEYCODE_I) return Event.KEY_I;
		if (gdxKeyCode == Input.Keys.KEYCODE_J) return Event.KEY_J;
		if (gdxKeyCode == Input.Keys.KEYCODE_K) return Event.KEY_K;
		if (gdxKeyCode == Input.Keys.KEYCODE_L) return Event.KEY_L;
		if (gdxKeyCode == Input.Keys.KEYCODE_M) return Event.KEY_M;
		if (gdxKeyCode == Input.Keys.KEYCODE_N) return Event.KEY_N;
		if (gdxKeyCode == Input.Keys.KEYCODE_O) return Event.KEY_O;
		if (gdxKeyCode == Input.Keys.KEYCODE_P) return Event.KEY_P;
		if (gdxKeyCode == Input.Keys.KEYCODE_Q) return Event.KEY_Q;
		if (gdxKeyCode == Input.Keys.KEYCODE_R) return Event.KEY_R;
		if (gdxKeyCode == Input.Keys.KEYCODE_S) return Event.KEY_S;
		if (gdxKeyCode == Input.Keys.KEYCODE_T) return Event.KEY_T;
		if (gdxKeyCode == Input.Keys.KEYCODE_U) return Event.KEY_U;
		if (gdxKeyCode == Input.Keys.KEYCODE_V) return Event.KEY_V;
		if (gdxKeyCode == Input.Keys.KEYCODE_W) return Event.KEY_W;
		if (gdxKeyCode == Input.Keys.KEYCODE_X) return Event.KEY_X;
		if (gdxKeyCode == Input.Keys.KEYCODE_Y) return Event.KEY_Y;
		if (gdxKeyCode == Input.Keys.KEYCODE_Z) return Event.KEY_Z;
		if (gdxKeyCode == Input.Keys.KEYCODE_ALT_LEFT) return Event.KEY_LMETA;
		if (gdxKeyCode == Input.Keys.KEYCODE_ALT_RIGHT) return Event.KEY_RMETA;
		if (gdxKeyCode == Input.Keys.KEYCODE_BACKSLASH) return Event.KEY_BACKSLASH;
		if (gdxKeyCode == Input.Keys.KEYCODE_COMMA) return Event.KEY_COMMA;
		if (gdxKeyCode == Input.Keys.KEYCODE_DEL) return Event.KEY_DELETE;
		if (gdxKeyCode == Input.Keys.KEYCODE_DPAD_LEFT) return Event.KEY_LEFT;
		if (gdxKeyCode == Input.Keys.KEYCODE_DPAD_RIGHT) return Event.KEY_RIGHT;
		if (gdxKeyCode == Input.Keys.KEYCODE_DPAD_UP) return Event.KEY_UP;
		if (gdxKeyCode == Input.Keys.KEYCODE_DPAD_DOWN) return Event.KEY_DOWN;
		if (gdxKeyCode == Input.Keys.KEYCODE_ENTER) return Event.KEY_RETURN;
		if (gdxKeyCode == Input.Keys.KEYCODE_HOME) return Event.KEY_HOME;
		if (gdxKeyCode == Input.Keys.KEYCODE_MINUS) return Event.KEY_MINUS;
		if (gdxKeyCode == Input.Keys.KEYCODE_PERIOD) return Event.KEY_PERIOD;
		if (gdxKeyCode == Input.Keys.KEYCODE_PLUS) return Event.KEY_ADD;
		if (gdxKeyCode == Input.Keys.KEYCODE_SEMICOLON) return Event.KEY_SEMICOLON;
		if (gdxKeyCode == Input.Keys.KEYCODE_SHIFT_LEFT) return Event.KEY_LSHIFT;
		if (gdxKeyCode == Input.Keys.KEYCODE_SHIFT_RIGHT) return Event.KEY_RSHIFT;
		if (gdxKeyCode == Input.Keys.KEYCODE_SLASH) return Event.KEY_SLASH;
		if (gdxKeyCode == Input.Keys.KEYCODE_SPACE) return Event.KEY_SPACE;
		if (gdxKeyCode == Input.Keys.KEYCODE_TAB) return Event.KEY_TAB;
		return Event.KEY_NONE;
	}

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
						return fileHandle.readFile();
					}
				};
			}
		});
	}
}

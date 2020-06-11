
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;

public class UIUtils {
	//Adapted system checks from com.badlogic.gdx.utils.SharedLibraryLoader
	static public boolean isAndroid = System.getProperty("java.runtime.name").contains("Android");
	static public boolean isMac = !isAndroid && System.getProperty("os.name").contains("Mac");
	static public boolean isWindows = !isAndroid && System.getProperty("os.name").contains("Windows");
	static public boolean isLinux = !isAndroid && System.getProperty("os.name").contains("Linux");
	static public boolean isIos = !isAndroid && ("iOS".equals(System.getProperty("moe.platform.name")) || !(isWindows || isLinux || isMac));

	static public boolean left () {
		return Gdx.input.isButtonPressed(Buttons.LEFT);
	}

	static public boolean left (int button) {
		return button == Buttons.LEFT;
	}

	static public boolean right () {
		return Gdx.input.isButtonPressed(Buttons.RIGHT);
	}

	static public boolean right (int button) {
		return button == Buttons.RIGHT;
	}

	static public boolean middle () {
		return Gdx.input.isButtonPressed(Buttons.MIDDLE);
	}

	static public boolean middle (int button) {
		return button == Buttons.MIDDLE;
	}

	static public boolean shift () {
		return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
	}

	static public boolean shift (int keycode) {
		return keycode == Keys.SHIFT_LEFT || keycode == Keys.SHIFT_RIGHT;
	}

	static public boolean ctrl () {
		if (isMac)
			return Gdx.input.isKeyPressed(Keys.SYM);
		else
			return Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
	}

	static public boolean ctrl (int keycode) {
		if (isMac)
			return keycode == Keys.SYM;
		else
			return keycode == Keys.CONTROL_LEFT || keycode == Keys.CONTROL_RIGHT;
	}

	static public boolean alt () {
		return Gdx.input.isKeyPressed(Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Keys.ALT_RIGHT);
	}

	static public boolean alt (int keycode) {
		return keycode == Keys.ALT_LEFT || keycode == Keys.ALT_RIGHT;
	}
}

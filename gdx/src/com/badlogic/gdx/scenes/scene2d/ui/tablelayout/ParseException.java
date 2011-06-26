
package com.badlogic.gdx.scenes.scene2d.ui.tablelayout;

public class ParseException extends RuntimeException {
	public int line, column;

	public ParseException (String message, Throwable cause) {
		super(message, cause);
	}
}

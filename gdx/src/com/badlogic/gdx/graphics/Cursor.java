
package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Graphics;

/** <p>
 * A class representing a native cursor.
 * </p>
 * 
 * <p>
 * Cursor instances are created via a call to {@link Graphics#newCursor(Pixmap, int, int)}.
 * </p>
 * 
 * <p>
 * Cursor are set using {@link Graphics#setCursor(Cursor)}.
 * </p> **/
public interface Cursor {
	/**
	 * set the desktop mouse cursor to this cursor. 
	 */
	public void setSystemCursor ();
}

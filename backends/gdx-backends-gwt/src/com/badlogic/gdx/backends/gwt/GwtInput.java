
package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.Input;

public interface GwtInput extends Input {

	/** Resets all Input events (called on main loop after rendering) */
	void reset ();
}

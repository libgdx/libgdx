
package com.badlogic.gdx.backends.android;

import android.content.Intent;

/** A listener for special Android events such onActivityResult(...). This can be used by e.g. extensions to plug into the Android
 * system.
 * 
 * @author noblemaster */
public interface AndroidEventListener {

	/** Will be called if the application's onActivityResult(...) method is called. */
	public void onActivityResult (int requestCode, int resultCode, Intent data);
}

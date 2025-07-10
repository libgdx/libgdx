
package com.badlogic.gdx.backends.android;

import android.text.InputType;
import com.badlogic.gdx.Input.OnscreenKeyboardType;

/** Utility class to map libGDX onscreen keyboard types to Android input types. */
public class AndroidInputType {

	/** Returns the Android input type for the given onscreen keyboard type.
	 *
	 * @param type The onscreen keyboard type.
	 * @param defaultDisableAutocorrection If true, disables autocorrection for text input types that do not have a specific input
	 *           type defined.
	 * @return The corresponding Android input type. */
	public static int getType (OnscreenKeyboardType type, boolean defaultDisableAutocorrection) {
		switch (type) {
		case NumberPad:
			return InputType.TYPE_CLASS_NUMBER;
		case PhonePad:
			return InputType.TYPE_CLASS_PHONE;
		case Email:
			return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
		case Password:
			return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
		case URI:
			return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI;
		case None:
			// This is used to disable the IME keyboard, usually used with OTG keyboards.
			return InputType.TYPE_NULL;
		default:
			if (defaultDisableAutocorrection) {
				return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
					| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
			} else {
				return InputType.TYPE_CLASS_TEXT;
			}
		}
	}
}

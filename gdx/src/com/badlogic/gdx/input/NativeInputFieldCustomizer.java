
package com.badlogic.gdx.input;

/** Hook to customize the platform's native input field beyond what {@link NativeInputConfiguration} exposes. Implementations are
 * platform specific: define this interface's implementation in your platform launcher (or inject it from there) and cast the
 * passed object to the platform type.
 * <p>
 * The passed object is:
 * <ul>
 * <li>iOS: `UITextField` (single line) or `UITextView` (multiline)</li>
 * <li>Android: {@code android.widget.AutoCompleteTextView}</li>
 * </ul>
 * The callback is invoked on the platform's UI thread every time the native input field is opened, after libGDX has applied the
 * {@link NativeInputConfiguration} — so customizations win over the defaults. The field's position and size remain managed by
 * libGDX (they are re-applied when the keyboard shows); use {@link NativeInputConfiguration#setHorizontalInsetFraction(float)} to
 * influence sizing. */
public interface NativeInputFieldCustomizer {
	void customize (Object nativeField);
}

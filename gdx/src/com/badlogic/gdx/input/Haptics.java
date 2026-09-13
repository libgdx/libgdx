
package com.badlogic.gdx.input;

public interface Haptics {

	enum ImpactType {
		LIGHT, MEDIUM, HEAVY;
	}

	/** Generates a simple haptic effect of a given duration or a vibration effect on devices without haptic capabilities. Note
	 * that on Android backend you'll need the permission
	 * <code> <uses-permission android:name="android.permission.VIBRATE" /></code> in your manifest file in order for this to work.
	 * On iOS backend you'll need to set <code>useHaptics = true</code> for devices with haptics capabilities to use them.
	 *
	 * @param milliseconds the number of milliseconds to vibrate. */
	void vibrate (int milliseconds);

	/** Generates a simple haptic effect of a given duration and amplitude. Note that on Android backend you'll need the permission
	 * <code> <uses-permission android:name="android.permission.VIBRATE" /></code> in your manifest file in order for this to work.
	 * On iOS backend you'll need to set <code>useHaptics = true</code> for devices with haptics capabilities to use them.
	 *
	 * @param milliseconds the duration of the haptics effect
	 * @param amplitude the amplitude/strength of the haptics effect. Valid values in the range [0, 255]. **/
	void vibrate (int milliseconds, int amplitude);

	/** Generates a simple impact haptic effect of a type. ImpactTypes are length/amplitude haptic effect presets that depend on
	 * each device and are defined by manufacturers. Should give most consistent results across devices and OSs. Note that on
	 * Android backend you'll need the permission <code> <uses-permission android:name="android.permission.VIBRATE" /></code> in
	 * your manifest file in order for this to work. On iOS backend you'll need to set <code>useHaptics = true</code> for devices
	 * with haptics capabilities to use them.
	 *
	 * @param impactType the type of vibration */
	void impact (ImpactType impactType);

	boolean isHapticsSupported ();

	boolean isVibratorSupported ();

	boolean isFallbackEnabled ();

	void setFallbackEnabled (boolean enabled);

}

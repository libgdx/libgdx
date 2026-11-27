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

package com.badlogic.gdx.input;

/** Provides vibration and haptic feedback. Obtain an instance through {@link com.badlogic.gdx.Input#getHaptics()}, or install a
 * custom implementation (e.g. for a gamepad) with {@link com.badlogic.gdx.Input#setHaptics(Haptics)}.
 * <p>
 * Each effect needs certain device capabilities. {@link #isHapticsSupported()} tells whether the device supports the full effect,
 * and {@link #isVibratorSupported()} tells whether it can only vibrate. If an effect isn't supported, it does nothing unless
 * fallback is enabled (see {@link #setFallbackEnabled(boolean)}). In that case a simpler effect is played that ignores the
 * unsupported parameters, such as amplitude or duration. Which effects can fall back depends on the platform and is noted on each
 * method.
 * <p>
 * Platform notes:
 * <ul>
 * <li>Android: requires <code>&lt;uses-permission android:name="android.permission.VIBRATE" /&gt;</code> in the manifest. Haptics
 * support means a vibrator with amplitude control (API 29+).</li>
 * <li>iOS: set <code>useHaptics = true</code> in the application configuration to use Core Haptics. Haptics support means a
 * device with Core Haptics and <code>useHaptics</code> enabled. Vibrator support means an iPhone. The only fallback effect is the
 * default system vibration, which has a fixed duration.</li>
 * <li>Other backends: all methods do nothing and both support queries return false.</li>
 * </ul>
 */
public interface Haptics {

	/** Predefined impact effects. Their exact duration and strength are defined by the platform and device manufacturer. */
	enum ImpactType {
		LIGHT, MEDIUM, HEAVY;
	}

	/** Vibrates for the given duration at the device's default strength.
	 * <ul>
	 * <li>Android: needs only a vibrator, so fallback never applies.</li>
	 * <li>iOS: needs haptics support. Without it, on a device with a vibrator and with fallback enabled, plays the default system
	 * vibration and ignores <code>milliseconds</code>.</li>
	 * </ul>
	 * @param milliseconds the duration of the vibration */
	void vibrate (int milliseconds);

	/** Vibrates for the given duration at the given strength. Needs haptics support on all platforms. Without it, and with
	 * fallback enabled, behaves like {@link #vibrate(int)} and ignores <code>amplitude</code>.
	 * @param milliseconds the duration of the vibration
	 * @param amplitude the strength of the vibration, in the range [1, 255]. Values outside the range are clamped. */
	void vibrate (int milliseconds, int amplitude);

	/** Plays a predefined impact effect. This gives the most consistent results across devices and platforms. Needs haptics
	 * support. Fallback never applies, so it does nothing on devices without haptics support.
	 * @param impactType the type of impact */
	void impact (ImpactType impactType);

	/** Stops any vibration or haptic effect in progress. Not supported on iOS, where effects always play to completion. */
	void cancel ();

	/** @return whether the device supports haptic effects with controllable strength. If false, only {@link #vibrate(int)} may
	 *         work. See the platform notes in the class documentation. */
	boolean isHapticsSupported ();

	/** @return whether the device has a vibrator. */
	boolean isVibratorSupported ();

	/** @return whether unsupported effects fall back to a simpler vibration. See {@link #setFallbackEnabled(boolean)}. */
	boolean isFallbackEnabled ();

	/** Sets whether effects the device can't fully play should fall back to a simpler vibration instead of doing nothing. Each
	 * method documents when its fallback applies. The initial value comes from <code>hapticsFallback</code> in the application
	 * configuration.
	 * @param enabled true to enable fallback */
	void setFallbackEnabled (boolean enabled);
}

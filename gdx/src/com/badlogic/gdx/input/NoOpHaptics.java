
package com.badlogic.gdx.input;

/** A {@link Haptics} implementation that does nothing, used on platforms without haptics support. */
public class NoOpHaptics implements Haptics {

	public static final NoOpHaptics INSTANCE = new NoOpHaptics();

	@Override
	public void vibrate (int milliseconds) {
	}

	@Override
	public void vibrate (int milliseconds, int amplitude) {
	}

	@Override
	public void impact (ImpactType impactType) {
	}

	@Override
	public void cancel () {
	}

	@Override
	public boolean isHapticsSupported () {
		return false;
	}

	@Override
	public boolean isVibratorSupported () {
		return false;
	}

	@Override
	public boolean isFallbackEnabled () {
		return false;
	}

	@Override
	public void setFallbackEnabled (boolean enabled) {

	}
}

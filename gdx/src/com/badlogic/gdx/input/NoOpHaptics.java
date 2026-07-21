
package com.badlogic.gdx.input;

public class NoOpHaptics implements Haptics {

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
	public boolean isHapticsSupported () {
		return false;
	}

	@Override
	public boolean isVibratorSupported () {
		return false;
	}

	@Override
	public boolean isFallbackEnabled() {
		return false;
	}

	@Override
	public void setFallbackEnabled(boolean enabled) {

	}
}

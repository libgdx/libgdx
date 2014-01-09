package com.badlogic.gdx.backends.headless.mock.audio;

import com.badlogic.gdx.audio.AudioDevice;

public class MockAudioDevice implements AudioDevice {

	@Override
	public boolean isMono() {
		return false;
	}

	@Override
	public void writeSamples(short[] samples, int offset, int numSamples) {

	}

	@Override
	public void writeSamples(float[] samples, int offset, int numSamples) {

	}

	@Override
	public int getLatency() {
		return 0;
	}

	@Override
	public void dispose() {

	}

	@Override
	public void setVolume(float volume) {

	}
}

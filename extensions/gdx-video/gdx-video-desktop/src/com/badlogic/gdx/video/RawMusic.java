package com.badlogic.gdx.video;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALAudio;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic;

/**
 * The RawMusic class extends OpenAlMusic, and retrieves it's audio from a VideoDecoder instance.
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 *
 */
class RawMusic
extends OpenALMusic {
	VideoDecoder decoder;
	ByteBuffer backBuffer;

	public RawMusic(VideoDecoder decoder, ByteBuffer buffer, int channels, int sampleRate) {
		super((OpenALAudio) Gdx.audio, null);
		this.decoder = decoder;
		backBuffer = buffer;
		setup(channels, sampleRate);
	}

	@Override
	public int read(byte[] buffer) {
		int sizeNeeded = buffer.length;
		int currentIndex = 0;

		while (sizeNeeded > 0) {
			if (backBuffer.remaining() > 0) {
				int numBytes = Math.min(backBuffer.remaining(), sizeNeeded);
				backBuffer.get(buffer, currentIndex, numBytes);
				currentIndex += numBytes;
				sizeNeeded -= numBytes;
			} else {
				// We need to fill the buffer;
				backBuffer.rewind();
				decoder.updateAudioBuffer();
			}
		}

		return buffer.length;
	}

	@Override
	public void reset() {

	}

}

/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
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

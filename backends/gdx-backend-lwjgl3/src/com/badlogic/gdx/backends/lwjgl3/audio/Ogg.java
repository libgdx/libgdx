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

package com.badlogic.gdx.backends.lwjgl3.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;

/** @author Nathan Sweet */
public class Ogg {
	static public class Music extends OpenALMusic {
		private OggInputStream input;
		private OggInputStream previousInput;

		public Music (OpenALLwjgl3Audio audio, FileHandle file) {
			super(audio, file);
			if (audio.noDevice) return;
			input = new OggInputStream(file.read());
			setup(input.getChannels(), input.getSampleRate());
		}

		public int read (byte[] buffer) {
			if (input == null) {
				input = new OggInputStream(file.read(), previousInput);
				setup(input.getChannels(), input.getSampleRate());
				previousInput = null; // release this reference
			}
			return input.read(buffer);
		}

		public void reset () {
			StreamUtils.closeQuietly(input);
			previousInput = null;
			input = null;
		}

		@Override
		protected void loop () {
			StreamUtils.closeQuietly(input);
			previousInput = input;
			input = null;
		}
	}

	static public class Sound extends OpenALSound {
		public Sound (OpenALLwjgl3Audio audio, FileHandle file) {
			super(audio);
			if (audio.noDevice) return;

			// read file into byte array
			InputStream stream = file.read();
			final ByteArrayOutputStream converterStream = new ByteArrayOutputStream();
			final byte[] converterBuffer = new byte[20000];
			try {
				while (true) {
					final int read = stream.read(converterBuffer);
					if (read <= 0) {
						break;
					}
					converterStream.write(converterBuffer, 0, read);
				}
			} catch (IOException e) {
				throw new GdxRuntimeException("Error reading OGG file: " + file, e);
			} finally {
				StreamUtils.closeQuietly(stream);
			}

			// put the encoded audio data in a ByteBuffer
			byte[] streamData = converterStream.toByteArray();
			ByteBuffer encodedData = BufferUtils.newByteBuffer(streamData.length);
			encodedData.put(streamData);
			encodedData.flip();

			try (MemoryStack stack = MemoryStack.stackPush()) {
				final IntBuffer channelsBuffer = stack.mallocInt(1);
				final IntBuffer sampleRateBuffer = stack.mallocInt(1);

				// decode
				final ShortBuffer decodedData = STBVorbis.stb_vorbis_decode_memory(encodedData, channelsBuffer, sampleRateBuffer);
				if (decodedData == null) {
					throw new GdxRuntimeException("Error decoding OGG file: " + file);
				}

				final int channels = channelsBuffer.get(0);
				final int sampleRate = sampleRateBuffer.get(0);
				setup(decodedData, channels, sampleRate);
			}
		}
	}
}

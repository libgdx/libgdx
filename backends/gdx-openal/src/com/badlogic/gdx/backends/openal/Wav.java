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
package com.badlogic.gdx.backends.openal;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Wav {
	static public class Music extends OpenALMusic {
		private WavInputStream input;

		public Music (OpenALAudio audio, FileHandle file) {
			super(audio, file);
		}

		protected int read (byte[] buffer) {
			if (input == null) {
				input = new WavInputStream(file);
				setup(input.channels, input.sampleRate);
			}
			try {
				return input.readData(buffer);
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error reading WAV file: " + file, ex);
			}
		}

		protected void reset () {
			if (input == null) return;
			try {
				input.close();
			} catch (IOException ignored) {
			}
			input = null;
		}
	}

	static public class Sound extends OpenALSound {
		public Sound (OpenALAudio audio, FileHandle file) {
			super(audio);

			WavInputStream input = new WavInputStream(file);
			ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
			try {
				byte[] buffer = new byte[2048];
				while (true) {
					int length = input.readData(buffer);
					if (length == -1) break;
					output.write(buffer, 0, length);
				}
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error reading WAV file: " + file, ex);
			}
			setup(output.toByteArray(), input.channels, input.sampleRate);
		}
	}

	static private class WavInputStream extends FilterInputStream {
		int channels, sampleRate, dataRemaining;

		WavInputStream (FileHandle file) {
			super(file.read());
			try {
				if (read() != 'R' || read() != 'I' || read() != 'F' || read() != 'F')
					throw new GdxRuntimeException("RIFF header not found: " + file);

				skip(4);

				if (read() != 'W' || read() != 'A' || read() != 'V' || read() != 'E')
					throw new GdxRuntimeException("Invalid wave file header: " + file);

				if (read() != 'f' || read() != 'm' || read() != 't' || read() != ' ')
					throw new GdxRuntimeException("fmt header not found: " + file);

				int waveChunkLength = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;

				int type = read() & 0xff | (read() & 0xff) << 8;
				if (type != 1) throw new GdxRuntimeException("WAV files must be PCM: " + type);

				channels = read() & 0xff | (read() & 0xff) << 8;
				if (channels != 1 && channels != 2)
					throw new GdxRuntimeException("WAV files must have 1 or 2 channels: " + channels);

				sampleRate = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;

				skip(6);

				int bitsPerSample = read() & 0xff | (read() & 0xff) << 8;
				if (bitsPerSample != 16) throw new GdxRuntimeException("WAV files must have 16 bits per sample: " + bitsPerSample);

				skip(waveChunkLength - 16);

				if (read() != 'd' || read() != 'a' || read() != 't' || read() != 'a')
					throw new GdxRuntimeException("data header not found: " + file);

				dataRemaining = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;
			} catch (Throwable ex) {
				try {
					close();
				} catch (IOException ignored) {
				}
				throw new GdxRuntimeException("Error reading WAV file: " + file, ex);
			}
		}

		public int readData (byte[] buffer) throws IOException {
			if (dataRemaining == 0) return -1;
			int length = Math.min(read(buffer), dataRemaining);
			if (length == -1) return -1;
			dataRemaining -= length;
			return length;
		}
	}
}

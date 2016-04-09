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

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

public class Wav {
	static public class Music extends OpenALMusic {
		private WavInputStream input;

		public Music (OpenALAudio audio, FileHandle file) {
			super(audio, file);
			input = new WavInputStream(file);
			if (audio.noDevice) return;
			setup(input.channels, input.sampleRate);
		}

		public int read (byte[] buffer) {
			if (input == null) {
				input = new WavInputStream(file);
				setup(input.channels, input.sampleRate);
			}
			try {
				return input.read(buffer);
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error reading WAV file: " + file, ex);
			}
		}

		public void reset () {
			StreamUtils.closeQuietly(input);
			input = null;
		}
	}

	static public class Sound extends OpenALSound {
		public Sound (OpenALAudio audio, FileHandle file) {
			super(audio);
			if (audio.noDevice) return;

			WavInputStream input = null;
			try {
				input = new WavInputStream(file);
				setup(StreamUtils.copyStreamToByteArray(input, input.dataRemaining), input.channels, input.sampleRate);
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error reading WAV file: " + file, ex);
			} finally {
				StreamUtils.closeQuietly(input);
			}
		}
	}

	/** @author Nathan Sweet */
	static private class WavInputStream extends FilterInputStream {
		int channels, sampleRate, dataRemaining;

		WavInputStream (FileHandle file) {
			super(file.read());
			try {
				if (read() != 'R' || read() != 'I' || read() != 'F' || read() != 'F')
					throw new GdxRuntimeException("RIFF header not found: " + file);

				skipFully(4);

				if (read() != 'W' || read() != 'A' || read() != 'V' || read() != 'E')
					throw new GdxRuntimeException("Invalid wave file header: " + file);

				int fmtChunkLength = seekToChunk('f', 'm', 't', ' ');

				int type = read() & 0xff | (read() & 0xff) << 8;
				if (type != 1) throw new GdxRuntimeException("WAV files must be PCM: " + type);

				channels = read() & 0xff | (read() & 0xff) << 8;
				if (channels != 1 && channels != 2)
					throw new GdxRuntimeException("WAV files must have 1 or 2 channels: " + channels);

				sampleRate = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;

				skipFully(6);

				int bitsPerSample = read() & 0xff | (read() & 0xff) << 8;
				if (bitsPerSample != 16) throw new GdxRuntimeException("WAV files must have 16 bits per sample: " + bitsPerSample);

				skipFully(fmtChunkLength - 16);

				dataRemaining = seekToChunk('d', 'a', 't', 'a');
			} catch (Throwable ex) {
				StreamUtils.closeQuietly(this);
				throw new GdxRuntimeException("Error reading WAV file: " + file, ex);
			}
		}

		private int seekToChunk (char c1, char c2, char c3, char c4) throws IOException {
			while (true) {
				boolean found = read() == c1;
				found &= read() == c2;
				found &= read() == c3;
				found &= read() == c4;
				int chunkLength = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;
				if (chunkLength == -1) throw new IOException("Chunk not found: " + c1 + c2 + c3 + c4);
				if (found) return chunkLength;
				skipFully(chunkLength);
			}
		}

		private void skipFully (int count) throws IOException {
			while (count > 0) {
				long skipped = in.skip(count);
				if (skipped <= 0) throw new EOFException("Unable to skip.");
				count -= skipped;
			}
		}

		public int read (byte[] buffer) throws IOException {
			if (dataRemaining == 0) return -1;
			int length = Math.min(super.read(buffer), dataRemaining);
			if (length == -1) return -1;
			dataRemaining -= length;
			return length;
		}
	}
}

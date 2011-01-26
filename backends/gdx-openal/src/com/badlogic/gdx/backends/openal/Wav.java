
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
				int read = input.read(buffer);
				System.out.println(read);
				return read;
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
				while (input.dataLength > 0) {
					int length = input.read(buffer);
					if (length == -1) break;
					length = Math.min(length, input.dataLength);
					input.dataLength -= length;
					output.write(buffer, 0, length);
				}
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error reading WAV file: " + file, ex);
			}
			setup(output.toByteArray(), input.channels, input.sampleRate);
		}
	}

	static private class WavInputStream extends FilterInputStream {
		int channels, sampleRate, dataLength;

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

				dataLength = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;
			} catch (Throwable ex) {
				try {
					close();
				} catch (IOException ignored) {
				}
				throw new GdxRuntimeException("Error reading WAV file: " + file, ex);
			}
		}
	}
}

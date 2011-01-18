
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
				while (true) {
					int length = input.read(buffer);
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
		static private final int headerBytes = 12 + 24 + 8; // RIFF chunk + FORMAT chunk + DATA chunk

		int channels, sampleRate;

		WavInputStream (FileHandle file) {
			super(file.read());
			try {
				byte[] head = new byte[headerBytes];
				int total = 0;
				while (total < headerBytes) {
					int bytes = read(head, total, headerBytes - total);
					if (bytes == -1) break;
					total += bytes;
				}
				if (total != headerBytes) throw new GdxRuntimeException("Unexpected EOF while reading header: " + file);

				if (head[0] != 'R' || head[1] != 'I' || head[2] != 'F' || head[3] != 'F' || head[8] != 'W' || head[9] != 'A'
					|| head[10] != 'V' || head[11] != 'E') throw new GdxRuntimeException("RIFF header not found: " + file);

				int type = head[21] << 8 & 0x0000FF00 | head[20] & 0x00000FF;
				if (type != 1) throw new GdxRuntimeException("WAV files must be PCM: " + type);

				channels = head[23] << 8 & 0xFF00 | head[22] & 0xFF;
				if (channels != 1 && channels != 2)
					throw new GdxRuntimeException("WAV files must have 1 or 2 channels: " + channels);

				sampleRate = head[25] << 8 & 0xFF00 | head[24] & 0xFF | head[26] << 32 & 0xFF000000 | head[27] << 16 & 0xFF0000;

				int bitsPerSample = head[35] << 8 & 0xFF00 | head[34] & 0xFF;
				if (bitsPerSample != 16) throw new GdxRuntimeException("WAV files must have 16 bits per sample: " + bitsPerSample);
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

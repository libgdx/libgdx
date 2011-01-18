
package com.badlogic.gdx.backends.openal;

import java.io.ByteArrayOutputStream;

import com.badlogic.gdx.files.FileHandle;

/**
 * @author Nathan Sweet
 */
public class Ogg {
	static public class Music extends OpenALMusic {
		private OggInputStream input;

		public Music (OpenALAudio audio, FileHandle file) {
			super(audio, file);
		}

		protected int read (byte[] buffer) {
			if (input == null) {
				input = new OggInputStream(file.read());
				setup(input.getChannels(), input.getSampleRate());
			}
			return input.read(buffer);
		}

		protected void reset () {
			if (input == null) return;
			input.close();
			input = null;
		}
	}

	static public class Sound extends OpenALSound {
		public Sound (OpenALAudio audio, FileHandle file) {
			super(audio);

			OggInputStream input = new OggInputStream(file.read());
			ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
			byte[] buffer = new byte[2048];
			while (!input.atEnd()) {
				int length = input.read(buffer);
				if (length == -1) break;
				output.write(buffer, 0, length);
			}
			setup(output.toByteArray(), input.getChannels(), input.getSampleRate());
		}
	}
}

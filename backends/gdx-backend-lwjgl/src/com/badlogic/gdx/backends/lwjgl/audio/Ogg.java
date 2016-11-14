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

package com.badlogic.gdx.backends.lwjgl.audio;

import java.io.ByteArrayOutputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

/** @author Nathan Sweet */
public class Ogg {
	static public class Music extends OpenALMusic {
		private OggInputStream input;
		private OggInputStream previousInput;

		public Music (OpenALAudio audio, FileHandle file) {
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
		public Sound (OpenALAudio audio, FileHandle file) {
			super(audio);
			if (audio.noDevice) return;
			OggInputStream input = null;
			try {
				input = new OggInputStream(file.read());
				ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
				byte[] buffer = new byte[2048];
				while (!input.atEnd()) {
					int length = input.read(buffer);
					if (length == -1) break;
					output.write(buffer, 0, length);
				}
				setup(output.toByteArray(), input.getChannels(), input.getSampleRate());
			} finally {
				StreamUtils.closeQuietly(input);
			}
		}
	}
}

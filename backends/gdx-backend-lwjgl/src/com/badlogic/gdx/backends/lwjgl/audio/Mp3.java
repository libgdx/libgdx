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

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.MP3Decoder;
import javazoom.jl.decoder.OutputBuffer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @author Nathan Sweet */
public class Mp3 {
	static public class Music extends OpenALMusic {
		// Note: This uses a slightly modified version of JLayer.

		private Bitstream bitstream;
		private OutputBuffer outputBuffer;
		private MP3Decoder decoder;

		public Music (OpenALAudio audio, FileHandle file) {
			super(audio, file);
			if (audio.noDevice) return;
			bitstream = new Bitstream(file.read());
			decoder = new MP3Decoder();
			try {
				Header header = bitstream.readFrame();
				if (header == null) throw new GdxRuntimeException("Empty MP3");
				int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
				outputBuffer = new OutputBuffer(channels, false);
				decoder.setOutputBuffer(outputBuffer);
				setup(channels, header.getSampleRate());
			} catch (BitstreamException e) {
				throw new GdxRuntimeException("error while preloading mp3", e);
			}
		}

		public int read (byte[] buffer) {
			try {
				boolean setup = bitstream == null;
				if (setup) {
					bitstream = new Bitstream(file.read());
					decoder = new MP3Decoder();
				}

				int totalLength = 0;
				int minRequiredLength = buffer.length - OutputBuffer.BUFFERSIZE * 2;
				while (totalLength <= minRequiredLength) {
					Header header = bitstream.readFrame();
					if (header == null) break;
					if (setup) {
						int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
						outputBuffer = new OutputBuffer(channels, false);
						decoder.setOutputBuffer(outputBuffer);
						setup(channels, header.getSampleRate());
						setup = false;
					}
					try {
						decoder.decodeFrame(header, bitstream);
					} catch (Exception ignored) {
						// JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes!?
					}
					bitstream.closeFrame();

					int length = outputBuffer.reset();
					System.arraycopy(outputBuffer.getBuffer(), 0, buffer, totalLength, length);
					totalLength += length;
				}
				return totalLength;
			} catch (Throwable ex) {
				reset();
				throw new GdxRuntimeException("Error reading audio data.", ex);
			}
		}

		public void reset () {
			if (bitstream == null) return;
			try {
				bitstream.close();
			} catch (BitstreamException ignored) {
			}
			bitstream = null;
		}
	}

	static public class Sound extends OpenALSound {
		// Note: This uses a slightly modified version of JLayer.

		public Sound (OpenALAudio audio, FileHandle file) {
			super(audio);
			if (audio.noDevice) return;
			ByteArrayOutputStream output = new ByteArrayOutputStream(4096);

			Bitstream bitstream = new Bitstream(file.read());
			MP3Decoder decoder = new MP3Decoder();

			try {
				OutputBuffer outputBuffer = null;
				int sampleRate = -1, channels = -1;
				while (true) {
					Header header = bitstream.readFrame();
					if (header == null) break;
					if (outputBuffer == null) {
						channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
						outputBuffer = new OutputBuffer(channels, false);
						decoder.setOutputBuffer(outputBuffer);
						sampleRate = header.getSampleRate();
					}
					try {
						decoder.decodeFrame(header, bitstream);
					} catch (Exception ignored) {
						// JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes!?
					}
					bitstream.closeFrame();
					output.write(outputBuffer.getBuffer(), 0, outputBuffer.reset());
				}
				bitstream.close();
				setup(output.toByteArray(), channels, sampleRate);
			} catch (Throwable ex) {
				throw new GdxRuntimeException("Error reading audio data.", ex);
			}
		}
	}
}

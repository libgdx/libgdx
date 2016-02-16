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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;

public class M4a {

	private static class Stream {

		private FileHandle file;
		private Decoder decoder;
		private Track track;
		private final SampleBuffer sampleBuffer = new SampleBuffer();

		Stream (FileHandle file) {
			this.file = file;
			setup();
		}

		private void setup () {
			try {
				MP4Container container = new MP4Container(file.read());

				Movie movie = container.getMovie();
				if (movie == null) {
					throw new GdxRuntimeException("Empty M4A");
				}

				List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
				if (tracks.size() <= 0) {
					throw new GdxRuntimeException("No AAC tracks in M4A");
				}

				track = tracks.get(0);

				byte[] decoderSpecificInfo = track.getDecoderSpecificInfo();
				decoder = new Decoder(decoderSpecificInfo);
				sampleBuffer.setBigEndian(false);

			} catch (IOException e) {
				throw new GdxRuntimeException("Error while preloading M4A.", e);
			}
		}

		int read (byte[] buffer) {

			if (decoder == null || track == null) {
				setup();
			}

			int totalLength = 0;
			int minLengthRequired = buffer.length - decoder.getConfig().getFrameLength() * 2;
			while (totalLength <= minLengthRequired) {
				try {
					Frame frame = track.readNextFrame();
					if (frame == null) {
						break;
					}
					decoder.decodeFrame(frame.getData(), sampleBuffer);
					byte[] pcmFrame = sampleBuffer.getData();
					int length = pcmFrame.length;
					System.arraycopy(pcmFrame, 0, buffer, totalLength, length);
					totalLength += length;
				} catch (IOException e) {
					throw new GdxRuntimeException("Error reading audio data.", e);
				}
			}
			return totalLength;
		}

		void reset () {
			decoder = null;
			track = null;
		}

		int getChannelCount () {
			return decoder.getConfig().getChannelConfiguration().getChannelCount();
		}

		int getFrequency () {
			return decoder.getConfig().getSampleFrequency().getFrequency();
		}

	}

	public static class Music extends OpenALMusic {

		private Stream stream;

		public Music (OpenALAudio audio, FileHandle file) {
			super(audio, file);
			if (audio.noDevice) return;

			stream = new Stream(file);
			setup(stream.getChannelCount(), stream.getFrequency());
		}

		@Override
		public int read (byte[] buffer) {
			return stream.read(buffer);
		}

		@Override
		public void reset () {
			stream.reset();
		}
	}

	public static class Sound extends OpenALSound {

		private Stream stream;

		public Sound (OpenALAudio audio, FileHandle file) {

			super(audio);
			if (audio.noDevice) return;

			stream = new Stream(file);

			ByteArrayOutputStream output = new ByteArrayOutputStream();

			byte[] buffer = new byte[4096];
			int length;
			while ((length = stream.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}

			setup(output.toByteArray(), stream.getChannelCount(), stream.getFrequency());
		}
	}
}

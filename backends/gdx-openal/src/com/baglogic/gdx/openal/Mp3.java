
package com.baglogic.gdx.openal;

import java.io.ByteArrayOutputStream;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.MP3Decoder;
import javazoom.jl.decoder.OutputBuffer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * @author Nathan Sweet
 */
public class Mp3 {
	static public class Music extends OpenALMusic {
		// Note: This uses a slightly modified version of JLayer.

		private Bitstream bitstream;
		private OutputBuffer outputBuffer;
		private MP3Decoder decoder;

		public Music (OpenALAudio audio, FileHandle file) {
			super(audio, file);
		}

		protected int read (byte[] buffer) {
			try {
				boolean setup = bitstream == null;
				if (setup) {
					bitstream = new Bitstream(file.read());

					outputBuffer = new OutputBuffer(2, false);

					decoder = new MP3Decoder();
					decoder.setOutputBuffer(outputBuffer);
				}

				int totalLength = 0;
				int minRequiredLength = buffer.length - OutputBuffer.BUFFERSIZE * 2 - 1;
				while (totalLength < minRequiredLength) {
					Header header = bitstream.readFrame();
					if (header == null) break;
					if (setup) {
						setup(outputBuffer.isStereo() ? 2 : 1, header.getSampleRate());
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

		protected void reset () {
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

			ByteArrayOutputStream output = new ByteArrayOutputStream(4096);

			Bitstream bitstream = new Bitstream(file.read());

			OutputBuffer outputBuffer = new OutputBuffer(2, false);

			MP3Decoder decoder = new MP3Decoder();
			decoder.setOutputBuffer(outputBuffer);

			try {
				int sampleRate = -1;
				while (true) {
					Header header = bitstream.readFrame();
					if (header == null) break;
					if (sampleRate == -1) sampleRate = header.getSampleRate();
					try {
						decoder.decodeFrame(header, bitstream);
					} catch (Exception ignored) {
						// JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes!?
					}
					bitstream.closeFrame();
					output.write(outputBuffer.getBuffer(), 0, outputBuffer.reset());
				}
				bitstream.close();
				setup(output.toByteArray(), outputBuffer.isStereo() ? 2 : 1, sampleRate);
			} catch (Throwable ex) {
				throw new GdxRuntimeException("Error reading audio data.", ex);
			}
		}
	}
}

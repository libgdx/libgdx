package com.badlogic.gdx.audio.io;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * {@link Decoder} implementation for WAV files, pure Java, beware.
 * @author mzechner
 *
 */
public class WavDecoder extends Decoder {
	WavInputStream in;
	byte[] buffer = new byte[1024];
	
	/**
	 * Creates a new WAV decoder. The file can be of any type.
	 * @param file the {@link FileHandle}
	 */
	public WavDecoder(FileHandle file) {
		in = new WavInputStream(file);
	}
	
	@Override
	public int readSamples(short[] samples, int offset, int numSamples) {
		int read = 0;
		int total = 0;
		if(buffer.length < samples.length * 2) buffer = new byte[samples.length * 2];
		numSamples *= 2;
		try {
			while((read = in.read(buffer, total, numSamples - total)) > 0) {
				total += read;
			}
			total = total - (total % 2);
			for(int j=0; j < total; j+=2) {
				samples[offset++] = (short)(((buffer[j + 1] << 8) & 0xff00) | (buffer[j] & 0xff));
			}
		} catch (IOException e) {
			return 0;
		}
		total = total / 2; // note integer divide, total will always be even,discarding trailing bytes.
		return total;
	}

	@Override
	public int skipSamples(int numSamples) {
		try {
			return (int)in.skip(numSamples * 2 * getChannels()) / (2 * getChannels());
		} catch (IOException e) {
			Gdx.app.error("WavDecoder", "Couldn't skip");
			return 0;
		}
	}

	@Override
	public int getChannels() {
		return in.channels;
	}

	@Override
	public int getRate() {
		return in.sampleRate;
	}

	@Override
	public float getLength() {
		return (in.dataRemaining / (2 * getChannels()) / (float)getRate());
	}

	@Override
	public void dispose() {
		try {
			if(in != null) in.close();
		} catch(Exception e) {
			// silent catch ftw...
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
				try {
					close();
				} catch (IOException ignored) {
				}
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

		public int readData (byte[] buffer) throws IOException {
			if (dataRemaining == 0) return -1;
			int length = Math.min(read(buffer), dataRemaining);
			if (length == -1) return -1;
			dataRemaining -= length;
			return length;
		}
	}
}

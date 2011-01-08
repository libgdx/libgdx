
package com.baglogic.gdx.audio;

import java.io.ByteArrayOutputStream;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.MP3Decoder;
import javazoom.jl.decoder.OutputBuffer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Mp3Sound extends OpenALSound {
	public Mp3Sound (OpenALAudio audio, FileHandle file) {
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

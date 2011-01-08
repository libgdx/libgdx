
package com.baglogic.gdx.openal;

import java.io.ByteArrayOutputStream;

import com.badlogic.gdx.files.FileHandle;

public class OggSound extends OpenALSound {
	public OggSound (OpenALAudio audio, FileHandle file) {
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


package com.baglogic.gdx.openal;

import com.badlogic.gdx.files.FileHandle;

public class OggMusic extends OpenALMusic {
	private OggInputStream input;

	public OggMusic (OpenALAudio audio, FileHandle file) {
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

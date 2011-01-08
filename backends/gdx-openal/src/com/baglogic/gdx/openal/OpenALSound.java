
package com.baglogic.gdx.openal;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import static org.lwjgl.openal.AL10.*;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class OpenALSound implements Sound {
	private int bufferID = -1;
	private final OpenALAudio audio;
	private int streamID;

	public OpenALSound (OpenALAudio audio) {
		this.audio = audio;
	}

	void setup (byte[] pcm, int channels, int sampleRate) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(pcm.length);
		buffer.put(pcm);
		buffer.flip();

		if (bufferID == -1) {
			bufferID = alGenBuffers();
			alBufferData(bufferID, channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16, buffer, sampleRate);
		}
	}

	public void play () {
		play(1);
	}

	public void play (float volume) {
		streamID = audio.getIdleStreamID();
		if (streamID == -1) return;
		alSourceStop(streamID);
		alSourcei(streamID, AL_BUFFER, bufferID);
		alSourcei(streamID, AL_LOOPING, AL_FALSE);
		alSourcef(streamID, AL_GAIN, volume);
		alSourcePlay(streamID);
	}

	public void loop () {
		streamID = audio.getIdleStreamID();
		if (streamID == -1) return;
		alSourceStop(streamID);
		alSourcei(streamID, AL_BUFFER, bufferID);
		alSourcei(streamID, AL_LOOPING, AL_TRUE);
		alSourcePlay(streamID);
	}

	public void stop () {
		alSourceStop(streamID);
	}

	public void dispose () {
		alDeleteBuffers(bufferID);
	}
}

package com.badlogic.gdx.backends.lwjgl3.audio;

import static org.lwjgl.openal.AL10.AL_BUFFERS_PROCESSED;
import static org.lwjgl.openal.AL10.AL_BUFFERS_QUEUED;
import static org.lwjgl.openal.AL10.AL_INVALID_VALUE;
import static org.lwjgl.openal.AL10.AL_NO_ERROR;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGetError;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceQueueBuffers;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourceUnqueueBuffers;
import static org.lwjgl.openal.AL10.alSourcef;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL11;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * This is the part of {@link OpenALMusic} that should only be called by {@link OpenALMusicProcessor}
 * Separation in 2 classes concurrent access isolation.
 */
public class OpenALMusicAsync implements Disposable {

	static final int bufferCount = 3;
	static private final int bufferSize = 4096 * 10;
	static private final int bytesPerSample = 2;

	static private final byte[] tempBytes = new byte[bufferSize];
	static private final ByteBuffer tempBuffer = BufferUtils.createByteBuffer(bufferSize);

	private IntBuffer buffers;
	
	FloatArray renderedSecondsQueue = new FloatArray(bufferCount);
	float renderedSeconds;
	float maxSecondsPerBuffer;
	float positionToSeek;

	private final OpenALMusic music;

	public OpenALMusicAsync (OpenALMusic music) {
		this.music = music;
	}
	
	void setup (int channels, int sampleRate) {
		maxSecondsPerBuffer = (float)bufferSize / (bytesPerSample * channels * sampleRate);
	}
	
	void stop(){
		renderedSeconds = 0;
		renderedSecondsQueue.clear();
		music.reset();
	}
	
	/**
	 * @return true if buffers are filled, false if finished (was seek to end)
	 */
	boolean seek()
	{
		final float position = positionToSeek;
		final int sourceID = music.sourceID;
		
		alSourceStop(sourceID);
		alSourceUnqueueBuffers(sourceID, buffers);
		while (renderedSecondsQueue.size > 0) {
			renderedSeconds = renderedSecondsQueue.pop();
		}
		if (position <= renderedSeconds) {
			music.reset();
			renderedSeconds = 0;
		}
		
		while (renderedSeconds < (position - maxSecondsPerBuffer)) {
			if (music.read(tempBytes) <= 0) break;
			renderedSeconds += maxSecondsPerBuffer;
		}
		renderedSecondsQueue.add(renderedSeconds);
		
		boolean buffersFilled = prefillBuffers();
		
		renderedSecondsQueue.pop();
		if (!buffersFilled) {
			return false;
		}
		alSourcef(sourceID, AL11.AL_SEC_OFFSET, position - renderedSeconds);
		if (music.isPlaying) {
			alSourcePlay(sourceID);
		}
		return true;
	}
	
	/**
	 * @return true if stream is completed, false otherwise.
	 */
	boolean updateBuffers(){
		final int sourceID = music.sourceID;
		
		boolean buffersEnded = false;
		int buffers = alGetSourcei(sourceID, AL_BUFFERS_PROCESSED);
		while (buffers-- > 0) {
			int bufferID = alSourceUnqueueBuffers(sourceID);
			if (bufferID == AL_INVALID_VALUE) break;
			if (renderedSecondsQueue.size > 0) renderedSeconds = renderedSecondsQueue.pop();
			if (buffersEnded) continue;
			if (fill(bufferID))
				alSourceQueueBuffers(sourceID, bufferID);
			else
				buffersEnded = true;
		}
		if (buffersEnded && alGetSourcei(sourceID, AL_BUFFERS_QUEUED) == 0) {
			return true;
		}
		// A buffer underflow will cause the source to stop.
		if (music.isPlaying && alGetSourcei(sourceID, AL_SOURCE_STATE) != AL_PLAYING) alSourcePlay(sourceID);
		return false;
	}

	private boolean fill (int bufferID) {
		((Buffer) tempBuffer).clear();
		int length = music.read(tempBytes);
		if (length <= 0) {
			if (music.isLooping) {
				music.loop();
				length = music.read(tempBytes);
				if (length <= 0) return false;
				if (renderedSecondsQueue.size > 0) {
					renderedSecondsQueue.set(0, 0);
				}
			} else
				return false;
		}
		float previousLoadedSeconds = renderedSecondsQueue.size > 0 ? renderedSecondsQueue.first() : 0;
		float currentBufferSeconds = maxSecondsPerBuffer * (float)length / (float)bufferSize;
		renderedSecondsQueue.insert(0, previousLoadedSeconds + currentBufferSeconds);

		((Buffer) tempBuffer.put(tempBytes, 0, length)).flip();
		alBufferData(bufferID, music.format, tempBuffer, music.sampleRate);
		return true;
	}
	
	boolean prefillBuffers(){
		
		if (buffers == null) {
			buffers = BufferUtils.createIntBuffer(OpenALMusicAsync.bufferCount);
			alGenBuffers(buffers);
			int errorCode = alGetError();
			if (errorCode != AL_NO_ERROR)
				throw new GdxRuntimeException("Unable to allocate audio buffers. AL Error: " + errorCode);
		}
		
		boolean buffersFilled = false; // Check if there's anything to actually play.
		for (int i = 0; i < bufferCount; i++) {
			int bufferID = buffers.get(i);
			if (!fill(bufferID)) break;
			buffersFilled = true;
			alSourceQueueBuffers(music.sourceID, bufferID);
		}
		if (alGetError() != AL_NO_ERROR) {
			return false;
		}
		return buffersFilled;
	}

	@Override
	public void dispose () {
		if (buffers == null) return;
		alDeleteBuffers(buffers);
		buffers = null;
	}

}

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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL11;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT;

/** @author Nathan Sweet */
public abstract class OpenALMusic implements Music {

	final OpenALLwjgl3Audio audio;
	int sourceID = -1;
	int format, sampleRate;
	boolean isLooping, isPlaying;
	private float volume = 1;
	private float pan = 0;

	protected final FileHandle file;

	private OnCompletionListener onCompletionListener;
	
	final OpenALMusicAsync async;
	
	boolean disposing;
	boolean seeking;

	public OpenALMusic (OpenALLwjgl3Audio audio, FileHandle file) {
		this.audio = audio;
		this.file = file;
		this.onCompletionListener = null;
		this.async = new OpenALMusicAsync(this);
	}

	protected void setup (int channels, int sampleRate) {
		this.format = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
		this.sampleRate = sampleRate;
		async.setup(channels, sampleRate);
	}

	public void play () {
		if (audio.noDevice) return;
		if (sourceID == -1) {
			sourceID = audio.obtainSource(true);
			if (sourceID == -1) return;

			alSourcei(sourceID, AL_DIRECT_CHANNELS_SOFT, AL_TRUE);
			alSourcei(sourceID, AL_LOOPING, AL_FALSE);
			setPan(pan, volume);
			
			audio.startPlayback(this);

			
		}
		// TODO to be called on the thread ?
		if (!isPlaying) {
			alSourcePlay(sourceID);
			isPlaying = true;
		}
	}
	
	public void stop () {
		if (audio.noDevice) return;
		if (sourceID == -1) return;
		isPlaying = false;
		
		audio.stop(this);
	}
	
	void stopInternal(){
		audio.freeSource(sourceID);
		sourceID = -1;
		isPlaying = false;
		if(disposing){
			async.dispose();
			disposing = false;
		}
	}

	public void pause () {
		if (audio.noDevice) return;
		if (sourceID != -1) alSourcePause(sourceID);
		isPlaying = false;
	}

	public boolean isPlaying () {
		if (audio.noDevice) return false;
		if (sourceID == -1) return false;
		return isPlaying;
	}

	public void setLooping (boolean isLooping) {
		this.isLooping = isLooping;
	}

	public boolean isLooping () {
		return isLooping;
	}

	public void setVolume (float volume) {
		this.volume = volume;
		if (audio.noDevice) return;
		if (sourceID != -1) alSourcef(sourceID, AL_GAIN, volume);
	}

	public float getVolume () {
		return this.volume;
	}

	public void setPan (float pan, float volume) {
		this.volume = volume;
		this.pan = pan;
		if (audio.noDevice) return;
		if (sourceID == -1) return;
		alSource3f(sourceID, AL_POSITION, MathUtils.cos((pan - 1) * MathUtils.HALF_PI), 0,
			MathUtils.sin((pan + 1) * MathUtils.HALF_PI));
		alSourcef(sourceID, AL_GAIN, volume);
	}

	public void setPosition (float position) {
		if (audio.noDevice) return;
		if (sourceID == -1) return;
		
		async.positionToSeek = position;
		if(!seeking){
			seeking = true;
			audio.seek(this);
		}
	}
	
	public float getPosition () {
		if (audio.noDevice) return 0;
		if (sourceID == -1) return 0;
		if(seeking) return async.positionToSeek;
		return async.renderedSeconds + alGetSourcef(sourceID, AL11.AL_SEC_OFFSET);
	}

	/** Fills as much of the buffer as possible and returns the number of bytes filled. Returns <= 0 to indicate the end of the
	 * stream. 
	 * This method is only called by the audio thread. */
	abstract public int read (byte[] buffer);

	/** Resets the stream to the beginning. 
	 * This method is only called by the audio thread. */
	abstract public void reset ();

	/** By default, does just the same as reset(). Used to add special behaviour in Ogg.Music. 
	 * This method is only called by the audio thread. */
	protected void loop () {
		reset();
	}

	public int getChannels () {
		return format == AL_FORMAT_STEREO16 ? 2 : 1;
	}

	public int getRate () {
		return sampleRate;
	}

	void onFinished(){
		stopInternal();
		if (onCompletionListener != null) onCompletionListener.onCompletion(this);
	}
	
	public void dispose () {
		if (audio.noDevice) return;
		onCompletionListener = null;
		
		disposing = true;
	}

	public void setOnCompletionListener (OnCompletionListener listener) {
		onCompletionListener = listener;
	}

	public int getSourceId () {
		return sourceID;
	}
}

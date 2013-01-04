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

package com.badlogic.gdx.backends.joal;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.openal.*;
import com.jogamp.openal.util.ALut;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.ObjectMap;

/** @author Nathan Sweet */
public class OpenALAudio implements Audio {
	
	static {
      ALut.alutInit();
   }
	private IntBuffer ib = IntBuffer.allocate(1);
	private final int deviceBufferSize;
	private final int deviceBufferCount;
	private IntArray idleSources, allSources;
	private LongMap<Integer> soundIdToSource;
	private IntMap<Long> sourceToSoundId;
	private long nextSoundId = 0;
	private ObjectMap<String, Class<? extends OpenALSound>> extensionToSoundClass = new ObjectMap();
	private ObjectMap<String, Class<? extends OpenALMusic>> extensionToMusicClass = new ObjectMap();

	Array<OpenALMusic> music = new Array(false, 1, OpenALMusic.class);
	boolean noDevice = false;
	private ALC alc;
   private AL al;

	public OpenALAudio () {
		this(16, 9, 512);
	}

	public OpenALAudio (int simultaneousSources, int deviceBufferCount, int deviceBufferSize) {
		this.deviceBufferSize = deviceBufferSize;
		this.deviceBufferCount = deviceBufferCount;

		registerSound("ogg", Ogg.Sound.class);
		registerMusic("ogg", Ogg.Music.class);
		registerSound("wav", Wav.Sound.class);
		registerMusic("wav", Wav.Music.class);
		registerSound("mp3", Mp3.Sound.class);
		registerMusic("mp3", Mp3.Music.class);

		try {
			alc = ALFactory.getALC();
         al = ALFactory.getAL();
		} catch (ALException ex) {
			noDevice = true;
			ex.printStackTrace();
			return;
		}

		allSources = new IntArray(false, simultaneousSources);
		IntBuffer channelsNioBuffer = Buffers.newDirectIntBuffer(simultaneousSources);
		al.alGenSources(simultaneousSources, channelsNioBuffer);
		for (int i = 0; i < simultaneousSources; i++) {
			int sourceID = channelsNioBuffer.get(i);
			if (sourceID == 0) break;
			allSources.add(sourceID);
		}
		idleSources = new IntArray(allSources);
		soundIdToSource = new LongMap<Integer>();
		sourceToSoundId = new IntMap<Long>();

		FloatBuffer orientation = (FloatBuffer)Buffers.newDirectFloatBuffer(6)
			.put(new float[] {0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).flip();
		al.alListenerfv(ALConstants.AL_ORIENTATION, orientation);
		FloatBuffer velocity = (FloatBuffer)Buffers.newDirectFloatBuffer(3).put(new float[] {0.0f, 0.0f, 0.0f}).flip();
		al.alListenerfv(ALConstants.AL_VELOCITY, velocity);
		FloatBuffer position = (FloatBuffer)Buffers.newDirectFloatBuffer(3).put(new float[] {0.0f, 0.0f, 0.0f}).flip();
		al.alListenerfv(ALConstants.AL_POSITION, position);
	}
	
	public AL getAL(){
		return(al);
	}

	public void registerSound (String extension, Class<? extends OpenALSound> soundClass) {
		if (extension == null) throw new IllegalArgumentException("extension cannot be null.");
		if (soundClass == null) throw new IllegalArgumentException("soundClass cannot be null.");
		extensionToSoundClass.put(extension, soundClass);
	}

	public void registerMusic (String extension, Class<? extends OpenALMusic> musicClass) {
		if (extension == null) throw new IllegalArgumentException("extension cannot be null.");
		if (musicClass == null) throw new IllegalArgumentException("musicClass cannot be null.");
		extensionToMusicClass.put(extension, musicClass);
	}

	public OpenALSound newSound (FileHandle file) {
		if (file == null) throw new IllegalArgumentException("file cannot be null.");
		Class<? extends OpenALSound> soundClass = extensionToSoundClass.get(file.extension());
		if (soundClass == null) throw new GdxRuntimeException("Unknown file extension for sound: " + file);
		try {
			return soundClass.getConstructor(new Class[] {OpenALAudio.class, FileHandle.class}).newInstance(this, file);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error creating sound " + soundClass.getName() + " for file: " + file, ex);
		}
	}

	public OpenALMusic newMusic (FileHandle file) {
		if (file == null) throw new IllegalArgumentException("file cannot be null.");
		Class<? extends OpenALMusic> musicClass = extensionToMusicClass.get(file.extension());
		if (musicClass == null) throw new GdxRuntimeException("Unknown file extension for music: " + file);
		try {
			return musicClass.getConstructor(new Class[] {OpenALAudio.class, FileHandle.class}).newInstance(this, file);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error creating music " + musicClass.getName() + " for file: " + file, ex);
		}
	}

	int obtainSource (boolean isMusic) {
		if (noDevice) return 0;
		for (int i = 0, n = idleSources.size; i < n; i++) {
			int sourceId = idleSources.get(i);
			al.alGetSourcei(sourceId, ALConstants.AL_SOURCE_STATE, ib);
			int state = ib.get(0);
			if (state != ALConstants.AL_PLAYING && state != ALConstants.AL_PAUSED) {
				if (isMusic) {
					idleSources.removeIndex(i);
				} else {
					if (sourceToSoundId.containsKey(sourceId)) {
						long soundId = sourceToSoundId.get(sourceId);
						sourceToSoundId.remove(sourceId);
						soundIdToSource.remove(soundId);
					}

					long soundId = nextSoundId++;
					sourceToSoundId.put(sourceId, soundId);
					soundIdToSource.put(soundId, sourceId);
				}
				al.alSourceStop(sourceId);
				al.alSourcei(sourceId, ALConstants.AL_BUFFER, 0);
				al.alSourcef(sourceId, ALConstants.AL_GAIN, 1);
				al.alSourcef(sourceId, ALConstants.AL_PITCH, 1);
				al.alSource3f(sourceId, ALConstants.AL_POSITION, 0, 0, 1f);
				return sourceId;
			}
		}
		return -1;
	}

	void freeSource (int sourceID) {
		if (noDevice) return;
		al.alSourceStop(sourceID);
		al.alSourcei(sourceID, ALConstants.AL_BUFFER, 0);
		if (sourceToSoundId.containsKey(sourceID)) {
			long soundId = sourceToSoundId.remove(sourceID);
			soundIdToSource.remove(soundId);
		}
		idleSources.add(sourceID);
	}

	void freeBuffer (int bufferID) {
		if (noDevice) return;
		for (int i = 0, n = idleSources.size; i < n; i++) {
			int sourceID = idleSources.get(i);
			al.alGetSourcei(sourceID, ALConstants.AL_BUFFER, ib);
			if (ib.get(0) == bufferID) {
				if (sourceToSoundId.containsKey(sourceID)) {
					long soundId = sourceToSoundId.remove(sourceID);
					soundIdToSource.remove(soundId);
				}
				al.alSourceStop(sourceID);
				al.alSourcei(sourceID, ALConstants.AL_BUFFER, 0);
			}
		}
	}

	void stopSourcesWithBuffer (int bufferID) {
		if (noDevice) return;
		for (int i = 0, n = idleSources.size; i < n; i++) {
			int sourceID = idleSources.get(i);
			al.alGetSourcei(sourceID, ALConstants.AL_BUFFER, ib);
			if (ib.get(0) == bufferID) {
				if (sourceToSoundId.containsKey(sourceID)) {
					long soundId = sourceToSoundId.remove(sourceID);
					soundIdToSource.remove(soundId);
				}
				al.alSourceStop(sourceID);
			}
		}
	}

	public void update () {
		if (noDevice) return;
		for (int i = 0; i < music.size; i++)
			music.items[i].update();
	}

	public long getSoundId (int sourceId) {
		if (!sourceToSoundId.containsKey(sourceId)) return -1;
		return sourceToSoundId.get(sourceId);
	}

	public void stopSound (long soundId) {
		if (!soundIdToSource.containsKey(soundId)) return;
		int sourceId = soundIdToSource.get(soundId);
		al.alSourceStop(sourceId);
	}

	public void setSoundGain (long soundId, float volume) {
		if (!soundIdToSource.containsKey(soundId)) return;
		int sourceId = soundIdToSource.get(soundId);
		al.alSourcef(sourceId, ALConstants.AL_GAIN, volume);
	}

	public void setSoundLooping (long soundId, boolean looping) {
		if (!soundIdToSource.containsKey(soundId)) return;
		int sourceId = soundIdToSource.get(soundId);
		al.alSourcei(sourceId, ALConstants.AL_LOOPING, looping ? ALConstants.AL_TRUE : ALConstants.AL_FALSE);
	}

	public void setSoundPitch (long soundId, float pitch) {
		if (!soundIdToSource.containsKey(soundId)) return;
		int sourceId = soundIdToSource.get(soundId);
		al.alSourcef(sourceId, ALConstants.AL_PITCH, pitch);
	}

	public void setSoundPan (long soundId, float pan, float volume) {
		if (!soundIdToSource.containsKey(soundId)) return;
		int sourceId = soundIdToSource.get(soundId);

		al.alSource3f(sourceId, ALConstants.AL_POSITION, MathUtils.cos((pan - 1) * MathUtils.PI / 2), 0,
			MathUtils.sin((pan + 1) * MathUtils.PI / 2));
		al.alSourcef(sourceId, ALConstants.AL_GAIN, volume);
	}

	public void dispose () {
		if (noDevice) return;
		for (int i = 0, n = allSources.size; i < n; i++) {
			int sourceID = allSources.get(i);
			al.alGetSourcei(sourceID, ALConstants.AL_SOURCE_STATE, ib);
			int state = ib.get(0);
			if (state != ALConstants.AL_STOPPED) al.alSourceStop(sourceID);
			ib.put(0, sourceID).rewind();
			al.alDeleteSources(ib.limit(), ib);
		}

		sourceToSoundId.clear();
		soundIdToSource.clear();

		//AL.destroy();
		/*while (AL.isCreated()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}*/
	}

	public AudioDevice newAudioDevice (int sampleRate, final boolean isMono) {
		if (noDevice) return new AudioDevice() {
			@Override
			public void writeSamples (float[] samples, int offset, int numSamples) {
			}

			@Override
			public void writeSamples (short[] samples, int offset, int numSamples) {
			}

			@Override
			public void setVolume (float volume) {
			}

			@Override
			public boolean isMono () {
				return isMono;
			}

			@Override
			public int getLatency () {
				return 0;
			}

			@Override
			public void dispose () {
			}
		};
		return new OpenALAudioDevice(this, sampleRate, isMono, deviceBufferSize, deviceBufferCount);
	}

	public AudioRecorder newAudioRecorder (int samplingRate, boolean isMono) {
		if (noDevice) return new AudioRecorder() {
			@Override
			public void read (short[] samples, int offset, int numSamples) {
			}

			@Override
			public void dispose () {
			}
		};
		return new JavaSoundAudioRecorder(samplingRate, isMono);
	}
}

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

package com.badlogic.gdx;

import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** This interface encapsulates the creation and management of audio resources. It allows you to get direct access to the audio
 * hardware via the {@link AudioDevice} and {@link AudioRecorder} interfaces, create sound effects via the {@link Sound} interface
 * and play music streams via the {@link Music} interface.
 * 
 * <p>
 * All resources created via this interface have to be disposed as soon as they are no longer used.
 * </p>
 * 
 * <p>
 * Note that all {@link Music} instances will be automatically paused when the {@link ApplicationListener#pause()} method is
 * called, and automatically resumed when the {@link ApplicationListener#resume()} method is called.
 * </p>
 * 
 * @author mzechner */
public interface Audio {
	/** Creates a new {@link AudioDevice} either in mono or stereo mode. The AudioDevice has to be disposed via its
	 * {@link AudioDevice#dispose()} method when it is no longer used.
	 * 
	 * @param samplingRate the sampling rate.
	 * @param isMono whether the AudioDevice should be in mono or stereo mode
	 * @return the AudioDevice
	 * 
	 * @throws GdxRuntimeException in case the device could not be created */
	public AudioDevice newAudioDevice (int samplingRate, boolean isMono);

	/** Creates a new {@link AudioRecorder}. The AudioRecorder has to be disposed after it is no longer used.
	 * 
	 * @param samplingRate the sampling rate in Hertz
	 * @param isMono whether the recorder records in mono or stereo
	 * @return the AudioRecorder
	 * 
	 * @throws GdxRuntimeException in case the recorder could not be created */
	public AudioRecorder newAudioRecorder (int samplingRate, boolean isMono);

	/** <p>
	 * Creates a new {@link Sound} which is used to play back audio effects such as gun shots or explosions. The Sound's audio data
	 * is retrieved from the file specified via the {@link FileHandle}. Note that the complete audio data is loaded into RAM. You
	 * should therefore not load big audio files with this methods. The current upper limit for decoded audio is 1 MB.
	 * </p>
	 * 
	 * <p>
	 * Currently supported formats are WAV, MP3 and OGG.
	 * </p>
	 * 
	 * <p>
	 * The Sound has to be disposed if it is no longer used via the {@link Sound#dispose()} method.
	 * </p>
	 * 
	 * @return the new Sound
	 * @throws GdxRuntimeException in case the sound could not be loaded */
	public Sound newSound (FileHandle fileHandle);

	/** Creates a new {@link Music} instance which is used to play back a music stream from a file. Currently supported formats are
	 * WAV, MP3 and OGG. The Music instance has to be disposed if it is no longer used via the {@link Music#dispose()} method.
	 * Music instances are automatically paused when {@link ApplicationListener#pause()} is called and resumed when
	 * {@link ApplicationListener#resume()} is called.
	 * 
	 * @param file the FileHandle
	 * @return the new Music or null if the Music could not be loaded
	 * @throws GdxRuntimeException in case the music could not be loaded */
	public Music newMusic (FileHandle file);
}

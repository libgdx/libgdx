/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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

/**
 * This interface encapsulates the creation and managment of audio resources.
 * It allows you to create direct access links to the audio hardware via
 * the {@link AudioDevice} interface, create sound effects via the {@link 
 * Sound} interface and music streams via the {@link Music} interface.
 * All resources created via this interface have to be disposed once no
 * longer used.
 * 
 * @author mzechner
 *
 */
public interface Audio 
{
	/**
	 * Creates a new {@link AudioDevice} either in 44.1khz mono
	 * or stereo mode. The AudioDevice has to be disposed via its
	 * {@link AudioDevice.dispose()} method when it is no longer used.
	 * 
	 * @param isMono whether the AudioDevice should be in mono or stereo mode
	 * @return the AudioDevice
	 */
	public AudioDevice newAudioDevice( boolean isMono );
	
	/**
	 * Creates a new {@link AudioRecorder}. The AudioDevice has to be disposed
	 * after it is no longer used.
	 * 
	 * @param samplingRate the sampling rate in Herz
	 * @param isMono whether the recorder records in mono or stereo
	 * @return the AudioRecorder
	 */
	public AudioRecorder newAudioRecoder( int samplingRate, boolean isMono );
	
	/**
	 * Creates a new {@link Sound} which is used to playback audio effects such
	 * as gun shots or explosions. The Sound's audio data is retrieved from the
	 * file specified via the {@link FileHandle}. Note that the complete audio
	 * data is loaded into RAM. You should therefore not load big audio files 
	 * with this methods. Currently supported formats are WAV, MP3 and OGG. The
	 * Sound has to be disposed if it is no longer used via the {@link Sound.dispose()}
	 * method.
	 * 
	 * @param file the FileHandle to the audio file
	 * @return the new Sound or null if the Sound could not be loaded.
	 */
	public Sound newSound( FileHandle fileHandle );
	
	/**
	 * Creates a new {@link Music} instance which is used to playback a music
	 * stream from a file. Currently supported formats are WAV, MP3 and OGG.
	 * The Music has to be disposed if it is no longer used via the {@link Music.dispose()}
	 * method.
	 * 
	 * @param file the FileHandle 
	 * @return the new Music or null if the Music could not be loaded.
	 */
	public Music newMusic( FileHandle file );
}

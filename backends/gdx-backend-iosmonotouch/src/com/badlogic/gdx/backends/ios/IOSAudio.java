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
package com.badlogic.gdx.backends.ios;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import cli.MonoTouch.AVFoundation.AVAudioPlayer;
import cli.MonoTouch.Foundation.NSData;
import cli.MonoTouch.Foundation.NSError;
import cli.MonoTouch.Foundation.NSUrl;

public class IOSAudio implements Audio {

	@Override
	public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
		return null;
	}

	@Override
	public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
		return null;
	}

	@Override
	public Sound newSound(FileHandle fileHandle) {
		// verify file format
		if (fileHandle.extension().equalsIgnoreCase("ogg")) {  
			// Ogg is not supported on iOS (return a sound object that does nothing)
			Gdx.app.error("IOSAudio", "Audio format .ogg is not supported on iOS. Cannot load: " + fileHandle.path());
			return new Sound() {
				@Override
				public long play () {
					return 0;
				}
				@Override
				public long play (float volume) {
					return 0;
				}
				@Override
				public long play (float volume, float pitch, float pan) {
					return 0;
				}
				@Override
				public long loop () {
					return 0;
				}
				@Override
				public long loop (float volume) {
					return 0;
				}
				@Override
				public long loop (float volume, float pitch, float pan) {
					return 0;
				}
				@Override
				public void stop () {
				}
				@Override
				public void dispose () {
				}
				@Override
				public void stop (long soundId) {
				}
				@Override
				public void setLooping (long soundId, boolean looping) {
				}
				@Override
				public void setPitch (long soundId, float pitch) {
				}
				@Override
				public void setVolume (long soundId, float volume) {
				}
				@Override
				public void setPan (long soundId, float pan, float volume) {
				}				
			};
		}
				
		// create audio player - from byte array
		NSError[] error = new NSError[1];
		NSData data = NSData.FromArray(fileHandle.readBytes());
		AVAudioPlayer player = AVAudioPlayer.FromData(data, error);
		if (error[0] == null) {
			// no error: return the music object
			return new IOSSound(player);
		}
		else {
			// throw an exception
			throw new GdxRuntimeException("Error opening file at " + fileHandle.path() + ": " + error[0].ToString());
		}
	}

	@Override
	public Music newMusic(FileHandle fileHandle) {
		// verify file format
		if (fileHandle.extension().equalsIgnoreCase("ogg")) {  
			// Ogg is not supported on iOS (return a music object that does nothing)
			Gdx.app.error("IOSAudio", "Audio format .ogg is not supported on iOS. Cannot load: " + fileHandle.path());
			return new Music() {
				@Override
				public void play () {
				}
				@Override
				public void pause () {
				}
				@Override
				public void stop () {
				}
				@Override
				public boolean isPlaying () {
					return false;
				}
				@Override
				public void setLooping (boolean isLooping) {
				}
				@Override
				public boolean isLooping () {
					return false;
				}
				@Override
				public void setVolume (float volume) {
				}
				@Override
				public float getPosition () {
					return 0;
				}
				@Override
				public void dispose () {
				}				
			};
		}
		
		// create audio player - from file path
		NSError[] error = new NSError[1];
		AVAudioPlayer player = AVAudioPlayer.FromUrl(NSUrl.FromFilename(fileHandle.path()), error);
		if (error[0] == null) {
			// no error: return the music object
			return new IOSMusic(player);
		}
		else {
			// throw an exception
			throw new GdxRuntimeException("Error opening file at " + fileHandle.path() + ": " + error[0].ToString());
		}
	}
}
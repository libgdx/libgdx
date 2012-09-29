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

	/**
	 * Let's verify the file format. 
	 * 
	 * @param fileHandle  The file to load.
	 * @throws GdxRuntimeException  If we are using a OGG file (not supported under iOS).
	 */
	private void verify(FileHandle fileHandle) {
		if (fileHandle.extension().equalsIgnoreCase("ogg")) {  
			// Ogg is not supported on iOS (return a sound object that does nothing)
			throw new GdxRuntimeException("Audio format .ogg is not supported on iOS. Cannot load: " + fileHandle.path());
		}
	}
	
	/**
	 * Returns a new sound object. We are playing from memory, a.k.a. suited for short 
	 * sound FXs.
	 * 
	 * @return  The new sound object.
	 * @throws GdxRuntimeException  If we are unable to load the file for some reason.
	 */
	@Override
	public Sound newSound(FileHandle fileHandle) {
		// verify file format (make sure we don't have an OGG file)
		verify(fileHandle);
				
		// create audio player - from byte array
		NSData data = NSData.FromArray(fileHandle.readBytes());
	   return new IOSSound(data);
	}

	/**
	 * Returns a new music object. We are playing directly from file, a.k.a. suited for
	 * background music.
	 * 
	 * @return  The new music object.
	 * @throws GdxRuntimeException  If we are unable to load the file for some reason.
	 */
	@Override
	public Music newMusic(FileHandle fileHandle) {
		// verify file format (make sure we don't have an OGG file)
		verify(fileHandle);
		
		// create audio player - from file path
		NSError[] error = new NSError[1];
		AVAudioPlayer player = AVAudioPlayer.FromUrl(NSUrl.FromFilename(fileHandle.path()), error);
		if (error[0] == null) {
			// no error: return the music object
			return new IOSMusic(player);
		}
		else {
			// throw an exception
			throw new GdxRuntimeException("Error opening music file at " + fileHandle.path() + ": " + error[0].ToString());
		}
	}
}
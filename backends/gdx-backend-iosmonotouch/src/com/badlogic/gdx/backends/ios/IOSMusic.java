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

import cli.MonoTouch.AVFoundation.AVAudioPlayer;
import cli.MonoTouch.AVFoundation.AVAudioPlayerDelegate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/** A music player, suitable for background music. Supports MP3 and WAV files which are played via hardware on iOS.
 * <p>
 * Limitations: does not play OGG.
 * 
 * @author noblemaster */
public class IOSMusic implements Music {

	private AVAudioPlayer player;
	private float volume = 1f;
	
	private AudioDelegate audioDelegate;

	public IOSMusic (AVAudioPlayer player) {
		this.player = player;
		audioDelegate = new AudioDelegate();
		this.player.set_Delegate(audioDelegate);
	}

	@Override
	public void play () {
		player.Play();
	}

	@Override
	public void pause () {
		player.Pause();
	}

	@Override
	public void stop () {
		player.Stop();
		// We need to set currentTime to 0 in order to restart from the beginning the next time, more info at
		// http://developer.apple.com/library/ios/DOCUMENTATION/AVFoundation/Reference/AVFoundationFramework/_index.html#//apple_ref/occ/instm/AVAudioPlayer/stop
		player.set_CurrentTime(0);
	}

	@Override
	public boolean isPlaying () {
		return player.get_Playing();
	}

	@Override
	public void setLooping (boolean isLooping) {
		player.set_NumberOfLoops(isLooping ? -1 : 0); // Note: -1 for looping!
	}

	@Override
	public boolean isLooping () {
		return player.get_NumberOfLoops() == -1; // Note: -1 for looping!
	}

	@Override
	public void setVolume (float volume) {
		player.set_Volume(volume);
		this.volume = volume;
	}
	
	@Override
	public float getVolume () {
		return volume;
	}
	
	@Override
	public void setPan (float pan, float volume) {
		player.set_Pan(pan);
		player.set_Volume(volume);
		this.volume = volume;
	}

	@Override
	public float getPosition () {
		return (float)(player.get_CurrentTime() * 1000); // Note: player returns seconds => x1000 to convert to millis!
	}

	@Override
	public void dispose () {
		stop();
		player.Dispose();
		player = null;
		audioDelegate = null;
	}
	
	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		audioDelegate.onCompletionListener = listener;
	}
	
	private class AudioDelegate extends AVAudioPlayerDelegate {
		
		public OnCompletionListener onCompletionListener;
		
		public AudioDelegate () {
			onCompletionListener = null;
		}
		
		@Override
		public void FinishedPlaying(AVAudioPlayer player, boolean successful) {
			if (onCompletionListener != null) {
				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run () {
						onCompletionListener.onCompletion(IOSMusic.this);
					}
				});
			}
		}
	}
}
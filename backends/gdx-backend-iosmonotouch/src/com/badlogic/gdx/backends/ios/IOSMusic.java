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

import com.badlogic.gdx.audio.Music;

public class IOSMusic implements Music {

	private AVAudioPlayer player;
	
	
	public IOSMusic(AVAudioPlayer player) {
		this.player = player;
	}
	
	@Override
	public void play() {
		player.Play();
	}

	@Override
	public void pause() {
		player.Pause();
	}

	@Override
	public void stop() {
		player.Stop();
	}

	@Override
	public boolean isPlaying() {
		return player.get_Playing();
	}

	@Override
	public void setLooping(boolean isLooping) {
		// TODO Auto-generated method stub		
	}

	@Override
	public boolean isLooping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVolume(float volume) {
		player.set_Volume(volume);
	}

	@Override
	public float getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dispose() {
		player.Dispose();
	}
}
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

import com.badlogic.gdx.audio.Sound;

public class IOSSound implements Sound {

	private AVAudioPlayer player;

	
	public IOSSound(AVAudioPlayer player) {
		this.player = player;
	}

	@Override
	public long play() {
		return play(1.0f);
	}

	@Override
	public long play(float volume) {
		return play(volume, 1.0f, 0.5f);
	}

	@Override
	public long play(float volume, float pitch, float pan) {
		player.set_Volume(volume);
		player.set_Pan(pan);
		player.Play();
		
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long loop() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long loop(float volume) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long loop(float volume, float pitch, float pan) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void stop() {
		player.Stop();
	}

	@Override
	public void dispose() {
		player.Dispose();
	}

	@Override
	public void stop(long soundId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLooping(long soundId, boolean looping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPitch(long soundId, float pitch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVolume(long soundId, float volume) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPan(long soundId, float pan, float volume) {
		// TODO Auto-generated method stub
		
	}
}
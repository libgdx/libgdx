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

import java.util.ArrayList;
import java.util.List;

import cli.MonoTouch.AVFoundation.AVAudioPlayer;
import cli.MonoTouch.Foundation.NSData;
import cli.MonoTouch.Foundation.NSError;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * A music player, suitable for short sound effects. Effects are played from
 * memory. Supports MP3 and WAV files which are played via hardware on iOS.
 * <p>
 * Limitations: does not play OGG. Does not support pitched audio (i.e. variable 
 * rate playback).
 * 
 * @author noblemaster
 */
public class IOSSound implements Sound {

	/** The maximum number of players to instantiate to support simultaneous playback. Bigger? */
	private static final int MAX_PLAYERS = 10;
	
	/** Our sound players. More than one to support simultaneous playback. */
	private AVAudioPlayer[] players;

	
	/**
	 * Creates a new sound object. We are creating several AVAudioPlayer objects to
	 * support synchronous playback. A single AVAudioPlayer can only play one sound 
	 * at a time (non-simultaneous).
	 * 
	 * @param data  The audio data stored in memory.
	 * @throws GdxRuntimeException  If we are unable to create the player for some reason.
	 */
	public IOSSound(NSData data) {
		// create players for playback (more than 1 to support simultaneous playback)
		NSError[] error = new NSError[1];
		players = new AVAudioPlayer[MAX_PLAYERS];
		for (int i = 0; i < players.length; i++) {
	      players[i] = AVAudioPlayer.FromData(data, error);
	      
	      // check for errors
			if (error[0] != null) {
				// error creating the player (maybe file missing? unsupported format?)
				throw new GdxRuntimeException("Error creating audio player (index: " + i + "): " + error[0].ToString());
			}
		}
	}

	/**
	 * Let's find a player that isn't currently playing.
	 * 
	 * @return  The index of the player or -1 if none is available.
	 */
	private int findAvailablePlayer() {
		for (int i = 0; i < players.length; i++) {
			if (!players[i].get_Playing()) {
				return i;
			}
		}
		
		// all are busy playing... :/
		return -1;
	}
	
	@Override
	public long play() {
		return play(1.0f);
	}

	@Override
	public long play(float volume) {
		return play(volume, 1.0f, 0.0f);
	}

	@Override
	public long play(float volume, float pitch, float pan) {
		return play(false, volume, pitch, pan);
	}
	
	/**
	 * Our actual player. Will be call both for looping and non-looping 
	 * playback.
    *
	 * @return  The sound index or -1 if none was available for playback.
	 */
   private int play(boolean looping, float volume, float pitch, float pan) {
		int soundId = findAvailablePlayer();
		if (soundId >= 0) {
			AVAudioPlayer player = players[soundId];
			player.set_NumberOfLoops(looping ? -1 : 0);  // Note: -1 for looping!
			player.set_Volume(volume);
			player.set_Pan(pan);
			player.Play();
		}
		
		// and return the index/id of the player
		return soundId;
	}

	@Override
	public long loop() {
		return loop(1.0f);
	}

	@Override
	public long loop(float volume) {
		return loop(volume, 1.0f, 0.0f);
	}

	@Override
	public long loop(float volume, float pitch, float pan) {
		return play(true, volume, pitch, pan);
	}

	@Override
	public void stop() {
		// stop all players
		for (int i = 0; i < players.length; i++) {
			players[i].Stop();
		}
	}

	@Override
	public void dispose() {
		// dispose all players
		for (int i = 0; i < players.length; i++) {
			players[i].Dispose();
			players[i] = null;
		}
	}

	@Override
	public void stop(long soundId) {
		if (soundId >= 0) {
			players[(int)soundId].Stop();
		}
	}

	@Override
	public void setLooping(long soundId, boolean looping) {
		if (soundId >= 0) {
			players[(int)soundId].set_NumberOfLoops(looping ? -1 : 0);  // Note: -1 for looping!
		}
	}

	@Override
	public void setPitch(long soundId, float pitch) {
		if (soundId >= 0) {
			// NOTE: It's odd, AVAudioPlayer supports variable rate playing, but is not
			// available via MonoTouch!? Let's put out a warning...
			Gdx.app.debug("IOSSound", "Warning: setting a pitch not supported on iOS.");
		}
	}

	@Override
	public void setVolume(long soundId, float volume) {
		if (soundId >= 0) {
			players[(int)soundId].set_Volume(volume);
		}
	}

	@Override
	public void setPan(long soundId, float pan, float volume) {
		if (soundId >= 0) {
			players[(int)soundId].set_Pan(pan);
			players[(int)soundId].set_Volume(volume);
		}
	}
}
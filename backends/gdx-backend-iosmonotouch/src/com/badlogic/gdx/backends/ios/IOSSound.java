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

import java.util.concurrent.LinkedBlockingQueue;
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
 * 
 * FIXME rewrite using OpenAL!
 */
public class IOSSound implements Sound {

	/** The maximum number of players to instantiate to support simultaneous playback. Bigger? */
	private static final int MAX_PLAYERS = 8;
	
	/** Our sound players. More than one to support simultaneous playback. */
	private AVAudioPlayer[] players;
	/** The next player we think should be available for play - we circling through them to find a free one. */
	private int playerIndex;
	
	// one single thread will play sounds outside the rendering low (otherwise our FPS drops!)
	static class PlayThread extends Thread {
		@Override
		public void run () {
			Gdx.app.debug("IOSSound", "Sound player is running.");
			
			// our play loop which will continue as long as we are the active thread
			try {
				while(true) {
					AVAudioPlayer player = playQueue.take();
					player.Play();
				}
			} catch(InterruptedException e) {
				// Exit player
				Gdx.app.debug("IOSSound", "Sound player exiting.");
			}
		}		
	}
	private static PlayThread playThread = null;
	private static int soundCounter = 0;
	static final LinkedBlockingQueue<AVAudioPlayer> playQueue = new LinkedBlockingQueue<AVAudioPlayer>();
	
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
		playerIndex = 0;
		
		// create the play thread if it doesn't exist yet
		synchronized(IOSSound.class) {
			soundCounter++;
			
			// create play thread as needed: plays sounds outside the rendering loop (smoother rendering)
			if (playThread == null) {
				playThread = new PlayThread();
				playThread.setPriority(Thread.MIN_PRIORITY);
				playThread.start();
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
			int index = (playerIndex + i) % players.length;
			if (!players[index].get_Playing()) {
				// point to the next likely free player
				playerIndex = (index + 1) % players.length; 
				
				// return the free player
				return index;
			}
		}
		
		// all are busy playing, stop the next sound in the queue and reuse it
		int index = playerIndex % players.length;
		playerIndex = (index + 1) % players.length;
		return index;
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
			player.Stop();
			player.set_NumberOfLoops(looping ? -1 : 0);  // Note: -1 for looping!
			player.set_Volume(volume);
			player.set_Pan(pan);
			
			// we let the thread play our song as not to impact rendering performance (FPS)
			playQueue.offer(player);
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
		// We clear the player queue so no song is player after call to stop!
		playQueue.clear();
		
		// stop all players
		for (int i = 0; i < players.length; i++) {
			players[i].Stop();
		}
	}

	@Override
	public void dispose() {
		// dispose play thread if no more sounds are available
		synchronized (IOSSound.class) {
			soundCounter--;
			
			// dispose all players
			stop();
			for (int i = 0; i < players.length; i++) {
				players[i].Dispose();
			}
			players = null;
			
			// no more sounds?
			if (soundCounter == 0) {
				playThread.interrupt();
			}
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
			// FIXME implement this by figuring out how to make MonoTouch support this...
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

	@Override
	public void setPriority (long soundId, int priority) {
		// FIXME
	}
}
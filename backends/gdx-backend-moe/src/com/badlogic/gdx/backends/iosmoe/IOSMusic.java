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

package com.badlogic.gdx.backends.iosmoe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.iosmoe.objectal.OALAudioTrack;
import apple.avfoundation.AVAudioPlayer;
import apple.avfoundation.protocol.AVAudioPlayerDelegate;
import org.moe.natj.objc.ObjCRuntime;

public class IOSMusic implements Music {

	private final OALAudioTrack track;
	OnCompletionListener onCompletionListener;

	public IOSMusic (OALAudioTrack track) {
		this.track = track;
		AVAudioPlayerDelegate delegate = new AVAudioPlayerDelegate() {
			@Override
			public void audioPlayerDidFinishPlayingSuccessfully (AVAudioPlayer player, boolean flag) {
				final OnCompletionListener listener = onCompletionListener;
				if (onCompletionListener != null) {
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run () {
							listener.onCompletion(IOSMusic.this);
						}
					});
				}
			}
		};
		this.track.setDelegate(delegate);
	}

	@Override
	public void play () {
		if (track.paused()) {
			track.setPaused(false);
		} else if (!track.playing()) {
			track.play();
		}
	}

	@Override
	public void pause () {
		if (track.playing()) {
			track.setPaused(true);
		}
	}

	@Override
	public void stop () {
		track.stop();
	}

	@Override
	public boolean isPlaying () {
		return track.playing() && !track.paused();
	}

	@Override
	public void setLooping (boolean isLooping) {
		track.setNumberOfLoops(isLooping ? -1 : 0);
	}

	@Override
	public boolean isLooping () {
		return track.numberOfLoops() == -1;
	}

	@Override
	public void setVolume (float volume) {
		track.setVolume(volume);
	}

	@Override
	public void setPosition (float position) {
		track.setCurrentTime(position);
	}

	@Override
	public float getPosition () {
		return (float)(track.currentTime());
	}

	@Override
	public void dispose () {
		track.clear();
		ObjCRuntime.disposeObject(this);
	}

	@Override
	public float getVolume () {
		return track.volume();
	}

	@Override
	public void setPan (float pan, float volume) {
		track.setPan(pan);
		track.setVolume(volume);
	}

	@Override
	public void setOnCompletionListener (OnCompletionListener listener) {
		this.onCompletionListener = listener;
	}

}

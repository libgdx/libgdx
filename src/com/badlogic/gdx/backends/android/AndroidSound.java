package com.badlogic.gdx.backends.android;

import android.media.AudioManager;
import android.media.SoundPool;

import com.badlogic.gdx.Sound;

public class AndroidSound implements Sound 
{
	final SoundPool soundPool;
	final AudioManager manager;
	final int soundId;
	
	public AndroidSound( SoundPool pool, AudioManager manager, int soundId )
	{
		this.soundPool = pool;
		this.manager = manager;
		this.soundId = soundId;
	}

	@Override
	public void dispose() {		
	}

	@Override
	public void play() {
		int streamVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		soundPool.play( soundId, streamVolume, streamVolume, 1, 0, 1 );
	}
	
}

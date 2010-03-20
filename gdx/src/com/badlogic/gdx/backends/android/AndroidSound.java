package com.badlogic.gdx.backends.android;

import android.media.AudioManager;
import android.media.SoundPool;

import com.badlogic.gdx.audio.Sound;

final class AndroidSound implements Sound
{
	final SoundPool soundPool;
	final AudioManager manager;
	final int soundId;
	
	AndroidSound( SoundPool pool, AudioManager manager, int soundId )
	{		
		this.soundPool = pool;
		this.manager = manager;
		this.soundId = soundId;
	}
	
	@Override
	public void dispose() 
	{	
		soundPool.unload( soundId );
	}

	@Override
	public void play() 
	{	
		play( 1 );
	}

	@Override
	public void play(float volume) 
	{	
		int streamVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		soundPool.play( soundId, streamVolume * volume, streamVolume * volume, 1, 0, 1 );
	}
	
}

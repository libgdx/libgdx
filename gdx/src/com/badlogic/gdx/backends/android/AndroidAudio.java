package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

/**
 * An implementation of the {@link Audio} interface for Android.
 * 
 * @author mzechner
 *
 */
final class AndroidAudio implements Audio
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AudioDevice newAudioDevice(boolean isMono) 
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Music newMusic(FileHandle file) 
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sound newSound(FileHandle file) 
	{
		return null;
	}
}

package com.badlogic.gdx.backends.android;

import java.io.FileDescriptor;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

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
	public Music newMusic(FileDescriptor fileDescriptor) 
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sound newSound(FileDescriptor fileDescriptor) 
	{
		return null;
	}
}

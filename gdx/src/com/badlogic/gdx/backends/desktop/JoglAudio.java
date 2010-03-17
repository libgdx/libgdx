package com.badlogic.gdx.backends.desktop;

import java.io.FileDescriptor;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * An implementation of the {@link Audio} interface for the desktop.
 * 
 * @author mzechner
 *
 */
final class JoglAudio implements Audio
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public AudioDevice newAudioDevice(boolean isMono) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Music newMusic(FileDescriptor fileDescriptor) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sound newSound(FileDescriptor fileDescriptor) {
		// TODO Auto-generated method stub
		return null;
	}

}

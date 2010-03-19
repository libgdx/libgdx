package com.badlogic.gdx.backends.desktop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

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
	public Music newMusic(FileHandle file) 
	{	
		try {			
			JoglMusic music = new JoglMusic( ((JoglFileHandle)file) );			
			return music;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sound newSound(FileHandle file) {
		try {			
			JoglSound sound = new JoglSound( this, ((JoglFileHandle)file) );			
			return sound;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

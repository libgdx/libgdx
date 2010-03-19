package com.badlogic.gdx.backends.desktop;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

/**
 * Implements the {@link Sound} interface for the desktop 
 * using {@link Clip}s internally.
 * @author mzechner
 *
 */
public class JoglSound implements Sound 
{
	/** the audio data in its raw format **/
	protected short[] samples = new short[44100*2*5];
	
	/** the number of samples **/
	protected int numSamples;
	
	/** the audio instance **/
	private final JoglAudio audio;
	
	public JoglSound( JoglAudio audio, JoglFileHandle file ) throws UnsupportedAudioFileException, FileNotFoundException
	{		
		
		this.audio = audio;
		InputStream fin = new BufferedInputStream( new FileInputStream( file.getFile() ) );		
		AudioFormat decodeFormat = new AudioFormat( 44100, 16, 1, true, false );
		int idx = 0;		
		try
		{
			AudioInputStream ain = AudioSystem.getAudioInputStream( decodeFormat, AudioSystem.getAudioInputStream( fin ) );		
			DataInputStream din = new DataInputStream( ain );			
			while( true )
			{
				samples[idx++] = din.readShort();
				if( idx == samples.length )
				{
					short[] tmp = new short[(int)(samples.length * 1.2)];
					System.arraycopy( samples, 0, tmp, 0, samples.length );
				}
			}						
		}
		catch( IOException ex )
		{
			if( ex instanceof EOFException == false )
				throw new UnsupportedAudioFileException();
			numSamples = idx;
		}
	}
	
	@Override
	public void dispose() 
	{
		
	}

	@Override
	public void play() 
	{
		audio.enqueuSound( this );
	}

}

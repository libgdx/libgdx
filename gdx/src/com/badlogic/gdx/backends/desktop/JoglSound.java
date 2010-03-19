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
import javax.sound.sampled.LineUnavailableException;
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
	/** the audio format **/
	private final AudioFormat format;
	
	/** the audio data **/
	private final byte[] samples;

	
	public JoglSound( JoglAudio audio, JoglFileHandle file ) throws UnsupportedAudioFileException, IOException
	{			
		InputStream fin = new BufferedInputStream( new FileInputStream( file.getFile() ) );		
		AudioInputStream ain = AudioSystem.getAudioInputStream( fin );
		AudioFormat baseFormat = ain.getFormat();
		AudioFormat  decodedFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				baseFormat.getSampleRate(),
				16,
				baseFormat.getChannels(),
				baseFormat.getChannels() * 2,
				baseFormat.getSampleRate(),
				false);
		
		ain = AudioSystem.getAudioInputStream(decodedFormat, ain);	
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024*10];
		int readBytes = ain.read( buffer );
		while( readBytes != -1 )
		{
			bytes.write( buffer, 0, readBytes );
			readBytes = ain.read( buffer );
		}		
		ain.close();
		System.out.println(decodedFormat);
		format = decodedFormat;
		samples = bytes.toByteArray();
	}
	
	@Override
	public void dispose() 
	{
		
	}

	@Override
	public void play() 
	{
		Clip clip;
		try {
			clip = AudioSystem.getClip();
			clip.open( format, samples, 0, samples.length );
			clip.start();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}

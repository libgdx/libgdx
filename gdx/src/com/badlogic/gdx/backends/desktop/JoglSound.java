package com.badlogic.gdx.backends.desktop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent.Type;

import com.badlogic.gdx.audio.Sound;

/**
 * Implements the {@link Sound} interface for the desktop 
 * using {@link Clip}s internally.
 * @author mzechner
 *
 */
public class JoglSound implements Sound 
{
	/** the audio data in its raw format **/
	private final byte[] bytes;
	
	public JoglSound( InputStream in ) throws IOException 
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024*10];
		int readBytes = in.read( buffer );
		while( readBytes != -1 )
		{
			bytes.write( buffer, 0, readBytes );
			readBytes = in.read(buffer);
		}
		this.bytes = bytes.toByteArray();
	}
	
	@Override
	public void dispose() 
	{
		
	}

	@Override
	public void play() 
	{
		try
		{
			AudioInputStream in = AudioSystem.getAudioInputStream( new ByteArrayInputStream( bytes ) );
			AudioFormat baseFormat = in.getFormat();
			AudioFormat  decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(),
					16,
					baseFormat.getChannels(),
					baseFormat.getChannels() * 2,
					baseFormat.getSampleRate(),
					false);
			in = AudioSystem.getAudioInputStream(decodedFormat, in );
			final Clip clip = AudioSystem.getClip();
			clip.open( in );
// 			FIXME the clip is never closed. Uncommenting this however
//			leads to a shitload of lag, i have no idea why. The java sound API is a mess.
//			clip.addLineListener( new LineListener() {
//				
//				@Override
//				public void update(LineEvent arg0) {
//					if( arg0.getType() == Type.STOP )
//						clip.close();
//				}
//			});
			
			clip.start();
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}	
	}

}

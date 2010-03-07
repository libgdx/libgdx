package com.badlogic.gdx.backends.jogl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent.Type;

import com.badlogic.gdx.Sound;

public class JoglSound implements Sound
{
	byte[] bytes;
	
	public JoglSound( InputStream in ) throws IOException
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		int b = -1;
		while( (b = in.read()) != -1 )
			bytes.write( b );
		this.bytes = bytes.toByteArray();
	}
	
	@Override
	public void play() {
		try
		{
			final Clip clip = AudioSystem.getClip();
			clip.open( AudioSystem.getAudioInputStream( new ByteArrayInputStream( bytes ) ) );
			clip.addLineListener( new LineListener() {
				
				@Override
				public void update(LineEvent arg0) {
					if( arg0.getType() == Type.STOP )
						clip.close();
				}
			});
			
			clip.start();
		}
		catch( Exception ex )
		{
			
		}	
	}
	

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
}


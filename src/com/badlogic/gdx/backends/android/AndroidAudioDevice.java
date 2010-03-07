package com.badlogic.gdx.backends.android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.badlogic.gdx.AudioDevice;

public class AndroidAudioDevice implements AudioDevice 
{
	AudioTrack track;
	short[] buffer = new short[1024];
	
	public AndroidAudioDevice( )
	{
		int minSize =AudioTrack.getMinBufferSize( 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );        
        track = new AudioTrack( AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
        track.play();        
	}
	
	@Override
	public void writeSamples(short[] samples) 
	{	
		fillBuffer( samples );
		track.write( buffer, 0, samples.length );
	}

	@Override
	public void writeSamples(float[] samples) 
	{	
		fillBuffer( samples );
		track.write( buffer, 0, samples.length );
	}

	private void fillBuffer( float[] samples )
	{
		for( int i = 0, j = 0; i < samples.length; i++, j+=2 )
		{
			short value = (short)(samples[i] * Short.MAX_VALUE);
			buffer[i] = value;
		}
	}	
	
	private void fillBuffer( short[] samples )
	{
		System.arraycopy( samples, 0, buffer, 0, samples.length );
	}	
}

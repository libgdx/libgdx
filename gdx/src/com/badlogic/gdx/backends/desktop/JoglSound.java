package com.badlogic.gdx.backends.desktop;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.badlogic.gdx.audio.Sound;

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
	private final byte[] originalSamples;
	
	/** the float audio data **/
	private float[] samples;
	
	/** the audio instance **/
	private final JoglAudio audio;

	
	public JoglSound( JoglAudio audio, JoglFileHandle file ) throws UnsupportedAudioFileException, IOException
	{			
		this.audio = audio;
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
		originalSamples = bytes.toByteArray();		
		
		ByteBuffer tmpBuffer = ByteBuffer.wrap( originalSamples );
		tmpBuffer.order(ByteOrder.LITTLE_ENDIAN);
		ShortBuffer shorts = tmpBuffer.asShortBuffer();
		samples = new float[originalSamples.length/2];
		for( int i = 0; i < samples.length; i++ )
		{
			float value = shorts.get(i) / (float)Short.MAX_VALUE;
			if( value < -1 )
				value = -1;
			if( value > 1 )
				value = 1;
			samples[i] = value;
		}
		
		samples = resample( samples, decodedFormat.getSampleRate(), decodedFormat.getChannels() == 1 );		
	}
	
	private float[] resample( float[] samples, float sampleRate, boolean isMono )
	{
		if( sampleRate == 44100 )
			return samples;
		
		float idxInc = sampleRate / 44100;
		int numSamples = (int)((samples.length / (float)sampleRate) * 44100);
		if( !isMono && numSamples % 2 != 0 )
			numSamples--;
		
		float[] newSamples = new float[numSamples];
		
		if( isMono )
		{
			float idx = 0;
			for( int i = 0; i < newSamples.length; i++ )
			{				
				int intIdx = (int)idx;			
				if( intIdx >= samples.length - 1 )
					break;
				
				float value = samples[intIdx] + samples[intIdx+1];
				value /= 2;
				if( value > 1 )
					value = 1;
				if( value < -1 )
					value = -1;
				newSamples[i] = value;
				idx += idxInc;
			}
		}
		else
		{
			float idx = 0;			
			for( int i = 0; i < newSamples.length; i+=2 )
			{				
				int intIdxL = (int)idx * 2;			
				int intIdxR = (int)idx * 2 + 1;
				if( intIdxL >= samples.length - 2 )
					break;
				
				float value = samples[intIdxL] + samples[intIdxL+2];
				value /= 2;
				if( value > 1 )
					value = 1;
				if( value < -1 )
					value = -1;
				newSamples[i] = value;
				
				value = samples[intIdxR] + samples[intIdxR+2];
				value /= 2;
				if( value > 1 )
					value = 1;
				if( value < -1 )
					value = -1;
				newSamples[i+1] = value;
				
				idx += idxInc;
			}
		}
				
		return newSamples;
	}
	
	@Override
	public void dispose() 
	{
		
	}

	@Override
	public void play() 
	{
		audio.enqueueSound( this );
	}

	/**
	 * @return the {@link AudioFormat} of the audio data
	 */
	public AudioFormat getAudioFormat() 
	{	
		return format;
	}

	/**
	 * @return the audio samples in form of a byte array
	 */
	public float[] getAudioData() 
	{	
		return samples;
	}

}

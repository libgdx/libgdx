package com.badlogic.gdx.tests;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import com.badlogic.gdx.audio.analysis.AudioTools;
import com.badlogic.gdx.audio.analysis.FFT;
import com.badlogic.gdx.audio.analysis.KissFFT;

public class FFTTest 
{
	static int SAMPLES = 1024;
	
	public static void main( String[] argv )
	{		
		float[] samples = new float[SAMPLES];
		ShortBuffer shortSamples = AudioTools.allocateShortBuffer( SAMPLES, 1 );		
		
//		createFlat( samples, shortSamples );
		createSine(samples, shortSamples);
		analyse( samples, shortSamples );
	}
	
	private static void analyse( float samples[], ShortBuffer shortSamples )
	{
		FFT fft = new FFT( SAMPLES, 44100 );
		KissFFT kfft = new KissFFT( SAMPLES );
		fft.forward( samples );		
		float[] spectrum = fft.getSpectrum();
		FloatBuffer floatSpectrum = AudioTools.allocateFloatBuffer( SAMPLES / 2 + 1, 1 );
		
		System.out.println(Arrays.toString( spectrum ) );
						
		System.out.print( "[" );
		kfft.spectrum( shortSamples, floatSpectrum);		
		for( int i = 0; i < SAMPLES / 2 + 1; i++ )
			System.out.print( floatSpectrum.get( i ) + ", " );
		System.out.println( "]" );
		
		System.out.println( sum( samples ) );
		System.out.println( sum( spectrum ) );
		System.out.println( sum( floatSpectrum ));		
		kfft.dispose();
	}	
	
	private static float sum( float samples[] )
	{
		float sum = 0;
		for( int i = 1; i < samples.length; i++)
			sum+=samples[i];
		return sum;
	}
	
	private static float sum( FloatBuffer buffer )
	{
		float sum = 0;
		for( int i = 1; i < SAMPLES / 2 + 1; i++ )
			sum += buffer.get(i) * 2 / SAMPLES;
		return sum;
	}
	
	private static void createFlat( float samples[], ShortBuffer shortSamples )
	{
		for( int i = 0; i < samples.length; i++ )
		{				
			samples[i] = 1;
			shortSamples.put( Short.MAX_VALUE );
		}
	}
	
	private static void createSine( float samples[], ShortBuffer shortSamples )
	{
		final float frequency = 440;
        float increment = (float)(2*Math.PI) * frequency / 44100; // angular increment for each sample
        float angle = 0;	                    
        
        for( int i = 0; i < samples.length; i++ )
        {
           samples[i] = (float)Math.sin( angle );
           shortSamples.put( (short)(Short.MAX_VALUE * samples[i]) );
           angle += increment;
        }
	}
}

package com.badlogic.gdx.audio.analysis;

import java.nio.FloatBuffer;

/**
 * A native implementation of the Fast Fourier Transform, directly
 * ported from the {@link FFT} class.
 * 
 * @author mzechner
 *
 */
public class NativeFFT 
{
	/** the handle to the native fft instance **/
	private long handle;
	
	
	public NativeFFT( int timeSize, int sampleRate )
	{
		handle = createFFT( timeSize, sampleRate );
	}
	
	private native long createFFT( int timeSize, int sampleRate );
	
	private native void destroyFFT( long handle );
	
	private native void nativeSpectrum( long handle, FloatBuffer samples, FloatBuffer spectrum, int numSamples );
	
	public void spectrum( FloatBuffer samples, FloatBuffer spectrum, int numSamples )
	{
		nativeSpectrum( handle, samples, spectrum, numSamples );
	}
	
	public void dispose( )
	{
		destroyFFT( handle );
	}	
}

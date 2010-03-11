/**
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.audio.analysis;

import com.badlogic.gdx.audio.io.Decoder;

/**
 * Provides float[] arrays of successive spectrum frames retrieved via
 * FFT from a Decoder. The frames might overlapp by n samples also called
 * the hop size. Using a hop size smaller than the spectrum size is beneficial
 * in most cases as it smears out the spectra of successive frames somewhat. 
 * @author badlogicgames@gmail.com
 *
 */
public class SpectrumProvider 
{
	/** the decoder to use **/
	private final Decoder decoder;	
	
	/** the current sample array **/
	private float[] samples;
	
	/** the look ahead sample array **/
	private float[] nextSamples;
	
	/** temporary samples array **/ 
	private float[] tempSamples; 
	
	/** the current sample, always modulo sample window size **/
	private int currentSample = 0;	
	
	/** the hop size **/
	private final int hopSize;
	
	/** the fft **/
	private final FFT fft;	
	
	/**
	 * Constructor, sets the {@link Decoder}, the sample window size and the
	 * hop size for the spectra returned. Say the sample window size is 1024
	 * samples. To get an overlapp of 50% you specify a hop size of 512 samples,
	 * for 25% overlap you specify a hopsize of 256 and so on. Hop sizes are of
	 * course not limited to powers of 2. 
	 * 
	 * @param decoder The decoder to get the samples from.
	 * @param sampleWindowSize The sample window size.
	 * @param hopSize The hop size.
	 * @param useHamming Wheter to use hamming smoothing or not.
	 */
	public SpectrumProvider( Decoder decoder, int sampleWindowSize, int hopSize, boolean useHamming )
	{
		if( decoder == null )
			throw new IllegalArgumentException( "Decoder must be != null" );
		
		if( sampleWindowSize <= 0 )
			throw new IllegalArgumentException( "Sample window size must be > 0" );
		if( hopSize <= 0 )
			throw new IllegalArgumentException( "Hop size must be > 0" );
		
		if( sampleWindowSize < hopSize )
			throw new IllegalArgumentException( "Hop size must be <= sampleSize" );
		
		
		this.decoder = decoder;		
		this.samples = new float[sampleWindowSize];
		this.nextSamples = new float[sampleWindowSize];
		this.tempSamples = new float[sampleWindowSize];
		this.hopSize = hopSize;			
		fft = new FFT( sampleWindowSize, 44100 );
		if( useHamming )
			fft.window(FFT.HAMMING);
		
		decoder.readSamples( samples );
		decoder.readSamples( nextSamples );
	}
	
	/**
	 * Returns the next spectrum or null if there's no more data.
	 * @return The next spectrum or null.
	 */
	public float[] nextSpectrum( )
	{		
		if( currentSample >= samples.length )
		{
			float[] tmp = nextSamples;
			nextSamples = samples;
			samples = tmp;
			if( decoder.readSamples( nextSamples ) == 0 )
				return null;
			currentSample -= samples.length;
		}
		
		System.arraycopy( samples, currentSample, tempSamples, 0, samples.length - currentSample );
		System.arraycopy( nextSamples, 0, tempSamples, samples.length - currentSample, currentSample );					
		fft.forward( tempSamples );		
		currentSample += hopSize;						
		return fft.getSpectrum();
	}
	
	/**
	 * @return the FFT instance used
	 */
	public FFT getFFT( )
	{
		return fft;
	}
}

package com.badlogic.gdx.audio.analysis;
/*
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
 *  along with libgdx.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class KissFFT 
{
	static
	{
		System.loadLibrary( "gdx" );
	}

	/** the pointer to the kiss fft object **/
	private final long handle;

	/**
	 * Creates a new fft instance that can analyse numSamples samples.
	 * timeSize must be a power of two.
	 * 
	 * @param numSamples the number of samples to be analysed.
	 */
	public KissFFT( int numSamples )
	{
		handle = create( numSamples );
	}

	/**
	 * Creates a new kiss fft object
	 * @param timeSize the number of samples
	 * @return the handle to the kiss fft object
	 */
	private native long create( int timeSize );

	/**
	 * Destroys a kiss fft object
	 * @param handle the handle to the kiss fft object
	 */
	private native void destroy( long handle );

	/**
	 * Calculates the frequency spectrum of the given samples. There must be
	 * as many samples as specified in the constructor of this class. Spectrum
	 * must hold timeSize / 2 + 1 elements
	 * 
	 * @param handle the handle to the kiss fft object
	 * @param samples the samples in 16-bit signed PCM encoding
	 * @param spectrum the spectrum
	 */
	private native void spectrum( long handle, ShortBuffer samples, FloatBuffer spectrum );

	/**
	 * Calculates the frequency spectrum of the given samples. There must be
	 * as many samples as specified in the constructor of this class. Spectrum
	 * must hold timeSize / 2 + 1 elements
	 * 
	 * @param samples the samples
	 * @param spectrum the spectrum
	 */
	public void spectrum( ShortBuffer samples, FloatBuffer spectrum )
	{
		spectrum( handle, samples, spectrum );
	}

	/**
	 * Releases all resources of this object
	 */
	public void dispose( )
	{
		destroy(handle);
	}
	
	public void getRealPart( ShortBuffer real )
	{
		getRealPart(handle, real);
	}
	
	public void getImagPart( ShortBuffer imag )
	{
		getImagPart( handle, imag );
	}
	
	private native void getRealPart( long handle, ShortBuffer real );
	
	private native void getImagPart( long handle, ShortBuffer imag );
	
	public static void main( String[] argv )
	{
		final float frequency = 440;
		float increment = (float)(2*Math.PI) * frequency / 44100; // angular increment for each sample
		float angle = 0;	            		
		short samples[] = new short[1024];

		for( int i = 0; i < samples.length; i++ )
		{
			float value = (float)Math.sin( angle );	
			samples[i] = (short)(value * 32767);
			angle += increment;
		}
		
		ShortBuffer samplesBuffer = AudioTools.allocateShortBuffer( 1024, 1 );
		ShortBuffer realBuffer = AudioTools.allocateShortBuffer(512,1 );
		ShortBuffer imagBuffer = AudioTools.allocateShortBuffer(512,1 );
		samplesBuffer.put( samples );
		FloatBuffer spectrum = AudioTools.allocateFloatBuffer( 513, 1 );
		KissFFT fft = new KissFFT( 1024 );
		
		fft.spectrum( samplesBuffer, spectrum );
		
		fft.getRealPart( realBuffer );
		fft.getImagPart( imagBuffer );
		short[] re = new short[512];
		short[] im = new short[512];
		float[] sp = new float[513];
		realBuffer.position(0);
		realBuffer.get(re);
		imagBuffer.position(0);
		imagBuffer.get(im);
		spectrum.position(0);
		spectrum.get(sp);
		
		for( int i = 0; i < 30; i++ )
		{
			System.out.println( sp[i] );
		}
	}
}

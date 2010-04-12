package com.badlogic.gdx.audio.analysis;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.audio.io.Mpg123Decoder;

/**
 * Class holding various static native methods for processing 
 * audio data.
 * 
 * @author mzechner
 *
 */
public class AudioTools 
{
	static
	{
		System.loadLibrary( "gdx" );
	}
	
	/**
	 * Converts the 16-bit signed PCM data given in source to 32-bit float PCM
	 * in the range [-1,1]. It is assumed that there's numSamples elements available
	 * in both buffers. Source and target get read and written to from offset 0.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples
	 */
	public static native void convertToFloat( ShortBuffer source, FloatBuffer target, int numSamples );

	/**
	 * Converts the 32-bit float PCM data given in source to 16-bit signed PCM 
	 * in the range [-1,1]. It is assumed that there's numSamples elements available
	 * in both buffers. Source and target get read and written to from offset 0.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples
	 */
	public static native void convertToShort( FloatBuffer source, FloatBuffer target, int numSamples );
	
	/**
	 * Converts the samples in source which are assumed to be interleaved left/right stereo samples
	 * to mono, writting the converted samples to target. Source is assumed to hold numSamples samples,
	 * target should hold numSamples / 2. Samples are read and written from position 0 up to numSamples.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples to convert (target will have numSamples /2 filled after a call to this)
	 */
	public static native void convertToMono( ShortBuffer source, ShortBuffer target, int numSamples );

	/**
	 * Converts the samples in source which are assumed to be interleaved left/right stereo samples
	 * to mono, writting the converted samples to target. Source is assumed to hold numSamples samples,
	 * target should hold numSamples / 2. Samples are read and written from position 0 up to numSamples.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples to convert (target will have numSamples /2 filled after a call to this)
	 */
	public static native void convertToMono( FloatBuffer source, FloatBuffer target, int numSamples );
	
	/**
	 * Calculates the spectral flux between the two given spectra. Both buffers are assumed to hold
	 * numSamples elements. Spectrum B is the current spectrum spectrum A the last spectrum
	 * 
	 * @param bufferA the first spectrum
	 * @param bufferB the second spectrum
	 * @param numSamples the number of elements
	 * @return the spectral flux 
	 */
	public static native float spectralFlux( FloatBuffer spectrumA, FloatBuffer spectrumB, int numSamples );	
	
	/**
	 * Allcoates a direct buffer for the given number of samples and channels. The final
	 * numer of samples is numSamples * numChannels.
	 * 
	 * @param numSamples the number of samples
	 * @param numChannels the number of channels
	 * @return the direct buffer
	 */
	public static FloatBuffer allocateFloatBuffer( int numSamples, int numChannels )
	{
		ByteBuffer b = ByteBuffer.allocateDirect( numSamples * numChannels * 4 );
		b.order(ByteOrder.nativeOrder());
		return b.asFloatBuffer();
	}
	
	/**
	 * Allcoates a direct buffer for the given number of samples and channels. The final
	 * numer of samples is numSamples * numChannels.
	 * 
	 * @param numSamples the number of samples
	 * @param numChannels the number of channels
	 * @return the direct buffer
	 */
	public static ShortBuffer allocateShortBuffer( int numSamples, int numChannels )
	{
		ByteBuffer b = ByteBuffer.allocateDirect( numSamples * numChannels * 2 );
		b.order(ByteOrder.nativeOrder());
		return b.asShortBuffer();	
	}
	
//	public static void main( String[] argv )
//	{
//		Mpg123Decoder decoder = new Mpg123Decoder( "data/threeofaperfectpair.mp3");
//		System.out.println( "rate: " + decoder.getRate() + ", channels: " + decoder.getNumChannels() + ", length: " + decoder.getLength() );
//		JoglAudioDevice device = new JoglAudioDevice( true );
//		
//		ShortBuffer stereoSamples = AudioTools.allocateShortBuffer( 1024, decoder.getNumChannels() );
//		ShortBuffer monoSamples = AudioTools.allocateShortBuffer( 1024, 1 );
//		FloatBuffer floatSamples = AudioTools.allocateFloatBuffer( 1024, 1 );
//				
//		float[] samples = new float[1024];
//		
//		while( decoder.readSamples( stereoSamples ) > 0 )
//		{
//			AudioTools.convertToMono( stereoSamples, monoSamples, stereoSamples.capacity() );
//			AudioTools.convertToFloat( monoSamples, floatSamples, 1024 );
//			
//			floatSamples.position(0);
//			floatSamples.get(samples);
//			device.writeSamples(samples, 0, monoSamples.capacity());
//		}
//		decoder.dispose();
//	}
}

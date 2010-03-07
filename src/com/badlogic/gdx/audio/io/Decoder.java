package com.badlogic.gdx.audio.io;

/**
 * Interface for audio decoders that return successive
 * amplitude frames. When a decoder is no longer used
 * it has to be disposed.
 * 
 * @author mzechner
 *
 */
public interface Decoder
{
	/**
	 * Reads in samples.length samples in float PCM format from the decoder. Returns
	 * the actual number read in. If this number is smaller than
	 * samples.length then the end of stream has been reached.
	 * 
	 * @param samples The number of read samples.
	 */
	public int readSamples( float[] samples );
	
	/**
	 * Reads in samples.length samples in 16-bit signed PCM format from the decoder. Returns
	 * the actual number read in. If this number is smaller than
	 * samples.length then the end of stream has been reached.
	 * 
	 * @param samples The number of read samples.
	 */
	public int readSamples( short[] samples );
	
	/**
	 * Disposes the decoder and frees all associated resources
	 */
	public void dispose( );	
}

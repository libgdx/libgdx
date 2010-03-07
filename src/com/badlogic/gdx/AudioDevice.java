package com.badlogic.gdx;

/**
 * Encapsulates an audio device in 44.1khz mono mode. Use
 * the {@link AudioDevice.writeSamples()} methods to write
 * float or 16-bit signed short PCM data directly to the 
 * audio device.
 * 
 * FIXME add stereo output.
 * 
 * @author mzechner
 *
 */
public interface AudioDevice 
{
	/**
	 * Writes the array of 16-bit signed PCM samples to the
	 * audio device and blocks until they have been processed.
	 * 
	 * @param samples The samples.
	 */
	public void writeSamples( short[] samples );
	
	/**
	 * Writes the array of float PCM samples to the 
	 * audio device and blocks until they have been processed.
	 * 
	 * @param samples The samples.
	 */
	public void writeSamples( float[] samples );
}

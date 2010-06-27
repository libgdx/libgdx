package com.badlogic.gdx.audio;

/**
 * An AudioRecorder allows to record input from an audio device. It 
 * has a sampling rate and is either stereo or mono. Samples are returned
 * in signed 16-bit PCM format. 
 * 
 * @author mzechner
 *
 */
public interface AudioRecorder 
{
	/**
	 * Reads in numSamples samples into the array samples starting at
	 * offset. If the recorder is in stereo you have to multiply numSamples
	 * by 2.  
	 * 
	 * @param samples the array to write the samples to
	 * @param offset the offset into the array
	 * @param numSamples the number of samples to be read
	 */
	public void read( short[] samples, int offset, int numSamples );
	
	/**
	 * Disposes the AudioRecorder
	 */
	public void dispose( );
}

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
package com.badlogic.gdx.audio;

/**
 * Encapsulates an audio device in 44.1khz mono or stereo mode. Use
 * the {@link AudioDevice.writeSamples()} methods to write
 * float or 16-bit signed short PCM data directly to the 
 * audio device. Stereo samples are interleaved in the order left channel
 * sample, right channel sample. The {@link dispose()} method must be called
 * when this AudioDevice is no longer needed. 
 * 
 * @author badlogicgames@gmail.com
 *
 */
public interface AudioDevice 
{
	/**
	 * @return whether this AudioDevice is in mono or stereo mode. 
	 */
	public boolean isMono( );
	
	/**
	 * Writes the array of 16-bit signed PCM samples to the
	 * audio device and blocks until they have been processed.
	 * 
	 * @param samples The samples.
	 * @param offset The offset into the samples array
	 * @param numSamples the number of samples to write to the device
	 */
	public void writeSamples( short[] samples, int offset, int numSamples );
	
	/**
	 * Writes the array of float PCM samples to the 
	 * audio device and blocks until they have been processed.
	 * 
	 * @param samples The samples.
	 * @param offset The offset into the samples array
	 * @param numSamples the number of samples to write to the device
	 */
	public void writeSamples( float[] samples, int offset, int numSamples );
	
	/**
	 * Frees all resources associated with this AudioDevice. Needs
	 * to be called when the device is no longer needed.
	 */
	public void dispose( );
}

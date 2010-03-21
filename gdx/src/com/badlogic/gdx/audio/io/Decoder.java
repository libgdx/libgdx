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
package com.badlogic.gdx.audio.io;

/**
 * Interface for audio decoders that return successive
 * amplitude frames. When a decoder is no longer used
 * it has to be disposed.
 * 
 * @author badlogicgames@gmail.com
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

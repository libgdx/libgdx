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

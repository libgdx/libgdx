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
package com.badlogic.gdx.audio;

/**
 * A sound is a short audio clip that can be played numerous times.
 * it's completely loaded into memory. Call the {@link Sound.dispose()}
 * method when you're done using the Sound.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public interface Sound 
{
	/**
	 * Plays the sound, you can call this repeatedly to
	 * play the same sound a couple of times with a bit
	 * of lag introduced.
	 */
	public void play( );
	
	/**
	 * Releases all the resources.
	 */
	public void dispose( );
}

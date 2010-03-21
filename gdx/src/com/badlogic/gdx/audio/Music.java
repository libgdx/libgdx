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
 * A Music instance represents a streamed audio file which is played back. The interface
 * supports setting the playback position, pausing and resuming and so on. When you are
 * done with using the Music instance you have to dispose it via the {@link Music.dispose()}
 * method.
 * @author mzechner
 *
 */
public interface Music 
{
	/**
	 * Starts the playback of the music stream.
	 * In case the stream was paused this will
	 * resume the playback. In case the music stream
	 * is finished playing this will restart the playback.
	 */
	public void play( );
	
	/**
	 * Pauses the playback. If the music stream has
	 * not been started yet or has finished playing
	 * a call to this method will be ignored. 
	 */
	public void pause( );
	
	/**
	 * Stops a playing or paused Music instance. Next time play()
	 * is invoked the Music will start from the beginning.
	 */
	public void stop( );
	
	/**
	 * @return whether this music stream is playing or not
	 */
	public boolean isPlaying( );	
	
	/**
	 * Sets whether the music stream is looping or not. This
	 * can be called at any time, whether the stream is playing 
	 * or not.
	 * 
	 * @param isLooping whether to loop the stream or not
	 */
	public void setLooping( boolean isLooping );
	
	/**
	 * @return whether the music stream is playing or not. 
	 */
	public boolean isLooping( );
	
	/**
	 * Sets the volume of this music stream. The volume
	 * must be given in the range [0,1] with 0 being silent
	 * and 1 being the maximum volume.
	 * @param volume
	 */
	public void setVolume( float volume );
	
	/**
	 * Needs to be called when the Music is no longer needed.
	 */
	public void dispose( );	
}

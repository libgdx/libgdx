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
package com.badlogic.gdx.backends.android;

import android.media.AudioManager;
import android.media.SoundPool;

import com.badlogic.gdx.audio.Sound;

/**
 * An implementation of {@link Sound} for Android using {@link SoundPool}
 * 
 * @author badlogicgames@gmail.com
 *
 */
final class AndroidSound implements Sound 
{
	final SoundPool soundPool;
	final AudioManager manager;
	final int soundId;
	
	AndroidSound( SoundPool pool, AudioManager manager, int soundId )
	{
		this.soundPool = pool;
		this.manager = manager;
		this.soundId = soundId;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void play() {
		int streamVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		soundPool.play( soundId, streamVolume, streamVolume, 1, 0, 1 );
	}
	
}

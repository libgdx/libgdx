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
package com.badlogic.gdx.backends.android;

import android.media.AudioManager;
import android.media.SoundPool;

import com.badlogic.gdx.audio.Sound;

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
	
	@Override
	public void dispose() 
	{	
		soundPool.unload( soundId );
	}

	@Override
	public void play() 
	{	
		play( 1 );
	}

	@Override
	public void play(float volume) 
	{	
		int streamVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		soundPool.play( soundId, streamVolume * volume, streamVolume * volume, 1, 0, 1 );
	}
	
}

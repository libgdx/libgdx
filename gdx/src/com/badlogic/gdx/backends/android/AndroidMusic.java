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

import java.io.IOException;

import android.media.MediaPlayer;

import com.badlogic.gdx.audio.Music;

public class AndroidMusic implements Music
{
	private final AndroidAudio audio;
	private final MediaPlayer player;
	private boolean isPrepared = true;
	
	AndroidMusic( AndroidAudio audio, MediaPlayer player )
	{
		this.audio = audio;
		this.player = player;
	}

	@Override
	public void dispose() 
	{	
		if( player.isPlaying() )
			player.stop();
		player.release();
		audio.musics.remove(this);
	}

	@Override
	public boolean isLooping() 
	{	
		return player.isLooping();
	}

	@Override
	public boolean isPlaying() 
	{	
		return player.isPlaying();
	}

	@Override
	public void pause() 
	{	
		if( player.isPlaying() )
			player.pause();		
	}

	@Override
	public void play() 
	{	
		if( player.isPlaying() )
			return;
		
		try 
		{
			if( !isPrepared )
				player.prepare();
			player.start();
		} catch (IllegalStateException e) 
		{		
			e.printStackTrace();
		} 
		catch (IOException e) 
		{		
			e.printStackTrace();
		}		
	}

	@Override
	public void setLooping(boolean isLooping) 
	{	
		player.setLooping( isLooping );
	}

	@Override
	public void setVolume(float volume) 
	{	
		player.setVolume( volume, volume );
	}

	@Override
	public void stop() 
	{	
		player.stop();
		isPrepared = false;
	}

}

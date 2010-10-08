/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class SoundTest implements RenderListener, InputListener 
{
	Sound sound;
	Music music;
	float volume = 1.0f;

	@Override
	public void dispose( ) 
	{	
		
	}

	@Override
	public void render( ) 
	{		
		try {
			Thread.sleep( 100 );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated( ) 
	{
		if( music == null )
		{
			Gdx.input.addInputListener( this );	
			sound = Gdx.audio.newSound( Gdx.files.getFileHandle( "data/shotgun.wav", FileType.Internal ) );
			
			music = Gdx.audio.newMusic( Gdx.files.getFileHandle( "data/cloudconnected.ogg", FileType.Internal ) );				
			music.play();		
			music.setLooping( true );
		}
	}

	@Override
	public boolean keyDown(int keycode) 
	{	
		return false;
	}

	@Override
	public boolean keyTyped(char character) 
	{
		if( character == '+' )
			volume += 0.1f;
		if( character == '-' )
			volume -= 0.1f;
		music.setVolume( volume );		
			
		return false;
	}

	@Override
	public boolean keyUp(int keycode) 
	{	
		
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer) {
		sound.play();
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer) 
	{		
		return false;
	}
}

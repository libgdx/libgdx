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
package com.badlogic.gdxinvaders;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdxinvaders.screens.GameLoop;
import com.badlogic.gdxinvaders.screens.GameOver;
import com.badlogic.gdxinvaders.screens.MainMenu;
import com.badlogic.gdxinvaders.screens.Screen;

public class GdxInvaders implements RenderListener 
{
	/** flag indicating whether we were initialized already **/
	private boolean isInitialized = false;
	
	/** the current screen **/
	private Screen screen;
	
	@Override
	public void dispose(Application app) 
	{	

	}

	@Override
	public void render(Application app) 
	{	
		// update the screen
		screen.update(app);
		
		// render the screen
		screen.render(app);
		
		// when the screen is done we change to the 
		// next screen
		if( screen.isDone() )
		{
			// dispose the current screen
			screen.dispose();
			
			// if this screen is a main menu screen we switch to
			// the game loop
			if( screen instanceof MainMenu )			
				screen = new GameLoop( app );			
			else
			// if this screen is a game loop screen we switch to the
			// game over screen
			if( screen instanceof GameLoop )			
				screen = new GameOver( app );			
			else				
			// if this screen is a game over screen we switch to the
			// main menu screen
			if( screen instanceof GameOver )			
				screen = new MainMenu(app);					
		}
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	

	}

	@Override
	public void surfaceCreated(Application app) 
	{	
		if( !isInitialized )
		{
			screen = new MainMenu(app);
			Music music = app.getAudio().newMusic( app.getFiles().getFileHandle( "data/8.12.mp3", FileType.Internal ) );
			music.setLooping(true);
			music.play();
			isInitialized = true;
		}
	}
}

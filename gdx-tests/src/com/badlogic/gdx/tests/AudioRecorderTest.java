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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.ImmediateModeRenderer;

public class AudioRecorderTest implements RenderListener, ApplicationListener
{
	short[] samples = new short[1024*4];
	AudioDevice device;
	AudioRecorder recorder;
	ImmediateModeRenderer renderer;
	
	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{	
		GL10 gl = app.getGraphics().getGL10();
		gl.glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );					
		
		float incX = 2.0f / samples.length;
		float x = -1;
//		renderer.begin( GL10.GL_LINE_STRIP );
//		for( int i = 0; i < samples.length/100; i++, x+=incX )			
//			renderer.vertex( x, samples[i] / (float)Short.MAX_VALUE, 0 );
//		renderer.end();
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{	
		device = app.getAudio().newAudioDevice(true);
		recorder = app.getAudio().newAudioRecoder( 44100, true );
		renderer = new ImmediateModeRenderer( app.getGraphics().getGL10() );
		
		Thread t = new Thread( new Runnable() {

			@Override
			public void run() {
				while( true )
				{
					recorder.read( samples, 0, samples.length );
					device.writeSamples( samples, 0, samples.length );
				}			
			}			
		});
		t.setDaemon( true );
		t.start();
		
		app.setApplicationListener( this );
	}

	@Override
	public void destroy(Application app) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause(Application app) 
	{
		device.dispose();
		recorder.dispose();
	}

	@Override
	public void resume(Application app) 
	{
		device = app.getAudio().newAudioDevice(true);
		recorder = app.getAudio().newAudioRecoder( 44100, true );		
	}	
}

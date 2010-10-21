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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.audio.AudioDevice;

public class AudioDeviceTest implements RenderListener
{
	Thread thread;
	boolean stop = false;
	
	@Override
	public void dispose() {
		stop = true;
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceChanged(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated() 
	{	
		if( thread == null )
		{
			final AudioDevice device = Gdx.app.getAudio().newAudioDevice( false );
			thread = new Thread( new Runnable() {
				@Override
				public void run() 
				{			
					final float frequency = 440;
		            float increment = (float)(2*Math.PI) * frequency / 44100; // angular increment for each sample
		            float angle = 0;	            
		            float samples[] = new float[1024];
		 
		            while( !stop )
		            {
		               for( int i = 0; i < samples.length; i+=2 )
		               {
		                  samples[i] = 0.5f * (float)Math.sin( angle );
		                  samples[i+1] = 2 * samples[i];
		                  angle += increment;
		               }
		 
		               device.writeSamples( samples, 0, samples.length );
		            } 
		            
		            device.dispose();
				}			
			});
			thread.start();
		}
	}

}

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
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.audio.AudioDevice;

public class AudioDeviceTest implements RenderListener
{
	Thread thread;
	boolean stop = false;
	
	@Override
	public void dispose(Application app) {
		stop = true;
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void render(Application app) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{	
		if( thread == null )
		{
			final AudioDevice device = app.getAudio().newAudioDevice( false );
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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.backends.desktop.JoglApplication;

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

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "AudioDevice Test", 480, 320, false );
		app.getGraphics().setRenderListener( new AudioDeviceTest() );
	}
}

package com.badlogic.gdx.samples;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.AudioDevice;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Mesh;
import com.badlogic.gdx.Mesh.PrimitiveType;
import com.badlogic.gdx.audio.io.NativeMP3Decoder;
import com.badlogic.gdx.backends.jogl.JoglApplication;

public class AudioDecoding implements RenderListener 
{
	float[] samples = new float[1024];
	Mesh amplitude;
	Thread thread;
	boolean finished = false;
	
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Audio Decoding", 480, 320 );
		app.addRenderListener( new AudioDecoding() );
	}
	
	@Override
	public void dispose(Application application) 
	{	
		finished = true;
		try {
			thread.join( );
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void render(Application application) 
	{	
		application.clear( true, false, false );
		
		for( int i = 0; i < samples.length; i++ )
			amplitude.vertex( i / ((float)samples.length / 2) - 1, samples[i], 0 );
		amplitude.render(PrimitiveType.LineStrip );
	}

	@Override
	public void setup( final Application application) 
	{	
		amplitude = application.newMesh( 1024, false, false, false, false, 0, false );
		
		thread = new Thread( new Runnable() {
			@Override
			public void run() {
				AudioDevice device = application.getAudioDevice();
				NativeMP3Decoder decoder = new NativeMP3Decoder( "/sdcard/audio/cloudconnected.mp3");				
				while( decoder.readSamples( samples ) > 0 && ! finished )
				{
					device.writeSamples( samples );					
				}
			}					
		});
		thread.start();
	}

}

package com.badlogic.gdx.tests;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.audio.io.Mpg123Decoder;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.desktop.JoglApplication;

public class Mpg123Test implements RenderListener
{

	@Override
	public void dispose(Application app) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(final Application app) 
	{		
		
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{
		String file = null;
		if( app instanceof AndroidApplication )
			 file = "/sdcard/audio/schism.mp3";
		else
			 file = "data/threeofaperfectpair.mp3";
		
		Mpg123Decoder decoder = new Mpg123Decoder( file );
		app.log( "Mpg123", "rate: " + decoder.getRate() + ", channels: " + decoder.getNumChannels());
//		AudioDevice device = app.getAudio().newAudioDevice(false);
		
		ByteBuffer tmp = ByteBuffer.allocateDirect( 1024 * 2 * decoder.getNumChannels() );
		tmp.order(ByteOrder.nativeOrder());
		ShortBuffer buffer = tmp.asShortBuffer();
//		short[] samples = new short[1024*decoder.getNumChannels()];
		
		long start = System.nanoTime();
		while( decoder.readSamples( decoder.handle, buffer, 1024 * decoder.getNumChannels() ) > 0 )
		{
//			buffer.position(0);
//			buffer.get(samples);
//			device.writeSamples(samples, 0, 1024*2);
	//		System.out.println( "decoded" );
		}
		app.log( "Mpg123", "took " + (System.nanoTime()-start) / 1000000000.0 );
		decoder.dispose();			
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Mpg123 Test", 480, 320, false );
		app.getGraphics().setRenderListener( new Mpg123Test() );
	}
}

package com.badlogic.gdx.tests;

import java.nio.ShortBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.audio.analysis.AudioTools;
import com.badlogic.gdx.audio.io.VorbisDecoder;

public class VorbisTest implements RenderListener
{

	@Override
	public void dispose(Application app) {
		// TODO Auto-generated method stub
		
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
		VorbisDecoder decoder = null;
		if( app.getType() == ApplicationType.Android )
			decoder = new VorbisDecoder( "/sdcard/audio/schism.ogg" );
		else
			decoder = new VorbisDecoder( "data/cloudconnected.ogg" );
		app.log( "Vorbis", "channels: "+ decoder.getNumChannels() + ", rate: " + decoder.getRate() + ", length: " + decoder.getLength() );;
				
		ShortBuffer samplesBuffer = AudioTools.allocateShortBuffer( 1024, 2 );		
			
		long start = System.nanoTime();
		while( decoder.readSamples( samplesBuffer ) > 0 )
		{
			
		}
		app.log( "Vorbis", "took " + (System.nanoTime()-start)/1000000000.0 );
		decoder.dispose();	
	}

}

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

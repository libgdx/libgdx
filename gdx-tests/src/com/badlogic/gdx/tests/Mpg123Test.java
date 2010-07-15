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

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.audio.analysis.AudioTools;
import com.badlogic.gdx.audio.analysis.KissFFT;
import com.badlogic.gdx.audio.io.Mpg123Decoder;

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
	public void surfaceCreated( final Application app) 
	{	
//		Thread thread = new Thread( new Runnable() {
//
//			public void run( )
//			{
				String file = null;
				if( app.getType() == ApplicationType.Android )
					file = "/sdcard/audio/schism.mp3";
				else
					file = "data/threeofaperfectpair.mp3";

				Mpg123Decoder decoder = new Mpg123Decoder( file );
				ShortBuffer stereoSamples = AudioTools.allocateShortBuffer( 1024, decoder.getNumChannels() );
				ShortBuffer monoSamples = AudioTools.allocateShortBuffer( 1024, 1 );
				FloatBuffer spectrum = AudioTools.allocateFloatBuffer( 1024 / 2 + 1, 1);
				KissFFT fft = new KissFFT( 1024 );

				app.log( "Mpg123", "rate: " + decoder.getRate() + ", channels: " + decoder.getNumChannels() + ", length: " + decoder.getLength() );		

				long start = System.nanoTime();
				while( decoder.readSamples( stereoSamples ) > 0 )
				{
					AudioTools.convertToMono( stereoSamples, monoSamples, stereoSamples.capacity() );
					fft.spectrum( monoSamples, spectrum );
				}

				app.log( "Mpg123", "took " + (System.nanoTime()-start) / 1000000000.0 );
				decoder.dispose();
//			} 
//		});
//		thread.setPriority(Thread.MAX_PRIORITY);
//		thread.start();
	}
	
}

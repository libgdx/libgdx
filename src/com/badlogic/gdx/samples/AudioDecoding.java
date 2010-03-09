/**
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
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.samples;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.io.NativeMP3Decoder;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.RenderListener;
import com.badlogic.gdx.graphics.Mesh.PrimitiveType;

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

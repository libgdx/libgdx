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
package com.badlogic.gdx.backends.android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.badlogic.gdx.AudioDevice;

final class AndroidAudioDevice implements AudioDevice 
{
	AudioTrack track;
	short[] buffer = new short[1024];
	
	AndroidAudioDevice( )
	{
		int minSize =AudioTrack.getMinBufferSize( 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );        
        track = new AudioTrack( AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
        track.play();        
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeSamples(short[] samples) 
	{	
		fillBuffer( samples );
		track.write( buffer, 0, samples.length );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeSamples(float[] samples) 
	{	
		fillBuffer( samples );
		track.write( buffer, 0, samples.length );
	}
	
	private void fillBuffer( float[] samples )
	{
		for( int i = 0, j = 0; i < samples.length; i++, j+=2 )
		{
			short value = (short)(samples[i] * Short.MAX_VALUE);
			buffer[i] = value;
		}
	}	
	
	private void fillBuffer( short[] samples )
	{
		System.arraycopy( samples, 0, buffer, 0, samples.length );
	}	
}

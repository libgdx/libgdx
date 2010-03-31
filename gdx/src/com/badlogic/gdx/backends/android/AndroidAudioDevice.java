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
package com.badlogic.gdx.backends.android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.badlogic.gdx.audio.AudioDevice;

/**
 * Implementation of the {@link AudioDevice} interface for Android
 * using the AudioTrack class.
 * @author mzechner
 *
 */
class AndroidAudioDevice implements AudioDevice
{
	/** the audio track **/
	private final AudioTrack track;
	
	/** the mighty buffer **/
	private short[] buffer = new short[1024];

	/** whether this device is in mono or stereo mode **/
	private final boolean isMono;
	
	AndroidAudioDevice( boolean isMono )
	{
		this.isMono = isMono;
		int minSize =AudioTrack.getMinBufferSize( 44100, isMono?AudioFormat.CHANNEL_CONFIGURATION_MONO:AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT );        
	    track = new AudioTrack( AudioManager.STREAM_MUSIC, 44100, 
	    						isMono?AudioFormat.CHANNEL_CONFIGURATION_MONO:AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT, 
	                            minSize, AudioTrack.MODE_STREAM);
	    track.play();   
	}
	
	@Override
	public void dispose() 
	{	
		track.stop();
		track.release();
	}

	@Override
	public boolean isMono() 
	{	
		return isMono;
	}

	@Override
	public void writeSamples(short[] samples, int offset, int numSamples) 
	{	
		int writtenSamples = track.write( samples, offset, numSamples );
		while( writtenSamples != numSamples )
			track.write( samples, offset + writtenSamples, numSamples - writtenSamples );
	}

	@Override
	public void writeSamples(float[] samples, int offset, int numSamples) 
	{	
		if( buffer.length < samples.length )
			buffer = new short[samples.length];
		
		int bound = offset + numSamples;
		for( int i = offset, j = 0; i < bound; i++, j++ )
		{
    		float fValue = samples[i];
    		if( fValue > 1 )
    			fValue = 1;
    		if( fValue < -1 )
    			fValue = -1;
            short value = (short)( fValue * Short.MAX_VALUE);				
			buffer[j] = value;
		}
		
		int writtenSamples = track.write( buffer, 0, numSamples );
		while( writtenSamples != numSamples )
			track.write( buffer, writtenSamples, numSamples - writtenSamples );
	}

}

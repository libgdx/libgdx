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
package com.badlogic.gdx.backends.jogl;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat.Encoding;

import com.badlogic.gdx.audio.AudioDevice;

/**
 * An implementation of {@link AudioDevice} for desktop Java based on
 * java sound.
 * 
 * @author badlogicgames@gmail.com
 *
 */
final class JoglAudioDevice implements AudioDevice 
{
	/** the buffer size in samples **/
	private final static int BUFFER_SIZE = 1024;
	
	/** the java sound line we write our samples to **/
	private final SourceDataLine out;
	
	/** buffer for BUFFER_SIZE 16-bit samples **/
	private byte[] buffer = new byte[BUFFER_SIZE*2];
	
	/**
	 * Constructor, initializes the audio system for
	 * 44100Hz 16-bit signed mono output. 
	 * 
	 * @throws Exception in case the audio system could not be initialized
	 */
	JoglAudioDevice( )
	{
		try
		{
			AudioFormat format = new AudioFormat( Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false );
			out = AudioSystem.getSourceDataLine( format );
			out.open(format);	
			out.start();
		}
		catch( Exception ex )
		{
			throw new IllegalStateException( "Couldn't initialize audio device!" );
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void writeSamples( float[] samples )
	{
		if( buffer.length / 2 < samples.length )
			buffer = new byte[samples.length * 2];
		
		fillBuffer( samples );
		out.write( buffer, 0, buffer.length );
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeSamples(short[] samples) 
	{	
		if( buffer.length / 2 < samples.length )
			buffer = new byte[samples.length * 2];
		
		fillBuffer( samples );
		out.write( buffer, 0, buffer.length );
	}
	
	private void fillBuffer( float[] samples )
	{
		for( int i = 0, j = 0; i < samples.length; i++, j+=2 )
		{
			short value = (short)(samples[i] * Short.MAX_VALUE);
			buffer[j] = (byte)(value | 0xff);
			buffer[j+1] = (byte)(value >> 8 );
		}
	}	
	
	private void fillBuffer( short[] samples )
	{
		for( int i = 0, j = 0; i < samples.length; i++, j+=2 )
		{
			short value = samples[i];
			buffer[j] = (byte)(value | 0xff);
			buffer[j+1] = (byte)(value >> 8 );
		}
	}	
}

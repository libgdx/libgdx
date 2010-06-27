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
package com.badlogic.gdx.backends.desktop;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import com.badlogic.gdx.audio.AudioDevice;

/**
 * Implementation of the {@link AudioDevice} interface for the 
 * desktop using Java Sound.
 * 
 * @author mzechner
 *
 */
public final class JoglAudioDevice implements AudioDevice 
{
	/** the audio line **/
	private SourceDataLine line;
	
	/** whether this device is mono **/
	private final boolean isMono;
	
	/** byte buffer **/
	private byte[] bytes = new byte[44100*2*2];
	
	public JoglAudioDevice( boolean isMono )
	{
		this.isMono = isMono;
		
		try {
			AudioFormat format = new AudioFormat( 44100.0f, 16, isMono?1:2, true, false );
			line = AudioSystem.getSourceDataLine( format );
			line.open(format, 4410*2);
			line.start();				
		} catch (Exception e) {
			e.printStackTrace();			
		}			
	}
	
	@Override
	public void dispose() 
	{			
		line.drain();
		line.close();
	}

	@Override
	public boolean isMono() 
	{	
		return isMono;
	}

	@Override
	public void writeSamples(short[] samples, int offset, int numSamples ) 
	{	
		if( bytes.length < samples.length * 2 )
			bytes = new byte[samples.length*2];
		
		for( int i = offset, j = 0; i < offset + numSamples; i++, j+=2 )
        {
        		short value = samples[i];        		                		                   
                bytes[j+1] = (byte)(value & 0xff);
                bytes[j] = (byte)(value >> 8 );
        }
		
		int writtenBytes = line.write( bytes, 0, numSamples * 2 );
		while( writtenBytes != numSamples * 2 )
			writtenBytes += line.write( bytes, writtenBytes, numSamples * 2 - writtenBytes );
	}

	@Override
	public void writeSamples(float[] samples, int offset, int numSamples) 
	{	
		if( bytes.length < samples.length * 2 )
			bytes = new byte[samples.length*2];
		
		for( int i = offset, j = 0; i < offset + numSamples; i++, j+=2 )
        {
        		float fValue = samples[i];
        		if( fValue > 1 )
        			fValue = 1;
        		if( fValue < -1 )
        			fValue = -1;
                short value = (short)( fValue * Short.MAX_VALUE);		                    
                bytes[j] = (byte)(value | 0xff);
                bytes[j+1] = (byte)(value >> 8 );
        }
		
		int writtenBytes = line.write( bytes, 0, numSamples * 2 );
		while( writtenBytes != numSamples * 2 )
			writtenBytes += line.write( bytes, writtenBytes, numSamples * 2 - writtenBytes );
	}
}

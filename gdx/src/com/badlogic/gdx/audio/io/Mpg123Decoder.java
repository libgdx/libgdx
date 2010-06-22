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
package com.badlogic.gdx.audio.io;

import java.nio.ShortBuffer;

import com.badlogic.gdx.Version;
import com.badlogic.gdx.audio.analysis.AudioTools;

/**
 * A {@link Decoder} implementation that decodes MP3 files via
 * libmpg123 natively. 
 * 
 * @author mzechner
 *
 */
public class Mpg123Decoder implements Decoder
{
	static
	{
		System.loadLibrary( "gdx-" + Version.VERSION );
	}
	
	public final long handle;
	
	/**
	 * Opens the given file for mp3 decoding. Throws an IllegalArugmentException
	 * in case the file could not be opened.
	 * 
	 * @param filename the filename
	 */
	public Mpg123Decoder( String filename )
	{
		handle = openFile( filename );
		
		if( handle == -1 )
			throw new IllegalArgumentException( "couldn't open file" );			
	}
		

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int readSamples(ShortBuffer samples) 
	{	
		int read = readSamples( handle, samples, samples.capacity() );
		samples.position(0);
		return read;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int skipSamples(int numSamples) 
	{	
		return skipSamples( handle, numSamples );
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getNumChannels( )
	{
		return getNumChannels( handle );
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getRate( )
	{
		return getRate( handle );
	}
	
	/**
	 * {@inheritDoc}
	 */
	public float getLength( )
	{
		return getLength( handle );
	}
	
	private native long openFile( String filename );	
	
	private native int readSamples( long handle, ShortBuffer buffer, int numSamples );
	
	private native int skipSamples( long handle, int numSamples );
	
	private native int getNumChannels( long handle );
	
	private native int getRate( long handle );
	
	private native float getLength( long handle );
	
	private native void closeFile( long handle );

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() 
	{	
		closeFile( handle );
	}
	
//	public static void main( String[] argv )
//	{
//		Mpg123Decoder decoder = new Mpg123Decoder( "data/threeofaperfectpair.mp3");
//		JoglAudioDevice device = new JoglAudioDevice( false );
//		ShortBuffer buffer = AudioTools.allocateShortBuffer( 1024, decoder.getNumChannels() );
//		short[] samples = new short[1024*decoder.getNumChannels()];
//		
//		System.out.println( "rate: " + decoder.getRate() + ", channels: " + decoder.getNumChannels() + ", length: " + decoder.getLength() );							
//		
//		while( decoder.readSamples( buffer ) > 0 )
//		{			
//			buffer.get(samples);
//			device.writeSamples(samples, 0, 1024*2);
//		}
//		decoder.dispose();
//	}
}

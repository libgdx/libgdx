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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.backends.desktop.JoglAudioDevice;

public class Mpg123Decoder implements Decoder
{
	static
	{
		System.loadLibrary( "gdx" );
	}
	
	public final long handle;
	
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
	public int readSamples(float[] samples) 
	{	
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int readSamples(short[] samples) 
	{	
		return 0;
	}
	
	/** 
	 * @return the number of channels
	 */
	public int getNumChannels( )
	{
		return getNumChannels( handle );
	}
	
	/**
	 * @return the sampling rate
	 */
	public int getRate( )
	{
		return getRate( handle );
	}
	
	/**
	 * @return the length of the track in seconds or 0 if the length could not be estimated
	 */
	public float getLength( )
	{
		return getLength( handle );
	}
	
	private native long openFile( String file );
	
	/**
	 * Reads in numSamples float PCM samples to the provided direct FloatBuffer using the
	 * handle retrievable via {@link NativeMP3Decoder.getHandle()}. This is for 
	 * people who know what they do. Returns the number of samples actually read.
	 * 
	 * @param handle The handle
	 * @param buffer The direct FloatBuffer
	 * @param numSamples The number of samples to read
	 * @return The number of samples read.
	 */
	public native int readSamples( long handle, FloatBuffer buffer, int numSamples );

	/**
	 * Reads in numSamples 16-bit signed PCM samples to the provided direct FloatBuffer using the
	 * handle retrievable via {@link NativeMP3Decoder.getHandle()}. This is for 
	 * people who know what they do. Returns the number of samples actually read.
	 * 
	 * @param handle The handle
	 * @param buffer The direct FloatBuffer
	 * @param numSamples The number of samples to read
	 * @return The number of samples read.
	 */
	public native int readSamples( long handle, ShortBuffer buffer, int numSamples );
	
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
	
	public static void main( String[] argv )
	{
		Mpg123Decoder decoder = new Mpg123Decoder( "data/threeofaperfectpair.mp3");
		System.out.println( "rate: " + decoder.getRate() + ", channels: " + decoder.getNumChannels() + ", length: " + decoder.getLength() );
		JoglAudioDevice device = new JoglAudioDevice( false );
		
		ByteBuffer tmp = ByteBuffer.allocateDirect( 1024 * 2 * decoder.getNumChannels() );
		tmp.order(ByteOrder.nativeOrder());
		ShortBuffer buffer = tmp.asShortBuffer();
		short[] samples = new short[1024*decoder.getNumChannels()];
		
		while( decoder.readSamples( decoder.handle, buffer, 1024 * decoder.getNumChannels() ) > 0 )
		{
			buffer.position(0);
			buffer.get(samples);
			device.writeSamples(samples, 0, 1024*2);
//			System.out.println( "decoded" );
		}
		decoder.dispose();
	}
}

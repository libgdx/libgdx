package com.badlogic.gdx.audio.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

public class NativeMP3Decoder implements Decoder 
{	
	/** the handle to the native mp3 decoder **/
	private int handle;
	
	/** the float buffer used to read in the samples **/
	private FloatBuffer buffer;
	
	/** the short buffer **/
	private ShortBuffer shortBuffer;
	
	public NativeMP3Decoder( String file )
	{
		handle = openFile( file );
		if( handle == -1 )
			throw new IllegalArgumentException( "Couldn't open file '" + file + "'" );
	}
	
	public native int openFile( String file );
	
	public native int readSamples( int handle, FloatBuffer buffer, int samples );
	
	public native int readSamples( int handle, ShortBuffer buffer, int samples );
	
	public native void closeFile( int handle );
	
	public int getHandle( )
	{
		return handle;
	}
	
	@Override
	public int readSamples(float[] samples) 
	{	
		if( buffer == null || buffer.capacity() != samples.length )
		{
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect( samples.length * Float.SIZE / 8 );
			byteBuffer.order(ByteOrder.nativeOrder());
			buffer = byteBuffer.asFloatBuffer();
		}
		
		int readSamples = readSamples( handle, buffer, samples.length );
		if( readSamples == 0 )
		{
			closeFile( handle );
			return 0;
		}
		
		buffer.position(0);
		buffer.get( samples );
		
		return samples.length;
	}
	
	@Override
	public int readSamples(short[] samples) 
	{	
		if( shortBuffer == null || shortBuffer.capacity() != samples.length )
		{
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect( samples.length * Short.SIZE / 8 );
			byteBuffer.order(ByteOrder.nativeOrder());
			shortBuffer = byteBuffer.asShortBuffer();
		}
		
		int readSamples = readSamples( handle, shortBuffer, samples.length );
		if( readSamples == 0 )
		{
			closeFile( handle );
			return 0;
		}
		
		shortBuffer.position(0);
		shortBuffer.get( samples );
		
		return samples.length;
	}

}

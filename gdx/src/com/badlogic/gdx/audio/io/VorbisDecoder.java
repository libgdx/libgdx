package com.badlogic.gdx.audio.io;

import java.nio.ShortBuffer;

import com.badlogic.gdx.audio.analysis.AudioTools;
import com.badlogic.gdx.backends.desktop.JoglAudioDevice;

/**
 * A {@link Decoder} implementation that decodes OGG Vorbis files using
 * libvorbis and libogg
 * @author mzechner
 *
 */
public class VorbisDecoder implements Decoder 
{
	static
	{		
		System.loadLibrary( "gdx" );
	}
	
	/** the handle **/
	private final long handle;
	
	/**
	 * Opens the given file for ogg decoding. Throws an IllegalArugmentException
	 * in case the file could not be opened.
	 * 
	 * @param filename the filename
	 */
	public VorbisDecoder( String filename )
	{
		handle = openFile( filename );
		if( handle == 0 )
			throw new IllegalArgumentException( "couldn't open file '" + filename + "'" );
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() 
	{	
		closeFile( handle );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getLength() 
	{	
		return getLength(handle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumChannels() 
	{	
		return getNumChannels(handle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRate() 
	{	
		return getRate(handle);
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

	private native long openFile( String file );
	
	private native int getNumChannels( long handle );
	
	private native int getRate( long handle );
	
	private native float getLength( long handle );
	
	private native int readSamples( long handle, ShortBuffer buffer, int numSamples );
	
	private native void closeFile( long handle );
	
	public static void main( String[] argv )
	{
		VorbisDecoder decoder = new VorbisDecoder( "data/cloudconnected.ogg" );
		System.out.println( "channels: "+ decoder.getNumChannels() + ", rate: " + decoder.getRate() + ", length: " + decoder.getLength() );;
		
		JoglAudioDevice device = new JoglAudioDevice( decoder.getNumChannels() == 2?false:true );
		ShortBuffer samplesBuffer = AudioTools.allocateShortBuffer( 1024*10, 2 );
		short[] samples = new short[samplesBuffer.capacity()];
				
		while( decoder.readSamples( samplesBuffer ) > 0 )
		{
			samplesBuffer.get(samples);		
			device.writeSamples( samples, 0, samples.length );		
		}
		
		decoder.dispose();
		device.dispose();
	}
}

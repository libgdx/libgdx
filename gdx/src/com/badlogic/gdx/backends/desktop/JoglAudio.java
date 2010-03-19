package com.badlogic.gdx.backends.desktop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

/**
 * An implementation of the {@link Audio} interface for the desktop.
 * 
 * @author mzechner
 *
 */
final class JoglAudio implements Audio, Runnable
{
	/** the thread responsible for mixing JoglSounds **/
	private final Thread thread;
	/** the 44.1khz stereo line to mix the JoglSounds to **/
	private SourceDataLine line;	
	/** list of JoglSoundBuffers currently played back **/
	private List<JoglSoundBuffer> buffers = new ArrayList<JoglSoundBuffer>( );
	
	/**
	 * Helper class used to queue a JoglSound for playback
	 * 
	 * @author mzechner
	 *
	 */
	class JoglSoundBuffer
	{
		public final short[] samples;
		public final int numSamples;
		public int readSamples;				
		
		public JoglSoundBuffer( short[] samples, int numSamples )
		{
			this.samples = samples;			
			this.numSamples = numSamples;
		}
	}
	
	JoglAudio( )
	{
		initLine( );
			
		thread = new Thread( this );
		thread.setDaemon(true);
		
		if( line != null )
			thread.start();
	}
	
	private void initLine( )
	{
		AudioFormat format = new AudioFormat( 44100, 16, 2, true, false );
		try {
			line = AudioSystem.getSourceDataLine( format );
			line.open();
			line.start();
		} catch (LineUnavailableException e) 
		{
			line = null;
			e.printStackTrace();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public AudioDevice newAudioDevice(boolean isMono) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Music newMusic(FileHandle file) 
	{	
		try {			
			JoglMusic music = new JoglMusic( ((JoglFileHandle)file) );			
			return music;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sound newSound(FileHandle file) {
		try {			
			JoglSound sound = new JoglSound( this, ((JoglFileHandle)file) );			
			return sound;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void enqueuSound( JoglSound sound )
	{
		synchronized( this )
		{
			buffers.add( new JoglSoundBuffer( sound.samples, sound.numSamples ) );
		}
	}
	
	@Override
	public void run() 
	{	
		int NUM_SAMPLES = 1000;
		byte[] buffer = new byte[NUM_SAMPLES * 4];
		ShortBuffer shortBuffer = ByteBuffer.wrap( buffer ).asShortBuffer();
		int[] intBuffer = new int[NUM_SAMPLES * 2]; 
		
		
		while( true )
		{
			for( int i = 0; i < buffer.length; i++ )
				buffer[i] = 0;
			for( int i = 0; i < intBuffer.length; i++ )
				intBuffer[i] = 0;
			
			synchronized( this )
			{
				int numBuffers = buffers.size();
				Iterator<JoglSoundBuffer> bufferIter = buffers.iterator();
				while( bufferIter.hasNext() )
				{
					JoglSoundBuffer soundBuffer = bufferIter.next();
					
					for( int i = 0; i < NUM_SAMPLES * 2; i+= 2 )
					{
						if( soundBuffer.readSamples >= soundBuffer.numSamples )
						{
							bufferIter.remove();
							break;
						}
						
						int left = soundBuffer.samples[soundBuffer.readSamples++];
						int right = soundBuffer.samples[soundBuffer.readSamples++];
						intBuffer[i] += left;
						intBuffer[i+1] += right;
					}
				}
								
				if( numBuffers > 0 )
				{
					shortBuffer.position(0);
					for( int i = 0; i < intBuffer.length; i++ )
					{
						int sample = (intBuffer[i] / numBuffers);
						if( sample < Short.MIN_VALUE )
							sample = Short.MIN_VALUE;
						if( sample > Short.MAX_VALUE )
							sample = Short.MAX_VALUE;
						shortBuffer.put( (short)sample );
					}
				}
				
				int readBytes = line.write( buffer, 0, buffer.length );
				while( readBytes != buffer.length )
					readBytes+= line.write( buffer, readBytes, buffer.length - readBytes );
			}			
			
//			try {
//				Thread.sleep( 10 );
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}

}

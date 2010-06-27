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
package com.badlogic.gdx.backends.applet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

/**
 * An implementation of the {@link Audio} interface for the desktop.
 * 
 * @author mzechner
 *
 */
final class AppletAudio implements Audio, Runnable
{		
	/** the audio line for sound effects **/
	private SourceDataLine line;
	
	/** The current buffers to play **/	
	private final List<JoglSoundBuffer> buffers = new ArrayList<JoglSoundBuffer>( );
	
	/** The sound effects thread **/
	private Thread thread;
	
	/**
	 * Helper class for playing back sound effects concurrently.
	 * 
	 * @author mzechner
	 *
	 */
	class JoglSoundBuffer
	{
		private final float[] samples;
		private final AudioFormat format;
		private final float volume;
		private int writtenSamples = 0;
		
		public JoglSoundBuffer( AppletSound sound, float volume ) throws Exception
		{			
			samples = sound.getAudioData( );
			format = sound.getAudioFormat();
			this.volume = volume;
		}
		
		/**
		 * Writes the next numFrames frames to the line for playback 
		 * @return whether playback is done or not.
		 */
		public boolean writeSamples( int numSamples, float[] buffer )
		{
			if( format.getChannels() == 1 )
			{
				int remainingSamples = Math.min( samples.length, writtenSamples + numSamples / 2 );
				for( int i = writtenSamples, j = 0; i < remainingSamples; i++, j+=2 )
				{
					buffer[j] += samples[i] * volume;
					buffer[j+1] += samples[i] * volume;
					writtenSamples++;
				}
			}
			else
			{
				int remainingSamples = Math.min( samples.length, writtenSamples + numSamples );
				for( int i = writtenSamples, j = 0; i < remainingSamples; i+= 2, j+=2 )
				{
					buffer[j] += samples[i] * volume;
					buffer[j+1] += samples[i+1] * volume;
					writtenSamples+=2;
				}							
			}	
			
			if( writtenSamples >= samples.length )
				return false;
			else
				return true;
		}
	}
	
	AppletAudio( )
	{
		try {
			AudioFormat format = new AudioFormat( 44100.0f, 16, 2, true, false );
			line = AudioSystem.getSourceDataLine( format );
			line.open(format, 4410);
			line.start();
			thread = new Thread( this );		
			thread.setDaemon( true );
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public AudioDevice newAudioDevice(boolean isMono) 
	{	
		return new AppletAudioDevice( isMono );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Music newMusic(FileHandle file) 
	{	
		try {			
			AppletMusic music = new AppletMusic( ((AppletFileHandle)file) );			
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
			AppletSound sound = new AppletSound( this, ((AppletFileHandle)file) );			
			return sound;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void enqueueSound( AppletSound sound, float volume )
	{
		try
		{
			synchronized( this )
			{				
				buffers.add( new JoglSoundBuffer( sound, volume ) );
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	public void run() 
	{	
		
		int NUM_SAMPLES = 44100 * 2;
		float[] buffer = new float[NUM_SAMPLES];
		byte[] bytes = new byte[2 * NUM_SAMPLES];				
		
		while( true )
		{			
			int samplesToWrite = line.available() / 2;			
						
			if( samplesToWrite > 0 )
			{
				fillBuffer( buffer, bytes, samplesToWrite );
				int writtenBytes = line.write(bytes, 0, samplesToWrite * 2 );
				while( writtenBytes != samplesToWrite * 2 )
					writtenBytes += line.write( bytes, writtenBytes, samplesToWrite - writtenBytes );			
			}
					
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void fillBuffer( float[] buffer, byte[] bytes, int samplesToWrite )
	{
		for( int i = 0; i < buffer.length; i++ )
			buffer[i] = 0.0f;
		for( int i = 0; i < bytes.length; i++ )
			bytes[i] = 0;
		
		int numBuffers = buffers.size();
		synchronized( this )
		{				
			Iterator<JoglSoundBuffer> bufferIter = buffers.iterator();
			while( bufferIter.hasNext() )
			{							
				JoglSoundBuffer soundBuffer = bufferIter.next();
				if( !soundBuffer.writeSamples(samplesToWrite, buffer) )
					bufferIter.remove();
			}
		}			
		
		if( numBuffers > 0 )
		{			
            for( int i = 0, j = 0; i < samplesToWrite; i++, j+=2 )
            {
            		float fValue = buffer[i];
            		if( fValue > 1 )
            			fValue = 1;
            		if( fValue < -1 )
            			fValue = -1;
                    short value = (short)( fValue * Short.MAX_VALUE);		                    
                    bytes[j] = (byte)(value | 0xff);
                    bytes[j+1] = (byte)(value >> 8 );
            }
		}	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AudioRecorder newAudioRecoder(int samplingRate, boolean isMono) {
		return new AppletAudioRecorder(samplingRate, isMono);
	}
}

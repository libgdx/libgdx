package com.badlogic.gdx.backends.desktop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.badlogic.gdx.audio.Music;

public class JoglMusic implements Music, Runnable
{
	private enum State
	{
		Playing,
		Stopped,
		Paused
	}
	
	private State state = State.Stopped;
	private final Thread thread;
	private final File file;
	private AudioInputStream ain;
	private final SourceDataLine line;
	private final byte[] buffer;
	private boolean looping = false;
	private boolean disposed = false;
	
	public JoglMusic( JoglFileHandle handle ) throws UnsupportedAudioFileException, IOException, LineUnavailableException
	{			
		this.file = handle.getFile();
		
		openAudioInputStream();		
		AudioFormat audioFormat = ain.getFormat();		
		line = AudioSystem.getSourceDataLine( audioFormat );
		line.open(audioFormat); //FIXME reduce latency, gotta reimplement the playback thread.
		line.start();
		buffer = new byte[10000*ain.getFormat().getFrameSize()];		
		ain.close();
		ain = null;
		
				
		thread = new Thread( this );
		thread.setDaemon(true);
		thread.start();
	}
	
	private void openAudioInputStream( ) throws UnsupportedAudioFileException, IOException
	{			
		ain = AudioSystem.getAudioInputStream(file);		
		AudioFormat baseFormat = ain.getFormat();
		AudioFormat  decodedFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				baseFormat.getSampleRate(),
				16,
				baseFormat.getChannels(),
				baseFormat.getChannels() * 2,
				baseFormat.getSampleRate(),
				false);
		
		ain = AudioSystem.getAudioInputStream(decodedFormat, ain);				
	}
	
	@Override
	public void dispose() 
	{	
		disposed = true;
		try {
			thread.join();
			line.close();
			ain.close();
		} catch (Exception e) 
		{
			// nothing we can do here
		}		
	}

	@Override
	public boolean isLooping() 
	{	
		return looping;
	}

	@Override
	public boolean isPlaying() 
	{	
		return state == State.Playing;
	}

	@Override
	public void pause() 
	{	
		synchronized( this )
		{
			if( state == State.Playing )
				state = State.Paused;
		}
	}

	@Override
	public void play() 
	{			
		synchronized( this )
		{
			if( state == State.Playing )
				return;
			
			if( state == State.Paused )
			{
				state = State.Playing;
				return;
			}
			
			try 
			{
				openAudioInputStream();
				state = State.Playing;
			} 
			catch (Exception e) 
			{
				state = State.Stopped;
			}			
		}
	}

	@Override
	public void stop() 
	{	
		synchronized( this )
		{
			if( state == State.Stopped )
				return;
			
			state = State.Stopped;
			line.flush();
			try {
				ain.close();
			} catch (IOException e) 
			{			
				e.printStackTrace();
			}
			ain = null;
		}
	}
	
	@Override
	public void setLooping(boolean isLooping) 
	{	
		looping = isLooping;
	}

	@Override
	public void setVolume(float volume) 
	{	
		try
		{
			volume = Math.min( 1, volume );
			volume = Math.max( 0, volume );
			FloatControl control = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
			control.setValue( -80 + volume * 80 );
		}
		catch( IllegalArgumentException ex )
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void run() 
	{			
		int readBytes = 0;		
		long readSamples = 0;
		
		while( !disposed  )
		{
			synchronized( this )
			{			
				if( state == State.Playing )
				{
					try
					{
						
						readBytes = ain.read( buffer );
						
						if( readBytes != -1 )
						{
							int writtenBytes = line.write( buffer, 0, readBytes );
							while( writtenBytes != readBytes )
								writtenBytes += line.write( buffer, writtenBytes, readBytes - writtenBytes );
							readSamples += readBytes / ain.getFormat().getFrameSize();
						}
						else
						{
							System.out.println( "samples: " + readSamples );
							ain.close();
							if( !isLooping() )
								state = State.Stopped;
							else						
								openAudioInputStream();						
						}								
					}
					catch( Exception ex )
					{
						try {
							ain.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						line.close();
						ex.printStackTrace();
						state = State.Stopped;
						return;
					}
				}
				
				try {
					Thread.sleep( 10 );
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}	
}

package com.badlogic.gdx.tests.desktop;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.backends.desktop.JoglAudioDevice;
import com.badlogic.gdx.backends.desktop.JoglAudioRecorder;


public class SimpleAudioRecorder
{
	public static void main( String[] argv ) throws LineUnavailableException
	{
		AudioDevice device = new JoglAudioDevice( true );
		AudioRecorder recorder = new JoglAudioRecorder( 44100, true );
		
		short[] samples = new short[1024];		
		
		while( true )
		{
			recorder.read( samples, 0, samples.length );								
			device.writeSamples(samples, 0, samples.length );
		}					
	}
}



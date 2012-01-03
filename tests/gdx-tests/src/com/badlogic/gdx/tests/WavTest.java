package com.badlogic.gdx.tests;

import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.analysis.AudioTools;
import com.badlogic.gdx.audio.io.VorbisDecoder;
import com.badlogic.gdx.audio.io.WavDecoder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tests.utils.GdxTest;

public class WavTest extends GdxTest {
	/** the file to playback **/
	private static final String FILE = "data/sell_buy_item.wav";
	/** a VorbisDecoder to read PCM data from the ogg file **/
	WavDecoder decoder;
	/** an AudioDevice for playing back the PCM data **/
	AudioDevice device;
	
	@Override
	public void create () {
		// Create the decoder and log some properties. 
		decoder = new WavDecoder(Gdx.files.internal(FILE));
		Gdx.app.log("WavTest", "channels: " + decoder.getChannels() + ", rate: " + decoder.getRate() + ", length: " + decoder.getLength());

		// Create an audio device for playback
		device = Gdx.audio.newAudioDevice(decoder.getRate(), decoder.getChannels() == 1? true: false);
		
		// start a thread for playback
		Thread playbackThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int readSamples = 0;
				// we need a short[] to pass the data to the AudioDevice
				short[] samples = new short[2048];
				
				// read until we reach the end of the file
				while((readSamples = decoder.readSamples(samples, 0, samples.length)) > 0) {
					Gdx.app.log("WavTest", "read " + readSamples + " samples");
					// write the samples to the AudioDevice
					device.writeSamples(samples, 0, readSamples);
				}
			}
		});
		playbackThread.setDaemon(true);
		playbackThread.start();
	}

	@Override
	public void dispose() {
		// we should synchronize with the thread here
		// left as an excercise to the reader :)
		device.dispose();
		decoder.dispose();
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}

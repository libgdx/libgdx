package com.badlogic.gdx.tests;

import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.analysis.AudioTools;
import com.badlogic.gdx.audio.io.VorbisDecoder;
import com.badlogic.gdx.audio.transform.SoundTouch;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * Demonstrates how to read an OGG file and play it back with a {@link VorbisDecoder} as
 * well as how to alter the audio pitch with the {@link SoundTouch} API exposed in the
 * gdx-audio extension.
 * @author mzechner
 *
 */
public class SoundTouchTest extends GdxTest {
	/** the file to playback **/
	private static final String FILE = "data/cloudconnected.ogg";
	/** a VorbisDecoder to read PCM data from the ogg file **/
	VorbisDecoder decoder;
	/** an AudioDevice for playing back the PCM data **/
	AudioDevice device;
	/** SoundTouch instance to modify the PCM data **/
	SoundTouch soundTouch;
	
	@Override
	public void create () {
		// copy ogg file to SD card, can't playback from assets
		FileHandle externalFile = Gdx.files.external("tmp/test.ogg");
		Gdx.files.internal(FILE).copyTo(externalFile);
		
		// Create the decoder and log some properties. Note that we need
		// and external or absolute file!
		decoder = new VorbisDecoder(externalFile);
		Gdx.app.log("SoundTouchTest", "channels: " + decoder.getChannels() + ", rate: " + decoder.getRate() + ", length: " + decoder.getLength());

		// Create an audio device for playback
		device = Gdx.audio.newAudioDevice(decoder.getRate(), decoder.getChannels() == 1? true: false);
		
		// Create the SoundTouch instance
		soundTouch = new SoundTouch();
		soundTouch.setChannels(decoder.getChannels());
		soundTouch.setSampleRate(decoder.getRate());
		soundTouch.setPitchSemiTones(2);
		
		// start a thread for playback
		Thread playbackThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// we need a short[] to pass the data to the AudioDevice
				short[] samples = new short[2048];
				int readSamples = 0;
				
				// read until we reach the end of the file, we read the file
				// fully as SoundTouch#receiveSamples returns uneven numbers
				// of samples from time to time. Could be solved with a ring
				// buffer :)
				while((readSamples = decoder.readSamples(samples, 0, samples.length)) > 0) {
					// process samples with sound touch, divide by 2 for stereo samples
					// as one sample contains both left and right channel data in SoundTouch.
					soundTouch.putSamples(samples, 0, readSamples / decoder.getChannels());
				}
				
				// read the samples from SoundTouch and play them back
				while((readSamples = soundTouch.receiveSamples(samples, 0, samples.length / decoder.getChannels())) > 0) {
					device.writeSamples(samples, 0, readSamples * decoder.getChannels());
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
		// kill the file again
		Gdx.files.external("tmp/test.ogg").delete();
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}

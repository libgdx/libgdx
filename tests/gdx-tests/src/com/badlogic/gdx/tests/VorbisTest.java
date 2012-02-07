/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.analysis.AudioTools;
import com.badlogic.gdx.audio.io.VorbisDecoder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * Demonstrates how to read and playback an OGG file with the {@link VorbisDecoder} found
 * in the gdx-audio extension.
 * @author mzechner
 *
 */
public class VorbisTest extends GdxTest {
	/** the file to playback **/
	private static final String FILE = "data/engine-2.ogg";
	/** a VorbisDecoder to read PCM data from the ogg file **/
	VorbisDecoder decoder;
	/** an AudioDevice for playing back the PCM data **/
	AudioDevice device;
	
	@Override
	public void create () {
		// copy ogg file to SD card, can't playback from assets
		FileHandle externalFile = Gdx.files.external("tmp/test.ogg");
		Gdx.files.internal(FILE).copyTo(externalFile);
		
		// Create the decoder and log some properties. Note that we need
		// an external or absolute file
		decoder = new VorbisDecoder(externalFile);
		Gdx.app.log("Vorbis", "channels: " + decoder.getChannels() + ", rate: " + decoder.getRate() + ", length: " + decoder.getLength());

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
		// kill the file again
		Gdx.files.external("tmp/test.ogg").delete();
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}

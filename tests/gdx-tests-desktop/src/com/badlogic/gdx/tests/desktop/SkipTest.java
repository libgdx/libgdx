/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.desktop;

import java.nio.ShortBuffer;

import com.badlogic.gdx.audio.analysis.AudioTools;
import com.badlogic.gdx.audio.io.Mpg123Decoder;

public class SkipTest {
	public static void main (String[] argv) {
		Mpg123Decoder decoder = new Mpg123Decoder("data/threeofaperfectpair.mp3");
		ShortBuffer samples = AudioTools.allocateShortBuffer(512, decoder.getNumChannels());
		int skipSamples = decoder.getRate() / 25;
		int i = 0;

		while (decoder.readSamples(samples) > 0) {
			i++;
		}
		decoder.dispose();
		System.out.println(i + " fetches");

		i = 0;
		decoder = new Mpg123Decoder("data/threeofaperfectpair.mp3");
		while (decoder.readSamples(samples) > 0) {
			decoder.skipSamples((skipSamples - 512) * decoder.getNumChannels());

			i++;
		}
		decoder.dispose();
	}
}

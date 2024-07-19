
package com.badlogic.gdx.backends.lwjgl3.audio;

import com.badlogic.gdx.utils.GdxRuntimeException;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.EXTDouble.*;
import static org.lwjgl.openal.EXTFloat32.*;
import static org.lwjgl.openal.EXTMCFormats.*;

public class OpenALUtils {

	/** @param channels The number of channels for the sound. Most commonly 1 (for mono) or 2 (for stereo).
	 * @param bitDepth The number of bits in each sample. Normally 16. Can also be 8, 32, 64.
	 * @return An OpenAL enum for use with {@link OpenALSound} and {@link OpenALMusic} */
	static int determineFormat (int channels, int bitDepth) { // @off
		int format;
		switch (channels) {
			case 1:
				switch (bitDepth) {
					case 8: format = AL_FORMAT_MONO8; break;
					case 16: format = AL_FORMAT_MONO16; break;
					case 32: format = AL_FORMAT_MONO_FLOAT32; break;
					case 64: format = AL_FORMAT_MONO_DOUBLE_EXT; break;
					default: throw new GdxRuntimeException("Audio: Bit depth must be 8, 16, 32 or 64.");
				}
				break;
			case 2: // Doesn't work on mono devices (#6631)
				switch (bitDepth) {
					case 8: format = AL_FORMAT_STEREO8; break;
					case 16: format = AL_FORMAT_STEREO16; break;
					case 32: format = AL_FORMAT_STEREO_FLOAT32; break;
					case 64: format = AL_FORMAT_STEREO_DOUBLE_EXT; break;
					default: throw new GdxRuntimeException("Audio: Bit depth must be 8, 16, 32 or 64.");
				}
				break;
			case 4: format = AL_FORMAT_QUAD16; break; // Works on stereo devices but not mono as above
			case 6: format = AL_FORMAT_51CHN16; break;
			case 7: format = AL_FORMAT_61CHN16; break;
			case 8: format = AL_FORMAT_71CHN16; break;
			default: throw new GdxRuntimeException("Audio: Invalid number of channels. " +
				"Must be mono, stereo, quad, 5.1, 6.1 or 7.1.");
		}
		if (channels >= 4) {
			if (bitDepth == 8) format--; // Use 8-bit AL_FORMAT instead
			else if (bitDepth == 32) format++; // Use 32-bit AL_FORMAT instead
			else if (bitDepth != 16)
				throw new GdxRuntimeException("Audio: Bit depth must be 8, 16 or 32 when 4+ channels are present.");
		}
		return format; // @on
	}

}

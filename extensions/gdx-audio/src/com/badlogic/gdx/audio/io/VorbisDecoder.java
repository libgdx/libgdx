package com.badlogic.gdx.audio.io;
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


import java.nio.ShortBuffer;

import com.badlogic.gdx.utils.SharedLibraryLoader;

/** A {@link Decoder} implementation that decodes OGG Vorbis files using tremor
 * @author mzechner */
public class VorbisDecoder implements Decoder {
	static {
		new SharedLibraryLoader().load("gdx-audio");
	}
	
	/** the handle **/
	private final long handle;

	/** Opens the given file for ogg decoding. Throws an IllegalArugmentException in case the file could not be opened.
	 * 
	 * @param filename the filename */
	public VorbisDecoder (String filename) {
		handle = openFile(filename);
		if (handle == 0) throw new IllegalArgumentException("couldn't open file '" + filename + "'");
	}

	/** {@inheritDoc} */
	@Override
	public void dispose () {
		closeFile(handle);
	}

	/** {@inheritDoc} */
	@Override
	public float getLength () {
		return getLength(handle);
	}

	/** {@inheritDoc} */
	@Override
	public int getNumChannels () {
		return getNumChannels(handle);
	}

	/** {@inheritDoc} */
	@Override
	public int getRate () {
		return getRate(handle);
	}

	/** {@inheritDoc} */
	@Override
	public int readSamples (ShortBuffer samples) {
		int read = readSamples(handle, samples, samples.capacity());
		samples.position(0);
		return read;
	}

	/** {@inheritDoc} */
	@Override
	public int skipSamples (int numSamples) {
		return skipSamples(handle, numSamples);
	}

	/*JNI
	#include <ogg.h>
	#include <ivorbiscodec.h>
	#include <ivorbisfile.h>
	#include <stdlib.h>
	#include <stdio.h>
	
	struct OggFile
	{
		OggVorbis_File* ogg;
		int channels;
		int rate;
		float length;
		int bitstream;
	};
	
	static char buffer[10000];
	 */
	
	private static native long openFile (String filename); /*
		OggVorbis_File* ogg = new OggVorbis_File();
		FILE* file = fopen(filename, "rb" );
	
		if( file == 0 )
		{
			delete ogg;
			return 0;
		}
	
		if( ov_open( file, ogg, NULL, 0 ) != 0 )
		{
			fclose( file );
			delete ogg;
			return 0;
		}
	
		vorbis_info *info = ov_info( ogg, -1 );
		int channels = info->channels;
		int rate = info->rate;
		float length = (float)ov_time_total(ogg, -1 ) / 1000.0f;
	
		OggFile* oggFile = new OggFile();
		oggFile->ogg = ogg;
		oggFile->channels = channels;
		oggFile->rate = rate;
		oggFile->length = length;
	
		return (jlong)oggFile;	
	*/

	private static native int getNumChannels (long handle); /*
		OggFile* file = (OggFile*)handle;
		return file->channels;
	*/

	private static native int getRate (long handle); /*
		OggFile* file = (OggFile*)handle;
		return file->rate;
	*/

	private static native float getLength (long handle); /*
		OggFile* file = (OggFile*)handle;
		return file->length;
	*/

	private static native int readSamples (long handle, ShortBuffer samples, int numSamples); /*
		OggFile* file = (OggFile*)handle;
		int toRead = 2 * numSamples;
		int read = 0;
	
		while( read != toRead )
		{
			int ret = ov_read( file->ogg, (char*)samples + read, toRead - read, &file->bitstream );
			if( ret == OV_HOLE )
				continue;
			if( ret == OV_EBADLINK || ret == OV_EINVAL || ret == 0 )
				return read / 2;
			read+=ret;
		}
		return read / 2;
	*/

	private static native int skipSamples (long handle, int numSamples); /*
		OggFile* file = (OggFile*)handle;
		int toRead = 2 * numSamples;
		int read = 0;
	
		while( read != toRead )
		{
			int ret = ov_read( file->ogg, buffer, (toRead - read)>10000?10000:(toRead-read), &file->bitstream );
			if( ret == OV_HOLE )
				continue;
			if( ret == OV_EBADLINK || ret == OV_EINVAL || ret == 0 )
				return read / 2;
			read+=ret;
		}
	
		return read / 2;
	*/

	private static native void closeFile (long handle); /*
		OggFile* file = (OggFile*)handle;
		ov_clear(file->ogg);
		free(file->ogg);
		free(file);
	*/
}

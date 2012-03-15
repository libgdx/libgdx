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


import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** A {@link Decoder} implementation that decodes MP3 files via libmpg123 natively.
 * 
 * @author mzechner */
public class Mpg123Decoder extends Decoder {
	static {
		new SharedLibraryLoader().load("gdx-audio");
	}
	
	public final long handle;

	/** Opens the given file for mp3 decoding. Throws an IllegalArugmentException in case the file could not be opened.
	 */
	public Mpg123Decoder (FileHandle file) {
		if(file.type() != FileType.External && file.type() != FileType.Absolute)
			throw new IllegalArgumentException("File must be absolute or external!");
		handle = openFile(file.file().getAbsolutePath());
	}

	@Override
	public int readSamples (short[] samples, int offset, int numSamples) {
		int read = readSamples(handle, samples, offset, numSamples);
		return read;
	}

	@Override
	public int skipSamples (int numSamples) {
		return skipSamples(handle, numSamples);
	}

	public int getChannels () {
		return getNumChannels(handle);
	}

	public int getRate () {
		return getRate(handle);
	}

	public float getLength () {
		return getLength(handle);
	}
	
	@Override
	public void dispose () {
		closeFile(handle);
	}

	/*JNI
	extern "C" {
	#include "libmpg123/mpg123.h"
	}
	#include <stdio.h>
	#include <string.h>
	
	struct Mp3File
	{
	        mpg123_handle* handle;
	        int channels;
	        long rate;
	        float length;
	        size_t buffer_size;
	        unsigned char* buffer;
	        int leftSamples;
	        int offset;
	};
	
	void cleanup( mpg123_handle *handle )
	{
	        mpg123_close(handle);
	        mpg123_delete(handle);
	        mpg123_exit();
	}

	static inline int readBuffer( Mp3File* mp3 )
	{
		size_t done = 0;
		int err = mpg123_read( mp3->handle, mp3->buffer, mp3->buffer_size, &done );
	
		mp3->leftSamples = done / 2;
		mp3->offset = 0;
	
		if( err != MPG123_OK )
			return 0;
		else
			return done;
	}
	
	*/
	
	private native long openFile (String filename); /*
		mpg123_handle *mh = NULL;
		int  channels = 0, encoding = 0;
		long rate = 0;
		int  err  = MPG123_OK;
	
		err = mpg123_init();
		if( err != MPG123_OK || (mh = mpg123_new(NULL, &err)) == NULL
				|| mpg123_open(mh, filename) != MPG123_OK
				|| mpg123_getformat(mh, &rate, &channels, &encoding) != MPG123_OK )
		{
			fprintf( stderr, "Trouble with mpg123: %s\n",
					mh==NULL ? mpg123_plain_strerror(err) : mpg123_strerror(mh) );
			cleanup(mh);
			return 0;
		}
	
		if(encoding != MPG123_ENC_SIGNED_16)
		{ 
			// Signed 16 is the default output format anyways; it would actually by only different if we forced it.
		    // So this check is here just for this explanation.
			cleanup(mh);
			return 0;
		}
		// Ensure that this output format will not change (it could, when we allow it).
		mpg123_format_none(mh);
		mpg123_format(mh, rate, channels, encoding);
	
		size_t buffer_size = mpg123_outblock( mh );
		unsigned char* buffer = (unsigned char*)malloc(buffer_size);
		size_t done = 0;
		int samples = 0;
	
		Mp3File* mp3 = new Mp3File();
		mp3->handle = mh;
		mp3->channels = channels;
		mp3->rate = rate;
		mp3->buffer = buffer;
		mp3->buffer_size = buffer_size;
		int length = mpg123_length( mh );
		if( length == MPG123_ERR )
			mp3->length = 0;
		else
			mp3->length = length / rate;
	
		return (jlong)mp3;
	*/

	private native int readSamples (long handle, short[] buffer, int offset, int numSamples); /*	
		Mp3File* mp3 = (Mp3File*)handle;
		short* target = buffer + offset;
	
		int idx = 0;
		while( idx != numSamples )
		{
			if( mp3->leftSamples > 0 )
			{
				short* src = ((short*)mp3->buffer) + mp3->offset;
				for( ; idx < numSamples && mp3->offset < mp3->buffer_size / 2; mp3->leftSamples--, mp3->offset++, target++, src++, idx++ )
				{
					*target = *src;
				}
			}
			else
			{
				int result = readBuffer( mp3 );
				if( result == 0 )
					return 0;
			}
	
		}
	
		if( idx > numSamples )
			return 0;
	
		return idx;
	*/

	private native int skipSamples (long handle, int numSamples); /*
		Mp3File* mp3 = (Mp3File*)handle;
	
		int idx = 0;
		while( idx != numSamples )
		{
			if( mp3->leftSamples > 0 )
			{
				for( ; idx < numSamples && mp3->offset < mp3->buffer_size / 2; mp3->leftSamples--, mp3->offset++, idx++ );
			}
			else
			{
				int result = readBuffer( mp3 );
				if( result == 0 )
					return 0;
			}
	
		}
	
		if( idx > numSamples )
			return 0;
	
		return idx;
	*/

	private native int getNumChannels (long handle); /*
		Mp3File* mp3 = (Mp3File*)handle;
		return mp3->channels;
	*/

	private native int getRate (long handle); /*
		Mp3File* mp3 = (Mp3File*)handle;
		return mp3->rate;
	*/

	private native float getLength (long handle); /*
		Mp3File* mp3 = (Mp3File*)handle;
		return mp3->length;
	*/

	private native void closeFile (long handle); /*
		Mp3File* mp3 = (Mp3File*)handle;
		free(mp3->buffer);
		cleanup(mp3->handle);
	*/
}

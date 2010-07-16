/**
* Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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
*/
#include "Mpg123Decoder.h"
#include "mpg123/mpg123.h"
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

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_openFile(JNIEnv *env, jobject, jstring file)
{
	mpg123_handle *mh = NULL;
	int  channels = 0, encoding = 0;
	long rate = 0;
	int  err  = MPG123_OK;

	err = mpg123_init();
	const char* fileString = env->GetStringUTFChars(file, NULL);
	if( err != MPG123_OK || (mh = mpg123_new(NULL, &err)) == NULL
			|| mpg123_open(mh, fileString) != MPG123_OK
			|| mpg123_getformat(mh, &rate, &channels, &encoding) != MPG123_OK )
	{
		fprintf( stderr, "Trouble with mpg123: %s\n",
				mh==NULL ? mpg123_plain_strerror(err) : mpg123_strerror(mh) );
		cleanup(mh);
		env->ReleaseStringUTFChars(file, fileString);
		return 0;
	}
	env->ReleaseStringUTFChars(file, fileString);

	if(encoding != MPG123_ENC_SIGNED_16)
	{ /* Signed 16 is the default output format anyways; it would actually by only different if we forced it.
	              So this check is here just for this explanation. */
		cleanup(mh);
		return 0;
	}
	/* Ensure that this output format will not change (it could, when we allow it). */
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

/*
 * Class:     com_badlogic_gdx_audio_io_Mpg123Decoder
 * Method:    readSamples
 * Signature: (ILjava/nio/ShortBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_readSamples(JNIEnv *env, jobject, jlong handle, jobject buffer, jint numSamples)
{
	Mp3File* mp3 = (Mp3File*)handle;
	short* target = (short*)env->GetDirectBufferAddress(buffer);

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
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_skipSamples(JNIEnv *env, jobject, jlong handle, jint numSamples)
{
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
}

/*
 * Class:     com_badlogic_gdx_audio_io_Mpg123Decoder
 * Method:    getNumChannels
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_getNumChannels(JNIEnv *, jobject, jlong handle)
{
	Mp3File* mp3 = (Mp3File*)handle;
	return mp3->channels;
}

/*
 * Class:     com_badlogic_gdx_audio_io_Mpg123Decoder
 * Method:    getRate
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_getRate(JNIEnv *, jobject, jlong handle)
{
	Mp3File* mp3 = (Mp3File*)handle;
	return mp3->rate;
}

/*
 * Class:     com_badlogic_gdx_audio_io_Mpg123Decoder
 * Method:    closeFile
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_closeFile(JNIEnv *, jobject, jlong handle)
{
	Mp3File* mp3 = (Mp3File*)handle;
	free(mp3->buffer);
	cleanup(mp3->handle);
}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_getLength(JNIEnv *, jobject, jlong handle)
{
	Mp3File* mp3 = (Mp3File*)handle;
	return mp3->length;
}

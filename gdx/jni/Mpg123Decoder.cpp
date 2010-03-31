#include "Mpg123Decoder.h"
#include "mpg123/mpg123.h"
#include <stdio.h>
#include <string.h>

struct Mp3File
{
	mpg123_handle* handle;
	int channels;
	long rate;
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

	return (jlong)mp3;


}

/*
 * Class:     com_badlogic_gdx_audio_io_Mpg123Decoder
 * Method:    readSamples
 * Signature: (ILjava/nio/FloatBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_readSamples__ILjava_nio_FloatBuffer_2I(JNIEnv *, jobject, jlong, jobject, jint)
{
	return 0;
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
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_readSamples__JLjava_nio_ShortBuffer_2I(JNIEnv *env, jobject, jlong handle, jobject buffer, jint numSamples)
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

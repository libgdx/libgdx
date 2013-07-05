#include <com.badlogic.gdx.audio.io.Mpg123Decoder.h>

//@line:100

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
	
	static inline jlong wrapped_Java_com_badlogic_gdx_audio_io_Mpg123Decoder_openFile
(JNIEnv* env, jobject object, jstring obj_filename, char* filename) {

//@line:142

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
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_openFile(JNIEnv* env, jobject object, jstring obj_filename) {
	char* filename = (char*)env->GetStringUTFChars(obj_filename, 0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_audio_io_Mpg123Decoder_openFile(env, object, obj_filename, filename);

	env->ReleaseStringUTFChars(obj_filename, filename);

	return JNI_returnValue;
}

static inline jint wrapped_Java_com_badlogic_gdx_audio_io_Mpg123Decoder_readSamples
(JNIEnv* env, jobject object, jlong handle, jshortArray obj_buffer, jint offset, jint numSamples, short* buffer) {

//@line:190
	
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
	
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_readSamples(JNIEnv* env, jobject object, jlong handle, jshortArray obj_buffer, jint offset, jint numSamples) {
	short* buffer = (short*)env->GetPrimitiveArrayCritical(obj_buffer, 0);

	jint JNI_returnValue = wrapped_Java_com_badlogic_gdx_audio_io_Mpg123Decoder_readSamples(env, object, handle, obj_buffer, offset, numSamples, buffer);

	env->ReleasePrimitiveArrayCritical(obj_buffer, buffer, 0);

	return JNI_returnValue;
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_skipSamples(JNIEnv* env, jobject object, jlong handle, jint numSamples) {


//@line:220

		Mp3File* mp3 = (Mp3File*)handle;
		off_t skipped = mpg123_seek( mp3->handle, numSamples, SEEK_SET );
		return skipped;
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_getNumChannels(JNIEnv* env, jobject object, jlong handle) {


//@line:226

		Mp3File* mp3 = (Mp3File*)handle;
		return mp3->channels;
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_getRate(JNIEnv* env, jobject object, jlong handle) {


//@line:231

		Mp3File* mp3 = (Mp3File*)handle;
		return mp3->rate;
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_getLength(JNIEnv* env, jobject object, jlong handle) {


//@line:236

		Mp3File* mp3 = (Mp3File*)handle;
		return mp3->length;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_io_Mpg123Decoder_closeFile(JNIEnv* env, jobject object, jlong handle) {


//@line:241

		Mp3File* mp3 = (Mp3File*)handle;
		free(mp3->buffer);
		cleanup(mp3->handle);
	

}


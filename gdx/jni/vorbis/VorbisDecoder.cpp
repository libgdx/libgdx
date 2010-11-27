/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
#include "VorbisDecoder.h"
#include "ogg.h"
#include "ivorbiscodec.h"
#include "ivorbisfile.h"
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

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_openFile(JNIEnv *env, jobject, jstring filename)
{
	char* fileString = (char*)env->GetStringUTFChars(filename, NULL);
	OggVorbis_File* ogg = new OggVorbis_File();
	FILE* file = fopen(fileString, "rb" );
	env->ReleaseStringUTFChars( filename, fileString );

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
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_getNumChannels(JNIEnv *, jobject, jlong handle)
{
	OggFile* file = (OggFile*)handle;
	return file->channels;
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_getRate(JNIEnv *, jobject, jlong handle)
{
	OggFile* file = (OggFile*)handle;
	return file->rate;
}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_getLength(JNIEnv *, jobject, jlong handle)
{
	OggFile* file = (OggFile*)handle;
	return file->length;
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_readSamples(JNIEnv *env, jobject, jlong handle, jobject buffer, jint numSamples)
{
	OggFile* file = (OggFile*)handle;
	char* samples = (char*)env->GetDirectBufferAddress( buffer );
	int toRead = 2 * numSamples;
	int read = 0;

	while( read != toRead )
	{
		int ret = ov_read( file->ogg, samples + read, toRead - read, &file->bitstream );
		if( ret == OV_HOLE )
			continue;
		if( ret == OV_EBADLINK || ret == OV_EINVAL || ret == 0 )
			return read / 2;
		read+=ret;
	}

	return read / 2;
}

static char buffer[10000];

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_skipSamples(JNIEnv *, jobject, jlong handle, jint numSamples)
{
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
}

/*
 * Class:     com_badlogic_gdx_audio_io_VorbisDecoder
 * Method:    closeFile
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_closeFile(JNIEnv *, jobject, jlong handle)
{
	OggFile* file = (OggFile*)handle;
	ov_clear(file->ogg);
	free(file->ogg);
	free(file);
}

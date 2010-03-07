#include "NativeMP3Decoder.h"
#include "mad/mad.h"
#include <stdio.h>
#include <string.h>

#define SHRT_MAX (32767)
#define INPUT_BUFFER_SIZE	(5*8192)
#define OUTPUT_BUFFER_SIZE	8192 /* Must be an integer multiple of 4. */

/**
 * Struct holding the pointer to a wave file.
 */
struct MP3FileHandle
{
	int size;
	FILE* file;
	mad_stream stream;
	mad_frame frame;
	mad_synth synth;
	mad_timer_t timer;
	int leftSamples;
	int offset;
	unsigned char inputBuffer[INPUT_BUFFER_SIZE];
};

/** static WaveFileHandle array **/
static MP3FileHandle* handles[100];

/**
 * Seeks a free handle in the handles array and returns its index or -1 if no handle could be found
 */
static int findFreeHandle( )
{
	for( int i = 0; i < 100; i++ )
	{
		if( handles[i] == 0 )
			return i;
	}

	return -1;
}

static inline void closeHandle( MP3FileHandle* handle )
{
	fclose( handle->file );
	mad_synth_finish(&handle->synth);
	mad_frame_finish(&handle->frame);
	mad_stream_finish(&handle->stream);
	delete handle;
}

static inline signed short fixedToShort(mad_fixed_t Fixed)
{
	if(Fixed>=MAD_F_ONE)
		return(SHRT_MAX);
	if(Fixed<=-MAD_F_ONE)
		return(-SHRT_MAX);

	Fixed=Fixed>>(MAD_F_FRACBITS-15);
	return((signed short)Fixed);
}


JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_NativeMP3Decoder_openFile(JNIEnv *env, jobject obj, jstring file)
{
	int index = findFreeHandle( );

	if( index == -1 )
		return -1;

	const char* fileString = env->GetStringUTFChars(file, NULL);
	FILE* fileHandle = fopen( fileString, "rb" );
	env->ReleaseStringUTFChars(file, fileString);
	if( fileHandle == 0 )
		return -1;

	MP3FileHandle* mp3Handle = new MP3FileHandle( );
	mp3Handle->file = fileHandle;
	fseek( fileHandle, 0, SEEK_END);
	mp3Handle->size = ftell( fileHandle );
	rewind( fileHandle );

	mad_stream_init(&mp3Handle->stream);
	mad_frame_init(&mp3Handle->frame);
	mad_synth_init(&mp3Handle->synth);
	mad_timer_reset(&mp3Handle->timer);

	handles[index] = mp3Handle;
	return index;
}

static inline int readNextFrame( MP3FileHandle* mp3 )
{
	do
	{
		if( mp3->stream.buffer == 0 || mp3->stream.error == MAD_ERROR_BUFLEN )
		{
			int inputBufferSize = 0;
			if( mp3->stream.next_frame != 0 )
			{
				int leftOver = mp3->stream.bufend - mp3->stream.next_frame;
				for( int i = 0; i < leftOver; i++ )
					mp3->inputBuffer[i] = mp3->stream.next_frame[i];
				int readBytes = fread( mp3->inputBuffer + leftOver, 1, INPUT_BUFFER_SIZE - leftOver, mp3->file );
				if( readBytes == 0 )
					return 0;
				inputBufferSize = leftOver + readBytes;
			}
			else
			{
				int readBytes = fread( mp3->inputBuffer, 1, INPUT_BUFFER_SIZE, mp3->file );
				if( readBytes == 0 )
					return 0;
				inputBufferSize = readBytes;
			}

			mad_stream_buffer( &mp3->stream, mp3->inputBuffer, inputBufferSize );
			mp3->stream.error = MAD_ERROR_NONE;
		}

		if( mad_frame_decode( &mp3->frame, &mp3->stream ) )
		{
			if( mp3->stream.error == MAD_ERROR_BUFLEN ||(MAD_RECOVERABLE(mp3->stream.error)))
				continue;
			else
				return 0;
		}
		else
			break;
	} while( true );

	mad_timer_add( &mp3->timer, mp3->frame.header.duration );
	mad_synth_frame( &mp3->synth, &mp3->frame );
	mp3->leftSamples = mp3->synth.pcm.length;
	mp3->offset = 0;

	return -1;
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_NativeMP3Decoder_readSamples__ILjava_nio_FloatBuffer_2I(JNIEnv *env, jobject obj, jint handle, jobject buffer, jint size)
{
	MP3FileHandle* mp3 = handles[handle];
	float* target = (float*)env->GetDirectBufferAddress(buffer);

	int idx = 0;
	while( idx != size )
	{
		if( mp3->leftSamples > 0 )
		{
			for( ; idx < size && mp3->offset < mp3->synth.pcm.length; mp3->leftSamples--, mp3->offset++ )
			{
				int value = fixedToShort(mp3->synth.pcm.samples[0][mp3->offset]);

				if( MAD_NCHANNELS(&mp3->frame.header) == 2 )
				{
					value += fixedToShort(mp3->synth.pcm.samples[1][mp3->offset]);
					value /= 2;
				}

				target[idx++] = value / (float)SHRT_MAX;
			}
		}
		else
		{
			int result = readNextFrame( mp3 );
			if( result == 0 )
				return 0;
		}

	}
	if( idx > size )
		return 0;

	return size;
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_NativeMP3Decoder_readSamples__ILjava_nio_ShortBuffer_2I(JNIEnv *env, jobject obj, jint handle, jobject buffer, jint size)
{
	MP3FileHandle* mp3 = handles[handle];
	short* target = (short*)env->GetDirectBufferAddress(buffer);

	int idx = 0;
	while( idx != size )
	{
		if( mp3->leftSamples > 0 )
		{
			for( ; idx < size && mp3->offset < mp3->synth.pcm.length; mp3->leftSamples--, mp3->offset++ )
			{
				int value = fixedToShort(mp3->synth.pcm.samples[0][mp3->offset]);

				if( MAD_NCHANNELS(&mp3->frame.header) == 2 )
				{
					value += fixedToShort(mp3->synth.pcm.samples[1][mp3->offset]);
					value /= 2;
				}

				target[idx++] = value;
			}
		}
		else
		{
			int result = readNextFrame( mp3 );
			if( result == 0 )
				return 0;
		}

	}
	if( idx > size )
		return 0;

	return size;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_io_NativeMP3Decoder_closeFile(JNIEnv *env, jobject obj, jint handle)
{
	if( handles[handle] != 0 )
	{
		closeHandle( handles[handle] );
		handles[handle] = 0;
	}
}

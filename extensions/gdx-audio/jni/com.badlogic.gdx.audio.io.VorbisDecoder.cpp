#include <com.badlogic.gdx.audio.io.VorbisDecoder.h>

//@line:79

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
	 static inline jlong wrapped_Java_com_badlogic_gdx_audio_io_VorbisDecoder_openFile
(JNIEnv* env, jclass clazz, jstring obj_filename, char* filename) {

//@line:98

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
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_openFile(JNIEnv* env, jclass clazz, jstring obj_filename) {
	char* filename = (char*)env->GetStringUTFChars(obj_filename, 0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_audio_io_VorbisDecoder_openFile(env, clazz, obj_filename, filename);

	env->ReleaseStringUTFChars(obj_filename, filename);

	return JNI_returnValue;
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_getNumChannels(JNIEnv* env, jclass clazz, jlong handle) {


//@line:129

		OggFile* file = (OggFile*)handle;
		return file->channels;
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_getRate(JNIEnv* env, jclass clazz, jlong handle) {


//@line:134

		OggFile* file = (OggFile*)handle;
		return file->rate;
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_getLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:139

		OggFile* file = (OggFile*)handle;
		return file->length;
	

}

static inline jint wrapped_Java_com_badlogic_gdx_audio_io_VorbisDecoder_readSamples
(JNIEnv* env, jclass clazz, jlong handle, jobject obj_samples, jint numSamples, short* samples) {

//@line:144

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
	
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_readSamples(JNIEnv* env, jclass clazz, jlong handle, jobject obj_samples, jint numSamples) {
	short* samples = (short*)env->GetDirectBufferAddress(obj_samples);

	jint JNI_returnValue = wrapped_Java_com_badlogic_gdx_audio_io_VorbisDecoder_readSamples(env, clazz, handle, obj_samples, numSamples, samples);


	return JNI_returnValue;
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_skipSamples(JNIEnv* env, jclass clazz, jlong handle, jint numSamples) {


//@line:161

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

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_io_VorbisDecoder_closeFile(JNIEnv* env, jclass clazz, jlong handle) {


//@line:179

		OggFile* file = (OggFile*)handle;
		ov_clear(file->ogg);
		free(file->ogg);
		free(file);
	

}


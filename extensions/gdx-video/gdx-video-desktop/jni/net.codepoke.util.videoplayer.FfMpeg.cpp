#include <net.codepoke.util.videoplayer.FfMpeg.h>

//@line:165

	 	extern "C"
	 	{
	 	//This makes certain C libraries usable for ffmpeg
	 	#define __STDC_CONSTANT_MACROS
		#include <libavcodec/avcodec.h>
		#include <libavformat/avformat.h>
		#include <libswscale/swscale.h>
		}
		#include "Utilities.h"
	 JNIEXPORT void JNICALL Java_net_codepoke_util_videoplayer_FfMpeg_register(JNIEnv* env, jclass clazz) {


//@line:177

		av_register_all();
		logDebug("av_register_all() called\n");
	 

}

JNIEXPORT void JNICALL Java_net_codepoke_util_videoplayer_FfMpeg_setDebugLoggingNative(JNIEnv* env, jclass clazz, jboolean debugLogging) {


//@line:186

		debug(debugLogging);
	 

}


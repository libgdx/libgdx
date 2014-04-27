#include <net.codepoke.util.videoplayer.VideoDecoder.h>

//@line:106

	 	#include "VideoDecoder.h"
	 	#include "Utilities.h"
	 	
	 	#include <stdexcept>
	 	
	 	JavaVM* jvm = NULL;
	 	JavaVMAttachArgs args;
	 	
	 	struct FfMpegCustomFileReaderData {
            jobject objectToCall;
            jmethodID methodToCall;
        };

        static int ffmpegCustomFileReader(void* data, u_int8_t* buffer, int bufferSize) {
            FfMpegCustomFileReaderData* customData = (FfMpegCustomFileReaderData*)data;
            JNIEnv * env;
		    // double check it's all ok
		    int getEnvStat = jvm->GetEnv((void **)&env, JNI_VERSION_1_6);
		    if (getEnvStat == JNI_EDETACHED) {
		        logDebug("Not attached\n");
		        if (jvm->AttachCurrentThread((void **) &env, NULL) != 0) {
		            logError("Failed to attach\n");
		        }
		    } else if (getEnvStat == JNI_OK) {
		        //
		    } else if (getEnvStat == JNI_EVERSION) {
		        logError("Unsupported version\n");
		    }
		
		jint integer = env->CallIntMethod(customData->objectToCall, customData->methodToCall, env->NewDirectByteBuffer(buffer, bufferSize));
		
		    if (env->ExceptionCheck()) {
		        env->ExceptionDescribe();
		    }
		
		    jvm->DetachCurrentThread();
            logDebug("Size %d on %p\n", bufferSize, buffer);
            return integer;
        }

        static void customReaderDataCleanup(void* data) {
            FfMpegCustomFileReaderData* customData = (FfMpegCustomFileReaderData*)data;
//            customData->env->DeleteGlobalRef(customData->objectToCall);
//            jvm->DetachCurrentThread();
//            customData->env = NULL;
//            customData->objectToCall = NULL;
//            customData->methodToCall = NULL;
        }
		
	 JNIEXPORT jlong JNICALL Java_net_codepoke_util_videoplayer_VideoDecoder_init(JNIEnv* env, jobject object) {


//@line:162

	
		if(jvm == NULL) {
            env->GetJavaVM(&jvm);
        }
	
		args.version = JNI_VERSION_1_6;
		args.name = "FFMpegInternalThread";
		
		VideoDecoder* pointer = new VideoDecoder();
		return (jlong)pointer;
	

}

static inline jobject wrapped_Java_net_codepoke_util_videoplayer_VideoDecoder_loadStream
(JNIEnv* env, jobject object, jobject decodingObject, jstring obj_methodName, char* methodName) {

//@line:184

		VideoDecoder* pointer = getClassPointer<VideoDecoder>(env, object);
		try {
			VideoBufferInfo bufferInfo;
            memset(&bufferInfo, 0, sizeof(VideoBufferInfo));
            FfMpegCustomFileReaderData* data = new FfMpegCustomFileReaderData();
            memset(data, 0, sizeof(FfMpegCustomFileReaderData));
            data->objectToCall = env->NewGlobalRef(decodingObject);
            jclass clazz = env->GetObjectClass(data->objectToCall);
            data->methodToCall = env->GetMethodID(clazz, methodName, "(Ljava/nio/ByteBuffer;)I");
            if(data->methodToCall == NULL) {
                delete data;
                throw std::invalid_argument("Supplied method name invalid! Is it having the correct signature?");
            }

            pointer->loadFile(ffmpegCustomFileReader, data, customReaderDataCleanup, &bufferInfo);
            jobject videoBuffer = NULL;
            jobject audioBuffer = NULL;
            jobject customIOBuffer = NULL;
            if(bufferInfo.videoBuffer != NULL && bufferInfo.videoBufferSize > 0) {
                videoBuffer = env->NewDirectByteBuffer(bufferInfo.videoBuffer, bufferInfo.videoBufferSize);
            }
            if(bufferInfo.audioBuffer != NULL && bufferInfo.audioBufferSize > 0) {
                audioBuffer = env->NewDirectByteBuffer(bufferInfo.audioBuffer, bufferInfo.audioBufferSize);
            }

            jclass cls = env->FindClass("net/codepoke/util/videoplayer/VideoDecoder$VideoDecoderBuffers");
            if(cls == NULL) {
                logError("[wrapped_Java_net_codepoke_util_videoplayer_VideoDecoder_loadFile] Could not find VideoDecoderBuffers class");
                return NULL;
            }
            jmethodID constructor = env->GetMethodID(cls, "<init>", "(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;IIII)V");
            return env->NewObject(cls, constructor, videoBuffer, audioBuffer, bufferInfo.videoWidth, bufferInfo.videoHeight, bufferInfo.audioChannels, bufferInfo.audioSampleRate);
		} catch(std::runtime_error e) {
			logDebug("Caught exception \n");
			jclass clazz = env->FindClass("java/lang/Exception");
			if(clazz == 0) { //Something went horribly wrong here...
				return 0;
			}
			env->ThrowNew(clazz, e.what());
		} catch(std::invalid_argument e) {
			jclass clazz = env->FindClass("java/lang/IllegalArgumentException");
			if(clazz == 0) { //Something went horribly wrong here...
				return 0;
			}
			env->ThrowNew(clazz, e.what());
		}
		return 0;
	
}

JNIEXPORT jobject JNICALL Java_net_codepoke_util_videoplayer_VideoDecoder_loadStream(JNIEnv* env, jobject object, jobject decodingObject, jstring obj_methodName) {
	char* methodName = (char*)env->GetStringUTFChars(obj_methodName, 0);

	jobject JNI_returnValue = wrapped_Java_net_codepoke_util_videoplayer_VideoDecoder_loadStream(env, object, decodingObject, obj_methodName, methodName);

	env->ReleaseStringUTFChars(obj_methodName, methodName);

	return JNI_returnValue;
}

JNIEXPORT jobject JNICALL Java_net_codepoke_util_videoplayer_VideoDecoder_nextVideoFrame(JNIEnv* env, jobject object) {


//@line:238

		VideoDecoder* pointer = getClassPointer<VideoDecoder>(env, object);
		u_int8_t* buffer = pointer->nextVideoFrame();
		
		return (buffer == NULL) ? NULL : env->NewDirectByteBuffer(buffer, pointer->getVideoFrameSize());
	

}

JNIEXPORT void JNICALL Java_net_codepoke_util_videoplayer_VideoDecoder_updateAudioBuffer(JNIEnv* env, jobject object) {


//@line:249

		VideoDecoder* pointer = getClassPointer<VideoDecoder>(env, object);
		pointer->updateAudioBuffer();
	

}

JNIEXPORT jdouble JNICALL Java_net_codepoke_util_videoplayer_VideoDecoder_getCurrentFrameTimestamp(JNIEnv* env, jobject object) {


//@line:259

		VideoDecoder* pointer = getClassPointer<VideoDecoder>(env, object);
		return pointer->getCurrentFrameTimestamp();
	

}

JNIEXPORT void JNICALL Java_net_codepoke_util_videoplayer_VideoDecoder_disposeNative(JNIEnv* env, jobject object) {


//@line:267

		VideoDecoder* pointer = getClassPointer<VideoDecoder>(env, object);
		FfMpegCustomFileReaderData* data = (FfMpegCustomFileReaderData*)pointer->getCustomFileBufferFuncData();
        if(data != NULL) {
            delete data;
        }
		delete pointer;
	

}

JNIEXPORT jboolean JNICALL Java_net_codepoke_util_videoplayer_VideoDecoder_isBuffered(JNIEnv* env, jobject object) {


//@line:279

		VideoDecoder* pointer = getClassPointer<VideoDecoder>(env, object);
		return pointer->isBuffered();
	

}


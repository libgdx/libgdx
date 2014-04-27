package net.codepoke.util.videoplayer;

import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.Disposable;

public class VideoDecoder
		implements AutoCloseable, Disposable {
	/**
	 * This value should not be used or altered in any way. It is used to store the pointer to the native object, for
	 * which this object is a wrapper.
	 */
	private long nativePointer;

	public static class VideoDecoderBuffers {
		private ByteBuffer videoBuffer;
		private ByteBuffer audioBuffer;
		private int videoWidth;
		private int videoHeight;
		private int audioChannels;
		private int audioSampleRate;

		// If constructor parameters are changed, please also update the native code to call the new constructor!
		private VideoDecoderBuffers(ByteBuffer videoBuffer, ByteBuffer audioBuffer, int videoWidth, int videoHeight, int audioChannels,
									int audioSampleRate) {
			this.videoBuffer = videoBuffer;
			this.audioBuffer = audioBuffer;
			this.videoWidth = videoWidth;
			this.videoHeight = videoHeight;
			this.audioChannels = audioChannels;
			this.audioSampleRate = audioSampleRate;
		}

		/**
		 * @return The audiobuffer
		 */
		public ByteBuffer getAudioBuffer() {
			return audioBuffer;
		}

		/**
		 * @return The videobuffer
		 */
		public ByteBuffer getVideoBuffer() {
			return videoBuffer;
		}

		/**
		 * @return The amount of audio channels
		 */
		public int getAudioChannels() {
			return audioChannels;
		}

		/**
		 * @return The audio's samplerate
		 */
		public int getAudioSampleRate() {
			return audioSampleRate;
		}

		/**
		 * @return The height of the video
		 */
		public int getVideoHeight() {
			return videoHeight;
		}

		/**
		 * @return The width of the video
		 */
		public int getVideoWidth() {
			return videoWidth;
		}
	}

	/**
	 * Constructs a VideoDecoder
	 */
	public VideoDecoder() {
		if (!FfMpeg.isLoaded())
			throw new IllegalStateException("The native libraries are not yet loaded!");
		nativePointer = init();
	}

	/**
	 * This will close the VideoDecoder, and with it cleanup everything.
	 */
	public void close() {
		disposeNative();
		nativePointer = 0;
	}

	/**
	 * Calls close
	 */
	public void dispose() {
		close();
	}

	/*
	 * Native functions
	 * @formatter:off
	 */
	
	/*JNI
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
		
	 */
	
	/**
	 * Creates an instance on the native side.
	 * @return A raw pointer to the native instance
	 */
	private native long init();/*
	
		if(jvm == NULL) {
            env->GetJavaVM(&jvm);
        }
	
		args.version = JNI_VERSION_1_6;
		args.name = "FFMpegInternalThread";
		
		VideoDecoder* pointer = new VideoDecoder();
		return (jlong)pointer;
	*/

	/**
	 * This will load a file for playback
	 * @param decodingObject The instance on which the next parameter should be used.
	 * @param methodSignature	The name of the function that should be called on the provided object. (The
	 * 							function should have return type int, and should accept a single ByteBuffer).
	 * @return A VideoDecoderBuffers object which contains all the information that may be needed about the video.
	 * @throws IllegalArgumentException When the filename is invalid.
	 * @throws Exception Runtime exceptions in c++, which can have different causes.
	 */
	public native VideoDecoderBuffers loadStream(Object decodingObject, String methodName) throws IllegalArgumentException, Exception;/*
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
	*/
	
	/**
	 * This will return a ByteBuffer pointing to the next videoframe. This bytebuffer contains a single frame in RGB888.
	 * @return A ByteBuffer pointing to the next frame.
	 */
	public native ByteBuffer nextVideoFrame();/*
		VideoDecoder* pointer = getClassPointer<VideoDecoder>(env, object);
		u_int8_t* buffer = pointer->nextVideoFrame();
		
		return (buffer == NULL) ? NULL : env->NewDirectByteBuffer(buffer, pointer->getVideoFrameSize());
	*/
	
	/**
	 * This will fill the ByteBuffer for the audio (The one gotten from VideoDecoderBuffers object retrieved from loadFile) 
	 * with new audio.
	 */
	public native void updateAudioBuffer();/*
		VideoDecoder* pointer = getClassPointer<VideoDecoder>(env, object);
		pointer->updateAudioBuffer();
	*/
	
	/**
	 * This gets the timestamp of the current displaying frame (The one that you got last by calling nextVideoFrame). The 
	 * timestamp is in seconds, and can be total nonsense if you never called nextVideoFrame. It is being corrected when the audio couldn't keep up.
	 * @return The timestamp in seconds.
	 */
	public native double getCurrentFrameTimestamp();/*
		VideoDecoder* pointer = getClassPointer<VideoDecoder>(env, object);
		return pointer->getCurrentFrameTimestamp();
	*/
	
	/**
	 * Disposes the native object.
	 */
	private native void disposeNative();/*
		VideoDecoder* pointer = getClassPointer<VideoDecoder>(env, object);
		FfMpegCustomFileReaderData* data = (FfMpegCustomFileReaderData*)pointer->getCustomFileBufferFuncData();
        if(data != NULL) {
            delete data;
        }
		delete pointer;
	*/

	/**
	 * @return Whether the buffer is completely filled.
	 */
	public native boolean isBuffered();/*
		VideoDecoder* pointer = getClassPointer<VideoDecoder>(env, object);
		return pointer->isBuffered();
	*/
}

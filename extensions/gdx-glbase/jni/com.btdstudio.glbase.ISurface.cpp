#include <com.btdstudio.glbase.ISurface.h>

//@line:5

	    #include "Isurface.h"
    JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_ISurface_getTexture(JNIEnv* env, jclass clazz, jlong handle) {


//@line:20

		return ((ISurface*)handle)->getTexture();
	

}


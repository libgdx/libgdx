package org.moe.svm;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.oracle.svm.core.jni.JNIRuntimeAccess;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.nio.*;

public class ConfigCollectionFeature implements Feature {

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        try {
            JNIRuntimeAccess.register(String.class);
            JNIRuntimeAccess.register(DoubleBuffer.class, IntBuffer.class, FloatBuffer.class, Buffer.class, LongBuffer.class, CharBuffer.class, ByteBuffer.class, ShortBuffer.class);
            RuntimeReflection.register(GlyphLayout.class.getConstructor());
            RuntimeReflection.register(GlyphLayout.GlyphRun.class.getConstructor());
            RuntimeReflection.register(Color.class.getConstructor());
        }catch (NoSuchMethodException e){
            e.printStackTrace();
        }
    }
}

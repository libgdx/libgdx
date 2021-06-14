package com.badlogic.gdx.backends.svm;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Array;
import com.oracle.svm.core.jni.JNIRuntimeAccess;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import com.badlogic.gdx.scenes.scene2d.actions.*;

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
            RuntimeReflection.register(Table.DebugRect.class.getConstructor());
            RuntimeReflection.register(AddAction.class.getConstructor(), RemoveAction.class.getConstructor(), MoveToAction.class.getConstructor(), MoveByAction.class.getConstructor(), SizeToAction.class.getConstructor(), SizeByAction.class.getConstructor(), ScaleToAction.class.getConstructor(), ScaleByAction.class.getConstructor(), RotateToAction.class.getConstructor(), RotateByAction.class.getConstructor(), ColorAction.class.getConstructor(), AlphaAction.class.getConstructor(), VisibleAction.class.getConstructor(), TouchableAction.class.getConstructor(), RemoveActorAction.class.getConstructor(), DelayAction.class.getConstructor(), TimeScaleAction.class.getConstructor(), SequenceAction.class.getConstructor(), ParallelAction.class.getConstructor(), RepeatAction.class.getConstructor(), RunnableAction.class.getConstructor(), LayoutAction.class.getConstructor(), AfterAction.class.getConstructor(), AddListenerAction.class.getConstructor(), RemoveListenerAction.class.getConstructor(), Array.class.getConstructor(), Rectangle.class.getConstructor(), ChangeListener.ChangeEvent.class.getConstructor(), Net.HttpRequest.class.getConstructor(), InputEvent.class.getConstructor(), Stage.TouchFocus.class.getConstructor(), FocusListener.FocusEvent.class.getConstructor());
        }catch (NoSuchMethodException e){
            e.printStackTrace();
        }
    }
}

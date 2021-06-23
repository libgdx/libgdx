package com.badlogic.gdx.backends.svm;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardControllerRenderData;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.*;
import com.oracle.svm.core.jni.JNIRuntimeAccess;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import com.badlogic.gdx.scenes.scene2d.actions.*;

import java.nio.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ConfigCollectionFeatureBase implements Feature {

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        try {
            // TODO: 05.07.2021 Many of the added classes/fields come from the json parser loading the assets. Maybe it shouldn't be always included
            //  An idea could be an analyzer of the assets to collect the used fields/classes.

            JNIRuntimeAccess.register(String.class);
            JNIRuntimeAccess.register(DoubleBuffer.class, IntBuffer.class, FloatBuffer.class, Buffer.class, LongBuffer.class, CharBuffer.class, ByteBuffer.class, ShortBuffer.class);
            RuntimeReflection.register(GlyphLayout.class.getConstructor());
            RuntimeReflection.register(GlyphLayout.GlyphRun.class.getConstructor());
            RuntimeReflection.register(Color.class.getConstructor());
            RuntimeReflection.register(Table.DebugRect.class.getConstructor());
            RuntimeReflection.register(AddAction.class.getConstructor(), RemoveAction.class.getConstructor(), MoveToAction.class.getConstructor(), MoveByAction.class.getConstructor(), SizeToAction.class.getConstructor(), SizeByAction.class.getConstructor(), ScaleToAction.class.getConstructor(), ScaleByAction.class.getConstructor(), RotateToAction.class.getConstructor(), RotateByAction.class.getConstructor(), ColorAction.class.getConstructor(), AlphaAction.class.getConstructor(), VisibleAction.class.getConstructor(), TouchableAction.class.getConstructor(), RemoveActorAction.class.getConstructor(), DelayAction.class.getConstructor(), TimeScaleAction.class.getConstructor(), SequenceAction.class.getConstructor(), ParallelAction.class.getConstructor(), RepeatAction.class.getConstructor(), RunnableAction.class.getConstructor(), LayoutAction.class.getConstructor(), AfterAction.class.getConstructor(), AddListenerAction.class.getConstructor(), RemoveListenerAction.class.getConstructor(), Array.class.getConstructor(), Rectangle.class.getConstructor(), ChangeListener.ChangeEvent.class.getConstructor(), Net.HttpRequest.class.getConstructor(), InputEvent.class.getConstructor(), Stage.TouchFocus.class.getConstructor(), FocusListener.FocusEvent.class.getConstructor());
            RuntimeReflection.register(BitmapFont.class);
            RuntimeReflection.register(Color.class);
            RuntimeReflection.register(Color.class.getDeclaredFields());
            RuntimeReflection.register(Button.ButtonStyle.class, CheckBox.CheckBoxStyle.class, ImageButton.ImageButtonStyle.class, ImageTextButton.ImageTextButtonStyle.class, Label.LabelStyle.class, List.ListStyle.class, ProgressBar.ProgressBarStyle.class, ScrollPane.ScrollPaneStyle.class, SelectBox.SelectBoxStyle.class, Skin.TintedDrawable.class, Slider.SliderStyle.class, SplitPane.SplitPaneStyle.class, Table.DebugRect.class, TextButton.TextButtonStyle.class, TextField.TextFieldFilter.DigitsOnlyFilter.class, TextField.DefaultOnscreenKeyboard.class, TextField.TextFieldStyle.class, TextTooltip.TextTooltipStyle.class, Touchpad.TouchpadStyle.class, Tree.TreeStyle.class, Value.Fixed.class, Window.WindowStyle.class);
            RuntimeReflection.register(Skin.class.getConstructor(), Button.ButtonStyle.class.getConstructor(), CheckBox.CheckBoxStyle.class.getConstructor(), ImageButton.ImageButtonStyle.class.getConstructor(), ImageTextButton.ImageTextButtonStyle.class.getConstructor(), Label.LabelStyle.class.getConstructor(), List.ListStyle.class.getConstructor(), ProgressBar.ProgressBarStyle.class.getConstructor(), ScrollPane.ScrollPaneStyle.class.getConstructor(), SelectBox.SelectBoxStyle.class.getConstructor(), Skin.TintedDrawable.class.getConstructor(), Slider.SliderStyle.class.getConstructor(), SplitPane.SplitPaneStyle.class.getConstructor(), Table.DebugRect.class.getConstructor(), TextButton.TextButtonStyle.class.getConstructor(), TextField.TextFieldFilter.DigitsOnlyFilter.class.getConstructor(), TextField.DefaultOnscreenKeyboard.class.getConstructor(), TextField.TextFieldStyle.class.getConstructor(), TextTooltip.TextTooltipStyle.class.getConstructor(), Touchpad.TouchpadStyle.class.getConstructor(), Tree.TreeStyle.class.getConstructor(), Value.Fixed.class.getConstructor(float.class), Window.WindowStyle.class.getConstructor());
            RuntimeReflection.register(Vector2.class.getConstructor(), Vector3.class.getConstructor());
            RuntimeReflection.register(concatArrays(Vector2.class.getDeclaredFields(), Vector3.class.getDeclaredFields()));
            RuntimeReflection.register(concatArrays(Button.ButtonStyle.class.getDeclaredFields(), CheckBox.CheckBoxStyle.class.getDeclaredFields(), ImageButton.ImageButtonStyle.class.getDeclaredFields(), ImageTextButton.ImageTextButtonStyle.class.getDeclaredFields(), Label.LabelStyle.class.getDeclaredFields(), List.ListStyle.class.getDeclaredFields(), ProgressBar.ProgressBarStyle.class.getDeclaredFields(), ScrollPane.ScrollPaneStyle.class.getDeclaredFields(), SelectBox.SelectBoxStyle.class.getDeclaredFields(), Skin.TintedDrawable.class.getDeclaredFields(), Slider.SliderStyle.class.getDeclaredFields(), SplitPane.SplitPaneStyle.class.getDeclaredFields(), Table.DebugRect.class.getDeclaredFields(), TextButton.TextButtonStyle.class.getDeclaredFields(), TextField.TextFieldFilter.DigitsOnlyFilter.class.getDeclaredFields(), TextField.DefaultOnscreenKeyboard.class.getDeclaredFields(), TextField.TextFieldStyle.class.getDeclaredFields(), TextTooltip.TextTooltipStyle.class.getDeclaredFields(), Touchpad.TouchpadStyle.class.getDeclaredFields(), Tree.TreeStyle.class.getDeclaredFields(), Value.Fixed.class.getDeclaredFields(), Window.WindowStyle.class.getDeclaredFields()));
            RuntimeReflection.register(Class.forName("com.badlogic.gdx.backends.iosmoe.IOSMusic$1").getMethods());
            RuntimeReflection.register(Class.forName("com.badlogic.gdx.backends.iosmoe.IOSInput$4").getMethods());
            RuntimeReflection.register(Class.forName("com.badlogic.gdx.backends.iosmoe.IOSInput$5").getMethods());
            RuntimeClassInitialization.initializeAtBuildTime(UIUtils.class);

            JNIRuntimeAccess.register(Class.forName("com.badlogic.gdx.backends.iosmoe.IOSMusic$1").getMethods());
            JNIRuntimeAccess.register(Class.forName("com.badlogic.gdx.backends.iosmoe.IOSInput$4").getMethods());
            JNIRuntimeAccess.register(Class.forName("com.badlogic.gdx.backends.iosmoe.IOSInput$5").getMethods());

            RuntimeReflection.register(BillboardControllerRenderData[].class);
            RuntimeReflection.register(ObjectMap.class.getConstructor(), ObjectIntMap.class.getConstructor(), ObjectFloatMap.class.getConstructor(), ObjectSet.class.getConstructor(), IntMap.class.getConstructor(), LongMap.class.getConstructor(), IntSet.class.getConstructor(), ArrayMap.class.getConstructor());
            RuntimeReflection.register(ResourceData.class.getConstructor());
            RuntimeReflection.register(ResourceData.SaveData[].class.getConstructor());
        }catch (NoSuchMethodException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public <T> T[] concatArrays(T[]... arrays) {
        ArrayList<T> list = new ArrayList<>();
        for (T[] array : arrays) {
            Collections.addAll(list, array);
        }
        return list.toArray(arrays[0]);
    }
}

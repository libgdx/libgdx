/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.bindings.metalangle;


import apple.coregraphics.struct.CGSize;
import apple.foundation.NSArray;
import apple.foundation.NSCoder;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSSet;
import apple.quartzcore.CALayer;
import apple.quartzcore.protocol.CAAction;
import org.moe.natj.c.ann.FunctionPtr;
import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.ByValue;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Library;
import org.moe.natj.general.ann.Mapped;
import org.moe.natj.general.ann.MappedReturn;
import org.moe.natj.general.ann.NFloat;
import org.moe.natj.general.ann.NInt;
import org.moe.natj.general.ann.NUInt;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.general.ptr.VoidPtr;
import org.moe.natj.objc.Class;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.SEL;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.ProtocolClassMethod;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;

@Generated
@Library("MetalANGLE")
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class MGLLayer extends CALayer {
    static {
        NatJ.register();
    }

    @Generated
    protected MGLLayer(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native MGLLayer alloc();

    @Generated
    @Selector("allocWithZone:")
    @MappedReturn(ObjCObjectMapper.class)
    public static native Object allocWithZone(VoidPtr zone);

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(String key);

    /**
     * Bind default framebuffer. Use this after drawing to offscreen FBO.
     */
    @Generated
    @Selector("bindDefaultFrameBuffer")
    public native void bindDefaultFrameBuffer();

    @Generated
    @Selector("cancelPreviousPerformRequestsWithTarget:")
    public static native void cancelPreviousPerformRequestsWithTarget(@Mapped(ObjCObjectMapper.class) Object aTarget);

    @Generated
    @Selector("cancelPreviousPerformRequestsWithTarget:selector:object:")
    public static native void cancelPreviousPerformRequestsWithTargetSelectorObject(
            @Mapped(ObjCObjectMapper.class) Object aTarget, SEL aSelector,
            @Mapped(ObjCObjectMapper.class) Object anArgument);

    @Generated
    @Selector("classFallbacksForKeyedArchiver")
    public static native NSArray<String> classFallbacksForKeyedArchiver();

    @Generated
    @Selector("classForKeyedUnarchiver")
    public static native Class classForKeyedUnarchiver();

    @Generated
    @Selector("cornerCurveExpansionFactor:")
    @NFloat
    public static native double cornerCurveExpansionFactor(String curve);

    @Generated
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("defaultActionForKey:")
    @MappedReturn(ObjCObjectMapper.class)
    public static native CAAction defaultActionForKey(String event);

    /**
     * Default OpenGL id of the framebuffer storing content of this layer.
     * Might not necessary be zero.
     */
    @Generated
    @Selector("defaultOpenGLFrameBufferID")
    public native int defaultOpenGLFrameBufferID();

    @Generated
    @Selector("defaultValueForKey:")
    @MappedReturn(ObjCObjectMapper.class)
    public static native Object defaultValueForKey(String key);

    @Generated
    @Selector("description")
    public static native String description_static();

    /**
     * Default is RGBA8888
     */
    @Generated
    @Selector("drawableColorFormat")
    public native int drawableColorFormat();

    /**
     * Default is DepthNone
     */
    @Generated
    @Selector("drawableDepthFormat")
    public native int drawableDepthFormat();

    @Generated
    @Selector("drawableMultisample")
    public native int drawableMultisample();

    /**
     * Return the size of the OpenGL framebuffer.
     */
    @Generated
    @Selector("drawableSize")
    @ByValue
    public native CGSize drawableSize();

    /**
     * Default is StencilNone
     */
    @Generated
    @Selector("drawableStencilFormat")
    public native int drawableStencilFormat();

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("init")
    public native MGLLayer init();

    @Generated
    @Selector("initWithCoder:")
    public native MGLLayer initWithCoder(NSCoder coder);

    @Generated
    @Selector("initWithLayer:")
    public native MGLLayer initWithLayer(@Mapped(ObjCObjectMapper.class) Object layer);

    @Generated
    @Selector("instanceMethodForSelector:")
    @FunctionPtr(name = "call_instanceMethodForSelector_ret")
    public static native Function_instanceMethodForSelector_ret instanceMethodForSelector(SEL aSelector);

    @Generated
    @Selector("instanceMethodSignatureForSelector:")
    public static native NSMethodSignature instanceMethodSignatureForSelector(SEL aSelector);

    @Generated
    @Selector("instancesRespondToSelector:")
    public static native boolean instancesRespondToSelector(SEL aSelector);

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(String key);

    @Generated
    @Selector("layer")
    public static native MGLLayer layer();

    @Generated
    @Selector("needsDisplayForKey:")
    public static native boolean needsDisplayForKey(String key);

    @Generated
    @Owned
    @Selector("new")
    @MappedReturn(ObjCObjectMapper.class)
    public static native Object new_objc();

    /**
     * Present the content of OpenGL backed framebuffer on screen as soon as possible.
     */
    @Generated
    @Selector("present")
    public native boolean present();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    /**
     * Default value is NO. Setting to YES will keep the framebuffer data after presenting.
     * Doing so will reduce performance and increase memory usage.
     */
    @Generated
    @Selector("retainedBacking")
    public native boolean retainedBacking();

    /**
     * Default is RGBA8888
     */
    @Generated
    @Selector("setDrawableColorFormat:")
    public native void setDrawableColorFormat(int value);

    /**
     * Default is DepthNone
     */
    @Generated
    @Selector("setDrawableDepthFormat:")
    public native void setDrawableDepthFormat(int value);

    @Generated
    @Selector("setDrawableMultisample:")
    public native void setDrawableMultisample(int value);

    /**
     * Default is StencilNone
     */
    @Generated
    @Selector("setDrawableStencilFormat:")
    public native void setDrawableStencilFormat(int value);

    /**
     * Default value is NO. Setting to YES will keep the framebuffer data after presenting.
     * Doing so will reduce performance and increase memory usage.
     */
    @Generated
    @Selector("setRetainedBacking:")
    public native void setRetainedBacking(boolean value);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    @Generated
    @Selector("superclass")
    public static native Class superclass_static();

    @Generated
    @Selector("supportsSecureCoding")
    public static native boolean supportsSecureCoding();

    @Generated
    @ProtocolClassMethod("supportsSecureCoding")
    public boolean _supportsSecureCoding() {
        return supportsSecureCoding();
    }

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();
}
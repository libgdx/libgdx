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


import apple.coregraphics.struct.CGRect;
import apple.foundation.NSArray;
import apple.foundation.NSBundle;
import apple.foundation.NSCoder;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSSet;
import apple.uikit.UIViewController;
import com.badlogic.gdx.backends.bindings.metalangle.protocol.MGLKViewControllerDelegate;
import com.badlogic.gdx.backends.bindings.metalangle.protocol.MGLKViewDelegate;
import org.moe.natj.c.ann.FunctionPtr;
import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.ByValue;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Library;
import org.moe.natj.general.ann.Mapped;
import org.moe.natj.general.ann.MappedReturn;
import org.moe.natj.general.ann.NInt;
import org.moe.natj.general.ann.NUInt;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.general.ptr.VoidPtr;
import org.moe.natj.objc.Class;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.SEL;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;

@Generated
@Library("MetalANGLE")
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class MGLKViewController extends UIViewController implements MGLKViewDelegate {
    static {
        NatJ.register();
    }

    @Generated
    protected MGLKViewController(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native MGLKViewController alloc();

    @Generated
    @Selector("allocWithZone:")
    @MappedReturn(ObjCObjectMapper.class)
    public static native Object allocWithZone(VoidPtr zone);

    @Generated
    @Selector("attemptRotationToDeviceOrientation")
    public static native void attemptRotationToDeviceOrientation();

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(String key);

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
    @Selector("clearTextInputContextIdentifier:")
    public static native void clearTextInputContextIdentifier(String identifier);

    @Generated
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("delegate")
    @MappedReturn(ObjCObjectMapper.class)
    public native MGLKViewControllerDelegate delegate();

    @Generated
    @Selector("description")
    public static native String description_static();

    @Generated
    @Selector("framesDisplayed")
    @NInt
    public native long framesDisplayed();

    @Generated
    @Selector("glView")
    public native MGLKView glView();

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("init")
    public native MGLKViewController init();

    @Generated
    @Selector("initWithCoder:")
    public native MGLKViewController initWithCoder(NSCoder coder);

    @Generated
    @Selector("initWithNibName:bundle:")
    public native MGLKViewController initWithNibNameBundle(String nibNameOrNil, NSBundle nibBundleOrNil);

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
    @Selector("isPaused")
    public native boolean isPaused();

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(String key);

    @Generated
    @Selector("mglkView:drawInRect:")
    public native void mglkViewDrawInRect(MGLKView view, @ByValue CGRect rect);

    @Generated
    @Owned
    @Selector("new")
    @MappedReturn(ObjCObjectMapper.class)
    public static native Object new_objc();

    @Generated
    @Selector("pauseOnWillResignActive")
    public native boolean pauseOnWillResignActive();

    /**
     * The default value is 30.
     * On iOS:
     *  - Setting to 0 or 1 will sync the framerate with display's refresh rate
     * On macOS:
     *  - Setting to 1 will sync the framerate with display's refresh rate
     *  - Setting to 0 will display the frames as fast as possible.
     */
    @Generated
    @Selector("preferredFramesPerSecond")
    @NInt
    public native long preferredFramesPerSecond();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    @Generated
    @Selector("resumeOnDidBecomeActive")
    public native boolean resumeOnDidBecomeActive();

    @Generated
    @Selector("setDelegate:")
    public native void setDelegate_unsafe(@Mapped(ObjCObjectMapper.class) MGLKViewControllerDelegate value);

    @Generated
    public void setDelegate(@Mapped(ObjCObjectMapper.class) MGLKViewControllerDelegate value) {
        Object __old = delegate();
        if (value != null) {
            ObjCRuntime.associateObjCObject(this, value);
        }
        setDelegate_unsafe(value);
        if (__old != null) {
            ObjCRuntime.dissociateObjCObject(this, __old);
        }
    }

    @Generated
    @Selector("setPauseOnWillResignActive:")
    public native void setPauseOnWillResignActive(boolean value);

    @Generated
    @Selector("setPaused:")
    public native void setPaused(boolean value);

    /**
     * The default value is 30.
     * On iOS:
     *  - Setting to 0 or 1 will sync the framerate with display's refresh rate
     * On macOS:
     *  - Setting to 1 will sync the framerate with display's refresh rate
     *  - Setting to 0 will display the frames as fast as possible.
     */
    @Generated
    @Selector("setPreferredFramesPerSecond:")
    public native void setPreferredFramesPerSecond(@NInt long value);

    @Generated
    @Selector("setResumeOnDidBecomeActive:")
    public native void setResumeOnDidBecomeActive(boolean value);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    @Generated
    @Selector("superclass")
    public static native Class superclass_static();

    @Generated
    @Selector("timeSinceLastUpdate")
    public native double timeSinceLastUpdate();

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();
}
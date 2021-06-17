package com.badlogic.gdx.backends.bindings.metalangle.protocol;


import com.badlogic.gdx.backends.bindings.metalangle.MGLKViewController;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Library;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCProtocolName;
import org.moe.natj.objc.ann.Selector;

@Generated
@Library("MetalANGLE")
@Runtime(ObjCRuntime.class)
@ObjCProtocolName("MGLKViewControllerDelegate")
public interface MGLKViewControllerDelegate {
    @Generated
    @Selector("mglkViewControllerUpdate:")
    void mglkViewControllerUpdate(MGLKViewController controller);
}
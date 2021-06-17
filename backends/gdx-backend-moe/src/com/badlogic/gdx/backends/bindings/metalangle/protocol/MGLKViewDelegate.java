package com.badlogic.gdx.backends.bindings.metalangle.protocol;


import apple.coregraphics.struct.CGRect;
import com.badlogic.gdx.backends.bindings.metalangle.MGLKView;
import org.moe.natj.general.ann.ByValue;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Library;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCProtocolName;
import org.moe.natj.objc.ann.Selector;

@Generated
@Library("MetalANGLE")
@Runtime(ObjCRuntime.class)
@ObjCProtocolName("MGLKViewDelegate")
public interface MGLKViewDelegate {
    /**
     * Implement this method to draw to the view using current OpenGL
     * context associated with the view.
     */
    @Generated
    @Selector("mglkView:drawInRect:")
    void mglkViewDrawInRect(MGLKView view, @ByValue CGRect rect);
}
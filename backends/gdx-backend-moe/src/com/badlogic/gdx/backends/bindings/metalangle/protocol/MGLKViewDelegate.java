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
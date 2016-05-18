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

package com.badlogic.gdx.backends.iosmoe;

import com.intel.moe.natj.general.NatJ;
import com.intel.moe.natj.general.Pointer;
import com.intel.moe.natj.general.ann.ByValue;
import com.intel.moe.natj.objc.ann.Selector;
import ios.coregraphics.struct.CGRect;
import ios.foundation.NSSet;
import ios.glkit.GLKView;
import ios.opengles.EAGLContext;
import ios.uikit.UIEvent;
import ios.uikit.UITouch;

public class IOSGLKView extends GLKView {

	private IOSGraphics graphics;

	static {
		NatJ.register();
	}

	@Selector("alloc")
	public static native IOSGLKView alloc ();

	@Selector("init")
	public native IOSGLKView init ();

	protected IOSGLKView (Pointer peer) {
		super(peer);
	}

	public IOSGLKView init (IOSGraphics graphics, CGRect bounds, EAGLContext context) {
		initWithFrameContext(bounds, context);
		this.graphics = graphics;
		return this;
	}

	@Override
	public void touchesBeganWithEvent (NSSet<? extends UITouch> nsSet, UIEvent uiEvent) {
		graphics.input.onTouch(nsSet);
	}

	@Override
	public void touchesCancelledWithEvent (NSSet<? extends UITouch> nsSet, UIEvent uiEvent) {
		graphics.input.onTouch(nsSet);
	}

	@Override
	public void touchesEndedWithEvent (NSSet<? extends UITouch> nsSet, UIEvent uiEvent) {
		graphics.input.onTouch(nsSet);
	}

	@Override
	public void touchesMovedWithEvent (NSSet<? extends UITouch> nsSet, UIEvent uiEvent) {
		graphics.input.onTouch(nsSet);
	}

	@Override
	public void drawRect (@ByValue CGRect cgRect) {
		graphics.glkViewDrawInRect(this, cgRect);
	}

}

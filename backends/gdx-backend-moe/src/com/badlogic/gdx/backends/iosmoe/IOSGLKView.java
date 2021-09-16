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

import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.ByValue;
import org.moe.natj.general.ann.RegisterOnStartup;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCClassName;
import org.moe.natj.objc.ann.Selector;

import apple.coregraphics.struct.CGRect;
import apple.foundation.NSSet;
import apple.glkit.GLKView;
import apple.uikit.UIEvent;
import apple.uikit.UITouch;

@org.moe.natj.general.ann.Runtime(ObjCRuntime.class)
@ObjCClassName("IOSGLKView")
@RegisterOnStartup
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

	public IOSGLKView init (CGRect bounds) {
		initWithFrame(bounds);
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

	public void setGraphics (IOSGraphics graphics) {
		this.graphics = graphics;
	}

}

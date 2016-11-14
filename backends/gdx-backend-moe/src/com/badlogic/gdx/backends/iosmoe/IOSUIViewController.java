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
import org.moe.natj.objc.ann.Selector;
import apple.coregraphics.struct.CGRect;
import apple.glkit.GLKViewController;
import apple.uikit.enums.UIInterfaceOrientation;


class IOSUIViewController extends GLKViewController {

	private IOSApplication app;
	private IOSGraphics graphics;

	static {
		NatJ.register();
	}

	@Selector("alloc")
	public static native IOSUIViewController alloc ();

	@Selector("init")
	public native IOSUIViewController init ();

	protected IOSUIViewController (Pointer peer) {
		super(peer);
	}
	public IOSUIViewController init (IOSApplication app, IOSGraphics graphics) {
		init();
		this.app = app;
		this.graphics = graphics;
		return this;
	}

	@Override
	public void viewWillAppear (boolean arg0) {
		super.viewWillAppear(arg0);
		// start GLKViewController even though we may only draw a single frame
		// (we may be in non-continuous mode)
		setPaused(false);
	}

	@Override
	public void viewDidAppear (boolean animated) {
		if (app.viewControllerListener != null)
			app.viewControllerListener.viewDidAppear(animated);
	}

	@Override
	public long supportedInterfaceOrientations () {
		long mask = 0;
		if (app.config.orientationLandscape) {
			mask |= ((1 << UIInterfaceOrientation.LandscapeLeft) | (1 << UIInterfaceOrientation.LandscapeRight));
		}
		if (app.config.orientationPortrait) {
			mask |= ((1 << UIInterfaceOrientation.Portrait) | (1 << UIInterfaceOrientation.PortraitUpsideDown));
		}
		return mask;
	}

	@Override
	public boolean shouldAutorotate () {
		return true;
	}

	@Override
	public boolean shouldAutorotateToInterfaceOrientation (long orientation) {
		// we return "true" if we support the orientation
		if (orientation == UIInterfaceOrientation.LandscapeLeft || orientation == UIInterfaceOrientation.LandscapeRight)
			return app.config.orientationLandscape;
		else
			// assume portrait
			return app.config.orientationPortrait;
	}

	@Override
	public void viewDidLayoutSubviews () {
		super.viewDidLayoutSubviews();
		// get the view size and update graphics
		CGRect bounds = app.getBounds();
		graphics.width = (int)bounds.size().width();
		graphics.height = (int)bounds.size().height();
		graphics.makeCurrent();
		if (app.graphics.created) {
			app.listener.resize(graphics.width, graphics.height);
		}
	}

	@Override
	public boolean prefersStatusBarHidden() {
		return !app.config.statusBarVisible;
	}
}

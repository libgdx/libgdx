/*******************************************************************************
 * Copyright 2017 See AUTHORS file.
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

import com.badlogic.gdx.ApplicationListener;

import apple.coregraphics.struct.CGRect;
import apple.uikit.UIApplication;

public class IOSGLKViewApplication extends IOSApplication {

	public IOSGLKViewApplication (ApplicationListener listener, IOSApplicationConfiguration config) {
		super(listener, config);
	}

	private IOSGLKView view;

	public void initializeForView (IOSGLKView view) {
		this.view = view;
		init();
		this.input.setView(view);
	}

	@Override
	protected IOSGraphics createGraphics (float scale) {
		return IOSGraphics.alloc().init(scale, this, config, input, config.useGL30, view);
	}

	@Override
	protected CGRect getOriginalBounds () {
		return view.bounds();
	}

	@Override
	protected double getStatusBarHeight (double screenHeight) {
		return 0;
	}

	@Override
	protected long getStatusBarOrientation () {
		return UIApplication.sharedApplication().statusBarOrientation();
	}
}

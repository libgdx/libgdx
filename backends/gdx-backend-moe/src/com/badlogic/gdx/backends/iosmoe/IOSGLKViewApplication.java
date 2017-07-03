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

import apple.coregraphics.struct.CGPoint;
import apple.coregraphics.struct.CGRect;
import apple.coregraphics.struct.CGSize;
import apple.glkit.GLKView;
import apple.uikit.UIApplication;
import apple.uikit.UIDevice;
import apple.uikit.UIScreen;
import apple.uikit.enums.UIInterfaceOrientation;
import apple.uikit.enums.UIUserInterfaceIdiom;
import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.iosmoe.IOSApplicationConfiguration;
import com.badlogic.gdx.backends.iosmoe.IOSAudio;
import com.badlogic.gdx.backends.iosmoe.IOSFiles;
import com.badlogic.gdx.backends.iosmoe.IOSGraphics;
import com.badlogic.gdx.backends.iosmoe.IOSInput;
import com.badlogic.gdx.backends.iosmoe.IOSNet;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;

import android.os.Handler;

import java.lang.reflect.Method;

import apple.uikit.UIView;

public class IOSGLKViewApplication extends IOSApplication {

	public IOSGLKViewApplication(ApplicationListener listener, IOSApplicationConfiguration config) {
		super(listener, config);
	}

	private IOSGLKView view;

	public void initializeForView (IOSGLKView view) {
		this.view = view;
		init();
		this.input.setView(view);
	}

	@Override
	protected void createGraphics(float scale) {
		this.graphics =  IOSGraphics.alloc().init(scale, this, config, input, config.useGL30, view);
	}

	@Override
	protected CGRect getOriginalBounds () {
		return view.bounds();
	}

	@Override
	protected double getStatusBarHeight(double screenHeight) {
		return 0;
	}

	@Override
	protected long getStatusBarOrientation() {
		return UIApplication.sharedApplication().statusBarOrientation();
	}
}

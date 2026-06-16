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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.backends.iosrobovm.IOSUIWindowSceneDelegate;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.*;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

public class IOSRobovmTests extends IOSApplication.Delegate {

	@Override
	protected IOSApplication createApplication () {
		IOSApplicationConfiguration config = new IOSApplicationConfiguration();
		config.useHaptics = true;
		return new IOSApplication(new IosTestWrapper(), config);
	}

	public static void main (String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
		UIApplication.main(argv, null, IOSRobovmTests.class);
		pool.close();
	}

	@Override
	public boolean didFinishLaunching (UIApplication application, UIApplicationLaunchOptions launchOptions) {
		return super.didFinishLaunching(application, launchOptions);
	}

	@Override
	public UISceneConfiguration getConfigurationForConnectingSceneSession (UIApplication application,
		UISceneSession connectingSceneSession, UISceneConnectionOptions options) {
		UISceneConfiguration config = new UISceneConfiguration("Default Configuration", connectingSceneSession.getRole());
		config.setDelegateClass(IOSUIWindowSceneDelegate.class);
		return config;
	}
}

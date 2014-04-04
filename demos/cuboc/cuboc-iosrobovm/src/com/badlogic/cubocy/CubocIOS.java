package com.badlogic.cubocy;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

public class CubocIOS extends IOSApplication.Delegate {
	@Override
	protected IOSApplication createApplication () {
		IOSApplicationConfiguration config = new IOSApplicationConfiguration();
		return new IOSApplication(new Cubocy(), config);
	}

	public static void main (String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
		UIApplication.main(argv, null, CubocIOS.class);
		pool.close();
	}
}

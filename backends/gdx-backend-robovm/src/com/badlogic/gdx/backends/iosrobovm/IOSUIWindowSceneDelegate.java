
package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Gdx;
import org.robovm.apple.uikit.*;

public class IOSUIWindowSceneDelegate extends UIWindowSceneDelegateAdapter {

	@Override
	public void willConnect (UIScene scene, UISceneSession session, UISceneConnectionOptions connectionOptions) {
		if (scene instanceof UIWindowScene) {
			IOSApplication app = (IOSApplication)Gdx.app;
			app.handleSceneConnection((UIWindowScene)scene);
		}
	}

	@Override
	public void sceneWillResignActive (UIScene scene) {
		IOSApplication app = (IOSApplication)Gdx.app;
		app.willResignActive(scene);
	}

	@Override
	public void sceneWillEnterForeground (UIScene scene) {
		IOSApplication app = (IOSApplication)Gdx.app;
		app.willEnterForeground(scene);
	}

	@Override
	public void sceneDidBecomeActive (UIScene scene) {
		IOSApplication app = (IOSApplication)Gdx.app;
		app.didBecomeActive(scene);
	}
}

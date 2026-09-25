
package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Gdx;
import org.robovm.apple.uikit.*;
import org.robovm.objc.annotation.CustomClass;

@CustomClass(preload = true)
public class IOSSceneDelegate extends UIWindowSceneDelegateAdapter {

	private UIWindow uiWindow;
	private UIWindowScene uiWindowScene;

	@Override
	public void setWindow (UIWindow window) {
		this.uiWindow = window;
	}

	@Override
	public UIWindow getWindow () {
		return uiWindow;
	}

	@Override
	public void willConnect (UIScene scene, UISceneSession session, UISceneConnectionOptions connectionOptions) {
		if (scene instanceof UIWindowScene) {
			// OS can disconnect scenes to free resources and reconnect with a new one when app goes to foreground. libGDX handles
			// single scene apps and currently doesn't handle graphics recreation so, if the original scene has been disconnected
			// and a new one is being created we have to kill the process.
			IOSApplication.Delegate userLauncher = (IOSApplication.Delegate)UIApplication.getSharedApplication().getDelegate();
			if (uiWindowScene != null) {
				if (userLauncher != null) {
					userLauncher.willTerminate(UIApplication.getSharedApplication());
				}
				// Unrecoverable, kill process
				System.exit(0);
			} else {
				uiWindowScene = (UIWindowScene)scene;
				IOSApplication app = (IOSApplication)Gdx.app;
				app.handleSceneConnection(uiWindowScene);
				if (userLauncher != null) {
					userLauncher.willConnect(scene, session, connectionOptions);
				}
			}
		}
	}

	@Override
	public void sceneWillResignActive (UIScene scene) {
		IOSApplication app = (IOSApplication)Gdx.app;
		IOSApplication.Delegate userLauncher = (IOSApplication.Delegate)UIApplication.getSharedApplication().getDelegate();
		if (userLauncher != null) {
			userLauncher.sceneWillResignActive(scene);
		}
		app.willResignActive(scene);
	}

	@Override
	public void sceneWillEnterForeground (UIScene scene) {
		IOSApplication app = (IOSApplication)Gdx.app;
		app.willEnterForeground(scene);
		IOSApplication.Delegate userLauncher = (IOSApplication.Delegate)UIApplication.getSharedApplication().getDelegate();
		if (userLauncher != null) {
			userLauncher.sceneWillEnterForeground(scene);
		}
	}

	@Override
	public void sceneDidBecomeActive (UIScene scene) {
		IOSApplication app = (IOSApplication)Gdx.app;
		app.didBecomeActive(scene);
		IOSApplication.Delegate userLauncher = (IOSApplication.Delegate)UIApplication.getSharedApplication().getDelegate();
		if (userLauncher != null) {
			userLauncher.sceneDidBecomeActive(scene);
		}
	}

	@Override
	public void sceneDidDisconnect (UIScene scene) {
		IOSApplication.Delegate userLauncher = (IOSApplication.Delegate)UIApplication.getSharedApplication().getDelegate();
		if (userLauncher != null) {
			userLauncher.sceneDidDisconnect(scene);
		}
	}

	@Override
	public void sceneDidEnterBackground (UIScene scene) {
		IOSApplication.Delegate userLauncher = (IOSApplication.Delegate)UIApplication.getSharedApplication().getDelegate();
		if (userLauncher != null) {
			userLauncher.sceneDidEnterBackground(scene);
		}
	}
}

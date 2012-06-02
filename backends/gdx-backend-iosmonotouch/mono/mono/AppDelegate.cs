	using System;
using System.Collections.Generic;
using System.Linq;
using MonoTouch.Foundation;
using MonoTouch.UIKit;
using com.badlogic.gdx.math;

namespace mono
{
	public class TestListener: com.badlogic.gdx.ApplicationListener {
		public void create() {
		}
		
		public void dispose() {
		}
		
		public void render() {
		}
		
		public void resume() {
		}
		
		public void pause() {
		}
		
		public void resize(int width, int height) {
		}
	}
	
	[Register ("AppDelegate")]
	public partial class AppDelegate : UIApplicationDelegate
	{
		UIWindow window;
		OpenGLViewController viewController;
		
		public override bool FinishedLaunching (UIApplication app, NSDictionary options)
		{
			com.badlogic.gdx.ApplicationListener listener = new TestListener();
			com.badlogic.gdx.backends.ios.IOSApplication gdx = new com.badlogic.gdx.backends.ios.IOSApplication(app, listener);
			gdx.log("test", "hello");
			
			window = new UIWindow (UIScreen.MainScreen.Bounds);

			// load the appropriate UI, depending on whether the app is running on an iPhone or iPad
			if (UIDevice.CurrentDevice.UserInterfaceIdiom == UIUserInterfaceIdiom.Phone) {
				viewController = new OpenGLViewController (
					"OpenGLViewController_iPhone",
					null
				);
			} else {
				viewController = new OpenGLViewController (
					"OpenGLViewController_iPad",
					null
				);
			}
			window.RootViewController = viewController;
			window.MakeKeyAndVisible ();
			return true;
		}
	}
}
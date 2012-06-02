	using System;
using System.Collections.Generic;
using System.Linq;
using MonoTouch.Foundation;
using MonoTouch.UIKit;
using com.badlogic.gdx.math;

namespace mono
{
	/// <summary>
	/// The UIApplicationDelegate for the application. This class is responsible for launching the 
	/// User Interface of the application, as well as listening (and optionally responding) to 
	/// application events from iOS.
	/// </summary>
	[Register ("AppDelegate")]
	public partial class AppDelegate : UIApplicationDelegate
	{
		// class-level declarations
		UIWindow window;
		OpenGLViewController viewController;
		
		// This method is invoked when the application has loaded its UI and is ready to run
		public override bool FinishedLaunching (UIApplication app, NSDictionary options)
		{
			Console.WriteLine(java.lang.System.nanoTime());
			Console.WriteLine(com.badlogic.gdx.Version.VERSION);
			
			Matrix4 mat = new Matrix4();
			com.badlogic.gdx.backends.ios.IOSApplication gdx = new com.badlogic.gdx.backends.ios.IOSApplication();
			gdx.log("test", "hello");
			
			// create a new window instance based on the screen size
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

			// make the window visible
			window.MakeKeyAndVisible ();
			
			return true;
		}
	}
}
using System;
using System.Collections.Generic;
using System.Linq;

using MonoTouch.Foundation;
using MonoTouch.UIKit;
using com.badlogic.gdx.backends.ios;
using com.dozingcatsoftware.bouncy;

namespace bouncy
{		
	public class Application
	{
		[Register ("AppDelegate")]
		public partial class AppDelegate : IOSApplication {
			public AppDelegate(): base(new Bouncy(), getConfig()) {
				
			}
		}

		internal static IOSApplicationConfiguration getConfig() {
			IOSApplicationConfiguration config = new IOSApplicationConfiguration();
			config.orientationLandscape = false;
			config.orientationPortrait = true;
			return config;
		}
		
		static void Main (string[] args)
		{
			UIApplication.Main (args, null, "AppDelegate");
		}
	}
}

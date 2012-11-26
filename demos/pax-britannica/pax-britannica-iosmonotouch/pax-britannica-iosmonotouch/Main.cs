using System;
using System.Collections.Generic;
using System.Linq;

using MonoTouch.Foundation;
using MonoTouch.UIKit;
using com.badlogic.gdx.backends.ios;
using de.swagner.paxbritannica;

namespace paxbritannicaiosmonotouch
{
	public class Application
	{
		[Register ("AppDelegate")]
		public partial class AppDelegate : IOSApplication {
			public AppDelegate(): base(new PaxBritannica(), getConfig()) {
				
			}
			
			internal static IOSApplicationConfiguration getConfig() {
				IOSApplicationConfiguration config = new IOSApplicationConfiguration();
				config.orientationLandscape = true;
				config.orientationPortrait = false;
				config.useAccelerometer = true;
				return config;
			}
		}

		static void Main (string[] args)
		{
			UIApplication.Main (args, null, "AppDelegate");
		}
	}
}

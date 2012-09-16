using System;
using System.Collections.Generic;
using System.Linq;

using MonoTouch.Foundation;
using MonoTouch.UIKit;
using com.badlogic.gdx.backends.ios;
using com.badlogic.gdxinvaders;
using com.badlogic.gdx;

namespace gdxinvaders
{		
	class AppListener: ApplicationAdapter {
	}

	public class Application
	{
		[Register ("AppDelegate")]
		public partial class AppDelegate : IOSApplication {
			public AppDelegate(): base(new AppListener(), new IOSApplicationConfiguration()) {
				
			}
		}
		
		static void Main (string[] args)
		{
			UIApplication.Main (args, null, "AppDelegate");
		}
	}
}

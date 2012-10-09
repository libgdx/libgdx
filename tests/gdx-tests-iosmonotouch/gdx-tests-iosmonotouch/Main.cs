using System;
using System.Collections.Generic;
using System.Linq;

using MonoTouch.Foundation;
using MonoTouch.UIKit;
using com.badlogic.gdx;
using com.badlogic.gdx.files;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.backends.ios;
using com.badlogic.gdx.tests.gwt;
using com.badlogic.gdx.tests;

namespace superjumperios
{		
	public class TestListener: ApplicationAdapter {
		public override void create() {
			FileHandle file = Gdx.files.@internal("data/uiskin.atlas").parent();
			new TextureAtlas(Gdx.files.@internal("data/uiskin.atlas"), false);
		}
	}

	public class Application
	{
		[Register ("AppDelegate")]
		public partial class AppDelegate : IOSApplication {
			public AppDelegate(): base(new FilesTest(), new IOSApplicationConfiguration()) {
				
			}
		}
		
		static void Main (string[] args)
		{
			UIApplication.Main (args, null, "AppDelegate");
		}
	}
}

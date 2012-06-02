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
		public override bool FinishedLaunching (UIApplication app, NSDictionary options)
		{
			return true;
		}
	}
}
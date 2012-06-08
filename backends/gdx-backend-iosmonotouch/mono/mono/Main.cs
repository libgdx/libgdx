using System;
using System.Collections.Generic;
using System.Linq;

using MonoTouch.Foundation;
using MonoTouch.UIKit;
using OpenTK.Graphics.ES20;
using OpenTK.Graphics;
using com.badlogic.gdx;
using java.nio;
using com.badlogic.gdx.math;

namespace mono
{
	public class TestListener: com.badlogic.gdx.ApplicationListener {
		float red = 0;
		
		public void create() {
			Gdx.app.log("Test", "created");
			ByteBuffer buffer = com.badlogic.gdx.utils.BufferUtils.newByteBuffer(0);
			Matrix4 mat = new Matrix4();
			mat.rotate(90, 0, 0, 1);
			Matrix4.mul(mat.val, new Matrix4().val);
			Gdx.app.log("Test", mat.toString());
		}
		
		public void dispose() {
			Gdx.app.log("Test", "destroyed");
		}
		
		public void render() {
			GL.ClearColor(red, 0, 0, 1);
			GL.Clear((uint)All.ColorBufferBit);
			red += 0.01f;
		}
		
		public void resume() {
			Gdx.app.log("Test", "resumed");
		}
		
		public void pause() {
			Gdx.app.log("Test", "paused");
		}
		
		public void resize(int width, int height) {
			Gdx.app.log("Test", "resized");
		}
	}
	
	
	public class Application
	{
		[Register ("AppDelegate")]
		public partial class AppDelegate : UIApplicationDelegate {
			public override bool FinishedLaunching (UIApplication app, NSDictionary options) {
				new com.badlogic.gdx.backends.ios.IOSApplication(app, new TestListener());
				return true;
			}
		}
		
		static void Main (string[] args)
		{
			UIApplication.Main (args, null, "AppDelegate");
		}
	}
}

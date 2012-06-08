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
using java.io;
using System.IO;
using com.badlogic.gdx.graphics;

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
			
			var directories = Directory.EnumerateDirectories("./data");
foreach (var directory in directories) {
      System.Console.WriteLine(directory);
}
			
			var text = System.IO.File.ReadAllText("info.plist");
			System.Console.WriteLine(text);
			
			com.badlogic.gdx.files.FileHandle file = new com.badlogic.gdx.files.FileHandle("data/test.txt");
			text = file.readString();
			System.Console.WriteLine(text);
			
			
			System.Console.WriteLine(file.readString());
			file.parent().child("dummy").mkdirs();
			
			Pixmap pixmap = new Pixmap(new com.badlogic.gdx.files.FileHandle("data/badlogic.jpg"));
			System.Console.WriteLine(pixmap.getWidth() + "," + pixmap.getHeight());
			pixmap.dispose();
			                         
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
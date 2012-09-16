using System;
using System.Collections.Generic;
using System.Linq;

using MonoTouch.Foundation;
using MonoTouch.UIKit;
using com.badlogic.gdx;
using com.badlogic.gdx.math;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.graphics.glutils;
using java.io;
using java.nio;
using System.IO;
using com.badlogic.gdx.backends.ios;
using com.badlogic.gdx.physics.box2d;

namespace mono
{
	class MyInputAdapter: InputAdapter {
		public override bool touchUp (int x, int y, int pointer, int button) {
			Gdx.app.log ("Touch", "touch up " + x + ", " + y + ", " + pointer);
			return true;
		}

		public override bool touchDown (int x, int y, int pointer, int button) {
			Gdx.app.log ("Touch", "touch down " + x + ", " + y + ", " + pointer);
			return true;
		}

		public override bool touchDragged (int x, int y, int pointer) {
			Gdx.app.log ("Touch", "touch dragged " + x + ", " + y + ", " + pointer);
			return true;
		}
	}

	public class TestListener: com.badlogic.gdx.ApplicationListener {
		public void create () {
			Gdx.input.setInputProcessor(new MyInputAdapter());
			World world = new World(new Vector2(0, -10), true);
		}
	
		public void render () {
			if(Gdx.input.justTouched()) {
				Gdx.app.log ("Touch", "just touched");
			}

		}
		
		public void dispose () {
		}
		
		public void resume() {
		}
		
		public void pause() {
		}
		
		public void resize(int width, int height) {
		}
	}
	
	
	public class Application
	{
		[Register ("AppDelegate")]
		public class IOSStarter : IOSApplication {
			public IOSStarter() : base(new TestListener(), new IOSApplicationConfiguration()) {
			}
		}
		
		static void Main (string[] args)
		{
			UIApplication.Main (args, null, "AppDelegate");
		}
	}
}
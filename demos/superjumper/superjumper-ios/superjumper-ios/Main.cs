using System;
using System.Collections.Generic;
using System.Linq;

using MonoTouch.Foundation;
using MonoTouch.UIKit;
using com.badlogic.gdx.backends.ios;
using com.badlogicgames.superjumper;

using com.badlogic.gdx;
using com.badlogic.gdx.math;
using com.badlogic.gdx.audio;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.graphics.glutils;
using com.badlogic.gdx.backends.ios;
using com.badlogic.gdx.physics.box2d;

namespace superjumperios
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
		ShapeRenderer renderer;
		SpriteBatch batch;
		Texture tex;

		public void create () {
			Gdx.input.setInputProcessor(new MyInputAdapter());
			renderer = new ShapeRenderer();
			batch = new SpriteBatch();
			tex = new Texture(Gdx.files.@internal("data/items.ugh"));
		}
		
		public void render () {
			Gdx.gl.glClearColor(1, 0, 0, 1);
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			renderer.begin(ShapeRenderer.ShapeType.FilledRectangle);
			renderer.filledRect(100, 100, 100, 100);
			renderer.end ();

			batch.begin();
			batch.draw(tex, 100, 400);
			batch.end ();

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
		public partial class AppDelegate : IOSApplication {
			public AppDelegate(): base(new TestListener(), new IOSApplicationConfiguration()) {

			}
		}
		
		static void Main (string[] args)
		{
			UIApplication.Main (args, null, "AppDelegate");
		}
	}
}

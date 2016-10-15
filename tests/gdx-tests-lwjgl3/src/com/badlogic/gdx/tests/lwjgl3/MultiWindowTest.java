package com.badlogic.gdx.tests.lwjgl3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.BulletTestCollection;
import com.badlogic.gdx.tests.UITest;
import com.badlogic.gdx.tests.g3d.Basic3DSceneTest;
import com.badlogic.gdx.tests.g3d.ShaderCollectionTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class MultiWindowTest {
	static Texture sharedTexture;
	static SpriteBatch sharedSpriteBatch;
	
	public static class MainWindow extends ApplicationAdapter {
		Class[] childWindowClasses = { ShaderCollectionTest.class, BulletTestCollection.class, UITest.class, Basic3DSceneTest.class };
		
		@Override
		public void create () {
			sharedSpriteBatch = new SpriteBatch();
			sharedTexture = new Texture("data/badlogic.jpg");
		}

		@Override		
		public void render () {
			Gdx.gl.glClearColor(1, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			sharedSpriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			sharedSpriteBatch.begin();
			sharedSpriteBatch.draw(sharedTexture, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY() - 1);
			sharedSpriteBatch.end();
			
			if(Gdx.input.justTouched()) {
				Lwjgl3Application app = (Lwjgl3Application)Gdx.app;
				Lwjgl3WindowConfiguration config = new Lwjgl3WindowConfiguration();
				DisplayMode mode = Gdx.graphics.getDisplayMode();
				config.setWindowPosition(MathUtils.random(0, mode.width - 640), MathUtils.random(0, mode.height - 480));
				config.setTitle("Child window");
				Class clazz = childWindowClasses[MathUtils.random(0, childWindowClasses.length - 1)];
				ApplicationListener listener = createChildWindowClass(clazz);
				Lwjgl3Window window = app.newWindow(listener, config);
			}
		}

		public ApplicationListener createChildWindowClass(Class clazz) {
			try {
				return (ApplicationListener) clazz.newInstance();
			} catch(Throwable t) {
				throw new GdxRuntimeException("Couldn't instantiate app listener", t);
			}
		}
	}
	
	public static void main(String[] argv) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Multi-window test");
		new Lwjgl3Application(new MainWindow(), config);
	}
}

package com.badlogic.gdx.tests.lwjgl3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.NoncontinuousRenderingTest;
import com.badlogic.gdx.tests.UITest;
import com.badlogic.gdx.tests.g3d.Basic3DSceneTest;
import com.badlogic.gdx.tests.g3d.ShaderCollectionTest;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

public class MultiWindowTest {
	static Texture sharedTexture;
	static SpriteBatch sharedSpriteBatch;
	
	public static class MainWindow extends ApplicationAdapter {
		Class[] childWindowClasses = { NoncontinuousRenderingTest.class, ShaderCollectionTest.class, Basic3DSceneTest.class, UITest.class };
		Lwjgl3Window latestWindow;
		int index;
		
		@Override
		public void create () {
			sharedSpriteBatch = new SpriteBatch();
			sharedTexture = new Texture("data/badlogic.jpg");
		}

		@Override		
		public void render () {
			ScreenUtils.clear(1, 0, 0, 1);
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
				config.useVsync(false);
				config.setWindowListener(new Lwjgl3WindowAdapter() {
					@Override
					public void created(Lwjgl3Window window) {
						latestWindow = window;
					}
				});
				Class clazz = childWindowClasses[index++ % childWindowClasses.length];
				ApplicationListener listener = createChildWindowClass(clazz);
				app.newWindow(listener, config);
			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && latestWindow != null){
				latestWindow.setTitle("Retitled window");
				int size = 48;
				Pixmap icon = new Pixmap(size, size, Pixmap.Format.RGBA8888);
				icon.setBlending(Blending.None);
				icon.setColor(Color.BLUE);
				icon.fill();
				icon.setColor(Color.CLEAR);
				for (int i = 0; i < size; i += 3)
					for (int j = 0; j < size; j += 3)
						icon.drawPixel(i, j);
				latestWindow.setIcon(icon);
				icon.dispose();
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
		config.useVsync(true);
		new Lwjgl3Application(new MainWindow(), config);
	}
}

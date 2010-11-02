package com.badlogic.gdx.backends.desktop;

import java.awt.Canvas;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class LwjglCanvas implements Application {
	LwjglGraphics graphics;
	LwjglAudio audio;
	LwjglFiles files;
	LwjglInput input;
	final ApplicationListener listener;
	Thread mainLoopThread;
	boolean running = true;
	Canvas canvas;
	
	public LwjglCanvas( ApplicationListener listener, boolean useGL2) {
		LwjglNativesLoader.load();
		
		canvas = new Canvas() {			
			private final Dimension minSize = new Dimension();

			public final void addNotify () {
				super.addNotify();
				try {
					graphics.setupDisplay();
					start();
				} catch (LWJGLException e) {
					throw new GdxRuntimeException(e);
				}							
			}

			public Dimension getMinimumSize () {
				return minSize;
			}			
		};			
		
		canvas.setSize(100, 100);
		canvas.setMinimumSize(new Dimension(2,2));
		graphics = new LwjglGraphics( canvas, useGL2);
		audio = new LwjglAudio();
		files = new LwjglFiles();
		input = new LwjglInput();
		this.listener = listener;
	}
	
	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public Audio getAudio() {	
		return audio;
	}

	@Override
	public Files getFiles() {
		return files;
	}

	@Override
	public Graphics getGraphics() {
		return graphics;
	}

	@Override
	public Input getInput() {
		return input;
	}

	@Override
	public ApplicationType getType() {
		return ApplicationType.Desktop;
	}

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public void log(String tag, String message) {
		System.out.println( tag + ": " + message );
	}
	
	private void start() {
		LwjglNativesLoader.load();
		mainLoopThread = new Thread("LWJGL Application") {
			@SuppressWarnings("synthetic-access")
			public void run () {				
				LwjglCanvas.this.mainLoop();							
			}
		};
		mainLoopThread.start();
	}
	
	private void mainLoop( ) {			
		Keyboard.enableRepeatEvents(true);
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					listener.create();
					listener.resize(graphics.getWidth(), graphics.getHeight());			
				}
			});
		} catch (Exception e1) {
			throw new GdxRuntimeException(e1);
		}
		
		Runnable runnable = new Runnable() {
			int lastWidth = graphics.getWidth();
			int lastHeight = graphics.getHeight();
			
			@Override
			public void run() {
				graphics.updateTime();
				input.update();
				
				if( lastWidth != graphics.getWidth() || lastHeight != graphics.getHeight() ) {
					lastWidth = graphics.getWidth();
					lastHeight = graphics.getHeight();
					try {
						Display.setDisplayMode(new DisplayMode(lastWidth, lastHeight));
					} catch (LWJGLException e) {
						throw new GdxRuntimeException(e);
					}
					listener.resize(lastWidth, lastHeight);
				}
				
				listener.render();
				input.processEvents(null);
				Display.update();
				Display.sync(60);		
			}
		};
		
		while(running) {
			SwingUtilities.invokeLater(runnable);	
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				listener.pause();
				listener.destroy();
				Display.destroy();		
			}
		});	
	}

	public void stop() {
		running = false;
		try {
			mainLoopThread.join();
		}
		catch(Exception ex) {			
		}
	}
}

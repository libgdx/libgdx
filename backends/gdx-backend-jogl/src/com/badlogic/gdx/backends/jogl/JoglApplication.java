/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.jogl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Version;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * An implemenation of the {@link Application} interface based on Jogl for Windows, Linux and Mac. Instantiate this class with
 * apropriate parameters and then register {@link ApplicationListener}, {@link RenderListener} or {@link InputProcessor} instances.
 * 
 * @author mzechner
 * 
 */
public final class JoglApplication implements Application {
	static {		
		Version.loadLibrary();
	}

	JoglGraphics graphics;	
	JoglInput input;	
	JoglFiles files;	
	JoglAudio audio;	
	JFrame frame;
	

	/**
	 * Creates a new {@link JoglApplication} with the given title and dimensions. If useGL20IfAvailable is set the JoglApplication
	 * will try to create an OpenGL 2.0 context which can then be used via {@link JoglApplication.getGraphics().getGL20()}. To query whether enabling OpenGL 2.0
	 * was successful use the {@link JoglApplication.getGraphics().isGL20Available()} method.
	 * 
	 * @param listener the ApplicationListener implementing the program logic
	 * @param title the title of the application
	 * @param width the width of the surface in pixels
	 * @param height the height of the surface in pixels
	 * @param useGL20IfAvailable wheter to use OpenGL 2.0 if it is available or not
	 */	
	public JoglApplication (final ApplicationListener listener, final String title, final int width, final int height, final boolean useGL20IfAvailable) {
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				public void run() {					
					JoglNativesLoader.loadLibraries();
					graphics = new JoglGraphics(listener, title, width, height, useGL20IfAvailable);
					input = new JoglInput(graphics.getCanvas());
					audio = new JoglAudio();
					files = new JoglFiles();
			
					Gdx.app = JoglApplication.this;
					Gdx.graphics = JoglApplication.this.getGraphics();
					Gdx.input = JoglApplication.this.getInput();
					Gdx.audio = JoglApplication.this.getAudio();
					Gdx.files = JoglApplication.this.getFiles();
					
					initialize(title, width, height);
				}
			});
		} catch (Exception e) {
			throw new GdxRuntimeException("Creating window failed");
		} 
	}
	
	private void initialize(String title, int width, int height) {
		frame = new JFrame(title);
		graphics.getCanvas().setPreferredSize(new Dimension(width, height));			
		frame.setSize(width + frame.getInsets().left + frame.getInsets().right, frame.getInsets().top + frame.getInsets().bottom
			+ height);
		frame.add(graphics.getCanvas(), BorderLayout.CENTER);		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		
		frame.addWindowListener( new WindowAdapter() {		
			@Override
			public void windowOpened(WindowEvent arg0) {
				graphics.getCanvas().requestFocus();
				graphics.getCanvas().requestFocusInWindow();				
			}
			
			@Override
			public void windowIconified(WindowEvent arg0) {			
				graphics.pause();								
			}
			
			@Override
			public void windowDeiconified(WindowEvent arg0) {
				graphics.resume();
			}		
			
			@Override
			public void windowClosing(WindowEvent arg0) {
				graphics.pause();				
				graphics.destroy();
				frame.remove(graphics.getCanvas());
			}				
		});
		
		frame.pack();
		frame.setVisible(true);
		graphics.create();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Audio getAudio () {
		return audio;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Files getFiles () {
		return files;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Graphics getGraphics () {
		return graphics;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Input getInput () {
		return input;
	}

	@Override public void log (String tag, String message) {
		System.out.println(tag + ": " + message);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public ApplicationType getType () {
		return ApplicationType.Desktop;
	}

	@Override public int getVersion () {
		return 0;
	}

	public static void main(String[] argv) {
		ApplicationListener listener = new ApplicationListener() {
			
			@Override
			public void resume() {
				System.out.println("resumed");
			}
			
			@Override
			public void render() {
				System.out.println("render");				
			}
			
			@Override
			public void pause() {
				System.out.println("paused");				
			}
			
			@Override
			public void dispose() {
				System.out.println("destroy");				
			}
			
			@Override
			public void create() {
				System.out.println("created");				
			}

			@Override
			public void resize(int width, int height) {
				System.out.println("resize");
			}
		};
		
		new JoglApplication(listener, "Jogl Application Test", 480, 320, false);
	}
}

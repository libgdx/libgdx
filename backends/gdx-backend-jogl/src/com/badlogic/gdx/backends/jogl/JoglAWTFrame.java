package com.badlogic.gdx.backends.jogl;

import java.awt.Dimension;

import javax.swing.JFrame;

import com.badlogic.gdx.ApplicationListener;

public class JoglAWTFrame extends JFrame {
	
	final JoglAWTCanvas joglAWTCanvas;

	public JoglAWTFrame(ApplicationListener listener, String title, int width, int height, boolean useGL2) {
		super(title);
		joglAWTCanvas = new JoglAWTCanvas(listener, title, width, height, useGL2) {
			protected void stopped () {
				JoglAWTFrame.this.dispose();
			}

			protected void setTitle (String title) {
				JoglAWTFrame.this.setTitle(title);
			}

			protected void setDisplayMode (int width, int height) {
				JoglAWTFrame.this.getContentPane().setPreferredSize(new Dimension(width, height));
				JoglAWTFrame.this.getContentPane().invalidate();
				JoglAWTFrame.this.pack();
				JoglAWTFrame.this.setLocationRelativeTo(null);
				updateSize(width, height);
			}

			protected void resize (int width, int height) {
				updateSize(width, height);
			}

			protected void start () {
				JoglAWTFrame.this.start();
			}
		};
		getContentPane().add(joglAWTCanvas.getCanvas());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run () {
				Runtime.getRuntime().halt(0); // Because fuck you, deadlock causing Swing shutdown hooks.
			}
		});

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setPreferredSize(new Dimension(width, height));
		initialize();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		joglAWTCanvas.getCanvas().requestFocus();
	}

	/** Called before the JFrame is shown. */
	protected void initialize () {
	}

	/** Called after {@link ApplicationListener} create and resize, but before the game loop iteration. */
	protected void start () {
	}

	/** Called when the canvas size changes. */
	public void updateSize (int width, int height) {
	}

	public JoglAWTCanvas getJoglAWTCanvas () {
		return joglAWTCanvas;
	}
}

package com.mojang.metagun;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.mojang.metagun.screen.*;

public class Metagun implements ApplicationListener {
	public static final int GAME_WIDTH = 320;
	public static final int GAME_HEIGHT = 240;
	public static final int SCREEN_SCALE = 2;

	private static final long serialVersionUID = 1L;

	private boolean running = false;
	private Screen screen;
	private Input input = new Input();
	private boolean started = false;
	private float accum = 0;
	
	public void create() {
		running = true;
		setScreen(new TitleScreen());
	}

	public void pause() {
		running = false;
	}

	public void resume() {
		running = true;
	}

	public void setScreen(Screen newScreen) {
		if (screen != null)
			screen.removed();
		screen = newScreen;
		if (screen != null)
			screen.init(this);
	}

	public void render() {
		accum += Gdx.graphics.getDeltaTime();
		while(accum > 1.0f / 60.0f) {				
			screen.tick(input);
			input.tick();
			accum -= 1.0f / 60.0f;
		}
		screen.render();
	}

	public void keyPressed(KeyEvent ke) {
		input.set(ke.getKeyCode(), true);
	}

	public void keyReleased(KeyEvent ke) {
		input.set(ke.getKeyCode(), false);
	}

	public void keyTyped(KeyEvent ke) {
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	// private Image splashImage;
	// public void update(Graphics g) {
	// paint(g);
	// }
	// public void paint(Graphics g) {
	// if (started) return;
	// if (splashImage==null) {
	// try {
	// splashImage = ImageIO.read(Metagun.class.getResource("/mojang.png"));
	// splashImage = splashImage.getScaledInstance(640, 480,
	// Image.SCALE_AREA_AVERAGING);
	// } catch (IOException e) {
	// }
	// }
	// g.drawImage(splashImage, 0, 0, null);
	// }
}

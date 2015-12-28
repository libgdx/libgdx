package com.badlogic.gdx.backends.lwjgl3;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;

class Lwjgl3Window {
	private final ApplicationListener listener;
	private final Graphics graphics;
	private final Input input;
	private final long glfwWindow;

	public Lwjgl3Window(ApplicationListener listener, Graphics graphics, Input input, long glfwWindow) {
		this.listener = listener;
		this.graphics = graphics;
		this.input = input;
		this.glfwWindow = glfwWindow;
	}

	public ApplicationListener getListener() {
		return listener;
	}

	public Graphics getGraphics() {
		return graphics;
	}

	public Input getInput() {
		return input;
	}

	public long getGlfwWindow() {
		return glfwWindow;
	}
}

package com.badlogic.gdx.tests.lwjgl;

import javax.swing.UIManager;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglHeadlessApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglHeadlessApplicationConfiguration;

public class LwjglHeadlessTest extends ApplicationAdapter {
	@Override
	public void create () {
		Gdx.app.log("LwjglHeadlessTest", "create();");
	}
	
	@Override
	public void render () {
		Gdx.app.log("LwjglHeadlessTest", "render();");
	}
	
	public static void main (String[] argv) throws Exception {
		LwjglHeadlessApplicationConfiguration config = new LwjglHeadlessApplicationConfiguration();
		config.renderInterval = 0.5f;
		LwjglHeadlessApplication app = new LwjglHeadlessApplication(new LwjglHeadlessTest(), config); 
	}
}

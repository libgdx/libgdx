package com.badlogic.gdx.tests.lwjgl;

import javax.swing.UIManager;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglHeadlessApplication;

public class LwjglHeadlessTest extends ApplicationAdapter {
	@Override
	public void create () {
		Gdx.app.log("LwjglHeadlessTest", "create();");
	}
	
	public static void main (String[] argv) throws Exception {
		LwjglHeadlessApplication app = new LwjglHeadlessApplication(new LwjglHeadlessTest()); 
	}
}

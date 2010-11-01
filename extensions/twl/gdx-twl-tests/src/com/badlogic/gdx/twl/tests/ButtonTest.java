
package com.badlogic.gdx.twl.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.twl.renderer.TwlInputListener;
import com.badlogic.gdx.twl.renderer.TwlRenderer;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;

public class ButtonTest implements ApplicationListener {
	GUI gui;	
	TwlInputListener guiInputListener;

	public void create () {
		if (gui != null) return;

		Button button = new Button("Click Me");
		FPSCounter fpsCounter = new FPSCounter(4, 2);

		DialogLayout layout = new DialogLayout();
		layout.setTheme("");
		layout.setHorizontalGroup(layout.createParallelGroup().addWidgets(button, fpsCounter));
		layout.setVerticalGroup(layout.createSequentialGroup().addWidget(button).addGap(5).addWidget(fpsCounter).addGap(5));

		gui = TwlRenderer.createGUI(layout, "data/widgets.xml", FileType.Internal);
		gui.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		guiInputListener = new TwlInputListener(gui);
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);	
		
		Gdx.input.processEvents(guiInputListener);
		gui.update();		
	}

	public void dispose () {
		gui.destroy();
	}	

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}
}

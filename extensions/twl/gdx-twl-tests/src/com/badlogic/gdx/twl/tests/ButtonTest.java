
package com.badlogic.gdx.twl.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.twl.renderer.TwlRenderer;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Timer;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import de.matthiasmann.twl.textarea.Style;
import de.matthiasmann.twl.textarea.StyleAttribute;
import de.matthiasmann.twl.textarea.TextAreaModel.Element;
import de.matthiasmann.twl.textarea.Value;

public class ButtonTest implements RenderListener {
	GUI gui;

	public void surfaceCreated () {
		if (gui != null) return;

		Button button = new Button("Click Me");
		FPSCounter fpsCounter = new FPSCounter(4, 2);

		DialogLayout layout = new DialogLayout();
		layout.setTheme("");
		layout.setHorizontalGroup(layout.createParallelGroup().addWidgets(button, fpsCounter));
		layout.setVerticalGroup(layout.createSequentialGroup().addWidget(button).addGap(5).addWidget(fpsCounter).addGap(5));

		gui = TwlRenderer.createGUI(layout, Gdx.files.getFileHandle("data/widgets.xml", FileType.Internal));
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gui.update();
	}

	public void surfaceChanged (int width, int height) {
		gui.setSize();
	}

	public void dispose () {
		gui.destroy();
	}
}

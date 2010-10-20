
package com.badlogic.gdx.twl.tests;

import java.io.IOException;
import java.net.URL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.twl.renderer.GdxInputListener;
import com.badlogic.gdx.twl.renderer.GdxRenderer;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Timer;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import de.matthiasmann.twl.textarea.Style;
import de.matthiasmann.twl.textarea.StyleAttribute;
import de.matthiasmann.twl.textarea.TextAreaModel;
import de.matthiasmann.twl.textarea.Value;
import de.matthiasmann.twl.textarea.TextAreaModel.Element;
import de.matthiasmann.twl.theme.ThemeManager;

public class TextAreaTest implements RenderListener {
	GUI gui;

	public void surfaceCreated () {
		if (gui != null) return;

		final HTMLTextAreaModel htmlText = new HTMLTextAreaModel();
		TextArea textArea = new TextArea(htmlText);
		htmlText
			.setHtml("<div style='font-family:heading;text-align:center'>TWL TextAreaTest</div><a href='badlogic'><img src='badlogic' id='badlogic' style='float:right; margin:10px'/></a>Lorem ipsum dolor sit amet, douchebagus joglus. Sed fermentum gravida turpis, sit amet gravida justo laoreet non. Donec ultrices suscipit metus a mollis. Mollis varius egestas quisque feugiat pellentesque mi, quis scelerisque velit bibendum eget. Nulla orci in enim nisl mattis varius dignissim fringilla.<br/><br/><img src='twllogo' style='float:left; margin:10px'/>Curabitur purus leo, ultricies ut cursus eget, adipiscing in quam. Duis non velit vel mauris vulputate fringilla et quis.<br/><br/><div>Suspendisse lobortis iaculis tellus id fermentum. Integer fermentum varius pretium. Nullam libero magna, mattis vel placerat ac, dignissim sed lacus. Mauris varius libero id neque auctor a auctor odio fringilla.</div><br/><div>Mauris orci arcu, porta eget porttitor luctus, malesuada nec metus. Nunc fermentum viverra leo eu pretium. Curabitur vitae nibh massa, imperdiet egestas lectus. Nulla odio quam, lobortis eget fermentum non, faucibus ac mi. Morbi et libero nulla. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Aliquam sit amet rhoncus nulla. Morbi consectetur ante convallis ante tristique et porta ligula hendrerit. Donec rhoncus ornare augue, sit amet lacinia nulla auctor venenatis.</div><br/><div>Etiam semper egestas porta. Proin luctus porta faucibus. Curabitur sagittis, lorem nec imperdiet ullamcorper, sem risus consequat purus, non faucibus turpis lorem ut arcu. Nunc tempus lobortis enim vitae facilisis. Morbi posuere quam nec sem aliquam eleifend.</div>");
		ScrollPane scrollPane = new ScrollPane(textArea);
		scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
		FPSCounter fpsCounter = new FPSCounter(4, 2);

		DialogLayout layout = new DialogLayout();
		layout.setTheme("");
		layout.setHorizontalGroup(layout.createParallelGroup().addWidgets(scrollPane, fpsCounter));
		layout.setVerticalGroup(layout.createSequentialGroup().addWidget(scrollPane).addGap(5).addWidget(fpsCounter).addGap(5));

		GdxRenderer renderer = new GdxRenderer();
		gui = new GUI(layout, renderer, null);
		URL themeURL = getClass().getResource("/widgets.xml");
		try {
			gui.applyTheme(ThemeManager.createThemeManager(themeURL, renderer));
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error loading theme: " + themeURL, ex);
		}

		Gdx.input.addInputListener(new GdxInputListener(gui));

		textArea.addCallback(new TextArea.Callback() {
			Timer timer;
			int speed = 8, size = 256;

			public void handleLinkClicked (String href) {
				final Element element = htmlText.getElementById("badlogic");
				if (timer == null) {
					timer = gui.createTimer();
					timer.setDelay(32);
					timer.setContinuous(true);
					timer.setCallback(new Runnable() {
						public void run () {
							size += speed;
							if (size == 256 || size == 128) timer.stop();
							Style style = element.getStyle();
							style = style.with(StyleAttribute.WIDTH, new Value(size, Value.Unit.PX));
							style = style.with(StyleAttribute.HEIGHT, new Value(size, Value.Unit.PX));
							element.setStyle(style);
							htmlText.domModified();
						}
					});
				}
				if (timer.isRunning()) return;
				timer.start();
				speed = -speed;
			}
		});
	}

	public void render () {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		gui.update();
	}

	public void surfaceChanged (int width, int height) {
	}

	public void dispose () {
	}
}

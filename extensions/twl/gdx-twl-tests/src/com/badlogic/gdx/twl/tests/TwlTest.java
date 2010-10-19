
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
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import de.matthiasmann.twl.theme.ThemeManager;

public class TwlTest implements RenderListener {
	private GUI gui;

	public void surfaceCreated () {
		if (gui != null) return;

		HTMLTextAreaModel htmlText = new HTMLTextAreaModel();
		TextArea textArea = new TextArea(htmlText);
		htmlText
			.setHtml("<img src='badlogic' style='float:right; margin:10px'/>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed fermentum gravida turpis, sit amet gravida justo laoreet non. Mollis varius douchebagus joglus egestas quisque feugiat pellentesque mi, quis scelerisque velit bibendum eget. Nulla orci in enim nisl mattis varius dignissim fringilla.<br/><br/>Curabitur purus leo, ultricies ut cursus eget, adipiscing in quam.<br/><div>Duis non velit vel mauris vulputate fringilla et quis.</div><br/><div>Phasellus at arcu mauris, at ullamcorper metus. Etiam tincidunt, ipsum ac scelerisque volutpat, enim urna laoreet magna, sed feugiat nisl velit sed felis. In aliquet orci eget neque varius non malesuada leo ultrices. Praesent malesuada, est id aliquet ultrices, magna leo faucibus diam, sit amet volutpat dui enim quis lectus. Donec ultrices suscipit metus a mollis.</div><br/><div>Suspendisse aliquam sodales eleifend. Aenean at ornare turpis. Maecenas eu quam lorem, id dapibus elit. Nunc ut neque quis diam faucibus lacinia ac quis neque. Duis vitae magna ante. Aenean posuere mauris id nisi convallis lacinia. Nunc sed nunc id nisl mattis hendrerit sed ut nunc. Sed imperdiet orci in enim laoreet ac ornare orci semper.</div><div>Duis non velit vel mauris vulputate fringilla et quis.</div><br/><div>Phasellus at arcu mauris, at ullamcorper metus. Etiam tincidunt, ipsum ac scelerisque volutpat, enim urna laoreet magna, sed feugiat nisl velit sed felis. In aliquet orci eget neque varius non malesuada leo ultrices. Praesent malesuada, est id aliquet ultrices, magna leo faucibus diam, sit amet volutpat dui enim quis lectus. Donec ultrices suscipit metus a mollis.</div><br/><div>Suspendisse aliquam sodales eleifend. Aenean at ornare turpis. Maecenas eu quam lorem, id dapibus elit. Nunc ut neque quis diam faucibus lacinia ac quis neque. Duis vitae magna ante. Aenean posuere mauris id nisi convallis lacinia. Nunc sed nunc id nisl mattis hendrerit sed ut nunc. Sed imperdiet orci in enim laoreet ac ornare orci semper.</div><div>Duis non velit vel mauris vulputate fringilla et quis.</div><br/><div>Phasellus at arcu mauris, at ullamcorper metus. Etiam tincidunt, ipsum ac scelerisque volutpat, enim urna laoreet magna, sed feugiat nisl velit sed felis. In aliquet orci eget neque varius non malesuada leo ultrices. Praesent malesuada, est id aliquet ultrices, magna leo faucibus diam, sit amet volutpat dui enim quis lectus. Donec ultrices suscipit metus a mollis.</div><br/><div>Suspendisse aliquam sodales eleifend. Aenean at ornare turpis. Maecenas eu quam lorem, id dapibus elit. Nunc ut neque quis diam faucibus lacinia ac quis neque. Duis vitae magna ante. Aenean posuere mauris id nisi convallis lacinia. Nunc sed nunc id nisl mattis hendrerit sed ut nunc. Sed imperdiet orci in enim laoreet ac ornare orci semper.</div><div>Duis non velit vel mauris vulputate fringilla et quis.</div><br/><div>Phasellus at arcu mauris, at ullamcorper metus. Etiam tincidunt, ipsum ac scelerisque volutpat, enim urna laoreet magna, sed feugiat nisl velit sed felis. In aliquet orci eget neque varius non malesuada leo ultrices. Praesent malesuada, est id aliquet ultrices, magna leo faucibus diam, sit amet volutpat dui enim quis lectus. Donec ultrices suscipit metus a mollis.</div><br/><div>Suspendisse aliquam sodales eleifend. Aenean at ornare turpis. Maecenas eu quam lorem, id dapibus elit. Nunc ut neque quis diam faucibus lacinia ac quis neque. Duis vitae magna ante. Aenean posuere mauris id nisi convallis lacinia. Nunc sed nunc id nisl mattis hendrerit sed ut nunc. Sed imperdiet orci in enim laoreet ac ornare orci semper.</div><div>Duis non velit vel mauris vulputate fringilla et quis.</div><br/><div>Phasellus at arcu mauris, at ullamcorper metus. Etiam tincidunt, ipsum ac scelerisque volutpat, enim urna laoreet magna, sed feugiat nisl velit sed felis. In aliquet orci eget neque varius non malesuada leo ultrices. Praesent malesuada, est id aliquet ultrices, magna leo faucibus diam, sit amet volutpat dui enim quis lectus. Donec ultrices suscipit metus a mollis.</div><br/><div>Suspendisse aliquam sodales eleifend. Aenean at ornare turpis. Maecenas eu quam lorem, id dapibus elit. Nunc ut neque quis diam faucibus lacinia ac quis neque. Duis vitae magna ante. Aenean posuere mauris id nisi convallis lacinia. Nunc sed nunc id nisl mattis hendrerit sed ut nunc. Sed imperdiet orci in enim laoreet ac ornare orci semper.</div>");
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

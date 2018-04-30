package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ScrollPaneTextAreaTest extends GdxTest {
	Stage stage;
   TextArea textArea;
   ScrollPane scrollPane;
   Skin skin;


   @Override
   public void create () {
   	stage = new Stage(new ScreenViewport());
      skin = new Skin(Gdx.files.internal("data/uiskin.json"));
      Gdx.input.setInputProcessor(stage);

      Table container = new Table();
      stage.addActor(container);

      container.setFillParent(true);
      container.pad(10).defaults().expandX().fillX().space(4);

      textArea = new TextArea(
      		">>> FIRST LINE <<<\n"
      		+ "Scrolling to the bottom of the area you should see the last line.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
				+ "Scrolling to the top of the area you should see the first line.\n"
				+ ">>> LAST LINE <<<",
          skin) {
          public float getPrefHeight () {
              return getLines() * getStyle().font.getLineHeight();
          }
      };

      scrollPane = new ScrollPane(textArea, skin);
      scrollPane.setFadeScrollBars(false);
      scrollPane.setFlickScroll(false);

      container.row().height(350);
      container.add(scrollPane);
      
      container.debugAll();
   }

   @Override
   public void render () {
   	if (textArea.getHeight() != textArea.getPrefHeight()) {
   		scrollPane.invalidate();
   		scrollPane.scrollTo(0, textArea.getHeight() - textArea.getCursorY(), 0, textArea.getStyle().font.getLineHeight());
   	}

      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
      stage.act(Gdx.graphics.getDeltaTime());
      stage.draw();
   }

   @Override
   public void resize (int width, int height) {
       stage.getViewport().update(width, height, true);
   }

   @Override
   public void dispose () {
       stage.dispose();
       skin.dispose();
   }
}
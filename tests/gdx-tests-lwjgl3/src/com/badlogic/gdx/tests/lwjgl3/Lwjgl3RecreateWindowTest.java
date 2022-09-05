package com.badlogic.gdx.tests.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

/** Tests for window recreation when changing settings such as MSAA sampling.
 *
 * {@link com.badlogic.gdx.InputProcessor} carries over into the newly created window.
 *
 * @author aretecorp */
public class Lwjgl3RecreateWindowTest extends GdxTest {

    private Skin skin;
    private Stage stage;

    @Override
    public void create () {
        stage = new Stage();
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        Gdx.input.setInputProcessor(stage);
        Table root = new Table();
        root.setFillParent(true);
        root.align(Align.left | Align.top);
        stage.addActor(root);

        createActors();

        System.out.println(Lwjgl3RecreateWindowTest.class.getSimpleName() + " has been created.");
    }

    private void createActors() {
        Table table = new Table();
        table.setFillParent(true);

        Label label = new Label("You gotta squint for this one.", skin);
        table.add(label).colspan(2).center();
        table.row();

        TextButton button = new TextButton("x16 MSAA", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                ((Lwjgl3Graphics)Gdx.graphics).setMSAASamples(16);
            }
        });
        table.add(button);

        button = new TextButton("x0 MSAA", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                ((Lwjgl3Graphics)Gdx.graphics).setMSAASamples(0);
            }
        });
        table.add(button);

        stage.addActor(table);
    }

    @Override
    public void render () {
        ScreenUtils.clear(1, 0, 0, 1);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize (int width, int height) {
        System.out.println(Lwjgl3RecreateWindowTest.class.getSimpleName() + " has resized with " + width + "x" + height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void resume () {
    }

    @Override
    public void pause () {
    }

    @Override
    public void dispose () {
        System.out.println(Lwjgl3RecreateWindowTest.class.getSimpleName() + " has been disposed of.");
    }

    public static void main (String[] argv) throws SecurityException {
        final Lwjgl3RecreateWindowTest test = new Lwjgl3RecreateWindowTest();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(800, 600);
        config.setTitle("Recreate window test");

        new Lwjgl3Application(test, config);
    }

}

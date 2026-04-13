/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tests.utils.GdxTest;

public class PreventTouchPropagationTest extends GdxTest {
    static final int ACTOR_DIMENSION = 100;

    Stage stage;
    ScrollPane pane;

    CheckBox checkBox;

    // Affected actors
    TextField textField;
    TextArea textArea;
    Slider slider;
    Touchpad touchpad;

    public void create () {
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

        checkBox = new CheckBox("Prevent touch propagation", skin);
        checkBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean isChecked = checkBox.isChecked();
                textField.setPreventTouchPropagation(isChecked);
                textArea.setPreventTouchPropagation(isChecked);
                slider.setPreventTouchPropagation(isChecked);
                touchpad.setPreventTouchPropagation(isChecked);
            }
        });

        textField = new TextField("This is a long line, and will continue to be long until the line is done.", skin);
        textArea = new TextArea("The quick brown fox jumps over the lazy dog.", skin);
        slider = new Slider(0, 10, 1, false, skin);
        touchpad = new Touchpad(20, skin);
        //touchpad.setBounds(15, 15, 100, 100);

        pane = new ScrollPane(new Table() {
            {
                add(textField).width(ACTOR_DIMENSION);
                row();
                addFillerLabels(this, skin, 5);
                add(textArea).size(ACTOR_DIMENSION);
                row();
                addFillerLabels(this, skin, 5);
                add(slider).width(ACTOR_DIMENSION);
                row();
                addFillerLabels(this, skin, 5);
                add(touchpad).size(ACTOR_DIMENSION);
            }
        }, skin);
        pane.setFadeScrollBars(false);

        stage.addActor(new Table() {
            {
                setFillParent(true);

                add(new Label("Drag on the the actors in the ScrollPane to see how they behave with/without preventTouchPropagation.", skin) {
                    {
                        setWrap(true);
                    }
                }).prefWidth(390);
                add(checkBox).expandX().right();

                row().padTop(15);
                add(pane).grow().colspan(2);
            }
        });
    }

    public void addFillerLabels(Table table, Skin skin, int count) {
        for (int i = 0; i < count; i++) {
            table.add(new Label("cheese " + i, skin));
            table.row();
        }
    }

    public void render () {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void resize (int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose () {
        stage.dispose();
    }
}

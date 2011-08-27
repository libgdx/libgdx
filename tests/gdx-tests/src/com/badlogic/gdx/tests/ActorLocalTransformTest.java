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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Forever;
import com.badlogic.gdx.scenes.scene2d.actions.RotateBy;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ActorLocalTransformTest extends GdxTest {

    Stage stage;
    Image dot;
    Image testbar;

    @Override
    public boolean needsGL20() {
        return false;
    }

    @Override
    public void create() {

        testbar = new Image("testbar", new Texture(Gdx.files.internal("data/testbar.png")));
        testbar.x = 240 - testbar.originX;
        testbar.y = 160 - testbar.originY;

        dot = new Image("dot", new Texture(Gdx.files.internal("data/testdot.png")));

        stage = new Stage(480, 320, true);
        stage.addActor(testbar);
        stage.addActor(dot);

        testbar.action(Forever.$(RotateBy.$(20, 1)));
    }

    private final Vector2 tmp = new Vector2();

    @Override
    public void render() {

        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        final float d = Gdx.graphics.getDeltaTime();

        tmp.set(testbar.x, testbar.y);
        testbar.toLocalCoordinates(tmp);

        dot.x = testbar.x + tmp.x;
        dot.y = testbar.y + tmp.y;

        stage.act(d);
        stage.draw();


    }


}

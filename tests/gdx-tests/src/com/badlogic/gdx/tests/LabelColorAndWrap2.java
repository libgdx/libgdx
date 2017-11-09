package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FillViewport;

import javax.swing.GroupLayout;

/**
 * Created by jdpujo on 21/10/2017.
 */

public class LabelColorAndWrap2 extends GdxTest {
    Stage activeStage;
    Label label;

    @Override
    public void create() {
        String tex1= "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris ut lacus maximus, volutpat ex vel, tempor leo. Vivamus elit risus, iaculis at ante vel, commodo mattis tellus. Etiam ac tempor quam, quis aliquet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Duis nisl velit, scelerisque sed ultricies ornare, ultrices non tortor. Duis justo magna, dapibus quis accumsan a, tristique eu metus. Vestibulum pellentesque lobortis ante, vel maximus odio ultricies ut. Sed lobortis cursus sem, a facilisis ex volutpat in. Pellentesque id libero quis ipsum euismod ullamcorper sit amet at enim. Donec sed dictum ex."
                + "Donec dignissim felis et malesuada egestas. Vivamus vel fringilla lorem. Mauris accumsan eget turpis non volutpat. Nunc facilisis laoreet mauris, scelerisque aliquet lacus eleifend eu. Integer id dignissim purus. Nunc dignissim commodo venenatis. Nullam massa mauris, elementum at rutrum sit amet, lacinia ut arcu. Quisque non nibh eget justo ornare maximus. Vestibulum finibus mi augue, eu gravida purus pulvinar ut. In fringilla dignissim suscipit."
                + "Fusce vitae massa consequat, finibus mauris id, efficitur nunc. Praesent nec orci quis sem euismod viverra. Vivamus non lacus nec ante consequat blandit sed sit amet purus. Mauris orci diam, interdum sit amet ipsum et, vestibulum varius sem. Integer ornare tortor id tortor varius, a varius sem volutpat. Suspendisse hendrerit condimentum tincidunt. Sed pellentesque vitae tellus sed efficitur. Etiam sodales lorem tellus, at dictum mauris pellentesque et. In vitae scelerisque nisl. Proin semper ante et quam volutpat, faucibus gravida felis consequat. Quisque ac urna mollis, scelerisque est et, venenatis risus. Etiam ac volutpat nibh.";

        String tex2= "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris ut lacus[GREEN] maximus, volutpat ex vel, tempor leo. Vivamus elit risus, iaculis at ante vel, commodo mattis tellus. Etiam ac tempor quam, quis aliquet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Duis nisl velit, scelerisque sed ultricies ornare, ultrices non tortor. Duis justo magna, dapibus quis accumsan a, tristique eu metus. Vestibulum pellentesque lobortis ante, vel maximus odio ultricies ut. Sed lobortis cursus sem, a facilisis ex volutpat in. Pellentesque id libero quis ipsum euismod ullamcorper sit amet at enim. Donec sed dictum ex."
                + "Donec dignissim felis et malesuada egestas. Vivamus vel fringilla lorem. Mauris accumsan eget turpis non volutpat. Nunc facilisis laoreet mauris, scelerisque aliquet lacus eleifend eu. Integer id dignissim purus. Nunc dignissim commodo venenatis. Nullam massa mauris, elementum at rutrum sit amet, lacinia ut arcu. Quisque non nibh eget justo ornare maximus. Vestibulum finibus mi augue, eu gravida purus pulvinar ut. In fringilla dignissim suscipit."
                + "Fusce vitae massa consequat, finibus mauris id, efficitur nunc. Praesent nec orci quis sem euismod viverra. Vivamus non lacus nec ante consequat blandit sed sit amet purus. Mauris orci diam, interdum sit amet ipsum et, vestibulum varius sem. Integer ornare tortor id tortor varius, a varius sem volutpat. Suspendisse hendrerit condimentum tincidunt. Sed pellentesque vitae tellus sed efficitur. Etiam sodales lorem tellus, at dictum mauris pellentesque et. In vitae scelerisque nisl. Proin semper ante et quam volutpat, faucibus gravida felis consequat. Quisque ac urna mollis, scelerisque est et, venenatis risus. Etiam ac volutpat nibh.";

        String tex3= "Lorem [[hello] ipsum [RED]dolor[] sit amet, consectetur adipiscing elit. Mauris ut lacus maximus, volutpat ex vel, tempor leo. Vivamus elit risus, iaculis at ante vel, commodo mattis tellus. Etiam ac tempor quam, quis aliquet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Duis nisl velit, scelerisque sed ultricies ornare, ultrices non tortor. Duis justo magna, dapibus quis accumsan a, tristique eu metus. Vestibulum pellentesque lobortis ante, vel maximus odio ultricies ut. Sed lobortis cursus sem, a facilisis ex volutpat in. Pellentesque id libero quis ipsum euismod ullamcorper sit amet at enim. Donec sed dictum ex."
                + "Donec dignissim felis ]et ]malesuada egestas. Vivamus vel fringilla lorem. Mauris accumsan eget turpis non volutpat. Nunc facilisis laoreet mauris, scelerisque aliquet lacus eleifend eu. Integer id dignissim purus. Nunc dignissim commodo venenatis. Nullam massa mauris, elementum at rutrum sit amet, lacinia ut arcu. Quisque non nibh eget justo ornare maximus. Vestibulum finibus mi augue, eu gravida purus pulvinar ut. In fringilla dignissim suscipit."
                + "Fusce v[[i]]tae [ [ []]massa ]consequat, finibus mauris id, efficitur nunc. Praesent nec orci quis sem euismod viverra. Vivamus non lacus nec ante consequat blandit sed sit amet purus. Mauris orci diam, interdum sit amet ipsum et, vestibulum varius sem. Integer ornare tortor id tortor varius, a varius sem volutpat. Suspendisse hendrerit condimentum tincidunt. Sed pellentesque vitae tellus sed efficitur. Etiam sodales lorem tellus, at dictum mauris pellentesque et. In vitae scelerisque nisl. Proin semper ante et quam volutpat, faucibus gravida felis consequat. Quisque ac urna mollis, scelerisque est et, venenatis risus. Etiam ac volutpat nibh.";

        String tex4= "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris ut lacus[GREEN]            maximus, volutpat ex vel";

        String TEX= tex2; //LabelColorAndWrap.GetRandomColorText(tex1);

        Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        activeStage = new Stage(new FillViewport(640, 480));
        //BitmapFont font = new BitmapFont();
        //font.getData().markupEnabled = true;
        Table stageContentTable = new Table();
        stageContentTable.setFillParent(true);
        //Label l = new Label("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris ut lacus [GREEN]maximus, volutpat ex vel, tempor leo. Vivamus elit risus, iaculis at ante vel, commodo mattis tellus. Etiam ac tempor quam, quis aliquet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Duis nisl velit, scelerisque sed ultricies ornare, ultrices non tortor. Duis justo magna, dapibus quis accumsan a, tristique eu metus. Vestibulum pellentesque lobortis ante, vel maximus odio ultricies ut. Sed lobortis cursus sem, a facilisis ex volutpat in. Pellentesque id libero quis ipsum euismod ullamcorper sit amet at enim. Donec sed dictum ex."
        Label l = new Label(TEX, skin);
        l.setWrap(true);
        //l.setAlignment(Align.right);
        l.getStyle().font.getData().markupEnabled = true;
        stageContentTable.add(l).top().left().width(560);
        stageContentTable.setDebug(true, true);
        activeStage.addActor(stageContentTable);
        label=l;
        dodebug=true;
    }

    private boolean dodebug=false;

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        activeStage.act();
        activeStage.draw();

        if (dodebug) {
            dodebug=false;
            Gdx.app.log("@@@JD", "s1="+label.getWidth());
        }

    }
}

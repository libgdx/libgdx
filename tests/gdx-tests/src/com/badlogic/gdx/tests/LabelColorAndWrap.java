package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.viewport.FillViewport;

import java.util.Random;

/**
 * Created by jdpujo on 21/10/2017.
 */

public class LabelColorAndWrap extends GdxTest {
    final String TEXT= "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris ut lacus maximus, volutpat ex vel, tempor leo. Vivamus elit risus, iaculis at ante vel, commodo mattis tellus. Etiam ac tempor quam, quis aliquet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Duis nisl velit, scelerisque sed ultricies ornare, ultrices non tortor.";
    final static String COLORS[]= {    "WHITE",
                                "LIGHT_GRAY",
                                "GRAY",
                                "DARK_GRAY",
                                "BLUE",
                                "NAVY",
                                "ROYAL",
                                "SLATE",
                                "SKY",
                                "CYAN",
                                "TEAL",
                                "GREEN",
                                "CHARTREUSE",
                                "LIME",
                                "FOREST",
                                "OLIVE",
                                "YELLOW",
                                "GOLD",
                                "GOLDENROD",
                                "ORANGE",
                                "BROWN",
                                "TAN",
                                "FIREBRICK",
                                "RED",
                                "SCARLET",
                                "CORAL",
                                "SALMON",
                                "PINK",
                                "MAGENTA",
                                "PURPLE",
                                "VIOLET",
                                "MAROON",
            "#404040",
            "#404050",
            "#404060",
            "#404070",
            "#404080",
            "#404090",
            "#4040A0",
            "#4040B0",
            "#4040C0",
            "#4040D0",
            "#4040E0",

    };

    Stage activeStage;

    // static final String TEXT_PLAIN = "123 456";
    // static final String TEXT_COLOR = "[RED]123 [BLUE]456";
    // static final String TEXT_ESCAPE = "[RED]123[] [BLUE]456[]";
    static final String TEXT_PLAIN = "AAA BBB CCC DDD EEE";
    static final String TEXT_COLOR = "[RED]AAA [BLUE]BBB [RED]CCC [BLUE]DDD [RED]EEE";
    static final String TEXT_ESCAPE = "[[ORANGE]Escaped, but is colored";


    //static final String TEXT_ESCAPE = "[[ORANGE]Escaped, but is colored";

    @Override
    public void create() {
    }

    public static String GetRandomColorText (String text)
    {
        Random rnd= new Random();

        int numColors= rnd.nextInt(20)+1;
        for (int i=0; i<numColors; i++) {
            String col= "["+COLORS[rnd.nextInt(COLORS.length)]+"]";

            int p1= rnd.nextInt(text.length());
            int p2= rnd.nextInt(text.length());
            if (p2>p1+20) {
                text= text.substring(0,p1) + col + text.substring(p1);
                text= text.substring(0,p2) + "[]" + text.substring(p2);
            }
            else {
                text= text.substring(0,p1) + col + text.substring(p1);
            }
        }
        return text;
    }

    Label l1,l2;

    @Override
    public void resize (int width, int height) {
        activeStage = new Stage(new FillViewport(640, 480));
        //BitmapFont font = new BitmapFont();
        //font.getData().markupEnabled = true;
        Table stageContentTable = new Table();
        Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        stageContentTable.setFillParent(true);

        stageContentTable.row();
        {
            Label l = new Label("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris ut lacus maximus, volutpat ex vel, tempor leo. Vivamus elit risus, iaculis at ante vel, commodo mattis tellus. Etiam ac tempor quam, quis aliquet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Duis nisl velit, scelerisque sed ultricies ornare, ultrices non tortor."
                    , skin); //, new Label.LabelStyle(font, Color.WHITE));
            l.setWrap(true);
            l.getStyle().font.getData().markupEnabled = true;
            stageContentTable.add(l).top().left().width(560);
            float s1= l.getWidth();
            l1=l;
        }

        stageContentTable.row();
        {
            Label l = new Label("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris ut lacus[GREEN] maximus, volutpat ex vel, tempor leo. Vivamus elit risus, iaculis at ante vel, commodo mattis tellus. Etiam ac tempor quam, quis aliquet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Duis nisl velit, scelerisque sed ultricies ornare, ultrices non tortor."
                    , skin); //, new Label.LabelStyle(font, Color.WHITE));
            l.setWrap(true);
            l.getStyle().font.getData().markupEnabled = true;
            stageContentTable.add(l).top().left().width(560);
            float s1= l.getWidth();
            l2=l;
        }



        stageContentTable.row();
        {
            String tex= GetRandomColorText(TEXT);
            Gdx.app.log("@@@JD", "TEXT=\""+tex+"\"");

            Label l = new Label(tex, skin); //, new Label.LabelStyle(font, Color.WHITE));
            l.setWrap(true);
            l.getStyle().font.getData().markupEnabled = true;
            stageContentTable.add(l).top().left().width(560);
            float s1= l.getWidth();
        }




        stageContentTable.row();
        {
            Label l1 = new Label(TEXT_PLAIN, skin); //, new Label.LabelStyle(font, Color.WHITE));
            float s1= l1.getWidth();
            Gdx.app.log("@@@JD", "s1="+s1);
            //l1.setWrap(true);
            l1.getStyle().font.getData().markupEnabled = true;
            stageContentTable.add(l1).top().left().width(560);
        }
        stageContentTable.row();
        {
            Label l1 = new Label(TEXT_COLOR, skin); //, new Label.LabelStyle(font, Color.WHITE));
            float s1= l1.getWidth();
            Gdx.app.log("@@@JD", "s2="+s1);
            //l1.setWrap(true);
            l1.getStyle().font.getData().markupEnabled = true;
            stageContentTable.add(l1).top().left().width(560);
        }
        stageContentTable.row();
        {
            Label l1 = new Label(TEXT_ESCAPE, skin); //, new Label.LabelStyle(font, Color.WHITE));
            //l1.setText(TEXT_ESCAPE);
            float s1= l1.getWidth();
            Gdx.app.log("@@@JD", "s3="+s1);
            //l1.setWrap(true);
            l1.getStyle().font.getData().markupEnabled = true;
            stageContentTable.add(l1).top().left().width(560);
        }
        stageContentTable.row();



        stageContentTable.setDebug(true, true);
        activeStage.addActor(stageContentTable);
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
            Gdx.app.log("@@@JD", "s1="+l1.getWidth()+" s2="+l2.getWidth());
        }

    }
}

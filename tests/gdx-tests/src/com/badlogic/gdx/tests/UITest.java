
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureAtlas;
import com.badlogic.gdx.graphics.TextureRegion;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.Forever;
import com.badlogic.gdx.scenes.scene2d.actions.MoveBy;
import com.badlogic.gdx.scenes.scene2d.actions.MoveTo;
import com.badlogic.gdx.scenes.scene2d.actions.Parallel;
import com.badlogic.gdx.scenes.scene2d.actions.Repeat;
import com.badlogic.gdx.scenes.scene2d.actions.RotateBy;
import com.badlogic.gdx.scenes.scene2d.actions.RotateTo;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleTo;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.actors.Button;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.scenes.scene2d.actors.LinearGroup;
import com.badlogic.gdx.scenes.scene2d.actors.Button.ClickListener;
import com.badlogic.gdx.scenes.scene2d.actors.LinearGroup.LinearGroupLayout;
import com.badlogic.gdx.tests.utils.GdxTest;

public class UITest implements GdxTest, InputListener {
	Texture uiTexture;
	Texture badlogic;
	TextureAtlas atlas;
	Stage ui;
//	Font font;

	@Override public void surfaceCreated () {
		if (uiTexture == null) {
			Gdx.input.addInputListener(this);
			Gdx.input.setCatchBackKey( true );

//			font = Gdx.graphics.newFont( "Droid Sans", 20, FontStyle.Plain );
			
			uiTexture = Gdx.graphics.newTexture(Gdx.files.getFileHandle("data/ui.png", FileType.Internal), TextureFilter.Linear,
				TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);

			badlogic = Gdx.graphics.newTexture(Gdx.files.getFileHandle("data/badlogic.jpg", FileType.Internal),
				TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);				

			ui = new Stage(480, 320, false);
			atlas = new TextureAtlas(uiTexture);
			atlas.addRegion("blend", 0, 0, 64, 32);
			atlas.addRegion("blendDown", -1, -1, 64, 32);
			atlas.addRegion("rotate", 64, 0, 64, 32);
			atlas.addRegion("rotateDown", 63, -1, 64, 32);
			atlas.addRegion("scale", 64, 32, 64, 32);
			atlas.addRegion("scaleDown", 63, 31, 64, 32);
			atlas.addRegion("button", 0, 64, 64, 32);
			atlas.addRegion("buttonDown", -1, 63, 64, 32);

			Image img1 = new Image("image1", new TextureRegion(badlogic, 0, 0, 256, 256));
			img1.width = img1.height = 64;
			img1.originX = img1.originY = 32;
			img1.action(Sequence.$(FadeOut.$(1), 
										  FadeIn.$(1), 
										  Delay.$(MoveTo.$(100, 100, 1), 2), ScaleTo.$(0.5f, 0.5f, 1),
										  FadeOut.$(0.5f), 
										  Delay.$(Parallel.$(RotateTo.$(360, 1), FadeIn.$(1), ScaleTo.$(1, 1, 1)), 1)));
			ui.addActor(img1);

			Image img2 = new Image("image2", new TextureRegion(badlogic, 0, 0, 256, 256));
			img2.width = img2.height = 64;
			img2.originX = img2.originY = 32;
			img2.action(Repeat.$(Sequence.$(MoveBy.$(50, 0, 1), 
													  MoveBy.$(0, 50, 1), 
													  MoveBy.$(-50, 0, 1), 
													  MoveBy.$(0, -50, 1)), 3));
			ui.addActor(img2);

			Button button = new Button("button", atlas.getRegion("button"), atlas.getRegion("buttonDown"));
			button.action(Forever.$(RotateBy.$(360, 4)));
			button.clickListener = new ClickListener() {
				
				@Override public void clicked (Button button) {
					if( Gdx.input.supportsOnscreenKeyboard() )
						Gdx.input.setOnscreenKeyboardVisible( true );
				}
			};
			ui.addActor(button);			
			
//			Label label = new Label( "label", font, "text input: " );
//			label.x = 10; label.y = Gdx.graphics.getHeight() - 20;
//			ui.addActor(label);

			LinearGroup linear = new LinearGroup("linear", 64, 32 * 3, LinearGroupLayout.Vertical);
			linear.x = 200;
			linear.y = 150;
			linear.scaleX = linear.scaleY = 0;
			linear.addActor(new Button("blend", atlas.getRegion("blend"), atlas.getRegion("blendDown")));
			linear.addActor(new Button("scale", atlas.getRegion("scale"), atlas.getRegion("scaleDown")));
			linear.addActor(new Button("rotate", atlas.getRegion("rotate"), atlas.getRegion("rotateDown")));
			linear.action(Parallel.$(ScaleTo.$(1, 1, 2), RotateTo.$(720, 2)));
			ui.addActor(linear);

			LinearGroup linearh = new LinearGroup("linearh", 64 * 3, 32, LinearGroupLayout.Horizontal);
			linearh.x = 500;
			linearh.y = 10;
			linearh.addActor(new Button("blendh", atlas.getRegion("blend"), atlas.getRegion("blendDown")));
			linearh.addActor(new Button("scaleh", atlas.getRegion("scale"), atlas.getRegion("scaleDown")));
			linearh.addActor(new Button("rotateh", atlas.getRegion("rotate"), atlas.getRegion("rotateDown")));
			linearh.action(MoveTo.$(100, 10, 1.5f));
			ui.addActor(linearh);

// Group.enableDebugging( "data/debug.png" );
		}
	}

	@Override public void surfaceChanged (int width, int height) {

	}

	@Override public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		ui.act(Gdx.graphics.getDeltaTime());
		ui.render();
	}

	@Override public void dispose () {

	}

	@Override public boolean keyDown (int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean keyTyped (char character) {
//		Label label = ((Label)ui.findActor( "label" ));
//		if( character == '\b' ) {
//			if( label.text.length() > "text input: ".length() )
//				label.text = label.text.substring( 0, label.text.length()-1 ); 
//		} else {			
//			label.text += character;
//		}
		return false;
	}

	@Override public boolean touchDown (int x, int y, int pointer) {
		ui.touchDown(x, y, pointer);
		return false;
	}

	Vector2 point = new Vector2();

	@Override public boolean touchUp (int x, int y, int pointer) {
		if (!ui.touchUp(x, y, pointer)) {
			Actor actor = ui.findActor("image1");
			if (actor != null) {
				ui.toStageCoordinates(x, y, point);
				actor.clearActions();
				actor.action(MoveTo.$(point.x, point.y, 2));
				actor.action(RotateBy.$(90, 2));
				if (actor.scaleX == 1.0f)
					actor.action(ScaleTo.$(0.5f, 0.5f, 2));
				else
					actor.action(ScaleTo.$(1f, 1f, 2));
			}
		}
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		ui.touchDragged(x, y, pointer);
		return false;
	}

	@Override public boolean needsGL20 () {
		// TODO Auto-generated method stub
		return false;
	}
}

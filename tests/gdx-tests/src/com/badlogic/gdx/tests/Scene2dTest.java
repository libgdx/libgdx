
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.ActorListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.FloatAction;
import static com.badlogic.gdx.scenes.scene2d.Actions.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.tests.utils.GdxTest;

public class Scene2dTest extends GdxTest {
	private Stage stage;
	private FloatAction meow = new FloatAction(10, 5);

	public void create () {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		final TextureRegion region = new TextureRegion(new Texture("data/badlogic.jpg"));
		Actor actor = new Actor() {
			public void draw (SpriteBatch batch, float parentAlpha) {
				batch.setColor(getColor());
				// batch.draw(region, getX(), getY(), getWidth(), getHeight());
				batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(),
					getRotation());
			}
		};
		actor.setBounds(100, 100, 100, 100);
		actor.setOrigin(50, 50);
// stage.getRoot().addCaptureListener(new ActorListener() {
// public boolean touchDown (ActorEvent event, float x, float y, int pointer) {
// System.out.println("down " + event.getTarget());
// return false;
// }
//
// public boolean touchUp (ActorEvent event, float x, float y, int pointer) {
// System.out.println("up");
// return false;
// }
//
// public boolean touchDragged (ActorEvent event, float x, float y, int pointer) {
// System.out.println("drag");
// return false;
// }
// });
// stage.addListener(new ActorGestureListener() {
//
// public void tap (ActorEvent event, float x, float y, int count) {
// System.out.println("tap");
// }
//
// public void longPress (ActorEvent event, float x, float y) {
// System.out.println("long press");
// }
//
// public void pan (ActorEvent event, float x, float y, float deltaX, float deltaY) {
// // System.out.println("panning " + x + ", " + y);
// }
// });

		stage.addActor(actor);

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		TextButton button = new TextButton("Some Shit!", skin);
// button.addListener(new ClickListener() {
// public void clicked (ActorEvent event, float x, float y) {
// System.out.println("click! " + event.getStageX() + " " + event.getStageY());
// }
// });
		button.addListener(new ActorListener() {
			public boolean touchDown (ActorEvent event, float x, float y, int pointer, int button) {
				captureTouchUp(event);
				System.out.println("down " + x + ", " + y);
				return true;
			}

			public boolean touchUp (ActorEvent event, float x, float y, int pointer, int button) {
				System.out.println("up " + x + ", " + y);
				return true;
			}

			public boolean touchDragged (ActorEvent event, float x, float y, int pointer) {
				System.out.println("dragged " + x + ", " + y);
				return true;
			}
		});
		button.setPosition(100, 100);
		stage.addActor(button);

		meow.setDuration(2);

		//actor.addAction(parallel(moveBy(250, 250, 2)));
		actor.addAction(parallel(moveBy(0, 250, 2), moveBy(250, 0, 2)));
		// actor.addAction(parallel(moveTo(250, 250, 2, elasticOut), color(RED, 6), delay(0.5f), rotateTo(180, 5, swing)));
		// actor.addAction(forever(sequence(scaleTo(2, 2, 0.5f), scaleTo(1, 1, 0.5f), delay(0.5f))));
	}

	public void render () {
		// System.out.println(meow.getValue());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, true);
	}

	public boolean needsGL20 () {
		return false;
	}

	public void dispose () {
		stage.dispose();
	}
}

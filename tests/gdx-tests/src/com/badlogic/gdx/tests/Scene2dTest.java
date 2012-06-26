
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.ActorListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.FloatAction;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.tests.utils.GdxTest;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class Scene2dTest extends GdxTest {
	Stage stage;
	private FloatAction meow = new FloatAction(10, 5);
	private NinePatch patch;

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

		stage.addActor(actor);

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		final TextButton button = new TextButton("Some Shit!", skin.getStyle("toggle", TextButtonStyle.class));

// button.addListener(new ClickListener() {
// public void clicked (ActorEvent event, float x, float y) {
// System.out.println("click! " + x + " " + y);
// }
// });

// button.addListener(new ActorGestureListener() {
// public void tap (ActorEvent event, float x, float y, int count) {
// System.out.println("tap");
// }
//
// public boolean longPress (ActorEvent event, float x, float y) {
// System.out.println("long press");
// return false;
// }
//
// public void pan (ActorEvent event, float x, float y, float deltaX, float deltaY) {
// System.out.println("panning " + x + ", " + y);
// }
// });

		button.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				// event.cancel();
			}
		});

		button.addListener(new ActorListener() {
			public boolean touchDown (ActorEvent event, float x, float y, int pointer, int b) {
				System.out.println("down " + x + ", " + y + " " + pointer + ", " + b);
				return true;
			}

			public void touchUp (ActorEvent event, float x, float y, int pointer, int b) {
				System.out.println("up " + x + ", " + y + " " + pointer + ", " + b);
			}
		});

		button.setPosition(50, 50);
		stage.addActor(button);

// List select = new List(skin);
// select.setBounds(200, 200, 100, 100);
// select.setItems(new Object[] {1, 2, 3, 4, 5});
// stage.addActor(select);

		stage.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				System.out.println(actor);
			}
		});

		meow.setDuration(2);

		// actor.addAction(parallel(moveBy(250, 250, 2)));
		actor.addAction(parallel(rotateBy(90, 2), rotateBy(90, 2)));
		// actor.addAction(parallel(moveTo(250, 250, 2, elasticOut), color(RED, 6), delay(0.5f), rotateTo(180, 5, swing)));
		// actor.addAction(forever(sequence(scaleTo(2, 2, 0.5f), scaleTo(1, 1, 0.5f), delay(0.5f))));

		patch = skin.getPatch("default-round");
	}

	public void render () {
		// System.out.println(meow.getValue());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

		stage.getSpriteBatch().begin();
		patch.draw(stage.getSpriteBatch(), 300, 100, 50, 50);
		stage.getSpriteBatch().end();
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, true);
	}

	public boolean needsGL20 () {
		return true;
	}

	public void dispose () {
		stage.dispose();
	}
}

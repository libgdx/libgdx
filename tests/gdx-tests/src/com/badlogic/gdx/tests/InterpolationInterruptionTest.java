
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Interpolation.SplineInterpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RotateByAction;
import com.badlogic.gdx.scenes.scene2d.actions.RotateToAction;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleByAction;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleToAction;
import com.badlogic.gdx.scenes.scene2d.actions.SizeByAction;
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction.*;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

@SuppressWarnings("synthetic-access")
public class InterpolationInterruptionTest extends GdxTest {

	private Stage stage;
	private String selectedStart, selectedInterrupt;
	private SupportedAction selectedAction;
	private boolean shouldBlend;
	private Label durationLabel;
	private Image imageActor;
	private final Vector2 tmp2 = new Vector2();
	private final Vector3 tmp3 = new Vector3();
	private final Color tmpC = new Color();
	private float duration = 1.0f;
	private static final float IMG_SIZE = 20;

	private enum SupportedAction { // action types this test can apply to the actor
		Color(ColorAction.class), MoveBy(MoveAction.class), MoveTo(MoveAction.class), RotateBy(RotateAction.class), RotateTo(
			RotateAction.class), ScaleBy(ScaleAction.class), ScaleTo(ScaleAction.class);

		public final Class effectType;

		private SupportedAction (Class effectType) {
			this.effectType = effectType;
		}
	}

	private InputAdapter scrollAdapter = new InputAdapter() {
		@Override
		public boolean scrolled (int amount) {
			if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) return false;
			duration -= amount * 0.2f;
			duration = Math.max(0, Math.round(duration * 5) / 5f); // sanitize value
			return true;
		}
	};

	private InputAdapter touchAdapter = new InputAdapter() {
		@Override
		public boolean touchUp (int x, int y, int pointer, int newParam) {
			stage.getViewport().unproject(tmp3.set(x, y, 0));

			tmp3.sub(IMG_SIZE / 2); // shift image to be centered on touch point.

			Action interruptedAction = findActionOfSameEffectType(imageActor, selectedAction);
			startNewAction(shouldBlend ? interruptedAction : null, tmp3.x, tmp3.y,
				interruptedAction == null ? selectedStart : selectedInterrupt);
			imageActor.removeAction(interruptedAction);
			return true;
		}
	};

	/** @return An action on the actor that is of the same type as the input action, or null if none is found. */
	private Action findActionOfSameEffectType (Actor actor, SupportedAction action) {
		Class effectType = action.effectType;
		for (Action a : actor.getActions()) {
			if (effectType.isAssignableFrom(a.getClass())) return a;
		}
		return null;
	}

	/** @param interruptedAction Can be null if nothing to interrupt. Must match the effect type of the interpolationName. */
	private void startNewAction (Action interruptedAction, float x, float y, String interpolationName) {
		tmp2.set(imageActor.getX(), imageActor.getY());

		Interpolation interpolation = getInterpolation(interpolationName);
		SplineInterpolation interruptingInterpolation = interruptedAction != null ? (SplineInterpolation)interpolation : null;

		switch (selectedAction) {
		case Color:
			tmpC.set(x / stage.getViewport().getWorldWidth(), y / stage.getViewport().getWorldHeight(),
				MathUtils.clamp(2 * tmp2.dst(x, y) / stage.getViewport().getWorldWidth(), 0, 1), 0.5f + 0.5f * (float)Math.random());
			ColorAction colorAction = color(tmpC, duration, interpolation);
			if (interruptingInterpolation != null)
				colorAction.setBlendFrom((ColorAction)interruptedAction, interruptingInterpolation);
			imageActor.addAction(colorAction);
			break;
		case MoveBy:
			MoveByAction moveByAction = moveBy(x - tmp2.x, y - tmp2.y, duration, interpolation);
			if (interruptingInterpolation != null)
				moveByAction.setBlendFrom((MoveAction)interruptedAction, interruptingInterpolation);
			imageActor.addAction(moveByAction);
			break;
		case MoveTo:
			MoveToAction moveToction = moveTo(x, y, duration, interpolation);
			if (interruptingInterpolation != null)
				moveToction.setBlendFrom((MoveAction)interruptedAction, interruptingInterpolation);
			imageActor.addAction(moveToction);
			break;
		case RotateBy:
			float deltaAngle = -MathUtils.atan2(x - tmp2.x, y - tmp2.y) * MathUtils.radDeg - imageActor.getRotation();
			RotateByAction rotateByAction = rotateBy(deltaAngle, duration, interpolation);
			if (interruptingInterpolation != null)
				rotateByAction.setBlendFrom((RotateAction)interruptedAction, interruptingInterpolation);
			imageActor.addAction(rotateByAction);
			break;
		case RotateTo:
			float angle = -MathUtils.atan2(x - tmp2.x, y - tmp2.y) * MathUtils.radDeg;
			RotateToAction rotateToAction = rotateTo(angle, duration, interpolation);
			if (interruptingInterpolation != null)
				rotateToAction.setBlendFrom((RotateAction)interruptedAction, interruptingInterpolation);
			imageActor.addAction(rotateToAction);
			break;
		case ScaleBy:
			float deltaXScale = Math.abs((x - tmp2.x) / (imageActor.getWidth() / 2)) - imageActor.getScaleX();
			float deltaYScale = Math.abs((y - tmp2.y) / (imageActor.getHeight() / 2)) - imageActor.getScaleY();
			ScaleByAction scaleByAction = scaleBy(deltaXScale, deltaYScale, duration, interpolation);
			if (interruptingInterpolation != null)
				scaleByAction.setBlendFrom((ScaleAction)interruptedAction, interruptingInterpolation);
			imageActor.addAction(scaleByAction);
			break;
		case ScaleTo:
			float targetX = Math.abs((x - tmp2.x) / (imageActor.getWidth() / 2));
			float targetY = Math.abs((y - tmp2.y) / (imageActor.getHeight() / 2));
			ScaleToAction scaleToction = scaleTo(targetX, targetY, duration, interpolation);
			if (interruptingInterpolation != null)
				scaleToction.setBlendFrom((ScaleAction)interruptedAction, interruptingInterpolation);
			imageActor.addAction(scaleToction);
			break;
		}
	}

	private Interpolation getInterpolation (String name) {
		try {
			return (Interpolation)Interpolation.class.getField(name).get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void create () {
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		stage = new Stage(new ExtendViewport(640, 480));
		imageActor = new Image(skin, "white");
		imageActor.setSize(IMG_SIZE, IMG_SIZE);
		imageActor.setOrigin(IMG_SIZE / 2, IMG_SIZE / 2);
		stage.addActor(imageActor);

		Gdx.input.setInputProcessor(new InputMultiplexer(scrollAdapter, stage, touchAdapter));

		Field[] interpolationFields = ClassReflection.getFields(Interpolation.class);

		Array<String> startNames = new Array<String>();
		Array<String> interruptNames = new Array<String>();
		for (int i = 0; i < interpolationFields.length; i++) {
			if (Interpolation.class.isAssignableFrom(interpolationFields[i].getDeclaringClass()))
				startNames.add(interpolationFields[i].getName());
			if (SplineInterpolation.class.isAssignableFrom(interpolationFields[i].getType()))
				interruptNames.add(interpolationFields[i].getName());
		}
		selectedStart = startNames.first();
		selectedInterrupt = interruptNames.first();

		final List<String> startList = new List(skin);
		startList.setItems(startNames);
		startList.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				selectedStart = startList.getSelected();
			}
		});

		final List<String> interruptList = new List(skin);
		interruptList.setItems(interruptNames);
		interruptList.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				selectedInterrupt = interruptList.getSelected();
			}
		});

		final List<SupportedAction> actionList = new List<SupportedAction>(skin);
		actionList.setItems(SupportedAction.values());
		actionList.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				selectedAction = actionList.getSelected();
			}
		});
		actionList.setSelected(SupportedAction.MoveTo);

		final CheckBox blendCheckBox = new CheckBox("Blend", skin);
		blendCheckBox.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				shouldBlend = blendCheckBox.isChecked();
			}
		});
		blendCheckBox.setChecked(true);
		durationLabel = new Label(" ", skin);

		ListStyle listStyle = skin.get(ListStyle.class);
		listStyle.background = skin.getDrawable("default-rect");

		ScrollPane scrollStarts = new ScrollPane(startList, skin);
		scrollStarts.setFadeScrollBars(false);
		scrollStarts.setScrollingDisabled(true, false);

		Table rightCol = new Table(skin);
		rightCol.add(interruptList).width(100).row();
		rightCol.add(blendCheckBox).left().row();
		rightCol.add("Action type:").padTop(10).left().row();
		rightCol.add(actionList).width(100).expandX().left().top();

		Table mainTable = new Table(skin);
		mainTable.setFillParent(true);
		mainTable.add("Start with:").left();
		mainTable.add("Interrupt with:").left().row();
		mainTable.add(scrollStarts).width(100).padRight(10).top();
		mainTable.add(rightCol).width(100).expandX().left().top().row();
		mainTable.add(durationLabel).left().colspan(2);
		stage.addActor(mainTable);
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
		imageActor.clearActions();
		imageActor.setPosition(width / 2, height / 2);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		durationLabel.setText("Duration: " + duration + "s. Ctrl + mouse wheel to adjust.");

		stage.act();
		stage.draw();
	}
}

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

package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Static convenience methods for using pooled actions, intended for static import.
 * @author Nathan Sweet */
public class Actions {
	/** Returns a new or pooled action of the specified type. */
	@NotNull
	static public <T extends Action> T action (@NotNull Class<T> type) {
		Pool<T> pool = Pools.get(type);
		T action = pool.obtain();
		action.setPool(pool);
		return action;
	}

	@NotNull
	static public AddAction addAction (@NotNull Action action) {
		AddAction addAction = action(AddAction.class);
		addAction.setAction(action);
		return addAction;
	}

	@NotNull
	static public AddAction addAction (@NotNull Action action, @NotNull Actor targetActor) {
		AddAction addAction = action(AddAction.class);
		addAction.setTarget(targetActor);
		addAction.setAction(action);
		return addAction;
	}

	@NotNull
	static public RemoveAction removeAction (@NotNull Action action) {
		RemoveAction removeAction = action(RemoveAction.class);
		removeAction.setAction(action);
		return removeAction;
	}

	@NotNull
	static public RemoveAction removeAction (@NotNull Action action, @NotNull Actor targetActor) {
		RemoveAction removeAction = action(RemoveAction.class);
		removeAction.setTarget(targetActor);
		removeAction.setAction(action);
		return removeAction;
	}

	/** Moves the actor instantly. */
	@NotNull
	static public MoveToAction moveTo (float x, float y) {
		return moveTo(x, y, 0, null);
	}

	@NotNull
	static public MoveToAction moveTo (float x, float y, float duration) {
		return moveTo(x, y, duration, null);
	}

	@NotNull
	static public MoveToAction moveTo (float x, float y, float duration, @Nullable Interpolation interpolation) {
		MoveToAction action = action(MoveToAction.class);
		action.setPosition(x, y);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	@NotNull
	static public MoveToAction moveToAligned (float x, float y, int alignment) {
		return moveToAligned(x, y, alignment, 0, null);
	}

	@NotNull
	static public MoveToAction moveToAligned (float x, float y, int alignment, float duration) {
		return moveToAligned(x, y, alignment, duration, null);
	}

	@NotNull
	static public MoveToAction moveToAligned (float x, float y, int alignment, float duration, @Nullable Interpolation interpolation) {
		MoveToAction action = action(MoveToAction.class);
		action.setPosition(x, y, alignment);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	/** Moves the actor instantly. */
	@NotNull
	static public MoveByAction moveBy (float amountX, float amountY) {
		return moveBy(amountX, amountY, 0, null);
	}

	@NotNull
	static public MoveByAction moveBy (float amountX, float amountY, float duration) {
		return moveBy(amountX, amountY, duration, null);
	}

	@NotNull
	static public MoveByAction moveBy (float amountX, float amountY, float duration, @Nullable Interpolation interpolation) {
		MoveByAction action = action(MoveByAction.class);
		action.setAmount(amountX, amountY);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	/** Sizes the actor instantly. */
	@NotNull
	static public SizeToAction sizeTo (float x, float y) {
		return sizeTo(x, y, 0, null);
	}

	@NotNull
	static public SizeToAction sizeTo (float x, float y, float duration) {
		return sizeTo(x, y, duration, null);
	}

	@NotNull
	static public SizeToAction sizeTo (float x, float y, float duration, @Nullable Interpolation interpolation) {
		SizeToAction action = action(SizeToAction.class);
		action.setSize(x, y);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	/** Sizes the actor instantly. */
	@NotNull
	static public SizeByAction sizeBy (float amountX, float amountY) {
		return sizeBy(amountX, amountY, 0, null);
	}

	@NotNull
	static public SizeByAction sizeBy (float amountX, float amountY, float duration) {
		return sizeBy(amountX, amountY, duration, null);
	}

	@NotNull
	static public SizeByAction sizeBy (float amountX, float amountY, float duration, @Nullable Interpolation interpolation) {
		SizeByAction action = action(SizeByAction.class);
		action.setAmount(amountX, amountY);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	/** Scales the actor instantly. */
	@NotNull
	static public ScaleToAction scaleTo (float x, float y) {
		return scaleTo(x, y, 0, null);
	}

	@NotNull
	static public ScaleToAction scaleTo (float x, float y, float duration) {
		return scaleTo(x, y, duration, null);
	}

	@NotNull
	static public ScaleToAction scaleTo (float x, float y, float duration, @Nullable Interpolation interpolation) {
		ScaleToAction action = action(ScaleToAction.class);
		action.setScale(x, y);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	/** Scales the actor instantly. */
	@NotNull
	static public ScaleByAction scaleBy (float amountX, float amountY) {
		return scaleBy(amountX, amountY, 0, null);
	}

	@NotNull
	static public ScaleByAction scaleBy (float amountX, float amountY, float duration) {
		return scaleBy(amountX, amountY, duration, null);
	}

	@NotNull
	static public ScaleByAction scaleBy (float amountX, float amountY, float duration, @Nullable Interpolation interpolation) {
		ScaleByAction action = action(ScaleByAction.class);
		action.setAmount(amountX, amountY);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	/** Rotates the actor instantly. */
	@NotNull
	static public RotateToAction rotateTo (float rotation) {
		return rotateTo(rotation, 0, null);
	}

	@NotNull
	static public RotateToAction rotateTo (float rotation, float duration) {
		return rotateTo(rotation, duration, null);
	}

	@NotNull
	static public RotateToAction rotateTo (float rotation, float duration, @Nullable Interpolation interpolation) {
		RotateToAction action = action(RotateToAction.class);
		action.setRotation(rotation);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	/** Rotates the actor instantly. */
	@NotNull
	static public RotateByAction rotateBy (float rotationAmount) {
		return rotateBy(rotationAmount, 0, null);
	}

	@NotNull
	static public RotateByAction rotateBy (float rotationAmount, float duration) {
		return rotateBy(rotationAmount, duration, null);
	}

	@NotNull
	static public RotateByAction rotateBy (float rotationAmount, float duration, @Nullable Interpolation interpolation) {
		RotateByAction action = action(RotateByAction.class);
		action.setAmount(rotationAmount);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	/** Sets the actor's color instantly. */
	@NotNull
	static public ColorAction color (@NotNull Color color) {
		return color(color, 0, null);
	}

	/** Transitions from the color at the time this action starts to the specified color. */
	@NotNull
	static public ColorAction color (@NotNull Color color, float duration) {
		return color(color, duration, null);
	}

	/** Transitions from the color at the time this action starts to the specified color. */
	@NotNull
	static public ColorAction color (@NotNull Color color, float duration, @Nullable Interpolation interpolation) {
		ColorAction action = action(ColorAction.class);
		action.setEndColor(color);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	/** Sets the actor's alpha instantly. */
	@NotNull
	static public AlphaAction alpha (float a) {
		return alpha(a, 0, null);
	}

	/** Transitions from the alpha at the time this action starts to the specified alpha. */
	@NotNull
	static public AlphaAction alpha (float a, float duration) {
		return alpha(a, duration, null);
	}

	/** Transitions from the alpha at the time this action starts to the specified alpha. */
	@NotNull
	static public AlphaAction alpha (float a, float duration, @Nullable Interpolation interpolation) {
		AlphaAction action = action(AlphaAction.class);
		action.setAlpha(a);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	/** Transitions from the alpha at the time this action starts to an alpha of 0. */
	@NotNull
	static public AlphaAction fadeOut (float duration) {
		return alpha(0, duration, null);
	}

	/** Transitions from the alpha at the time this action starts to an alpha of 0. */
	@NotNull
	static public AlphaAction fadeOut (float duration, @Nullable Interpolation interpolation) {
		AlphaAction action = action(AlphaAction.class);
		action.setAlpha(0);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	/** Transitions from the alpha at the time this action starts to an alpha of 1. */
	@NotNull
	static public AlphaAction fadeIn (float duration) {
		return alpha(1, duration, null);
	}

	/** Transitions from the alpha at the time this action starts to an alpha of 1. */
	@NotNull
	static public AlphaAction fadeIn (float duration, @Nullable Interpolation interpolation) {
		AlphaAction action = action(AlphaAction.class);
		action.setAlpha(1);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	@NotNull
	static public VisibleAction show () {
		return visible(true);
	}

	@NotNull
	static public VisibleAction hide () {
		return visible(false);
	}

	@NotNull
	static public VisibleAction visible (boolean visible) {
		VisibleAction action = action(VisibleAction.class);
		action.setVisible(visible);
		return action;
	}

	@NotNull
	static public TouchableAction touchable (@NotNull Touchable touchable) {
		TouchableAction action = action(TouchableAction.class);
		action.setTouchable(touchable);
		return action;
	}

	@NotNull
	static public RemoveActorAction removeActor () {
		return action(RemoveActorAction.class);
	}

	@NotNull
	static public RemoveActorAction removeActor (@NotNull Actor removeActor) {
		RemoveActorAction action = action(RemoveActorAction.class);
		action.setTarget(removeActor);
		return action;
	}

	@NotNull
	static public DelayAction delay (float duration) {
		DelayAction action = action(DelayAction.class);
		action.setDuration(duration);
		return action;
	}

	@NotNull
	static public DelayAction delay (float duration, @NotNull Action delayedAction) {
		DelayAction action = action(DelayAction.class);
		action.setDuration(duration);
		action.setAction(delayedAction);
		return action;
	}

	@NotNull
	static public TimeScaleAction timeScale (float scale, @NotNull Action scaledAction) {
		TimeScaleAction action = action(TimeScaleAction.class);
		action.setScale(scale);
		action.setAction(scaledAction);
		return action;
	}

	@NotNull
	static public SequenceAction sequence (@NotNull Action action1) {
		SequenceAction action = action(SequenceAction.class);
		action.addAction(action1);
		return action;
	}

	@NotNull
	static public SequenceAction sequence (@NotNull Action action1, @NotNull Action action2) {
		SequenceAction action = action(SequenceAction.class);
		action.addAction(action1);
		action.addAction(action2);
		return action;
	}

	@NotNull
	static public SequenceAction sequence (@NotNull Action action1, @NotNull Action action2, @NotNull Action action3) {
		SequenceAction action = action(SequenceAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		return action;
	}

	@NotNull
	static public SequenceAction sequence (@NotNull Action action1, @NotNull Action action2, @NotNull Action action3, @NotNull Action action4) {
		SequenceAction action = action(SequenceAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		action.addAction(action4);
		return action;
	}

	@NotNull
	static public SequenceAction sequence (@NotNull Action action1, @NotNull Action action2, @NotNull Action action3, @NotNull Action action4, @NotNull Action action5) {
		SequenceAction action = action(SequenceAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		action.addAction(action4);
		action.addAction(action5);
		return action;
	}

	@NotNull
	static public SequenceAction sequence (@NotNull Action... actions) {
		SequenceAction action = action(SequenceAction.class);
		for (int i = 0, n = actions.length; i < n; i++)
			action.addAction(actions[i]);
		return action;
	}

	@NotNull
	static public SequenceAction sequence () {
		return action(SequenceAction.class);
	}

	@NotNull
	static public ParallelAction parallel (@NotNull Action action1) {
		ParallelAction action = action(ParallelAction.class);
		action.addAction(action1);
		return action;
	}

	@NotNull
	static public ParallelAction parallel (@NotNull Action action1, @NotNull Action action2) {
		ParallelAction action = action(ParallelAction.class);
		action.addAction(action1);
		action.addAction(action2);
		return action;
	}

	@NotNull
	static public ParallelAction parallel (@NotNull Action action1, @NotNull Action action2, @NotNull Action action3) {
		ParallelAction action = action(ParallelAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		return action;
	}

	@NotNull
	static public ParallelAction parallel (@NotNull Action action1, @NotNull Action action2, @NotNull Action action3, @NotNull Action action4) {
		ParallelAction action = action(ParallelAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		action.addAction(action4);
		return action;
	}

	@NotNull
	static public ParallelAction parallel (@NotNull Action action1, @NotNull Action action2, @NotNull Action action3, @NotNull Action action4, @NotNull Action action5) {
		ParallelAction action = action(ParallelAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		action.addAction(action4);
		action.addAction(action5);
		return action;
	}

	@NotNull
	static public ParallelAction parallel (@NotNull Action... actions) {
		ParallelAction action = action(ParallelAction.class);
		for (int i = 0, n = actions.length; i < n; i++)
			action.addAction(actions[i]);
		return action;
	}

	@NotNull
	static public ParallelAction parallel () {
		return action(ParallelAction.class);
	}

	@NotNull
	static public RepeatAction repeat (int count, @NotNull Action repeatedAction) {
		RepeatAction action = action(RepeatAction.class);
		action.setCount(count);
		action.setAction(repeatedAction);
		return action;
	}

	@NotNull
	static public RepeatAction forever (@NotNull Action repeatedAction) {
		RepeatAction action = action(RepeatAction.class);
		action.setCount(RepeatAction.FOREVER);
		action.setAction(repeatedAction);
		return action;
	}

	@NotNull
	static public RunnableAction run (@NotNull Runnable runnable) {
		RunnableAction action = action(RunnableAction.class);
		action.setRunnable(runnable);
		return action;
	}

	@NotNull
	static public LayoutAction layout (boolean enabled) {
		LayoutAction action = action(LayoutAction.class);
		action.setLayoutEnabled(enabled);
		return action;
	}

	@NotNull
	static public AfterAction after (@NotNull Action action) {
		AfterAction afterAction = action(AfterAction.class);
		afterAction.setAction(action);
		return afterAction;
	}

	@NotNull
	static public AddListenerAction addListener (@NotNull EventListener listener, boolean capture) {
		AddListenerAction addAction = action(AddListenerAction.class);
		addAction.setListener(listener);
		addAction.setCapture(capture);
		return addAction;
	}

	@NotNull
	static public AddListenerAction addListener (@NotNull EventListener listener, boolean capture, @NotNull Actor targetActor) {
		AddListenerAction addAction = action(AddListenerAction.class);
		addAction.setTarget(targetActor);
		addAction.setListener(listener);
		addAction.setCapture(capture);
		return addAction;
	}

	@NotNull
	static public RemoveListenerAction removeListener (@NotNull EventListener listener, boolean capture) {
		RemoveListenerAction addAction = action(RemoveListenerAction.class);
		addAction.setListener(listener);
		addAction.setCapture(capture);
		return addAction;
	}

	@NotNull
	static public RemoveListenerAction removeListener (@NotNull EventListener listener, boolean capture, @NotNull Actor targetActor) {
		RemoveListenerAction addAction = action(RemoveListenerAction.class);
		addAction.setTarget(targetActor);
		addAction.setListener(listener);
		addAction.setCapture(capture);
		return addAction;
	}
}


package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.AlphaAction;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.actions.RemoveActorAction;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.actions.RotateByAction;
import com.badlogic.gdx.scenes.scene2d.actions.RotateToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleByAction;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleToAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.actions.SizeByAction;
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction;
import com.badlogic.gdx.scenes.scene2d.actions.TouchableAction;
import com.badlogic.gdx.scenes.scene2d.actions.VisibleAction;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/** Static convenience methods for using pooled actions, intended for static import.
 * @author Nathan Sweet */
public class Actions {
	/** Returns a new or pooled action of the specified type. */
	static public <T extends Action> T action (Class<T> type) {
		Pool<T> pool = Pools.get(type);
		T action = pool.obtain();
		action.setPool(pool);
		return action;
	}

	static public MoveToAction moveTo (float x, float y) {
		return moveTo(x, y, 0, null);
	}

	static public MoveToAction moveTo (float x, float y, float duration) {
		return moveTo(x, y, duration, null);
	}

	static public MoveToAction moveTo (float x, float y, float duration, Interpolation interpolation) {
		MoveToAction action = action(MoveToAction.class);
		action.setPosition(x, y);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public MoveByAction moveBy (float amountX, float amountY) {
		return moveBy(amountX, amountY, 0, null);
	}

	static public MoveByAction moveBy (float amountX, float amountY, float duration) {
		return moveBy(amountX, amountY, duration, null);
	}

	static public MoveByAction moveBy (float amountX, float amountY, float duration, Interpolation interpolation) {
		MoveByAction action = action(MoveByAction.class);
		action.setAmount(amountX, amountY);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public SizeToAction sizeTo (float x, float y) {
		return sizeTo(x, y, 0, null);
	}

	static public SizeToAction sizeTo (float x, float y, float duration) {
		return sizeTo(x, y, duration, null);
	}

	static public SizeToAction sizeTo (float x, float y, float duration, Interpolation interpolation) {
		SizeToAction action = action(SizeToAction.class);
		action.setSize(x, y);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public SizeByAction sizeBy (float amountX, float amountY) {
		return sizeBy(amountX, amountY, 0, null);
	}

	static public SizeByAction sizeBy (float amountX, float amountY, float duration) {
		return sizeBy(amountX, amountY, duration, null);
	}

	static public SizeByAction sizeBy (float amountX, float amountY, float duration, Interpolation interpolation) {
		SizeByAction action = action(SizeByAction.class);
		action.setAmount(amountX, amountY);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public ScaleToAction scaleTo (float x, float y) {
		return scaleTo(x, y, 0, null);
	}

	static public ScaleToAction scaleTo (float x, float y, float duration) {
		return scaleTo(x, y, duration, null);
	}

	static public ScaleToAction scaleTo (float x, float y, float duration, Interpolation interpolation) {
		ScaleToAction action = action(ScaleToAction.class);
		action.setScale(x, y);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public ScaleByAction scaleBy (float amountX, float amountY) {
		return scaleBy(amountX, amountY, 0, null);
	}

	static public ScaleByAction scaleBy (float amountX, float amountY, float duration) {
		return scaleBy(amountX, amountY, duration, null);
	}

	static public ScaleByAction scaleBy (float amountX, float amountY, float duration, Interpolation interpolation) {
		ScaleByAction action = action(ScaleByAction.class);
		action.setAmount(amountX, amountY);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public RotateToAction rotateTo (float rotation) {
		return rotateTo(rotation, 0, null);
	}

	static public RotateToAction rotateTo (float rotation, float duration) {
		return rotateTo(rotation, duration, null);
	}

	static public RotateToAction rotateTo (float rotation, float duration, Interpolation interpolation) {
		RotateToAction action = action(RotateToAction.class);
		action.setRotation(rotation);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public RotateByAction rotateBy (float rotationAmount) {
		return rotateBy(rotationAmount, 0, null);
	}

	static public RotateByAction rotateBy (float rotationAmount, float duration) {
		return rotateBy(rotationAmount, duration, null);
	}

	static public RotateByAction rotateBy (float rotationAmount, float duration, Interpolation interpolation) {
		RotateByAction action = action(RotateByAction.class);
		action.setAmount(rotationAmount);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public ColorAction color (Color color) {
		return color(color, 0, null);
	}

	static public ColorAction color (Color color, float duration) {
		return color(color, duration, null);
	}

	static public ColorAction color (Color color, float duration, Interpolation interpolation) {
		ColorAction action = action(ColorAction.class);
		action.setEndColor(color);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public AlphaAction alpha (float a) {
		return alpha(a, 0, null);
	}

	static public AlphaAction alpha (float a, float duration) {
		return alpha(a, duration, null);
	}

	static public AlphaAction alpha (float a, float duration, Interpolation interpolation) {
		AlphaAction action = action(AlphaAction.class);
		action.setAlpha(a);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public AlphaAction fadeOut (float duration) {
		return alpha(0, duration, null);
	}

	static public AlphaAction fadeOut (float duration, Interpolation interpolation) {
		AlphaAction action = action(AlphaAction.class);
		action.setAlpha(0);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public AlphaAction fadeIn (float duration) {
		return alpha(1, duration, null);
	}

	static public AlphaAction fadeIn (float duration, Interpolation interpolation) {
		AlphaAction action = action(AlphaAction.class);
		action.setAlpha(0);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	static public VisibleAction show () {
		return visible(true);
	}

	static public VisibleAction hide () {
		return visible(false);
	}

	static public VisibleAction visible (boolean visible) {
		VisibleAction action = action(VisibleAction.class);
		action.setVisible(visible);
		return action;
	}

	static public TouchableAction touchable (boolean touchable) {
		TouchableAction action = action(TouchableAction.class);
		action.setTouchable(touchable);
		return action;
	}

	static public RemoveActorAction removeActor () {
		return action(RemoveActorAction.class);
	}

	static public RemoveActorAction removeActor (Actor removeActor) {
		RemoveActorAction action = action(RemoveActorAction.class);
		action.setRemoveActor(removeActor);
		return action;
	}

	static public DelayAction delay (float duration) {
		DelayAction action = action(DelayAction.class);
		action.setDuration(duration);
		return action;
	}

	static public DelayAction delay (float duration, Action delayedAction) {
		DelayAction action = action(DelayAction.class);
		action.setDuration(duration);
		action.setAction(delayedAction);
		return action;
	}

	static public SequenceAction sequence (Action action1) {
		SequenceAction action = action(SequenceAction.class);
		action.addAction(action1);
		return action;
	}

	static public SequenceAction sequence (Action action1, Action action2) {
		SequenceAction action = action(SequenceAction.class);
		action.addAction(action1);
		action.addAction(action2);
		return action;
	}

	static public SequenceAction sequence (Action action1, Action action2, Action action3) {
		SequenceAction action = action(SequenceAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		return action;
	}

	static public SequenceAction sequence (Action action1, Action action2, Action action3, Action action4) {
		SequenceAction action = action(SequenceAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		action.addAction(action4);
		return action;
	}

	static public SequenceAction sequence (Action action1, Action action2, Action action3, Action action4, Action action5) {
		SequenceAction action = action(SequenceAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		action.addAction(action4);
		action.addAction(action5);
		return action;
	}

	static public SequenceAction sequence (Action... actions) {
		SequenceAction action = action(SequenceAction.class);
		for (int i = 0, n = actions.length; i < n; i++)
			action.addAction(actions[i]);
		return action;
	}

	static public ParallelAction parallel (Action action1) {
		ParallelAction action = action(ParallelAction.class);
		action.addAction(action1);
		return action;
	}

	static public ParallelAction parallel (Action action1, Action action2) {
		ParallelAction action = action(ParallelAction.class);
		action.addAction(action1);
		action.addAction(action2);
		return action;
	}

	static public ParallelAction parallel (Action action1, Action action2, Action action3) {
		ParallelAction action = action(ParallelAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		return action;
	}

	static public ParallelAction parallel (Action action1, Action action2, Action action3, Action action4) {
		ParallelAction action = action(ParallelAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		action.addAction(action4);
		return action;
	}

	static public ParallelAction parallel (Action action1, Action action2, Action action3, Action action4, Action action5) {
		ParallelAction action = action(ParallelAction.class);
		action.addAction(action1);
		action.addAction(action2);
		action.addAction(action3);
		action.addAction(action4);
		action.addAction(action5);
		return action;
	}

	static public ParallelAction parallel (Action... actions) {
		ParallelAction action = action(ParallelAction.class);
		for (int i = 0, n = actions.length; i < n; i++)
			action.addAction(actions[i]);
		return action;
	}

	static public RepeatAction repeat (int count, Action repeatedAction) {
		RepeatAction action = action(RepeatAction.class);
		action.setCount(count);
		action.setAction(repeatedAction);
		return action;
	}

	static public RepeatAction forever (Action repeatedAction) {
		RepeatAction action = action(RepeatAction.class);
		action.setCount(RepeatAction.FOREVER);
		action.setAction(repeatedAction);
		return action;
	}

	static public RunnableAction run (Runnable runnable) {
		RunnableAction action = action(RunnableAction.class);
		action.setRunnable(runnable);
		return action;
	}

	private Actions () {
	}
}

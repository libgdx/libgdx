package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class AnimationController extends BaseAnimationController {
	public interface AnimationListener {
		void onEnd(final AnimationDesc animation);
		void onLoop(final AnimationDesc animation);
	}
	public static class AnimationDesc {
		/** Listener which will be informed when the animation is looped or ended. */
		public AnimationListener listener;
		/** The animation to be applied. */
		public Animation animation;
		/** The speed at which to play the animation (can be negative), 1.0 for normal speed. */
		public float speed;
		/** The current animation time. */
		public float time;
		/** The number of remaining loops, negative for continuous, zero if stopped. */
		public int loopCount;
		/** @return the remaining time or 0 if still animating. */
		public float update(float delta) {
			if (loopCount != 0 && animation != null) {
				final float duration = animation.duration;
				final float diff = speed * delta; 
				time += diff;
				int loops = (int)Math.abs(time / duration);
				if (time < 0f) {
					loops++;
					while (time < 0f)
						time += duration;
				}
				time = Math.abs(time % duration);
				for (int i = 0; i < loops; i++) {
					if (loopCount > 0)
						loopCount--;
					if (listener != null)
						listener.onLoop(this);
					if (loopCount == 0) {
						final float result = ((loops - 1) - i) * duration + (diff < 0f ? duration - time : time); 
						time = (diff < 0f) ? duration : 0f;
						if (listener != null)
							listener.onEnd(this);
						return result;
					}
				}
				return 0f;
			} else
				return delta;
		}
	}
	protected final Pool<AnimationDesc> animationPool = new Pool<AnimationDesc>() {
		@Override
		protected AnimationDesc newObject() {
			return new AnimationDesc();
		}
	};
	
	public AnimationDesc current;
	public AnimationDesc queued;
	public float queuedTransitionTime;
	public AnimationDesc previous;
	public float transitionCurrentTime;
	public float transitionTargetTime;
	public boolean inAction;

	public AnimationController (ModelInstance target) {
		super(target);
	}
	
	private AnimationDesc obtain(final Animation anim, int loopCount, float speed, final AnimationListener listener) {
		final AnimationDesc result = animationPool.obtain();
		result.animation = anim;
		result.listener = listener;
		result.loopCount = loopCount;
		result.speed = speed;
		result.time = speed < 0 ? anim.duration : 0.f;
		return result;
	}
	
	private AnimationDesc obtain(final String id, int loopCount, float speed, final AnimationListener listener) {
		final Animation anim = target.getAnimation(id);
		if (anim == null)
			throw new GdxRuntimeException("Unknown animation: "+id);
		return obtain(anim, loopCount, speed, listener);
	}
	
	private AnimationDesc obtain(final AnimationDesc anim) {
		return obtain(anim.animation, anim.loopCount, anim.speed, anim.listener);
	}
	
	private boolean updating; //FIXME
	/** @param delta The time elapsed since last update, change this to alter the overall speed (can be negative). */
	public void update(float delta) {
		if (current == null || current.loopCount == 0 || current.animation == null)
			return;
		updating = true;
		final float remain = current.update(delta);
		if (remain != 0f && queued != null) {
			inAction = false;
			animate(queued, queuedTransitionTime);
			queued = null;
			updating = false;
			update(remain);
			return;
		}
		if (previous != null && ((transitionCurrentTime += delta) >= transitionTargetTime)) {
			animationPool.free(previous);
			previous = null;
		}
		if (previous != null)
			applyAnimations(previous.animation, previous.time, current.animation, current.time, transitionCurrentTime / transitionTargetTime);
		else
			applyAnimation(current.animation, current.time);
		updating = false;
	}
	
	/** Set the active animation, replacing any current animation. */
	public void setAnimation(final String id, int loopCount, float speed, final AnimationListener listener) {
		setAnimation(obtain(id, loopCount, speed, listener));
	}
	
	/** Set the active animation, replacing any current animation. */
	protected void setAnimation(final Animation anim, int loopCount, float speed, final AnimationListener listener) {
		setAnimation(obtain(anim, loopCount, speed, listener));
	}
	
	/** Set the active animation, replacing any current animation. */
	protected void setAnimation(final AnimationDesc anim) {
		if (updating) // FIXME Remove this? Just intended for debugging
			throw new GdxRuntimeException("Cannot change animation during update");
		if (current == null)
			current = anim;
		else {
			if (current.animation == anim.animation)
				anim.time = current.time;
			animationPool.free(current);
			current = anim;
		}
	}
	
	/** Changes the current animation by blending the new on top of the old during the transition time. */
	public void animate(final String id, int loopCount, float speed, final AnimationListener listener, float transitionTime) {
		animate(obtain(id, loopCount, speed, listener), transitionTime);
	}
	
	/** Changes the current animation by blending the new on top of the old during the transition time. */
	protected void animate(final Animation anim, int loopCount, float speed, final AnimationListener listener, float transitionTime) {
		animate(obtain(anim, loopCount, speed, listener), transitionTime);
	}
	
	/** Changes the current animation by blending the new on top of the old during the transition time. */ 
	protected void animate(final AnimationDesc anim, float transitionTime) {
		if (current == null)
			current = anim;
		else if (inAction)
			queue(anim, transitionTime);
		else if (current.animation == anim.animation) {
			anim.time = current.time;
			animationPool.free(current);
			current = anim;
		} else {
			if (previous != null)
				animationPool.free(previous);
			previous = current;
			current = anim;
			transitionCurrentTime = 0f;
			transitionTargetTime = transitionTime;
		}
	}
	
	/** Queue an animation to be applied when the current is finished. If current is continuous it will be synced on next loop. */
	public void queue(final String id, int loopCount, float speed, final AnimationListener listener, float transitionTime) {
		queue(obtain(id, loopCount, speed, listener), transitionTime);	
	}
	
	/** Queue an animation to be applied when the current is finished. If current is continuous it will be synced on next loop. */
	protected void queue(final Animation anim, int loopCount, float speed, final AnimationListener listener, float transitionTime) {
		queue(obtain(anim, loopCount, speed, listener), transitionTime);
	}
	
	/** Queue an animation to be applied when the current is finished. If current is continuous it will be synced on next loop. */
	protected void queue(final AnimationDesc anim, float transitionTime) {
		if (current == null || current.loopCount == 0)
			animate(anim, transitionTime);
		else {
			if (queued != null)
				animationPool.free(queued);
			queued = anim;
			queuedTransitionTime = transitionTime;
			if (current.loopCount < 0)
				current.loopCount = 1;
		}
	}
	
	/** Apply an action animation on top of the current animation. */
	public void action(final String id, int loopCount, float speed, final AnimationListener listener, float transitionTime) {
		action(obtain(id, loopCount, speed, listener), transitionTime);	
	}
	
	/** Apply an action animation on top of the current animation. */
	protected void action(final Animation anim, int loopCount, float speed, final AnimationListener listener, float transitionTime) {
		action(obtain(anim, loopCount, speed, listener), transitionTime);
	}
	
	/** Apply an action animation on top of the current animation. */
	protected void action(final AnimationDesc anim, float transitionTime) {
		if (anim.loopCount < 0)
			throw new GdxRuntimeException("An action cannot be continuous");
		if (current == null || current.loopCount == 0)
			animate(anim, transitionTime);
		else {
			AnimationDesc toQueue = inAction ? null : obtain(current);
			inAction = false;
			animate(anim, transitionTime);
			inAction = true;
			if (toQueue != null)
				queue(toQueue, transitionTime);
		}
	}
}

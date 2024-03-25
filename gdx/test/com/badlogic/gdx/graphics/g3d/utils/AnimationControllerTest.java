
package com.badlogic.gdx.graphics.g3d.utils;

import org.junit.Assert;
import org.junit.Test;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.utils.Array;

public class AnimationControllerTest {

	@Test
	public void testGetFirstKeyframeIndexAtTimeNominal () {

		Array<NodeKeyframe<String>> keyFrames = new Array<NodeKeyframe<String>>();

		keyFrames.add(new NodeKeyframe<String>(0f, "1st"));
		keyFrames.add(new NodeKeyframe<String>(3f, "2nd"));
		keyFrames.add(new NodeKeyframe<String>(12f, "3rd"));
		keyFrames.add(new NodeKeyframe<String>(13f, "4th"));

		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, -1f));
		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 0f));
		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 2f));
		Assert.assertEquals(1, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 9f));
		Assert.assertEquals(2, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 12.5f));
		Assert.assertEquals(2, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 13f));
		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 14f));
	}

	@Test
	public void testGetFirstKeyframeIndexAtTimeSingleKey () {

		Array<NodeKeyframe<String>> keyFrames = new Array<NodeKeyframe<String>>();

		keyFrames.add(new NodeKeyframe<String>(10f, "1st"));

		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 9f));
		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 10f));
		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 11f));
	}

	@Test
	public void testGetFirstKeyframeIndexAtTimeEmpty () {

		Array<NodeKeyframe<String>> keyFrames = new Array<NodeKeyframe<String>>();

		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 3f));
	}

	private static void assertSameAnimation (Animation expected, AnimationDesc actual) {
		if (!expected.id.equals(actual.animation.id)) {
			Assert.fail("expected: " + expected.id + ", actual: " + actual.animation.id);
		}
	}

	@Test
	public void testEndUpActionAtDurationTime () {

		Animation loop = new Animation();
		loop.id = "loop";
		loop.duration = 1f;

		Animation action = new Animation();
		action.id = "action";
		action.duration = 0.2f;

		ModelInstance modelInstance = new ModelInstance(new Model());
		modelInstance.animations.add(loop);
		modelInstance.animations.add(action);

		AnimationController animationController = new AnimationController(modelInstance);

		animationController.setAnimation("loop", -1);
		assertSameAnimation(loop, animationController.current);

		animationController.update(1);
		assertSameAnimation(loop, animationController.current);

		animationController.update(0.01f);
		assertSameAnimation(loop, animationController.current);

		animationController.action("action", 1, 1f, null, 0f);
		assertSameAnimation(action, animationController.current);

		animationController.update(0.2f);
		assertSameAnimation(loop, animationController.current);
	}

	@Test
	public void testEndUpActionAtDurationTimeReverse () {

		Animation loop = new Animation();
		loop.id = "loop";
		loop.duration = 1f;

		Animation action = new Animation();
		action.id = "action";
		action.duration = 0.2f;

		ModelInstance modelInstance = new ModelInstance(new Model());
		modelInstance.animations.add(loop);
		modelInstance.animations.add(action);

		AnimationController animationController = new AnimationController(modelInstance);

		animationController.setAnimation("loop", -1, -1f, null);
		assertSameAnimation(loop, animationController.current);

		animationController.update(1);
		assertSameAnimation(loop, animationController.current);

		animationController.update(0.01f);
		assertSameAnimation(loop, animationController.current);

		animationController.action("action", 1, -1f, null, 0f);
		assertSameAnimation(action, animationController.current);

		animationController.update(0.2f);
		assertSameAnimation(loop, animationController.current);
	}
}

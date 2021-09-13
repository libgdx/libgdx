
package com.badlogic.gdx.graphics.g3d.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;

public class AnimationDescTest {
	private static final float epsilon = 1e-6f;
	private AnimationDesc anim;

	@Before
	public void setup () {
		anim = new AnimationDesc();
		anim.animation = new Animation();
		anim.duration = 1f;
		anim.listener = null;
		anim.loopCount = 1;
		anim.offset = 0f;
		anim.speed = 1f;
		anim.time = 0f;
	}

	@Test
	public void testUpdateNominal () {
		Assert.assertEquals(-1, anim.update(.75f), epsilon);
		Assert.assertEquals(.5f, anim.update(.75f), epsilon);
		Assert.assertEquals(.75f, anim.update(.75f), epsilon);
	}

	@Test
	public void testUpdateJustEnd () {
		Assert.assertEquals(-1, anim.update(.5f), epsilon);
		Assert.assertEquals(0, anim.update(.5f), epsilon);
		Assert.assertEquals(.5f, anim.update(.5f), epsilon);
	}

	@Test
	public void testUpdateBigDelta () {
		Assert.assertEquals(4.2f, anim.update(5.2f), epsilon);
		Assert.assertEquals(7.3f, anim.update(7.3f), epsilon);
	}

	@Test
	public void testUpdateZeroDelta () {
		Assert.assertEquals(-1, anim.update(0f), epsilon);
		Assert.assertEquals(0f, anim.time, epsilon);
	}

	@Test
	public void testUpdateReverseNominal () {
		anim.speed = -1;
		anim.time = anim.duration;

		Assert.assertEquals(-1, anim.update(.75f), epsilon);
		Assert.assertEquals(.5f, anim.update(.75f), epsilon);
		Assert.assertEquals(.75f, anim.update(.75f), epsilon);
	}

	@Test
	public void testUpdateReverseJustEnd () {
		anim.speed = -1;
		anim.time = anim.duration;

		Assert.assertEquals(-1, anim.update(.5f), epsilon);
		Assert.assertEquals(0, anim.update(.5f), epsilon);
		Assert.assertEquals(.5f, anim.update(.5f), epsilon);
	}

	@Test
	public void testUpdateReverseBigDelta () {
		anim.speed = -1;
		anim.time = anim.duration;

		Assert.assertEquals(4.2f, anim.update(5.2f), epsilon);
		Assert.assertEquals(7.3f, anim.update(7.3f), epsilon);
	}

	@Test
	public void testUpdateReverseZeroDelta () {
		anim.speed = -1;
		anim.time = anim.duration;

		Assert.assertEquals(-1, anim.update(0f), epsilon);
		Assert.assertEquals(anim.duration, anim.time, epsilon);
	}
}

package com.badlogic.gdx.graphics.g3d.utils;

import org.junit.Assert;
import org.junit.Test;

import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.utils.Array;

public class AnimationControllerTest {

	@Test
	public void testGetFirstKeyframeIndexAtTimeNominal(){
		
		Array<NodeKeyframe<String>> keyFrames = new Array<NodeKeyframe<String>>();
		
		keyFrames.add(new NodeKeyframe<String>(0f,  "1st"));
		keyFrames.add(new NodeKeyframe<String>(3f,  "2nd"));
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
	public void testGetFirstKeyframeIndexAtTimeSingleKey(){
		
		Array<NodeKeyframe<String>> keyFrames = new Array<NodeKeyframe<String>>();
		
		keyFrames.add(new NodeKeyframe<String>(10f,  "1st"));
		
		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 9f));
		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 10f));
		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 11f));
	}
	
	@Test
	public void testGetFirstKeyframeIndexAtTimeEmpty(){
		
		Array<NodeKeyframe<String>> keyFrames = new Array<NodeKeyframe<String>>();
		
		Assert.assertEquals(0, BaseAnimationController.getFirstKeyframeIndexAtTime(keyFrames, 3f));
	}
}

package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Android implementation of the VideoPlayerCreator class.
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 *
 */
public class VideoPlayerCreatorAndroid
extends VideoPlayerCreator {

	@Override
	public VideoPlayer create() {
		return new VideoPlayerAndroid();
	}

	@Override
	public VideoPlayer create(Viewport viewport) {
		return new VideoPlayerAndroid(viewport);
	}

	@Override
	public VideoPlayer create(Camera cam, Mesh mesh) {
		return new VideoPlayerAndroid(cam, mesh);
	}
}

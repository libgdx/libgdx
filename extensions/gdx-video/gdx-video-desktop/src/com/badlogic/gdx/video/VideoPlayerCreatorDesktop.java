package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;

/**
 * Desktop implementation of the VideoPlayerCreator
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 *
 */
public class VideoPlayerCreatorDesktop
extends VideoPlayerCreator {

	@Override
	public VideoPlayer create() {
		return new VideoPlayerDesktop();
	}

	@Override
	public VideoPlayer create(Camera cam, float x, float y, float width, float height) {
		return new VideoPlayerDesktop(cam, x, y, width, height);
	}

	@Override
	public VideoPlayer create(Camera cam, Mesh mesh) {
		return new VideoPlayerDesktop(cam, mesh);
	}
}

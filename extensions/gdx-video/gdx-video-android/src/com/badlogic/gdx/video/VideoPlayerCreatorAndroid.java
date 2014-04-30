package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

public class VideoPlayerCreatorAndroid
extends VideoPlayerCreator {

	@Override
	public VideoPlayer create() {
		return new VideoPlayerAndroid();
	}

	@Override
	public VideoPlayer create(Camera cam, float x, float y, float width, float height) {
		return new VideoPlayerAndroid(cam, x, y, width, height);
	}

	@Override
	public VideoPlayer create(Camera cam, Mesh mesh) {
		return new VideoPlayerAndroid(cam, mesh);
	}
}

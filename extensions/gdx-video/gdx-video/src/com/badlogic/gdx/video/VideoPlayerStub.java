package com.badlogic.gdx.video;

import java.io.FileNotFoundException;

import com.badlogic.gdx.files.FileHandle;

public class VideoPlayerStub
implements VideoPlayer {

	@Override
	public boolean play(FileHandle file) throws FileNotFoundException {
		return true;
	}

	@Override
	public boolean render() {
		return false;
	}

	@Override
	public boolean isBuffered() {
		return true;
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void setOnVideoSizeListener(VideoSizeListener listener) {
	}

	@Override
	public void setOnCompletionListener(CompletionListener listener) {
	}

	@Override
	public int getVideoWidth() {
		return 0;
	}

	@Override
	public int getVideoHeight() {
		return 0;
	}

	@Override
	public boolean isPlaying() {
		return false;
	}

	@Override
	public void dispose() {
	}

}

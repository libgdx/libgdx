package com.badlogic.gdx.video;

import java.io.FileNotFoundException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;

/**
 * The VideoPlayer will play a video.
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 *
 */
public interface VideoPlayer {
	/**
	 * This function will prepare the VideoPlayer to play the given file. If a video is already played, it will be
	 * stopped, and the new video will be loaded.
	 *
	 * @param file
	 *            The file containing the video which should be played.
	 * @return Whether loading the file was successful.
	 */
	boolean play(FileHandle file) throws FileNotFoundException;

	/**
	 * This function needs to be called every frame, so that the player can update all the buffers.
	 *
	 * @return It returns true if a new frame is being displayed, false if none available (file is finished playing).
	 */
	boolean render();

	/**
	 * Whether the buffer containing the video is completely filled.
	 *
	 * @return buffer completely filled or not.
	 */
	boolean isBuffered();

	/**
	 * Method that resizes the video. It also accepts a new OrthographicCamera and SpriteBatch, which can be left out.
	 *
	 * @param cam
	 *            The camera to use, can be null
	 * @param x
	 *            The x value that should be used when drawing with the given (or default) camera or spritebatch.
	 * @param y
	 *            The y value that should be used when drawing with the given (or default) camera or spritebatch.
	 * @param width
	 *            The width of the video to display
	 * @param height
	 *            The height of the video to display
	 */
	void resize(Camera cam, float x, float y, float width, float height);

	/**
	 * This pauses the video, and should be called when the app is paused, to prevent the video from playing while being
	 * swapped away.
	 */
	void pause();

	/**
	 * This resumes the video after it is paused.
	 */
	void resume();

	/**
	 * This will stop playing the file, and will clear all buffers. Use this before deleting the reference
	 */
	void stop();
}

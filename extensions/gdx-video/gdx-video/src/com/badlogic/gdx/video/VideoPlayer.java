package com.badlogic.gdx.video;

import java.io.FileNotFoundException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Disposable;

/**
 * The VideoPlayer will play a video on any given mesh, using textures. It can be reused, but can only play one video at
 * the time.
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 *
 */
public interface VideoPlayer
		extends Disposable {
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
	 * Normal usecase is to start rendering after {@link isBuffered()} returns true.
	 *
	 * @return It returns true if a new frame is being displayed, false if none available (file is finished playing).
	 */
	boolean render();

	/**
	 * Whether the buffer containing the video is completely filled.
	 * The size of the buffer is platform specific, and cannot necessarily be depended upon.
	 * Review the documentation per platform for specifics.
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
	 * This will stop playing the file, and implicitely clears all buffers and invalidate resources used.
	 */
	void stop();

	/**
	 * Disposes the VideoPlayer and ensures all buffers and resources are invalidated and disposed.
	 */
	@Override
	void dispose();

}

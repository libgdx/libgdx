package com.badlogic.gdx.video;

import java.io.FileNotFoundException;

import com.badlogic.gdx.files.FileHandle;
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
	public interface VideoSizeListener {
		public void onVideoSize(int width, int height);
	}

	public interface CompletionListener {
		public void onCompletionListener(FileHandle file);
	}

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
	 * Resize the videoplayer. This function will NOT work when working with custom meshes. It will set the world
	 * width and height of the (interal) viewport.
	 *
	 * @param width
	 *            The width of the screen
	 * @param height
	 *            The height of the screen
	 */
	void resize(float width, float height);

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
	 * This will set a listener for whenever the video size of a file is known (after calling play). This is needed
	 * since the size of the video is not directly known after using the play method.
	 *
	 * @param listener
	 *            The listener to set
	 */
	void setOnVideoSizeListener(VideoSizeListener listener);

	/**
	 * This will set a listener for when the video is done playing. The listener will be called every time a video is
	 * done playing.
	 *
	 * @param listener
	 *            The listener to set
	 */
	void setOnCompletionListener(CompletionListener listener);

	/**
	 * This will return the width of the currently playing video. This function cannot be called when {@link
	 * isBuffered()} returns false.
	 *
	 * @return the width of the video
	 */
	int getVideoWidth();

	/**
	 * This will return the height of the currently playing video. This function cannot be called when {@link
	 * isBuffered()} returns false.
	 *
	 * @return the height of the video
	 */
	int getVideoHeight();

	/**
	 * Whether the video is playing or not.
	 * 
	 * @return whether the video is still playing
	 */
	boolean isPlaying();

	/**
	 * Disposes the VideoPlayer and ensures all buffers and resources are invalidated and disposed.
	 */
	@Override
	void dispose();

}

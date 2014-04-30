package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * This class is used to provide a way of creating a VideoPlayer, without knowing the platform the program is running
 * on. This has to be extended for each supported platform.
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 *
 */
public abstract class VideoPlayerCreator {
	public static VideoPlayerCreator creator;

	/**
	 * Creates a VideoPlayer with default rendering parameters. It will use a FitViewport which uses the video size as
	 * world height.
	 *
	 * @return A new instance of VideoPlayer
	 */
	public abstract VideoPlayer create();

	/**
	 * Creates a VideoPlayer with the given viewport. The video's dimensions will be used to set the world size on this
	 * viewport. When using the resize method, the update method with the new size will be called. This however is not
	 * needed if the viewport is updated on some other place.
	 *
	 * @param viewport
	 *            The viewport to use
	 * @return A new instance of VideoPlayer
	 */
	public abstract VideoPlayer create(Viewport viewport);

	/**
	 * Creates a VideoPlayer with a custom Camera and mesh. When using this, the resize method of VideoPlayer will not
	 * work, and the responsibility of resizing is for the developer when using this.
	 *
	 * @param cam
	 *            The camera that should be used during rendering.
	 * @param mesh
	 *            A mesh used to draw the texture on.
	 * @return A new instance of VideoPlayer
	 */
	public abstract VideoPlayer create(Camera cam, Mesh mesh);
}

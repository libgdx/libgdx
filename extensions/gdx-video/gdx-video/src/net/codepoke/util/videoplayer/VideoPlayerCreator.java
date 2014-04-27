package net.codepoke.util.videoplayer;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;

/**
 * This class is used to provide a way of creating a VideoPlayer, without knowing the platform the program is running
 * on. This has to be extended for each supported platform.
 *
 * @author Rob Bogie <bogie.rob@gmail.com>
 *
 */
public abstract class VideoPlayerCreator {
	public static VideoPlayerCreator creator;

	/**
	 * Creates a VideoPlayer with default rendering parameters
	 *
	 * @return A new instance of VideoPlayer
	 */
	public abstract VideoPlayer create();

	/**
	 * Creates a VideoPlayer with the given rendering parameters
	 *
	 * @param cam
	 *            The camera that should be used during rendering.
	 * @param x
	 *            The x coordinate to start drawing on
	 * @param y
	 *            The y coordinate to start drawing on
	 * @param width
	 *            The width of drawing
	 * @param height
	 *            The height of drawing
	 * @return A new instance of VideoPlayer
	 */
	public abstract VideoPlayer create(Camera cam, float x, float y, float width, float height);

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

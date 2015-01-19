/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.video;

import java.lang.reflect.InvocationTargetException;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * This class is used to provide a way of creating a VideoPlayer, without knowing the platform the program is running
 * on. This has to be extended for each supported platform.
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 *
 */
public class VideoPlayerCreator {
	private static Class<? extends VideoPlayer> videoPlayerClass;

	/**
	 * Creates a VideoPlayer with default rendering parameters. It will use a FitViewport which uses the video size as
	 * world height.
	 *
	 * @return A new instance of VideoPlayer
	 */
	public static VideoPlayer createVideoPlayer() {
		initialize();
		if (videoPlayerClass == null)
			return new VideoPlayerStub();
		try {
			return videoPlayerClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a VideoPlayer with the given viewport. The video's dimensions will be used to set the world size on this
	 * viewport. When using the resize method, the update method with the new size will be called. This however is not
	 * needed if the viewport is updated on some other place.
	 *
	 * @param viewport
	 *            The viewport to use
	 * @return A new instance of VideoPlayer
	 */
	public static VideoPlayer createVideoPlayer(Viewport viewport) {
		initialize();
		if (videoPlayerClass == null)
			return new VideoPlayerStub();

		try {
			return videoPlayerClass.getConstructor(Viewport.class)
									.newInstance(viewport);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

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
	public static VideoPlayer createVideoPlayer(Camera cam, Mesh mesh, int primitiveType) {
		initialize();
		if (videoPlayerClass == null)
			return new VideoPlayerStub();

		try {
			return videoPlayerClass.getConstructor(Camera.class, Mesh.class, Integer.TYPE)
									.newInstance(cam, mesh, primitiveType);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void initialize() {
		if (videoPlayerClass != null)
			return;

		String className = null;
		ApplicationType type = Gdx.app.getType();

		if (type == ApplicationType.Android) {
			if (Gdx.app.getVersion() >= 12) {
				className = "com.badlogic.gdx.video.VideoPlayerAndroid";
			} else {
				Gdx.app.log("Gdx-Video", "VideoPlayer can't be used on android < API level 12");
			}
		} else if (type == ApplicationType.Desktop) {
			className = "com.badlogic.gdx.video.VideoPlayerDesktop";
		} else {
			Gdx.app.log("Gdx-Video", "Platform is not supported by the Gdx Video Extension");
		}

		try {
			videoPlayerClass = ClassReflection.forName(className);
		} catch (ReflectionException e) {
			e.printStackTrace();
		}
	}
}

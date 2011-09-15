/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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
package com.badlogic.gdx.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

public class GestureDetector extends InputAdapter {
	public static interface GestureListener {
		public boolean touchDown (int x, int y, int pointer);

		public boolean tap (int x, int y, int count);

		public boolean longPress (int x, int y);

		public boolean fling (float velocityX, float velocityY);

		public boolean pan (int x, int y, int deltaX, int deltaY);

		public boolean zoom (float originalDistance, float currentDistance);
	}

	static class VelocityTracker {
		int sampleSize = 10;
		int lastX;
		int lastY;
		int deltaX;
		int deltaY;
		long lastTime;
		int numSamples;
		float[] meanX = new float[sampleSize];
		float[] meanY = new float[sampleSize];
		long[] meanTime = new long[sampleSize];

		public void start (int x, int y, long timeStamp) {
			lastX = x;
			lastY = y;
			deltaX = 0;
			deltaY = 0;
			numSamples = 0;
			for (int i = 0; i < sampleSize; i++) {
				meanX[i] = 0;
				meanY[i] = 0;
				meanTime[i] = 0;
			}
			lastTime = timeStamp;
		}

		public void update (int x, int y, long timeStamp) {
			long currTime = timeStamp;
			deltaX = (x - lastX);
			deltaY = (y - lastY);
			lastX = x;
			lastY = y;
			long deltaTime = currTime - lastTime;
			lastTime = currTime;
			int index = numSamples % sampleSize;
			meanX[index] = deltaX;
			meanY[index] = deltaY;
			meanTime[index] = deltaTime;
			numSamples++;
		}

		public float getVelocityX () {
			float meanX = getAverage(this.meanX, numSamples);
			float meanTime = getAverage(this.meanTime, numSamples) / 1000000000.0f;
			if (meanTime == 0) return 0;
			return meanX / meanTime;
		}

		public float getVelocityY () {
			float meanY = getAverage(this.meanY, numSamples);
			float meanTime = getAverage(this.meanTime, numSamples) / 1000000000.0f;
			if (meanTime == 0) return 0;
			return meanY / meanTime;
		}

		private float getAverage (float[] values, int numSamples) {
			numSamples = Math.min(sampleSize, numSamples);
			float sum = 0;
			for (int i = 0; i < numSamples; i++) {
				sum += values[i];
			}
			return sum / numSamples;
		}

		private long getAverage (long[] values, int numSamples) {
			numSamples = Math.min(sampleSize, numSamples);
			long sum = 0;
			for (int i = 0; i < numSamples; i++) {
				sum += values[i];
			}
			if (numSamples == 0) return 0;
			return sum / numSamples;
		}

		private float getSum (float[] values, int numSamples) {
			numSamples = Math.min(sampleSize, numSamples);
			float sum = 0;
			for (int i = 0; i < numSamples; i++) {
				sum += values[i];
			}
			if (numSamples == 0) return 0;
			return sum;
		}
	}

	private final int tapSquareSize;
	private final long tapCountInterval;
	private final long longPressDuration;
	private long maxFlingDelay;
	private boolean inTapSquare;
	private int tapCount;
	private long lastTapTime;
	private boolean longPressFired;
	private boolean pinching;
	private boolean panning;

	private final VelocityTracker tracker = new VelocityTracker();
	private int tapSquareCenterX;
	private int tapSquareCenterY;
	private long gestureStartTime;
	private Vector2 firstPointer = new Vector2();
	private Vector2 secondPointer = new Vector2();
	private float initialDistance;

	private final GestureListener listener;

	public GestureDetector (GestureListener listener) {
		this(20, 0.4f, 1.5f, 0.15f, listener);
	}

	public GestureDetector (int halfTapSquareSize, float tapCountInterval, float longPressDuration, float maxFlingDelay,
		GestureListener listener) {
		this.tapSquareSize = halfTapSquareSize;
		this.tapCountInterval = (long)(tapCountInterval * 1000000000l);
		this.longPressDuration = (long)(longPressDuration * 1000000000l);
		this.maxFlingDelay = (long)(maxFlingDelay * 1000000000l);
		this.listener = listener;
	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		if (pointer > 1) return false;

		if (pointer == 0) {
			firstPointer.set(x, y);
			inTapSquare = true;
			pinching = false;
			longPressFired = false;
			tapSquareCenterX = x;
			tapSquareCenterY = y;
			gestureStartTime = Gdx.input.getCurrentEventTime();
			tracker.start(x, y, gestureStartTime);
		} else {
			secondPointer.set(x, y);
			inTapSquare = false;
			pinching = true;
			initialDistance = firstPointer.dst(secondPointer);
		}
		return listener.touchDown(x, y, pointer);
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		if (pointer > 1) return false;

		// handle pinch zoom
		if (pinching) {
			if (pointer == 0)
				firstPointer.set(x, y);
			else
				secondPointer.set(x, y);
			if (listener != null) return listener.zoom(initialDistance, firstPointer.dst(secondPointer));
			return false;
		}

		// update tracker
		tracker.update(x, y, Gdx.input.getCurrentEventTime());

		// check if we are still tapping.
		if (!(inTapSquare && Math.abs(x - tapSquareCenterX) < tapSquareSize && Math.abs(y - tapSquareCenterY) < tapSquareSize)) {
			inTapSquare = false;
		}

		if (!inTapSquare) {
			// handle scroll
			inTapSquare = false;
			panning = true;
			return listener.pan(tracker.lastX, tracker.lastY, tracker.deltaX, tracker.deltaY);
		} else {
			// handle longpress
			if (!longPressFired && Gdx.input.getCurrentEventTime() - gestureStartTime > longPressDuration) {
				longPressFired = true;
				return listener.longPress(x, y);
			}
		}

		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		if (pointer > 1) return false;

		panning = false;
		if (inTapSquare & !longPressFired) {
			// handle taps
			if (System.nanoTime() - lastTapTime > tapCountInterval) tapCount = 0;
			tapCount++;
			lastTapTime = System.nanoTime();
			return listener.tap(tapSquareCenterX, tapSquareCenterY, tapCount);
		} else if (pinching) {
			// handle pinch end
			pinching = false;
			// we are basically in pan/scroll mode again, reset velocity tracker
			if (pointer == 0)	{
			   // first pointer has lifted off, set up panning to use the second pointer...
			   tracker.start((int)secondPointer.x, (int)secondPointer.y, Gdx.input.getCurrentEventTime());
			} else {
			   // second pointer has lifted off, set up panning to use the first pointer...
			   tracker.start((int)firstPointer.x, (int)firstPointer.y, Gdx.input.getCurrentEventTime());
			}
		} else {
			// handle fling
			long time = Gdx.input.getCurrentEventTime();
			if (time - tracker.lastTime < maxFlingDelay) {
				tracker.update(x, y, time);
				return listener.fling(tracker.getVelocityX(), tracker.getVelocityY());
			}
		}
		return false;
	}
	
	public boolean isPanning () {
		return panning;
	}
}
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

package com.badlogic.gdx;

import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.TimeUtils;

/** Queues events that are later passed to the wrapped {@link InputProcessor}.
 * @author Nathan Sweet */
public class InputEventQueue implements InputProcessor {
	static private final int KEY_DOWN = 0;
	static private final int KEY_UP = 1;
	static private final int KEY_TYPED = 2;
	static private final int TOUCH_DOWN = 3;
	static private final int TOUCH_UP = 4;
	static private final int TOUCH_DRAGGED = 5;
	static private final int MOUSE_MOVED = 6;
	static private final int SCROLLED = 7;

	private InputProcessor processor;
	private final IntArray queue = new IntArray();
	private final IntArray processingQueue = new IntArray();
	private long currentEventTime;

	public InputEventQueue () {
	}

	public InputEventQueue (InputProcessor processor) {
		this.processor = processor;
	}

	public void setProcessor (InputProcessor processor) {
		this.processor = processor;
	}

	public InputProcessor getProcessor () {
		return processor;
	}

	public void drain () {
		IntArray q = processingQueue;
		synchronized (this) {
			if (processor == null) {
				queue.clear();
				return;
			}
			q.addAll(queue);
			queue.clear();
		}
		InputProcessor localProcessor = processor;
		for (int i = 0, n = q.size; i < n;) {
			currentEventTime = (long)q.get(i++) << 32 | q.get(i++) & 0xFFFFFFFFL;
			switch (q.get(i++)) {
			case KEY_DOWN:
				localProcessor.keyDown(q.get(i++));
				break;
			case KEY_UP:
				localProcessor.keyUp(q.get(i++));
				break;
			case KEY_TYPED:
				localProcessor.keyTyped((char)q.get(i++));
				break;
			case TOUCH_DOWN:
				localProcessor.touchDown(q.get(i++), q.get(i++), q.get(i++), q.get(i++));
				break;
			case TOUCH_UP:
				localProcessor.touchUp(q.get(i++), q.get(i++), q.get(i++), q.get(i++));
				break;
			case TOUCH_DRAGGED:
				localProcessor.touchDragged(q.get(i++), q.get(i++), q.get(i++));
				break;
			case MOUSE_MOVED:
				localProcessor.mouseMoved(q.get(i++), q.get(i++));
				break;
			case SCROLLED:
				localProcessor.scrolled(q.get(i++));
				break;
			}
		}
		q.clear();
	}

	private void queueTime () {
		long time = TimeUtils.nanoTime();
		queue.add((int)(time >> 32));
		queue.add((int)time);
	}

	public synchronized boolean keyDown (int keycode) {
		queueTime();
		queue.add(KEY_DOWN);
		queue.add(keycode);
		return false;
	}

	public synchronized boolean keyUp (int keycode) {
		queueTime();
		queue.add(KEY_UP);
		queue.add(keycode);
		return false;
	}

	public synchronized boolean keyTyped (char character) {
		queueTime();
		queue.add(KEY_TYPED);
		queue.add(character);
		return false;
	}

	public synchronized boolean touchDown (int screenX, int screenY, int pointer, int button) {
		queueTime();
		queue.add(TOUCH_DOWN);
		queue.add(screenX);
		queue.add(screenY);
		queue.add(pointer);
		queue.add(button);
		return false;
	}

	public synchronized boolean touchUp (int screenX, int screenY, int pointer, int button) {
		queueTime();
		queue.add(TOUCH_UP);
		queue.add(screenX);
		queue.add(screenY);
		queue.add(pointer);
		queue.add(button);
		return false;
	}

	public synchronized boolean touchDragged (int screenX, int screenY, int pointer) {
		queueTime();
		queue.add(TOUCH_DRAGGED);
		queue.add(screenX);
		queue.add(screenY);
		queue.add(pointer);
		return false;
	}

	public synchronized boolean mouseMoved (int screenX, int screenY) {
		queueTime();
		queue.add(MOUSE_MOVED);
		queue.add(screenX);
		queue.add(screenY);
		return false;
	}

	public synchronized boolean scrolled (int amount) {
		queueTime();
		queue.add(SCROLLED);
		queue.add(amount);
		return false;
	}

	public long getCurrentEventTime () {
		return currentEventTime;
	}
}

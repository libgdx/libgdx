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
	static private final int SKIP = -1;
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
		synchronized (this) {
			if (processor == null) {
				queue.clear();
				return;
			}
			processingQueue.addAll(queue);
			queue.clear();
		}
		int[] q = processingQueue.items;
		InputProcessor localProcessor = processor;
		for (int i = 0, n = processingQueue.size; i < n;) {
			int type = q[i++];
			currentEventTime = (long)q[i++] << 32 | q[i++] & 0xFFFFFFFFL;
			switch (type) {
			case SKIP:
				i += q[i];
				break;
			case KEY_DOWN:
				localProcessor.keyDown(q[i++]);
				break;
			case KEY_UP:
				localProcessor.keyUp(q[i++]);
				break;
			case KEY_TYPED:
				localProcessor.keyTyped((char)q[i++]);
				break;
			case TOUCH_DOWN:
				localProcessor.touchDown(q[i++], q[i++], q[i++], q[i++]);
				break;
			case TOUCH_UP:
				localProcessor.touchUp(q[i++], q[i++], q[i++], q[i++]);
				break;
			case TOUCH_DRAGGED:
				localProcessor.touchDragged(q[i++], q[i++], q[i++]);
				break;
			case MOUSE_MOVED:
				localProcessor.mouseMoved(q[i++], q[i++]);
				break;
			case SCROLLED:
				localProcessor.scrolled(q[i++]);
				break;
			default:
				throw new RuntimeException();
			}
		}
		processingQueue.clear();
	}

	private synchronized int next (int nextType, int i) {
		int[] q = queue.items;
		for (int n = queue.size; i < n;) {
			int type = q[i];
			if (type == nextType) return i;
			i += 3;
			switch (type) {
			case SKIP:
				i += q[i];
				break;
			case KEY_DOWN:
				i++;
				break;
			case KEY_UP:
				i++;
				break;
			case KEY_TYPED:
				i++;
				break;
			case TOUCH_DOWN:
				i += 4;
				break;
			case TOUCH_UP:
				i += 4;
				break;
			case TOUCH_DRAGGED:
				i += 3;
				break;
			case MOUSE_MOVED:
				i += 2;
				break;
			case SCROLLED:
				i++;
				break;
			default:
				throw new RuntimeException();
			}
		}
		return -1;
	}

	private void queueTime () {
		long time = TimeUtils.nanoTime();
		queue.add((int)(time >> 32));
		queue.add((int)time);
	}

	public synchronized boolean keyDown (int keycode) {
		queue.add(KEY_DOWN);
		queueTime();
		queue.add(keycode);
		return false;
	}

	public synchronized boolean keyUp (int keycode) {
		queue.add(KEY_UP);
		queueTime();
		queue.add(keycode);
		return false;
	}

	public synchronized boolean keyTyped (char character) {
		queue.add(KEY_TYPED);
		queueTime();
		queue.add(character);
		return false;
	}

	public synchronized boolean touchDown (int screenX, int screenY, int pointer, int button) {
		queue.add(TOUCH_DOWN);
		queueTime();
		queue.add(screenX);
		queue.add(screenY);
		queue.add(pointer);
		queue.add(button);
		return false;
	}

	public synchronized boolean touchUp (int screenX, int screenY, int pointer, int button) {
		queue.add(TOUCH_UP);
		queueTime();
		queue.add(screenX);
		queue.add(screenY);
		queue.add(pointer);
		queue.add(button);
		return false;
	}

	public synchronized boolean touchDragged (int screenX, int screenY, int pointer) {
		// Skip any queued touch dragged events for the same pointer.
		for (int i = next(TOUCH_DRAGGED, 0); i >= 0; i = next(TOUCH_DRAGGED, i + 6)) {
			if (queue.get(i + 5) == pointer) {
				queue.set(i, SKIP);
				queue.set(i + 3, 3);
			}
		}
		queue.add(TOUCH_DRAGGED);
		queueTime();
		queue.add(screenX);
		queue.add(screenY);
		queue.add(pointer);
		return false;
	}

	public synchronized boolean mouseMoved (int screenX, int screenY) {
		// Skip any queued mouse moved events.
		for (int i = next(MOUSE_MOVED, 0); i >= 0; i = next(MOUSE_MOVED, i + 5)) {
			queue.set(i, SKIP);
			queue.set(i + 3, 2);
		}
		queue.add(MOUSE_MOVED);
		queueTime();
		queue.add(screenX);
		queue.add(screenY);
		return false;
	}

	public synchronized boolean scrolled (int amount) {
		queue.add(SCROLLED);
		queueTime();
		queue.add(amount);
		return false;
	}

	public long getCurrentEventTime () {
		return currentEventTime;
	}
}

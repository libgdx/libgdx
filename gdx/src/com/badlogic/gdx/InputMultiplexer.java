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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;

/** An {@link InputProcessor} that delegates to an ordered list of other InputProcessors. Delegation for an event stops if a
 * processor returns true, which indicates that the event was handled.
 * @author Nathan Sweet */
public class InputMultiplexer implements InputProcessor {
	private SnapshotArray<InputProcessor> processors = new SnapshotArray(4);

	public InputMultiplexer () {
	}

	public InputMultiplexer (InputProcessor... processors) {
		this.processors.addAll(processors);
	}

	public void addProcessor (int index, InputProcessor processor) {
		if (processor == null) throw new NullPointerException("processor cannot be null");
		processors.insert(index, processor);
	}

	public void removeProcessor (int index) {
		processors.removeIndex(index);
	}

	public void addProcessor (InputProcessor processor) {
		if (processor == null) throw new NullPointerException("processor cannot be null");
		processors.add(processor);
	}

	public void removeProcessor (InputProcessor processor) {
		processors.removeValue(processor, true);
	}

	/** @return the number of processors in this multiplexer */
	public int size () {
		return processors.size;
	}

	public void clear () {
		processors.clear();
	}

	public void setProcessors (InputProcessor... processors) {
		this.processors.clear();
		this.processors.addAll(processors);
	}

	public void setProcessors (Array<InputProcessor> processors) {
		this.processors.clear();
		this.processors.addAll(processors);
	}

	public SnapshotArray<InputProcessor> getProcessors () {
		return processors;
	}

	public boolean keyDown (int keycode) {
		Object[] items = processors.begin();
		try {
			for (int i = 0, n = processors.size; i < n; i++)
				if (((InputProcessor)items[i]).keyDown(keycode)) return true;
		} finally {
			processors.end();
		}
		return false;
	}

	public boolean keyUp (int keycode) {
		Object[] items = processors.begin();
		try {
			for (int i = 0, n = processors.size; i < n; i++)
				if (((InputProcessor)items[i]).keyUp(keycode)) return true;
		} finally {
			processors.end();
		}
		return false;
	}

	public boolean keyTyped (char character) {
		Object[] items = processors.begin();
		try {
			for (int i = 0, n = processors.size; i < n; i++)
				if (((InputProcessor)items[i]).keyTyped(character)) return true;
		} finally {
			processors.end();
		}
		return false;
	}

	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		Object[] items = processors.begin();
		try {
			for (int i = 0, n = processors.size; i < n; i++)
				if (((InputProcessor)items[i]).touchDown(screenX, screenY, pointer, button)) return true;
		} finally {
			processors.end();
		}
		return false;
	}

	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		Object[] items = processors.begin();
		try {
			for (int i = 0, n = processors.size; i < n; i++)
				if (((InputProcessor)items[i]).touchUp(screenX, screenY, pointer, button)) return true;
		} finally {
			processors.end();
		}
		return false;
	}

	public boolean touchDragged (int screenX, int screenY, int pointer) {
		Object[] items = processors.begin();
		try {
			for (int i = 0, n = processors.size; i < n; i++)
				if (((InputProcessor)items[i]).touchDragged(screenX, screenY, pointer)) return true;
		} finally {
			processors.end();
		}
		return false;
	}

	public boolean mouseMoved (int screenX, int screenY) {
		Object[] items = processors.begin();
		try {
			for (int i = 0, n = processors.size; i < n; i++)
				if (((InputProcessor)items[i]).mouseMoved(screenX, screenY)) return true;
		} finally {
			processors.end();
		}
		return false;
	}

	public boolean scrolled (int amount) {
		Object[] items = processors.begin();
		try {
			for (int i = 0, n = processors.size; i < n; i++)
				if (((InputProcessor)items[i]).scrolled(amount)) return true;
		} finally {
			processors.end();
		}
		return false;
	}
}

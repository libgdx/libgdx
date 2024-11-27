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

	/** Constructor of an InputMultiplexer */
	public InputMultiplexer () {
	}

	/** Constructor of an InputMultiplexer with processors
	 * 
	 * @param processors */
	public InputMultiplexer (InputProcessor... processors) {
		this.processors.addAll(processors);
	}

	/** Add a processor to the list of processors with a given index
	 * 
	 * @param index
	 * @param processor */
	public void addProcessor (int index, InputProcessor processor) {
		if (processor == null) throw new NullPointerException("processor cannot be null");
		processors.insert(index, processor);
	}

	/** Remove a processor with a given index from the list of processors
	 * 
	 * @param index */
	public void removeProcessor (int index) {
		processors.removeIndex(index);
	}

	/** Add a processor to the list of processors at the end of the list
	 * 
	 * @param processor */
	public void addProcessor (InputProcessor processor) {
		if (processor == null) throw new NullPointerException("processor cannot be null");
		processors.add(processor);
	}

	/** Remove a processor from the list of processors
 	 * It is removed by identity not value
	 * 
	 * @param processor */
	public void removeProcessor (InputProcessor processor) {
		processors.removeValue(processor, true);
	}

	/** @return the number of processors in this multiplexer */
	public int size () {
		return processors.size;
	}

	/** Empty the list of processors */
	public void clear () {
		processors.clear();
	}

	/**
	 * Replaces the current processors with a new set of {@link InputProcessor} instances.
	 * This method clears the existing processors and adds all provided processors.
	 * 
	 * @param processors one or more {@link InputProcessor} instances to set, or none
	 *                   to clear the current processors.
	 *                   Accepts varargs or an array.
 	*/
	public void setProcessors (InputProcessor... processors) {
		this.processors.clear();
		this.processors.addAll(processors);
	}

	/** Empty the list of processors and implement a new list of processors
	 * 
	 * @param processors */
	public void setProcessors (Array<InputProcessor> processors) {
		this.processors.clear();
		this.processors.addAll(processors);
	}

	
	@Override
	public SnapshotArray<InputProcessor> getProcessors () {
		return processors;
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
	public boolean touchCancelled (int screenX, int screenY, int pointer, int button) {
		Object[] items = processors.begin();
		try {
			for (int i = 0, n = processors.size; i < n; i++)
				if (((InputProcessor)items[i]).touchCancelled(screenX, screenY, pointer, button)) return true;
		} finally {
			processors.end();
		}
		return false;
	}

	@Override
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

	@Override
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

	@Override
	public boolean scrolled (float amountX, float amountY) {
		Object[] items = processors.begin();
		try {
			for (int i = 0, n = processors.size; i < n; i++)
				if (((InputProcessor)items[i]).scrolled(amountX, amountY)) return true;
		} finally {
			processors.end();
		}
		return false;
	}
}

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

package com.badlogic.gdx.ai.fsm;

import com.badlogic.gdx.utils.Array;

/** A {@link StateMachine} implementation that keeps track of all previous {@link State}s via a stack. This makes sense for example
 * in case of a hierarchical menu structure where each menu screen is one state and one wants to navigate back to the main menu
 * anytime, via {@link #revertToPreviousState()}.
 * @param <E> is the type of the entity handled by this state machine before the author.
 * @author Daniel Holderbaum */
public class StackStateMachine<E> extends DefaultStateMachine<E> {

	private Array<State<E>> stateStack;

	/** Creates a StackStateMachine for the specified owner.
	 * @param owner the owner of the state machine */
	public StackStateMachine (E owner) {
		this(owner, null, null);
	}

	/** Creates a StackStateMachine for the specified owner and initial state.
	 * @param owner the owner of the state machine
	 * @param initialState the initial state */
	public StackStateMachine (E owner, State<E> initialState) {
		this(owner, initialState, null);
	}

	/** Creates a StackStateMachine for the specified owner, initial state and global state.
	 * @param owner the owner of the state machine
	 * @param initialState the initial state
	 * @param globalState the global state */
	public StackStateMachine (E owner, State<E> initialState, State<E> globalState) {
		super(owner, initialState, globalState);
	}

	@Override
	public void setInitialState (State<E> state) {
		if (stateStack == null) {
			stateStack = new Array<State<E>>();
		}

		this.stateStack.clear();
		this.currentState = state;
	}

	@Override
	public State<E> getCurrentState () {
		return currentState;
	}

	/** Returns the last state of this state machine. That is the high-most state on the internal stack of previous states. */
	@Override
	public State<E> getPreviousState () {
		if (stateStack.size == 0) {
			return null;
		} else {
			return stateStack.peek();
		}
	}

	@Override
	public void changeState (State<E> newState) {
		changeState(newState, true);
	}

	/** Change state back to the previous state. That is the high-most state on the internal stack of previous states. */
	@Override
	public boolean revertToPreviousState () {
		if (stateStack.size == 0) {
			return false;
		}

		State<E> previousState = stateStack.pop();
		changeState(previousState, false);
		return true;
	}

	private void changeState (State<E> newState, boolean pushCurrentStateToStack) {
		if (pushCurrentStateToStack) {
			stateStack.add(currentState);
		}

		// Call the exit method of the existing state
		currentState.exit(owner);

		// change state to the new state
		currentState = newState;

		// call the entry method of the new state
		currentState.enter(owner);
	}

}

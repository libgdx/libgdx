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

import com.badlogic.gdx.ai.msg.Telegram;

/** Default implementation of the {@link StateMachine} interface.
 * @author davebaol */
public class DefaultStateMachine<E> implements StateMachine<E> {

	/** The entity that owns this state machine. */
	private E owner;

	/** The current state the owner is in. */
	private State<E> currentState;

	/** The last state the owner was in. */
	private State<E> previousState;

	/** The global state of the owner. Its logic is called every time the FSM is updated. */
	private State<E> globalState;

	/** Creates a DefaultStateMachine for the specified owner.
	 * @param owner the owner of the state machine */
	public DefaultStateMachine (E owner) {
		this(owner, null, null);
	}

	/** Creates a DefaultStateMachine for the specified owner and initial state.
	 * @param owner the owner of the state machine
	 * @param initialState the initial state */
	public DefaultStateMachine (E owner, State<E> initialState) {
		this(owner, initialState, null);
	}

	/** Creates a DefaultStateMachine for the specified owner, initial state and global state.
	 * @param owner the owner of the state machine
	 * @param initialState the initial state
	 * @param globalState the global state */
	public DefaultStateMachine (E owner, State<E> initialState, State<E> globalState) {
		this.owner = owner;
		this.setInitialState(initialState);
		this.setGlobalState(globalState);
	}

	@Override
	public void setInitialState (State<E> state) {
		this.previousState = null;
		this.currentState = state;
	}

	@Override
	public void setGlobalState (State<E> state) {
		this.globalState = state;
	}

	@Override
	public State<E> getCurrentState () {
		return currentState;
	}

	@Override
	public State<E> getGlobalState () {
		return globalState;
	}

	@Override
	public State<E> getPreviousState () {
		return previousState;
	}

	/** Updates the state machine by invoking first the {@code execute} method of the global state (if any) then the {@code execute}
	 * method of the current state. */
	@Override
	public void update () {
		// Execute the global state (if any)
		if (globalState != null) globalState.update(owner);

		// Execute the current state (if any)
		if (currentState != null) currentState.update(owner);
	}

	@Override
	public void changeState (State<E> newState) {

		// Keep a record of the previous state
		previousState = currentState;

		// Call the exit method of the existing state
		currentState.exit(owner);

		// change state to the new state
		currentState = newState;

		// call the entry method of the new state
		currentState.enter(owner);
	}

	@Override
	public void revertToPreviousState () {
		changeState(previousState);
	}

	/** Indicates whether the state machine is in the given state.
	 * <p>
	 * This implementation assumes states are singletons (typically a enum) so they are compared with the {@code ==} operator
	 * instead of the {@code equals} method.
	 * 
	 * @param state the state to be compared with the current state
	 * @returns true if the current state's type is equal to the type of the class passed as a parameter. */
	@Override
	public boolean isInState (State<E> state) {
		return currentState == state;
	}

	/** Handles received telegrams. The telegram is first routed to the current state. If the current state does not deal with the
	 * message, it's routed to the global state's message handler.
	 * 
	 * @param telegram the received telegram
	 * @returns true if telegram has been successfully handled; false otherwise. */
	@Override
	public boolean handleMessage (Telegram telegram) {

		// First see if the current state is valid and that it can handle the message
		if (currentState != null && currentState.onMessage(telegram)) {
			return true;
		}

		// If not, and if a global state has been implemented, send
		// the message to the global state
		if (globalState != null && globalState.onMessage(telegram)) {
			return true;
		}

		return false;
	}
}

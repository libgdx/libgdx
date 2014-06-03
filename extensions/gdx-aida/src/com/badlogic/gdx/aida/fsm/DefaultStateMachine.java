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

package com.badlogic.gdx.aida.fsm;

import com.badlogic.gdx.aida.msg.Telegram;

/**
 * @author davebaol
 */
public class DefaultStateMachine<A> implements StateMachine<A> {

	// The agent that owns this state machine
	private A owner;

	private State<A> currentState;
	// a record of the last state the agent was in
	private State<A> previousState;
	// this state logic is called every time the FSM is updated
	private State<A> globalState;

	public DefaultStateMachine(A owner) {
		this(owner, null, null);
	}

	public DefaultStateMachine(A owner, State<A> initialState) {
		this(owner, initialState, null);
	}

	public DefaultStateMachine(A owner, State<A> initialState,
			State<A> globalState) {
		this.owner = owner;
		this.setInitialState(initialState);
		this.setGlobalState(globalState);
	}

	@Override
	public void setInitialState(State<A> state) {
		this.previousState = null;
		this.currentState = state;
	}

	@Override
	public void setGlobalState(State<A> state) {
		this.globalState = state;
	}

	@Override
	public State<A> getCurrentState() {
		return currentState;
	}

	public State<A> getGlobalState() {
		return globalState;
	}

	public State<A> getPreviousState() {
		return previousState;
	}

	public void setPreviousState(State<A> state) {
		this.previousState = state;
	}

	// call this to update the FSM
	@Override
	public void update() {
		// Execute the global state (if any)
		if (globalState != null)
			globalState.execute(owner);

		// Execute the current state (if any)
		if (currentState != null)
			currentState.execute(owner);
	}

	/**
	 * Performs a transition to the specified state.
	 * 
	 * @param newState
	 *            the state to transition to
	 */
	@Override
	public void changeState(State<A> newState) {

		// Keep a record of the previous state
		previousState = currentState;

		// Call the exit method of the existing state
		currentState.exit(owner);

		// change state to the new state
		currentState = newState;

		// call the entry method of the new state
		currentState.enter(owner);
	}

	/**
	 * Change state back to the previous state.
	 */
	@Override
	public void revertToPreviousState() {
		changeState(previousState);
	}

	/**
	 * Indicates whether the state machine is in the given state.
	 * <p>
	 * This implementation assumes states are singletons (typically a enum) so
	 * they are compared with the {@code ==} operator instead of the
	 * {@code equals} method.
	 * 
	 * @param state
	 *            the state to be compared with the current state
	 * @returns true if the current state's type is equal to the type of the
	 *          class passed as a parameter.
	 */
	@Override
	public boolean isInState(State<A> state) {
		return currentState == state;
	}

	/**
	 * Handles received telegrams. The telegram is first routed to the current
	 * state. If the current state does not deal with the message, it's routed
	 * to the global state's message handler.
	 * 
	 * @param telegram
	 *            the received telegram
	 * @returns true if telegram has been successfully handled; false otherwise.
	 */
	@Override
	public boolean handleMessage(Telegram telegram) {

		// First see if the current state is valid and that it can handle the
		// message
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

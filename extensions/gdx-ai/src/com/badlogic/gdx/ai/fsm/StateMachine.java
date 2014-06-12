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

/** A state machine manages the state transitions of its entity. Additionally, the state machine may be delegated by the entity to
 * handle its messages.
 * 
 * @author davebaol */
public interface StateMachine<E> {

	/** Updates the state machine.
	 * <p>
	 * Implementation classes should invoke first the {@code execute} method of the global state (if any) then the {@code execute}
	 * method of the current state. */
	public void update ();

	/** Performs a transition to the specified state.
	 * 
	 * @param newState the state to transition to */
	public void changeState (State<E> newState);

	/** Change state back to the previous state. */
	public void revertToPreviousState ();

	/** Sets the initial state of this state machine.
	 * @param state the initial state. */
	public void setInitialState (State<E> state);

	/** Sets the global state of this state machine.
	 * @param state the global state. */
	public void setGlobalState (State<E> state);

	/** Returns the current state of this state machine. */
	public State<E> getCurrentState ();

	/** Returns the global state of this state machine.
	 * <p>
	 * Implementation classes should invoke the {@code execute} method of the global state every time the FSM is updated. Also,
	 * they should never invoke its {@code enter} and {@code exit} method. */
	public State<E> getGlobalState ();

	/** Returns the last state of this state machine. */
	public State<E> getPreviousState ();

	/** Indicates whether the state machine is in the given state.
	 * 
	 * @param state the state to be compared with the current state
	 * @returns true if the current state's type is equal to the type of the class passed as a parameter. */
	public boolean isInState (State<E> state);

	/** Handles received telegrams.
	 * <p>
	 * Implementation classes should first route the telegram to the current state. If the current state does not deal with the
	 * message, it should be routed to the global state.
	 * @param telegram the received telegram
	 * @returns true if telegram has been successfully handled; false otherwise. */
	public boolean handleMessage (Telegram telegram);
}

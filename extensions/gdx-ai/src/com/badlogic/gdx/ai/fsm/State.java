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

/** The state of a state machine defines the logic of the entities that enter, exit and last this state. Additionally, a state may
 * be delegated by an entity to handle its messages.
 * 
 * @param <E> is the type of the entity handled by this state machine
 * 
 * @author davebaol */
public interface State<E> {

	/** This method will execute when the state is entered.
	 * 
	 * @param entity the entity entering the state */
	public void enter (E entity);

	/** This is the state's normal update function
	 * 
	 * @param entity the entity lasting the state */
	public void update (E entity);

	/** This method will execute when the state is exited.
	 * 
	 * @param entity the entity exiting the state */
	public void exit (E entity);

	/** This method executes if the {@code entity} receives a {@code telegram} from the message dispatcher while it is in this
	 * state.
	 * 
	 * @param entity the entity that received the message
	 * @param telegram the message sent to the entity
	 * @return true if the message has been successfully handled; false otherwise. */
	public boolean onMessage (E entity, Telegram telegram);
}

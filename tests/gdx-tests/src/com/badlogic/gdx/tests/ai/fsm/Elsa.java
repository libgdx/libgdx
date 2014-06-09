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

package com.badlogic.gdx.tests.ai.fsm;

import com.badlogic.gdx.ai.Agent;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;

/** @author davebaol */
public class Elsa implements Agent {

	// an instance of the state machine class
	private StateMachine<Elsa> stateMachine;

	// is she presently cooking?
	boolean cooking;

	Bob bob;

	public Elsa () {
		this(null);
	}

	public Elsa (Bob bob) {
		stateMachine = new DefaultStateMachine<Elsa>(this, ElsaState.DO_HOUSE_WORK, ElsaState.GLOBAL_STATE);
		this.bob = bob;
	}

	@Override
	public boolean handleMessage (Telegram msg) {
		return stateMachine.handleMessage(msg);
	}

	@Override
	public void update (float delta) {
		stateMachine.update();
	}

	public StateMachine<Elsa> getStateMachine () {
		return stateMachine;
	}

	public Bob getBob () {
		return bob;
	}

	public void setBob (Bob bob) {
		this.bob = bob;
	}

	public boolean isCooking () {
		return cooking;
	}

	public void setCooking (boolean cooking) {
		this.cooking = cooking;
	}

}

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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.tests.ai.fsm.Bob;
import com.badlogic.gdx.tests.ai.fsm.Elsa;
import com.badlogic.gdx.tests.utils.GdxTest;

/** A simple test to demonstrate state machines combined with message handling.
 * @author davebaol */
public class StateMachineTest extends GdxTest {

	Bob bob;
	Elsa elsa;
	float elapsedTime;

	@Override
	public void create () {

		elapsedTime = 0;

		// Create Bob and his wife
		bob = new Bob();
		elsa = new Elsa(bob);
		bob.setElsa(elsa);

	}

	@Override
	public void render () {
		elapsedTime += Gdx.graphics.getRawDeltaTime();

		if (elapsedTime > 0.8f) {
			// Update Bob and his wife
			bob.update(elapsedTime);
			elsa.update(elapsedTime);

			// Dispatch any delayed messages
			MessageDispatcher.getInstance().dispatchDelayedMessages();

			elapsedTime = 0;
		}
	}
}

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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.Telegram;

/** @author davebaol */
public enum BobState implements State<Bob> {

	ENTER_MINE_AND_DIG_FOR_NUGGET() {
		@Override
		public void enter (Bob bob) {
			// if the miner is not already located at the goldmine, he must
			// change location to the gold mine
			if (bob.getLocation() != Location.GOLD_MINE) {
				talk(bob, "Walkin' to the goldmine");
				bob.setLocation(Location.GOLD_MINE);
			}
		}

		@Override
		public void update (Bob bob) {
			// Now bob is at the goldmine he digs for gold until he
			// is carrying in excess of MAX_NUGGETS. If he gets thirsty during
			// his digging he packs up work for a while and changes state to
			// go to the saloon for a whiskey.
			bob.addToGoldCarried(1);

			bob.increaseFatigue();

			talk(bob, "Pickin' up a nugget");

			// if enough gold mined, go and put it in the bank
			if (bob.isPocketsFull()) {
				bob.getStateMachine().changeState(VISIT_BANK_AND_DEPOSIT_GOLD);
			}

			if (bob.isThirsty()) {
				bob.getStateMachine().changeState(QUENCH_THIRST);
			}
		}

		@Override
		public void exit (Bob bob) {
			talk(bob, "Ah'm leavin' the goldmine with mah pockets full o' sweet gold");
		}
	},

	GO_HOME_AND_SLEEP_TILL_RESTED() {
		@Override
		public void enter (Bob bob) {
			if (bob.getLocation() != Location.SHACK) {
				talk(bob, "Walkin' home");
				bob.setLocation(Location.SHACK);

				// Let Elsa know I'm home
				MessageDispatcher.getInstance().dispatchMessage( //
					0.0f, // time delay
					bob, // ID of sender
					bob.elsa, // ID of recipient
					MessageType.HI_HONEY_I_M_HOME, // the message
					null);
			}
		}

		@Override
		public void update (Bob bob) {
			// if miner is not fatigued start to dig for nuggets again.
			if (!bob.isFatigued()) {
				talk(bob, "All mah fatigue has drained away. Time to find more gold!");

				bob.getStateMachine().changeState(ENTER_MINE_AND_DIG_FOR_NUGGET);
			} else {
				// sleep
				bob.decreaseFatigue();
				talk(bob, "ZZZZ... ");
			}
		}

		@Override
		public void exit (Bob bob) {
		}

		@Override
		public boolean onMessage (Telegram telegram) {
			if (telegram.message == MessageType.STEW_READY) {
				Bob bob = (Bob)(telegram.receiver);

				System.out.println("Message handled by " + bob.getClass().getSimpleName() + " at time: "
					+ MessageDispatcher.getCurrentTime());

				talk(bob, "Okay Hun, ahm a comin'!");

				bob.getStateMachine().changeState(EAT_STEW);

				return true;

			}

			return false; // send message to global message handler
		}
	},

	QUENCH_THIRST() {
		@Override
		public void enter (Bob bob) {
			if (bob.getLocation() != Location.SALOON) {
				bob.setLocation(Location.SALOON);

				talk(bob, "Boy, ah sure is thusty! Walking to the saloon");
			}
		}

		@Override
		public void update (Bob bob) {
			bob.buyAndDrinkAWhiskey();

			talk(bob, "That's mighty fine sippin liquer");

			bob.getStateMachine().changeState(ENTER_MINE_AND_DIG_FOR_NUGGET);
		}

		@Override
		public void exit (Bob bob) {
			talk(bob, "Leaving the saloon, feelin' good");
		}
	},

	VISIT_BANK_AND_DEPOSIT_GOLD() {
		@Override
		public void enter (Bob bob) {
			// On entry bob makes sure he is located at the bank
			if (bob.getLocation() != Location.BANK) {
				talk(bob, "Goin' to the bank. Yes siree");

				bob.setLocation(Location.BANK);
			}
		}

		@Override
		public void update (Bob bob) {
			// Deposit the gold
			bob.addToWealth(bob.getGoldCarried());

			bob.setGoldCarried(0);

			talk(bob, "Depositing gold. Total savings now: " + bob.getWealth());

			// Wealthy enough to have a well earned rest?
			if (bob.getWealth() >= Bob.COMFORT_LEVEL) {
				talk(bob, "WooHoo! Rich enough for now. Back home to mah li'lle lady");

				bob.getStateMachine().changeState(GO_HOME_AND_SLEEP_TILL_RESTED);
			} else { // otherwise get more gold
				bob.getStateMachine().changeState(ENTER_MINE_AND_DIG_FOR_NUGGET);
			}
		}

		@Override
		public void exit (Bob bob) {
			talk(bob, "Leavin' the bank");
		}
	},

	EAT_STEW() {
		@Override
		public void enter (Bob bob) {
			talk(bob, "Smells Reaaal goood Elsa!");
		}

		@Override
		public void update (Bob bob) {
			talk(bob, "Tastes real good too!");
			bob.getStateMachine().revertToPreviousState();
		}

		@Override
		public void exit (Bob bob) {
			talk(bob, "Thankya li'lle lady. Ah better get back to whatever ah wuz doin'");
		}
	};

	@Override
	public boolean onMessage (Telegram telegram) {
		return false;
	}

	protected void talk (Bob bob, String msg) {
		Gdx.app.log(bob.getClass().getSimpleName(), msg);
	}

}

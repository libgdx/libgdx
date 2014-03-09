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

package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.utils.Array;

public class ContactCallbackTest2 extends BaseBulletTest {
	public static class TestContactListener extends ContactListener {
		public Array<BulletEntity> entities;

		@Override
		public void onContactStarted (int userValue0, boolean match0, int userValue1, boolean match1) {
			if (match0) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue0));
				e.setColor(Color.RED);
				Gdx.app.log("ContactCallbackTest", "Contact started " + userValue0);
			}
			if (match1) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue1));
				e.setColor(Color.RED);
				Gdx.app.log("ContactCallbackTest", "Contact started " + userValue1);
			}
		}

		@Override
		public void onContactEnded (int userValue0, boolean match0, int userValue1, boolean match1) {
			if (match0) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue0));
				e.setColor(Color.BLUE);
				Gdx.app.log("ContactCallbackTest", "Contact ended " + userValue0);
			}
			if (match1) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue1));
				e.setColor(Color.BLUE);
				Gdx.app.log("ContactCallbackTest", "Contact ended " + userValue1);
			}
		}
	}

	final int BOXCOUNT_X = 5;
	final int BOXCOUNT_Y = 1;
	final int BOXCOUNT_Z = 5;

	final float BOXOFFSET_X = -5f;
	final float BOXOFFSET_Y = 0.5f;
	final float BOXOFFSET_Z = -5f;

	TestContactListener contactListener;

	@Override
	public void create () {
		super.create();

		// Create the entities
		world.add("ground", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		for (int x = 0; x < BOXCOUNT_X; x++) {
			for (int y = 0; y < BOXCOUNT_Y; y++) {
				for (int z = 0; z < BOXCOUNT_Z; z++) {
					final BulletEntity e = (BulletEntity)world.add("box", BOXOFFSET_X + x * 2f, BOXOFFSET_Y + y * 2f, BOXOFFSET_Z + z
						* 2f);
					e.setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
						0.5f + 0.5f * (float)Math.random(), 1f);

					e.body.setContactCallbackFlag(2);
					e.body.setContactCallbackFilter(2);
				}
			}
		}

		// Creating a contact listener, also enables that particular type of contact listener and sets it active.
		contactListener = new TestContactListener();
		contactListener.entities = world.entities;
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}

	@Override
	public void dispose () {
		// Deleting the active contact listener, also disables that particular type of contact listener.
		if (contactListener != null) contactListener.dispose();
		contactListener = null;

		super.dispose();
	}
}

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

package com.badlogic.gdx.scenes.scene2d.actions;

/** Moves an actor from its current position to a specific position.
 * @author Nathan Sweet */
public class MoveToAction extends TemporalAction {
	private float startX, startY;
	private float endX, endY;

	protected void begin () {
		startX = actor.getX();
		startY = actor.getY();
	}

	protected void update (float percent) {
		actor.setPosition(startX + (endX - startX) * percent, startY + (endY - startY) * percent);
	}

	public void setPosition (float x, float y) {
		endX = x;
		endY = y;
	}

	public float getX () {
		return endX;
	}

	public void setX (float x) {
		endX = x;
	}

	public float getY () {
		return endY;
	}

	public void setY (float y) {
		endY = y;
	}
}

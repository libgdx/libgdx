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

 
package com.dozingcatsoftware.bouncy;

import com.badlogic.gdx.physics.box2d.Body;
import com.dozingcatsoftware.bouncy.elements.DropTargetGroupElement;
import com.dozingcatsoftware.bouncy.elements.FieldElement;
import com.dozingcatsoftware.bouncy.elements.RolloverGroupElement;
import com.dozingcatsoftware.bouncy.elements.SensorElement;

/** This class implements the Field.Delegate interface and does nothing for each of the interface methods. Real delegates can
 * subclass this class to avoid having to create empty implementations for events they don't care about. If a field definition
 * doesn't specify a delegate class, an instance of this class will be used as a placeholder delegate.
 * @author brian */
public class BaseFieldDelegate implements Field.Delegate {

	@Override
	public void allDropTargetsInGroupHit (Field field, DropTargetGroupElement targetGroup) {
	}

	@Override
	public void allRolloversInGroupActivated (Field field, RolloverGroupElement rolloverGroup) {
	}

	@Override
	public void flipperActivated (Field field) {
	}

	@Override
	public void processCollision (Field field, FieldElement element, Body hitBody, Body ball) {
	}

	@Override
	public void gameStarted (Field field) {
	}

	@Override
	public void ballLost (Field field) {
	}

	@Override
	public void gameEnded (Field field) {
	}

	@Override
	public void tick (Field field, long msecs) {
	}

	@Override
	public void ballInSensorRange (Field field, SensorElement sensor) {
	}
}

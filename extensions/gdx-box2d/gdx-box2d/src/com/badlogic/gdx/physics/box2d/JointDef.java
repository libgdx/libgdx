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

package com.badlogic.gdx.physics.box2d;

public class JointDef {
	public enum JointType {
		Unknown(0), RevoluteJoint(1), PrismaticJoint(2), DistanceJoint(3), PulleyJoint(4), MouseJoint(5), GearJoint(6), WheelJoint(
			7), WeldJoint(8), FrictionJoint(9), RopeJoint(10);

		public static JointType[] valueTypes = new JointType[] {Unknown, RevoluteJoint, PrismaticJoint, DistanceJoint, PulleyJoint,
			MouseJoint, GearJoint, WheelJoint, WeldJoint, FrictionJoint, RopeJoint};
		private int value;

		JointType (int value) {
			this.value = value;
		}

		public int getValue () {
			return value;
		}
	}

	/** The joint type is set automatically for concrete joint types. **/
	public JointType type = JointType.Unknown;

	/** The first attached body. **/
	public Body bodyA = null;

	/** The second attached body **/
	public Body bodyB = null;

	/** Set this flag to true if the attached bodies should collide. **/
	public boolean collideConnected = false;
}

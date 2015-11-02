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

/** Base class for actions that transition over time using the percent complete since the last frame.
 * @author Nathan Sweet */
abstract public class RelativeTemporalAction extends TemporalAction {
	private float lastPercent;
	private float lastPercent1;
	private float lastPercent2;
	private float lastPercent3;

	protected void begin () {
		lastPercent = 0;
		lastPercent1 = 0;
		lastPercent2 = 0;
		lastPercent3 = 0;
	}

	protected void update (float percent) {
		updateRelative(percent - lastPercent);
		lastPercent = percent;
	}
	
	protected void updateIndependently (float percent0, float percent1, float percent2, float percent3){
		updateRelativeIndependently(percent0 - lastPercent, percent1 - lastPercent1, percent2 - lastPercent2, percent3 - lastPercent3);
		lastPercent = percent0;
		lastPercent1 = percent1;
		lastPercent2 = percent2;
		lastPercent3 = percent3;
	};

	abstract protected void updateRelative (float percentDelta);
	protected void updateRelativeIndependently (float percentDelta0, float percentDelta1, float percentDelta2, float percentDelta3){};
}

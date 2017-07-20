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

package com.badlogic.gdx.tools.flame;

import com.badlogic.gdx.graphics.g3d.particles.influencers.Scale2Influencer;

/** @author Inferno
 * @author Pieter Schaap - P_je@hotmail.com */
public class Scale2InfluencerPanel extends InfluencerPanel<Scale2Influencer> {

	ScaledNumericPanel scalePanelX, scalePanelY;

	public Scale2InfluencerPanel (FlameMain editor, Scale2Influencer influencer) {
		super(editor, influencer, "Scale2 Influencer", "Particle scale X and Y, in world units.");
		setValue(influencer);
	}

	@Override
	public void setValue (Scale2Influencer value) {
		super.setValue(value);
		if (value == null) return;
		scalePanelX.setValue(value.valueX);
		scalePanelY.setValue(value.valueY);
	}

	@Override
	protected void initializeComponents () {
		super.initializeComponents();
		int i = 0;
		addContent(i++, 0, scalePanelX = new ScaledNumericPanel(editor, null, "Life", "Scale X", ""));
		addContent(i++, 0, scalePanelY = new ScaledNumericPanel(editor, null, "Life", "Scale Y", ""));
		scalePanelX.setIsAlwayShown(true);
		scalePanelY.setIsAlwayShown(true);
	}

}

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

package com.badlogic.gdx.scenes.scene2d;

import java.util.ArrayList;

/** A base class for composite actions which deals with multiple child {@link Action}.
 * 
 * @author Moritz Post <moritzpost@gmail.com> */
public abstract class CompositeAction extends Action {

	protected final ArrayList<Action> actions = new ArrayList<Action>();

	/** Gets all target {@link Action}s which are affected by the composite action.
	 * 
	 * @return the {@link Action}s orchestrated by this {@link CompositeAction} */
	public ArrayList<Action> getActions () {
		return actions;
	}

}

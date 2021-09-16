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

package com.badlogic.gdx.graphics.g3d.model.data;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ModelNodeAnimation {
	/** the id of the node animated by this animation FIXME should be nodeId **/
	public String nodeId;
	/** the keyframes, defining the translation of a node for a specific timestamp **/
	public Array<ModelNodeKeyframe<Vector3>> translation;
	/** the keyframes, defining the rotation of a node for a specific timestamp **/
	public Array<ModelNodeKeyframe<Quaternion>> rotation;
	/** the keyframes, defining the scaling of a node for a specific timestamp **/
	public Array<ModelNodeKeyframe<Vector3>> scaling;
}
